package com.example.reproductor;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.app.AlertDialog;
import androidx.fragment.app.Fragment;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;
import java.util.List;

public class SongsFragment extends Fragment implements SongAdapter.OnOptionsButtonClickListener, SongAdapter.OnSongSelectionListener {

    private ListView songListView;
    SongAdapter adapter;
    private OnSongInteractionListener mListener; // Listener para interactuar con MainActivity
    private SwipeRefreshLayout swipeRefreshLayout;

    // Interface para la interacción con la MainActivity
    public interface OnSongInteractionListener {
        ArrayList<Song> getDisplayableSongList(); // Obtener la lista de canciones a mostrar
        void onRefreshList(); // Para refrescar la lista
        void onSongSelected(int position); // Para reproducir una canción
        void showAddToPlaylistDialog(List<Long> songIds); // NUEVO: Mostrar diálogo de añadir a playlist
        void showSongDetails(Song song); // NUEVO: Mostrar detalles de la canción
        void onSelectionModeChanged(boolean inSelectionMode); // NUEVO: Notificar a MainActivity sobre el modo de selección
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
        swipeRefreshLayout = view.findViewById(R.id.swipeRefreshLayout);

        // Obtener la lista de canciones desde MainActivity
        ArrayList<Song> displayableSongs = mListener.getDisplayableSongList();

        adapter = new SongAdapter(getContext(), displayableSongs);
        adapter.setOnOptionsButtonClickListener(this); // Asignar este fragmento como listener
        adapter.setOnSongSelectionListener(this); // ¡NUEVO! Asignar este fragmento como listener de selección
        songListView.setAdapter(adapter);

        songListView.setOnItemClickListener((parent, view1, position, id) -> {
            Song clickedSong = displayableSongs.get(position);
            if (adapter.isInSelectionMode()) {
                adapter.toggleSelection(clickedSong);
            } else {
                mListener.onSongSelected(position); // Reproducir la canción
            }
        });

        // ¡NUEVO! Manejar clic largo para activar selección múltiple
        songListView.setOnItemLongClickListener((parent, view1, position, id) -> {
            if (!adapter.isInSelectionMode()) {
                adapter.setInSelectionMode(true);
            }
            Song longClickedSong = displayableSongs.get(position);
            adapter.toggleSelection(longClickedSong);
            return true; // Consume el evento
        });


        swipeRefreshLayout.setOnRefreshListener(() -> mListener.onRefreshList());

        return view;
    }

    public void notifyAdapterChange() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
            songListView.invalidateViews(); // Forzar redibujado
        }
    }

    public void setRefreshing(boolean refreshing) {
        if (swipeRefreshLayout != null) {
            swipeRefreshLayout.setRefreshing(refreshing);
        }
    }

    public ListView getSongListView() {
        return songListView;
    }


    public boolean isInSelectionMode() {
        return adapter != null && adapter.isInSelectionMode();
    }

    // Implementación de OnOptionsButtonClickListener
    @Override
    public void onOptionsButtonClick(Song song) {
        // Muestra un diálogo para elegir entre ver detalles o añadir a playlist
        AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
        builder.setTitle(song.getTitle());
        String[] options = {"Ver Detalles", "Añadir a Playlist"};
        builder.setItems(options, (dialog, which) -> {
            switch (which) {
                case 0: // Ver Detalles
                    mListener.showSongDetails(song);
                    break;
                case 1: // Añadir a Playlist
                    // Crea una lista con una sola canción para añadir
                    List<Long> songIds = new ArrayList<>();
                    songIds.add(song.getId());
                    mListener.showAddToPlaylistDialog(songIds);
                    break;
            }
        });
        builder.show();
    }



    // Implementación de OnSongSelectionListener
    @Override
    public void onSongSelected(Song song) {
        // Puedes poner un Toast aquí o manejarlo en MainActivity si lo necesitas
        // Toast.makeText(getContext(), "Seleccionado: " + song.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSongDeselected(Song song) {
        // Puedes poner un Toast aquí o manejarlo en MainActivity si lo necesitas
        // Toast.makeText(getContext(), "Deseleccionado: " + song.getTitle(), Toast.LENGTH_SHORT).show();
    }

    @Override
    public void onSelectionModeChanged(boolean inSelectionMode) {
        mListener.onSelectionModeChanged(inSelectionMode); // Notifica a MainActivity
        // Podrías mostrar/ocultar una barra de herramientas de contexto aquí
        // Por ejemplo:
        // if (inSelectionMode) {
        //     Toast.makeText(getContext(), "Modo de selección activado", Toast.LENGTH_SHORT).show();
        // } else {
        //     Toast.makeText(getContext(), "Modo de selección desactivado", Toast.LENGTH_SHORT).show();
        // }
    }

    // Método para salir del modo de selección desde fuera (ej. un botón de "Cancelar")
    public void exitSelectionMode() {
        if (adapter != null) {
            adapter.clearSelection();
            // notifyDataSetChanged() se llama dentro de clearSelection
        }
    }

    public List<Long> getSelectedSongIds() {
        if (adapter != null) {
            return adapter.getSelectedSongIds();
        }
        return new ArrayList<>();
    }
}