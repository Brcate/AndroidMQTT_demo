package com.example.mqtt_demo;

//<code class="language-plaintext hljs">
        import android.media.MediaPlayer;
        import java.io.File;
        import java.io.IOException;
        import java.io.FileOutputStream;
        import android.util.Base64;
        import java.io.FileInputStream;
        import android.app.Service;
        import android.content.Intent;
        import android.os.IBinder;

public class NosoundMusic extends Service {
    MediaPlayer mediaplayer=null;
    //转base64的音频文件
    String base64 = "AAAAGGZ0eXBtcDQyAAAAAG1wNDFpc29tAAAAKHV1aWRcpwj7Mo5CBahhZQ7KCpWWAAAADDEwLjAuMTgzNjMuMAAAAG5tZGF0AAAAAAAAABAnDEMgBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBAIBDSX5AAAAAAAAB9Pp9Pp9Pp9Pp9Pp9Pp9Pp9Pp9Pp9Pp9Pp9AAAC/m1vb3YAAABsbXZoZAAAAADeilCc3opQnAAAu4AAAAIRAAEAAAEAAAAAAAAAAAAAAAABAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAAMAAAHBdHJhawAAAFx0a2hkAAAAAd6KUJzeilCcAAAAAgAAAAAAAAIRAAAAAAAAAAAAAAAAAQAAAAABAAAAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAAQAAAAAAAAAAAAAAAAAABXW1kaWEAAAAgbWRoZAAAAADeilCc3opQnAAAu4AAAAIRVcQAAAAAAC1oZGxyAAAAAAAAAABzb3VuAAAAAAAAAAAAAAAAU291bmRIYW5kbGVyAAAAAQhtaW5mAAAAEHNtaGQAAAAAAAAAAAAAACRkaW5mAAAAHGRyZWYAAAAAAAAAAQAAAAx1cmwgAAAAAQAAAMxzdGJsAAAAZHN0c2QAAAAAAAAAAQAAAFRtcDRhAAAAAAAAAAEAAAAAAAAAAAACABAAAAAAu4AAAAAAADBlc2RzAAAAAAOAgIAfAAAABICAgBRAFQAGAAACM2gAAjNoBYCAgAIRkAYBAgAAABhzdHRzAAAAAAAAAAEAAAABAAACEQAAABxzdHNjAAAAAAAAAAEAAAABAAAAAQAAAAEAAAAYc3RzegAAAAAAAAAAAAAAAQAAAF4AAAAUc3RjbwAAAAAAAAABAAAAUAAAAMl1ZHRhAAAAkG1ldGEAAAAAAAAAIWhkbHIAAAAAAAAAAG1kaXIAAAAAAAAAAAAAAAAAAAAAY2lsc3QAAAAeqW5hbQAAABZkYXRhAAAAAQAAAADlvZXpn7MAAAAcqWRheQAAABRkYXRhAAAAAQAAAAAyMDIyAAAAIWFBUlQAAAAZZGF0YQAAAAEAAAAA5b2V6Z+z5py6AAAAMVh0cmEAAAApAAAAD1dNL0VuY29kaW5nVGltZQAAAAEAAAAOABUA2rD/dVfYAQ==";

    @Override
    public void onCreate() {
        super.onCreate();

        if(mediaplayer==null){
            new Thread(new Runnable(){

                @Override
                public void run() {
                    mediaplayer=new MediaPlayer();
                    try {
                        byte[] mp3SoundByteArray = Base64.decode(base64, Base64.DEFAULT);// 将字符串转换为byte数组
                        File tempMp3 = File.createTempFile("s", ".mp3");
                        tempMp3.deleteOnExit();
                        FileOutputStream fos = new FileOutputStream(tempMp3);
                        fos.write(mp3SoundByteArray);
                        fos.close();
                        FileInputStream fis = new FileInputStream(tempMp3);
                        mediaplayer.setDataSource(fis.getFD());
                        mediaplayer.setLooping(true);
                        mediaplayer.prepareAsync ();//异步准备播放 这部必须设置不然无法播放
                        mediaplayer.start();//开始播放
                    } catch (IllegalStateException e) {
                        System.out.print("出错了="+e);
                    } catch (SecurityException e) {} catch (IOException e) {
                        System.out.print("出错了="+e);
                    } catch (IllegalArgumentException e) {}
                }
            }).start();

        }
    }

    @Override
    public void onDestroy() {
        super.onDestroy();
        //停止
        mediaplayer.stop();
        mediaplayer=null;
    }
    @Override
    public IBinder onBind(Intent intent) {

        return null;
    }
}
//</code>
