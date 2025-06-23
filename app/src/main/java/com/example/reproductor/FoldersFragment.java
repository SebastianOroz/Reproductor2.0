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

public class FoldersFragment extends Fragment {

    private ListView folderListView;
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

        folderListView = view.findViewById(R.id.folderListView);


        ArrayList<Folder> folders = ((MainActivity) requireActivity()).getDisplayableFoldersList();

        adapter = new FolderAdapter(getContext(), folders);
        folderListView.setAdapter(adapter);

        folderListView.setOnItemClickListener((parent, view1, position, id) -> {
            String path = folders.get(position).getPath();
            mListener.onFolderSelected(path);
        });



        return view;
    }


    public void notifyAdapterChange() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            folderListView.invalidateViews();
        }
    }


    public ListView getFolderListView() {
        return folderListView;
    }


}