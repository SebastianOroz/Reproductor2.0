package com.example.reproductor;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
import android.widget.Button;
import android.widget.ImageView;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.provider.MediaStore;
import android.view.Gravity;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.ListView;
import android.widget.PopupWindow;
import android.widget.RadioButton;
import android.widget.RadioGroup;
import android.widget.RelativeLayout;
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;



import android.content.Context;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.inputmethod.InputMethodManager;
import android.widget.EditText;
import android.widget.LinearLayout;




import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.app.AppCompatDelegate;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.preference.PreferenceManager;
import androidx.viewpager2.widget.ViewPager2;

import com.google.android.material.navigation.NavigationView;
import com.google.android.material.tabs.TabLayout;
import com.google.android.material.tabs.TabLayoutMediator;





import android.content.SharedPreferences;
import androidx.preference.PreferenceManager;
import android.graphics.Bitmap;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.Drawable;
import android.widget.ImageView;

import com.google.gson.Gson;
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Callback;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SongAdapter.OnOptionsButtonClickListener,
        SongsFragment.OnSongInteractionListener,
        FoldersFragment.OnFolderSelectedListener,
        PlaylistsFragment.OnPlaylistInteractionListener{


    //Declaraciones
    private static final int PERMISSION_REQUEST_READ_STORAGE = 1;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;


    private ArrayList<Playlist> allPlaylists; // Lista de todas las playlists
    private String PLAYLISTS_PREFS = "playlists_prefs";
    private ArrayList<Folder> allFoldersList;
    private Drawable customBackgroundDrawable;

    private MediaPlayer mediaPlayer;
    private int currentSongIndex = -1;
    private ImageButton btnPlay, btnNext, btnPrevious, btnShuffle, btnSort;
    private SeekBar songProgressBar;
    private Handler handler = new Handler();
    private LinearLayout songInfoLayout;
    private ImageView currentSongAlbumArt;
    private TextView currentSongTitle;

    private ArrayList<Song> allSongsList;
    private ArrayList<Song> currentDisplayList;
    private ArrayList<Song> shuffledSongList;
    private boolean isShuffleOn = false;
    private Random random = new Random();



    private ImageView customBackgroundImageView;

    private static final String SORT_ORDER_NAME_ASC = "name_asc";
    private static final String SORT_ORDER_NAME_DESC = "name_desc";
    private static final String SORT_ORDER_ARTIST = "artist";
    private static final String SORT_ORDER_DURATION_ASC = "duration_asc";
    private static final String SORT_ORDER_DURATION_DESC = "duration_desc";
    private static final String SORT_ORDER_DATE_ADDED_ASC = "date_added_asc";
    private static final String SORT_ORDER_DATE_ADDED_DESC = "date_added_desc";
    private static final String SORT_ORDER_DATE_MODIFIED_ASC = "date_modified_asc";
    private static final String SORT_ORDER_DATE_MODIFIED_DESC = "date_modified_desc";

    private String currentSortOrder = SORT_ORDER_NAME_ASC;

    private LinearLayout searchLayout;
    private EditText searchEditText;
    private ImageButton btnClearSearch;
    private ImageButton btnCloseSearch;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        applyAppTheme();
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


//Inicializar

        allSongsList = new ArrayList<>();
        currentDisplayList = new ArrayList<>();
        shuffledSongList = new ArrayList<>();



        allFoldersList = new ArrayList<>();
        allPlaylists = new ArrayList<>();

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);
        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                this, drawerLayout, toolbar, R.string.navigation_drawer_open, R.string.navigation_drawer_close);
        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        viewPager = findViewById(R.id.viewPager);
        tabLayout = findViewById(R.id.tabLayout);
        customBackgroundImageView = findViewById(R.id.custom_background_image_view);



        TabsPagerAdapter tabsAdapter = new TabsPagerAdapter(this);
        viewPager.setAdapter(tabsAdapter);



        new TabLayoutMediator(tabLayout, viewPager, (tab, position) -> {
            switch (position) {
                case 0:
                    tab.setText("Canciones");
                    break;
                case 1:
                    tab.setText("Carpetas");
                    break;
                case 2:
                    tab.setText("Playlists");
                    break;
                case 3:
                    tab.setText("YouTube");
                    break;
            }
        }).attach();

        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnSort = findViewById(R.id.btnSort);
        songProgressBar = findViewById(R.id.songProgressBar);
        songInfoLayout = findViewById(R.id.songInfoLayout);
        currentSongAlbumArt = findViewById(R.id.currentSongAlbumArt);
        currentSongTitle = findViewById(R.id.currentSongTitle);
        searchLayout = findViewById(R.id.searchLayout);
        searchEditText = findViewById(R.id.searchEditText);
        btnClearSearch = findViewById(R.id.btnClearSearch);
        btnCloseSearch = findViewById(R.id.btnCloseSearch);


        loadPlaylists();
        setupPlayerControls();
        setupSearchControls();
        checkStoragePermission();
    }

