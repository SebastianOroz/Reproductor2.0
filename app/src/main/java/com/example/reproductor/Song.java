package com.example.reproductor;

public class Song {
    private String title;
    private String artist;
    private String path;
    private long duration;
    private long albumId;
    private String album; // Nuevo campo para el nombre del álbum

    public Song(String title, String artist, String path, long duration, long albumId, String album) {
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.duration = duration;
        this.albumId = albumId;
        this.album = album;
    }

    // Añade el getter para el álbum
    public String getAlbum() {
        return album;
    }

    public String getTitle() { return title; }
    public String getArtist() { return artist; }
    public String getPath() { return path; }
    public long getDuration() { return duration; }
    public long getAlbumId() { return albumId; }
}