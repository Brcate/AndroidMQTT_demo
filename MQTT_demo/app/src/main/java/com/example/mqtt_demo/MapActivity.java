package com.example.mqtt_demo;


import android.app.Activity;
import android.content.Intent;
import android.graphics.Color;
import android.graphics.Point;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import com.amap.api.location.AMapLocation;
import com.amap.api.location.AMapLocationClient;
import com.amap.api.location.AMapLocationClientOption;
import com.amap.api.location.AMapLocationListener;
import com.amap.api.maps.AMap;
import com.amap.api.maps.AMapOptions;
import com.amap.api.maps.CameraUpdateFactory;
import com.amap.api.maps.LocationSource;
import com.amap.api.maps.MapView;
import com.amap.api.maps.Projection;
import com.amap.api.maps.UiSettings;
import com.amap.api.maps.model.CameraPosition;
import com.amap.api.maps.model.LatLng;
import com.amap.api.maps.model.Marker;
import com.amap.api.maps.model.MarkerOptions;
import com.amap.api.maps.model.MyLocationStyle;
import com.amap.api.maps.model.Text;
import com.amap.api.services.core.AMapException;
import com.amap.api.services.core.LatLonPoint;
import com.amap.api.services.geocoder.GeocodeQuery;
import com.amap.api.services.geocoder.GeocodeResult;
import com.amap.api.services.geocoder.GeocodeSearch;
import com.amap.api.services.geocoder.RegeocodeAddress;
import com.amap.api.services.geocoder.RegeocodeQuery;
import com.amap.api.services.geocoder.RegeocodeResult;

import org.json.JSONException;
import org.json.JSONObject;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Timer;
import java.util.TimerTask;
import java.util.concurrent.atomic.AtomicBoolean;

