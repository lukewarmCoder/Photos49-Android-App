package com.example.photos49.adapters;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

import com.example.photos49.R;
import com.example.photos49.models.Album;

public class AlbumAdapter extends RecyclerView.Adapter<AlbumAdapter.AlbumViewHolder> {
    private final List<Album> albums;
    private final OnAlbumClickListener listener;

    public interface OnAlbumClickListener {
        void onAlbumClick(Album album);
        void onAlbumMenuClick(Album album, View view);
    }

    public AlbumAdapter(List<Album> albums, OnAlbumClickListener listener) {
        this.albums = albums;
        this.listener = listener;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.album_item, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        Album album = albums.get(position);
        holder.bind(album);
    }

    @Override
    public int getItemCount() {
        return albums.size();
    }

    public void addAlbum(Album album) {
        albums.add(album);
        notifyItemInserted(albums.size() - 1);
    }

    class AlbumViewHolder extends RecyclerView.ViewHolder {
        private final TextView albumTitle;
        private final ImageView albumMenu;

        public AlbumViewHolder(@NonNull View itemView) {
            super(itemView);
            albumTitle = itemView.findViewById(R.id.albumTitle);
            albumMenu = itemView.findViewById(R.id.albumMenu);
        }

        public void bind(Album album) {
            albumTitle.setText(album.getName());

            itemView.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onAlbumClick(albums.get(getAdapterPosition()));
                }
            });

            albumMenu.setOnClickListener(v -> {
                if (listener != null && getAdapterPosition() != RecyclerView.NO_POSITION) {
                    listener.onAlbumMenuClick(albums.get(getAdapterPosition()), albumMenu);
                }
            });
        }
    }
}