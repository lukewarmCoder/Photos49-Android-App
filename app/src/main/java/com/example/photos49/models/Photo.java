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

    public List<Tag> getTags() {
        return tags;
    }

    public List<String> getTagStrings() {
        List<String> tagStrings = new ArrayList<>();
        for (Tag t : tags) {
            tagStrings.add(t.toString());
        }
        return tagStrings;
    }

    public void addTag(String tag) {
        // todo
    }


    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Photo)) {
            return false;
        }
        Photo photo = (Photo) obj;
        return id.equals(photo.id);
    }

}
