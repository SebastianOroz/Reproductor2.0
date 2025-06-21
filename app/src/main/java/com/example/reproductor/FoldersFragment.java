package com.example.reproductor;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.io.File;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;

// FoldersFragment.java
public class FoldersFragment extends Fragment {

    private ArrayList<Folder> folderList = new ArrayList<>();
    private FolderAdapter adapter;
    private OnFolderSelectedListener mListener;

    public interface OnFolderSelectedListener {
        void onFolderSelected(String folderPath);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnFolderSelectedListener) {
            mListener = (OnFolderSelectedListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnFolderSelectedListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_folders, container, false);
        ListView folderListView = view.findViewById(R.id.folderListView);

        loadFolders();

        adapter = new FolderAdapter(getContext(), folderList);
        folderListView.setAdapter(adapter);

        folderListView.setOnItemClickListener((parent, view1, position, id) -> {
            String path = folderList.get(position).getPath();
            mListener.onFolderSelected(path); // Notificamos a la Activity
        });

        return view;
    }

    private void loadFolders() {
        // Lógica para cargar carpetas desde MediaStore
        // (Verás esta lógica integrada en MainActivity en el siguiente paso)
        // CORRECTED: Changed getSongList() to getMasterSongList()
        ArrayList<Song> allSongs = ((MainActivity) requireActivity()).getMasterSongList();
        if (allSongs == null || allSongs.isEmpty()) return;

        HashMap<String, Folder> folderMap = new HashMap<>();

        for (Song song : allSongs) {
            String path = song.getPath();
            File file = new File(path);
            String parentPath = file.getParent();
            String parentName = file.getParentFile().getName();

            if (parentPath != null) { // Add a null check for parentPath
                if (folderMap.containsKey(parentPath)) {
                    folderMap.get(parentPath).setSongCount(folderMap.get(parentPath).getSongCount() + 1);
                } else {
                    // Ensure parentName is not null for Folder constructor
                    if (parentName == null) {
                        parentName = "Unknown Folder"; // Or handle as appropriate
                    }
                    Folder folder = new Folder(parentName, parentPath, 1);
                    folderMap.put(parentPath, folder);
                }
            }
        }
        folderList.clear();
        folderList.addAll(folderMap.values());

        // Ordenar carpetas alfabéticamente
        Collections.sort(folderList, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));
    }
}