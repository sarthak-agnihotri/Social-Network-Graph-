package com.socialgraph.server;

import com.sun.net.httpserver.HttpServer;
import com.sun.net.httpserver.HttpHandler;
import com.sun.net.httpserver.HttpExchange;

import com.socialgraph.graph.Graph;
import com.socialgraph.model.User;

import java.io.IOException;
import java.io.OutputStream;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.BufferedReader;
import java.net.InetSocketAddress;
import java.util.*;
import java.nio.charset.StandardCharsets;

public class SimpleHttpServer {
    private static volatile Graph graph = new Graph();
    
    // 🔥 DYNAMIC PORT - Render ke environment variable se lega
    private static int getPort() {
        String portEnv = System.getenv("PORT");
        if (portEnv != null && !portEnv.isEmpty()) {
            try {
                return Integer.parseInt(portEnv);
            } catch (NumberFormatException e) {
                System.out.println("❌ Invalid PORT value: " + portEnv + ", using default 8000");
            }
        }
        System.out.println("✅ Using default port 8000");
        return 8000; // default fallback
    }

    public static void main(String[] args) throws IOException {
        // 🔥 PORT dynamically set ho raha hai
        int port = getPort();
        HttpServer server = HttpServer.create(new InetSocketAddress(port), 0);

        // API Endpoints define kar rahe hain
        server.createContext("/api/user", new UserHandler());
        server.createContext("/api/friendship", new FriendshipHandler());
        server.createContext("/api/graph", new GraphHandler());
        server.createContext("/api/bfs", new BFSHandler());
        server.createContext("/api/dfs", new DFSHandler());
        server.createContext("/api/shortest-path", new ShortestPathHandler());
        server.createContext("/api/reset", new ResetHandler());

        server.setExecutor(null); // Default executor
        System.out.println("\n🚀 Server started successfully on port " + port);
        System.out.println("📌 API Endpoints:");
        System.out.println("   POST /api/user");
        System.out.println("   POST /api/friendship");
        System.out.println("   GET  /api/graph");
        System.out.println("   POST /api/bfs");
        System.out.println("   POST /api/dfs");
        System.out.println("   POST /api/shortest-path");
        System.out.println("   POST /api/reset\n");
        server.start();
    }

    // --- Utility Methods ---

    private static void sendResponse(HttpExchange exchange, int statusCode, String response) throws IOException {
        // Add CORS headers
        exchange.getResponseHeaders().add("Access-Control-Allow-Origin", "*");
        exchange.getResponseHeaders().add("Access-Control-Allow-Methods", "GET, POST, OPTIONS, DELETE");
        exchange.getResponseHeaders().add("Access-Control-Allow-Headers", "Content-Type");

        if (exchange.getRequestMethod().equalsIgnoreCase("OPTIONS")) {
            exchange.sendResponseHeaders(204, -1);
            return;
        }

        byte[] responseBytes = response.getBytes(StandardCharsets.UTF_8);
        exchange.sendResponseHeaders(statusCode, responseBytes.length);
        OutputStream os = exchange.getResponseBody();
        os.write(responseBytes);
        os.close();
    }

    private static String readRequestBody(HttpExchange exchange) throws IOException {
        InputStream is = exchange.getRequestBody();
        StringBuilder sb = new StringBuilder();
        try (BufferedReader br = new BufferedReader(new InputStreamReader(is, StandardCharsets.UTF_8))) {
            String line;
            while ((line = br.readLine()) != null) {
                sb.append(line);
            }
        }
        return sb.toString();
    }
    
    // Very basic JSON parser for {"key": "value"}
    private static Map<String, String> parseSimpleJson(String json) {
        Map<String, String> map = new HashMap<>();
        json = json.trim().replace("{", "").replace("}", "").replace("\"", "");
        String[] pairs = json.split(",");
        for (String pair : pairs) {
            String[] keyValue = pair.split(":");
            if (keyValue.length == 2) {
                map.put(keyValue[0].trim(), keyValue[1].trim());
            }
        }
        return map;
    }

    // --- Handlers ---

