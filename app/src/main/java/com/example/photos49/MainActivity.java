package com.example.photos49;

import android.os.Bundle;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.Toast;

import com.example.photos49.adapters.AlbumAdapter;
import com.example.photos49.models.Album;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity {

    private RecyclerView albumRecyclerView;
    private List<Album> albums;
    private AlbumAdapter albumAdapter;
    private FloatingActionButton fabAddAlbum;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        albumRecyclerView = findViewById(R.id.albumRecyclerView);
        fabAddAlbum = findViewById(R.id.fabAddAlbum);

        albums = new ArrayList<>();
        albumRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        albumAdapter = new AlbumAdapter(albums, new AlbumAdapter.OnAlbumClickListener() {
            @Override
            public void onAlbumClick(Album album) {
                Toast.makeText(MainActivity.this, "Clicked on: " + album.getName(), Toast.LENGTH_SHORT).show();
            }

            @Override
            public void onAlbumMenuClick(Album album, View view) {
                showAlbumContextMenu(album, view);
            }
        });

        albumRecyclerView.setAdapter(albumAdapter);
        fabAddAlbum.setOnClickListener(v -> showCreateAlbumDialog());
    }

    private void showAlbumContextMenu(Album album, View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.album_context_menu);

        popup.setOnMenuItemClickListener(item -> {
            int itemId = item.getItemId();
            if (itemId == R.id.rename_album) {
                Toast.makeText(this, "Rename option selected", Toast.LENGTH_SHORT).show();
                return true;
            } else if (itemId == R.id.delete_album) {
                Toast.makeText(this, "Delete option selected", Toast.LENGTH_SHORT).show();
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

    private boolean isDuplicateName(String name) {
        for (Album a : albums) {
            if (a.getName().equalsIgnoreCase(name)) return true;
        }
        return false;
    }

    private void addAlbumToList(String albumName) {
        Album newAlbum = new Album(albumName);
        albumAdapter.addAlbum(newAlbum);
    }

    public void onAlbumClick(Album album) {
        Toast.makeText(this, "Clicked on: " + album.getName(), Toast.LENGTH_SHORT).show();
        // TODO: Launch album detail screen
    }

    public void onAlbumMenuClick(Album album, View view) {
        PopupMenu popup = new PopupMenu(this, view);
        popup.inflate(R.menu.album_context_menu);

        popup.setOnMenuItemClickListener(item -> {
            if (item.getItemId() == R.id.rename_album) {
                Toast.makeText(this, "Rename option selected", Toast.LENGTH_SHORT).show();
                // TODO: Rename album logic
                return true;
            } else if (item.getItemId() == R.id.delete_album) {
                Toast.makeText(this, "Delete option selected", Toast.LENGTH_SHORT).show();
                // TODO: Delete album logic
                return true;
            }
            return false;
        });

        popup.show();
    }
}
