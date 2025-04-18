package com.example.photos49.adapters;

import android.app.Activity;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.view.LayoutInflater;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.PopupMenu;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AlertDialog;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos49.R;
import com.example.photos49.activities.AlbumActivity;
import com.example.photos49.activities.PhotoDisplayActivity;
import com.example.photos49.models.Album;
import com.example.photos49.models.Photo;
import com.example.photos49.util.DataStorage;

import java.util.ArrayList;
import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    public interface OnPhotoActionListener {
        void onPhotoMenuClick(View view, Photo photo, int position);
    }

    private Context context;
    private List<Photo> photos;
    private OnPhotoActionListener listener;
    private String albumTitle;

    public PhotoAdapter(Context context, List<Photo> photos, String albumTitle, OnPhotoActionListener listener) {
        this.context = context;
        this.photos = photos;
        this.listener = listener;
        this.albumTitle = albumTitle;
    }

    @NonNull
    @Override
    public PhotoViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(context).inflate(R.layout.photo_item, parent, false);
        return new PhotoViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull PhotoViewHolder holder, int position) {
        Photo photo = photos.get(position);
        Uri uri = Uri.parse(photo.getUri());
        holder.photoThumbnail.setImageURI(uri); // Assumes image is local

        holder.photoMenu.setOnClickListener(v -> {
            if (listener != null) {
                listener.onPhotoMenuClick(v, photo, position);
            }
        });

        // Add click listener to the thumbnail to open PhotoDisplayActivity
        holder.itemView.setOnClickListener(v -> {
            Photo clickedPhoto = photos.get(position);

            if (albumTitle.equals("Search Results") && context instanceof Activity) {
                // Show the "View Original Album" dialog
                new AlertDialog.Builder(context)
                        .setTitle("Search Result")
                        .setMessage("This photo is part of search results. Would you like to see it in its original album?")
                        .setPositiveButton("View Original", (dialog, which) -> {
                            // Find the original album in storage
                            List<Album> allAlbums = DataStorage.loadAlbumsFromStorage(context);
                            for (Album album : allAlbums) {
                                for (Photo p : album.getPhotos()) {
                                    if (p.getUri().equals(clickedPhoto.getUri())) {
                                        Intent intent = new Intent(context, AlbumActivity.class);
                                        intent.putExtra("album_name", album.getName());
                                        context.startActivity(intent);
                                        return;
                                    }
                                }
                            }

                            Toast.makeText(context, "Original album not found", Toast.LENGTH_SHORT).show();
                        })
                        .setNegativeButton("Cancel", null)
                        .show();
            } else {
                // Normal photo open behavior
                Intent intent = new Intent(context, PhotoDisplayActivity.class);
                intent.putExtra("album_name", albumTitle);
                intent.putExtra("photo_position", position);
                context.startActivity(intent);
            }
        });
    }

    @Override
    public int getItemCount() {
        return photos.size();
    }

    // First, add this method to your PhotoAdapter class:
    public void updateData(List<Photo> newPhotos) {
        this.photos = newPhotos;
        notifyDataSetChanged();
    }

    public static class PhotoViewHolder extends RecyclerView.ViewHolder {
        ImageView photoThumbnail, photoMenu;

        public PhotoViewHolder(@NonNull View itemView) {
            super(itemView);
            photoThumbnail = itemView.findViewById(R.id.photoThumbnail);
            photoMenu = itemView.findViewById(R.id.photoMenu);
        }
    }
}