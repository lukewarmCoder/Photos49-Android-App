package com.example.photos49;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.photos49.activities.AlbumActivity;
import com.example.photos49.adapters.AlbumAdapter;
import com.example.photos49.util.DataStorage;
import com.example.photos49.models.Album;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class MainActivity extends AppCompatActivity {

    private List<Album> albums;
    private AlbumAdapter albumAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView albumRecyclerView = findViewById(R.id.albumRecyclerView);
        FloatingActionButton fabAddAlbum = findViewById(R.id.fabAddAlbum);

        albums = DataStorage.loadAlbumsFromStorage(this);
        albumRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        albumAdapter = new AlbumAdapter(
                albums,
                album -> onAlbumClick(album),
                (album, view) -> showAlbumContextMenu(album, view)
        );

        albumRecyclerView.setAdapter(albumAdapter);
        fabAddAlbum.setOnClickListener(v -> showCreateAlbumDialog());
    }

    private void showAlbumContextMenu(Album album, View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.album_context_menu);

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.rename_album) {
                showRenameAlbumDialog(album);
                return true;
            } else if (itemId == R.id.delete_album) {
                deleteAlbum(album);
                return true;
            }
            return false;
        });

        popup.show();
    }

    private void showCreateAlbumDialog() {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setHint("Album Name");

        // Padding via FrameLayout container
        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT
        );
        params.setMargins(50, 20, 50, 20);
        input.setLayoutParams(params);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Create New Album")
                .setView(container)
                .setPositiveButton("Create", (dialog, which) -> {
                    String albumName = input.getText().toString().trim();
                    if (albumName.isEmpty()) {
                        Toast.makeText(this, "Album name can't be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    if (isDuplicateName(albumName)) {
                        Toast.makeText(this, "Album with this name already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }
                    addAlbumToList(albumName);
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void showRenameAlbumDialog(Album album) {
        final EditText input = new EditText(this);
        input.setInputType(InputType.TYPE_CLASS_TEXT);
        input.setText(album.getName());

        FrameLayout container = new FrameLayout(this);
        FrameLayout.LayoutParams params = new FrameLayout.LayoutParams(
                ViewGroup.LayoutParams.MATCH_PARENT,
                ViewGroup.LayoutParams.WRAP_CONTENT);
        params.setMargins(50, 20, 50, 20);
        input.setLayoutParams(params);
        container.addView(input);

        new AlertDialog.Builder(this)
                .setTitle("Rename Album")
                .setView(container)
                .setPositiveButton("Rename", (dialog, which) -> {
                    String newName = input.getText().toString().trim();

                    if (newName.isEmpty()) {
                        Toast.makeText(this, "Album name can't be empty", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    if (newName.equals(album.getName())) {
                        return;
                    }

                    if (isDuplicateName(newName)) {
                        Toast.makeText(this, "An album with that name already exists", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    album.setName(newName);
                    albumAdapter.notifyDataSetChanged();
                    DataStorage.saveAlbumsToStorage(this, albums);
                    Toast.makeText(this, "Album renamed", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    @SuppressLint("NotifyDataSetChanged")
    private void deleteAlbum(Album album) {
        albums.remove(album);
        albumAdapter.notifyDataSetChanged();
        DataStorage.saveAlbumsToStorage(this, albums);
        Toast.makeText(this, "Album deleted", Toast.LENGTH_SHORT).show();
    }

    private boolean isDuplicateName(String name) {
        for (Album a : albums) {
            if (a.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private void addAlbumToList(String albumName) {
        // Create new album
        Album newAlbum = new Album(albumName);

        // Add to the list and update adapter
        albums.add(newAlbum);
        albumAdapter.notifyItemInserted(albums.size() - 1);

        // Save all albums to storage
        DataStorage.saveAlbumsToStorage(this, albums);

    }

    public void onAlbumClick(Album album) {
        Intent intent = new Intent(this, AlbumActivity.class);
        intent.putExtra("album_name", album.getName());
        startActivity(intent);
    }
}
