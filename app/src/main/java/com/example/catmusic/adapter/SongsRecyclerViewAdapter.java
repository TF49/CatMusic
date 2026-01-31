package com.example.catmusic.adapter;

import android.annotation.SuppressLint;
import android.content.Context;
import android.content.Intent;
import com.example.catmusic.utils.LogUtil;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import com.bumptech.glide.Glide;
import com.example.catmusic.R;
import com.example.catmusic.bean.SongsList;
import com.example.catmusic.ui.activity.PlayerActivity;
import com.example.catmusic.utils.FavoriteManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

public class SongsRecyclerViewAdapter extends RecyclerView.Adapter<SongsRecyclerViewAdapter.SongViewHolder>
{

    private Context context;
    private List<SongsList.ResultBean.SongsBean> songs;
    private OnItemClickListener listener;
    private OnFavoriteClickListener favoriteListener;
    private FavoriteManager favoriteManager;
    private boolean showFavoriteButton = true;

    public interface OnItemClickListener {
        void onItemClick(int position);
    }

    public interface OnFavoriteClickListener {
        void onFavoriteClick(int position, boolean isNowFavorite);
    }

    public void setOnItemClickListener(OnItemClickListener listener) {
        this.listener = listener;
    }

    public void setOnFavoriteClickListener(OnFavoriteClickListener listener) {
        this.favoriteListener = listener;
    }

    /** 设置收藏管理器，为 null 则不显示收藏按钮 */
    public void setFavoriteManager(FavoriteManager manager) {
        this.favoriteManager = manager;
    }

    /** 是否在列表项中显示收藏按钮 */
    public void setShowFavoriteButton(boolean show) {
        this.showFavoriteButton = show;
    }

    public SongsRecyclerViewAdapter(Context context, List<SongsList.ResultBean.SongsBean> songs)
    {
        this.context = context;
        this.songs = songs;
    }

    @NonNull
    @Override
    public SongViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(context).inflate(R.layout.item_song, parent, false);
        return new SongViewHolder(view);
    }

    @Override
    public void onBindViewHolder(@NonNull SongViewHolder holder,@SuppressLint("RecyclerView") int position)
    {
        if (songs != null && position < songs.size())
        {
            SongsList.ResultBean.SongsBean song = songs.get(position);

            // 设置歌曲名称
            if (holder.songTitle != null)
            {
                holder.songTitle.setText(song.getName() != null ? song.getName() : "未知歌曲");
            }

            // 设置歌手信息
            if (holder.songArtist != null)
            {
                holder.songArtist.setText(song.getSinger() != null ? song.getSinger() : "未知歌手");
            }

            // 加载歌曲封面图片
            if (holder.songIcon != null && song.getPic() != null && !song.getPic().isEmpty())
            {
                Glide.with(context)
                        .load(song.getPic())
                        .placeholder(R.drawable.ic_launcher_foreground)
                        .error(R.drawable.ic_launcher_foreground)
                        .into(holder.songIcon);
            }

            // 收藏按钮：显示状态与点击
            if (holder.songFavorite != null) {
                boolean showFav = showFavoriteButton && favoriteManager != null;
                holder.songFavorite.setVisibility(showFav ? View.VISIBLE : View.GONE);
                if (showFav) {
                    boolean isFav = favoriteManager.isFavorite(song);
                    holder.songFavorite.setImageResource(isFav ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
                    holder.songFavorite.setOnClickListener(new View.OnClickListener() {
                        @Override
                        public void onClick(View v) {
                            int pos = holder.getAdapterPosition();
                            if (pos < 0 || pos >= songs.size()) return;
                            SongsList.ResultBean.SongsBean s = songs.get(pos);
                            boolean nowFav = favoriteManager.toggleFavorite(s);
                            holder.songFavorite.setImageResource(nowFav ? R.drawable.ic_favorite_filled : R.drawable.ic_favorite_border);
                            if (favoriteListener != null) favoriteListener.onFavoriteClick(pos, nowFav);
                        }
                    });
                }
            }

            // 设置点击事件
            holder.itemView.setOnClickListener(new View.OnClickListener()
            {
                @Override
                public void onClick(View v)
                {
                    int pos = holder.getAdapterPosition();
                    LogUtil.d("SongsAdapter", "点击位置: " + pos);
                    
                    // 调用Activity设置的监听器
                    if (listener != null)
                    {
                        listener.onItemClick(pos);
                    }
                    
                    // 检查上下文和数据有效性
                    if (context == null || songs == null || songs.isEmpty() || pos < 0 || pos >= songs.size()) {
                        LogUtil.e("SongsAdapter", "无效的上下文或数据");
                        return;
                    }
                    
                    // 跳转到播放器Activity
                    try {
                        Intent intent = new Intent(context, PlayerActivity.class);
                        // 使用 ArrayList 包装 songs 列表以确保可序列化
                        intent.putExtra("songs_list", (Serializable) new ArrayList<>(songs));
                        intent.putExtra("current_position", pos);
                        intent.setFlags(Intent.FLAG_ACTIVITY_NEW_TASK);
                        context.startActivity(intent);
                        LogUtil.d("SongsAdapter", "成功启动PlayerActivity");
                    } catch (Exception e) {
                        LogUtil.e("SongsAdapter", "启动PlayerActivity失败: " + e.getMessage());
                        Toast.makeText(context, "无法打开播放器: " + e.getMessage(), Toast.LENGTH_SHORT).show();
                    }
                }
            });
        }
    }

    @Override
    public int getItemCount()
    {
        return songs != null ? songs.size() : 0;
    }

    /**
     * 更新歌曲列表数据
     * @param newSongs 新的歌曲列表数据
     */
    public void updateSongs(List<SongsList.ResultBean.SongsBean> newSongs)
    {
        this.songs = newSongs;
        notifyDataSetChanged();
    }

    /**
     * 歌曲列表项视图持有者
     */
    static class SongViewHolder extends RecyclerView.ViewHolder
    {
        ImageView songIcon;
        TextView songTitle;
        TextView songArtist;
        ImageView songFavorite;

        public SongViewHolder(@NonNull View itemView)
        {
            super(itemView);
            songIcon = itemView.findViewById(R.id.song_icon);
            songTitle = itemView.findViewById(R.id.song_title);
            songArtist = itemView.findViewById(R.id.song_artist);
            songFavorite = itemView.findViewById(R.id.song_favorite);
        }
    }
}