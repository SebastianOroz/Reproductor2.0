package com.example.reproductor;

public class Song {

    private long id;
    private String title;
    private String artist;
    private String path;
    private long duration;
    private long albumId;
    private String album;
    private long dateAdded;
    private long dateModified; // NUEVO: Fecha de modificación

    public Song(long id,String title, String artist, String path, long duration, long albumId, String album, long dateAdded, long dateModified) {
        this.id = id;
        this.title = title;
        this.artist = artist;
        this.path = path;
        this.duration = duration;
        this.albumId = albumId;
        this.album = album;
        this.dateAdded = dateAdded;
        this.dateModified = dateModified; // Asignar dateModified
    }

    public long getId() {
        return id;
    }


    public void setId(long id) {
        this.id = id;
    }
    public String getTitle() {
        return title;
    }

    public String getArtist() {
        return artist;
    }

    public String getPath() {
        return path;
    }

    public long getDuration() {
        return duration;
    }

    public long getAlbumId() {
        return albumId;
    }

    public String getAlbum() {
        return album;
    }

    public long getDateAdded() {
        return dateAdded;
    }

    public long getDateModified() { // NUEVO: Getter para dateModified
        return dateModified;
    }
}