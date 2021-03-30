package com.littlestone.chatuipractice;

import androidx.appcompat.app.ActionBar;
import androidx.appcompat.app.AppCompatActivity;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Bundle;
import android.view.View;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.Toast;

import org.litepal.crud.DataSupport;
import org.litepal.tablemanager.Connector;

import java.util.ArrayList;
import java.util.List;

public class MainActivity extends AppCompatActivity implements View.OnClickListener {
    private EditText editText;
    private Button btn_send;
    private RecyclerView recyclerView;
    private ImageView img_add;
    private ImageView img_camera;
    private ImageView img_photo;
    private ImageView img_express;
    private MyAdapter adapter;
    private List<Message> data = new ArrayList<>();

    //*******************************监听网络状态*********************
    //网络变化
    private NetworkChangeReceiver networkChangeReceiver;
    private IntentFilter intentFilter;
    //网络状态
    private ConnectivityManager connectivityManager;
    private NetworkInfo networkInfo;
    //第一次安装APP
    private SharedPreferences sharedPreferences;
    private boolean isFirst;
    private SharedPreferences.Editor editor;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);
        ActionBar actionBar = getSupportActionBar();
        if (actionBar != null) {
            actionBar.hide();
        }

        sharedPreferences = getSharedPreferences("isFirstRun", MODE_PRIVATE);
        isFirst = sharedPreferences.getBoolean("isFirstRun", true);
        editor = sharedPreferences.edit();
        //判断是否是第一次安装APP
        if (isFirst) {
            //初始化数据库
            initChatDatabase();
            editor.putBoolean("isFirstRun", false);
            editor.apply();
        }
        bindView();
        initChatRecord();
        initNetworkListener();
        LinearLayoutManager linearLayoutManager = new LinearLayoutManager(this);
        recyclerView.setLayoutManager(linearLayoutManager);

        adapter = new MyAdapter(data, this);
        recyclerView.setAdapter(adapter);
        //滚动到聊天记录的最后一条
        recyclerView.scrollToPosition(data.size() - 1);
    }

    @Override
    public void onClick(View v) {
        int id = v.getId();
        switch (id) {
            case R.id.btn_send:
                String s = editText.getText().toString().trim();
                if (s.equals("") == false) {
                    Message message = new Message(s, Message.TYPE_SEND);
                    //保存到数据库中
                    message.save();
                    data.add(message);
                    //当有新消息时，刷新RecyclerView
//                    adapter.notifyItemInserted(data.size() - 1);
                    adapter.notifyDataSetChanged();
                    //将屏幕滚动到最新消息
                    recyclerView.scrollToPosition(data.size() - 1);
                    editText.setText("");
                } else {
                    Toast.makeText(MainActivity.this, "发送消息不能为空！", Toast.LENGTH_SHORT).show();
                }
                break;
        }
    }


    @Override
    protected void onDestroy() {
        super.onDestroy();
        unregisterReceiver(networkChangeReceiver);
    }

    /**
     * 初始化控件
     */
    public void bindView() {
        editText = findViewById(R.id.edit);
        btn_send = findViewById(R.id.btn_send);
        recyclerView = findViewById(R.id.rv);
        img_add = findViewById(R.id.img_add);
        img_camera = findViewById(R.id.img_camera);
        img_express = findViewById(R.id.img_express);
        img_photo = findViewById(R.id.img_photo);
        btn_send.setOnClickListener(this);

    }

    /**
     * 加载聊天记录
     */
    public void initChatRecord() {
        data = DataSupport.findAll(Message.class);
    }

    /**
     * 初始化聊天数据库
     */
    private void initChatDatabase() {
        Connector.getDatabase();
        Message meg1 = new Message("Hello, 我叫白文磊", Message.TYPE_RECEIVE);
        meg1.save();
        Message meg2 = new Message("Hello, 我叫郭涵博", Message.TYPE_SEND);
        meg2.save();

    }

    /**
     * 初始化监听网络变化的参数
     */
    public void initNetworkListener() {
        networkChangeReceiver = new NetworkChangeReceiver();
        intentFilter = new IntentFilter();
        intentFilter.addAction("android.net.conn.CONNECTIVITY_CHANGE");
        registerReceiver(networkChangeReceiver, intentFilter);
    }

    /**
     * 自定义个广播接收器，当等待的广播到来时，触发onReceive
     */
    class NetworkChangeReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            connectivityManager = (ConnectivityManager) getSystemService(Context.CONNECTIVITY_SERVICE);
            //注意添加监听权限
            networkInfo = connectivityManager.getActiveNetworkInfo();
            if (networkInfo != null && networkInfo.isAvailable()) {
//                Toast.makeText(MainActivity.this, "已联网", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(MainActivity.this, "未联网,检查网络！", Toast.LENGTH_SHORT).show();
            }
        }
    }
}