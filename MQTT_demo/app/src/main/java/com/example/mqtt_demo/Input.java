package com.example.mqtt_demo;

import android.Manifest;
import android.annotation.SuppressLint;
import android.app.DatePickerDialog;
import android.app.TimePickerDialog;
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
import android.os.Handler;
import android.os.Message;
import android.provider.BlockedNumberContract;
import android.provider.Settings;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.Spinner;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import androidx.annotation.RequiresApi;
import androidx.appcompat.app.AppCompatActivity;
import androidx.core.app.ActivityCompat;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.example.mqtt_demo.db.MySQLiteOpenHelper;

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

import java.io.File;
import java.io.InputStream;
import java.security.KeyStore;
import java.security.cert.CertificateFactory;
import java.security.cert.X509Certificate;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import javax.net.ssl.SSLContext;
import javax.net.ssl.TrustManagerFactory;

import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.location.AMapLocationListener;
import androidx.appcompat.app.AppCompatActivity;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import com.example.mqtt_demo.db.MySQLiteOpenHelper;

public class Input extends AppCompatActivity {

//    private Button btnAdd;
//    private Button btnReduce;
    private Button btn_cancel;
    private Button btn_register;
    private Button timePickerButton;
    private Button timePickerButton1;
    private Button bt_local;
    private EditText et_name;
    private EditText et_phone;
    private EditText case_start_time;
    private EditText case_end_time;
    private EditText et_local;
    private EditText et_localCN;
    private EditText et_Latitude;
    private EditText et_Longitude;
    private TextView spinner;
    private Spinner sp_select;
    private String host = "ssl://qf69fee3.ala.cn-hangzhou.emqxsl.cn:8883";
    private String userName = "android";
    private String passWord = "android";
    //    private String mqtt_id = "513223951"; //定义成自己的QQ号  切记！不然会掉线！！！
    private String androidId;
    private String mqtt_id;
    private String mqtt_sub_topic = "513223951"; //为了保证你不受到别人的消息  哈哈
    private String mqtt_input_info = "513223951_input_info";
    private String mqtt_pub_topic = "513223951_ESP"; //为了保证你不受到别人的消息  哈哈  自己QQ好后面加 _PC
    private String mqtt_gps_topic = "513223951_GPS";
    private String mqtt_gps_topic1 = "513223951_GPS1";
    private String mqtt_Cell_tac = "513223951_Celltac";
    private String mqtt_Cell_lac = "513223951_Celllac";
    private String mqtt_Cell_5G = "513223951_Celll5G";
    private String mqtt_WIFI_SSID = "513223951_SSID";
    private MqttClient client;
    private MqttConnectOptions options;
    private Handler handler;
    private static final int LOCATION_PERMISSION_REQUEST_CODE = 100;
    private ScheduledExecutorService scheduler;
    private Handler mHandler = new Handler();
    private final int INTERVAL = 2000; // 1分钟，以毫秒为单位
    private MySQLiteOpenHelper mySQLiteOpenHelper;
    private SQLiteDatabase db;

