package com.example.mqtt_demo;

import android.content.Intent;
import android.os.Bundle;
import android.view.View;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity_Login extends AppCompatActivity {

    private TextView tvContent;
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main_login);
//        getSupportActionBar().setTitle("首页");

        tvContent=findViewById(R.id.tv_content);
        Intent intent=getIntent();
        String account=intent.getStringExtra("account");
        tvContent.setText("欢迎你："+account);
    }

    //退出登录按钮点击事件
    public void loginOut(View view) {
        Intent intent=new Intent(this,LoginActivity.class);
        startActivity(intent);
        this.finish();
    }
}