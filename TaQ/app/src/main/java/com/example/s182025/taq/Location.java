package com.example.s182025.taq;


import android.Manifest;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.database.sqlite.SQLiteDatabase;
import android.location.LocationListener;
import android.location.LocationManager;
import android.location.LocationProvider;
import android.media.AudioAttributes;
import android.media.AudioManager;
import android.media.SoundPool;
import android.os.Build;
import android.provider.Settings;
import android.support.annotation.NonNull;
import android.support.v4.app.ActivityCompat;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;

public class Location extends AppCompatActivity {

    LocationManager locationManager;

    private String arrayText[];                             //MainActivityからのQRコードの情報を格納するための配列
    private String[] date;                                  //日付データを格納しておくための配列
    private ArrayList<String> lat = new ArrayList<>();      //取得した緯度を記録するためのリスト
    private ArrayList<String> lng = new ArrayList<>();      //取得した経度を記録するためのリスト
    private ArrayList<String> time = new ArrayList<>();     //取得した時刻を記録するためのリスト

    private boolean isFirst = true;         //座標の取得が初回なのかどうかの判断のための変数

    SQLiteDatabase sqLiteDatabase;
    DBHelper dbHelper;
    SingleItemDAO singleItemDAO;
    MultiItemDAO multiItemDAO;

    //準備（コンポを部屋に置く・コピペOK）
    SoundPool soundPool;    // 効果音を鳴らす本体（コンポ）
    int mp3a;          // 効果音データ（mp3）



    @Override
    protected void onDestroy() {
        super.onDestroy();
        // 重要：requestLocationUpdatesしたままアプリを終了すると挙動がおかしくなる。
        locationManager.removeUpdates(listener);
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_location);

        getWindow().addFlags(WindowManager.LayoutParams.FLAG_KEEP_SCREEN_ON);       //画面の自動消灯機能を停止させる

        Intent intent = this.getIntent();       //MainActivityから遷移する
        arrayText = intent.getStringExtra("sendText").split(",", 0);        //QR情報が入っているデータを受け取る

        date = getNowDate().split("/",0);       //getNowDate()で入手した日付を「年/月/日」ごとに分けて配列に格納する

