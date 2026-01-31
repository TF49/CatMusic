package com.example.catmusic.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.widget.TextView;

import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.example.catmusic.R;
import com.example.catmusic.adapter.SongsRecyclerViewAdapter;
import com.example.catmusic.bean.SongsList;
import com.example.catmusic.utils.FavoriteManager;

import java.io.Serializable;
import java.util.ArrayList;
import java.util.List;

/**
 * 我的收藏页面：展示收藏的歌曲列表，点击可进入播放器播放。
 */
public class FavoriteActivity extends BaseActivity {

    private RecyclerView favoriteList;
    private TextView favoriteCount;
    private SongsRecyclerViewAdapter adapter;
    private List<SongsList.ResultBean.SongsBean> songs = new ArrayList<>();
    private FavoriteManager favoriteManager;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_favorite);

        initTabBar(true, getString(R.string.my_favorites), false);
        favoriteManager = new FavoriteManager(this);

        favoriteList = findViewById(R.id.favorite_list);
        favoriteCount = findViewById(R.id.favorite_count);

        LinearLayoutManager lm = new LinearLayoutManager(this);
        favoriteList.setLayoutManager(lm);
        adapter = new SongsRecyclerViewAdapter(this, songs);
        adapter.setFavoriteManager(favoriteManager);
        adapter.setOnFavoriteClickListener(new SongsRecyclerViewAdapter.OnFavoriteClickListener() {
            @Override
            public void onFavoriteClick(int position, boolean isNowFavorite) {
                refreshList();
            }
        });
        adapter.setOnItemClickListener(new SongsRecyclerViewAdapter.OnItemClickListener() {
            @Override
            public void onItemClick(int position) {
                if (position < 0 || position >= songs.size()) return;
                Intent intent = new Intent(FavoriteActivity.this, PlayerActivity.class);
                intent.putExtra("songs_list", (Serializable) new ArrayList<>(songs));
                intent.putExtra("current_position", position);
                startActivity(intent);
            }
        });
        favoriteList.setAdapter(adapter);

        refreshList();
    }

    private void refreshList() {
        songs.clear();
        songs.addAll(favoriteManager.getAllFavorites());
        adapter.notifyDataSetChanged();
        favoriteCount.setText(getString(R.string.my_favorites) + "（" + songs.size() + " 首）");
    }

    @Override
    protected void onResume() {
        super.onResume();
        refreshList();
    }
}
