package com.socialgraph;

import com.socialgraph.model.User;
import com.socialgraph.graph.Graph;
import java.util.List;
import java.util.Set;

public class Main {
    public static void main(String[] args) {
        System.out.println("VERIFICATION START");

        Graph graph = new Graph();

        User alice = new User("1", "Alice", "alice@test.com");
        User bob = new User("2", "Bob", "bob@test.com");
        User charlie = new User("3", "Charlie", "charlie@test.com");
        User david = new User("4", "David", "david@test.com");
        User eve = new User("5", "Eve", "eve@test.com");

        graph.addUser(alice);
        graph.addUser(bob);
        graph.addUser(charlie);
        graph.addUser(david);
        graph.addUser(eve);

        // A-B, A-C, B-D, C-E, D-E
        graph.addFriendship(alice, bob);
        graph.addFriendship(alice, charlie);
        graph.addFriendship(bob, david);
        graph.addFriendship(charlie, eve);
        graph.addFriendship(david, eve);

        System.out.println("FRIENDS:");
        printFriends(graph, alice);
        printFriends(graph, bob);
        
        System.out.println("BFS from Alice:");
        List<String> bfs = graph.bfs(alice);
        System.out.println(bfs);

        System.out.println("DFS from Alice:");
        List<String> dfs = graph.dfs(alice);
        System.out.println(dfs);

        System.out.println("Shortest Path Alice -> Eve:");
        List<String> path = graph.getShortestPath(alice, eve);
        System.out.println(path);
        
        System.out.println("VERIFICATION END");
    }

    private static void printFriends(Graph g, User u) {
        List<User> friends = g.getFriends(u);
        System.out.print(u.getName() + ": ");
        for(User f : friends) {
            System.out.print(f.getName() + " ");
        }
        System.out.println();
    }
}
