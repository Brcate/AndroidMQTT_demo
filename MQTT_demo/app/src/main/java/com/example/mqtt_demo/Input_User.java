package com.example.mqtt_demo;

import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.view.View;
import android.widget.TableLayout;
import android.widget.TableRow;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import com.example.mqtt_demo.db.MySQLiteOpenHelper;

public class Input_User extends AppCompatActivity {

    private SQLiteDatabase db;
    private MySQLiteOpenHelper mySQLiteOpenHelper;
    private TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.user1);
        tableLayout = findViewById(R.id.TableLayout_test);

        displayDataInTable(this, tableLayout);
    }

    public void displayDataInTable(Context context, TableLayout tableLayout) {
        // 初始化数据库对象
        mySQLiteOpenHelper = new MySQLiteOpenHelper(context);
        db = mySQLiteOpenHelper.getReadableDatabase();

        Cursor cursor = db.rawQuery("SELECT * FROM Input_user", null);
        if (cursor.moveToFirst()) {
            do {
                TableRow row = new TableRow(context);
                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                int gender = cursor.getInt(cursor.getColumnIndexOrThrow("gender"));
                int age = cursor.getInt(cursor.getColumnIndexOrThrow("age"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                int email = cursor.getInt(cursor.getColumnIndexOrThrow("email"));
                int home_page = cursor.getInt(cursor.getColumnIndexOrThrow("home_page"));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("Latitude"));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("Longitude"));
                double altitude = cursor.getDouble(cursor.getColumnIndexOrThrow("Altitude"));
                String in_time = cursor.getString(cursor.getColumnIndexOrThrow("in_time"));

//                TextView textViewId = new TextView(context);
//                textViewId.setText("ID: " + id);
//                row.addView(textViewId);

                TextView textViewSSID = new TextView(context);
                textViewSSID.setText("名字: " + name + " ");
                row.addView(textViewSSID);

                TextView textViewLongitude = new TextView(context);
                textViewLongitude.setText("经度: " + longitude + " ");
                row.addView(textViewLongitude);

                TextView textViewLatitude = new TextView(context);
                textViewLatitude.setText("纬度: " + latitude + " ");
                row.addView(textViewLatitude);

                TextView textViewSignalStrength = new TextView(context);
                textViewSignalStrength.setText("简要案情: " + home_page + " ");
                row.addView(textViewSignalStrength);

                TextView textViewInTime = new TextView(context);
                textViewInTime.setText("收录时间: " + in_time + " ");
                row.addView(textViewInTime);

                tableLayout.addView(row);

            } while (cursor.moveToNext());
        } else {
            // 如果查询结果为空，显示相应消息
            Toast.makeText(context, "No data found in user1 table", Toast.LENGTH_SHORT).show();
        }

        // 关闭游标
        cursor.close();
    }
}
