package com.example.reproductor;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.EditorInfo;
import android.view.inputmethod.InputMethodManager;
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

import com.android.volley.NetworkResponse;
import com.android.volley.Request;
import com.android.volley.RequestQueue;
import com.android.volley.VolleyError;
import com.android.volley.toolbox.JsonObjectRequest;
import com.android.volley.toolbox.Volley;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.util.ArrayList;
import java.util.List;

public class YouTubeFragment extends Fragment {


    private static final String YOUTUBE_API_KEY = "AIzaSyB2KdO1OBqyJ48URVyPZcCJab6naVW69-w";


    private WebView webView;
    private EditText searchEditText;
    private Button searchButton;
    private RecyclerView recyclerView;
    private ProgressBar progressBar;
    private YouTubeAdapter adapter;
    private final List<YoutubeItem> youtubeItems = new ArrayList<>();
    private RequestQueue requestQueue;

    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        requestQueue = Volley.newRequestQueue(requireContext());
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        View view = inflater.inflate(R.layout.fragment_youtube, container, false);

        webView = view.findViewById(R.id.webView);
        searchEditText = view.findViewById(R.id.search_edit_text);
        searchButton = view.findViewById(R.id.search_button);
        recyclerView = view.findViewById(R.id.recycler_view_youtube);
        progressBar = view.findViewById(R.id.progress_bar);

        setupRecyclerView();
        setupWebView();

        searchButton.setOnClickListener(v -> performSearch());
        searchEditText.setOnEditorActionListener((v, actionId, event) -> {
            if (actionId == EditorInfo.IME_ACTION_SEARCH) {
                performSearch();
                return true;
            }
            return false;
        });

        return view;
    }

    private void performSearch() {
        InputMethodManager imm = (InputMethodManager) requireActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
        imm.hideSoftInputFromWindow(searchEditText.getWindowToken(), 0);

        String query = searchEditText.getText().toString().trim();
        if (query.isEmpty()) return;

        if (YOUTUBE_API_KEY.equals("TU_API_KEY_AQUI")) {
            Toast.makeText(getContext(), "Por favor, añade tu API Key en YouTubeFragment.java", Toast.LENGTH_LONG).show();
            return;
        }

        searchWithYouTubeApi(query);
    }

    private void setupRecyclerView() {
        recyclerView.setLayoutManager(new LinearLayoutManager(getContext()));
        adapter = new YouTubeAdapter(youtubeItems, item -> {
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

    private void searchWithYouTubeApi(String query) {
        progressBar.setVisibility(View.VISIBLE);
        recyclerView.setVisibility(View.GONE);
        youtubeItems.clear();
        adapter.notifyDataSetChanged();

        String url = "https://www.googleapis.com/youtube/v3/search" +
                "?part=snippet" +
                "&q=" + query +
                "&type=video" +
                "&maxResults=20" +
                "&key=" + YOUTUBE_API_KEY;

        JsonObjectRequest jsonObjectRequest = new JsonObjectRequest(Request.Method.GET, url, null,
                response -> {
                    progressBar.setVisibility(View.GONE);
                    recyclerView.setVisibility(View.VISIBLE);
                    try {
                        JSONArray items = response.getJSONArray("items");
                        for (int i = 0; i < items.length(); i++) {
                            JSONObject item = items.getJSONObject(i);
                            JSONObject id = item.getJSONObject("id");
                            String videoId = id.getString("videoId");
                            JSONObject snippet = item.getJSONObject("snippet");
                            String title = snippet.getString("title");
                            String thumbnailUrl = snippet.getJSONObject("thumbnails").getJSONObject("medium").getString("url");
                            youtubeItems.add(new YoutubeItem(title, videoId, thumbnailUrl));
                        }
                        adapter.notifyDataSetChanged();
                        if(youtubeItems.isEmpty()){
                            Toast.makeText(getContext(), "No se encontraron resultados", Toast.LENGTH_SHORT).show();
                        }
                    } catch (JSONException e) {
                        e.printStackTrace();
                        Toast.makeText(getContext(), "Error al procesar la respuesta", Toast.LENGTH_SHORT).show();
                    }
                },
                error -> {

                    progressBar.setVisibility(View.GONE);
                    String errorMessage = "Error en la petición: ";
                    NetworkResponse response = error.networkResponse;
                    if (response != null && response.data != null) {

                        String jsonError = new String(response.data);
                        errorMessage += response.statusCode + " " + jsonError;
                    } else {
                        errorMessage += error.getMessage();
                    }
                    Toast.makeText(getContext(), errorMessage, Toast.LENGTH_LONG).show();
                });

        requestQueue.add(jsonObjectRequest);
    }

    public boolean canGoBack() {
        if (webView.getVisibility() == View.VISIBLE) {
            webView.loadUrl("about:blank");
            webView.setVisibility(View.GONE);
            recyclerView.setVisibility(View.VISIBLE);
            return true;
        }
        return false;
    }
}