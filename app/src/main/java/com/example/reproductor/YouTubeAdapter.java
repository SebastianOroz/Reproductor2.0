package com.example.reproductor;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.squareup.picasso.Picasso;
import java.util.List;

public class YouTubeAdapter extends RecyclerView.Adapter<YouTubeAdapter.YouTubeViewHolder> {

    private List<YoutubeItem> youtubeItems;
    private OnItemClickListener listener;

    public interface OnItemClickListener {
        void onItemClick(YoutubeItem item);
    }

    public YouTubeAdapter(List<YoutubeItem> youtubeItems, OnItemClickListener listener) {
        this.youtubeItems = youtubeItems;
        this.listener = listener;
    }

    @NonNull
    @Override
    public YouTubeViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.youtube_item_layout, parent, false);
        return new YouTubeViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull YouTubeViewHolder holder, int position) {
        YoutubeItem currentItem = youtubeItems.get(position);
        holder.titleTextView.setText(currentItem.getTitle());
        Picasso.get().load(currentItem.getThumbnailUrl()).into(holder.thumbnailImageView);
        holder.itemView.setOnClickListener(v -> listener.onItemClick(currentItem));
    }

    @Override
    public int getItemCount() {
        return youtubeItems.size();
    }

    static class YouTubeViewHolder extends RecyclerView.ViewHolder {
        ImageView thumbnailImageView;
        TextView titleTextView;

        YouTubeViewHolder(@NonNull View itemView) {
            super(itemView);
            thumbnailImageView = itemView.findViewById(R.id.thumbnail_image_view);
            titleTextView = itemView.findViewById(R.id.title_text_view);
        }
    }
}