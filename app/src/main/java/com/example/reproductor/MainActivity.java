package com.example.reproductor;

import android.Manifest;
import android.content.ContentUris;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
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
import android.widget.SeekBar;
import android.widget.TextView;
import android.widget.Toast;
import android.os.Handler;
import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;
import androidx.core.view.GravityCompat;
import androidx.drawerlayout.widget.DrawerLayout;
import androidx.swiperefreshlayout.widget.SwipeRefreshLayout;
import com.google.android.material.navigation.NavigationView;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;
import android.app.AlertDialog;
import java.util.concurrent.TimeUnit;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener, SongAdapter.OnOptionsButtonClickListener {

    private static final int PERMISSION_REQUEST_READ_STORAGE = 1;
    private ListView songListView;
    private ArrayList<Song> songList;
    private SongAdapter adapter;
    private SwipeRefreshLayout swipeRefreshLayout;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;

    private MediaPlayer mediaPlayer;
    private int currentSongIndex = -1;
    private ImageButton btnPlay, btnNext, btnPrevious, btnShuffle, btnSort;
    private SeekBar songProgressBar;
    private Handler handler = new Handler();

    private LinearLayout songInfoLayout;
    private ImageView currentSongAlbumArt;
    private TextView currentSongTitle;

    private boolean isShuffleOn = false;
    private ArrayList<Song> shuffledSongList;
    private Random random = new Random();

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

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        toolbar = findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);

        drawerLayout = findViewById(R.id.drawer_layout);
        NavigationView navigationView = findViewById(R.id.nav_view);
        navigationView.setNavigationItemSelectedListener(this);

        androidx.appcompat.app.ActionBarDrawerToggle toggle = new androidx.appcompat.app.ActionBarDrawerToggle(
                this, drawerLayout, toolbar,
                R.string.navigation_drawer_open,
                R.string.navigation_drawer_close);

        drawerLayout.addDrawerListener(toggle);
        toggle.syncState();

        toolbar.bringToFront();

        toolbar.setOnTouchListener((v, event) -> {
            v.performClick();
            return true;
        });

        swipeRefreshLayout = findViewById(R.id.swipeRefreshLayout);
        songListView = findViewById(R.id.songListView);
        songList = new ArrayList<>();
        shuffledSongList = new ArrayList<>();
        adapter = new SongAdapter(this, songList);
        adapter.setOnOptionsButtonClickListener(this);
        songListView.setAdapter(adapter);

        btnPlay = findViewById(R.id.btnPlay);
        btnNext = findViewById(R.id.btnNext);
        btnPrevious = findViewById(R.id.btnPrevious);
        btnShuffle = findViewById(R.id.btnShuffle);
        btnSort = findViewById(R.id.btnSort);
        songProgressBar = findViewById(R.id.songProgressBar);

        songInfoLayout = findViewById(R.id.songInfoLayout);
        currentSongAlbumArt = findViewById(R.id.currentSongAlbumArt);
        currentSongTitle = findViewById(R.id.currentSongTitle);

        swipeRefreshLayout.setOnRefreshListener(this::refreshSongList);
        swipeRefreshLayout.setColorSchemeResources(
                android.R.color.holo_blue_bright,
                android.R.color.holo_green_light,
                android.R.color.holo_orange_light,
                android.R.color.holo_red_light
        );

        songListView.setOnItemClickListener((parent, view, position, id) -> {
            if (isShuffleOn) {
                playSong(position, true);
            } else {
                playSong(position, false);
            }
        });

        btnPlay.setOnClickListener(v -> {
            if (mediaPlayer != null) {
                if (mediaPlayer.isPlaying()) {
                    pauseSong();
                } else {
                    resumeSong();
                }
            } else if (!songList.isEmpty()) {
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
        checkStoragePermission();
    }

    private void playSong(int position, boolean fromOrderedList) {
        ArrayList<Song> activeSongList = isShuffleOn ? shuffledSongList : songList;

        if (activeSongList.isEmpty()) {
            Toast.makeText(this, "No hay canciones para reproducir.", Toast.LENGTH_SHORT).show();
            songInfoLayout.setVisibility(View.GONE);
            return;
        }

        if (fromOrderedList && isShuffleOn) {
            Song selectedSong = songList.get(position);
            int indexInShuffledList = -1;
            for (int i = 0; i < shuffledSongList.size(); i++) {
                if (shuffledSongList.get(i).getPath().equals(selectedSong.getPath())) {
                    indexInShuffledList = i;
                    break;
                }
            }
            if (indexInShuffledList != -1) {
                currentSongIndex = indexInShuffledList;
            } else {
                currentSongIndex = 0;
            }
        } else {
            currentSongIndex = position;
        }

        if (currentSongIndex < 0 || currentSongIndex >= activeSongList.size()) {
            currentSongIndex = 0;
        }

        if (mediaPlayer != null) {
            mediaPlayer.release();
            mediaPlayer = null;
        }

        Song songToPlay = activeSongList.get(currentSongIndex);
        String songPath = songToPlay.getPath();

        mediaPlayer = new MediaPlayer();
        try {
            mediaPlayer.setDataSource(songPath);
            mediaPlayer.prepare();
            mediaPlayer.start();
            btnPlay.setImageResource(R.drawable.ic_pause);

            updateCurrentSongInfo(songToPlay);

            songProgressBar.setMax(mediaPlayer.getDuration());
            updateProgressBar();

            mediaPlayer.setOnCompletionListener(mp -> playNextSong());

        } catch (IOException e) {
            Toast.makeText(this, "Error al reproducir la canción: " + e.getMessage(), Toast.LENGTH_LONG).show();
            e.printStackTrace();
            btnPlay.setImageResource(R.drawable.ic_play);
            songInfoLayout.setVisibility(View.GONE);
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
        ArrayList<Song> activeSongList = isShuffleOn ? shuffledSongList : songList;
        if (activeSongList.isEmpty()) { return; }

        currentSongIndex++;
        if (currentSongIndex >= activeSongList.size()) {
            currentSongIndex = 0;
        }
        playSong(currentSongIndex, false);
    }

    private void playPreviousSong() {
        ArrayList<Song> activeSongList = isShuffleOn ? shuffledSongList : songList;
        if (activeSongList.isEmpty()) { return; }

        currentSongIndex--;
        if (currentSongIndex < 0) {
            currentSongIndex = activeSongList.size() - 1;
        }
        playSong(currentSongIndex, false);
    }

    private void toggleShuffle() {
        isShuffleOn = !isShuffleOn;
        updateShuffleButtonIcon();

        if (isShuffleOn) {
            createShuffledList();
        } else {
            if (mediaPlayer != null && mediaPlayer.isPlaying() && currentSongIndex != -1 && !shuffledSongList.isEmpty()) {
                Song currentPlayingSong = shuffledSongList.get(currentSongIndex);
                currentSongIndex = songList.indexOf(currentPlayingSong);
                if (currentSongIndex == -1) currentSongIndex = 0;
            }
        }
    }

    private void updateShuffleButtonIcon() {
        if (isShuffleOn) {
            btnShuffle.setImageResource(R.drawable.ic_shuffle_on);
        } else {
            btnShuffle.setImageResource(R.drawable.ic_shuffle_off);
        }
    }

    private void createShuffledList() {
        shuffledSongList.clear();
        shuffledSongList.addAll(songList);
        Collections.shuffle(shuffledSongList);
    }

    private void updateProgressBar() {
        handler.postDelayed(updateTimeTask, 100);
    }

    private Runnable updateTimeTask = new Runnable() {
        public void run() {
            if (mediaPlayer != null) {
                int currentPosition = mediaPlayer.getCurrentPosition();
                songProgressBar.setProgress(currentPosition);
                handler.postDelayed(this, 100);
            }
        }
    };

    private void updateCurrentSongInfo(Song song) {
        songInfoLayout.setVisibility(View.VISIBLE);
        currentSongTitle.setText(song.getTitle());

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
            e.printStackTrace();
            return null;
        }
    }

    @Override
    public void onOptionsButtonClick(Song song) {
        showSongDetailsDialog(song);
    }

    private void showSongDetailsDialog(Song song) {
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setTitle("Detalles de la Canción");

        String details = "Título: " + song.getTitle() + "\n" +
                "Artista: " + song.getArtist() + "\n" +
                "Álbum: " + song.getAlbum() + "\n" +
                "Duración: " + formatDuration(song.getDuration()) + "\n" +
                "Ruta: " + song.getPath() + "\n" +
                "ID del Álbum: " + song.getAlbumId() + "\n" +
                "Fecha de Adición: " + convertUnixToDateTime(song.getDateAdded()) + "\n" +
                "Fecha de Modificación: " + convertUnixToDateTime(song.getDateModified());

        builder.setMessage(details);
        builder.setPositiveButton("Cerrar", (dialog, which) -> dialog.dismiss());
        builder.show();
    }

    private String formatDuration(long milliseconds) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(milliseconds);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(milliseconds) -
                TimeUnit.MINUTES.toSeconds(minutes);
        long hours = TimeUnit.MILLISECONDS.toHours(milliseconds);

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }

    private String convertUnixToDateTime(long unixSeconds) {
        java.util.Date date = new java.util.Date(unixSeconds * 1000L);
        java.text.SimpleDateFormat sdf = new java.text.SimpleDateFormat("dd/MM/yyyy HH:mm:ss", java.util.Locale.getDefault());
        return sdf.format(date);
    }

    private void showSortOptionsPopup() {
        LayoutInflater inflater = (LayoutInflater) getSystemService(LAYOUT_INFLATER_SERVICE);
        View popupView = inflater.inflate(R.layout.dialog_sort_options, null);

        final PopupWindow popupWindow = new PopupWindow(
                popupView,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                LinearLayout.LayoutParams.WRAP_CONTENT,
                true
        );

        popupWindow.setBackgroundDrawable(ContextCompat.getDrawable(this, android.R.color.transparent));
        popupWindow.setOutsideTouchable(true);

        RadioGroup sortRadioGroup = popupView.findViewById(R.id.sortRadioGroup);

        switch (currentSortOrder) {
            case SORT_ORDER_NAME_ASC:
                ((RadioButton) popupView.findViewById(R.id.radioSortByNameAsc)).setChecked(true);
                break;
            case SORT_ORDER_NAME_DESC:
                ((RadioButton) popupView.findViewById(R.id.radioSortByNameDesc)).setChecked(true);
                break;
            case SORT_ORDER_ARTIST:
                ((RadioButton) popupView.findViewById(R.id.radioSortByArtist)).setChecked(true);
                break;
            case SORT_ORDER_DURATION_ASC:
                ((RadioButton) popupView.findViewById(R.id.radioSortByDurationAsc)).setChecked(true);
                break;
            case SORT_ORDER_DURATION_DESC:
                ((RadioButton) popupView.findViewById(R.id.radioSortByDurationDesc)).setChecked(true);
                break;
            case SORT_ORDER_DATE_ADDED_ASC:
                ((RadioButton) popupView.findViewById(R.id.radioSortByDateAddedAsc)).setChecked(true);
                break;
            case SORT_ORDER_DATE_ADDED_DESC:
                ((RadioButton) popupView.findViewById(R.id.radioSortByDateAddedDesc)).setChecked(true);
                break;
            case SORT_ORDER_DATE_MODIFIED_ASC:
                ((RadioButton) popupView.findViewById(R.id.radioSortByDateModifiedAsc)).setChecked(true);
                break;
            case SORT_ORDER_DATE_MODIFIED_DESC:
                ((RadioButton) popupView.findViewById(R.id.radioSortByDateModifiedDesc)).setChecked(true);
                break;
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
                Toast.makeText(this, "Lista ordenada por: " + getSortOrderName(currentSortOrder), Toast.LENGTH_SHORT).show();
            }
            popupWindow.dismiss();
        });

        popupWindow.showAsDropDown(btnSort, 0, 0, Gravity.END);
    }

    private String getSortOrderName(String sortOrder) {
        switch (sortOrder) {
            case SORT_ORDER_NAME_ASC:
                return "Título (A-Z)";
            case SORT_ORDER_NAME_DESC:
                return "Título (Z-A)";
            case SORT_ORDER_ARTIST:
                return "Artista";
            case SORT_ORDER_DURATION_ASC:
                return "Duración (Corta a Larga)";
            case SORT_ORDER_DURATION_DESC:
                return "Duración (Larga a Corta)";
            case SORT_ORDER_DATE_ADDED_ASC:
                return "Fecha de Adición (Antiguas a Recientes)";
            case SORT_ORDER_DATE_ADDED_DESC:
                return "Fecha de Adición (Recientes a Antiguas)";
            case SORT_ORDER_DATE_MODIFIED_ASC:
                return "Fecha de Modificación (Antiguas a Recientes)";
            case SORT_ORDER_DATE_MODIFIED_DESC:
                return "Fecha de Modificación (Recientes a Antiguas)";
            default:
                return "Desconocido";
        }
    }

    private void sortSongList() {
        Collections.sort(songList, (s1, s2) -> {
            switch (currentSortOrder) {
                case SORT_ORDER_NAME_ASC:
                    return s1.getTitle().compareToIgnoreCase(s2.getTitle());
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
                default:
                    return 0;
            }
        });
        adapter.notifyDataSetChanged();

        if (isShuffleOn) {
            createShuffledList();
        }
    }

    @Override
    public boolean onNavigationItemSelected(@NonNull MenuItem item) {
        int id = item.getItemId();

        if (id == R.id.nav_settings) {
            Toast.makeText(this, "Abrir configuración", Toast.LENGTH_SHORT).show();
        }

        drawerLayout.closeDrawer(GravityCompat.START);
        return true;
    }

    @Override
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else {
            super.onBackPressed();
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

    private void checkStoragePermission() {
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.TIRAMISU) {
            if (ContextCompat.checkSelfPermission(this, Manifest.permission.READ_MEDIA_AUDIO)
                    != PackageManager.PERMISSION_GRANTED) {
                requestPermission(Manifest.permission.READ_MEDIA_AUDIO);
            } else {
                loadSongs();
            }
        } else {
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
        if (mediaPlayer != null && mediaPlayer.isPlaying()) {
            mediaPlayer.stop();
            mediaPlayer.release();
            mediaPlayer = null;
            btnPlay.setImageResource(R.drawable.ic_play);
            songInfoLayout.setVisibility(View.GONE);
            songProgressBar.setProgress(0);
        }

        songList.clear();
        shuffledSongList.clear();
        adapter.notifyDataSetChanged();
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
                    MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DATE_ADDED,
                    MediaStore.Audio.Media.DATE_MODIFIED
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
                        String album = cursor.getString(5);
                        long dateAdded = cursor.getLong(6);
                        long dateModified = cursor.getLong(7);

                        songList.add(new Song(title, artist, path, duration, albumId, album, dateAdded, dateModified));
                    }
                }
            } catch (Exception e) {
                runOnUiThread(() -> {
                    Toast.makeText(this, "Error al cargar canciones", Toast.LENGTH_SHORT).show();
                    e.printStackTrace();
                });
            }

            runOnUiThread(() -> {
                sortSongList();
                adapter.notifyDataSetChanged();
                if (swipeRefreshLayout.isRefreshing()) {
                    swipeRefreshLayout.setRefreshing(false);
                }
            });
        }).start();
    }
}