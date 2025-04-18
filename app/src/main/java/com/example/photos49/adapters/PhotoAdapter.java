package com.example.photos49.adapters;

import android.content.Context;
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
import com.example.photos49.models.Photo;

import java.util.List;

public class PhotoAdapter extends RecyclerView.Adapter<PhotoAdapter.PhotoViewHolder> {

    public interface OnPhotoActionListener {
        void onPhotoMenuClick(View view, Photo photo, int position);
    }

    private Context context;
    private List<Photo> photos;
    private OnPhotoActionListener listener;

    public PhotoAdapter(Context context, List<Photo> photos, OnPhotoActionListener listener) {
        this.context = context;
        this.photos = photos;
        this.listener = listener;
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
    }

    @Override
    public int getItemCount() {
        return photos.size();
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