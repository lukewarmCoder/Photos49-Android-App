package com.example.photos49.models;

import java.io.Serializable;

public class Tag implements Serializable {
    public static final String TYPE_PERSON = "person";
    public static final String TYPE_LOCATION = "location";

    private String type;
    private String value;

    public Tag(String type, String value) {
        this.type = type;
        this.value = value;
    }

    public String getType() {
        return type;
    }

    public String getValue() {
        return value;
    }

    @Override
    public boolean equals(Object obj) {
        if (!(obj instanceof Tag)) {
            return false;
        }
        Tag tag = (Tag) obj;
        return type.equalsIgnoreCase(tag.type) &&
                value.equalsIgnoreCase(tag.value);
    }

    @Override
    public String toString() {
        return type + ": " + value;
    }
}