//metodos
    private void setupSearchControls() {

        searchEditText.addTextChangedListener(new TextWatcher() {
            @Override
            public void beforeTextChanged(CharSequence s, int start, int count, int after) {

            }

            @Override
            public void onTextChanged(CharSequence s, int start, int before, int count) {

                filterSongs(s.toString());

                btnClearSearch.setVisibility(s.length() > 0 ? View.VISIBLE : View.GONE);
            }

            @Override
            public void afterTextChanged(Editable s) {

            }
        });


        btnClearSearch.setOnClickListener(v -> searchEditText.setText(""));


        btnCloseSearch.setOnClickListener(v -> closeSearch());
    }
    private void setupPlayerControls() {
        btnPlay.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    pauseSong();
                } else {
                    resumeSong();
                }
            } else if (!currentDisplayList.isEmpty()) {
                playSong(0, false);
            }
        });
        btnNext.setOnClickListener(v -> playNextSong());
        btnPrevious.setOnClickListener(v -> playPreviousSong());
        btnShuffle.setOnClickListener(v -> toggleShuffle());
        btnSort.setOnClickListener(v -> showSortOptionsPopup());

        songProgressBar.setOnSeekBarChangeListener(new SeekBar.OnSeekBarChangeListener() {
            @Override
            public void onProgressChanged(SeekBar seekBar, int progress, boolean fromUser) {
                if (mediaPlayer != null && fromUser) {
                    mediaPlayer.seekTo(progress);
                }
            }
            @Override
            public void onStartTrackingTouch(SeekBar seekBar) {
                handler.removeCallbacks(updateTimeTask);
            }
            @Override
            public void onStopTrackingTouch(SeekBar seekBar) {
                handler.postDelayed(updateTimeTask, 100);
            }
        });
        updateShuffleButtonIcon();
    }

    private void checkStoragePermission() {
        String permission = Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU ?
                Manifest.permission.READ_MEDIA_AUDIO : Manifest.permission.READ_EXTERNAL_STORAGE;
        if (ContextCompat.checkSelfPermission(this, permission) != PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this, new String[]{permission}, PERMISSION_REQUEST_READ_STORAGE);
        } else {
            loadSongs();
        }
    }






    private void filterSongs(String query) {
        currentDisplayList.clear();
        if (query.isEmpty()) {

            currentDisplayList.addAll(allSongsList);
        } else {

            String lowerCaseQuery = query.toLowerCase();
            for (Song song : allSongsList) {
                if (song.getTitle().toLowerCase().contains(lowerCaseQuery) ||
                        (song.getArtist() != null && song.getArtist().toLowerCase().contains(lowerCaseQuery)) ||
                        (song.getAlbum() != null && song.getAlbum().toLowerCase().contains(lowerCaseQuery))) {
                    currentDisplayList.add(song);
                }
            }
        }
        sortSongList();
        notifySongsFragmentAdapterChanged();
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        super.onRequestPermissionsResult(requestCode, permissions, grantResults);
        if (requestCode == PERMISSION_REQUEST_READ_STORAGE) {
            if (grantResults.length > 0 && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                loadSongs();
            } else {
                Toast.makeText(this, "Permiso denegado.", Toast.LENGTH_LONG).show();
            }
        }
    }


    private void loadSongs() {
        new Thread(() -> {
            allSongsList.clear(); // Limpia la lista de canciones existente
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {
                    MediaStore.Audio.Media._ID, // ¡IMPORTANTE: Añade esta línea!
                    MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.DATE_MODIFIED
            };
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
            try (Cursor cursor = getContentResolver().query(uri, projection, selection, null, null)) {
                if (cursor != null) {
                    // También necesitas obtener el índice de la nueva columna _ID
                    int idColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media._ID); // ¡IMPORTANTE: Añade esta línea!
                    int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                    int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                    int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                    int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                    int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                    int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                    int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED);
                    int dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED);

                    while (cursor.moveToNext()) {
                        long id = cursor.getLong(idColumn); // ¡IMPORTANTE: Obtén el ID!
                        allSongsList.add(new Song(
                                id, // Pasa el ID al constructor de Song
                                cursor.getString(titleColumn), cursor.getString(artistColumn),
                                cursor.getString(pathColumn), cursor.getLong(durationColumn),
                                cursor.getLong(albumIdColumn), cursor.getString(albumColumn),
                                cursor.getLong(dateAddedColumn), cursor.getLong(dateModifiedColumn)
                        ));
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
            runOnUiThread(() -> {
                showAllSongs();
                loadAllFolders();
            });
        }).start();
    }

    @Override
    public void onRefreshList() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            btnPlay.setImageResource(R.drawable.ic_play);
            songInfoLayout.setVisibility(View.GONE);
            songProgressBar.setProgress(0);
        }
        loadSongs();
    }

    @Override
    public void onSongSelected(int position) {
        playSong(position, false);
    }

