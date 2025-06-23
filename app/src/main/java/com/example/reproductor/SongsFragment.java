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
        void showAddToPlaylistDialog(List<Long> songIds);
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

            if (adapter.isInSelectionMode()) {
                adapter.toggleSelection(clickedSong);
            } else {
                mListener.onSongSelected(position);
            }
        });

        songListView.setOnItemLongClickListener((parent, view1, position, id) -> {

            if (!adapter.isInSelectionMode()) {
                adapter.setInSelectionMode(true);
            }
            Song longClickedSong = displayableSongs.get(position);
            adapter.toggleSelection(longClickedSong);
            return true;
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


    @Override
    public void onOptionsButtonClick(Song song) {

        if (!adapter.isInSelectionMode()) {
            AlertDialog.Builder builder = new AlertDialog.Builder(requireContext());
            builder.setTitle(song.getTitle());
            String[] options = {"Ver Detalles", "Añadir a Playlist"};
            builder.setItems(options, (dialog, which) -> {
                switch (which) {
                    case 0:
                        mListener.showSongDetails(song);
                        break;
                    case 1:
                        List<Long> songIds = new ArrayList<>();
                        songIds.add(song.getId());
                        mListener.showAddToPlaylistDialog(songIds);
                        break;
                }
            });
            builder.show();
        } else {

            Toast.makeText(getContext(), "Usa las opciones de la barra superior para selección múltiple.", Toast.LENGTH_SHORT).show();
        }
    }


    @Override
    public void onSongSelected(Song song) {

        if (mListener != null && adapter.isInSelectionMode()) {
            mListener.onSelectionModeChanged(true);
        }
    }

    @Override
    public void onSongDeselected(Song song) {

        if (mListener != null) {

            if (adapter.getSelectedSongIds().isEmpty()) {
                mListener.onSelectionModeChanged(false);
            } else {
                mListener.onSelectionModeChanged(true);
            }
        }
    }

    @Override
    public void onSelectionModeChanged(boolean inSelectionMode) {
        mListener.onSelectionModeChanged(inSelectionMode);
    }

    public void exitSelectionMode() {
        if (adapter != null) {
            adapter.clearSelection();

        }
    }

    public List<Long> getSelectedSongIds() {
        if (adapter != null) {
            return adapter.getSelectedSongIds();
        }
        return new ArrayList<>();
    }
}