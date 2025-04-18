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
import android.os.Build;
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

import java.util.ArrayList;
import java.util.List;

public class AlbumActivity extends AppCompatActivity {

    private Album album;
    private boolean isSearchResult = false;
    private List<Photo> searchResults;
    private List<Photo> photos;
    private RecyclerView photoRecyclerView;

    private static final int REQUEST_PICK_PHOTO = 101;
    private String albumTitle;
    private PhotoAdapter adapter;

    private final ActivityResultLauncher<Intent> pickPhotoLauncher =
            registerForActivityResult(new ActivityResultContracts.StartActivityForResult(), result -> {
                if (result.getResultCode() == RESULT_OK && result.getData() != null) {
                    Uri selectedImageUri = result.getData().getData();
                    if (selectedImageUri != null) {
                        boolean alreadyExists = false;
                        String existingAlbum = "";
                        for (Album a : DataStorage.loadAlbumsFromStorage(this)) {
                            for (Photo p : a.getPhotos()) {
                                if (p.getUri().equals(selectedImageUri.toString())) {
                                    alreadyExists = true;
                                    existingAlbum = a.getName();
                                    break;
                                }
                            }
                        }

                        if (alreadyExists) {
                            Toast.makeText(this, "Photo already exists in: " + existingAlbum, Toast.LENGTH_SHORT).show();
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
        isSearchResult = getIntent().getBooleanExtra("is_search_result", false);

        if (albumTitle != null && !albumTitle.isEmpty()) {
            if (isSearchResult) {
                toolbarTitle.setText("Search Results");
            } else {
                toolbarTitle.setText(albumTitle);
            }
        }

        if (isSearchResult) {
            // Handle search results using Serializable with proper API level check
            if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
                searchResults = getIntent().getSerializableExtra("search_results", ArrayList.class);
            } else {
                searchResults = (ArrayList<Photo>) getIntent().getSerializableExtra("search_results");
            }

            // Create a temporary album with search results
            album = new Album("Search Results");
            if (searchResults != null) {
                for (Photo photo : searchResults) {
                    album.addPhoto(photo);
                }
            }
            photos = album.getPhotos();
        } else {
            // Regular album, load from storage
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
            photos = album.getPhotos();
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

        photoRecyclerView = findViewById(R.id.photoRecyclerView);
        adapter = new PhotoAdapter(this, photos, albumTitle, (view, photo, position) -> showPhotoMenu(view, photo, position));
        photoRecyclerView.setAdapter(adapter);
        photoRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        // FAB add photo click - hide if showing search results
        FloatingActionButton fabAddPhoto = findViewById(R.id.fabAddPhoto);
        if (isSearchResult) {
            fabAddPhoto.setVisibility(View.GONE);
        } else {
            fabAddPhoto.setOnClickListener(v -> {
                Intent intent = new Intent(Intent.ACTION_OPEN_DOCUMENT);
                intent.setType("image/*");
                intent.addCategory(Intent.CATEGORY_OPENABLE);
                pickPhotoLauncher.launch(intent);
            });
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        // Don't reload search results from storage since they're temporary
        if (!isSearchResult) {
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
        // Don't allow editing photos in search results
        if (isSearchResult) {
            // Show dialog to view original album instead
            new AlertDialog.Builder(this)
                    .setTitle("Search Result")
                    .setMessage("This photo is part of search results. Would you like to see it in its original album?")
                    .setPositiveButton("View Original", (dialog, which) -> {
                        // Find the original album containing this photo
                        String originalAlbumName = findOriginalAlbumName(photo);
                        if (originalAlbumName != null) {
                            // Open the original album
                            Intent intent = new Intent(this, AlbumActivity.class);
                            intent.putExtra("album_name", originalAlbumName);
                            startActivity(intent);
                        } else {
                            Toast.makeText(this, "Original album not found", Toast.LENGTH_SHORT).show();
                        }
                    })
                    .setNegativeButton("Cancel", null)
                    .show();
            return;
        }

        PopupMenu popupMenu = new PopupMenu(this, anchorView);
        popupMenu.inflate(R.menu.photo_context_menu);

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

    private String findOriginalAlbumName(Photo photo) {
        List<Album> allAlbums = DataStorage.loadAlbumsFromStorage(this);
        for (Album album : allAlbums) {
            for (Photo p : album.getPhotos()) {
                if (p.equals(photo) || p.getUri().equals(photo.getUri())) {
                    return album.getName();
                }
            }
        }
        return null;
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