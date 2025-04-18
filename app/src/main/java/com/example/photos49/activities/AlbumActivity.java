package com.example.photos49.activities;

import android.Manifest;
import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.net.Uri;
import android.os.Bundle;
import android.provider.MediaStore;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.widget.PopupMenu;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos49.R;
import com.example.photos49.adapters.PhotoAdapter;
import com.example.photos49.models.Album;
import com.example.photos49.models.Photo;
import com.example.photos49.util.DataStorage;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.util.ArrayList;
import java.util.List;
import java.util.Objects;

public class AlbumActivity extends AppCompatActivity {

    private Album album;
    private List<Photo> photos;
    private RecyclerView photoRecyclerView;
    private TextView albumTitleTextView;
    private static final int REQUEST_PICK_PHOTO = 101;
    private String albumTitle;
    private PhotoAdapter adapter;

    private final ActivityResultLauncher<Intent> pickPhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        boolean alreadyExists = false;
                        for (Photo p : album.getPhotos()) {
                            if (p.getUri().equals(selectedImageUri.toString())) {
                                alreadyExists = true;
                                break;
                            }
                        }

                        if (alreadyExists) {
                            Toast.makeText(this, "Photo already exists in album.", Toast.LENGTH_SHORT).show();
                        } else {
                            // Persist permission
                            final int takeMoreFlags = Intent.FLAG_GRANT_READ_URI_PERMISSION;
                            getContentResolver().takePersistableUriPermission(selectedImageUri, takeMoreFlags);

                            Photo newPhoto = new Photo(selectedImageUri);
                            album.addPhoto(newPhoto);

                            // Save and refresh
                            List<Album> albums = DataStorage.loadAlbumsFromStorage(this);
                            for (Album a : albums) {
                                if (a.getName().equals(albumTitle)) {
                                    a.getPhotos().add(newPhoto);
                                    break;
                                }
                            }
                            DataStorage.saveAlbumsToStorage(this, albums);
                            adapter.notifyItemInserted(album.getPhotos().size() - 1);
                        }
                    }
                }
            });

    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_album);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        // Enable back button
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);

        // Hide default title to use our centered TextView
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Set up the centered title
        TextView toolbarTitle = findViewById(R.id.toolbarTitle);

        // Get album title from intent with the correct key
        albumTitle = getIntent().getStringExtra("album_name");
        if (albumTitle != null && !albumTitle.isEmpty()) {
            toolbarTitle.setText(albumTitle);
        }

        List<Album> albums = DataStorage.loadAlbumsFromStorage(this);
        for (Album a : albums) {
            if (a.getName().equals(albumTitle)) {
                album = a;
                break;
            }
        }

        if (album == null) {
            Toast.makeText(this, "Album not found!", Toast.LENGTH_SHORT).show();
            finish();
            return;
        }

        // Set navigation icon (back button) manually to ensure visibility
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);

        // Set navigation icon (back button) tint to white
        Drawable navigationIcon = toolbar.getNavigationIcon();
        if (navigationIcon != null) {
            navigationIcon = DrawableCompat.wrap(navigationIcon).mutate();
            DrawableCompat.setTint(navigationIcon, Color.WHITE);
            toolbar.setNavigationIcon(navigationIcon);
        }


        photoRecyclerView = findViewById(R.id.photoRecyclerView); // Make sure this ID exists in your XML
        photos = album.getPhotos(); // assign to class variable
        adapter = new PhotoAdapter(this, photos, albumTitle, (view, photo, position) -> showPhotoMenu(view, photo, position));
        photoRecyclerView.setAdapter(adapter);
        photoRecyclerView.setLayoutManager(new GridLayoutManager(this, 2)); // or LinearLayoutManager


        // FAB add photo click
        FloatingActionButton fabAddPhoto = findViewById(R.id.fabAddPhoto);
        fabAddPhoto.setOnClickListener(v -> {
            Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
            intent.setType("image/*");
            intent.addCategory(Intent.CATEGORY_OPENABLE);
            pickPhotoLauncher.launch(intent);
        });
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Reload album data from storage
        List<Album> albums = DataStorage.loadAlbumsFromStorage(this);
        for (Album a : albums) {
            if (a.getName().equals(albumTitle)) {
                album = a;
                photos = album.getPhotos(); // Update the photos list reference
                break;
            }
        }

        // Refresh adapter with new data
        if (adapter != null) {
            adapter = new PhotoAdapter(this, photos, albumTitle, (view, photo, position) -> showPhotoMenu(view, photo, position));
            photoRecyclerView.setAdapter(adapter);
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, @Nullable Intent data) {
        super.onActivityResult(requestCode, resultCode, data);

        if (requestCode == REQUEST_PICK_PHOTO && resultCode == RESULT_OK && data != null) {
            Uri selectedImageUri = data.getData();
            if (selectedImageUri != null) {
                Photo newPhoto = new Photo(selectedImageUri);
                album.addPhoto(newPhoto);

                // Save album change
                List<Album> albums = DataStorage.loadAlbumsFromStorage(this);
                for (Album a : albums) {
                    if (a.getName().equals(albumTitle)) {
                        a.getPhotos().add(newPhoto);
                        break;
                    }
                }
                DataStorage.saveAlbumsToStorage(this, albums);
                adapter.notifyItemInserted(photos.size() - 1);
            }
        }
    }

    private void showPhotoMenu(View anchorView, Photo photo, int position) {
        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.inflate(R.menu.photo_context_menu); // You said you had this ready

        popupMenu.setOnMenuItemClickListener(item -> {
            int id = item.getItemId();

            if (id == R.id.move_photo) {
                handleMove(photo, position);
                return true;
            } else if (id == R.id.delete_photo) {
                album.getPhotos().remove(photo);

                // Persist changes
                List<Album> albums = DataStorage.loadAlbumsFromStorage(this);
                for (Album a : albums) {
                    if (a.getName().equals(albumTitle)) {
                        a.setPhotos(album.getPhotos()); // if you have a setter
                        break;
                    }
                }
                DataStorage.saveAlbumsToStorage(this, albums);

                adapter.notifyItemRemoved(position);
                return true;
            } else {
                return false;
            }
        });

        popupMenu.show();
    }

    private void handleMove(Photo photo, int position) {
        List<Album> allAlbums = DataStorage.loadAlbumsFromStorage(this);
        List<String> otherAlbumNames = new ArrayList<>();

        for (Album a : allAlbums) {
            if (!a.getName().equals(albumTitle)) {
                otherAlbumNames.add(a.getName());
            }
        }

        if (otherAlbumNames.isEmpty()) {
            Toast.makeText(this, "No other albums available.", Toast.LENGTH_SHORT).show();
            return;
        }

        String[] albumArray = otherAlbumNames.toArray(new String[0]);
        new AlertDialog.Builder(this)
                .setTitle("Move photo to:")
                .setItems(albumArray, (dialog, which) -> {
                    String targetAlbumName = albumArray[which];
                    Album targetAlbum = null;
                    Album currentAlbumInStorage = null;

                    for (Album a : allAlbums) {
                        if (a.getName().equals(targetAlbumName)) {
                            targetAlbum = a;
                        }
                        if (a.getName().equals(albumTitle)) {
                            currentAlbumInStorage = a;
                        }
                    }

                    if (targetAlbum == null || currentAlbumInStorage == null) {
                        Toast.makeText(this, "Album not found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Avoid duplicates in the target album
                    for (Photo p : targetAlbum.getPhotos()) {
                        if (p.getUri().equals(photo.getUri())) {
                            Toast.makeText(this, "Photo already exists in selected album.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // Remove from current album in storage and in-memory
                    currentAlbumInStorage.getPhotos().remove(photo);
                    album.getPhotos().remove(photo);

                    // Add to target album
                    targetAlbum.addPhoto(photo);

                    // Save updated albums to storage
                    DataStorage.saveAlbumsToStorage(this, allAlbums);

                    // Notify adapter of removal
                    adapter.notifyItemRemoved(position);
                    Toast.makeText(this, "Photo moved to " + targetAlbumName, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }
}