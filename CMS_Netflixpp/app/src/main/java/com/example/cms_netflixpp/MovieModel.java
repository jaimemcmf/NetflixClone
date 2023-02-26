package com.example.cms_netflixpp;

public class MovieModel {
    String name;
    int id;
    String thumbnail;

    MovieModel(String name, int id, String thumbnail) {
        this.name = name;
        this.id = id;
        this.thumbnail = thumbnail;
    }

    public String getName() {
        return name;
    }

    public int getId() {
        return id;
    }

    public void setId(int id) {
        this.id = id;
    }
}
