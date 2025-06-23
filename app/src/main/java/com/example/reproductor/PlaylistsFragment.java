package com.example.reproductor;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.Collections;

public class PlaylistsFragment extends Fragment {

    private ListView playlistsListView;
    private PlaylistAdapter adapter;
    private ArrayList<Playlist> playlists;
    private OnPlaylistInteractionListener mListener;

    public interface OnPlaylistInteractionListener {
        ArrayList<Playlist> getAllPlaylists();
        void savePlaylists();
        void addPlaylist(Playlist playlist);
        void playPlaylist(Playlist playlist);
    }

    @Override
    public void onAttach(@NonNull Context context) {
        super.onAttach(context);
        if (context instanceof OnPlaylistInteractionListener) {
            mListener = (OnPlaylistInteractionListener) context;
        } else {
            throw new RuntimeException(context.toString() + " must implement OnPlaylistInteractionListener");
        }
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_playlists, container, false);

        playlistsListView = view.findViewById(R.id.playlistsListView);
        FloatingActionButton fabAddPlaylist = view.findViewById(R.id.fabAddPlaylist);


        playlists = mListener.getAllPlaylists();
        adapter = new PlaylistAdapter(getContext(), playlists);
        playlistsListView.setAdapter(adapter);

        fabAddPlaylist.setOnClickListener(v -> showCreatePlaylistDialog());

        playlistsListView.setOnItemClickListener((parent, view1, position, id) -> {
            Playlist selectedPlaylist = playlists.get(position);
            mListener.playPlaylist(selectedPlaylist);
        });


        playlistsListView.setOnItemLongClickListener((parent, view1, position, id) -> {
            Playlist selectedPlaylist = playlists.get(position);
            showPlaylistOptionsDialog(selectedPlaylist, position);
            return true;
        });

        return view;
    }

    @Override
    public void onResume() {
        super.onResume();

        if (adapter != null) {
            adapter.notifyDataSetChanged();
        }
    }

    public void notifyAdapterChange() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            playlistsListView.invalidateViews();
        }
    }

    private void showCreatePlaylistDialog() {
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle("Crear Nueva Playlist");

        final EditText input = new EditText(requireContext());
        input.setHint("Nombre de la playlist");
        builder.setView(input);

        builder.setPositiveButton("Crear", (dialog, which) -> {
            String playlistName = input.getText().toString().trim();
            if (playlistName.isEmpty()) {
                Toast.makeText(requireContext(), "El nombre de la playlist no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }


            for (Playlist p : playlists) {
                if (p.getName().equalsIgnoreCase(playlistName)) {
                    Toast.makeText(requireContext(), "Ya existe una playlist con ese nombre.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Playlist newPlaylist = new Playlist(playlistName);
            mListener.addPlaylist(newPlaylist);
            Toast.makeText(requireContext(), "Playlist '" + playlistName + "' creada.", Toast.LENGTH_SHORT).show();
            notifyAdapterChange();
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> dialog.cancel());
        builder.show();
    }

    private void showPlaylistOptionsDialog(Playlist playlist, int position) {
        String[] options = {"Ver Canciones", "Eliminar Playlist"};
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(playlist.getName());
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0:

                    Toast.makeText(requireContext(), "Funcionalidad 'Ver Canciones' pendiente.", Toast.LENGTH_SHORT).show();
                    break;
                case 1:
                    showDeletePlaylistConfirmation(playlist, position);
                    break;
            }
        });
        builder.show();
    }

    private void showDeletePlaylistConfirmation(Playlist playlist, int position) {
        new AlertDialog.Builder(requireContext())
                .setTitle("Eliminar Playlist")
                .setMessage("¿Estás seguro de que quieres eliminar la playlist '" + playlist.getName() + "'?")
                .setPositiveButton("Eliminar", (dialog, which) -> {
                    playlists.remove(position);
                    mListener.savePlaylists();
                    notifyAdapterChange();
                    Toast.makeText(requireContext(), "Playlist eliminada.", Toast.LENGTH_SHORT).show();
                })
                .setNegativeButton("Cancelar", null)
                .show();
    }
}