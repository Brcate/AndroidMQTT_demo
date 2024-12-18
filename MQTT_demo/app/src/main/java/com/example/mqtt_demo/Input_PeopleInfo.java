package com.example.mqtt_demo;

import android.Manifest;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.location.Location;
import android.location.LocationManager;
import android.os.Build;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.mqtt_demo.db.MySQLiteOpenHelper;

import java.util.HashMap;
import java.util.Map;

public class Input_PeopleInfo extends AppCompatActivity {

    private Button btn_cancel;
    private Button btn_register;
    private EditText check_people;
    private EditText check_people_num;
    private MySQLiteOpenHelper mySQLiteOpenHelper;
    private SQLiteDatabase db;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input1);
        {
            btn_cancel = findViewById(R.id.btn_cancel);
            btn_register = findViewById(R.id.btn_register);
            check_people = findViewById(R.id.check_people);
            check_people_num = findViewById(R.id.check_people_num);

        }
        {

            Input_return();
            Input_register();
            initData();
        }

    }


    private void Input_return(){
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Input_PeopleInfo.this, MainActivity.class);
                startActivity(intent);
                finish(); // 销毁当前页面
            }
        });
    }


    private void Input_register(){
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                try {
//                    startLocation(1);
                    input_people_info();
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }

            }
        });
    }


    private void input_people_info(){
        String check_people1 = check_people.getText().toString();
        String check_people_num1 = check_people_num.getText().toString();
        Cursor cursor = db.rawQuery("SELECT * FROM people_info", null);
        ContentValues values = new ContentValues();
        values.put("check_people", check_people1);
        values.put("check_people_num", check_people_num1);
        if (cursor.getCount() > 0) {
            // 更新id=1的记录
            int rowsAffected = db.update("people_info", values, "id=?", new String[]{"1"});
            if (rowsAffected > 0) {
                Toast.makeText(Input_PeopleInfo.this, "Location data updated successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Input_PeopleInfo.this, "Failed to update location data", Toast.LENGTH_SHORT).show();
            }

            Toast.makeText(Input_PeopleInfo.this, "修改成功", Toast.LENGTH_SHORT).show();
        }
        else {
            long newRowId = db.insert("people_info", null, values);
//                                    publishmessageplus(mqtt_WIFI_SSID, combinedInfo);// 将位置信息发布到 MQTT 主题
            if (newRowId != -1) {
                Toast.makeText(Input_PeopleInfo.this, "Location data inserted successfully", Toast.LENGTH_SHORT).show();
            } else {
                Toast.makeText(Input_PeopleInfo.this, "Failed to insert location data", Toast.LENGTH_SHORT).show();
            }
            Toast.makeText(Input_PeopleInfo.this, "注册成功", Toast.LENGTH_SHORT).show();
        }


        // 关闭游标
        cursor.close();
        Intent intent = new Intent(Input_PeopleInfo.this, MainActivity.class);
        startActivity(intent);
        finish(); // 销毁当前页面
    }


    private void initData() {
        mySQLiteOpenHelper = new MySQLiteOpenHelper(this);
        db = mySQLiteOpenHelper.getWritableDatabase();
    }
}