//Playlist



    public ArrayList<Playlist> getAllPlaylists() {
        return allPlaylists;
    }


    public void savePlaylists() {
        // Implementa el guardado de playlists (ver método más abajo)
        savePlaylistsToPreferences();
    }


    public void addPlaylist(Playlist playlist) {
        allPlaylists.add(playlist);
        savePlaylistsToPreferences(); // Guardar después de añadir
        PlaylistsFragment playlistsFragment = getPlaylistsFragment();
        if (playlistsFragment != null) {
            playlistsFragment.notifyAdapterChange();
        }
    }


    public void playPlaylist(Playlist playlist) {
        // Lógica para reproducir una playlist
        if (playlist.getSongIds().isEmpty()) {
            Toast.makeText(this, "La playlist está vacía.", Toast.LENGTH_SHORT).show();
            return;
        }

        // Crear una currentDisplayList temporal con las canciones de la playlist
        currentDisplayList.clear();
        for (long songId : playlist.getSongIds()) {
            Song song = getSongById(songId); // Necesitarás un método para obtener Song por ID
            if (song != null) {
                currentDisplayList.add(song);
            }
        }

        if (!currentDisplayList.isEmpty()) {
            // Ir a la pestaña de Canciones y empezar a reproducir la primera
            viewPager.setCurrentItem(0, true);
            toolbar.setTitle("Playlist: " + playlist.getName()); // Cambiar título de la toolbar
            notifySongsFragmentAdapterChanged(); // Asegurar que el fragmento de canciones se actualice
            playSong(0, false); // Reproducir la primera canción de la playlist
        } else {
            Toast.makeText(this, "No se encontraron canciones válidas en esta playlist.", Toast.LENGTH_SHORT).show();
        }
    }

    // Metodos para SongsFragment.OnSongInteractionListener




    @Override
    public void showAddToPlaylistDialog(List<Long> songIdsToAdd) {
        // Implementa el diálogo para añadir a playlist (ver método showAddToPlaylistDialog más abajo)
        showAddToPlaylistDialogInternal(songIdsToAdd);
    }

    @Override
    public void showSongDetails(Song song) {
        // Ya tienes una implementación de esto en onOptionsButtonClick en la versión antigua.
        // Ahora lo centralizamos aquí.
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detalles de la Canción");
        String details = "Título: " + song.getTitle() + "\n" +
                "Artista: " + song.getArtist() + "\n" +
                "Álbum: " + song.getAlbum() + "\n" +
                "Duración: " + formatDuration(song.getDuration()); // Necesitas un método formatDuration
        builder.setMessage(details);
        builder.setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    // Metodo para SongsFragment.OnSongSelectionListener
    @Override
    public void onSelectionModeChanged(boolean inSelectionMode) {
        if (inSelectionMode) {
            // Mostrar una barra de acciones de contexto (CAB) o botones flotantes
            // Por ejemplo, podrías cambiar la visibilidad de un FAB para "Añadir a Playlist"
            Toast.makeText(this, "Modo de selección múltiple activado", Toast.LENGTH_SHORT).show();
            // Puedes mostrar un botón de "Añadir a Playlist" aquí
            showSelectionModeActionBar(true);
        } else {
            Toast.makeText(this, "Modo de selección múltiple desactivado", Toast.LENGTH_SHORT).show();
            // Ocultar la barra de acciones de contexto o limpiar el estado
            showSelectionModeActionBar(false);
        }
    }


// Dentro de MainActivity.java

    // Helper para obtener una canción por su ID (necesario para construir playlists)
    private Song getSongById(long songId) {
        // Podrías optimizar esto si allSongsList estuviera en un HashMap<Long, Song>
        for (Song song : allSongsList) {
            if (song.getId() == songId) {
                return song;
            }
        }
        return null;
    }

    // Helper para formatear la duración (si no lo tienes)
    private String formatDuration(long millis) {
        long minutes = (millis / 1000) / 60;
        long seconds = (millis / 1000) % 60;
        return String.format("%02d:%02d", minutes, seconds);
    }

    // Nuevo método para mostrar el diálogo de añadir a playlist
    private void showAddToPlaylistDialogInternal(List<Long> songIdsToAdd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        View dialogView = getLayoutInflater().inflate(R.layout.dialog_add_to_playlist, null);
        builder.setView(dialogView);

        Button btnCreateNew = dialogView.findViewById(R.id.btnCreateNewPlaylist);
        ListView existingPlaylistsListView = dialogView.findViewById(R.id.existingPlaylistsListView);

        // Adaptador para la lista de playlists existentes dentro del diálogo
        PlaylistAdapter dialogPlaylistAdapter = new PlaylistAdapter(this, allPlaylists);
        existingPlaylistsListView.setAdapter(dialogPlaylistAdapter);

        AlertDialog dialog = builder.create();

        btnCreateNew.setOnClickListener(v -> {
            dialog.dismiss();
            showCreateNewPlaylistDialogForSongs(songIdsToAdd);
        });

        existingPlaylistsListView.setOnItemClickListener((parent, view, position, id) -> {
            Playlist selectedPlaylist = allPlaylists.get(position);
            selectedPlaylist.addSongIds(songIdsToAdd);
            savePlaylistsToPreferences();
            Toast.makeText(this, songIdsToAdd.size() + " canciones añadidas a '" + selectedPlaylist.getName() + "'.", Toast.LENGTH_SHORT).show();
            dialog.dismiss();
            // Notificar al fragmento de playlists si está visible
            PlaylistsFragment playlistsFragment = getPlaylistsFragment();
            if (playlistsFragment != null) {
                playlistsFragment.notifyAdapterChange();
            }
            // Salir del modo de selección si estamos en él (después de añadir)
            SongsFragment songsFragment = getSongsFragment();
            if (songsFragment != null && songsFragment.adapter.isInSelectionMode()) {
                songsFragment.exitSelectionMode();
            }
        });

        dialog.show();
    }

    private void showCreateNewPlaylistDialogForSongs(List<Long> songIdsToAdd) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Crear Nueva Playlist");

        final EditText input = new EditText(this);
        input.setHint("Nombre de la playlist");
        builder.setView(input);

        builder.setPositiveButton("Crear", (dialog, which) -> {
            String playlistName = input.getText().toString().trim();
            if (playlistName.isEmpty()) {
                Toast.makeText(this, "El nombre de la playlist no puede estar vacío", Toast.LENGTH_SHORT).show();
                return;
            }

            // Verificar si ya existe una playlist con ese nombre
            for (Playlist p : allPlaylists) {
                if (p.getName().equalsIgnoreCase(playlistName)) {
                    Toast.makeText(this, "Ya existe una playlist con ese nombre.", Toast.LENGTH_SHORT).show();
                    return;
                }
            }

            Playlist newPlaylist = new Playlist(playlistName);
            newPlaylist.addSongIds(songIdsToAdd); // Añade las canciones seleccionadas
            allPlaylists.add(newPlaylist);
            savePlaylistsToPreferences();
            Toast.makeText(this, playlistName + " creada y " + songIdsToAdd.size() + " canciones añadidas.", Toast.LENGTH_SHORT).show();

            // Notificar al fragmento de playlists si está visible
            PlaylistsFragment playlistsFragment = getPlaylistsFragment();
            if (playlistsFragment != null) {
                playlistsFragment.notifyAdapterChange();
            }
            // Salir del modo de selección si estamos en él (después de añadir)
            SongsFragment songsFragment = getSongsFragment();
            if (songsFragment != null && songsFragment.adapter.isInSelectionMode()) {
                songsFragment.exitSelectionMode();
            }
        });
        builder.setNegativeButton("Cancelar", (dialog, which) -> {
            dialog.cancel();
            // Opcional: Si el usuario cancela la creación, salir del modo de selección
            SongsFragment songsFragment = getSongsFragment();
            if (songsFragment != null && songsFragment.adapter.isInSelectionMode()) {
                songsFragment.exitSelectionMode();
            }
        });
        builder.show();
    }

    // Métodos para guardar y cargar playlists (usando SharedPreferences)
    private void savePlaylistsToPreferences() {
        SharedPreferences sharedPrefs = getSharedPreferences(PLAYLISTS_PREFS, Context.MODE_PRIVATE);
        SharedPreferences.Editor editor = sharedPrefs.edit();

        Gson gson = new Gson(); // Necesitarás la librería Gson
        String json = gson.toJson(allPlaylists);
        editor.putString("all_playlists_json", json);
        editor.apply();
        // Toast.makeText(this, "Playlists guardadas.", Toast.LENGTH_SHORT).show(); // Para depuración
    }

    private void loadPlaylists() {
        SharedPreferences sharedPrefs = getSharedPreferences(PLAYLISTS_PREFS, Context.MODE_PRIVATE);
        Gson gson = new Gson();
        String json = sharedPrefs.getString("all_playlists_json", null);

        if (json != null) {
            // Necesitamos un Type para que Gson sepa cómo deserializar ArrayList<Playlist>
            java.lang.reflect.Type type = new com.google.gson.reflect.TypeToken<ArrayList<Playlist>>() {}.getType();
            ArrayList<Playlist> loadedPlaylists = gson.fromJson(json, type);
            if (loadedPlaylists != null) {
                allPlaylists.clear();
                allPlaylists.addAll(loadedPlaylists);
            }
        }
        // Toast.makeText(this, "Playlists cargadas.", Toast.LENGTH_SHORT).show(); // Para depuración
    }

    // Nuevo getter para PlaylistsFragment
    private PlaylistsFragment getPlaylistsFragment() {
        // Asumiendo que PlaylistsFragment es el tercer tab (índice 2)
        return (PlaylistsFragment) getSupportFragmentManager().findFragmentByTag("f" + viewPager.getAdapter().getItemId(2));
    }

    // **¡NUEVO!** Método para manejar la barra de acciones contextual de selección múltiple