//public class MapActivity extends Activity implements AMap.OnMapLongClickListener {
    public class MapActivity extends Activity implements LocationSource, AMapLocationListener, AMap.OnCameraChangeListener, GeocodeSearch.OnGeocodeSearchListener {
        private MapView amapView;
        private AMap aMap;
        private AMapLocationClient mlocationClient;
        private AMapLocationClientOption mLocationOption;
        private MapView mMapView;
        private AMap mAMap;
        private Marker mGPSMarker;             //定位位置显示
        private AMapLocation location;
        private LocationSource.OnLocationChangedListener mListener;
        //你编码对象
        private GeocodeSearch geocoderSearch;
        private View centerPoint;
        private EditText et_local;
        private Button bt_Yes;
        private double centerLatitude;
        private double centerLongitude;
        private Timer locationUpdateTimer;
        private TimerTask locationUpdateTask;
        private final long LOCATION_UPDATE_DELAY = 1000; // 1秒延迟
        @Override
        protected void onCreate(Bundle savedInstanceState) {
            super.onCreate(savedInstanceState);
            {
                setContentView(R.layout.map);
                centerPoint = findViewById(R.id.center_point);
                et_local = findViewById(R.id.et_local);
                amapView = findViewById(R.id.map_view);
                bt_Yes = findViewById(R.id.bt_yes);
            }
            amapView.onCreate(savedInstanceState);  // 1. 创建地图
            if (aMap == null) {
                aMap = amapView.getMap();
//                设置显示定位按钮 并且可以点击
                // 在初始化地图后，设置地图状态改变监听器
                aMap.setOnCameraChangeListener(this);
                UiSettings settings = aMap.getUiSettings();
//                aMap.setLocationSource(this);//设置了定位的监听,这里要实现LocationSource接口
                // 是否显示定位按钮
                settings.setMyLocationButtonEnabled(true);
                aMap.setMyLocationEnabled(true);//显示定位层并且可以触发定位,默认是flase

            }
            //设置定位蓝点
            MyLocationStyle myLocationStyle;
            myLocationStyle = new MyLocationStyle();//初始化定位蓝点样式类myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);//连续定位、且将视角移动到地图中心点，定位点依照设备方向旋转，并且会跟随设备移动。（1秒1次定位）如果不设置myLocationType，默认也会执行此种模式。
//            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE_NO_CENTER);
            myLocationStyle.myLocationType(MyLocationStyle.LOCATION_TYPE_LOCATION_ROTATE);
            myLocationStyle.interval(300000); //设置连续定位模式下的定位间隔，只在连续定位模式下生效，单次定位模式下不会生效。单位为毫秒。
            myLocationStyle.strokeColor(Color.argb(0, 0, 0, 0));// 设置圆形的边框颜色    不显示范围圆圈
            myLocationStyle.radiusFillColor(Color.argb(0, 0, 0, 0));// 设置圆形的填充颜色 不显示范围圆圈
            aMap.setMyLocationStyle(myLocationStyle);//设置定位蓝点的Style
            aMap.getUiSettings().setMyLocationButtonEnabled(true);//设置默认定位按钮是否显示，非必需设置。
            aMap.setMyLocationEnabled(true);// 设置为true表示启动显示定位蓝点，false表示隐藏定位蓝点并不进行定位，默认是false。
            //设置缩放级别
            aMap.moveCamera(CameraUpdateFactory.zoomTo(15));
            // 控件交互 缩放按钮、指南针、定位按钮、比例尺等
            UiSettings mUiSettings;//定义一个UiSettings对象
            mUiSettings = aMap.getUiSettings();//实例化UiSettings类对象
            mUiSettings.setZoomControlsEnabled(false);
            mUiSettings.setMyLocationButtonEnabled(true); //显示默认的定位按钮
            aMap.setMyLocationEnabled(true);// 可触发定位并显示当前位置
            mUiSettings.setScaleControlsEnabled(true);//控制比例尺控件是否显示
            mUiSettings.setLogoPosition(AMapOptions.LOGO_MARGIN_LEFT);//设置logo位置
            //获取位置信息
            try {
                mlocationClient = new AMapLocationClient(this);
                mLocationOption = new AMapLocationClientOption();
                mLocationOption.setLocationMode(AMapLocationClientOption.AMapLocationMode.Hight_Accuracy);
                mLocationOption.setInterval(2000); // 设置定位间隔为2000ms，正值表示间隔时间
                mLocationOption.setOnceLocation(true);
//                aMap.setOnMapLongClickListener(this);
//                mlocationClient.setLocationListener(this); // 设置定位监听器为当前Activity
                mlocationClient.setLocationOption(mLocationOption);
                mlocationClient.startLocation();
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
            // 在Activity的onCreate()方法中调用GeocodeSearch()方法
            {
                try {
                    GeocodeSearch();
                } catch (AMapException e) {
                    e.printStackTrace();
                }
                setBt_Yes();
            }
        }
        public void setBt_Yes(){
            bt_Yes.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {
                    Log.d("inputInfo", "我在Map这");
                    String input_Info = getIntent().getStringExtra("inputInfo"); // 获取inputInfo信息
                    if (input_Info != null) {
                        try {
                            JSONObject jsonObject = new JSONObject(input_Info);
                            String name = jsonObject.getString("name");
                            String start_time = jsonObject.getString("start_time");
                            String end_time = jsonObject.getString("end_time");
                            String phone = jsonObject.getString("phone");
                            Log.d("inputInfo_map", name);
                            Log.d("inputInfo_map", start_time);
                            Log.d("inputInfo_map", end_time);
                            Log.d("inputInfo_map", phone);
                            String inputInfo = "{\"name\": \"" + name + "\", \"start_time\": \"" + start_time + "\", \"end_time\": \"" + end_time + "\", \"phone\": \"" + phone + "\"}";
                            String address = et_local.getText().toString();
                            double newLatitude = centerLatitude;
                            double newLongitude = centerLongitude;
                            String Latitude = String.valueOf(newLatitude);
                            String Longitude = String.valueOf(newLongitude);
                            Intent intent = new Intent(MapActivity.this, Input.class);
                            intent.putExtra("inputInfo", inputInfo); // 将inputInfo信息放入Intent中
                            intent.putExtra("address", address);
                            intent.putExtra("Latitude", Latitude);
                            intent.putExtra("Longitude", Longitude);
                            startActivity(intent);
                            finish(); // 销毁当前页面
                        } catch (JSONException e) {
                            e.printStackTrace();
                        }
                    }
                    else{
                        Log.d("inputInfo_map", "我在Map确定这出错了");
                    }
                }
            });
        }
    @Override
    public void onLocationChanged(AMapLocation aMapLocation) {
        if (aMapLocation != null) {
            if (aMapLocation.getErrorCode() == 0) {
                //定位成功回调信息，设置相关消息
                aMapLocation.getLocationType();//获取当前定位结果来源，如网络定位结果，详见官方定位类型表
                aMapLocation.getLatitude();//获取纬度
                aMapLocation.getLongitude();//获取经度
                aMapLocation.getAccuracy();//获取精度信息
                SimpleDateFormat df = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss");
                Date date = new Date(aMapLocation.getTime());
                df.format(date);//定位时间
                aMapLocation.getAddress();//地址，如果option中设置isNeedAddress为false，则没有此结果，网络定位结果中会有地址信息，GPS定位不返回地址信息。
                aMapLocation.getCountry();//国家信息
                aMapLocation.getProvince();//省信息
                aMapLocation.getCity();//城市信息
                aMapLocation.getDistrict();//城区信息
                aMapLocation.getStreet();//街道信息
                aMapLocation.getStreetNum();//街道门牌号信息
                aMapLocation.getCityCode();//城市编码
                aMapLocation.getAdCode();//地区编码
                Log.d("Text", "aMapLocation: " + aMapLocation.getLatitude() + "aMapLocation: " + aMapLocation.getLongitude()+ "aMapLocation: " + aMapLocation.getDistrict()+ "aMapLocation: " +  aMapLocation.getStreet()+ "aMapLocation: " + aMapLocation.getStreetNum());
                Log.d("Text", "不成功");
            } else {
                //显示错误信息ErrCode是错误码，errInfo是错误信息，详见错误码表。
                Log.e("AmapError", "location Error, ErrCode:"
                        + aMapLocation.getErrorCode() + ", errInfo:"
                        + aMapLocation.getErrorInfo());
                Toast.makeText(getApplicationContext(), "定位失败", Toast.LENGTH_LONG).show();
            }
        }
    }
    //激活定位
    @Override
    public void activate(OnLocationChangedListener onLocationChangedListener) {
        mListener = onLocationChangedListener;
    }

    @Override
    public void onCameraChange(CameraPosition cameraPosition) {
        // 获取屏幕中心点的经纬度坐标
        LatLng centerLatLng = cameraPosition.target;
        double latitude = centerLatLng.latitude; // 纬度
        double longitude = centerLatLng.longitude; // 经度
        Log.d("Text","latitude"+latitude + "longitude" + longitude);
        centerLatitude = centerLatLng.latitude; // 纬度
        centerLongitude = centerLatLng.longitude; // 经度
//        Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");
//        Log.d("Text","latitude"+centerLatitude + "longitude" + centerLongitude);
//        Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");

        // 在这里可以根据需要对获取到的屏幕中心点坐标进行处理
        // 当地图移动停止后，启动定时器延迟1秒执行位置更新逻辑
        if (locationUpdateTimer != null) {
            locationUpdateTimer.cancel();
        }
        locationUpdateTimer = new Timer();
        locationUpdateTask = new TimerTask() {
            @Override
            public void run() {
                // 在定时器任务中处理位置更新逻辑
                updateLocation();
            }
        };

        // 延迟1秒后执行定时器任务
        locationUpdateTimer.schedule(locationUpdateTask, LOCATION_UPDATE_DELAY);
    }


    // 更新位置信息的方法