    static class ResetHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod()) || "DELETE".equals(exchange.getRequestMethod())) {
                 graph = new Graph(); // Replace with fresh graph
                 sendResponse(exchange, 200, "{\"message\": \"Graph cleared\"}");
            } else if ("OPTIONS".equals(exchange.getRequestMethod())) {
                 sendResponse(exchange, 204, "");
            } else {
                 sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }

    static class UserHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("POST".equals(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                Map<String, String> data = parseSimpleJson(body);
                String id = data.get("id");
                String name = data.get("name");
                String email = data.get("email");

                if (id != null && name != null && email != null) {
                    graph.addUser(new User(id, name, email));
                    sendResponse(exchange, 200, "{\"message\": \"User added\"}");
                } else {
                    sendResponse(exchange, 400, "{\"error\": \"Invalid data\"}");
                }
            } else {
                 sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }

    static class FriendshipHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
             if ("POST".equals(exchange.getRequestMethod())) {
                String body = readRequestBody(exchange);
                Map<String, String> data = parseSimpleJson(body);
                String id1 = data.get("sourceId");
                String id2 = data.get("targetId");

                User u1 = findUserById(id1);
                User u2 = findUserById(id2);

                if (u1 != null && u2 != null) {
                    graph.addFriendship(u1, u2);
                    sendResponse(exchange, 200, "{\"message\": \"Friendship added\"}");
                } else {
                    sendResponse(exchange, 404, "{\"error\": \"User not found\"}");
                }
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }

    static class GraphHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
            if ("GET".equals(exchange.getRequestMethod())) {
                // Manually build JSON for the graph
                StringBuilder json = new StringBuilder();
                json.append("{ \"nodes\": [");
                
                Set<User> users = graph.getAllUsers();
                int i = 0;
                for (User u : users) {
                    json.append(String.format("{\"id\": \"%s\", \"name\": \"%s\", \"email\": \"%s\"}", u.getId(), u.getName(), u.getEmail()));
                    if (i < users.size() - 1) json.append(",");
                    i++;
                }
                json.append("], \"links\": [");

                // Avoid duplicate edges for undirected graph
                Set<String> addedEdges = new HashSet<>();
                List<String> links = new ArrayList<>();
                
                for (User u : users) {
                    for (User friend : graph.getFriends(u)) {
                        String id1 = u.getId();
                        String id2 = friend.getId();
                        // Sort to avoid duplicates
                        if (id1.compareTo(id2) > 0) {
                            String temp = id1; id1 = id2; id2 = temp;
                        }
                        String edgeKey = id1 + "-" + id2;

                        if (!addedEdges.contains(edgeKey)) {
                            links.add(String.format("{\"source\": \"%s\", \"target\": \"%s\"}", u.getId(), friend.getId()));
                            addedEdges.add(edgeKey);
                        }
                    }
                }
                
                for (int k = 0; k < links.size(); k++) {
                    json.append(links.get(k));
                    if (k < links.size() - 1) json.append(",");
                }

                json.append("]}");
                sendResponse(exchange, 200, json.toString());
            } else {
                sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
            }
        }
    }
    
    static class BFSHandler implements HttpHandler {
         @Override
        public void handle(HttpExchange exchange) throws IOException {
             if ("POST".equals(exchange.getRequestMethod())) {
                 String body = readRequestBody(exchange);
                 Map<String, String> data = parseSimpleJson(body);
                 String startId = data.get("startId");
                 User startUser = findUserById(startId);

                 if (startUser != null) {
                     List<String> result = graph.bfs(startUser);
                     sendResponse(exchange, 200, listToJson(result));
                 } else {
                     sendResponse(exchange, 404, "{\"error\": \"User not found\"}");
                 }
             } else {
                 sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
             }
        }
    }

    static class DFSHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
             if ("POST".equals(exchange.getRequestMethod())) {
                 String body = readRequestBody(exchange);
                 Map<String, String> data = parseSimpleJson(body);
                 String startId = data.get("startId");
                 User startUser = findUserById(startId);

                 if (startUser != null) {
                     List<String> result = graph.dfs(startUser);
                     sendResponse(exchange, 200, listToJson(result));
                 } else {
                     sendResponse(exchange, 404, "{\"error\": \"User not found\"}");
                 }
             } else {
                 sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
             }
        }
    }
    
    static class ShortestPathHandler implements HttpHandler {
        @Override
        public void handle(HttpExchange exchange) throws IOException {
             if ("POST".equals(exchange.getRequestMethod())) {
                 String body = readRequestBody(exchange);
                 Map<String, String> data = parseSimpleJson(body);
                 String startId = data.get("startId");
                 String endId = data.get("endId");
                 
                 User startUser = findUserById(startId);
                 User endUser = findUserById(endId);

                 if (startUser != null && endUser != null) {
                     List<String> result = graph.getShortestPath(startUser, endUser);
                     sendResponse(exchange, 200, listToJson(result));
                 } else {
                     sendResponse(exchange, 404, "{\"error\": \"User not found\"}");
                 }
             } else {
                 sendResponse(exchange, 405, "{\"error\": \"Method not allowed\"}");
             }
        }
    }

    // Helper to find user by ID
    private static User findUserById(String id) {
        if (id == null) return null;
        for (User u : graph.getAllUsers()) {
            if (u.getId().equals(id)) return u;
        }
        return null;
    }

    private static String listToJson(List<String> list) {
        if (list == null || list.isEmpty()) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder();
        sb.append("[");
        for (int i = 0; i < list.size(); i++) {
            sb.append("\"").append(list.get(i)).append("\"");
            if (i < list.size() - 1) sb.append(",");
        }
        sb.append("]");
        return sb.toString();
    }
}
