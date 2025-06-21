package com.example.reproductor;

public class Folder {
    private String name;
    private String path;
    private int songCount;

    public Folder(String name, String path, int songCount) {
        this.name = name;
        this.path = path;
        this.songCount = songCount;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public int getSongCount() {
        return songCount;
    }

    public void setSongCount(int songCount) {
        this.songCount = songCount;
    }

    public String getPath() {
        return path;
    }

    public void setPath(String path) {
        this.path = path;
    }
}