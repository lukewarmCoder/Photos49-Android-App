package com.example.photos49.activities; // Replace with your package name

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.content.Intent;
import android.content.res.ColorStateList;
import android.graphics.Color;
import android.graphics.drawable.Drawable;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.text.InputType;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.FrameLayout;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.graphics.drawable.DrawableCompat;
import androidx.viewpager2.widget.ViewPager2;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos49.R;
import com.example.photos49.models.Album;
import com.example.photos49.models.Photo;
import com.example.photos49.models.Tag;
import com.example.photos49.util.DataStorage;
import com.google.android.material.chip.Chip;
import com.google.android.material.chip.ChipGroup;
import com.google.android.material.textfield.TextInputLayout;

import java.util.ArrayList;
import java.util.List;

public class PhotoDisplayActivity extends AppCompatActivity {

    private ViewPager2 photoViewPager;
    private TextView photoCounter;
    private ChipGroup tagChipGroup;
    private ImageButton btnPrevious, btnNext;
    private String albumTitle;
    private Album album;
    private List<Photo> photos;
    private int currentPosition;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_photo_display);

        // Set up toolbar
        Toolbar toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);
        getSupportActionBar().setDisplayShowHomeEnabled(true);
        getSupportActionBar().setDisplayShowTitleEnabled(false);

        // Set navigation icon tint to white
        Drawable navigationIcon = toolbar.getNavigationIcon();
        if (navigationIcon != null) {
            navigationIcon = DrawableCompat.wrap(navigationIcon).mutate();
            DrawableCompat.setTint(navigationIcon, Color.WHITE);
            toolbar.setNavigationIcon(navigationIcon);
        }

        // Initialize views
        photoViewPager = findViewById(R.id.photoViewPager);
        photoCounter = findViewById(R.id.photoCounter);
        tagChipGroup = findViewById(R.id.tagChipGroup);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnNext = findViewById(R.id.btnNext);

        // Get data from intent
        albumTitle = getIntent().getStringExtra("album_name");
        currentPosition = getIntent().getIntExtra("photo_position", 0);

        TextView toolbarTitle = findViewById(R.id.toolbarTitle);
        toolbarTitle.setText(albumTitle != null ? albumTitle : "Photo");

        // Load album
        loadAlbum();

        // Set up ViewPager
        if (album != null && !photos.isEmpty()) {
            PhotoPagerAdapter adapter = new PhotoPagerAdapter(this, photos);
            photoViewPager.setAdapter(adapter);
            photoViewPager.setCurrentItem(currentPosition, false);
            updatePhotoCounter();

            // Display tags for the current photo
            displayTags();
        }

        // Set up navigation buttons
        btnPrevious.setOnClickListener(v -> navigateToPrevious());
        btnNext.setOnClickListener(v -> navigateToNext());

        // Set up ViewPager page change callback
        photoViewPager.registerOnPageChangeCallback(new ViewPager2.OnPageChangeCallback() {
            @Override
            public void onPageSelected(int position) {
                currentPosition = position;
                updatePhotoCounter();
                displayTags();
            }
        });

        // Set up action buttons
        findViewById(R.id.btnMove).setOnClickListener(v -> moveCurrentPhoto());
        findViewById(R.id.btnDelete).setOnClickListener(v -> deleteCurrentPhoto());
        findViewById(R.id.btnAddTag).setOnClickListener(v -> showAddTagDialog());
    }

    private void loadAlbum() {
        List<Album> albums = DataStorage.loadAlbumsFromStorage(this);
        for (Album a : albums) {
            if (a.getName().equals(albumTitle)) {
                album = a;
                photos = album.getPhotos();
                break;
            }
        }

        if (album == null || photos == null || photos.isEmpty()) {
            Toast.makeText(this, "No photos found!", Toast.LENGTH_SHORT).show();
            finish();
        }
    }

    private void updatePhotoCounter() {
        if (photos != null && !photos.isEmpty()) {
            photoCounter.setText((currentPosition + 1) + "/" + photos.size());
        }
    }

    private void navigateToPrevious() {
        if (currentPosition > 0) {
            currentPosition--;
            photoViewPager.setCurrentItem(currentPosition, true);
        }
    }

    private void navigateToNext() {
        if (currentPosition < photos.size() - 1) {
            currentPosition++;
            photoViewPager.setCurrentItem(currentPosition, true);
        }
    }

    private void moveCurrentPhoto() {
        if (photos == null || photos.isEmpty() || currentPosition >= photos.size()) {
            return;
        }

        Photo currentPhoto = photos.get(currentPosition);
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

                    for (Album a : allAlbums) {
                        if (a.getName().equals(targetAlbumName)) {
                            targetAlbum = a;
                            break;
                        }
                    }

                    if (targetAlbum == null) {
                        Toast.makeText(this, "Album not found.", Toast.LENGTH_SHORT).show();
                        return;
                    }

                    // Avoid duplicates in the target album
                    for (Photo p : targetAlbum.getPhotos()) {
                        if (p.getUri().equals(currentPhoto.getUri())) {
                            Toast.makeText(this, "Photo already exists in selected album.", Toast.LENGTH_SHORT).show();
                            return;
                        }
                    }

                    // Remove from current album in memory
                    photos.remove(currentPosition);

                    // Update all albums in storage at once
                    for (Album a : allAlbums) {
                        if (a.getName().equals(targetAlbumName)) {
                            // Add to target album
                            a.addPhoto(currentPhoto);
                        } else if (a.getName().equals(albumTitle)) {
                            // Update source album
                            a.setPhotos(photos);
                        }
                    }

                    // Save changes
                    DataStorage.saveAlbumsToStorage(this, allAlbums);

                    // Update UI
                    photoViewPager.getAdapter().notifyItemRemoved(currentPosition);
                    if (photos.isEmpty()) {
                        finish();
                    } else {
                        if (currentPosition >= photos.size()) {
                            currentPosition = photos.size() - 1;
                        }
                        photoViewPager.setCurrentItem(currentPosition, false);
                        updatePhotoCounter();
                    }

                    Toast.makeText(this, "Photo moved to " + targetAlbumName, Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void deleteCurrentPhoto() {
        if (photos == null || photos.isEmpty() || currentPosition >= photos.size()) {
            return;
        }

        new AlertDialog.Builder(this)
                .setTitle("Delete Photo")
                .setMessage("Are you sure you want to delete this photo?")
                .setPositiveButton("Delete", (dialog, which) -> {
                    Photo photoToDelete = photos.get(currentPosition);
                    photos.remove(currentPosition);

                    // Save changes
                    List<Album> albums = DataStorage.loadAlbumsFromStorage(this);
                    for (Album a : albums) {
                        if (a.getName().equals(albumTitle)) {
                            a.setPhotos(photos);
                            break;
                        }
                    }
                    DataStorage.saveAlbumsToStorage(this, albums);

                    // Update UI
                    photoViewPager.getAdapter().notifyItemRemoved(currentPosition);
                    if (photos.isEmpty()) {
                        finish();
                    } else {
                        if (currentPosition >= photos.size()) {
                            currentPosition = photos.size() - 1;
                        }
                        photoViewPager.setCurrentItem(currentPosition, false);
                        updatePhotoCounter();
                    }
                })
                .setNegativeButton("Cancel", null)
                .show();
    }

    private void showAddTagDialog() {
        if (photos == null || photos.isEmpty() || currentPosition >= photos.size()) {
            return;
        }

        // Create dialog view
        LayoutInflater inflater = LayoutInflater.from(this);
        View dialogView = inflater.inflate(R.layout.dialog_add_tag, null);

        // Initialize views
        final RadioGroup radioGroupTagType = dialogView.findViewById(R.id.radioGroupTagType);
        final TextInputLayout inputLayoutTagValue = dialogView.findViewById(R.id.inputLayoutTagValue);
        final EditText editTextTagValue = dialogView.findViewById(R.id.editTextTagValue);

        // Get current photo
        Photo currentPhoto = photos.get(currentPosition);

        // Check if location tag already exists
        final boolean hasLocationTag = false; // Make it final here
        for (Tag tag : currentPhoto.getTags()) {
            if (tag.getType().equals(Tag.TYPE_LOCATION)) {
                // Can't modify a final variable, so we need another approach
                // hasLocationTag = true; - This would cause the error
                break;
            }
        }

        // Instead, let's determine this at the time it's needed
        final boolean[] locationTagExists = {false}; // Using an array as a mutable container
        for (Tag tag : currentPhoto.getTags()) {
            if (tag.getType().equals(Tag.TYPE_LOCATION)) {
                locationTagExists[0] = true;
                break;
            }
        }

        // Create alert dialog
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Add Tag");
        builder.setView(dialogView);

        // Set action buttons
        builder.setPositiveButton("Add", null); // We'll override this later
        builder.setNegativeButton("Cancel", null);

        // Create and show the dialog
        final AlertDialog dialog = builder.create();
        dialog.show();

        // Override the positive button to prevent automatic dismissal on input errors
        dialog.getButton(AlertDialog.BUTTON_POSITIVE).setOnClickListener(v -> {
            // Get selected tag type
            int selectedId = radioGroupTagType.getCheckedRadioButtonId();
            RadioButton radioButton = dialogView.findViewById(selectedId);

            if (radioButton == null) {
                Toast.makeText(PhotoDisplayActivity.this, "Please select a tag type", Toast.LENGTH_SHORT).show();
                return;
            }

            final String tagType = radioButton.getText().toString().toLowerCase();
            final String tagValue = editTextTagValue.getText().toString().trim();

            // Validate input
            if (tagValue.isEmpty()) {
                editTextTagValue.setError("Please enter a value");
                return;
            }

            // Check if we're adding a location tag when one already exists
            if (tagType.equals(Tag.TYPE_LOCATION) && locationTagExists[0]) {
                // Confirm replacement of existing location tag
                new AlertDialog.Builder(PhotoDisplayActivity.this)
                        .setTitle("Replace Location Tag")
                        .setMessage("This photo already has a location tag. Do you want to replace it?")
                        .setPositiveButton("Replace", new DialogInterface.OnClickListener() {
                            @Override
                            public void onClick(DialogInterface dialog2, int which) {
                                addTagToPhoto(tagType, tagValue);
                                dialog.dismiss();
                            }
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                // Add the tag
                addTagToPhoto(tagType, tagValue);
                dialog.dismiss();
            }
        });
    }

    private void addTagToPhoto(String tagType, String tagValue) {
        Photo currentPhoto = photos.get(currentPosition);
        Tag newTag = new Tag(tagType, tagValue);
        currentPhoto.addTag(newTag);

        // Save changes
        saveChanges();

        // Update tag display
        displayTags();

        Toast.makeText(this, "Tag added", Toast.LENGTH_SHORT).show();
    }

    private void displayTags() {
        // Clear existing tags
        tagChipGroup.removeAllViews();

        // Get current photo
        if (photos != null && currentPosition < photos.size()) {
            Photo currentPhoto = photos.get(currentPosition);
            List<Tag> tags = currentPhoto.getTags();

            if (tags != null && !tags.isEmpty()) {
                for (final Tag tag : tags) {
                    // Create chip for this tag
                    Chip chip = new Chip(this);
                    chip.setText(tag.toString());
                    chip.setTextColor(Color.WHITE);

                    // Set different colors for different tag types
                    if (tag.getType().equals(Tag.TYPE_PERSON)) {
                        chip.setChipBackgroundColorResource(R.color.cornflowerBlue); // Person tag color
                    } else {
                        chip.setChipBackgroundColorResource(R.color.darkCyan); // Location tag color
                    }

                    // Add close icon to allow tag removal
                    chip.setCloseIconVisible(true);
                    chip.setCloseIconTint(ColorStateList.valueOf(Color.WHITE));
                    chip.setOnCloseIconClickListener(v -> {
                        // Remove tag
                        currentPhoto.removeTag(tag);

                        // Save changes
                        List<Album> albums = DataStorage.loadAlbumsFromStorage(this);
                        for (Album a : albums) {
                            if (a.getName().equals(albumTitle)) {
                                a.setPhotos(photos);
                                break;
                            }
                        }
                        DataStorage.saveAlbumsToStorage(this, albums);

                        // Update tag display
                        tagChipGroup.removeView(chip);

                        Toast.makeText(PhotoDisplayActivity.this, "Tag removed", Toast.LENGTH_SHORT).show();
                    });

                    tagChipGroup.addView(chip);
                }
            }
        }
    }

    private void saveChanges() {
        List<Album> albums = DataStorage.loadAlbumsFromStorage(this);
        boolean found = false;

        for (Album a : albums) {
            if (a.getName().equals(albumTitle)) {
                // Directly update the photo in the album
                List<Photo> albumPhotos = a.getPhotos();
                if (currentPosition < albumPhotos.size()) {
                    albumPhotos.set(currentPosition, photos.get(currentPosition));
                }
                found = true;
                break;
            }
        }

        if (found) {
            // Save all albums
            DataStorage.saveAlbumsToStorage(this, albums);
        } else {
            Log.e("PhotoDisplayActivity", "Could not find album " + albumTitle + " to save changes");
        }
    }

    @Override
    public boolean onSupportNavigateUp() {
        finish();
        return true;
    }

    // ViewPager adapter for photos
    private static class PhotoPagerAdapter extends RecyclerView.Adapter<PhotoPagerAdapter.PhotoViewHolder> {
        private Context context;
        private List<Photo> photos;

        public PhotoPagerAdapter(Context context, List<Photo> photos) {
            this.context = context;
            this.photos = photos;
        }

        @NonNull
        @Override
        public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
            View view = LayoutInflater.from(context).inflate(R.layout.item_photo_pager, parent, false);
            return new PhotoViewHolder(view);
        }

        @Override
        public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
            Photo photo = photos.get(position);
            holder.bind(photo);
        }

        @Override
        public int getItemCount() {
            return photos.size();
        }

        static class PhotoViewHolder extends RecyclerView.ViewHolder {
            ImageView photoImageView;

            public PhotoViewHolder(@NonNull View itemView) {
                super(itemView);
                photoImageView = itemView.findViewById(R.id.photoImageView);
            }

            public void bind(Photo photo) {
                photoImageView.setImageURI(android.net.Uri.parse(photo.getUri()));
            }
        }
    }
}