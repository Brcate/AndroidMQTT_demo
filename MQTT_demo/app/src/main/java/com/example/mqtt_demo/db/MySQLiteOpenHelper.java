package com.example.mqtt_demo.db;

import android.content.Context;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteDatabase;
import android.widget.Toast;

import androidx.annotation.Nullable;

import com.example.mqtt_demo.MainActivity;
import com.example.mqtt_demo.User1;

public class MySQLiteOpenHelper extends SQLiteOpenHelper {
    private static final long CLEANUP_INTERVAL = 30 * 24 * 60 * 60 * 1000; // 30天的毫秒数
    private static final String PREF_LAST_CLEANUP_TIME = "last_cleanup_time";

    public static String CREATE_USER = "create table user (" +
            "id integer primary key autoincrement," +
            "SSID varchar," +
            "SignalStrength varchar," +
            "strength varchar," +
            "MAC varchar," +
            "frequency varchar," +
            "Latitude varchar," +
            "Longitude varchar," +
            "Altitude varchar," +
            "in_time varchar)";

    public static String CREATE_LOCATION = "create table location (" +
            "MAC varchar primary key," +
            "SSID varchar," +
            "SignalStrength integer," +
            "strength integer," +
            "frequency integer," +
            "Latitude varchar," +
            "Longitude varchar," +
            "Altitude varchar," +
            "in_time integer)";

    public static String CREATE_CELLINFO = "create table cell_info (" +
            "MAC varchar primary key," +
            "MCC varchar," +
            "MNC varchar," +
            "tac_lac varchar," +
            "Cell_ID varchar," +
            "Signal_Strength varchar," +
            "Latitude varchar," +
            "Longitude varchar," +
            "Altitude varchar," +
            "in_time integer)";

    public static String CREATE_CELLINFO1 = "create table cell_info1 (" +
            "MCC varchar," +
            "MNC varchar," +
            "tac_lac varchar," +
            "Cell_ID varchar primary key," +
            "Signal_Strength varchar," +
            "Latitude varchar," +
            "Longitude varchar," +
            "Altitude varchar," +
            "in_time integer)";

    public static String CREATE_USER1 = "create table user1 (" +
            "id integer primary key autoincrement," +
            "SSID varchar," +
            "SignalStrength varchar," +
            "strength varchar," +
            "MAC varchar," +
            "frequency varchar," +
            "Latitude varchar," +
            "Longitude varchar," +
            "Altitude varchar," +
            "in_time varchar)";

//    public static String Input_user = "create table Input_user (" +
//            "id integer primary key autoincrement," +
//            "name integer," +//事主姓名
//            "phone integer," +//事主联系电话
//            "case_start_time integer," +//案发开始时间
//            "case_end_time integer," +//案发开始时间
//            "home_page integer," +  //物证数量
//            "memo integer," +  //简要案情
//            "case_type integer," +  //简要案情
//            "danwei integer," +  //受理单位
//            "check_info integer," +  //勘查情况
//            "check_time integer," +  //勘查时间
//            "check_people integer," +  //勘查人员
//            "zhiwen_YN integer," +  //是否提取指纹
//            "DNA_YN integer," +  //是否提取DNA
//            "DNA_info integer," +  //DNA提取信息
//            "foot_YN integer," +  //是否提取足迹
//            "video_YN integer," +  //有无视频
//            "Latitude integer," +//经度
//            "Longitude integer," +//纬度
//            "Altitude integer," +//高度
//            "in_time integer)";  //输入时间

    public static String Input_user = "create table Input_user (" +
            "id integer primary key autoincrement," +
            "name varchar," +//事主姓名
            "phone varchar," +//事主联系电话
            "start_time varchar," +//案发开始时间
            "end_time varchar," +//案发结束时间
            "check_people varchar," +  //勘查人员
            "check_people_num varchar," +//勘查人员警号
            "case_type varchar," +  //案件类型
            "Latitude varchar," +//经度
            "Longitude varchar," +//纬度
            "Altitude varchar," +//高度
            "Location_CN varchar," +//地址信息
            "in_time varchar)";  //输入时间

