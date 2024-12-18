//package com.example.mqtt_demo;
//
//import android.annotation.SuppressLint;
//import android.app.Notification;
//import android.app.NotificationChannel;
//import android.app.NotificationManager;
//import android.app.Service;
//import android.content.Context;
//import android.content.Intent;
//import android.os.Build;
//import android.os.IBinder;
//import android.util.Log;
//
//import androidx.annotation.Nullable;
//
//public class ForegroundService extends Service {
////    private static final int NOTIFICATION_ID = 1;
////
////    @SuppressLint("ForegroundServiceType")
////    @Override
////    public int onStartCommand(Intent intent, int flags, int startId) {
////        Notification notification = new Notification.Builder(this)
////                .setContentTitle("App正在运行")
////                .setContentText("点击返回App")
////                .setSmallIcon(R.drawable.open).build();
////        startForeground(NOTIFICATION_ID, notification);
////        return super.onStartCommand(intent, flags, startId);
////    }
////
////    @Nullable
////    @Override
////    public IBinder onBind(Intent intent) {
////        return null;
////    }
//private NotificationManager notificationManager;
//    private String notificationId = "serviceid";
//    private String notificationName = "servicename";
//
//    @SuppressLint("ForegroundServiceType")
//    @Override
//    public void onCreate() {
//        super.onCreate();
//        notificationManager = (NotificationManager) getSystemService(Context.NOTIFICATION_SERVICE);
//        //创建NotificationChannel
//        if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.O){
//            NotificationChannel channel = new NotificationChannel(notificationId, notificationName, NotificationManager.IMPORTANCE_HIGH);
//            notificationManager.createNotificationChannel(channel);
//        }
//        startForeground(1,getNotification());
//    }
//
//    private Notification getNotification() {
//        Notification.Builder builder = new Notification.Builder(this)
//                .setSmallIcon(R.mipmap.ic_launcher)
//                .setContentTitle("title")
//                .setContentText("text");
//        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
//            builder.setChannelId(notificationId);
//        }
//        Notification notification = builder.build();
//        return notification;
//    }
//    @Override
//    public int onStartCommand(Intent intent, int flags, int startId) {
//
//        new Thread(new Runnable() {
//            @Override
//            public void run() {
//                while (true){
//
//                    try {
//                        Thread.sleep(2000);
//
//                         Log.d("Services","====保活服务===数据支持====");
//                    } catch (InterruptedException e) {
//                        e.printStackTrace();
//                    }
//
//                }
//            }
//        }).start();
//
//
//        return super.onStartCommand(intent, flags, startId);
//    }
//
//    @Nullable
//    @Override
//    public IBinder onBind(Intent intent) {
//
//        return null;
//    }
//
//    @Override
//    public void onDestroy() {
//        super.onDestroy();
//
//    }
//
//}
//
