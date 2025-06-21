package com.example.reproductor;

import android.os.AsyncTask;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebChromeClient;
import android.webkit.WebView;
import android.webkit.WebViewClient;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ProgressBar;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.select.Elements;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class YouTubeFragment extends Fragment {

    private WebView webView;
    private EditText searchEditText;
    private Button searchButton;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private YouTubeAdapter adapter;
    private final List<YoutubeItem> youtubeItems = new ArrayList<>();

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_youtube, container, false);

        // Inicializar vistas
        webView = view.findViewById(R.id.webView);
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchButton = view.findViewById(R.id.search_button);
        recyclerView = view.findViewById(R.id.recycler_view_youtube);
        progressBar = view.findViewById(R.id.progress_bar);

        setupRecyclerView();
        setupWebView();

        searchButton.setOnClickListener(v -> {
            String query = searchEditText.getText().toString().trim();
            if (!query.isEmpty()) {
                searchOnYouTube(query);
            }
        });

        return view;
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new YouTubeAdapter(youtubeItems, item -> {
            // Cuando se hace clic en un video, se reproduce en el WebView
            String embedUrl = "https://www.youtube.com/embed/" + item.getVideoId();
            webView.loadUrl(embedUrl);
            webView.setVisibility(View.VISIBLE);
            recyclerView.setVisibility(View.GONE);
        });
        recyclerView.setAdapter(adapter);
    }

    private void setupWebView() {
        webView.getSettings().setJavaScriptEnabled(true);
        webView.setWebViewClient(new WebViewClient());
        webView.setWebChromeClient(new WebChromeClient());
    }

    private void searchOnYouTube(String query) {
        // Mostrar la barra de progreso y limpiar resultados anteriores
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        youtubeItems.clear();
        adapter.notifyDataSetChanged();

        // Usar un Executor para realizar la operación de red en un hilo secundario
        ExecutorService executor = Executors.newSingleThreadExecutor();
        Handler handler = new Handler(Looper.getMainLooper());

        executor.execute(() -> {
            // Tarea en segundo plano
            List<YoutubeItem> results = new ArrayList<>();
            try {
                String url = "https://www.youtube.com/results?search_query=" + query.replace(" ", "+");
                Document doc = Jsoup.connect(url)
                        .userAgent("Mozilla/5.0 (Windows NT 10.0; Win64; x64) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/58.0.3029.110 Safari/537.36")
                        .get();

                // Selector para encontrar los elementos de video
                Elements videoElements = doc.select("div#contents ytd-video-renderer");

                for (Element element : videoElements) {
                    // Se extrae el ID del video, título y miniatura de forma más segura
                    String videoId = element.select("a#video-title").attr("href");
                    if (videoId.contains("?v=")) {
                        videoId = videoId.substring(videoId.indexOf("?v=") + 3);
                    }
                    String title = element.select("a#video-title").attr("title");
                    String thumbnailUrl = "https://i.ytimg.com/vi/" + videoId + "/hqdefault.jpg";

                    if (!videoId.isEmpty() && !title.isEmpty()) {
                        results.add(new YoutubeItem(title, videoId, thumbnailUrl));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }

            // De vuelta en el hilo principal para actualizar la UI
            handler.post(() -> {
                progressBar.setVisibility(View.GONE);
                recyclerView.setVisibility(View.VISIBLE);
                if (results.isEmpty()) {
                    Toast.makeText(getContext(), "No se encontraron resultados", Toast.LENGTH_SHORT).show();
                } else {
                    youtubeItems.addAll(results);
                    adapter.notifyDataSetChanged();
                }
            });
        });
    }

    /**
     * Maneja el botón de "atrás". Si el WebView es visible, lo oculta
     * y muestra la lista de resultados de nuevo.
     * @return true si el evento fue consumido, false de lo contrario.
     */
    public boolean canGoBack() {
        if (webView.getVisibility() == View.VISIBLE) {
            webView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }

    public void goBack() {
        webView.goBack();
    }

    private class SearchYouTubeTask extends AsyncTask<String, Void, List<YoutubeItem>> {
        @Override
        protected List<YoutubeItem> doInBackground(String... queries) {
            String query = queries[0];
            List<YoutubeItem> results = new ArrayList<>();
            try {
                String url = "[https://www.youtube.com/results?search_query=](https://www.youtube.com/results?search_query=)" + query;
                Document doc = Jsoup.connect(url).get();
                Elements videoElements = doc.select("a.yt-simple-endpoint.style-scope.ytd-video-renderer");

                for (Element element : videoElements) {
                    String videoId = element.attr("href").split("v=")[1];
                    String title = element.attr("title");
                    String thumbnailUrl = "[https://i.ytimg.com/vi/](https://i.ytimg.com/vi/)" + videoId + "/0.jpg";
                    if (!title.isEmpty()) {
                        results.add(new YoutubeItem(title, videoId, thumbnailUrl));
                    }
                }
            } catch (IOException e) {
                e.printStackTrace();
            }
            return results;
        }

        @Override
        protected void onPostExecute(List<YoutubeItem> results) {
            youtubeItems.clear();
            youtubeItems.addAll(results);
            adapter.notifyDataSetChanged();
        }
    }
}