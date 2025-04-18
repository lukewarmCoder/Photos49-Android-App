package com.example.photos49.models;

import android.graphics.Bitmap;
import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;

import java.util.List;
import java.util.UUID;

public class Photo implements Serializable {
    private String id;
    private String uri;
    private List<Tag> tags;
    private String caption;

    public Photo(Uri imageUri) {
        this.id = UUID.randomUUID().toString();
        this.uri = imageUri.toString();
        this.tags = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public String getUri() {
        return uri;
    }

//    public Bitmap getThumbnail() {
//        return thumbnail;
//    }
//
//    public void setThumbnail(Bitmap thumbnail) {
//        this.thumbnail = thumbnail;
//    }

    public List<Tag> getTags() {
        return tags;
    }

//    public String getAlbumName() {
//        return albumName;
//    }
//
//    public void setAlbumName(String albumName) {
//        this.albumName = albumName;
//    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Photo)) {
            return false;
        }
        Photo photo = (Photo) obj;
        return id.equals(photo.id);
    }

}
