package com.example.catmusic.adapter;

import android.content.Context;
import android.content.Intent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.catmusic.R;
import com.example.catmusic.bean.Recommend;
import com.example.catmusic.ui.activity.SongListActivity;
import java.util.List;

public class AlbumsRecyclerViewAdapter extends RecyclerView.Adapter<AlbumsRecyclerViewAdapter.ViewHolder>
{
    private List<Recommend.ResultBean.AlbumsBean> albums;
    private Context mContext;
    private OnAlbumClickListener listener;
    public interface OnAlbumClickListener
    {
        void onAlbumClick(int position);
    }
    public void setOnAlbumClickListener(OnAlbumClickListener listener)
    {
        this.listener = listener;
    }

    public AlbumsRecyclerViewAdapter(Context context, List<Recommend.ResultBean.AlbumsBean> albums)
    {
        this.albums = albums;
        this.mContext = context;
    }

    @NonNull
    @Override
    public ViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType)
    {
        View view = LayoutInflater.from(parent.getContext()).inflate(R.layout.item_song, parent, false);
        return new ViewHolder(view);
    }


//     每渲染一条数据就会调用这个方法
@Override
public void onBindViewHolder(@NonNull ViewHolder holder, int position)
{
    // 使用getAdapterPosition确保位置正确
    int realPosition = holder.getAdapterPosition();
    if (realPosition == RecyclerView.NO_POSITION) return;

    Recommend.ResultBean.AlbumsBean album = albums.get(realPosition);

    Glide.with(holder.albumsIcon.getContext())
            .load(album.getPic())
            .into(holder.albumsIcon);

    holder.albumsTitle.setText(album.getTitle());
    holder.albumsDes.setText(album.getUsername()!= null ? album.getUsername() : album.getTitle());

    //添加点击事件
    holder.itemView.setOnClickListener(new View.OnClickListener()
    {
        @Override
        public void onClick(View view)
        {
            int pos = holder.getAdapterPosition();
            if (pos != RecyclerView.NO_POSITION) {
                Intent intent = new Intent(mContext, SongListActivity.class);
                intent.putExtra("id", album.getId());
                intent.putExtra("title", album.getTitle());
                intent.putExtra("pic", album.getPic());

                mContext.startActivity(intent);
                if (listener != null)
                {
                    listener.onAlbumClick(pos);
                }
            }
        }
    });
}


    @Override
    public int getItemCount()
    {
        return albums.size();
    }


//    做缓存 优化性能
    static class ViewHolder extends RecyclerView.ViewHolder
    {
        ImageView albumsIcon;
        TextView albumsTitle;
        TextView albumsDes;

        public ViewHolder(@NonNull View itemView)
        {
            super(itemView);
            albumsIcon = itemView.findViewById(R.id.song_icon);
            albumsTitle = itemView.findViewById(R.id.song_title);
            albumsDes = itemView.findViewById(R.id.song_artist);
        }
    }
}