    @SuppressLint("HandlerLeak")
    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.input_main1);
        {
            btn_cancel = findViewById(R.id.btn_cancel);
            btn_register = findViewById(R.id.btn_register);
            et_name = findViewById(R.id.et_name);
            case_start_time = findViewById(R.id.case_start_time);
            case_end_time = findViewById(R.id.case_end_time);
            sp_select = findViewById(R.id.sp_select);
            et_phone = findViewById(R.id.et_phone);
            timePickerButton = findViewById(R.id.time_picker_button_start);
            timePickerButton1 = findViewById(R.id.time_picker_button_end);
            bt_local = findViewById(R.id.bt_local);
            et_local = findViewById(R.id.et_local);
            et_localCN = findViewById(R.id.et_localCN);
            et_Latitude = findViewById(R.id.et_Latitude);
            et_Longitude = findViewById(R.id.et_Longitude);
            androidId = Settings.Secure.getString(getContentResolver(), Settings.Secure.ANDROID_ID);
            spinner = findViewById(R.id.et_people_info);
            mqtt_id = androidId;
        }
        {
            bt_local_btn();
            Input_return();
            Mqtt_init();
            Mqtt_connect();
            startReconnect();
            Input_register();
            initData();
            time_Onclick_start();
            time_Onclick_end();

        }
        {
            Cursor cursor1 = db.rawQuery("SELECT * FROM people_info", null);
            Map<String, String> peopleInfoMap = new HashMap<>();
            String lastCheckPeople = "";
            String lastCheckPeopleNum = "";

            if (cursor1 != null && cursor1.moveToFirst()) {
                do {
                    String check_people = cursor1.getString(cursor1.getColumnIndexOrThrow("check_people"));
                    String check_people_num = cursor1.getString(cursor1.getColumnIndexOrThrow("check_people_num"));

                    // 存储到Map结构中
                    Log.d("people_info", "check_people: " + check_people);
                    Log.d("people_info", "check_people_num: " + check_people_num);

                    peopleInfoMap.put(check_people, check_people_num);

                    // 保存最后一行的数据
                    lastCheckPeople = check_people;
                    lastCheckPeopleNum = check_people_num;
                } while (cursor1.moveToNext());
            }

            if (cursor1 != null) {
                cursor1.close();
            }

            Log.d("people_info", "Last row - check_people: " + lastCheckPeople);
            Log.d("people_info", "Last row - check_people_num: " + lastCheckPeopleNum);

// 使用最后一行数据
            String Account = lastCheckPeople + lastCheckPeopleNum;
            spinner.setText(Account);

        }
        {
            Intent intent = getIntent();
            if (intent != null) {
                String address = intent.getStringExtra("address");
                Log.d("address","address:" + address);
                String Latitude = intent.getStringExtra("Latitude");
                String Longitude = intent.getStringExtra("Longitude");
                if (address != null) {
                    int addressLength = address.length();
                    if (addressLength > 8) {
                        // address不为空且长度大于0
                        et_localCN.setText(address);
                        et_Latitude.setText(Latitude);
                        et_Longitude.setText(Longitude);
                    } else {
                        // address为空或长度为0
                        et_localCN.setText("未获取地址信息");
                    }
                }
            }
        }
