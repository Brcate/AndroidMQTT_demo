package com.example.mqtt_demo;


import static android.content.ContentValues.TAG;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;
import androidx.core.content.ContextCompat;

import android.annotation.SuppressLint;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.Cursor;
import android.location.Location;
import android.location.LocationManager;
import android.net.wifi.WifiInfo;
import android.os.Build;
import android.os.Bundle;
import android.os.Handler;
import android.os.Message;
import android.provider.Settings;
import android.telephony.CellIdentityGsm;
import android.telephony.CellIdentityLte;
import android.telephony.CellIdentityNr;
import android.telephony.CellInfo;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellInfoNr;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.CellSignalStrengthNr;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.View;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import org.eclipse.paho.client.mqttv3.IMqttDeliveryToken;
import org.eclipse.paho.client.mqttv3.MqttCallback;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.eclipse.paho.client.mqttv3.persist.MemoryPersistence;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;


import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;

import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.widget.ListView;

import android.Manifest;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;

import android.database.sqlite.SQLiteDatabase;

import com.example.mqtt_demo.db.MySQLiteOpenHelper;

public class MainActivity extends AppCompatActivity {//定义


    //***********************************************************************************************//

    private AMapLocationClient mLocationClient;
    private AMapLocationClientOption mLocationOption;
    private String strength = "";
    private String host = "ssl://qf69fee3.ala.cn-hangzhou.emqxsl.cn:8883";
    private String userName = "android";
    private String passWord = "android";
    //    private String mqtt_id = "513223951"; //定义成自己的QQ号  切记！不然会掉线！！！
    private String androidId;
    private String mqtt_id;
    private String mqtt_sub_topic = "513223951"; //为了保证你不受到别人的消息  哈哈
    private String mqtt_pub_topic = "513223951_ESP"; //为了保证你不受到别人的消息  哈哈  自己QQ好后面加 _PC
    private String mqtt_input_info = "513223951_input_info";
    private String mqtt_gps_topic = "513223951_GPS";
    private String mqtt_gps_topic1 = "513223951_GPS1";
    private String mqtt_gps_topic2 = "513223951_GPS2";
    private String mqtt_Cell_tac = "513223951_Celltac";
    private String mqtt_Cell_lac = "513223951_Celllac";
    private String mqtt_Cell_5G = "513223951_Celll5G";
    private String mqtt_WIFI_SSID = "513223951_SSID";
    private String mqtt_WIFI_SSID1 = "513223951_SSID1";
    private int led_flag = 1;
    private ImageView image_1;
    private ImageView image_2;
    private ImageView image_3;
    private ImageView input_1;
    private ImageView Cell_input;
    private TextView text_test;
    private ImageView WIFI_SSID;
    private ImageView Login_button;
    private ImageView Register_button;
    private MqttClient client;
    private MqttConnectOptions options;
    private Handler handler;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private ScheduledExecutorService scheduler;
    private Handler mHandler = new Handler();
    private int INTERVAL = 5000; // 以毫秒为单位
//    private int INTERVAL = 1000; // 以毫秒为单位
    //    private final int INTERVAL_car = 1000; // 以毫秒为单位
    private double lastLatitude = 0;
    private double lastLongitude = 0;
    private static final int THRESHOLD_DISTANCE = 30; // 设定的阈值距离，单位为米
    private Location firstLocation;
    private WifiManager wifiManager;
    private ListView listView;
//    private TextView title_app;
    private int startId; // 类级别的变量，用于存储起始值
    private int endId; // 类级别的变量，用于存储结束值
    private MySQLiteOpenHelper mySQLiteOpenHelper;
    private SQLiteDatabase db;
    private TextView tvContent;

    //***********************************************************************************************//


    @SuppressLint("HandlerLeak")
    @Override


    protected void onCreate(Bundle savedInstanceState) {
        //这里是界面打开后 最先运行的地方
        super.onCreate(savedInstanceState);
        //***********************************************************************************************//
        setContentView(R.layout.activity_main); // 对应界面UI
        //一般先用来进行界面初始化 控件初始化  初始化一些参数和变量。。。。。
        {
            image_3 = findViewById(R.id.image_3);
            image_2 = findViewById(R.id.image_2);
            image_1 = findViewById(R.id.image_1);
            text_test = findViewById(R.id.text_test);
            WIFI_SSID = findViewById(R.id.WIFI_SSID);
            input_1 = findViewById(R.id.input_1);
            Cell_input = findViewById(R.id.Cell_input);
//            title_app = findViewById(R.id.title_app);
//            Login_button = findViewById(R.id.Login);
//            Register_button = findViewById(R.id.Register);
            // 在合适的时机获取Android ID并赋值给mqtt_id
            androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            mqtt_id = androidId;
            tvContent=findViewById(R.id.tv_content);
        }//调用前端ID

        Intent intent=getIntent();
        String account = String.valueOf(intent.getStringExtra("account"));
        // 设置 TextView 内容
        // 调试输出
        Log.d("Account Debug", "Account: " + account);
        if (account != null) {
            tvContent.setText("欢迎你：" + account);
        } else {
            tvContent.setText("账号信息未获取");
        }
        //***********************************************************************************************//
        {
            setImage_3();
            SendAllInfo();
            Send_Input();
            Send_Wifi();
            Mqtt_init();
            Mqtt_connect();
            startReconnect();
            Telephone();
            Input_1();
            app_runserver();
            Views_Input();
//            Views_Login();
//            Views_Register();
//            Noshound_Music();
            // 启动服务
//            Intent serviceIntent = new Intent(this, MyBackgroundService.class);
//            startService(serviceIntent);
            // 开始定时执行获取位置信息的操作
            mHandler.postDelayed(mRunnable, INTERVAL);
            initData();
        }//函数调用
        //***********************************************************************************************//
        handler = new Handler() {
            @SuppressLint("SetTextI18n")
            public void handleMessage(Message msg) {
                super.handleMessage(msg);
                switch (msg.what) {
                    case 1: //开机校验更新回传
                        break;
                    case 2:  // 反馈回传

                        break;
                    case 3:  //MQTT 收到消息回传   UTF8Buffer msg=new UTF8Buffer(object.toString());
                        //处理message 传过来的 obj字段（里面包了数据）
//                        Toast.makeText(MainActivity.this, msg.obj.toString(), Toast.LENGTH_SHORT).show();
//                        text_test.setText(msg.obj.toString());
                        break;
                    case 30:  //连接失败
//                        Toast.makeText(MainActivity.this, "连接失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 31:   //连接成功
//                        Toast.makeText(MainActivity.this, "连接成功", Toast.LENGTH_SHORT).show();
                        if (client != null) {
                            try {
                                client.subscribe(mqtt_sub_topic, 1);//订阅我的mqtt_sub_topic号，QoS端口为:1
                            } catch (MqttException e) {
                                e.printStackTrace();
                            }
                        } else {
                            Log.e("Handler", "Client is null, cannot subscribe");
                        }
                        break;
                    default:
                        break;
                }
            }
        };

    }   //主函数

