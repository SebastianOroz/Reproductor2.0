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

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager; // ¡Importante! Añadir esta importación
import android.content.SharedPreferences; // ¡Importante! Añadir esta importación

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
    private boolean showAlbumArtPreference; // Variable para almacenar la preferencia de mostrar carátula

    // --- Interfaz para el listener del botón de opciones ---
    public interface OnOptionsButtonClickListener {
        void onOptionsButtonClick(Song song);
    }

    private OnOptionsButtonClickListener onOptionsButtonClickListener;

    public SongAdapter(Context context, ArrayList<Song> songs) {
        super(context, 0, songs);
        this.context = context;
        this.songs = songs;
        this.defaultAlbumArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album_art);
        // Inicializar la preferencia al crear el adaptador
        loadShowAlbumArtPreference();
    }

    // --- Método para establecer el listener del botón de opciones ---
    public void setOnOptionsButtonClickListener(OnOptionsButtonClickListener listener) {
        this.onOptionsButtonClickListener = listener;
    }

    // --- NUEVO: Método para cargar la preferencia de visibilidad de la carátula ---
    private void loadShowAlbumArtPreference() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.showAlbumArtPreference = sharedPrefs.getBoolean("pref_show_album_art", true);
    }

    // --- NUEVO: Método para actualizar la preferencia y forzar el redibujado de la lista ---
    public void updateShowAlbumArtPreference(boolean showAlbumArt) {
        this.showAlbumArtPreference = showAlbumArt;
        notifyDataSetChanged(); // Notifica al adaptador que los datos han cambiado para redibujar la lista
    }

    @NonNull
    @Override
    public View getView(int position, View convertView, @NonNull ViewGroup parent) {
        Song song = getItem(position);
        ViewHolder holder; // Declara el ViewHolder

        if (convertView == null) {
            // Infla el layout y crea un nuevo ViewHolder si la vista no existe
            convertView = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
            holder = new ViewHolder();
            holder.songTitle = convertView.findViewById(R.id.songTitle);
            holder.songArtist = convertView.findViewById(R.id.songArtist);
            holder.songDuration = convertView.findViewById(R.id.songDuration);
            holder.albumArt = convertView.findViewById(R.id.albumArt);
            holder.btnOptions = convertView.findViewById(R.id.btnOptions);
            convertView.setTag(holder); // Almacena el ViewHolder en la vista
        } else {
            // Recupera el ViewHolder de la vista reciclada
            holder = (ViewHolder) convertView.getTag();
        }

        if (song != null) {
            holder.songTitle.setText(song.getTitle());
            holder.songArtist.setText(song.getArtist());
            holder.songDuration.setText(formatDuration(song.getDuration()));

            // --- Lógica para mostrar/ocultar la carátula según la preferencia ---
            if (showAlbumArtPreference) {
                holder.albumArt.setVisibility(View.VISIBLE); // Asegurarse de que esté visible
                loadAlbumArt(song, holder.albumArt); // Cargar la imagen
            } else {
                holder.albumArt.setVisibility(View.GONE); // Ocultar el ImageView
                holder.albumArt.setImageDrawable(null); // ¡Importante! Limpiar cualquier imagen previa
            }

            // Configurar el listener para el botón de opciones
            holder.btnOptions.setOnClickListener(v -> {
                if (onOptionsButtonClickListener != null) {
                    onOptionsButtonClickListener.onOptionsButtonClick(song); // Notificar a la MainActivity
                }
            });
        }

        return convertView;
    }

    // --- Clase ViewHolder estática para optimizar el rendimiento ---
    static class ViewHolder {
        TextView songTitle;
        TextView songArtist;
        TextView songDuration;
        ImageView albumArt;
        ImageButton btnOptions;
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
                        // Si no hay imagen local, buscar en la web.
                        // La condición `!imageView.getDrawable().getConstantState().equals(...)` ha sido eliminada
                        // para simplificar y mejorar el manejo del reciclaje de vistas.
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
                // Advertencia: El scraping puede no ser fiable a largo plazo y puede ir contra los términos de servicio
                // de Google. Considera usar una API si esta funcionalidad es crítica.
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
                imageView.setImageResource(R.drawable.default_album_art); // Asegurarse de mostrar el placeholder si no se encuentra nada
            }
        }

        private String downloadHtml(String url) throws IOException {
            HttpURLConnection connection = (HttpURLConnection) new URL(url).openConnection();
            connection.setRequestProperty("User-Agent", "Mozilla/5.0"); // Simular un navegador
            connection.setConnectTimeout(5000); // 5 segundos para conectar
            connection.
                    setReadTimeout(10000); // 10 segundos para leer
            InputStream inputStream = connection.getInputStream();

            StringBuilder html = new StringBuilder();
            byte[] buffer = new byte[1024];
            int bytesRead;

            while ((bytesRead = inputStream.read(buffer)) != -1) {
                html.append(new String(buffer, 0, bytesRead));
            }

            inputStream.close();
            connection.disconnect(); // Cerrar la conexión
            return html.toString();
        }

        private String extractFirstImageUrl(String html) {
            // Expresión regular para encontrar URLs de imágenes en los resultados de Google
            // Buscar URLs que estén en el formato esperado de Google Images
            Pattern pattern = Pattern.compile("\"ou\":\"(https?://[^\"]+?\\.(?:png|jpg|jpeg|gif|bmp))\""); // Busca URLs de imagen comunes
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