        //初期化
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.LOLLIPOP) {
            soundPool = new SoundPool(1, AudioManager.STREAM_MUSIC, 0);
        } else {
            AudioAttributes attr = new AudioAttributes.Builder()
                    .setUsage(AudioAttributes.USAGE_MEDIA)
                    .setContentType(AudioAttributes.CONTENT_TYPE_MUSIC)
                    .build();
            soundPool = new SoundPool.Builder()
                    .setAudioAttributes(attr)
                    .setMaxStreams(1)
                    .build();
        }

        //読込処理
        mp3a = soundPool.load(this, R.raw.button, 1);


        //位置情報の許可を確認する処理
        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,},
                    1000);      //許可されていない場合はrequestPermission()を実行する
                                            //requestPermission()はパーミッションの許可を要求してくれる関数
        } else {
            locationStart();        //許可されていた場合は位置情報の取得を開始する



            //位置情報の通知の感覚を指定する処理
            //複数のプロバイダに要求することで、要求が来るまでの時間を短縮することができる
            locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                    50, 5, listener);     //requestLocationUpdates(プロバイダー名,通知の最小時間間隔,通知の最小距離間隔)
            locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 50, 5, listener);    //requestLocationUpdates(プロバイダー名,通知の最小時間間隔,通知の最小距離間隔)

        }

        if (dbHelper == null) {                      //dbHelperにインスタンスが入っているか判断
            dbHelper = new DBHelper(this); //DBHelperクラスのインスタンスを生成(DBHelperクラスはデータベースを生成する、
        }                                             // SQLiteOpenHelperクラスを継承している)


        if (sqLiteDatabase == null) {                               //sqLiteDatabaseにインスタンスが入っているか判断

            sqLiteDatabase = dbHelper.getWritableDatabase();        //データベースを操作するSQLiteDatabaseのインスタンスを生成する。
            //dbHelperのgetWritableDatabaseメソッドは読み書きできるインスタンスを生成できる
            //メモリが足りない場合はgetReadableDatabaseを使うと読み取り専用のインスタンスを生成できる

            singleItemDAO = new SingleItemDAO(sqLiteDatabase);      //SingleItemDAOのインスタンスを生成する(データベースを操作するために分けたクラス)

            multiItemDAO = new MultiItemDAO(sqLiteDatabase);        //MultiItemDAOのインスタンスを生成する(データベースを操作するために分けたクラス)


        }

    }

    @Override
    //requestPermission()からの結果を受け取る関数
    public void onRequestPermissionsResult(
            int requestCode, @NonNull String[] permissions, @NonNull int[] grantResults) {
        if (requestCode == 1000) {
            // 使用が許可された
            if (grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                Log.d("debug", "checkSelfPermission true");

                locationStart();

            } else {
                // それでも拒否された時の対応
                Toast toast = Toast.makeText(this,
                        "これ以上なにもできません", Toast.LENGTH_SHORT);
                toast.show();
            }
        }
    }

    private void locationStart() {

        // LocationManager インスタンス生成
        locationManager =
                (LocationManager) getSystemService(LOCATION_SERVICE);


        if (locationManager != null && locationManager.isProviderEnabled(LocationManager.GPS_PROVIDER)) {   //GPSの使用が許可されているとき

        } else {
            // GPSを設定するように促す
            Intent settingsIntent =
                    new Intent(Settings.ACTION_LOCATION_SOURCE_SETTINGS);
            startActivity(settingsIntent);

        }


        if (ContextCompat.checkSelfPermission(this,
                Manifest.permission.ACCESS_FINE_LOCATION) !=
                PackageManager.PERMISSION_GRANTED) {
            ActivityCompat.requestPermissions(this,
                    new String[]{Manifest.permission.ACCESS_FINE_LOCATION,}, 1000);

            return;
        }

        locationManager.requestLocationUpdates(LocationManager.GPS_PROVIDER,
                50, 5, listener);
        locationManager.requestLocationUpdates(LocationManager.NETWORK_PROVIDER, 50, 5, listener);

    }

    protected void onStop() {
        super.onStop();

        locationManager.removeUpdates(listener);
    }


    private final LocationListener listener = new LocationListener() {              //座標取得のためのリスナーを配置

        @Override
        public void onStatusChanged(String provider, int status, Bundle extras) {   //座標が変化したときに呼ばれる
            Log.v("location","start");
            switch (status) {
                case LocationProvider.AVAILABLE:
                    Log.d("debug", "LocationProvider.AVAILABLE");
                    break;
                case LocationProvider.OUT_OF_SERVICE:
                    Log.d("debug", "LocationProvider.OUT_OF_SERVICE");
                    break;
                case LocationProvider.TEMPORARILY_UNAVAILABLE:
                    Log.d("debug", "LocationProvider.TEMPORARILY_UNAVAILABLE");
                    break;
            }
        }

        //座標の値が変わったときに呼ばれるメソッド
        @Override
        public void onLocationChanged(final android.location.Location location) {


            //呼ばれた時の緯度を取得して追加
            lat.add(String.valueOf(location.getLatitude()));

            //呼ばれた時の経度を取得して追加
            lng.add(String.valueOf(location.getLongitude()));

            //呼ばれた時の時刻を取得して追加
            time.add(getSendingTimeDate());


            if(isFirst) {       //一番最初に呼ばれた場合（出発時）

                soundPool.play(mp3a, 1f, 1f, 0, 0, 1f);         //座標取得が完了した合図を出すための音声を再生する

                isFirst = false;
            }

            TextView textView = (TextView)findViewById(R.id.location_view);
            textView.setText("座標取得完了");




            //到着ボタンが押された場合の処理
            Button button2 = findViewById(R.id.location_stop_btn);
            button2.setOnClickListener(new View.OnClickListener() {
                @Override
                public void onClick(View v) {


                    //到着時点の緯度を取得して追加
                    lat.add(String.valueOf(location.getLatitude()));


                    //到着時点の経度を取得して追加
                    lng.add(String.valueOf(location.getLongitude()));


                    //到着時点の時刻を取得して追加
                    time.add(getSendingTimeDate());

                    writeDatabase(arrayText[0], Integer.parseInt(arrayText[1]), date[2], date[0], date[1]);

                    //トーストの表示
                    Context context = getApplicationContext();
                    Toast.makeText(context, "データ登録完了", Toast.LENGTH_SHORT).show();


                    onStop();

                    //このActivityを終了させる
                    finish();
                }
            });
        }


        @Override
        public void onProviderEnabled(String provider) {

        }

        @Override
        public void onProviderDisabled(String provider) {

        }
    };



    //到着ボタンを押されると呼ばれるメソッド
    private void writeDatabase (String gen, Integer age, String day, String year, String month) {


        int dataCount = singleItemDAO.countData() + 1;          //現在保管されているデータの個数からこのデータが何番目のデータなのかを指定する

        String llt = "llt" + dataCount;

        singleItemDAO.insert(dataCount,age,gen,year,month,day,llt);

        for (int i = 0; i < lat.size(); i++) {
            multiItemDAO.insert(i + 1,llt,lat.get(i),lng.get(i),time.get(i));       //記憶されている座標データすべてを挿入する
        }
    }


    //日付取得のためのメソッド）
    public static String getNowDate(){
        final DateFormat df = new SimpleDateFormat("yyyy/MM/dd");       //「年/月/日」の形式で取得する
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }

    //時刻取得のためのメソッド
    public static String getSendingTimeDate(){
        final DateFormat df = new SimpleDateFormat("HH:mm");            //「時/分」の形式で取得する
        final Date date = new Date(System.currentTimeMillis());
        return df.format(date);
    }
}
