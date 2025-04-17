package com.example.photos49.models;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.UUID;

public class Photo implements Serializable {
    private String id;
    private String uri;
    private Date dateTaken;
    private List<Tag> tags;
    private String caption;

    public Photo(Uri imageUri) {
        this.id = UUID.randomUUID().toString();
        this.uri = imageUri.toString();
        this.dateTaken = new Date();
        this.tags = new ArrayList<>();
    }

    public String getId() {
        return id;
    }

    public Uri getUri() {
        return Uri.parse(uri);
    }

    public Date getDateTaken() {
        return dateTaken;
    }

    public void setDateTaken(Date dateTaken) {
        this.dateTaken = dateTaken;
    }

    public List<Tag> getTags() {
        return tags;
    }

    public String getCaption() {
        return caption;
    }

    public void setCaption(String caption) {
        this.caption = caption;
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
