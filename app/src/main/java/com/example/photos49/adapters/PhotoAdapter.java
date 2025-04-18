package com.example.photos49.adapters;

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

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.photos49.R;
import com.example.photos49.activities.PhotoDisplayActivity;
import com.example.photos49.models.Photo;

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
            Intent intent = new Intent(context, PhotoDisplayActivity.class);
            intent.putExtra("album_name", albumTitle);
            intent.putExtra("photo_position", position);
            context.startActivity(intent);
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