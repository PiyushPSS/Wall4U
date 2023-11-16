package com.lithium.wall4u;

public class WallpaperAttributes {

    String originalPhoto, mediumPhoto, id;
    String openInPexels;
    String photographerName, photographerUrl, photographerId;

    public WallpaperAttributes(String id, String originalPhoto, String mediumPhoto, String openInPexels,
                               String photographerName, String photographerUrl, String photographerId) {
        this.id = id;
        this.originalPhoto = originalPhoto;
        this.mediumPhoto = mediumPhoto;
        this.openInPexels = openInPexels;
        this.photographerName = photographerName;
        this.photographerUrl = photographerUrl;
        this.photographerId = photographerId;
    }

    public String getOpenInPexels() {
        return openInPexels;
    }

    public void setOpenInPexels(String openInPexels) {
        this.openInPexels = openInPexels;
    }

    public String getId() {
        return id;
    }

    public void setId(String id) {
        this.id = id;
    }

    public String getOriginalPhoto() {
        return originalPhoto;
    }

    public void setOriginalPhoto(String originalPhoto) {
        this.originalPhoto = originalPhoto;
    }

    public String getMediumPhoto() {
        return mediumPhoto;
    }

    public void setMediumPhoto(String mediumPhoto) {
        this.mediumPhoto = mediumPhoto;
    }

    public String getPhotographerName() {
        return photographerName;
    }

    public void setPhotographerName(String photographerName) {
        this.photographerName = photographerName;
    }

    public String getPhotographerUrl() {
        return photographerUrl;
    }

    public void setPhotographerUrl(String photographerUrl) {
        this.photographerUrl = photographerUrl;
    }

    public String getPhotographerId() {
        return photographerId;
    }

    public void setPhotographerId(String photographerId) {
        this.photographerId = photographerId;
    }
}
