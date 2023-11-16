package com.lithium.wall4u.Favorites;

import androidx.annotation.Keep;

@Keep
public class FavouritesModel {

    String photoId;
    String mediumPic;
    String originalPic;
    String openInWebsite;
    String photographerName;
    String photographerId;
    String photographerUrl;

    public FavouritesModel(String photoId, String mediumPic, String originalPic,
                           String openInWebsite, String photographerName,
                           String photographerId, String photographerUrl) {
        this.photoId = photoId;
        this.mediumPic = mediumPic;
        this.originalPic = originalPic;
        this.openInWebsite = openInWebsite;
        this.photographerName = photographerName;
        this.photographerId = photographerId;
        this.photographerUrl = photographerUrl;
    }

    public FavouritesModel() {
    }

    public void setPhotoId(String photoId) {
        this.photoId = photoId;
    }

    public void setMediumPic(String mediumPic) {
        this.mediumPic = mediumPic;
    }

    public void setOriginalPic(String originalPic) {
        this.originalPic = originalPic;
    }

    public void setOpenInWebsite(String openInWebsite) {
        this.openInWebsite = openInWebsite;
    }

    public void setPhotographerName(String photographerName) {
        this.photographerName = photographerName;
    }

    public void setPhotographerId(String photographerId) {
        this.photographerId = photographerId;
    }

    public void setPhotographerUrl(String photographerUrl) {
        this.photographerUrl = photographerUrl;
    }

    public String getPhotoId() {
        return photoId;
    }

    public String getMediumPic() {
        return mediumPic;
    }

    public String getOriginalPic() {
        return originalPic;
    }

    public String getOpenInWebsite() {
        return openInWebsite;
    }

    public String getPhotographerName() {
        return photographerName;
    }

    public String getPhotographerId() {
        return photographerId;
    }

    public String getPhotographerUrl() {
        return photographerUrl;
    }
}