    public void loginOut(View view) {
        Intent intent=new Intent(this,LoginActivity.class);
        startActivity(intent);
        this.finish();
    }
    private void requestLocationPermission() {
        ActivityCompat.requestPermissions(this, new String[]{Manifest.permission.ACCESS_FINE_LOCATION, Manifest.permission.ACCESS_COARSE_LOCATION}, LOCATION_PERMISSION_REQUEST_CODE);
    }       //获取位置信息//


    private Runnable mRunnable = new Runnable() {
        @Override
        public void run() {
            // 获取您的当前位置信息
            if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED &&
                    ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                requestLocationPermission();
            } else {
                try {
                    startLocation(1);
                    startLocation(2);
                    startLocation(3);
                    startLocation(4);
                    startLocation(5);
                } catch (Exception e) {
                    throw new RuntimeException(e);
                }
            }
            mHandler.postDelayed(this, INTERVAL);
        }
    };



    @SuppressLint("Range")
    private int start_Max_Id(String sql_id) {
        Cursor cursorMaxId = db.rawQuery("SELECT num FROM max_id WHERE id_name=?", new String[]{sql_id});
//        int startId = 0;
        if (cursorMaxId.moveToFirst()) {
            startId = cursorMaxId.getInt(cursorMaxId.getColumnIndex("num"));
        }
        cursorMaxId.close();
        return startId;
    }
    @SuppressLint("Range")
    private int end_Max_Id(String sql_id, String columnName, String sql_name) {
        Cursor cursorMaxUserId = db.rawQuery("SELECT MAX(" + sql_id + ") AS max_id FROM " + sql_name, null);
//        int endId = 0;
        if (cursorMaxUserId.moveToFirst()) {
            endId = cursorMaxUserId.getInt(cursorMaxUserId.getColumnIndex(columnName));
        }
        cursorMaxUserId.close();
        return endId;
    }



    private void Mqtt_init() {
        try {
            // 加载CA证书文件
            InputStream caCertInputStream = getResources().openRawResource(R.raw.emqxslca);

            // 创建X.509证书工厂
            CertificateFactory certificateFactory = CertificateFactory.getInstance("X.509");
            X509Certificate caCert = (X509Certificate) certificateFactory.generateCertificate(caCertInputStream);

            // 创建密钥库，并将CA证书加载到密钥库中
            KeyStore keyStore = KeyStore.getInstance(KeyStore.getDefaultType());
            keyStore.load(null, null);
            keyStore.setCertificateEntry("caCert", caCert);

            // 创建信任管理器工厂，并初始化
            TrustManagerFactory trustManagerFactory = TrustManagerFactory.getInstance(TrustManagerFactory.getDefaultAlgorithm());
            trustManagerFactory.init(keyStore);

            // 创建SSL上下文
            SSLContext sslContext = SSLContext.getInstance("TLS");
            sslContext.init(null, trustManagerFactory.getTrustManagers(), null);
            //host为主机名，test为clientid即连接MQTT的客户端ID，一般以客户端唯一标识符表示，MemoryPersistence设置clientid的保存形式，默认为以内存保存
            client = new MqttClient(host, mqtt_id,
                    new MemoryPersistence());
            //MQTT的连接设置
            options = new MqttConnectOptions();
            //设置是否清空session,这里如果设置为false表示服务器会保留客户端的连接记录，这里设置为true表示每次连接到服务器都以新的身份连接
            options.setCleanSession(false);
            //设置连接的用户名
            options.setUserName(userName);
            //设置连接的密码
            options.setPassword(passWord.toCharArray());
            // 设置超时时间 单位为秒
            options.setConnectionTimeout(10);
            // 设置会话心跳时间 单位为秒 服务器会每隔1.5*20秒的时间向客户端发送个消息判断客户端是否在线，但这个方法并没有重连的机制
            options.setKeepAliveInterval(20);
            //设置回调
            client.setCallback(new MqttCallback() {
                @Override
                public void connectionLost(Throwable cause) {
                    //连接丢失后，一般在这里面进行重连
                    System.out.println("connectionLost----------");
                    //startReconnect();
                }

                @Override
                public void deliveryComplete(IMqttDeliveryToken token) {
                    //publish后会执行到这里
                    System.out.println("deliveryComplete---------"
                            + token.isComplete());
                }

                @Override
                public void messageArrived(String topicName, MqttMessage message)
                        throws Exception {
                    //subscribe后得到的消息会执行到这里面
                    System.out.println("messageArrived----------");
                    Message msg = new Message();
                    //封装message包
                    msg.what = 3;   //收到消息标志位
                    msg.obj = topicName + "---" + message.toString();
                    //发送messge到handler
                    handler.sendMessage(msg);    // hander 回传
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }    //Mqtt连接函数


    private void Mqtt_connect() {
        new Thread(new Runnable() {
            @Override
            public void run() {
                try {
                    if (client != null && !(client.isConnected()))  //如果client对象不为null且还未连接
                    {
                        client.connect(options);
                        Message msg = new Message();
                        msg.what = 31;
                        // 没有用到obj字段
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
//                    Message msg = new Message();
//                    msg.what = 30;
//                    // 没有用到obj字段
//                    handler.sendMessage(msg);
                }
            }
        }).start();
    }


    private void startReconnect() {
        scheduler = Executors.newSingleThreadScheduledExecutor();
        scheduler.scheduleAtFixedRate(new Runnable() {
            @Override
            public void run() {
                if (!client.isConnected()) {
                    Mqtt_connect();
                }
            }
        }, 0 * 1000, 10 * 1000, TimeUnit.MILLISECONDS);
    }    //Mqtt连接函数


    private void publishmessageplus(String topic, String message2) {
        if (client == null || !client.isConnected()) {
            return;
        }
        MqttMessage message = new MqttMessage();
        message.setPayload(message2.getBytes());
        try {
            client.publish(topic, message);
        } catch (MqttException e) {

            e.printStackTrace();
        }
    }    //发送信息到MQTT函数，"publishmessageplus(id,内容)"

    private void send_input(){

        startId = start_Max_Id("input_id");
        int start_trueID = startId + 1;
        endId = end_Max_Id("id","max_id","Input_user");
        Log.d("Max_id_input", "start_id: "+ String.valueOf(startId) + "end_id"+ String.valueOf(endId));
        // 获取当前时间戳
        long currentTimeStamp = System.currentTimeMillis();
        // 计算150000000毫秒之前的时间戳
        long timeThreshold = currentTimeStamp - 150000000;
//        long timeThreshold = currentTimeStamp - 864000000;
        // 执行查询
        String query = "SELECT * FROM Input_user WHERE in_time >= '" + timeThreshold + "'";
        if(startId != endId) {
            Cursor cursor = db.rawQuery(query, null);
//            Cursor cursor = db.rawQuery("SELECT * FROM Input_user WHERE id BETWEEN ? AND ?", new String[]{String.valueOf(start_trueID), String.valueOf(endId)});
//                Cursor cursor = db.rawQuery("SELECT * FROM Input_user", null);
            int batchSize = 100;
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int count = 0;
                    JSONArray dataBatch = new JSONArray();
                    do {
                        String name = cursor.getString(cursor.getColumnIndexOrThrow("name"));
                        String phone = cursor.getString(cursor.getColumnIndexOrThrow("phone"));
                        String start_time = cursor.getString(cursor.getColumnIndexOrThrow("start_time"));
                        String end_time = cursor.getString(cursor.getColumnIndexOrThrow("end_time"));
                        String check_people = cursor.getString(cursor.getColumnIndexOrThrow("check_people"));
                        String check_people_num = cursor.getString(cursor.getColumnIndexOrThrow("check_people_num"));
                        String case_type = cursor.getString(cursor.getColumnIndexOrThrow("case_type"));
                        String latitude = cursor.getString(cursor.getColumnIndexOrThrow("Latitude"));
                        String longitude = cursor.getString(cursor.getColumnIndexOrThrow("Longitude"));
                        String altitude = cursor.getString(cursor.getColumnIndexOrThrow("Altitude"));
                        String Location_CN = cursor.getString(cursor.getColumnIndexOrThrow("Location_CN"));
                        String in_time = cursor.getString(cursor.getColumnIndexOrThrow("in_time"));
                        // 处理数据
                        String input_info = "{\"name\": \"" + name + "\", \"start_time\": \"" + start_time + "\", \"end_time\": \"" + end_time + "\", \"check_people\": \"" + check_people + "\", \"phone\": \"" + phone + "\", \"check_people_num\": \"" + check_people_num + "\", \"case_type\": \"" + case_type + "\", \"in_time\": \"" + in_time + "\",\"Location_CN\": \"" + Location_CN + "\"}";
                        Toast.makeText(MainActivity.this, input_info, Toast.LENGTH_SHORT).show();
//                                publishmessageplus(mqtt_input_info, input_info);
                        String locationInfo = "{\"mqtt_id\": \"" + mqtt_id + "\", \"Latitude\": " + latitude + ", \"Longitude\": " + longitude + ", \"Altitude\": " + altitude + "}";
//                                Toast.makeText(Input.this, locationInfo, Toast.LENGTH_SHORT).show();
                        // 将位置信息发布到 MQTT 主题
                        String combinedInfo = "[" + locationInfo + "," + input_info + "]";
                        publishmessageplus(mqtt_input_info, combinedInfo);// 将位置信息发布到 MQTT 主题
                        Log.d("Max_id_input", "start_id: " + String.valueOf(locationInfo) + "end_id" + String.valueOf(combinedInfo));

//                            JSONObject locationObject = null;
//                            try {
//                                locationObject = new JSONObject(locationInfo);
//                            } catch (JSONException e) {
//                                throw new RuntimeException(e);
//                            }
//                            JSONObject wifiObject = null;
//                            try {
//                                wifiObject = new JSONObject(input_info);
//                            } catch (JSONException e) {
//                                throw new RuntimeException(e);
//                            }
//                            dataBatch.put(locationObject);
//                            dataBatch.put(wifiObject);
//                            count++;
                    } while (cursor.moveToNext() && count < batchSize);
//                        String batchData = dataBatch.toString();
//                        publishmessageplus(mqtt_input_info, batchData); // 将位置信息发布到 MQTT 主题
//                        dataBatch = new JSONArray(); // 重置数据批次
                } while (!cursor.isAfterLast());
            }
            if (cursor != null) {
                cursor.close();
            }
        }

}
    private void send_wifi(){
        startId = start_Max_Id("wifi_id");
        int start_trueID = startId + 1;
        endId = end_Max_Id("id","max_id","user");
        Log.d("Max_id_wifi", "start_id: "+ String.valueOf(startId) + "end_id"+ String.valueOf(endId));
        // 获取当前时间戳
        long currentTimeStamp = System.currentTimeMillis();
        // 计算150000000毫秒之前的时间戳
        long timeThreshold = currentTimeStamp - 80000000;
//        long timeThreshold = currentTimeStamp - 864000000;
        // 执行查询
        String query = "SELECT * FROM user WHERE in_time >= '" + timeThreshold + "'";
        if(startId != endId) {
            Cursor cursor = db.rawQuery(query, null);
//            Cursor cursor = db.rawQuery("SELECT * FROM user WHERE id BETWEEN ? AND ?", new String[]{String.valueOf(start_trueID), String.valueOf(endId)});
//                Cursor cursor = db.rawQuery("SELECT * FROM user", null);
            int batchSize = 100;
            if (cursor != null && cursor.moveToFirst()) {
                do {
                    int count = 0;
                    JSONArray dataBatch = new JSONArray();
                    do {
                        String ssid = cursor.getString(cursor.getColumnIndexOrThrow("SSID"));
                        String signalStrength = cursor.getString(cursor.getColumnIndexOrThrow("SignalStrength"));
                        String MAC = cursor.getString(cursor.getColumnIndexOrThrow("MAC"));
                        String frequency = cursor.getString(cursor.getColumnIndexOrThrow("frequency"));
                        String latitude = cursor.getString(cursor.getColumnIndexOrThrow("Latitude"));
                        String longitude = cursor.getString(cursor.getColumnIndexOrThrow("Longitude"));
                        String altitude = cursor.getString(cursor.getColumnIndexOrThrow("Altitude"));
                        String in_time = cursor.getString(cursor.getColumnIndexOrThrow("in_time"));
                        // 处理数据
                        String locationInfo = "{\"mqtt_id\": \"" + mqtt_id + "\", \"Latitude\": " + latitude + ", \"Longitude\": " + longitude + ", \"Altitude\": " + String.valueOf(altitude) + "}";
                        String wifi_Info = "{\"mqtt_id\": \"" + mqtt_id + "\",\"SSID\": \"" + ssid + "\",\"SignalStrength\": " + String.valueOf(signalStrength) + ",\"MAC\": \"" + MAC + "\",\"frequency\":\"" + frequency + "\",\"in_time\":\"" + in_time + "\"}";
                        String combinedInfo = "[" + locationInfo + "," + wifi_Info + "]";
//                            publishmessageplus(mqtt_WIFI_SSID, combinedInfo);// 将位置信息发布到 MQTT 主题
                        JSONObject locationObject = null;
                        try {
                            locationObject = new JSONObject(locationInfo);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        JSONObject wifiObject = null;
                        try {
                            wifiObject = new JSONObject(wifi_Info);
                        } catch (JSONException e) {
                            throw new RuntimeException(e);
                        }
                        dataBatch.put(locationObject);
                        dataBatch.put(wifiObject);
                        count++;
                    } while (cursor.moveToNext() && count < batchSize);
                    String batchData = dataBatch.toString();
                    publishmessageplus(mqtt_WIFI_SSID, batchData); // 将位置信息发布到 MQTT 主题

                    dataBatch = new JSONArray(); // 重置数据批次
                } while (!cursor.isAfterLast());
                Toast.makeText(MainActivity.this, "发送成功", Toast.LENGTH_SHORT).show();
            }
            if (cursor != null) {
                cursor.close();
            }
        }
    }
    private void send_wifi_small(){
        Cursor cursor = db.rawQuery("SELECT * FROM user1", null);
        int batchSize = 100;
        if (cursor != null && cursor.moveToFirst()) {
            do{
                int count = 0;
                JSONArray dataBatch = new JSONArray();
                do {
                    String ssid = cursor.getString(cursor.getColumnIndexOrThrow("SSID"));
                    String signalStrength = cursor.getString(cursor.getColumnIndexOrThrow("SignalStrength"));
                    String MAC = cursor.getString(cursor.getColumnIndexOrThrow("MAC"));
                    String frequency = cursor.getString(cursor.getColumnIndexOrThrow("frequency"));
                    String latitude = cursor.getString(cursor.getColumnIndexOrThrow("Latitude"));
                    String longitude = cursor.getString(cursor.getColumnIndexOrThrow("Longitude"));
                    String altitude = cursor.getString(cursor.getColumnIndexOrThrow("Altitude"));
                    String in_time = cursor.getString(cursor.getColumnIndexOrThrow("in_time"));
                    // 处理数据
                    String locationInfo = "{\"mqtt_id\": \"" + mqtt_id + "\", \"Latitude\": " + latitude + ", \"Longitude\": " + longitude + ", \"Altitude\": " + String.valueOf(altitude) + "}";
                    String wifi_Info = "{\"mqtt_id\": \"" + mqtt_id + "\",\"SSID\": \"" + ssid + "\",\"SignalStrength\": " + String.valueOf(signalStrength) + ",\"MAC\": \"" + MAC + "\",\"frequency\":\"" + frequency + "\",\"in_time\":\"" + in_time + "\"}";
                    String combinedInfo = "[" + locationInfo + "," + wifi_Info + "]";
//                                                    publishmessageplus(mqtt_WIFI_SSID, combinedInfo);// 将位置信息发布到 MQTT 主题
                    JSONObject locationObject = null;
                    try {
                        locationObject = new JSONObject(locationInfo);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    JSONObject wifiObject = null;
                    try {
                        wifiObject = new JSONObject(wifi_Info);
                    } catch (JSONException e) {
                        throw new RuntimeException(e);
                    }
                    dataBatch.put(locationObject);
                    dataBatch.put(wifiObject);
                    count++;
                } while (cursor.moveToNext() && count < batchSize);
                String batchData = dataBatch.toString();
                publishmessageplus(mqtt_WIFI_SSID1, batchData); // 将位置信息发布到 MQTT 主题
//                        Setwifi_id();
                dataBatch = new JSONArray(); // 重置数据批次
            } while (!cursor.isAfterLast());
        }
        if (cursor != null) {
            cursor.close();
        }
    }
    private void Send_Input() {
        WIFI_SSID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                send_input();
//                WIFI_SSID.setOnClickListener(null); // 确保在设置监听器之前先移除之前的监听器
//                WIFI_SSID.setOnClickListener(this); // 重新添加点击事件监听器
//                Setinput_id();
            }
        });
    }       //发送案件信息
    private void Send_Wifi() {
        image_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Cursor cursor = db.rawQuery("SELECT * FROM people_info", null);
                if (cursor.getCount() > 0) {

                    Intent intent = new Intent(MainActivity.this, Input_PeopleInfo.class);
                    startActivity(intent);
                    finish(); // 销毁当前页面

                }
                else {
                    Intent intent = new Intent(MainActivity.this, Input_PeopleInfo.class);
                    startActivity(intent);
                    ContentValues values = new ContentValues();
                    values.put("id_name", "input_id");
                    values.put("num", 0);
                    db.insert("max_id", null, values);
                    values.put("id_name", "wifi_id");
                    values.put("num", 0);
                    db.insert("max_id", null, values);
                    finish(); // 销毁当前页面
                }
                cursor.close();
            }
        });
    }//发送wifi信息
    private void SendAllInfo() {

        image_2.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                send_wifi();
                send_input();
                image_2.setOnClickListener(null); // 确保在设置监听器之前先移除之前的监听器
                image_2.setOnClickListener(this); // 重新添加点击事件监听器
//                Setinput_id();
//                Setwifi_id();
            }
        });
    } //send Location to MQTT 单击
    private void Views_Input() {
        WIFI_SSID.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, input_info.class);
                startActivity(intent);
            }
        });
    }       //发送案件信息
