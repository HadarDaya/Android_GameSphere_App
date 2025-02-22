package com.example.gamesphere.model;

import java.io.Serializable;

/**
 * The class represents User entity
 * The class expends Firebase Authentication table, allowing us to save username and isAdmin flag.
 * (also represents a row in corresponding table in database)
 */
public class User  implements Serializable { // To allow passing User through Bundle.
    private String uid; // as generated in Firebase Authentication
    private String username;
    private boolean isAdmin;

    public User(String uid, String username,boolean isAdmin) {
        this.uid = uid;
        this.username = username;
        this.isAdmin=isAdmin;
    }

    public User(){
    }

    public String getUid() {
        return uid;
    }

    public void setUid(String uid) {
        this.uid = uid;
    }

    public String getUsername() {
        return username;
    }

    public void setUsername(String username) {
        this.username = username;
    }

    public boolean isAdmin() {
        return isAdmin;
    }

    public void setAdmin(boolean admin) {
        isAdmin = admin;
    }
}