//    private void updateLocation() {
//        try {
//            // 在这里调用需要延迟执行的方法，例如获取新的位置信息或更新地图显示
//            // 在onRegeocodeSearched方法中处理逆地理编码查询逻辑
//            // 获取新的经纬度值
//            double newLatitude = centerLatitude;
//            double newLongitude = centerLongitude;
//            LatLonPoint latLonPoint = new LatLonPoint(newLatitude, newLongitude); // 使用保存的经纬度值
//            // 创建新的RegeocodeResult对象
//            RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP); // 200表示查询范围为200米
//            geocoderSearch.getFromLocationAsyn(query);
//            RegeocodeResult regeocodeResult = new RegeocodeResult(query, new RegeocodeAddress());
////            RegeocodeResult regeocodeResult = new RegeocodeResult(new RegeocodeQuery(new LatLonPoint(newLatitude, newLongitude), 200, GeocodeSearch.AMAP), new RegeocodeAddress());
//            // 调用onRegeocodeSearched方法并传入新的RegeocodeResult对象和参数
//            onRegeocodeSearched(regeocodeResult, 1000);
//        } catch (Exception e) {
//            e.printStackTrace();
//        }
//    }
    public void GeocodeSearch() throws AMapException {
        geocoderSearch = new GeocodeSearch(this);
        geocoderSearch.setOnGeocodeSearchListener(this);
    }
    private void updateLocation() {
        try {
            // 获取新的经纬度值
            double newLatitude = centerLatitude;
            double newLongitude = centerLongitude;
            LatLonPoint latLonPoint = new LatLonPoint(newLatitude, newLongitude); // 使用保存的经纬度值
            // 创建查询对象
            RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP); // 200表示查询范围为200米
            // 异步进行逆地理编码查询
            geocoderSearch.getFromLocationAsyn(query);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    @Override
    public void onCameraChangeFinish(CameraPosition cameraPosition) {
        // 地图状态变化完成时被调用
        // 可根据业务需求在地图状态变化完成后进行特定逻辑处理
    }

    @Override
    public void deactivate() {
        mListener = null;
    }
        @Override
        protected void onDestroy() {
            super.onDestroy();
            //在activity执行onDestroy时执行mMapView.onDestroy()，销毁地图
            amapView.onDestroy();
        }
        @Override
        protected void onResume() {
            super.onResume();
            //在activity执行onResume时执行mMapView.onResume ()，重新绘制加载地图
            amapView.onResume();
        }
        @Override
        protected void onPause() {
            super.onPause();
            //在activity执行onPause时执行mMapView.onPause ()，暂停地图的绘制
            amapView.onPause();
        }
        @Override
        protected void onSaveInstanceState(Bundle outState) {
            super.onSaveInstanceState(outState);
            //在activity执行onSaveInstanceState时执行mMapView.onSaveInstanceState (outState)，保存地图当前的状态
            amapView.onSaveInstanceState(outState);
        }



    @Override
public void onRegeocodeSearched(RegeocodeResult result, int rCode) {
    if (rCode == 1000) {
        if (result != null && result.getRegeocodeAddress() != null) {
            Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");
            Log.d("text", result.toString()); // 输出地址信息
            Log.d("text", String.valueOf(result.getRegeocodeAddress())); // 输出地址信息
            Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");
            // 假设result是您的结果对象，包含了RegeocodeAddress对象
            // 假设result是您的结果对象，包含了RegeocodeAddress对象
            RegeocodeAddress regeocodeAddress = result.getRegeocodeAddress(); // 获取RegeocodeAddress对象
            if(regeocodeAddress != null){
                // 打印调试信息，查看RegeocodeAddress对象具体内容
                System.out.println("RegeocodeAddress对象内容：" + regeocodeAddress.toString());
                // 通过RegeocodeAddress对象获取地址信息
                String formatAddress = regeocodeAddress.getFormatAddress(); // 获取格式化地址
                String district = regeocodeAddress.getDistrict(); // 获取区域名称
                System.out.println("格式化地址：" + formatAddress);
                System.out.println("区域名称：" + district);
            } else {
                System.out.println("RegeocodeAddress对象为空");
            }
            // 通过RegeocodeAddress对象获取地址信息
            String formatAddress = regeocodeAddress.getFormatAddress(); // 获取格式化地址
//            String street = regeocodeAddress.getStreet(); // 获取街道名称
//            String number = regeocodeAddress.getStreetNumber(); // 获取门牌号
            String district = regeocodeAddress.getDistrict(); // 获取区域名称
            String address = result.getRegeocodeAddress().getFormatAddress() + "附近";
            // 在这里处理返回的地理信息，例如获取地址描述信息
            Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");
            Log.d("text","第一步"); // 输出地址信息
            Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");

            et_local.setText(address);
            Log.e("formatAddress", "rCode:"+rCode);
            Log.d("Address", address); // 输出地址信息
            // 可以在这里执行地理编码查询的逻辑
//            try {
//                Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");
//
//                Log.d("text","第二步"); // 输出地址信息
//                Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");
//
//                geocoderSearch = new GeocodeSearch(this);
//                LatLonPoint latLonPoint = new LatLonPoint(centerLatitude, centerLongitude); // 使用保存的经纬度值
//                RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP); // 200表示查询范围为200米
//                RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, "广州"); // 200表示查询范围为200米
//                query.setExtensions("all");
//                geocoderSearch.getFromLocationAsyn(query);
//            } catch (AMapException e) {
//                throw new RuntimeException(e);
//            }
        } else {
            // 处理无结果情况
            Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");

            Log.d("text","处理无结果情况"); // 输出地址信息
            Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");
        }
    } else {
        // 处理查询失败情况
        Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");

        Log.d("text","查询失败"); // 输出地址信息
        Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");

    }
}//监听逆地理编码----坐标转换为中文地址信息
    @Override
    public void onGeocodeSearched(GeocodeResult geocodeResult, int i) {
//        try {
//            Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");
//
//            Log.d("text","第3步"); // 输出地址信息
//            Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");
//
//            geocoderSearch = new GeocodeSearch(this);
//            // name表示地址，第二个参数表示查询城市，中文或者中文全拼，citycode、adcode
//            LatLonPoint latLonPoint = new LatLonPoint(centerLatitude, centerLongitude); // 使用保存的经纬度值
//            RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, GeocodeSearch.AMAP); // 200表示查询范围为200米
////            RegeocodeQuery query = new RegeocodeQuery(latLonPoint, 200, "广州"); // 200表示查询范围为200米
//            query.setExtensions("all");
////            GeocodeQuery query1 = new GeocodeQuery(latLonPoint, "广州");
////            geocoderSearch.getFromLocationNameAsyn(query1);
//            geocoderSearch.getFromLocationAsyn(query);
//        } catch (AMapException e) {
//            throw new RuntimeException(e);
//        }
//        Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");
//
//        Log.d("text","第4步"); // 输出地址信息
//        Log.d("Debug", "-----------------------------------------------------------------------------------------------------------------------------------------------------------");
//
//        geocoderSearch.setOnGeocodeSearchListener(this);
    }//监听地理编码----中文地址信息转换为坐标
}
