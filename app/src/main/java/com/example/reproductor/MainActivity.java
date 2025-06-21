package com.example.reproductor;

import android.Manifest;
import android.app.AlertDialog;
import android.content.ContentUris;
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
import com.squareup.picasso.Picasso;
import com.squareup.picasso.Target;
import com.squareup.picasso.Callback;
import android.util.Log;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Random;

public class MainActivity extends AppCompatActivity
        implements NavigationView.OnNavigationItemSelectedListener,
        SongAdapter.OnOptionsButtonClickListener,
        SongsFragment.OnSongInteractionListener,
        FoldersFragment.OnFolderSelectedListener {

    private static final int PERMISSION_REQUEST_READ_STORAGE = 1;
    private DrawerLayout drawerLayout;
    private Toolbar toolbar;
    private ViewPager2 viewPager;
    private TabLayout tabLayout;

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
            allSongsList.clear();
            Uri uri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;
            String[] projection = {
                    MediaStore.Audio.Media.TITLE, MediaStore.Audio.Media.ARTIST,
                    MediaStore.Audio.Media.DATA, MediaStore.Audio.Media.DURATION,
                    MediaStore.Audio.Media.ALBUM_ID, MediaStore.Audio.Media.ALBUM,
                    MediaStore.Audio.Media.DATE_ADDED, MediaStore.Audio.Media.DATE_MODIFIED
            };
            String selection = MediaStore.Audio.Media.IS_MUSIC + " != 0";
            try (Cursor cursor = getContentResolver().query(uri, projection, selection, null, null)) {
                if (cursor != null) {
                    int titleColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.TITLE);
                    int artistColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ARTIST);
                    int pathColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATA);
                    int durationColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DURATION);
                    int albumIdColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM_ID);
                    int albumColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.ALBUM);
                    int dateAddedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_ADDED);
                    int dateModifiedColumn = cursor.getColumnIndexOrThrow(MediaStore.Audio.Media.DATE_MODIFIED);

                    while (cursor.moveToNext()) {
                        allSongsList.add(new Song(
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
            runOnUiThread(this::showAllSongs);
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

    @Override
    public void onFolderSelected(String folderPath) {
        currentDisplayList.clear();
        for (Song song : allSongsList) {
            if (song.getPath() != null && song.getPath().startsWith(folderPath)) {
                currentDisplayList.add(song);
            }
        }
        sortSongList();
        viewPager.setCurrentItem(0, true);
        toolbar.setTitle(new File(folderPath).getName());
        notifySongsFragmentAdapterChanged();
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
    public void onBackPressed() {
        if (drawerLayout.isDrawerOpen(GravityCompat.START)) {
            drawerLayout.closeDrawer(GravityCompat.START);
        } else if (viewPager.getCurrentItem() == 3) {
            YouTubeFragment fragment = (YouTubeFragment) getSupportFragmentManager().findFragmentByTag("f3");
            if (fragment != null && fragment.canGoBack()) {
                fragment.goBack();
            } else {
                super.onBackPressed();
            }
        } else if (viewPager.getCurrentItem() == 0 && !toolbar.getTitle().toString().equals("Reproductor")) {
            showAllSongs();
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