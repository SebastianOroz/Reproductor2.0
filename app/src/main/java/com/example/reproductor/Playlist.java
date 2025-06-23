package com.example.reproductor;

import java.util.ArrayList;
import java.util.List;

public class Playlist {
    private String name;
    private List<Long> songIds;

    public Playlist(String name) {
        this.name = name;
        this.songIds = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Long> getSongIds() {
        return songIds;
    }

    public void addSongId(long songId) {
        if (!songIds.contains(songId)) {
            songIds.add(songId);
        }
    }

    public void addSongIds(List<Long> newSongIds) {
        for (long songId : newSongIds) {
            if (!songIds.contains(songId)) {
                songIds.add(songId);
            }
        }
    }

    public void removeSongId(long songId) {
        songIds.remove(songId);
    }

    public int getSongCount() {
        return songIds.size();
    }
}