// Puedes personalizar esto para usar un RelativeLayout, un AppBar, etc.
// Esto es un ejemplo básico.
    private void showSelectionModeActionBar(boolean show) {
        if (show) {
            // Aquí podrías inflar una vista de barra de acción personalizada
            // o mostrar un FAB con la opción "Añadir a Playlist"
            // Por simplicidad, por ahora haremos que el FAB de añadir playlist aparezca.
            // Si tienes un FAB específico para esta acción en tu layout principal:
            // btnAddToPlaylistSelection.setVisibility(View.VISIBLE);

            // Temporalmente, puedes cambiar el título de la toolbar o mostrar un mensaje
            toolbar.setTitle("Seleccionadas: " + getSongsFragment().getSelectedSongIds().size());

            // Y un botón para "Añadir a Playlist" si hay canciones seleccionadas
            if (getSongsFragment() != null && !getSongsFragment().getSelectedSongIds().isEmpty()) {
                // Podrías usar btnSort como un botón temporal de "Añadir a Playlist"
                btnSort.setImageResource(R.drawable.ic_add); // Asume que tienes este icono
                btnSort.setVisibility(View.VISIBLE);
                btnSort.setOnClickListener(v -> {
                    List<Long> selectedIds = getSongsFragment().getSelectedSongIds();
                    if (!selectedIds.isEmpty()) {
                        showAddToPlaylistDialog(selectedIds);
                    } else {
                        Toast.makeText(this, "No hay canciones seleccionadas.", Toast.LENGTH_SHORT).show();
                    }
                });
            } else {
                btnSort.setVisibility(View.GONE); // Oculta si no hay seleccionadas
            }

            // También puedes agregar un botón de "Cancelar" en la Toolbar
            // Esto es más avanzado y podría requerir Toolbar.setNavigationIcon()
            // Por ahora, el onBackPressed() puede servir para salir del modo.

        } else {
            // Ocultar elementos de la barra de acciones de contexto
            toolbar.setTitle("Reproductor"); // Vuelve al título normal
            btnSort.setImageResource(R.drawable.ic_sort); // Vuelve al icono de ordenar
            btnSort.setOnClickListener(v -> showSortOptionsPopup()); // Vuelve al listener original
            // btnAddToPlaylistSelection.setVisibility(View.GONE);
            // Asegurarse de que el fragmento de canciones salga del modo de selección
            SongsFragment songsFragment = getSongsFragment();
            if (songsFragment != null) {
                songsFragment.exitSelectionMode();
            }
        }
    }

    // Modificar onBackPressed para salir del modo de selección
    @Override
    public void onBackPressed() {
        SongsFragment songsFragment = getSongsFragment();
        if (songsFragment != null && songsFragment.isInSelectionMode()) {
            songsFragment.exitSelectionMode(); // Salir del modo de selección
            return; // Consumir el evento back
        }
        // ... (resto de tu lógica onBackPressed existente) ...
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (viewPager.getCurrentItem() == 3) { // YouTubeFragment
            YouTubeFragment fragment = (YouTubeFragment) getSupportFragmentManager().findFragmentByTag("f3");
            if (fragment != null && fragment.canGoBack()) {
                fragment.canGoBack(); // <--- ESTA LÍNEA CAUSA EL ERROR
            } else {
                super.onBackPressed();
            }
        } else if (viewPager.getCurrentItem() == 0 && !toolbar.getTitle().toString().equals("Reproductor")) {
            // Si estás en la pestaña de canciones y el título no es "Reproductor" (ej. estás viendo una carpeta/playlist)
            showAllSongs(); // Vuelve a mostrar todas las canciones
        } else {
            super.onBackPressed();
        }
    }











    //Carpetas


    public ArrayList<Folder> getDisplayableFoldersList() {
        return allFoldersList;
    }

    private void loadAllFolders() {
        new Thread(() -> {
            // Usar un HashMap para evitar duplicados y contar canciones por carpeta
            HashMap<String, Folder> folderMap = new HashMap<>();

            // Asegúrate de que allSongsList ya esté cargada antes de llamar a loadAllFolders()
            // (esto se gestiona si loadAllFolders() se llama después de loadSongs())
            for (Song song : allSongsList) {
                if (song.getPath() != null) {
                    File songFile = new File(song.getPath());
                    String parentPath = songFile.getParent(); // Ruta completa de la carpeta
                    String parentName = null;
                    if (songFile.getParentFile() != null) {
                        parentName = songFile.getParentFile().getName(); // Solo el nombre de la carpeta
                    }


                    if (parentPath != null) {
                        if (folderMap.containsKey(parentPath)) {
                            // Si la carpeta ya existe, incrementa el contador de canciones
                            folderMap.get(parentPath).setSongCount(folderMap.get(parentPath).getSongCount() + 1);
                        } else {
                            // Si es una carpeta nueva, crea un objeto Folder
                            // Manejar el caso de parentName nulo (ej. si la canción está en la raíz)
                            if (parentName == null || parentName.isEmpty()) {
                                parentName = "Almacenamiento Interno"; // Nombre por defecto para la raíz
                            }
                            Folder folder = new Folder(parentName, parentPath, 1);
                            folderMap.put(parentPath, folder);
                        }
                    }
                }
            }

            // Convertir el HashMap de carpetas únicas a un ArrayList
            final ArrayList<Folder> sortedFolders = new ArrayList<>(folderMap.values());

            // Ordenar las carpetas alfabéticamente por nombre
            Collections.sort(sortedFolders, (f1, f2) -> f1.getName().compareToIgnoreCase(f2.getName()));

            // Actualizar la lista en el hilo principal y notificar al FoldersFragment
            runOnUiThread(() -> {
                allFoldersList.clear(); // Limpia la lista de carpetas de MainActivity
                allFoldersList.addAll(sortedFolders); // Añade las carpetas recién escaneadas y ordenadas

                FoldersFragment foldersFragment = getFoldersFragment();
                if (foldersFragment != null && foldersFragment.isAdded()) {
                    // Notifica al adaptador del FoldersFragment para que se actualice
                    foldersFragment.notifyAdapterChange();
                }
                // Opcional: Toast para indicar que las carpetas han cargado (si lo deseas)
                // Toast.makeText(MainActivity.this, "Carpetas cargadas.", Toast.LENGTH_SHORT).show();
            });
        }).start();
    }


    private FoldersFragment getFoldersFragment() {
        // Asumiendo que FoldersFragment es el segundo tab (índice 1)
        return (FoldersFragment) getSupportFragmentManager().findFragmentByTag("f" + viewPager.getAdapter().getItemId(1));
        // O si tu TabsPagerAdapter siempre asigna tags como "f0", "f1", etc. y es el tab 1:
        // return (FoldersFragment) getSupportFragmentManager().findFragmentByTag("f1");
    }
    @Override
    public void onFolderSelected(String folderPath) {
        Toast.makeText(this, "Cargando canciones de la carpeta...", Toast.LENGTH_SHORT).show();

        String selectedFolderPath = folderPath;

        new Thread(() -> {
            ArrayList<Song> filteredSongs = new ArrayList<>();
            for (Song song : allSongsList) {
                if (song.getPath() != null && song.getPath().startsWith(selectedFolderPath)) {
                    filteredSongs.add(song);
                }
            }

            Collections.sort(filteredSongs, (s1, s2) -> {
                switch (currentSortOrder) {
                    case SORT_ORDER_NAME_DESC:
                        return s2.getTitle().compareToIgnoreCase(s1.getTitle());
                    case SORT_ORDER_ARTIST:
                        String artist1 = s1.getArtist() != null ? s1.getArtist() : "";
                        String artist2 = s2.getArtist() != null ? s2.getArtist() : "";
                        return artist1.compareToIgnoreCase(artist2);
                    case SORT_ORDER_DURATION_ASC:
                        return Long.compare(s1.getDuration(), s2.getDuration());
                    case SORT_ORDER_DURATION_DESC:
                        return Long.compare(s2.getDuration(), s1.getDuration());
                    case SORT_ORDER_DATE_ADDED_ASC:
                        return Long.compare(s1.getDateAdded(), s2.getDateAdded());
                    case SORT_ORDER_DATE_ADDED_DESC:
                        return Long.compare(s2.getDateAdded(), s1.getDateAdded());
                    case SORT_ORDER_DATE_MODIFIED_ASC:
                        return Long.compare(s1.getDateModified(), s2.getDateModified());
                    case SORT_ORDER_DATE_MODIFIED_DESC:
                        return Long.compare(s2.getDateModified(), s1.getDateModified());
                    case SORT_ORDER_NAME_ASC:
                    default:
                        return s1.getTitle().compareToIgnoreCase(s2.getTitle());
                }
            });

            runOnUiThread(() -> {
                currentDisplayList.clear();
                currentDisplayList.addAll(filteredSongs);

                viewPager.setCurrentItem(0, true);
                toolbar.setTitle(new File(selectedFolderPath).getName());

                SongsFragment songsFragment = getSongsFragment();
                if (songsFragment != null && songsFragment.isAdded()) {
                    songsFragment.notifyAdapterChange();

                    // **¡NUEVO CAMBIO AQUÍ!**
                    // Invalida la vista del ListView para forzar un redibujado inmediato
                    songsFragment.getSongListView().invalidateViews();

                    // Postear el Toast después de un pequeño retraso para asegurar el redibujado visual
                    songsFragment.getSongListView().postDelayed(() -> {
                        Toast.makeText(this, "Canciones cargadas.", Toast.LENGTH_SHORT).show();
                    }, 150); // Aumenté ligeramente el retraso para más seguridad, puedes ajustar.

                } else {
                    Toast.makeText(this, "Canciones cargadas.", Toast.LENGTH_SHORT).show();
                }
            });
        }).start();
    }








    private void playSong(int position, boolean fromShuffled) {
        ArrayList<Song> activeList = isShuffleOn ? shuffledSongList : currentDisplayList;
        if (activeList.isEmpty()) return;



        currentSongIndex = position;
        if (currentSongIndex < 0 || currentSongIndex >= activeList.size()) {
            currentSongIndex = 0;
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
        }

        Song songToPlay = activeList.get(currentSongIndex);
        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(songToPlay.getPath());
            mediaPlayer.prepare();
            mediaPlayer.start();
            btnPlay.setImageResource(R.drawable.ic_pause);
            updateCurrentSongInfo(songToPlay);
            songProgressBar.setMax(mediaPlayer.getDuration());
            updateProgressBar();
            mediaPlayer.setOnCompletionListener(mp -> playNextSong());
        } catch (IOException e) {
            e.printStackTrace();
            Toast.makeText(this, "Error al reproducir", Toast.LENGTH_SHORT).show();
        }
    }

    private void pauseSong() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.pause();
            btnPlay.setImageResource(R.drawable.ic_play);
            handler.removeCallbacks(updateTimeTask);
        }
    }

    private void resumeSong() {
        if (mediaPlayer != null && !mediaPlayer.isPlaying()) {
            mediaPlayer.start();
            btnPlay.setImageResource(R.drawable.ic_pause);
            updateProgressBar();
        }
    }

    private void playNextSong() {
        ArrayList<Song> activeSongList = isShuffleOn ? shuffledSongList : currentDisplayList;
        if (activeSongList.isEmpty()) { return; }
        currentSongIndex++;
        if (currentSongIndex >= activeSongList.size()) {
            currentSongIndex = 0;
        }
        playSong(currentSongIndex, false);
    }

    private void playPreviousSong() {
        ArrayList<Song> activeSongList = isShuffleOn ? shuffledSongList : currentDisplayList;
        if (activeSongList.isEmpty()) { return; }
        currentSongIndex--;
        if (currentSongIndex < 0) {
            currentSongIndex = activeSongList.size() - 1;
        }
        playSong(currentSongIndex, false);
    }

    private void toggleShuffle() {
        isShuffleOn = !isShuffleOn;
        if (isShuffleOn) {
            createShuffledList();


        }
        updateShuffleButtonIcon();

    }