//        {
//            List<PeopleInfo> peopleInfoList = new ArrayList<>();
//            Cursor selectedCursor = db.rawQuery("SELECT check_people, check_people_num FROM people_info", null);
//            if (selectedCursor != null && selectedCursor.moveToFirst()) {
//                do {
//                    @SuppressLint("Range") String checkPeople = selectedCursor.getString(selectedCursor.getColumnIndex("check_people"));
//                    @SuppressLint("Range") String num = selectedCursor.getString(selectedCursor.getColumnIndex("check_people_num"));
//                    PeopleInfo peopleInfo = new PeopleInfo(checkPeople, num);
//                    peopleInfoList.add(peopleInfo);
//                } while (selectedCursor.moveToNext());
//            }
//            selectedCursor.close();
//            ArrayAdapter<PeopleInfo> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, peopleInfoList);
//            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
//            spinner.setAdapter(adapter);
//        }
        handler = new Handler() {
            @SuppressLint({"SetTextI18n", "HandlerLeak"})
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
//                        Toast.makeText(Input.this, "连接失败", Toast.LENGTH_SHORT).show();
                        break;
                    case 31:   //连接成功
//                        Toast.makeText(Input.this, "连接成功", Toast.LENGTH_SHORT).show();
                        try {
                            client.subscribe(mqtt_sub_topic, 1);//订阅我的mqtt_sub_topic号，QoS端口为:1
                            update_edittext();
                        } catch (MqttException e) {
                            e.printStackTrace();
                        }
                        break;
                    default:
                        break;
                }
            }
        };
    }


    public class PeopleInfo {
        private String checkPeople;
        private String num;

        public PeopleInfo(String checkPeople, String num) {
            this.checkPeople = checkPeople;
            this.num = num;
        }

        public String getCheckPeople() {
            return checkPeople;
        }

        public String getNum() {
            return num;
        }

        @Override
        public String toString() {
            return checkPeople + " " + num;
        }
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
                    if (!(client.isConnected()))  //如果还未连接
                    {
                        client.connect(options);
                        Message msg = new Message();
                        msg.what = 31;
                        // 没有用到obj字段
                        handler.sendMessage(msg);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    Message msg = new Message();
                    msg.what = 30;
                    // 没有用到obj字段
//                    handler.sendMessage(msg);
                }
            }
        }).start();
    }    //Mqtt连接函数


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


    private void Input_return(){
        btn_cancel.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = new Intent(Input.this, MainActivity.class);
                startActivity(intent);
                finish(); // 销毁当前页面
            }
        });
    }


    private void Input_register(){
        btn_register.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Intent intent = getIntent();
                if (intent != null) {
                    String name = et_name.getText().toString();
                    String start_time = case_start_time.getText().toString();
                    String end_time = case_end_time.getText().toString();
                    String phone = et_phone.getText().toString();
                    String address = intent.getStringExtra("address");
                    Log.d("address","address:" + address);
                    if (address != null) {
                        int addressLength = address.length();
                        int nameLength = name.length();
                        int start_timeLength = start_time.length();
                        int end_timeLength = end_time.length();
                        int phoneLength = phone.length();
                        if (addressLength > 8 && nameLength > 0 && start_timeLength > 0 && end_timeLength > 0 && phoneLength > 0) {
                            try {
                                startLocation(1);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        } else {
                            // address为空或长度为0
                            Toast.makeText(Input.this, "保存失败，有数据未填", Toast.LENGTH_SHORT).show();
                        }
                    }
                }


            }
        });
    }


    private void time_Onclick_start(){
        timePickerButton.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);

                // 创建一个 DatePickerDialog 用于选择日期
                DatePickerDialog datePickerDialog = new DatePickerDialog(Input.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // 在日期选择后，创建一个 TimePickerDialog 用于选择时间
                                TimePickerDialog timePickerDialog = new TimePickerDialog(Input.this,
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                // 合并所选日期和时间
                                                Calendar calendar = Calendar.getInstance();
                                                calendar.set(year, month, dayOfMonth, hourOfDay, minute);

                                                // 创建 SimpleDateFormat 对象用于格式化日期时间
                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault());

                                                // 格式化日期时间
                                                String formattedDateTime = sdf.format(calendar.getTime());

                                                // 将格式化后的日期时间显示在界面上，例如设置到一个 TextView 中
                                                case_start_time.setText(formattedDateTime);
                                            }
                                        }, hour, minute, true);
                                timePickerDialog.show();
                            }
                        }, currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
    }

    private void time_Onclick_end(){
        timePickerButton1.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                Calendar currentTime = Calendar.getInstance();
                int hour = currentTime.get(Calendar.HOUR_OF_DAY);
                int minute = currentTime.get(Calendar.MINUTE);

                // 创建一个 DatePickerDialog 用于选择日期
                DatePickerDialog datePickerDialog = new DatePickerDialog(Input.this,
                        new DatePickerDialog.OnDateSetListener() {
                            @Override
                            public void onDateSet(DatePicker view, int year, int month, int dayOfMonth) {
                                // 在日期选择后，创建一个 TimePickerDialog 用于选择时间
                                TimePickerDialog timePickerDialog = new TimePickerDialog(Input.this,
                                        new TimePickerDialog.OnTimeSetListener() {
                                            @Override
                                            public void onTimeSet(TimePicker view, int hourOfDay, int minute) {
                                                // 合并所选日期和时间
                                                Calendar calendar = Calendar.getInstance();
                                                calendar.set(year, month, dayOfMonth, hourOfDay, minute);

                                                // 创建 SimpleDateFormat 对象用于格式化日期时间
                                                SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault());

                                                // 格式化日期时间
                                                String formattedDateTime = sdf.format(calendar.getTime());

                                                // 将格式化后的日期时间显示在界面上，例如设置到一个 TextView 中
                                                case_end_time.setText(formattedDateTime);
                                            }
                                        }, hour, minute, true);
                                timePickerDialog.show();
                            }
                        }, currentTime.get(Calendar.YEAR), currentTime.get(Calendar.MONTH), currentTime.get(Calendar.DAY_OF_MONTH));
                datePickerDialog.show();
            }
        });
    }

    private void bt_local_btn(){
        bt_local.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                String name = et_name.getText().toString();
                String start_time = case_start_time.getText().toString();
                String end_time = case_end_time.getText().toString();
                String phone = et_phone.getText().toString();
                Log.d("inputInfo_send", name);
                Log.d("inputInfo_send", start_time);
                Log.d("inputInfo_send", end_time);
                Log.d("inputInfo_send", phone);
                String inputInfo = "{\"name\": \"" + name + "\", \"start_time\": \"" + start_time + "\", \"end_time\": \"" + end_time + "\", \"phone\": \"" + phone + "\"}";
                Intent intent = new Intent(Input.this, MapActivity.class);
                intent.putExtra("inputInfo", inputInfo); // 将输入的信息放入Intent中
                startActivity(intent);
                finish(); // 销毁当前页面
            }
        });
    }

