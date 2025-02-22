package com.example.gamesphere.model;

/**
 * The class represents Platform entity
 * (also represents a row in corresponding table in database)
 */
public class Platform {
   private long id;
   private String name;

    public Platform() {
    }

    public Platform(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }
}