//Configuraciones

    private void applyAppTheme() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        boolean isDarkModeEnabled = sharedPrefs.getBoolean("pref_dark_mode", false);

        if (isDarkModeEnabled) {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_YES);
        } else {
            AppCompatDelegate.setDefaultNightMode(AppCompatDelegate.MODE_NIGHT_NO);
        }
    }

    private void loadCustomBackground() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(this);
        String backgroundUrl = sharedPrefs.getString("pref_background_url", null);

        if (backgroundUrl != null && !backgroundUrl.isEmpty()) {
            Picasso.get()
                    .load(backgroundUrl)

                    .placeholder(android.R.color.transparent)
                    .error(android.R.color.transparent)
                    .into(customBackgroundImageView);
        } else {

            customBackgroundImageView.setImageDrawable(null);
        }
    }





    private void createShuffledList() {
        shuffledSongList.clear();
        shuffledSongList.addAll(currentDisplayList);
        Collections.shuffle(shuffledSongList);
    }

    public ArrayList<Song> getMasterSongList() {
        return allSongsList;
    }

    public ArrayList<Song> getDisplayableSongList() {
        return currentDisplayList;
    }

    private void showAllSongs() {
        currentDisplayList.clear();
        currentDisplayList.addAll(allSongsList);
        sortSongList();
        toolbar.setTitle("Reproductor");

        SongsFragment songsFragment = getSongsFragment();
        if (songsFragment != null) {
            songsFragment.setRefreshing(false);
        }
        notifySongsFragmentAdapterChanged();
    }

    private void sortSongList() {
        Collections.sort(currentDisplayList, (s1, s2) -> {
            switch (currentSortOrder) {
                case SORT_ORDER_NAME_DESC:
                    return s2.getTitle().compareToIgnoreCase(s1.getTitle());
                case SORT_ORDER_ARTIST:
                    String artist1 = s1.getArtist() != null ? s1.getArtist() : "";
                    String artist2 = s2.getArtist() != null ? s2.getArtist() : "";
                    return artist1.compareToIgnoreCase(artist2);
                case SORT_ORDER_DURATION_ASC:
                    return Long.compare(s1.getDuration(), s2.getDuration());
                case SORT_ORDER_DURATION_DESC:
                    return Long.compare(s2.getDuration(), s1.getDuration());
                case SORT_ORDER_DATE_ADDED_ASC:
                    return Long.compare(s1.getDateAdded(), s2.getDateAdded());
                case SORT_ORDER_DATE_ADDED_DESC:
                    return Long.compare(s2.getDateAdded(), s1.getDateAdded());
                case SORT_ORDER_DATE_MODIFIED_ASC:
                    return Long.compare(s1.getDateModified(), s2.getDateModified());
                case SORT_ORDER_DATE_MODIFIED_DESC:
                    return Long.compare(s2.getDateModified(), s1.getDateModified());
                case SORT_ORDER_NAME_ASC:
                default:
                    return s1.getTitle().compareToIgnoreCase(s2.getTitle());
            }
        });
        notifySongsFragmentAdapterChanged();
        if (isShuffleOn) {
            createShuffledList();
        }
    }

    private SongsFragment getSongsFragment() {

        return (SongsFragment) getSupportFragmentManager().findFragmentByTag("f0");
    }

    private void notifySongsFragmentAdapterChanged() {
        SongsFragment songsFragment = getSongsFragment();
        if (songsFragment != null && songsFragment.isAdded()) {
            songsFragment.notifyAdapterChange();
        }
    }





    @Override
    protected void onDestroy() {
        super.onDestroy();
        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }
        handler.removeCallbacks(updateTimeTask);
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_search) {
            openSearch();
            Toast.makeText(this, "Abrir búsqueda", Toast.LENGTH_SHORT).show();
        } else if (id == R.id.nav_settings) {
            Intent intent = new Intent(this, SettingsActivity.class);
            startActivity(intent);
        }
        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }






    private void openSearch() {
        searchLayout.setVisibility(View.VISIBLE);
        searchEditText.setText("");
        searchEditText.requestFocus();

        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.showSoftInput(searchEditText, InputMethodManager.SHOW_IMPLICIT);


        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewPager.getLayoutParams();
        params.addRule(RelativeLayout.BELOW, R.id.searchLayout);
        viewPager.setLayoutParams(params);


        tabLayout.setVisibility(View.GONE);
        btnSort.setVisibility(View.GONE);
        toolbar.setTitle("Búsqueda");
    }


    private void closeSearch() {
        searchLayout.setVisibility(View.GONE);
        searchEditText.setText("");
        filterSongs("");


        InputMethodManager imm = (InputMethodManager) getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);


        RelativeLayout.LayoutParams params = (RelativeLayout.LayoutParams) viewPager.getLayoutParams();
        params.removeRule(RelativeLayout.BELOW);
        params.addRule(RelativeLayout.BELOW, R.id.appBarLayout);
        viewPager.setLayoutParams(params);

        tabLayout.setVisibility(View.VISIBLE);
        btnSort.setVisibility(View.VISIBLE);
        toolbar.setTitle("Reproductor");
    }



    @Override
    public void onOptionsButtonClick(Song song) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detalles de la Canción");
        String details = "Título: " + song.getTitle() + "\n" +
                "Artista: " + song.getArtist();
        builder.setMessage(details);
        builder.setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private void showSortOptionsPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.dialog_sort_options, null);
        final PopupWindow popupWindow = new PopupWindow(popupView, LinearLayout.LayoutParams.WRAP_CONTENT, LinearLayout.LayoutParams.WRAP_CONTENT, true);
        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, android.R.color.transparent));
        popupWindow.setOutsideTouchable(true);

        RadioGroup sortRadioGroup = popupView.findViewById(R.id.sortRadioGroup);
        if (SORT_ORDER_NAME_ASC.equals(currentSortOrder)) {
            ((RadioButton) popupView.findViewById(R.id.radioSortByNameAsc)).setChecked(true);
        } else if (SORT_ORDER_NAME_DESC.equals(currentSortOrder)) {
            ((RadioButton) popupView.findViewById(R.id.radioSortByNameDesc)).setChecked(true);
        } else if (SORT_ORDER_ARTIST.equals(currentSortOrder)) {
            ((RadioButton) popupView.findViewById(R.id.radioSortByArtist)).setChecked(true);
        } else if (SORT_ORDER_DURATION_ASC.equals(currentSortOrder)) {
            ((RadioButton) popupView.findViewById(R.id.radioSortByDurationAsc)).setChecked(true);
        } else if (SORT_ORDER_DURATION_DESC.equals(currentSortOrder)) {
            ((RadioButton) popupView.findViewById(R.id.radioSortByDurationDesc)).setChecked(true);
        } else if (SORT_ORDER_DATE_ADDED_ASC.equals(currentSortOrder)) {
            ((RadioButton) popupView.findViewById(R.id.radioSortByDateAddedAsc)).setChecked(true);
        } else if (SORT_ORDER_DATE_ADDED_DESC.equals(currentSortOrder)) {
            ((RadioButton) popupView.findViewById(R.id.radioSortByDateAddedDesc)).setChecked(true);
        } else if (SORT_ORDER_DATE_MODIFIED_ASC.equals(currentSortOrder)) {
            ((RadioButton) popupView.findViewById(R.id.radioSortByDateModifiedAsc)).setChecked(true);
        } else if (SORT_ORDER_DATE_MODIFIED_DESC.equals(currentSortOrder)) {
            ((RadioButton) popupView.findViewById(R.id.radioSortByDateModifiedDesc)).setChecked(true);
        }


        sortRadioGroup.setOnCheckedChangeListener((group, checkedId) -> {
            String newSortOrder = currentSortOrder;

            if (checkedId == R.id.radioSortByNameAsc) {
                newSortOrder = SORT_ORDER_NAME_ASC;
            } else if (checkedId == R.id.radioSortByNameDesc) {
                newSortOrder = SORT_ORDER_NAME_DESC;
            } else if (checkedId == R.id.radioSortByArtist) {
                newSortOrder = SORT_ORDER_ARTIST;
            } else if (checkedId == R.id.radioSortByDurationAsc) {
                newSortOrder = SORT_ORDER_DURATION_ASC;
            } else if (checkedId == R.id.radioSortByDurationDesc) {
                newSortOrder = SORT_ORDER_DURATION_DESC;
            } else if (checkedId == R.id.radioSortByDateAddedAsc) {
                newSortOrder = SORT_ORDER_DATE_ADDED_ASC;
            } else if (checkedId == R.id.radioSortByDateAddedDesc) {
                newSortOrder = SORT_ORDER_DATE_ADDED_DESC;
            } else if (checkedId == R.id.radioSortByDateModifiedAsc) {
                newSortOrder = SORT_ORDER_DATE_MODIFIED_ASC;
            } else if (checkedId == R.id.radioSortByDateModifiedDesc) {
                newSortOrder = SORT_ORDER_DATE_MODIFIED_DESC;
            }

            if (!newSortOrder.equals(currentSortOrder)) {
                currentSortOrder = newSortOrder;
                sortSongList();
            }
            popupWindow.dismiss();
        });
        popupWindow.showAsDropDown(btnSort, 0, 0, Gravity.END);
    }

    private void updateCurrentSongInfo(Song song) {
        songInfoLayout.setVisibility(View.VISIBLE);
        currentSongTitle.setText(song.getTitle());
        currentSongTitle.setSelected(true);
        Bitmap albumArt = getAlbumArt(song.getAlbumId());
        if (albumArt != null) {
            currentSongAlbumArt.setImageBitmap(albumArt);
        } else {
            currentSongAlbumArt.setImageResource(R.drawable.ic_music_placeholder);
        }


    }

    private Bitmap getAlbumArt(long albumId) {
        try {
            Uri sArtworkUri = Uri.parse("content://media/external/audio/albumart");
            Uri albumArtUri = ContentUris.withAppendedId(sArtworkUri, albumId);
            return MediaStore.Images.Media.getBitmap(this.getContentResolver(), albumArtUri);
        } catch (Exception e) {
            return null;
        }
    }

    private void updateProgressBar() {
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            handler.postDelayed(updateTimeTask, 100);
        }
    }

    private Runnable updateTimeTask = new Runnable() {
        public void run() {
            if (mediaPlayer != null) {
                songProgressBar.setProgress(mediaPlayer.getCurrentPosition());
                handler.postDelayed(this, 100);
            }
        }
    };

    private void updateShuffleButtonIcon() {
        if (isShuffleOn) {
            btnShuffle.setImageResource(R.drawable.ic_shuffle_on);
            Toast.makeText(this, "Modo aleatorio activado", Toast.LENGTH_SHORT).show();
        } else {
            btnShuffle.setImageResource(R.drawable.ic_shuffle_off);
            Toast.makeText(this, "Modo aleatorio desactivado", Toast.LENGTH_SHORT).show();
        }
    }

    @Override
    protected void onResume() {
        super.onResume();

        loadCustomBackground();
    }
}