package com.example.reproductor;

import android.Manifest;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.provider.MediaStore;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity {

    private static final int PERMISSION_REQUEST_READ_STORAGE = 1;
    private ListView songListView;
    private ArrayList<Song> songList;
    private SongAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        // Inicializar vistas
        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        songListView = findViewById(R.id.songListView);
        songList = new ArrayList<>();
        adapter = new SongAdapter(this, songList);
        songListView.setAdapter(adapter);

        // Configurar el SwipeRefreshLayout
        swipeRefreshLayout.setOnRefreshListener(this::refreshSongList);

        // Configurar colores del indicador de refresh
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        // Verificar permisos
        checkStoragePermission();
    }

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            // Android 13+ usa READ_MEDIA_AUDIO
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission(Manifest.permission.READ_MEDIA_AUDIO);
            } else {
                loadSongs();
            }
        } else {
            // Android < 13 usa READ_EXTERNAL_STORAGE
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_EXTERNAL_STORAGE)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission(Manifest.permission.READ_EXTERNAL_STORAGE);
            } else {
                loadSongs();
            }
        }
    }

    private void requestPermission(String permission) {
        ActivityCompat.requestPermissions(this,
                new String[]{permission},
                PERMISSION_REQUEST_READ_STORAGE);
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSongs();
            } else {
                Toast.makeText(this, "Permiso denegado. No se pueden cargar canciones.", Toast.LENGTH_LONG).show();
                swipeRefreshLayout.setRefreshing(false);
            }
        }
    }

    private void refreshSongList() {
        // Limpiar la lista actual
        songList.clear();
        adapter.notifyDataSetChanged();

        // Volver a cargar las canciones
        loadSongs();
    }

    private void loadSongs() {
        new Thread(() -> {
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {
                    MediaStore.Audio.Media.TITLE,
                    MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DATA,
                    MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.ALBUM_ID,
                    MediaStore.Audio.Media.ALBUM // Nuevo campo para el nombre del álbum
            };
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";

            try (Cursor cursor = getContentResolver().query(uri, projection, selection, null, null)) {
                if (cursor != null) {
                    while (cursor.moveToNext()) {
                        String title = cursor.getString(0);
                        String artist = cursor.getString(1);
                        String path = cursor.getString(2);
                        long duration = cursor.getLong(3);
                        long albumId = cursor.getLong(4);
                        String album = cursor.getString(5); // Obtener nombre del álbum

                        songList.add(new Song(title, artist, path, duration, albumId, album));
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error al cargar canciones", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
            }

            runOnUiThread(() -> {
                adapter.notifyDataSetChanged();
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }).start();
    }
}