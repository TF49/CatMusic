package com.example.catmusic.ui.activity;

import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.IdRes;
import androidx.appcompat.app.AppCompatActivity;

import com.example.catmusic.R;

public class BaseActivity extends AppCompatActivity
{
    private ImageView mIvBack, mIvMe;
    private TextView mTvTitle;

    /**
     * findViewById
     * @param id
     * @param <T>
     * @return
     */
    protected <T extends View> T fd(@IdRes int id)
    {
        return findViewById(id);
    }

    /**
     * 安全显示Toast，防止Activity销毁后崩溃
     */
    protected void showSafeToast(String message, int duration) {
        try {
            if (!isDestroyed() && !isFinishing()) {
                Toast.makeText(this, message, duration).show();
            }
        } catch (Exception e) {
            Log.w("BaseActivity", "Failed to show toast: " + e.getMessage());
        }
    }

    /**
     * 初始化NavigationBar
     * @param isShowBack 是否显示返回按钮
     * @param title      显示的标题
     * @param isShowMe   是否显示"我的"
     */
    protected void initTabBar(boolean isShowBack, String title, boolean isShowMe)
    {
        mIvBack = fd(R.id.tv_back);
        mTvTitle = fd(R.id.tv_title);
        mIvMe = fd(R.id.tv_logo_header);

        //判断哪些内容显示
        mIvBack.setVisibility(isShowBack ? View.VISIBLE : View.GONE);
        mIvMe.setVisibility(isShowMe ? View.VISIBLE : View.GONE);
        mTvTitle.setText(title);

        //设置返回按钮返回功能
        mIvBack.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                onBackPressed();
            }
        });
    }
}