//    private void Views_Login() {
//        Login_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, LoginActivity.class);
//                startActivity(intent);
//            }
//        });
//    }       //发送案件信息
//    private void Views_Register() {
//        Register_button.setOnClickListener(new View.OnClickListener() {
//            @Override
//            public void onClick(View v) {
//                Intent intent = new Intent(MainActivity.this, RegisterActivity.class);
//                startActivity(intent);
//            }
//        });
//    }       //发送案件信息
    private void Telephone(){
        Cell_input.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, User.class);
                startActivity(intent);
            }
        });
    }


    private void Input_1(){
        input_1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
//                Cursor cursor1 = db.rawQuery("SELECT * FROM max_id", null);

                Cursor cursor = db.rawQuery("SELECT * FROM people_info", null);
                if (cursor.getCount() > 0) {

                    Intent intent = new Intent(MainActivity.this, Input.class);
                    startActivity(intent);

                }
                else {
                    Intent intent = new Intent(MainActivity.this, Input_PeopleInfo.class);
                    startActivity(intent);
                    ContentValues values = new ContentValues();
                    values.put("id_name", "input_id");
                    values.put("num", 0);
                    db.insert("max_id", null, values);
                    values.put("id_name", "wifi_id");
                    values.put("num", 0);
                    db.insert("max_id", null, values);
                }
                cursor.close();
            }
        });
    }


    private void setImage_3(){
        image_3.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(MainActivity.this, User1.class);
                startActivity(intent);
            }
        });
    }


    public double calculateDistance(double lat1, double lon1, double lat2, double lon2) {
        int R = 6371; // 地球半径，单位：公里

        double dLat = Math.toRadians(lat2 - lat1);
        double dLon = Math.toRadians(lon2 - lon1);

        double a = Math.sin(dLat / 2) * Math.sin(dLat / 2) +
                Math.cos(Math.toRadians(lat1)) * Math.cos(Math.toRadians(lat2)) *
                        Math.sin(dLon / 2) * Math.sin(dLon / 2);

        double c = 2 * Math.atan2(Math.sqrt(a), Math.sqrt(1 - a));

        double distance = R * c; // 计算距离，单位：公里

        return distance;
    }


    public AMapLocation startLocation(final int locationType) throws Exception {

        AMapLocationClient.setApiKey("38f21c3ed4eaa1cef2ad103292fb7741"); // 设置您的API Key
        AMapLocationClient.updatePrivacyShow(getApplicationContext(), true, true);
        AMapLocationClient.updatePrivacyAgree(getApplicationContext(), true);
        AMapLocationClient mLocationClient = new AMapLocationClient(getApplicationContext());
        final AMapLocationListener mLocationListener = new AMapLocationListener() {
            @SuppressLint("Range")
            @RequiresApi(api = Build.VERSION_CODES.Q)
            @Override
            public void onLocationChanged(AMapLocation amapLocation) {
                if (amapLocation !=null ) {
                    if (amapLocation.getErrorCode() == 0) {

                             if(locationType == 1 ){
                            //定位成功回调信息，设置相关消息
//                    Log.i(TAG,"当前定位结果来源-----"+amapLocation.getLocationType());//获取当前定位结果来源，如网络定位结果，详见定位类型表
//                    Log.i(TAG,"纬度 ----------------"+amapLocation.getLatitude());//获取纬度
//                    Log.i(TAG,"经度-----------------"+amapLocation.getLongitude());//获取经度
//                    Log.i(TAG,"精度信息-------------"+amapLocation.getAccuracy());//获取精度信息
//                    Log.i(TAG,"地址-----------------"+amapLocation.getAddress());//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
//                    Log.i(TAG,"国家信息-------------"+amapLocation.getCountry());//国家信息
//                    Log.i(TAG,"省信息---------------"+amapLocation.getProvince());//省信息
//                    Log.i(TAG,"城市信息-------------"+amapLocation.getCity());//城市信息
//                    Log.i(TAG,"城区信息-------------"+amapLocation.getDistrict());//城区信息
//                    Log.i(TAG,"街道信息-------------"+amapLocation.getStreet());//街道信息
//                    Log.i(TAG,"街道门牌号信息-------"+amapLocation.getStreetNum());//街道门牌号信息
//                    Log.i(TAG,"城市编码-------------"+amapLocation.getCityCode());//城市编码
//                    Log.i(TAG,"地区编码-------------"+amapLocation.getAdCode());//地区编码
//                    Log.i(TAG,"当前定位点的信息-----"+amapLocation.getAoiName());//获取当前定位点的AOI信息
//                    Log.i(TAG,"当前定位点的信息-----"+amapLocation.getAltitude());//获取当前定位点的AOI信息
                             if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                     // TODO: Consider calling
                                     //    ActivityCompat#requestPermissions
                                     // here to request the missing permissions, and then overriding
                                     //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                     //                                          int[] grantResults)
                                     // to handle the case where the user grants the permission. See the documentation
                                     // for ActivityCompat#requestPermissions for more details.
                                     return;
                                 }
                             LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                             Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                                 if (location != null) {
                                     double altitude = location.getAltitude(); // 获取海拔高度
//                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                     String locationInfo = "{\"mqtt_id\": \"" + mqtt_id + "\", \"Latitude\": " + amapLocation.getLatitude() + ", \"Longitude\": " + amapLocation.getLongitude() + ", \"Altitude\": " + altitude + "}";
//                                     Toast.makeText(MainActivity.this, locationInfo, Toast.LENGTH_SHORT).show();
                                     // 将位置信息发布到 MQTT 主题
//                                     publishmessageplus(mqtt_gps_topic, locationInfo);
                                 }
                        }

                        else if(locationType == 2 ) {
                                 WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                 WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                 if (wifiManager == null) {
                                     Log.e(TAG, "WifiManager is null");
                                     Toast.makeText(MainActivity.this, "出错了", Toast.LENGTH_SHORT).show();
                                     return;
                                 }
                                 if (!wifiManager.isWifiEnabled()) {
                                     wifiManager.setWifiEnabled(true);
                                 }
                                 if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                     // TODO: Consider calling
                                     //    ActivityCompat#requestPermissions
                                     // here to request the missing permissions, and then overriding
                                     //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                     //                                          int[] grantResults)
                                     // to handle the case where the user grants the permission. See the documentation
                                     // for ActivityCompat#requestPermissions for more details.
                                     return;
                                 }
                                 LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                 Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                 double latitude = amapLocation.getLatitude(); // 获取纬度
                                 double longitude = amapLocation.getLongitude(); // 获取经度
                                 if (location != null) {
                                     double altitude = location.getAltitude(); // 获取海拔高度
                                     String locationInfo = "{\"mqtt_id\": \"" + mqtt_id + "\", \"Latitude\": " + latitude + ", \"Longitude\": " + longitude + ", \"Altitude\": " + altitude + "}";
                                     List<ScanResult> results = wifiManager.getScanResults();
                                     String[] wifiList = new String[results.size()];
                                     for (int i = 0; i < results.size(); i++) {
                                         wifiList[i] = results.get(i).SSID;
                                         ScanResult scanResult = results.get(i);
                                         String ssid = scanResult.SSID;
                                         int signalStrength = scanResult.level; // 获取WiFi信号强度
                                         int frequency = wifiInfo.getFrequency(); // 获取WiFi信号的频率，单位为MHz
                                         String macAddress = scanResult.BSSID; // 获取WiFi网络的MAC地址
                                         @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
                                        //获取当前时间
                                         Date date = new Date(System.currentTimeMillis());
                                         String formattedDate = simpleDateFormat.format(date); // 将日期转换为字符串
                                         long currentTimeMillis = System.currentTimeMillis();
                                         String wifi_Info = "{\"mqtt_id\": \"" + mqtt_id + "\",\"SSID\": \"" + ssid + "\",\"SignalStrength\": " + signalStrength + ",\"MAC\": \"" + macAddress + "\",\"frequency\":\"" + frequency + "\"}";
                                         String combinedInfo = "[" + locationInfo + "," + wifi_Info + "]";

                                         Cursor cursor = db.rawQuery("SELECT * FROM user WHERE MAC = ?", new String[]{macAddress});
                                         ContentValues values = new ContentValues();
                                         values.put("SSID", ssid);
                                         values.put("SignalStrength", signalStrength);
                                         values.put("MAC", macAddress);
                                         values.put("frequency", frequency);
                                         values.put("Latitude", latitude);
                                         values.put("Longitude", longitude);
                                         values.put("Altitude", altitude);
                                         values.put("in_time", currentTimeMillis);
                                         if (cursor.getCount() > 0) {

                                         }
                                         else {
                                             if (signalStrength >= -70) {

                                                 // 如果不存在相同的MAC地址，则插入新记录

                                                 long newRowId = db.insert("user", null, values);
//                                                 publishmessageplus(mqtt_WIFI_SSID, combinedInfo);// 将位置信息发布到 MQTT 主题
                                                 if (newRowId != -1) {
                                                     Toast.makeText(MainActivity.this, "位置数据插入成功", Toast.LENGTH_SHORT).show();
                                                 }
                                                 else {
                                                     Toast.makeText(MainActivity.this, "插入位置数据失败", Toast.LENGTH_SHORT).show();
                                                 }
                                             }
                                         }
                                            // 关闭游标
                                         cursor.close();
                                     }
                                 }
                        }

                        else if(locationType == 3 ){
                                 TelephonyManager telephonyManager = (TelephonyManager) getSystemService(Context.TELEPHONY_SERVICE);
                                 if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_COARSE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                     // 请求获取定位权限
                                     ActivityCompat.requestPermissions(MainActivity.this, new String[]{Manifest.permission.ACCESS_COARSE_LOCATION}, 1);
                                 }
                                 if (ContextCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                     ActivityCompat.requestPermissions(MainActivity.this, new String[] { Manifest.permission.ACCESS_FINE_LOCATION }, 1);
                                 }

                                 CellInfo cellInfo;
                                 if (telephonyManager == null) {
                                     Log.e(TAG, "telephonyManager is null");
                                     Toast.makeText(MainActivity.this, "出错了", Toast.LENGTH_SHORT).show();
                                     return;
                                 } else {
                                     cellInfo = telephonyManager.getAllCellInfo().get(0);
//                    Toast.makeText(MainActivity.this, "不是0", Toast.LENGTH_SHORT).show();
                                 }
                                 if (telephonyManager != null) {
                                     List<CellInfo> cellInfoList = telephonyManager.getAllCellInfo();
                                     for (CellInfo info : cellInfoList) {
                                         if (info instanceof CellInfoNr) {
                                             cellInfo = info;
                                             break;
                                         }
                                     }
                                 }
                                 LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                 Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                 if (location != null) {
                                     double altitude = location.getAltitude(); // 获取海拔高度
                                     double latitude = amapLocation.getLatitude();
                                     double longitude = amapLocation.getLongitude();
                                     SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
                                     //获取当前时间
                                     Date date = new Date(System.currentTimeMillis());
                                     String formattedDate = simpleDateFormat.format(date); // 将日期转换为字符串
                                     long currentTimeMillis = System.currentTimeMillis();
                                     String locationInfo = "{\"mqtt_id\": \"" + mqtt_id + "\", \"Latitude\": " + latitude + ", \"Longitude\": " + longitude + ", \"Altitude\": " + altitude + "}";
                                 if (cellInfo instanceof CellInfoGsm) {
                                         CellSignalStrengthGsm cellSignalStrengthGsm = ((CellInfoGsm) cellInfo).getCellSignalStrength();
                                         int signalStrength = cellSignalStrengthGsm.getDbm(); // 获取信号强度
                                         CellIdentityGsm cellIdentityGsm = ((CellInfoGsm) cellInfo).getCellIdentity();

                                         int MCC = cellIdentityGsm.getMcc();
                                         int MNC = cellIdentityGsm.getMnc();
                                         int cellId = cellIdentityGsm.getCid(); // 获取基站ID
                                         int lac = cellIdentityGsm.getLac(); // 获取位置区域码
                                         String payload = "{\"mqtt_id\": \"" + mqtt_id + "\", \"MCC\": " + MCC + ", \"MNC\": " + MNC + ", \"lac\": " + lac + ", \"Cell_ID\": " + cellId + ", \"Signal_Strength\": " + signalStrength + "}";
//                    Toast.makeText(MainActivity.this, "不是0", Toast.LENGTH_SHORT).show();
                                         String combinedInfo = "[" + locationInfo + "," + payload + "]";
                                         // 可以继续获取其它信息
                                     Cursor cursor = db.rawQuery("SELECT * FROM cell_info1 WHERE Cell_ID = ?", new String[]{String.valueOf(cellId)});
                                     if (cursor.getCount() > 0) {
                                     }
                                     else {
                                         // 如果不存在相同的MAC地址，则插入新记录
                                         ContentValues values = new ContentValues();
                                         values.put("MCC", MCC);
                                         values.put("MNC", MNC);
                                         values.put("tac_lac", lac);
                                         values.put("Cell_ID", cellId);
                                         values.put("Signal_Strength", latitude);
                                         values.put("Latitude", latitude);
                                         values.put("Longitude", longitude);
                                         values.put("Altitude", altitude);
                                         values.put("in_time", currentTimeMillis);

                                         long newRowId = db.insert("cell_info1", null, values);
                                         publishmessageplus(mqtt_Cell_lac, combinedInfo);// 将位置信息发布到 MQTT 主题
                                         if (newRowId != -1) {
                                             Toast.makeText(MainActivity.this, "位置数据插入成功", Toast.LENGTH_SHORT).show();
                                         } else {
                                             Toast.makeText(MainActivity.this, "插入位置数据失败", Toast.LENGTH_SHORT).show();
                                         }
                                     }
                                     // 关闭游标
                                     cursor.close();
                                     } else if (cellInfo instanceof CellInfoLte) {
                                         CellSignalStrengthLte cellSignalStrengthLte = ((CellInfoLte) cellInfo).getCellSignalStrength();
                                         int signalStrength = cellSignalStrengthLte.getDbm(); // 获取信号强度
                                         CellIdentityLte cellIdentityLte = ((CellInfoLte) cellInfo).getCellIdentity();
                                         int MCC = cellIdentityLte.getMcc();
                                         int MNC = cellIdentityLte.getMnc();
                                         int cellId = cellIdentityLte.getCi(); // 获取基站ID
                                         int tac = cellIdentityLte.getTac(); // 获取跟踪区域码
                                         String payload = "{\"mqtt_id\": \"" + mqtt_id + "\", \"MCC\": " + MCC + ", \"MNC\": " + MNC + ", \"tac\": " + tac + ", \"Cell_ID\": " + cellId + ", \"Signal_Strength\": " + signalStrength + "}";
//                    Toast.makeText(MainActivity.this, payload, Toast.LENGTH_SHORT).show();
                                         String combinedInfo = "[" + locationInfo + "," + payload + "]";
                                         // 可以继续获取其它信息
                                     Cursor cursor = db.rawQuery("SELECT * FROM cell_info1 WHERE Cell_ID = ?", new String[]{String.valueOf(cellId)});
                                     if (cursor.getCount() > 0) {
                                     }
                                     else {
                                         // 如果不存在相同的MAC地址，则插入新记录
                                         ContentValues values = new ContentValues();
                                         values.put("MCC", MCC);
                                         values.put("MNC", MNC);
                                         values.put("tac_lac", tac);
                                         values.put("Cell_ID", cellId);
                                         values.put("Signal_Strength", latitude);
                                         values.put("Latitude", latitude);
                                         values.put("Longitude", longitude);
                                         values.put("Altitude", altitude);
                                         values.put("in_time", currentTimeMillis);

                                         long newRowId = db.insert("cell_info1", null, values);
                                         publishmessageplus(mqtt_Cell_tac, combinedInfo);// 将位置信息发布到 MQTT 主题
                                         if (newRowId != -1) {
                                             Toast.makeText(MainActivity.this, "位置数据插入成功", Toast.LENGTH_SHORT).show();
                                         } else {
                                             Toast.makeText(MainActivity.this, "插入位置数据失败", Toast.LENGTH_SHORT).show();
                                         }
                                     }
                                     // 关闭游标
                                     cursor.close();
                                     } else if (cellInfo instanceof CellInfoNr) {
                                         CellSignalStrengthNr cellSignalStrengthNr = (CellSignalStrengthNr) ((CellInfoNr) cellInfo).getCellSignalStrength();
                                         int signalStrength = cellSignalStrengthNr.getDbm(); // 获取信号强度
                                         CellIdentityNr cellIdentityNr = (CellIdentityNr) ((CellInfoNr) cellInfo).getCellIdentity();
                                         int MCC = Integer.parseInt(cellIdentityNr.getMccString());
                                         int MNC = Integer.parseInt(cellIdentityNr.getMncString());
                                         int cellId = (int) cellIdentityNr.getNci(); // 获取基站ID
                                         int tac = cellIdentityNr.getTac(); // 获取跟踪区域码
                                         String payload = "{\"mqtt_id\": \"" + mqtt_id + "\", \"MCC\": " + MCC + ", \"MNC\": " + MNC + ", \"tac\": " + tac + ", \"Cell_ID\": " + cellId + ", \"Signal_Strength\": " + signalStrength + "}";
                                         String combinedInfo = "[" + locationInfo + "," + payload + "]";
                                     Cursor cursor = db.rawQuery("SELECT * FROM cell_info1 WHERE Cell_ID = ?", new String[]{String.valueOf(cellId)});
                                     if (cursor.getCount() > 0) {
                                     }
                                     else {
                                         // 如果不存在相同的MAC地址，则插入新记录
                                         ContentValues values = new ContentValues();
                                         values.put("MCC", MCC);
                                         values.put("MNC", MNC);
                                         values.put("tac_lac", tac);
                                         values.put("Cell_ID", cellId);
                                         values.put("Signal_Strength", latitude);
                                         values.put("Latitude", latitude);
                                         values.put("Longitude", longitude);
                                         values.put("Altitude", altitude);
                                         values.put("in_time", currentTimeMillis);

                                         long newRowId = db.insert("cell_info1", null, values);
                                         publishmessageplus(mqtt_Cell_5G, combinedInfo);// 将位置信息发布到 MQTT 主题
                                         if (newRowId != -1) {
                                             Toast.makeText(MainActivity.this, "位置数据插入成功", Toast.LENGTH_SHORT).show();
                                         } else {
                                             Toast.makeText(MainActivity.this, "插入位置数据失败", Toast.LENGTH_SHORT).show();
                                         }
                                     }
                                     // 关闭游标
                                     cursor.close();
                                     }
                                 }
                        }

                        else if(locationType == 4 ){
                             // 获取您的当前位置信息
                                 if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                     // TODO: Consider calling
                                     //    ActivityCompat#requestPermissions
                                     // here to request the missing permissions, and then overriding
                                     //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                     //                                          int[] grantResults)
                                     // to handle the case where the user grants the permission. See the documentation
                                     // for ActivityCompat#requestPermissions for more details.
                                     return;
                                 }
                                 double latitude = amapLocation.getLatitude();
                                 double longitude = amapLocation.getLongitude();
                                 Location nowlocation = new Location("nowlocation");
                                 nowlocation.setLatitude(latitude);
                                 nowlocation.setLongitude(longitude);
                                 if (lastLatitude != 0 && lastLongitude != 0) {
                                     Location lastLocation = new Location("lastLocation");
                                     lastLocation.setLatitude(lastLatitude);
                                     lastLocation.setLongitude(lastLongitude);
//                                     float distance = (float) (latitude * latitude + longitude * longitude + lastLatitude * lastLatitude + lastLongitude * lastLongitude - 2 * (latitude * lastLatitude + longitude * lastLongitude));
//                                     float distance = amapLocation.distanceTo(lastLocation);
                                     double distance = calculateDistance(latitude,longitude,lastLatitude,lastLongitude);
                                     double distanceInM = distance * 1000; // 将距离转换为公里
//                                     @SuppressLint("DefaultLocale") String str = String.format("距离相差：%.2f m", distanceInM); // 保留两位小数,单位为M
//                                     Toast.makeText(MainActivity.this, str, Toast.LENGTH_SHORT).show();
                                     if (distanceInM > THRESHOLD_DISTANCE) {
                                         // 切换到车载模式
                                         INTERVAL = 1000; // 将定时任务的间隔设置为1秒
                                         text_test.setText("车载模式");
                                     } else {
                                         // 切换到普通模式
                                         INTERVAL = 5000; // 将定时任务的间隔设置为5秒
                                         text_test.setText("普通模式");
                                     }
                                 }
                                 lastLatitude = latitude;
                                 lastLongitude = longitude;
                        }

                        else if (locationType == 5) {
//                         publishmessageplus(mqtt_pub_topic, "1");
                                 WifiManager wifiManager = (WifiManager) getApplicationContext().getSystemService(Context.WIFI_SERVICE);
                                 WifiInfo wifiInfo = wifiManager.getConnectionInfo();
                                 if (!wifiManager.isWifiEnabled()) {
                                     wifiManager.setWifiEnabled(true);
                                 }
                                 if (ActivityCompat.checkSelfPermission(MainActivity.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                     // TODO: Consider calling
                                     //    ActivityCompat#requestPermissions
                                     // here to request the missing permissions, and then overriding
                                     //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                     //                                          int[] grantResults)
                                     // to handle the case where the user grants the permission. See the documentation
                                     // for ActivityCompat#requestPermissions for more details.
                                     return;
                                 }
                                 LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                                 Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
//                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                 double latitude = amapLocation.getLatitude(); // 获取纬度
                                 double longitude = amapLocation.getLongitude(); // 获取经度
                                 if (location != null) {
                                     double altitude = location.getAltitude(); // 获取海拔高度
                                     String locationInfo = "{\"mqtt_id\": \"" + mqtt_id + "\", \"Latitude\": " + latitude + ", \"Longitude\": " + longitude + ", \"Altitude\": " + altitude + "}";
                                     List<ScanResult> results = wifiManager.getScanResults();
                                     String[] wifiList = new String[results.size()];
                                     for (int i = 0; i < results.size(); i++) {
                                         wifiList[i] = results.get(i).SSID;
                                         ScanResult scanResult = results.get(i);
                                         String ssid = scanResult.SSID;
                                         int signalStrength = scanResult.level; // 获取WiFi信号强度
                                         int frequency = wifiInfo.getFrequency(); // 获取WiFi信号的频率，单位为MHz
                                         String macAddress = scanResult.BSSID; // 获取WiFi网络的MAC地址
                                         @SuppressLint("SimpleDateFormat") SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy年MM月dd日 HH:mm:ss");// HH:mm:ss
                                         //获取当前时间
                                         Date date = new Date(System.currentTimeMillis());
                                         String formattedDate = simpleDateFormat.format(date); // 将日期转换为字符串
                                         long currentTimeMillis = System.currentTimeMillis();
                                         String wifi_Info = "{\"mqtt_id\": \"" + mqtt_id + "\",\"SSID\": \"" + ssid + "\",\"SignalStrength\": " + signalStrength + ",\"MAC\": \"" + macAddress + "\",\"frequency\":\"" + frequency + "\"}";
                                         String combinedInfo = "[" + locationInfo + "," + wifi_Info + "]";

                                         Cursor cursor = db.rawQuery("SELECT * FROM user1 WHERE MAC = ?", new String[]{macAddress});
                                         if (cursor.getCount() <= 0) {
                                             if (signalStrength < -70) {
                                                 ContentValues values = new ContentValues();
                                                 values.put("SSID", ssid);
                                                 values.put("SignalStrength", signalStrength);
                                                 values.put("MAC", macAddress);
                                                 values.put("frequency", frequency);
                                                 values.put("Latitude", latitude);
                                                 values.put("Longitude", longitude);
                                                 values.put("Altitude", altitude);
                                                 values.put("in_time", currentTimeMillis);
                                                 long newRowId = db.insert("user1", null, values);
//                                                 publishmessageplus(mqtt_WIFI_SSID, combinedInfo);// 将位置信息发布到 MQTT 主题
                                                 if (newRowId != -1) {
                                                     Toast.makeText(MainActivity.this, "位置数据插入成功", Toast.LENGTH_SHORT).show();
                                                 }
                                                 else {
                                                     Toast.makeText(MainActivity.this, "插入位置数据失败", Toast.LENGTH_SHORT).show();
                                                 }
                                             }
                                         }
                                         // 关闭游标
                                         cursor.close();
                                     }
                                 }
                             }
                    }
                    else {
                    //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                    Log.e("AmapError", "location Error, ErrCode:"
                            + amapLocation.getErrorCode() + ", errInfo:"
                            + amapLocation.getErrorInfo());
                    }
                }
            }
        };
        mLocationClient.setLocationListener(mLocationListener);
        AMapLocationClientOption mLocationOption = new AMapLocationClientOption();
        mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
        mLocationOption.setNeedAddress(true);
        mLocationOption.setOnceLocation(true);
        mLocationOption.setMockEnable(false);
        mLocationClient.setLocationOption(mLocationOption);
        mLocationClient.startLocation();
        return null;
    }


    private void initData() {
        mySQLiteOpenHelper = new MySQLiteOpenHelper(this);
        db = mySQLiteOpenHelper.getWritableDatabase();
    }

    private void app_runserver(){
//        Intent intent = new Intent(MainActivity.this, ForegroundService.class);
//        startService(intent);
    }

    private void Noshound_Music(){
        Intent intent = new Intent(MainActivity.this, NosoundMusic.class);
        startService(intent);
    }

    private void DatabaseToMQTT(){

    }

    @SuppressLint("Range")
    private void Setinput_id(){
        // 成功插入数据后获取最大ID
        Cursor cursor_id = db.rawQuery("SELECT MAX(id) AS max_id FROM Input_user", null);
        int maxId = 0;
        Log.d("test_id", String.valueOf(maxId));
        if (cursor_id.moveToFirst()) {
            Log.d("test_id", "在这3");
            maxId = cursor_id.getInt(cursor_id.getColumnIndex("max_id"));
            Log.d("test_id", String.valueOf(maxId));
        }
        cursor_id.close();

        ContentValues maxIdValues = new ContentValues();
        Log.d("test_id", String.valueOf(maxId));
        maxIdValues.put("num", maxId);
        String whereClause = "id_name=?";
        String[] whereArgs = {"input_id"};
        int updateResult = db.update("max_id", maxIdValues, whereClause, whereArgs);
        if (updateResult != -1) {
            Toast.makeText(MainActivity.this, "Num updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Failed to update num", Toast.LENGTH_SHORT).show();
        }
    }


    @SuppressLint("Range")
    private void Setwifi_id() {
        // 成功插入数据后获取最大ID
        Cursor cursor_id = db.rawQuery("SELECT MAX(id) AS max_id FROM user", null);
        int maxId = 0;
        Log.d("test_id", String.valueOf(maxId));
        if (cursor_id.moveToFirst()) {
            Log.d("test_id", "在这3");
            maxId = cursor_id.getInt(cursor_id.getColumnIndex("max_id"));
            Log.d("test_id", String.valueOf(maxId));
        }
        cursor_id.close();

        ContentValues maxIdValues = new ContentValues();
        Log.d("test_id", String.valueOf(maxId));
        maxIdValues.put("num", maxId);
        String whereClause = "id_name=?";
        String[] whereArgs = {"wifi_id"};
        int updateResult = db.update("max_id", maxIdValues, whereClause, whereArgs);
        if (updateResult != -1) {
            Toast.makeText(MainActivity.this, "Num updated successfully", Toast.LENGTH_SHORT).show();
        } else {
            Toast.makeText(MainActivity.this, "Failed to update num", Toast.LENGTH_SHORT).show();
        }
    }
}


