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

public class FolderAdapter extends ArrayAdapter<Folder> {
    private final Context context;
    private final ArrayList<Folder> folders;

    public FolderAdapter(Context context, ArrayList<Folder> folders) {
        super(context, 0, folders);
        this.context = context;
        this.folders = folders;
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Folder folder = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_folder, parent, false);
            holder = new ViewHolder();
            holder.folderName = convertView.findViewById(R.id.folderNameTextView);
            holder.songCount = convertView.findViewById(R.id.songCountTextView);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (folder != null) {
            holder.folderName.setText(folder.getName());
            holder.songCount.setText(folder.getSongCount() + " canciones");
        }
        return convertView;
    }


    static class ViewHolder {
        TextView folderName;
        TextView songCount;
    }
}