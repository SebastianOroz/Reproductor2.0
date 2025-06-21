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
import android.widget.LinearLayout; // ¡NUEVO! Importar LinearLayout para el fondo de selección

import androidx.annotation.NonNull;
import androidx.preference.PreferenceManager;
import android.content.SharedPreferences;
import androidx.core.content.ContextCompat; // ¡NUEVO! Para obtener colores de recursos

import com.squareup.picasso.Picasso;

import java.io.IOException;
import java.io.InputStream;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.ArrayList;
import java.util.HashSet; // ¡NUEVO! Para almacenar IDs de canciones seleccionadas
import java.util.Set; // ¡NUEVO! Para almacenar IDs de canciones seleccionadas
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SongAdapter extends ArrayAdapter<Song> {
    private final Context context;
    private final ArrayList<Song> songs;
    private final Bitmap defaultAlbumArt;
    private boolean showAlbumArtPreference;

    // --- Interfaz para el listener del botón de opciones (EXISTENTE) ---
    public interface OnOptionsButtonClickListener {
        void onOptionsButtonClick(Song song); // Este método ahora disparará el diálogo "Ver Detalles/Añadir a Playlist"
    }

    // --- ¡NUEVO! Interfaz para el listener de selección de canciones ---
    public interface OnSongSelectionListener {
        void onSongSelected(Song song); // Cuando una canción es seleccionada
        void onSongDeselected(Song song); // Cuando una canción es deseleccionada
        void onSelectionModeChanged(boolean inSelectionMode); // Cuando el modo de selección cambia
    }

    private OnOptionsButtonClickListener onOptionsButtonClickListener;
    private OnSongSelectionListener onSongSelectionListener; // ¡NUEVO! Declaración del listener de selección

    // --- ¡NUEVO! Variables para el modo de selección múltiple ---
    private boolean inSelectionMode = false;
    // Usamos un HashSet para un rendimiento rápido de añadir/eliminar/comprobar existencia
    private Set<Long> selectedSongIds = new HashSet<>();

    public SongAdapter(Context context, ArrayList<Song> songs) {
        super(context, 0, songs);
        this.context = context;
        this.songs = songs;
        this.defaultAlbumArt = BitmapFactory.decodeResource(context.getResources(), R.drawable.default_album_art);
        loadShowAlbumArtPreference();
    }

    // --- Método para establecer el listener del botón de opciones (EXISTENTE) ---
    public void setOnOptionsButtonClickListener(OnOptionsButtonClickListener listener) {
        this.onOptionsButtonClickListener = listener;
    }

    // --- ¡NUEVO! Método para establecer el listener de selección ---
    public void setOnSongSelectionListener(OnSongSelectionListener listener) {
        this.onSongSelectionListener = listener;
    }

    // --- Métodos para controlar el modo de selección (¡NUEVOS!) ---
    public boolean isInSelectionMode() {
        return inSelectionMode;
    }

    public void setInSelectionMode(boolean inSelectionMode) {
        if (this.inSelectionMode != inSelectionMode) { // Solo si el modo realmente cambia
            this.inSelectionMode = inSelectionMode;
            if (!inSelectionMode) { // Si salimos del modo, limpia cualquier selección
                selectedSongIds.clear();
            }
            if (onSongSelectionListener != null) {
                onSongSelectionListener.onSelectionModeChanged(inSelectionMode);
            }
            notifyDataSetChanged(); // Notifica para que todas las vistas se redibujen con el nuevo modo
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
        // Si no quedan canciones seleccionadas y estamos en modo de selección, salir del modo
        if (selectedSongIds.isEmpty() && inSelectionMode) {
            setInSelectionMode(false);
        }
        notifyDataSetChanged(); // Actualiza la vista de la canción específica
    }

    public ArrayList<Long> getSelectedSongIds() {
        return new ArrayList<>(selectedSongIds); // Devuelve una copia de la lista de IDs
    }

    public void clearSelection() {
        selectedSongIds.clear();
        setInSelectionMode(false); // Esto ya llama a notifyDataSetChanged()
    }


    // --- Método para cargar la preferencia de visibilidad de la carátula (EXISTENTE) ---
    private void loadShowAlbumArtPreference() {
        SharedPreferences sharedPrefs = PreferenceManager.getDefaultSharedPreferences(context);
        this.showAlbumArtPreference = sharedPrefs.getBoolean("pref_show_album_art", true);
    }

    // --- Método para actualizar la preferencia y forzar el redibujado de la lista (EXISTENTE) ---
    public void updateShowAlbumArtPreference(boolean showAlbumArt) {
        this.showAlbumArtPreference = showAlbumArt;
        notifyDataSetChanged(); // Notifica al adaptador que los datos han cambiado para redibujar la lista
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
            holder.btnOptions = convertView.findViewById(R.id.btnOptions); // Asegúrate de que el ID en item_song.xml sea 'optionsButton'
            holder.songItemContainer = convertView.findViewById(R.id.songItemContainer); // ¡NUEVO! Obtener referencia al contenedor principal
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

            // Configurar el listener para el botón de opciones
            holder.btnOptions.setOnClickListener(v -> {
                if (onOptionsButtonClickListener != null) {
                    // El botón de opciones debe seguir funcionando sin afectar el modo de selección
                    onOptionsButtonClickListener.onOptionsButtonClick(song);
                }
            });

            // --- ¡NUEVO! Lógica para el fondo de selección ---
            if (inSelectionMode && selectedSongIds.contains(song.getId())) {
                holder.songItemContainer.setBackgroundColor(ContextCompat.getColor(context, R.color.selected_item_background));
            } else {
                // Si no está seleccionado o no está en modo de selección, usa el fondo normal (transparente o tu color de fondo por defecto)
                holder.songItemContainer.setBackgroundColor(ContextCompat.getColor(context, android.R.color.transparent));
            }
        }

        return convertView;
    }

    // --- Clase ViewHolder estática para optimizar el rendimiento (Modificada para incluir songItemContainer) ---
    static class ViewHolder {
        TextView songTitle;
        TextView songArtist;
        TextView songDuration;
        ImageView albumArt;
        ImageButton btnOptions;
        LinearLayout songItemContainer; // ¡NUEVO! Campo para el contenedor del ítem
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
                        // Imagen local encontrada
                    }

                    @Override
                    public void onError(Exception e) {
                        // Si no hay imagen local, buscar en la web.
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