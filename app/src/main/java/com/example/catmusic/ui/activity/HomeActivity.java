package com.example.catmusic.ui.activity;

import android.os.Bundle;
import android.util.Log;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import com.bumptech.glide.Glide;
import com.example.catmusic.R;
import com.example.catmusic.adapter.AlbumsRecyclerViewAdapter;
import com.example.catmusic.bean.Recommend;
import com.google.gson.Gson;
import com.youth.banner.Banner;
import com.youth.banner.adapter.BannerImageAdapter;
import com.youth.banner.holder.BannerImageHolder;
import com.youth.banner.indicator.CircleIndicator;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;

/**
 * 首页Activity，用于展示推荐内容，包括轮播图等
 * 通过网络请求获取推荐数据并展示在界面上
 */
public class HomeActivity extends BaseActivity
{
    private OkHttpClient okHttpClient;//用于发起网络请求
    private Gson gson;//用于JSON数据解析
    private Banner banner;//轮播图控件
    private RecyclerView albumsList; // 添加RecyclerView引用
    private List<Recommend.ResultBean.AlbumsBean> albums = new ArrayList<>();
    private AlbumsRecyclerViewAdapter albumsAdapter; // 添加适配器引用t<>();

    /**
     * Activity的入口方法，在创建Activity时被系统调用
     * @param savedInstanceState 保存的Activity状态信息
     */
    @Override
    protected void onCreate(Bundle savedInstanceState)
    {
        //调用父类的onCreate方法
        super.onCreate(savedInstanceState);

        //设置当前Activity的布局文件为 activity_home.xml
        setContentView(R.layout.activity_home);

        //初始化
        initTabBar(false,"乐猫音乐",true);

        //初始化界面组件
        initView();

        //初始化网络请求相关的组件
        initOkHttp();

        //获取推荐数据并显示
        getRecommend();
    }

    /**
     * 初始化界面组件
     * 通过findViewById查找布局文件中的组件并赋值给成员变量
     */
    private void initView()
    {
        banner = fd(R.id.banner);
        albumsList = fd(R.id.albums_list);// 初始化RecyclerView
    }

    /**
     * 初始化网络请求相关的组件
     * 创建一个 OkHttpClient 对象，它是OkHttp库的核心类，用于执行网络请求
     * 创建一个 Gson 对象，用于JSON数据的序列化和反序列化
     */
    private void initOkHttp()
    {
        //1.创建一个OkHttpClient对象
        okHttpClient = new OkHttpClient.Builder().build();
        //2.创建一个Gson对象
        gson = new Gson();
    }
    
    /**
     * 从服务器获取推荐数据
     * 创建一个 Request 对象，指定请求的URL为http://172.18.15.220:3000/api/getRecommend
     * 通过 okHttpClient.newCall(request).enqueue() 发起异步网络请求
     * 在 onFailure 回调中处理请求失败的情况，使用 runOnUiThread 在主线程中显示错误提示
     * 在 onResponse 回调中处理请求成功的情况：
     * 检查响应是否成功且响应体不为空
     * 获取响应体的JSON字符串
     * 使用 Gson 将JSON字符串解析为 Recommend 对象
     * 使用 runOnUiThread 在主线程中调用 handlerRecomendData 方法处理数据
     */
    private void getRecommend()
    {
        //2.定义我们的请求
        Request request = new Request.Builder()
                .url("http://172.18.16.222:3000/api/getRecommend")
                .build();

        //3.通过异步的方式发送请求
        okHttpClient.newCall(request).enqueue(new Callback()
        {
            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e)
            {
                runOnUiThread(new Runnable()
                {
                    @Override
                    public void run()
                    {
                        showSafeToast("网络请求失败: " + e.getMessage(), Toast.LENGTH_SHORT);
                    }
                });
            }

            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException
            {
                //4.处理response相应对象 服务器返回的数据就放在这个对象上
                if (response.isSuccessful() && response.body() != null)
                {
                    String jsonData = response.body().string();
                    //5.将jsonData（json字符串）转换成Recommend对象
                    Recommend recommend = gson.fromJson(jsonData, Recommend.class);

                    runOnUiThread(new Runnable()
                    {
                        @Override
                        public void run()
                        {
                            handlerRecomendData(recommend);
                            handlerAlbumsData(recommend);
                        }
                    });
                }
            }
        });
    }

    /**
     * 处理推荐数据并设置轮播图
     * @param recommend 推荐数据对象，包含轮播图信息
     */
    private void handlerRecomendData(Recommend recommend)
    {
        // 格式化图片地址，从推荐数据中提取轮播图图片URL列表
        //Recommend.ResultBean.SlidersBean: 这是一个嵌套类路径，表示：
        //Recommend类中有一个ResultBean内部类
        //ResultBean中有一个SlidersBean内部类
        //SlidersBean代表单个轮播图项的数据结构
        //recommend.getResult(): 调用Recommend对象的getResult()方法，获取结果对象
        //.getSliders(): 从结果对象中获取轮播图列表
        //这行代码从推荐数据中提取出所有的轮播图信息
        List<Recommend.ResultBean.SlidersBean> slidersBean = recommend.getResult().getSliders();

        List<String> imageUrls =  new ArrayList<>();

        for( Recommend.ResultBean.SlidersBean slider: slidersBean)
        {
            imageUrls.add(slider.getPic());
        }

        // 设置Banner轮播图组件，配置图片适配器和指示器
        //banner: Banner轮播图组件实例
        //.setAdapter(): 为Banner设置数据适配器
        //BannerImageAdapter<String>: 泛型适配器，指定数据类型为String（图片URL）
        //imageUrls: 构造适配器时传入图片URL列表
        banner.setAdapter(new BannerImageAdapter<String>(imageUrls)
                {
                    @Override
                    //创建BannerImageAdapter的匿名子类
                    //重写onBindView方法来定义如何显示每个轮播图项
                    public void onBindView(BannerImageHolder holder, String data, int position, int size)
                    {
                        //Glide.with(holder.itemView): 使用Glide图片加载库，传入当前项的视图上下文
                        //.load(data): 加载数据（这里是图片URL）
                        //.into(holder.imageView): 将加载的图片设置到holder的ImageView中
                        Glide.with(holder.itemView).load( data).into(holder.imageView);
                    }
                    //.addBannerLifecycleObserver(this): 添加生命周期观察者，使Banner能够响应生命周期变化（如页面销毁时停止轮播）
                    //.setIndicator(new CircleIndicator(this)): 设置圆形指示器，显示当前轮播位置和总数量
                })
                // 修复：直接使用 this，因为 AppCompatActivity 已实现 LifecycleOwner
                .addBannerLifecycleObserver(this)  //添加生命周期观察者
                .setIndicator( new CircleIndicator(this));  //设置圆形指示器

    }
    private void handlerAlbumsData(Recommend recommend)
    {
        //1.处理专辑数据
         albums = recommend.getResult().getAlbums();
         initAlbumsRecyclerAdapter();
    }

    /**
     * 初始化专辑列表适配器
     */
    private void initAlbumsRecyclerAdapter()
    {
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        albumsList.setLayoutManager(linearLayoutManager);
        albumsAdapter = new AlbumsRecyclerViewAdapter(this, albums);
        albumsList.setAdapter(albumsAdapter);
    }
}