package com.example.gamesphere.model;

import androidx.annotation.NonNull;

/**
 * The class represents Series entity
 * (also represents a row in corresponding table in database)
 */
public class Series {
    private long id;
    private String name;

    public Series() {
    }

    public Series(long id, String name) {
        this.id = id;
        this.name = name;
    }

    public long getId() {
        return id;
    }

    public void setId(long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    @NonNull
    @Override // the function is necessary so that Spinner will display name only
    public String toString() {
        return name;
    }
}
