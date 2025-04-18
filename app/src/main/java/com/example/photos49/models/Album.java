package com.example.photos49.models;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;
public class Album implements  Serializable {
    private String name;
    private List<Photo> photos;

    public Album(String name) {
        this.name = name;
        this.photos = new ArrayList<>();
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public List<Photo> getPhotos() {
        return photos;
    }

    public void setPhotos(List<Photo> photos) {
        this.photos = photos;
    }

    public void addPhoto(Photo photo) {
        if (photo != null && !photos.contains(photo)) {
            photos.add(photo);
        }
    }

    public void removePhoto(Photo photo) {
        photos.remove(photo);
    }

    public int getPhotoCount() {
        return photos.size();
    }

    // Compare albums based on name, since no two albums can have the same name.
    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Album)) {
            return false;
        }
        Album album = (Album) obj;
        return name.equals(album.name);
    }
}
