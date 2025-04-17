package com.example.photos49.activities;

import android.graphics.Color;
import android.graphics.PorterDuff;
import android.graphics.drawable.Drawable;
import android.os.Bundle;
import android.widget.TextView;
import androidx.appcompat.widget.Toolbar;

import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos49.R;
import com.example.photos49.adapters.PhotoAdapter;
import com.example.photos49.models.Album;
import com.example.photos49.models.Photo;
import com.example.photos49.util.DataStorage;
import com.google.android.material.appbar.MaterialToolbar;

import java.util.List;
import java.util.Objects;

public class AlbumActivity extends AppCompatActivity {

    private Album album;
    private List<Photo> photos;
    private RecyclerView photoRecyclerView;
    private TextView albumTitleTextView;

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
        String albumTitle = getIntent().getStringExtra("album_name");
        if (albumTitle != null && !albumTitle.isEmpty()) {
            toolbarTitle.setText(albumTitle);
        }

        // Set navigation icon (back button) manually to ensure visibility
        toolbar.setNavigationIcon(R.drawable.ic_baseline_arrow_back_24);

        // Set navigation icon (back button) tint to white
        Drawable navigationIcon = toolbar.getNavigationIcon();
        if (navigationIcon != null) {
            navigationIcon.setColorFilter(Color.WHITE, PorterDuff.Mode.SRC_ATOP);
        }

        // Rest of your code for setting up RecyclerView, etc.
    }


    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

}