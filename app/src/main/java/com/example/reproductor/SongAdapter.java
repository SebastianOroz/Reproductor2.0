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

import androidx.annotation.NonNull;

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SongAdapter extends ArrayAdapter<Song> {
    private final Context context;
    private final ArrayList<Song> songs;
    private final Bitmap defaultAlbumArt;

    public SongAdapter(Context context, ArrayList<Song> songs) {
        super(context, 0, songs);
        this.context = context;
        this.songs = songs;
        this.defaultAlbumArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album_art);
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Song song = getItem(position);

        if (convertView == null) {
            convertView = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        }

        TextView songTitle = convertView.findViewById(R.id.songTitle);
        TextView songArtist = convertView.findViewById(R.id.songArtist);
        TextView songDuration = convertView.findViewById(R.id.songDuration);
        ImageView albumArt = convertView.findViewById(R.id.albumArt);

        if (song != null) {
            songTitle.setText(song.getTitle());
            songArtist.setText(song.getArtist());
            songDuration.setText(formatDuration(song.getDuration()));

            // Cargar imagen del álbum
            loadAlbumArt(song, albumArt);
        }

        return convertView;
    }

    private void loadAlbumArt(Song song, ImageView imageView) {
        // 1. Primero intentar con la imagen local del álbum
        Uri albumArtUri = Uri.parse("content://media/external/audio/albumart/" + song.getAlbumId());

        Picasso.get()
                .load(albumArtUri)
                .placeholder(R.drawable.default_album_art)
                .error(R.drawable.default_album_art)
                .into(imageView, new com.squareup.picasso.Callback() {
                    @Override
                    public void onSuccess() {
                        // Imagen local encontrada
                    }

                    @Override
                    public void onError(Exception e) {
                        // Si no hay imagen local, buscar en la web
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
                // Buscar en Google Images (técnica de scraping)
                String searchUrl = "https://www.google.com/search?tbm=isch&q=" +
                        Uri.encode(song.getTitle() + " " + song.getArtist() + " album cover 500x500");
                String html = downloadHtml(searchUrl);
                String demoImageUrl = "https://i.ytimg.com/vi/mlBZeNKCbSI/maxresdefault.jpg" ;
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
            }
        }

        private String downloadHtml(String url) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0");
            InputStream inputStream = connection.getInputStream();

            StringBuilder html = new StringBuilder();
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                html.append(new String(buffer, 0, bytesRead));
            }

            inputStream.close();
            return html.toString();
        }

        private String extractFirstImageUrl(String html) {
            // Expresión regular para encontrar URLs de imágenes en los resultados de Google
            Pattern pattern = Pattern.compile("\"ou\":\"(https?://[^\"]+?\\.[^\"]+?)\"");
            Matcher matcher = pattern.matcher(html);

            if (matcher.find()) {
                return matcher.group(1);
            }
            return null;
        }
    }

    private String formatDuration(long duration) {
        return String.format("%02d:%02d",
                TimeUnit.MILLISECONDS.toMinutes(duration),
                TimeUnit.MILLISECONDS.toSeconds(duration) -
                        TimeUnit.MINUTES.toSeconds(TimeUnit.MILLISECONDS.toMinutes(duration))
        );
    }
}