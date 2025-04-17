package com.example.photos49.util;

import android.content.Context;
import android.widget.Toast;

import com.example.photos49.models.Album;

import java.io.FileOutputStream;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.ArrayList;
import java.util.List;

public class DataStorage {
    private static final String ALBUM_FILE_NAME = "albums.json";

    @SuppressWarnings("unchecked")
    public static List<Album> loadAlbumsFromStorage(Context context) {
        try {
            FileInputStream fis = context.openFileInput(ALBUM_FILE_NAME);
            ObjectInputStream ois = new ObjectInputStream(fis);
            List<Album> albums = (List<Album>) ois.readObject();
            ois.close();
            fis.close();
            return albums;
        } catch (IOException | ClassNotFoundException e) {
            return new ArrayList<>(); // Return empty list if anything fails
        }
    }

    public static void saveAlbumsToStorage(Context context, List<Album> albums) {
        try {
            FileOutputStream fos = context.openFileOutput(ALBUM_FILE_NAME, Context.MODE_PRIVATE);
            ObjectOutputStream oos = new ObjectOutputStream(fos);
            oos.writeObject(albums);
            oos.close();
            fos.close();
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(context, "Failed to save albums", Toast.LENGTH_SHORT).show();
        }
    }

    public static Album getAlbumByName(Context context, String albumName) {
        List<Album> albums = loadAlbumsFromStorage(context);
        for (Album album : albums) {
            if (album.getName().equalsIgnoreCase(albumName)) {
                return album;
            }
        }
        return null; // Or throw an exception / show error if album not found
    }
}
