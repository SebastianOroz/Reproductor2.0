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
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout; // Import for SwipeRefreshLayout

import java.util.ArrayList;

// SongsFragment.java
public class SongsFragment extends Fragment {

    private ListView songListView;
    private SongAdapter adapter;
    private ArrayList<Song> songList;
    // Interfaz para comunicarnos con MainActivity
    private OnSongInteractionListener mListener;
    private SwipeRefreshLayout swipeRefreshLayout; // Declare SwipeRefreshLayout

    public interface OnSongInteractionListener {
        void onSongSelected(int position);
        void onRefreshList(); // Added for pull-to-refresh
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnSongInteractionListener) {
            mListener = (OnSongInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnSongInteractionListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_songs, container, false);

        songListView = view.findViewById(R.id.songListView);
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout); // Initialize SwipeRefreshLayout
        songList = ((MainActivity) requireActivity()).getDisplayableSongList(); // Obtenemos la lista de la Activity

        adapter = new SongAdapter(getContext(), songList);
        songListView.setAdapter(adapter);

        songListView.setOnItemClickListener((parent, view1, position, id) -> {
            mListener.onSongSelected(position); // Notificamos a la Activity qué canción se seleccionó
        });

        // Setup the SwipeRefreshLayout listener
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setOnRefreshListener(() -> {
                if (mListener != null) {
                    mListener.onRefreshList(); // Trigger refresh in MainActivity
                }
            });
        }

        return view;
    }

    // Método para que MainActivity pueda notificar al adaptador de cambios (como al ordenar)
    public void notifyAdapterChange() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    // Método para setear el estado de refrescando del SwipeRefreshLayout
    public void setRefreshing(boolean refreshing) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(refreshing);
        }
    }
}