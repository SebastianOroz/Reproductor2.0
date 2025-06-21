package com.example.reproductor;


public class YoutubeItem {
    private String title;
    private String videoId;
    private String thumbnailUrl;

    public YoutubeItem(String title, String videoId, String thumbnailUrl) {
        this.title = title;
        this.videoId = videoId;
        this.thumbnailUrl = thumbnailUrl;
    }

    public String getTitle() {
        return title;
    }

    public String getVideoId() {
        return videoId;
    }

    public String getThumbnailUrl() {
        return thumbnailUrl;
    }
}