package com.example.reproductor;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;

import java.util.ArrayList;

public class PlaylistAdapter extends ArrayAdapter<Playlist> {
    private final Context context;

    public PlaylistAdapter(Context context, ArrayList<Playlist> playlists) {
        super(context, 0, playlists);
        this.context = context;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Playlist playlist = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_playlist, parent, false);
            holder = new ViewHolder();
            holder.playlistName = convertView.findViewById(R.id.playlistNameTextView);
            holder.playlistSongCount = convertView.findViewById(R.id.playlistSongCountTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (playlist != null) {
            holder.playlistName.setText(playlist.getName());
            holder.playlistSongCount.setText(playlist.getSongCount() + " canciones");
        }
        return convertView;
    }

    static class ViewHolder {
        TextView playlistName;
        TextView playlistSongCount;
    }
}