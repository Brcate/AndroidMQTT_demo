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

public class input_info extends AppCompatActivity {

    private SQLiteDatabase db;
    private MySQLiteOpenHelper mySQLiteOpenHelper;
    private TableLayout tableLayout;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_info);
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
//                int id = cursor.getInt(cursor.getColumnIndexOrThrow("id"));
                String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                String check_people = cursor.getString(cursor.getColumnIndexOrThrow("check_people"));
                String check_people_num = cursor.getString(cursor.getColumnIndexOrThrow("check_people_num"));
                String case_type = cursor.getString(cursor.getColumnIndexOrThrow("case_type"));
                String Location_CN = cursor.getString(cursor.getColumnIndexOrThrow("Location_CN"));
                double latitude = cursor.getDouble(cursor.getColumnIndexOrThrow("Latitude"));
                double longitude = cursor.getDouble(cursor.getColumnIndexOrThrow("Longitude"));
                double altitude = cursor.getDouble(cursor.getColumnIndexOrThrow("Altitude"));
//                TextView textViewId = new TextView(context);
//                textViewId.setText("ID: " + id);
//                row.addView(textViewId);

                TextView textViewname = new TextView(context);
                textViewname.setText("事主: " + name + " ");
                row.addView(textViewname);

                TextView textViewphone = new TextView(context);
                textViewphone.setText("手机尾号: " + phone + " ");
                row.addView(textViewphone);

                TextView textViewCheck_people = new TextView(context);
                textViewCheck_people.setText("勘察人: " + check_people + " ");
                row.addView(textViewCheck_people);

                TextView textViewSignalStrength = new TextView(context);
                textViewSignalStrength.setText("警号: " + check_people_num + " ");
                row.addView(textViewSignalStrength);

                TextView textViewInTime = new TextView(context);
                textViewInTime.setText("案件类型: " + case_type + " ");
                row.addView(textViewInTime);

                TextView textViewLocation = new TextView(context);
                textViewLocation.setText("地址: " + Location_CN + " ");
                row.addView(textViewLocation);

                TextView textViewLongitude = new TextView(context);
                textViewLongitude.setText("经度: " + longitude + " ");
                row.addView(textViewLongitude);

                TextView textViewLatitude = new TextView(context);
                textViewLatitude.setText("纬度: " + latitude + " ");
                row.addView(textViewLatitude);
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
