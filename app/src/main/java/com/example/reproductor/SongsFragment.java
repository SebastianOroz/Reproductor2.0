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
    protected SongAdapter adapter;
    private OnSongInteractionListener mListener;
    private SwipeRefreshLayout swipeRefreshLayout;

    public interface OnSongInteractionListener {
        ArrayList<Song> getDisplayableSongList();
        void onRefreshList();
        void onSongSelected(int position);
        void showAddToPlaylistDialog(List<Long> songIds); // ¡IMPORTANTE! Asegúrate de que acepta List<Long>
        void showSongDetails(Song song);
        void onSelectionModeChanged(boolean inSelectionMode);
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

        ArrayList<Song> displayableSongs = mListener.getDisplayableSongList();

        adapter = new SongAdapter(getContext(), displayableSongs);
        adapter.setOnOptionsButtonClickListener(this);
        adapter.setOnSongSelectionListener(this);
        songListView.setAdapter(adapter);

        songListView.setOnItemClickListener((parent, view1, position, id) -> {
            Song clickedSong = displayableSongs.get(position);
            // Si estamos en modo de selección, togglea la selección.
            // Si no, reproduce la canción.
            if (adapter.isInSelectionMode()) {
                adapter.toggleSelection(clickedSong);
            } else {
                mListener.onSongSelected(position);
            }
        });

        songListView.setOnItemLongClickListener((parent, view1, position, id) -> {
            // Al hacer clic largo, activar el modo de selección y seleccionar la canción
            if (!adapter.isInSelectionMode()) {
                adapter.setInSelectionMode(true); // Esto activará la CAB en MainActivity
            }
            Song longClickedSong = displayableSongs.get(position);
            adapter.toggleSelection(longClickedSong); // Selecciona/deselecciona la canción
            return true; // Consume el evento de clic largo
        });

        swipeRefreshLayout.setOnRefreshListener(() -> mListener.onRefreshList());

        return view;
    }

    public void notifyAdapterChange() {
        if (adapter != null) {
            adapter.notifyDataSetChanged();
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
        // ¡MODIFICADO! Solo muestra el diálogo de opciones si NO estamos en modo de selección
        if (!adapter.isInSelectionMode()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(song.getTitle());
            String[] options = {"Ver Detalles", "Añadir a Playlist"};
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0: // Ver Detalles
                        mListener.showSongDetails(song);
                        break;
                    case 1: // Añadir a Playlist
                        List<Long> songIds = new ArrayList<>();
                        songIds.add(song.getId());
                        mListener.showAddToPlaylistDialog(songIds); // Llama al método con una lista de 1 ID
                        break;
                }
            });
            builder.show();
        } else {
            // Si estamos en modo de selección, el botón de opciones individual
            // no debería hacer nada o quizás mostrar un Toast indicando que se use la CAB.
            Toast.makeText(getContext(), "Usa las opciones de la barra superior para selección múltiple.", Toast.LENGTH_SHORT).show();
        }
    }

    // Implementación de OnSongSelectionListener
    @Override
    public void onSongSelected(Song song) {
        // Notifica a MainActivity que la selección ha cambiado para actualizar la CAB
        if (mListener != null && adapter.isInSelectionMode()) {
            mListener.onSelectionModeChanged(true); // Manda true para forzar la actualización del título
        }
    }

    @Override
    public void onSongDeselected(Song song) {
        // Notifica a MainActivity que la selección ha cambiado para actualizar la CAB
        if (mListener != null) {
            // Si no quedan canciones seleccionadas, el adapter ya llamará a setInSelectionMode(false)
            // lo que a su vez llamará a onSelectionModeChanged(false) en MainActivity y cerrará la CAB.
            // Si quedan, solo actualiza el título.
            if (adapter.getSelectedSongIds().isEmpty()) {
                mListener.onSelectionModeChanged(false); // Sale del modo de selección
            } else {
                mListener.onSelectionModeChanged(true); // Se mantiene en modo, solo actualiza el título
            }
        }
    }

    @Override
    public void onSelectionModeChanged(boolean inSelectionMode) {
        // Esta callback la recibe MainActivity, no el propio fragmento de su propio adaptador.
        // Aquí no necesitas hacer nada, ya que MainActivity se encarga de la CAB.
        // Sin embargo, si quieres alguna acción visual en el fragmento (ej. un toast), puedes hacerlo.
        mListener.onSelectionModeChanged(inSelectionMode); // Re-envía la notificación a MainActivity
    }

    public void exitSelectionMode() {
        if (adapter != null) {
            adapter.clearSelection(); // Esto limpia la selección y setInSelectionMode(false)
            // La llamada a onSelectionModeChanged(false) en MainActivity cerrará la CAB
        }
    }

    public List<Long> getSelectedSongIds() {
        if (adapter != null) {
            return adapter.getSelectedSongIds();
        }
        return new ArrayList<>();
    }
}