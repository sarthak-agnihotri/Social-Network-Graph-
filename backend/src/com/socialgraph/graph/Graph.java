package com.socialgraph.graph;

import com.socialgraph.model.User;
import java.util.*;

/**
 * Graph class to manage users and their connections.
 * Yeh class graph ko represent karti hai using Adjacency List.
 */
public class Graph {
    // Adjacency List: Map connecting a User to a list of their friends
    // Har user ke corresponding uske friends ki list store hoti hai.
    private Map<User, List<User>> adjList;

    public Graph() {
        this.adjList = new HashMap<>();
    }

    // Add a user to the network
    // Naya user add karne ke liye
    public void addUser(User user) {
        adjList.putIfAbsent(user, new ArrayList<>());
    }

    // Add a friendship connection (Undirected Graph)
    // Do users ke beech dosti karwane ke liye (donon taraf connection hoga)
    public void addFriendship(User u1, User u2) {
        if (!adjList.containsKey(u1)) addUser(u1);
        if (!adjList.containsKey(u2)) addUser(u2); 

        // Check if already friends to avoid duplicates
        if (!adjList.get(u1).contains(u2)) {
            adjList.get(u1).add(u2);
        }
        if (!adjList.get(u2).contains(u1)) {
            adjList.get(u2).add(u1);
        }
    }

    // Get friends of a user
    // Kisi user ke dost dekhne ke liye
    public List<User> getFriends(User user) {
        return adjList.getOrDefault(user, new ArrayList<>()); 
    }

    // Get all users
    public Set<User> getAllUsers() {
        return adjList.keySet();
    }
    
    // BFS Traversal
    // Breadth-First Search: Level by level traverse karne ke liye.
    // Pehle startUser, fir uske friends, fir unke friends...
    public List<String> bfs(User startUser) {
        List<String> visitedOrder = new ArrayList<>(); 
        if (startUser == null || !adjList.containsKey(startUser)) return visitedOrder; // Agar startUser null hai ya graph mein nahi hai, empty list return karo
        
        Set<User> visited = new HashSet<>(); // Visited nodes track karne ke liye
        Queue<User> queue = new LinkedList<>(); // BFS ke liye queue
        
        visited.add(startUser); // Start user visited mark karo
        queue.add(startUser); // Start user queue mein daalo
        
        while (!queue.isEmpty()) {
            User current = queue.poll(); // Queue se remove karo
            visitedOrder.add(current.getId()); // Add current node to visited order
            
            for (User neighbor : getFriends(current)) {
                if (!visited.contains(neighbor)) {
                    visited.add(neighbor); // Mark as visited
                    queue.add(neighbor);   // Queue mein add karo
                }
            }
        }
        return visitedOrder;
    }
    
    // DFS Traversal Logic (Recursive helper)
    private void dfsUtil(User user, Set<User> visited, List<String> result) { 
        visited.add(user); // Mark as visited
        result.add(user.getId()); // Add current node to result
        
        for (User neighbor : getFriends(user)) { // Explore neighbors
            if (!visited.contains(neighbor)) { // If neighbor not visited
                dfsUtil(neighbor, visited, result); // Recursively visit neighbor 
            }
        }
    }
    
    // DFS Traversal
    public List<String> dfs(User startUser) { 
        List<String> result = new ArrayList<>(); 
        if (startUser == null || !adjList.containsKey(startUser)) return result;
        
        dfsUtil(startUser, new HashSet<>(), result);
        return result;
    }

    // Shortest Path (BFS)
    // Do users ke beech sabse chhota rasta dhoondhne ke liye
    public List<String> getShortestPath(User start, User end) {
        // Agar start ya end graph me nahi hai, empty list return karo
        if (!adjList.containsKey(start) || !adjList.containsKey(end)) return Collections.emptyList();
        
        Queue<User> queue = new LinkedList<>(); // BFS ke liye queue
        Map<User, User> parentMap = new HashMap<>(); // Har node ka parent store karne ke liye
        Set<User> visited = new HashSet<>();// Visited nodes track karne ke liye
        
        queue.add(start);   // Start user queue me daalo
        visited.add(start); // Start user visited mark karo
        parentMap.put(start, null); // Start user ke parent null hai
        
        boolean found = false; // Target mil gaya ya nahi track karne ke liye
        
        while (!queue.isEmpty()) {
            User current = queue.poll(); // Queue se remove karo
            if (current.equals(end)) { // Agar target mil gaya, break karo
                found = true;
                break;
            }
            // Current ke saare neighbors explore karo
            for (User neighbor : getFriends(current)) { 
                if (!visited.contains(neighbor)) { // Agar neighbor nahi visited hai
                    visited.add(neighbor); // Mark as visited
                    parentMap.put(neighbor, current); // Store parent
                    queue.add(neighbor); // Add to queue
                }
            }
        }
        
        if (!found) return Collections.emptyList(); // Agar target nahi mila, empty list return karo
        
        // Reconstruct path
        List<String> path = new ArrayList<>();
        User curr = end;
        while (curr != null) { 
            path.add(curr.getId()); // Add current node to path
            curr = parentMap.get(curr); // Move to parent
        }
        Collections.reverse(path); // Reverse path to get correct order
        return path;
    }
}
