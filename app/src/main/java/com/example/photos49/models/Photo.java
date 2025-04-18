package com.example.photos49.models;

import android.net.Uri;

import java.io.Serializable;
import java.util.ArrayList;

import java.util.Iterator;
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
        if (tags == null) {
            tags = new ArrayList<>();
        }
        return tags;
    }

    public List<String> getTagStrings() {
        List<String> tagStrings = new ArrayList<>();
        for (Tag t : tags) {
            tagStrings.add(t.toString());
        }
        return tagStrings;
    }

    public void addTag(Tag tag) {
        // Check if it's a location tag - we can only have one
        if (tag.getType().equals(Tag.TYPE_LOCATION)) {
            // Remove any existing location tag
            removeTagsByType(Tag.TYPE_LOCATION);
        }
        // Check if this exact tag already exists to avoid duplicates
        if (!tags.contains(tag)) {
            tags.add(tag);
        }
    }

    public void removeTag(Tag tag) {
        tags.remove(tag);
    }

    public void removeTagsByType(String type) {
        Iterator<Tag> iterator = tags.iterator();
        while (iterator.hasNext()) {
            Tag tag = iterator.next();
            if (tag.getType().equals(type)) {
                iterator.remove();
            }
        }
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