//    private void people_info(){
//        // 设置Spinner的选择事件监听器
//        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
//            @Override
//            public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
//                // 获取选中的项
//                String selectedPeopleInfo = peopleInfoOptions[position];
//
//                // 根据选择的内容进行相应操作，例如获取id和显示相关信息
//                int selectedId = position + 1;
//                Toast.makeText(getApplicationContext(), "Selected id: " + selectedId + ", Selected info: " + selectedPeopleInfo, Toast.LENGTH_SHORT).show();
//            }
//
//            @Override
//            public void onNothingSelected(AdapterView<?> parent) {
//                // 未选择时的处理
//            }
//        });
//    }
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

                            if (ActivityCompat.checkSelfPermission(Input.this, Manifest.permission.ACCESS_FINE_LOCATION) != PackageManager.PERMISSION_GRANTED) {
                                // TODO: Consider calling
                                //    ActivityCompat#requestPermissions
                                // here to request the missing permissions, and then overriding
                                //   public void onRequestPermissionsResult(int requestCode, String[] permissions,
                                //                                          int[] grantResults)
                                // to handle the case where the user grants the permission. See the documentation
                                // for ActivityCompat#requestPermissions for more details.
                                return;
                            }
                            long currentTimeMillis = System.currentTimeMillis();
                            LocationManager locationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
                            Location location = locationManager.getLastKnownLocation(LocationManager.GPS_PROVIDER);
                            Cursor cursor1 = db.rawQuery("SELECT * FROM people_info", null);
                            Map<String, String> peopleInfoMap = new HashMap<>();
                            String lastCheckPeople = "";
                            String lastCheckPeopleNum = "";
                            if (cursor1 != null && cursor1.moveToFirst()) {
                                do {
                                    String check_people = cursor1.getString(cursor1.getColumnIndexOrThrow("check_people"));
                                    String check_people_num = cursor1.getString(cursor1.getColumnIndexOrThrow("check_people_num"));
                                    // 存储到Map结构中
                                    Log.d("Debug", "check_people: " + check_people);
                                    Log.d("Debug", "check_people_num: " + check_people_num);
                                    peopleInfoMap.put(check_people, check_people_num);

                                    // 保存最后一行的数据
                                    lastCheckPeople = check_people;
                                    lastCheckPeopleNum = check_people_num;
                                } while (cursor1.moveToNext());
                            }
                            if (cursor1 != null) {
                                cursor1.close();
                            }
                            if (location != null) {
                                double altitude = location.getAltitude(); // 获取海拔高度
                                double latitude = amapLocation.getLatitude();
                                double longitude = amapLocation.getLongitude();
                                Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");
                                Log.d("Debug", "latitude: " + latitude + " longitude: " + longitude);
//                    Location location = locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
                                String name = et_name.getText().toString();
                                String start_time = case_start_time.getText().toString();
                                String end_time = case_end_time.getText().toString();
                                String select_type = sp_select.getSelectedItem().toString();
//                                String people_info = spinner.getSelectedItem().toString();
//                                Log.d("people_info", "people_info: " + people_info);
//                                String regex = "(.*?)\\s(\\d+)";
//                                Pattern pattern = Pattern.compile(regex);
//                                Matcher matcher = pattern.matcher(people_info);
//                                Log.d("people_info", "pattern: " + pattern + " matcher: " + matcher);
//                                String check_people = "";
//                                String num = "";
//                                if (matcher.find()) {
//                                    check_people = matcher.group(1).trim();
//                                    num = matcher.group(2).trim();
//                                }
                                String phone = et_phone.getText().toString();
                                Intent intent = getIntent();
                                if (intent != null) {
                                    String address = intent.getStringExtra("address");
                                    String Latitude = intent.getStringExtra("Latitude");
                                    String Longitude = intent.getStringExtra("Longitude");
                                    if (address != null && Latitude != null && Longitude != null) {
// 创建 SimpleDateFormat 对象用于解析日期时间字符串
                                        SimpleDateFormat sdf = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSSSSS", Locale.getDefault());
                                        try {
                                            // 解析日期时间字符串为 Date 对象
                                            Date date_start = sdf.parse(start_time);
                                            Date date_end = sdf.parse(end_time);
                                            // 获取时间戳
                                            long timestamp_start = date_start.getTime();
                                            long timestamp_end = date_end.getTime();
                                            // 可以使用时间戳进行后续操作，例如保存到数据库或其他处理
                                            // 例如：保存到数据库
                                            // dbHelper.saveTimestamp(timestamp);
                                            Log.d("Debug", "Key: " + timestamp_start + " Value: " + timestamp_end);
                                            for (Map.Entry<String, String> entry : peopleInfoMap.entrySet()) {
                                                Log.d("Debug", "Key: " + entry.getKey() + " Value: " + entry.getValue());

                                                // 获取check_people对应的值
                                                String check_people_value = entry.getKey();
                                                // 获取check_people_num对应的值
                                                String check_people_num_value = entry.getValue();
//
//                                    if (check_people_value != null && check_people_num_value != null) {
//                                        Toast.makeText(Input.this, check_people_value, Toast.LENGTH_SHORT).show();
//                                        Toast.makeText(Input.this, check_people_num_value, Toast.LENGTH_SHORT).show();
//                                    }
                                                String input_info = "{\"name\": \"" + name + "\", \"start_time\": \"" + timestamp_start + "\", \"end_time\"" + timestamp_end + "\",\"phone\": \"" + phone + "\",\"case_type\": \"" + select_type + "\", \"in_time\"" + currentTimeMillis + "\", \"check_people\"" + lastCheckPeople + "\", \"check_people_num\"" + lastCheckPeopleNum + "\", \"Location_CN\"" + address + "\"}";
                                                Toast.makeText(Input.this, input_info, Toast.LENGTH_SHORT).show();
                                                String locationInfo = "{\"mqtt_id\": \"" + mqtt_id + "\", \"Latitude\": " + Latitude + ", \"Longitude\": " + Longitude + ", \"Altitude\": " + altitude + "}";
//                                Toast.makeText(Input.this, locationInfo, Toast.LENGTH_SHORT).show();
                                                // 将位置信息发布到 MQTT 主题
                                                String combinedInfo = "[" + locationInfo + "," + input_info + "]";
//                                publishmessageplus(mqtt_input_info, combinedInfo);

                                                Cursor cursor = db.rawQuery("SELECT * FROM Input_user WHERE name = ?", new String[]{name});
//                                Cursor cursor = db.rawQuery("SELECT * FROM Input_user", null);
                                                if (cursor.getCount() > 0) {

                                                } else {
                                                    ContentValues values = new ContentValues();
                                                    values.put("name", name);
                                                    values.put("start_time", timestamp_start);
                                                    values.put("end_time", timestamp_end);
                                                    values.put("phone", phone);
                                                    values.put("case_type", select_type);
                                                    values.put("check_people", lastCheckPeople);
                                                    values.put("check_people_num", lastCheckPeopleNum);
                                                    values.put("Latitude", Latitude);
                                                    values.put("Longitude", Longitude);
                                                    values.put("Altitude", altitude);
                                                    values.put("in_time", currentTimeMillis);
                                                    values.put("Location_CN", address);
                                                    long newRowId = db.insert("Input_user", null, values);// 在成功插入数据后获取最大ID
//                                    publishmessageplus(mqtt_WIFI_SSID, combinedInfo);// 将位置信息发布到 MQTT 主题
                                                    Log.d("test_id", "在这1");
                                                    if (newRowId != -1) {
                                                        Log.d("test_id", "在这2");
                                                    } else {
                                                        Toast.makeText(Input.this, "Failed to insert location data", Toast.LENGTH_SHORT).show();
                                                    }



                                                        Toast.makeText(Input.this, "Location data inserted successfully", Toast.LENGTH_SHORT).show();
                                                }

                                                // 关闭游标
                                                cursor.close();
                                            }

                                            Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");

                                        } catch (ParseException e) {
                                            Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");

                                            e.printStackTrace();
                                            Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");

                                        }
                                    }
                                }
                            }
                            Toast.makeText(Input.this, "保存成功", Toast.LENGTH_SHORT).show();
                            Intent intent = new Intent(Input.this, MainActivity.class);
                            startActivity(intent);
                            finish(); // 销毁当前页面
                        }
                        if(locationType == 2){
                            double latitude = amapLocation.getLatitude();
                            double longitude = amapLocation.getLongitude();
                            String formattedlocation = "经度：" + latitude + "纬度：" + longitude;
                            et_local.setText(formattedlocation);
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


    private void update_edittext(){
        Log.d("inputInfo", "我在这");
        String inputInfo = getIntent().getStringExtra("inputInfo"); // 获取inputInfo信息
        if (inputInfo != null && !inputInfo.isEmpty()) {
            try {
                JSONObject jsonObject = new JSONObject(inputInfo);
                String name = jsonObject.getString("name");
                String start_time = jsonObject.getString("start_time");
                String end_time = jsonObject.getString("end_time");
                String phone = jsonObject.getString("phone");
                Log.d("inputInfo", name);
                Log.d("inputInfo", start_time);
                Log.d("inputInfo", end_time);
                Log.d("inputInfo", phone);
                int nameLength = name.length();
                int start_timeLength = start_time.length();
                int end_timeLength = end_time.length();
                int phoneLength = phone.length();
                    if (nameLength > 0) {
                        et_name.setText(name);
                    }
                    if (phoneLength > 0) {
                        et_phone.setText(phone);
                    }
                    if (start_timeLength > 0) {
                        case_start_time.setText(start_time);
                    }
                    if (end_timeLength > 0) {
                        case_end_time.setText(end_time);
                    }

                // 在这里可以使用获取到的字段值进行操作
                // 比如设置到 EditText 或者 TextView 中
            } catch (JSONException e) {
                e.printStackTrace();
            }
        }
    }

}


