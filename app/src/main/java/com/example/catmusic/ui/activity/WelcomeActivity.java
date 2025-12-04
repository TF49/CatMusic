package com.example.catmusic.ui.activity;

import android.content.Intent;
import android.os.Bundle;
import android.os.Handler;
import android.view.View;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

import com.example.catmusic.R;

/**
 * 欢迎页面Activity
 * 启动应用后首先进入此页面，展示欢迎界面并延迟跳转到主页
 * 用户也可以点击跳过按钮直接进入主页
 */
public class WelcomeActivity extends AppCompatActivity
{
    private Button mBtnSkip;
    private Handler mHandler = new Handler();
    private Runnable mRunnableToLogin = new Runnable()
    {
        @Override
        public void run()
        {
            toHomeActivity();
        }
    };

    /**
     * Activity的入口方法，在创建Activity时被系统调用
     * 设置布局文件，初始化视图和事件监听器
     * 启动3秒延时跳转到主页的定时器
     * @param savedInstanceState 保存的Activity状态信息
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        super.onCreate(savedInstanceState);

        // 在设置内容视图之前设置主题
        setContentView(R.layout.activity_welcome);

        initView();
        initEvent();

        mHandler.postDelayed(mRunnableToLogin, 3000);
    }

    /**
     * 初始化事件监听器
     * 为跳过按钮设置点击事件监听器
     * 点击后取消定时跳转并立即跳转到主页
     */
    private void initEvent()
    {
        mBtnSkip.setOnClickListener(new View.OnClickListener()
        {
            @Override
            public void onClick(View v)
            {
                mHandler.removeCallbacks(mRunnableToLogin);
                toHomeActivity();
            }
        });
    }

    /**
     * 初始化界面组件
     * 通过findViewById查找布局文件中的跳过按钮并赋值给成员变量
     */
    private void initView()
    {
        mBtnSkip = findViewById(R.id.skip_btn);
    }

    /**
     * 跳转到主页Activity
     * 创建跳转到HomeActivity的Intent并启动
     * 结束当前Activity
     */
    private void toHomeActivity()
    {
        Intent intent = new Intent(this, HomeActivity.class);
        startActivity(intent);
        finish();
    }

    /**
     * Activity销毁时的回调方法
     * 在Activity被销毁前调用，用于清理资源
     * 移除定时跳转的回调，防止内存泄漏
     */
    @Override
    protected void onDestroy()
    {
        super.onDestroy();
        mHandler.removeCallbacks(mRunnableToLogin);
    }
}