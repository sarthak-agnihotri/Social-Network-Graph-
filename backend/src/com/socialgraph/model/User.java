package com.socialgraph.model;

import java.util.Objects;

/**
 * User class represents a node in the graph.
 * Yeh ek user ko represent karta hai jiske paas ID, name aur email hota hai.
 */
public class User {
    private String id;
    private String name;
    private String email;

    // Constructor
    public User(String id, String name, String email) {
        this.id = id;
        this.name = name;
        this.email = email;
    }

    // Getters and Setters
    public String getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    // Override equals and hashCode for correct usage in HashMaps/Sets
    // Yeh zaroori hai taaki hum User objects ko Map ya Set mein sahi se store kar sakein based on ID.
    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        User user = (User) o;
        return Objects.equals(id, user.id);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id);
    }

    @Override
    public String toString() {
        return "User{" +
                "id='" + id + '\'' +
                ", name='" + name + '\'' +
                '}';
    }
}
