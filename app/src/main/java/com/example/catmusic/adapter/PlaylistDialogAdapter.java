package com.example.catmusic.adapter;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.example.catmusic.R;
import com.example.catmusic.bean.SongsList;

import java.util.ArrayList;
import java.util.List;

/**
 * 播放列表对话框内的列表适配器：显示歌名、歌手，支持点击播放、点击删除。
 */
public class PlaylistDialogAdapter extends RecyclerView.Adapter<PlaylistDialogAdapter.ViewHolder> {

    private final List<SongsList.ResultBean.SongsBean> list = new ArrayList<>();
    private int currentIndex = -1;
    private OnItemPlayListener onItemPlayListener;
    private OnItemRemoveListener onItemRemoveListener;

    public interface OnItemPlayListener {
        void onPlay(int position);
    }

    public interface OnItemRemoveListener {
        void onRemove(int position);
    }

    public void setOnItemPlayListener(OnItemPlayListener l) { this.onItemPlayListener = l; }
    public void setOnItemRemoveListener(OnItemRemoveListener l) { this.onItemRemoveListener = l; }

    public void setList(List<SongsList.ResultBean.SongsBean> newList) {
        list.clear();
        if (newList != null) list.addAll(newList);
        notifyDataSetChanged();
    }

    public void setCurrentIndex(int index) {
        int old = currentIndex;
        currentIndex = index;
        if (old >= 0 && old < list.size()) notifyItemChanged(old);
        if (currentIndex >= 0 && currentIndex < list.size()) notifyItemChanged(currentIndex);
    }

    public void removeAt(int position) {
        if (position < 0 || position >= list.size()) return;
        list.remove(position);
        notifyItemRemoved(position);
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        View v = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_playlist, parent, false);
        return new ViewHolder(v);
    }

    @Override
    public void onBindViewHolder(@NonNull ViewHolder holder, int position) {
        if (position >= list.size()) return;
        SongsList.ResultBean.SongsBean song = list.get(position);
        holder.title.setText(song.getName() != null ? song.getName() : "未知歌曲");
        holder.artist.setText(song.getSinger() != null ? song.getSinger() : "未知歌手");
        holder.itemView.setSelected(position == currentIndex);
        holder.itemView.setOnClickListener(v -> {
            if (onItemPlayListener != null) onItemPlayListener.onPlay(holder.getAdapterPosition());
        });
        holder.remove.setOnClickListener(v -> {
            if (onItemRemoveListener != null) onItemRemoveListener.onRemove(holder.getAdapterPosition());
        });
    }

    @Override
    public int getItemCount() {
        return list.size();
    }

    static class ViewHolder extends RecyclerView.ViewHolder {
        TextView title, artist;
        ImageView remove;

        ViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.playlist_item_title);
            artist = itemView.findViewById(R.id.playlist_item_artist);
            remove = itemView.findViewById(R.id.playlist_item_remove);
        }
    }
}
