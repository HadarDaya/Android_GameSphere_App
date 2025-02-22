package com.example.gamesphere.model;

import java.io.Serializable;
import java.util.ArrayList;
import java.lang.Long;

/**
 * The class represents Game entity
 * (also represents a row in corresponding table in database)
 */
public class Game implements Serializable {

    private String name, description, imageURL, videoURL;
    private Integer releaseYear;
    private ArrayList<Long> platformIds, genreIds; // array because Game can relate to multiple platforms and genres
    private Long id, seriesID, developerID, publisherID;
    private boolean isSinglePlayer, isMultiPlayer;
    private double avgRate;
    private int numOfRaters;

    public Game() {

    }

    public Game(java.lang.Long id, String name, Integer releaseYear, String description, String imageURL,
                String videoURL, ArrayList<Long> platformIds, Long seriesID, Long developerID,
                Long publisherID, ArrayList<Long> genreIds, boolean isSinglePlayer,
                boolean isMultiPlayer, double avgRate, int numOfRaters) {
        this.id = id;
        this.name = name;
        this.releaseYear = releaseYear;
        this.description = description;
        this.imageURL = imageURL;
        this.videoURL = videoURL;
        this.platformIds = platformIds;
        this.seriesID = seriesID;
        this.developerID = developerID;
        this.publisherID = publisherID;
        this.genreIds = genreIds;
        this.isSinglePlayer = isSinglePlayer;
        this.isMultiPlayer = isMultiPlayer;
        this.avgRate = 0;
        this.numOfRaters = 0;
    }

    public java.lang.Long getId() {
        return id;
    }

    public void setId(java.lang.Long id) {
        this.id = id;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public Integer getReleaseYear() {
        return releaseYear;
    }

    public void setReleaseYear(Integer releaseYear) {
        this.releaseYear = releaseYear;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public String getImageURL() {
        return imageURL;
    }

    public void setImageURL(String imageURL) {
        this.imageURL = imageURL;
    }

    public String getVideoURL() {
        return videoURL;
    }

    public void setVideoURL(String videoURL) {
        this.videoURL = videoURL;
    }

    public ArrayList<Long> getPlatformIds() {
        return platformIds;
    }

    public void setPlatformIds(ArrayList<Long> platformIds) {
        this.platformIds = platformIds;
    }

    public Long getSeriesID() {
        return seriesID;
    }

    public void setSeriesID(Long seriesID) {
        this.seriesID = seriesID;
    }

    public Long getDeveloperID() {
        return developerID;
    }

    public void setDeveloperID(Long developerID) {
        this.developerID = developerID;
    }

    public Long getPublisherID() {
        return publisherID;
    }

    public void setPublisherID(Long publisherID) {
        this.publisherID = publisherID;
    }

    public ArrayList<Long> getGenreIds() {
        return genreIds;
    }

    public void setGenreIds(ArrayList<Long> genreIds) {
        this.genreIds = genreIds;
    }

    public boolean isSinglePlayer() {
        return isSinglePlayer;
    }

    public void setSinglePlayer(boolean singlePlayer) {
        isSinglePlayer = singlePlayer;
    }

    public boolean isMultiPlayer() {
        return isMultiPlayer;
    }

    public void setMultiPlayer(boolean multiPlayer) {
        isMultiPlayer = multiPlayer;
    }

    public double getAvgRate() {
        return avgRate;
    }

    public void setAvgRate(double avgRate) {
        this.avgRate = avgRate;
    }

    public int getNumOfRaters() {
        return numOfRaters;
    }

    public void setNumOfRaters(int numOfRaters) {
        this.numOfRaters = numOfRaters;
    }
}
