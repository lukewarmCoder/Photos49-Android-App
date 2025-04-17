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
    private final AlbumClickCallback clickCallback;
    private final AlbumMenuCallback menuCallback;

    // Functional interfaces for actions
    public interface AlbumClickCallback {
        void onClick(Album album);
    }

    public interface AlbumMenuCallback {
        void onMenuClick(Album album, View view);
    }

    public AlbumAdapter(List<Album> albums,
                        AlbumClickCallback clickCallback,
                        AlbumMenuCallback menuCallback) {
        this.albums = albums;
        this.clickCallback = clickCallback;
        this.menuCallback = menuCallback;
    }

    @NonNull
    @Override
    public AlbumViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext())
                .inflate(R.layout.album_item, parent, false);
        return new AlbumViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull AlbumViewHolder holder, int position) {
        holder.bind(albums.get(position));
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
                if (getAdapterPosition() != RecyclerView.NO_POSITION && clickCallback != null) {
                    clickCallback.onClick(album);
                }
            });

            albumMenu.setOnClickListener(v -> {
                if (getAdapterPosition() != RecyclerView.NO_POSITION && menuCallback != null) {
                    menuCallback.onMenuClick(album, albumMenu);
                }
            });
        }
    }
}