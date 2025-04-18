package com.example.photos49;

import android.annotation.SuppressLint;
import android.content.Intent;
import android.os.Build;
import android.os.Bundle;
import android.provider.ContactsContract;
import android.text.InputType;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.AutoCompleteTextView;
import android.widget.Button;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.LinearLayout;
import android.widget.Spinner;
import android.widget.Toast;

import com.example.photos49.activities.AlbumActivity;
import com.example.photos49.adapters.AlbumAdapter;
import com.example.photos49.models.Photo;
import com.example.photos49.models.Tag;
import com.example.photos49.util.DataStorage;
import com.example.photos49.models.Album;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

import androidx.appcompat.app.AlertDialog;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.PopupMenu;
import androidx.recyclerview.widget.GridLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MainActivity extends AppCompatActivity {

    private List<Album> albums;
    private AlbumAdapter albumAdapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        RecyclerView albumRecyclerView = findViewById(R.id.albumRecyclerView);
        FloatingActionButton fabAddAlbum = findViewById(R.id.fabAddAlbum);
        Button btnSearchByTag = findViewById(R.id.btnSearchByTag);

        albums = DataStorage.loadAlbumsFromStorage(this);
        albumRecyclerView.setLayoutManager(new GridLayoutManager(this, 2));

        albumAdapter = new AlbumAdapter(
                albums,
                album -> onAlbumClick(album),
                (album, view) -> showAlbumContextMenu(album, view)
        );

        albumRecyclerView.setAdapter(albumAdapter);
        fabAddAlbum.setOnClickListener(v -> showCreateAlbumDialog());
        btnSearchByTag.setOnClickListener(v -> showSearchByTagDialog());
    }

    private void showSearchByTagDialog() {
        // Create dialog with custom layout
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.search_by_tag_dialog, null);
        builder.setView(dialogView);

        AlertDialog dialog = builder.create();

        // Get all tag values from all photos for autocomplete suggestions
        Map<String, Set<String>> tagValuesByType = getAllTagValuesByType();

        // Initialize UI components
        Spinner spinnerTagType1 = dialogView.findViewById(R.id.spinnerTagType1);
        AutoCompleteTextView autoCompleteTagValue1 = dialogView.findViewById(R.id.autoCompleteTagValue1);
        Button btnAddCondition = dialogView.findViewById(R.id.btnAddCondition);
        LinearLayout logicalOperatorLayout = dialogView.findViewById(R.id.logicalOperatorLayout);
        Spinner spinnerLogicalOperator = dialogView.findViewById(R.id.spinnerLogicalOperator);
        LinearLayout secondTagLayout = dialogView.findViewById(R.id.secondTagLayout);
        Spinner spinnerTagType2 = dialogView.findViewById(R.id.spinnerTagType2);
        AutoCompleteTextView autoCompleteTagValue2 = dialogView.findViewById(R.id.autoCompleteTagValue2);
        Button btnPerformSearch = dialogView.findViewById(R.id.btnPerformSearch);

        // Set up tag type spinners
        setupTagTypeSpinners(spinnerTagType1, spinnerTagType2);

        // Set up logical operator spinner
        setupLogicalOperatorSpinner(spinnerLogicalOperator);

        // Set up autocomplete text views
        setupAutocompleteTextViews(spinnerTagType1, autoCompleteTagValue1, tagValuesByType);
        setupAutocompleteTextViews(spinnerTagType2, autoCompleteTagValue2, tagValuesByType);

        // Add condition button click listener
        btnAddCondition.setOnClickListener(v -> {
            btnAddCondition.setVisibility(View.GONE);
            logicalOperatorLayout.setVisibility(View.VISIBLE);
            secondTagLayout.setVisibility(View.VISIBLE);
        });

        // Search button click listener
        btnPerformSearch.setOnClickListener(v -> {
            // Get selected tag types and values
            String tagType1 = spinnerTagType1.getSelectedItem().toString();
            String tagValue1 = autoCompleteTagValue1.getText().toString().trim();

            if (tagValue1.isEmpty()) {
                Toast.makeText(this, "Please enter a tag value", Toast.LENGTH_SHORT).show();
                return;
            }

            // Check if second condition is visible
            boolean hasSecondCondition = secondTagLayout.getVisibility() == View.VISIBLE;
            String logicalOperator = hasSecondCondition ?
                    spinnerLogicalOperator.getSelectedItem().toString() : "";
            String tagType2 = hasSecondCondition ?
                    spinnerTagType2.getSelectedItem().toString() : "";
            String tagValue2 = hasSecondCondition ?
                    autoCompleteTagValue2.getText().toString().trim() : "";

            if (hasSecondCondition && tagValue2.isEmpty()) {
                Toast.makeText(this, "Please enter a second tag value", Toast.LENGTH_SHORT).show();
                return;
            }

            // Perform search
            List<Photo> searchResults = searchPhotosByTags(
                    tagType1, tagValue1, logicalOperator, tagType2, tagValue2);

            // Show search results
            showSearchResults(searchResults);

            dialog.dismiss();
        });

        dialog.show();
    }

    private void setupTagTypeSpinners(Spinner spinner1, Spinner spinner2) {
        String[] tagTypes = new String[]{Tag.TYPE_PERSON, Tag.TYPE_LOCATION};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, tagTypes);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner1.setAdapter(adapter);
        spinner2.setAdapter(adapter);
    }

    private void setupLogicalOperatorSpinner(Spinner spinner) {
        String[] operators = new String[]{"AND", "OR"};
        ArrayAdapter<String> adapter = new ArrayAdapter<>(
                this, android.R.layout.simple_spinner_item, operators);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);
    }

    private void setupAutocompleteTextViews(Spinner tagTypeSpinner,
                                            AutoCompleteTextView autoCompleteTextView,
                                            Map<String, Set<String>> tagValuesByType) {
        tagTypeSpinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                String selectedTagType = parent.getItemAtPosition(position).toString();
                Set<String> suggestions = tagValuesByType.getOrDefault(selectedTagType, new HashSet<>());
                ArrayAdapter<String> adapter = new ArrayAdapter<>(
                        MainActivity.this,
                        android.R.layout.simple_dropdown_item_1line,
                        new ArrayList<>(suggestions));
                autoCompleteTextView.setAdapter(adapter);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                // Do nothing
            }
        });

        // Enable filtering for autocomplete
        autoCompleteTextView.setOnItemClickListener((parent, view, position, id) -> {
            String selection = (String) parent.getItemAtPosition(position);
            autoCompleteTextView.setText(selection);
        });
    }

    private Map<String, Set<String>> getAllTagValuesByType() {
        Map<String, Set<String>> tagValuesByType = new HashMap<>();
        tagValuesByType.put(Tag.TYPE_PERSON, new HashSet<>());
        tagValuesByType.put(Tag.TYPE_LOCATION, new HashSet<>());

        // Collect all tag values from all photos across all albums
        albums = DataStorage.loadAlbumsFromStorage(this);
        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                for (Tag tag : photo.getTags()) {
                    Set<String> values = tagValuesByType.get(tag.getType());
                    if (values != null) {
                        values.add(tag.getValue());
                    }
                }
            }
        }

        return tagValuesByType;
    }

    private List<Photo> searchPhotosByTags(String tagType1, String tagValue1,
                                           String logicalOperator, String tagType2, String tagValue2) {
        Set<Photo> resultSet = new HashSet<>();

        for (Album album : albums) {
            for (Photo photo : album.getPhotos()) {
                boolean match1 = matchesTagCondition(photo, tagType1, tagValue1);

                if (tagType2.isEmpty() || tagValue2.isEmpty()) {
                    if (match1) resultSet.add(photo);
                    continue;
                }

                boolean match2 = matchesTagCondition(photo, tagType2, tagValue2);

                if (logicalOperator.equalsIgnoreCase("AND")) {
                    if (match1 && match2) resultSet.add(photo);
                } else if (logicalOperator.equalsIgnoreCase("OR")) {
                    if (match1 || match2) resultSet.add(photo);
                }
            }
        }

        return new ArrayList<>(resultSet);
    }

    private boolean matchesTagCondition(Photo photo, String tagType, String tagValuePrefix) {
        for (Tag tag : photo.getTags()) {
            if (tag.getType().equalsIgnoreCase(tagType)
                    && tag.getValue().toLowerCase().startsWith(tagValuePrefix.toLowerCase())) {
                return true;
            }
        }
        return false;
    }

    private void showSearchResults(List<Photo> searchResults) {
        if (searchResults.isEmpty()) {
            Toast.makeText(this, "No matching photos found.", Toast.LENGTH_SHORT).show();
            return;
        }

        Intent intent = new Intent(this, AlbumActivity.class);
        intent.putExtra("is_search_result", true);
        intent.putExtra("search_results", new ArrayList<>(searchResults));
        intent.putExtra("album_name", "Search Results"); // placeholder
        startActivity(intent);
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
