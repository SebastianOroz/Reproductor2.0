package com.example.reproductor;

import android.content.Context;
import android.support.annotation.NonNull;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.annotation.Nullable;

import java.util.ArrayList;

// FolderAdapter.java
public class FolderAdapter extends ArrayAdapter<Folder> {
    public FolderAdapter(Context context, ArrayList<Folder> folders) {
        super(context, android.R.layout.simple_list_item_2, folders);
    }

    @NonNull
    @Override
    public View getView(int position, @Nullable View convertView, @NonNull ViewGroup parent) {
        Folder folder = getItem(position);
        if (convertView == null) {
            convertView = LayoutInflater.from(getContext()).inflate(android.R.layout.simple_list_item_2, parent, false);
        }
        TextView text1 = convertView.findViewById(android.R.id.text1);
        TextView text2 = convertView.findViewById(android.R.id.text2);

        text1.setText(folder.getName());
        text2.setText(folder.getSongCount() + " canciones");
        return convertView;
    }
}