    public static String CREATE_PeopleInfo = "create table people_info (" +
            "id integer primary key autoincrement," +
            "check_people varchar," +  //勘查人员
            "check_people_num varchar)";  //号码

    public static String CREATE_MAX_id = "create table max_id (" +
            "id integer primary key autoincrement," +
            "id_name varchar," +  //
            "num integer)";  //
    Context mContext;
    public static String CREATE_Account_Password = "create table account_password (" +
            "id integer primary key autoincrement," +
            "account varchar," +
            "password varchar," +
            "unit varchar," +
            "identity varchar)";
//    public static final int DATABASE_VERSION = 9;
    public static final int DATABASE_VERSION = 12;

    public MySQLiteOpenHelper(Context  context) {
        super(context, "Android_demo", null, DATABASE_VERSION);
        this.mContext = context;
//        checkAndCleanTable();
    }

    @Override
    public void onCreate(SQLiteDatabase sqLiteDatabase) {
        sqLiteDatabase.execSQL(CREATE_USER);
        sqLiteDatabase.execSQL(CREATE_LOCATION);
        sqLiteDatabase.execSQL(CREATE_CELLINFO);
        sqLiteDatabase.execSQL(CREATE_CELLINFO1);
        sqLiteDatabase.execSQL(CREATE_USER1);
        sqLiteDatabase.execSQL(Input_user);
        sqLiteDatabase.execSQL(CREATE_PeopleInfo);
        sqLiteDatabase.execSQL(CREATE_MAX_id);
        sqLiteDatabase.execSQL(CREATE_Account_Password);
        Toast.makeText(mContext, "数据库首次创建成功！", Toast.LENGTH_SHORT).show();

    }

    @Override
    public void onUpgrade(SQLiteDatabase sqLiteDatabase, int oldVersion, int newVersion) {

        if (oldVersion < 2) {
            // 版本升级时创建 location 表
            sqLiteDatabase.execSQL(CREATE_LOCATION);
        }
        if (oldVersion < 3) {
            // 版本升级时创建 location 表
            sqLiteDatabase.execSQL("DELETE FROM user");
        }
        if (oldVersion < 4) {
            // 版本升级时创建 location 表
            sqLiteDatabase.execSQL(CREATE_CELLINFO);
        }
        if (oldVersion < 5) {
            // 版本升级时创建 location 表
            sqLiteDatabase.execSQL(CREATE_CELLINFO1);
        }
        if (oldVersion < 7) {
            // 版本升级时创建 location 表
            sqLiteDatabase.execSQL("DELETE FROM user");
        }
        if (oldVersion < 8) {
            // 版本升级时创建 location 表
            sqLiteDatabase.execSQL("DELETE FROM user");
        }
        if (oldVersion < 9) {
            // 版本升级时创建 location 表
            sqLiteDatabase.execSQL(CREATE_USER1);
        }
        if (oldVersion < 10) {
            // 版本升级时创建 location 表

        }
        if (oldVersion < 11) {
            // 版本升级时创建 location 表
            sqLiteDatabase.execSQL("DELETE FROM user");
            sqLiteDatabase.execSQL("DELETE FROM user1");
        }
        if (oldVersion < 12) {
            // 版本升级时创建 location 表
            sqLiteDatabase.execSQL(Input_user);
        }
    }
}

//    private void checkAndCleanTable() {
//        SharedPreferences sharedPreferences = mContext.getSharedPreferences("MyPrefs", Context.MODE_PRIVATE);
//        long lastCleanupTime = sharedPreferences.getLong(PREF_LAST_CLEANUP_TIME, 0);
//        long currentTime = System.currentTimeMillis();
//
//        if (currentTime - lastCleanupTime > CLEANUP_INTERVAL) {
//            SQLiteDatabase db = this.getWritableDatabase();
//            db.execSQL("DELETE FROM user"); // 执行清空表的操作
//
//            // 更新最后清空时间
//            sharedPreferences.edit().putLong(PREF_LAST_CLEANUP_TIME, currentTime).apply();
//        }
//    }
//}

