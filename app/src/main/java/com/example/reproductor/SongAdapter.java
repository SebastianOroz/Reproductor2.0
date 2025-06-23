package com.example.reproductor;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.net.Uri;
import android.os.AsyncTask;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ArrayAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ImageButton;
import android.widget.LinearLayout;

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import android.content.SharedPreferences;
import androidx.core.content.ContextCompat;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.Set;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SongAdapter extends ArrayAdapter<Song> {
    private final Context context;
    private final ArrayList<Song> songs;
    private final Bitmap defaultAlbumArt;
    private boolean showAlbumArtPreference;


    public interface OnOptionsButtonClickListener {
        void onOptionsButtonClick(Song song);
    }


    public interface OnSongSelectionListener {
        void onSongSelected(Song song);
        void onSongDeselected(Song song);
        void onSelectionModeChanged(boolean inSelectionMode);
    }

    private OnOptionsButtonClickListener onOptionsButtonClickListener;
    private OnSongSelectionListener onSongSelectionListener;


    private boolean inSelectionMode = false;

    private Set<Long> selectedSongIds = new HashSet<>();

    public SongAdapter(Context context, ArrayList<Song> songs) {
        super(context, 0, songs);
        this.context = context;
        this.songs = songs;
        this.defaultAlbumArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album_art);
        loadShowAlbumArtPreference();
    }


    public void setOnOptionsButtonClickListener(OnOptionsButtonClickListener listener) {
        this.onOptionsButtonClickListener = listener;
    }


    public void setOnSongSelectionListener(OnSongSelectionListener listener) {
        this.onSongSelectionListener = listener;
    }


    public boolean isInSelectionMode() {
        return inSelectionMode;
    }

    public void setInSelectionMode(boolean inSelectionMode) {
        if (this.inSelectionMode != inSelectionMode) {
            this.inSelectionMode = inSelectionMode;
            if (!inSelectionMode) {
                selectedSongIds.clear();
            }
            if (onSongSelectionListener != null) {
                onSongSelectionListener.onSelectionModeChanged(inSelectionMode);
            }
            notifyDataSetChanged();
        }
    }

    public void toggleSelection(Song song) {
        if (selectedSongIds.contains(song.getId())) {
            selectedSongIds.remove(song.getId());
            if (onSongSelectionListener != null) {
                onSongSelectionListener.onSongDeselected(song);
            }
        } else {
            selectedSongIds.add(song.getId());
            if (onSongSelectionListener != null) {
                onSongSelectionListener.onSongSelected(song);
            }
        }

        if (selectedSongIds.isEmpty() && inSelectionMode) {
            setInSelectionMode(false);
        }
        notifyDataSetChanged();
    }

    public ArrayList<Long> getSelectedSongIds() {
        return new ArrayList<>(selectedSongIds);
    }

    public void clearSelection() {
        selectedSongIds.clear();
        setInSelectionMode(false);
    }



    private void loadShowAlbumArtPreference() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.showAlbumArtPreference = sharedPrefs.getBoolean("pref_show_album_art", true);
    }


    public void updateShowAlbumArtPreference(boolean showAlbumArt) {
        this.showAlbumArtPreference = showAlbumArt;
        notifyDataSetChanged();
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Song song = getItem(position);
        ViewHolder holder;

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
            holder = new ViewHolder();
            holder.songTitle = convertView.findViewById(R.id.songTitle);
            holder.songArtist = convertView.findViewById(R.id.songArtist);
            holder.songDuration = convertView.findViewById(R.id.songDuration);
            holder.albumArt = convertView.findViewById(R.id.albumArt);
            holder.btnOptions = convertView.findViewById(R.id.btnOptions);
            holder.songItemContainer = convertView.findViewById(R.id.songItemContainer);
            convertView.setTag(holder);
        } else {
            holder = (ViewHolder) convertView.getTag();
        }

        if (song != null) {
            holder.songTitle.setText(song.getTitle());
            holder.songArtist.setText(song.getArtist());
            holder.songDuration.setText(formatDuration(song.getDuration()));

            if (showAlbumArtPreference) {
                holder.albumArt.setVisibility(View.VISIBLE);
                loadAlbumArt(song, holder.albumArt);
            } else {
                holder.albumArt.setVisibility(View.GONE);
                holder.albumArt.setImageDrawable(null);
            }


            holder.btnOptions.setOnClickListener(v -> {
                if (onOptionsButtonClickListener != null) {

                    onOptionsButtonClickListener.onOptionsButtonClick(song);
                }
            });


            if (inSelectionMode && selectedSongIds.contains(song.getId())) {
                holder.songItemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_item_background));
            } else {

                holder.songItemContainer.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            }
        }

        return convertView;
    }


    static class ViewHolder {
        TextView songTitle;
        TextView songArtist;
        TextView songDuration;
        ImageView albumArt;
        ImageButton btnOptions;
        LinearLayout songItemContainer;
    }

    private void loadAlbumArt(Song song, ImageView imageView) {
        Uri albumArtUri = Uri.parse("content://media/external/audio/albumart/" + song.getAlbumId());

        Picasso.get()
                .load(albumArtUri)
                .placeholder(R.drawable.default_album_art)
                .error(R.drawable.default_album_art)
                .into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {

                    }

                    @Override
                    public void onError(Exception e) {

                        new AlbumArtSearchTask(imageView).execute(song);
                    }
                });
    }

    private class AlbumArtSearchTask extends AsyncTask<Song, Void, String> {
        private final ImageView imageView;

        AlbumArtSearchTask(ImageView imageView) {
            this.imageView = imageView;
        }

        @Override
        protected String doInBackground(Song... songs) {
            Song song = songs[0];
            try {
                String searchUrl = "https://www.google.com/search?tbm=isch&q=" +
                        Uri.encode(song.getTitle() + " " + song.getArtist() + " album cover 500x500");
                String html = downloadHtml(searchUrl);
                return extractFirstImageUrl(html);
            } catch (IOException e) {
                e.printStackTrace();
                return null;
            }
        }

        @Override
        protected void onPostExecute(String imageUrl) {
            if (imageUrl != null && !imageUrl.isEmpty()) {
                Picasso.get()
                        .load(imageUrl)
                        .placeholder(R.drawable.default_album_art)
                        .error(R.drawable.default_album_art)
                        .into(imageView);
            } else {
                imageView.setImageResource(R.drawable.default_album_art);
            }
        }

        private String downloadHtml(String url) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            connection.setConnectTimeout(5000);
            connection.setReadTimeout(10000);
            InputStream inputStream = connection.getInputStream();

            StringBuilder html = new StringBuilder();
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                html.append(new String(buffer, 0, bytesRead));
            }

            inputStream.close();
            connection.disconnect();
            return html.toString();
        }

        private String extractFirstImageUrl(String html) {
            Pattern pattern = Pattern.compile("\"ou\":\"(https?://[^\"]+?\\.(?:png|jpg|jpeg|gif|bmp))\"");
            Matcher matcher = pattern.matcher(html);

            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }
    }

    private String formatDuration(long duration) {
        long minutes = TimeUnit.MILLISECONDS.toMinutes(duration);
        long seconds = TimeUnit.MILLISECONDS.toSeconds(duration) -
                TimeUnit.MINUTES.toSeconds(minutes);
        long hours = TimeUnit.MILLISECONDS.toHours(duration);

        if (hours > 0) {
            return String.format("%02d:%02d:%02d", hours, minutes, seconds);
        } else {
            return String.format("%02d:%02d", minutes, seconds);
        }
    }
}