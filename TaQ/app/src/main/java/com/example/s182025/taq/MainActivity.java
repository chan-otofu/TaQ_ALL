package com.example.s182025.taq;

import android.content.Context;
import android.content.Intent;
import android.database.sqlite.SQLiteDatabase;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.os.Handler;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Toast;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;
import com.google.zxing.integration.android.IntentIntegrator;
import com.google.zxing.integration.android.IntentResult;

import java.util.ArrayList;

public class MainActivity extends AppCompatActivity implements View.OnClickListener{

    SQLiteDatabase sqLiteDatabase;
    DBHelper dbHelper;
    SingleItemDAO singleItemDAO;
    MultiItemDAO multiItemDAO;

    boolean isLoop = true;      //onDataChangeはデータが変更されるたびに呼ばれてしまうので最後の処理を終えても一度だけ余分に呼ばれてしまう。
                                //それを無視するための変数

    int fbCount;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);


        findViewById(R.id.main_start_btn).setOnClickListener(this);
        findViewById(R.id.main_send_btn).setOnClickListener(this);


        if (dbHelper == null) {                      //dbHelperにインスタンスが入っているか判断
            dbHelper = new DBHelper(this); //DBHelperクラスのインスタンスを生成(DBHelperクラスはデータベースを生成する、
        }                                             // SQLiteOpenHelperクラスを継承している)

        if (sqLiteDatabase == null) {                          //sqLiteDatabaseにインスタンスが入っているか判断

            sqLiteDatabase = dbHelper.getWritableDatabase(); //データベースを操作するSQLiteDatabaseのインスタンスを生成する。
            //dbHelperのgetWritableDatabaseメソッドは読み書きできるインスタンスを生成できる
            //メモリが足りない場合はgetReadableDatabaseを使うと読み取り専用のインスタンスを生成できる

            singleItemDAO = new SingleItemDAO(sqLiteDatabase);    //SingleItemDAOのインスタンスを生成する(データベースを操作するために分けたクラス)

            multiItemDAO = new MultiItemDAO(sqLiteDatabase);       //SingleItemDAOのインスタンスを生成する(データベースを操作するために分けたクラス)


        }

    }

    //Firebaseへ「User]を登録するユーザ定義クラス
    public static class User {
        public String gen;      //性別
        public Integer age;     //年齢
        public String day;      //日
        public String year;     //年
        public String month;    //月

        public User(String _gen, Integer _age, String _day, String _year, String _month) {
            gen = _gen;
            age = _age;
            day = _day;
            year = _year;
            month = _month;

        }
    }

    private void writeNewUser() {
        //Firebase ファイルパスを指定してリファレンスを取得
        if(netWorkCheck(this.getApplicationContext())) {        //ネットワークに接続されているか
            if (singleItemDAO.countData() != 0) {               //SingleItemDatabaseの中身がゼロではないか

                final FirebaseDatabase database = FirebaseDatabase.getInstance();                 //firebaseインスタンスを生成

                //参照先を指定するための変数宣言
                final DatabaseReference refName = database.getReference("info");
                final DatabaseReference refSec = database.getReference("info/count");


                isLoop = true;

                //データの送信回数を記憶しておくための変数
                fbCount = 1;



                refSec.addValueEventListener(new ValueEventListener() {             //指定した参照先にリスナーを配置
                    @Override
                    public void onDataChange(@NonNull DataSnapshot dataSnapshot) {  //このメソッドは、アタッチされた場合か、
                                                                                    // 参照先のパス全体のどれかに変更が加わった場合に実行される
                        if (isLoop) {
                            int countNum = 1;

                            //firebaseに保管されているデータの個数を取得
                            if (dataSnapshot.getValue() != null) {      //データがない場合は１つ目のデータとして設定する
                                countNum = Integer.parseInt(String.valueOf(dataSnapshot.getValue())) + 1;       //取得したデータの個数の次の番号として保存する
                            }


                            //firebaseに保存するデータをdatabaseから読み取る処理
                            SingleItemDAO singleDAO = singleItemDAO.findData(fbCount);

                            MultiItemDAO multiDAO = multiItemDAO.findData(singleDAO.sendLLT);

                            ArrayList<String> lat = multiDAO.lat;

                            ArrayList<String> lng = multiDAO.lng;

                            ArrayList<String> time = multiDAO.times;

                            final User user = new User(singleDAO.sendGender, singleDAO.sendAge,
                                    singleDAO.sendDay, singleDAO.sendYear,
                                    singleDAO.sendMonth);

                            String sendCount = String.valueOf(countNum);

                            //個数が一つだけのデータを保存する処理
                            refName.child(sendCount).setValue(user);


                            //個数が複数のデータを保存する処理
                            for (int j = 0; j < lat.size(); j++) {
                                refName.child(sendCount).child("time").child(String.valueOf(j + 1)).setValue(String.valueOf(time.get(j)));
                                refName.child(sendCount).child("lat").child(String.valueOf(j + 1)).setValue(String.valueOf(lat.get(j)));
                                refName.child(sendCount).child("lng").child(String.valueOf(j + 1)).setValue(String.valueOf(lng.get(j)));
                            }

                            //データの個数を更新
                            refSec.setValue(countNum);

                        }
                        if (fbCount == singleItemDAO.countData()) {         //処理を実行した回数が、
                                                                            //SingleItemDBに保存されているデータの個数と等しいならば
                                                                            //上記の条件を満たすとき、DB内のデータをすべて送信し終わったことを意味する

                            //二つのDB内のデータをすべて削除する処理
                            sqLiteDatabase.delete("SingleItem", null, null);
                            sqLiteDatabase.delete("MultiItem", null, null);

                            //すべてのデータを送信し終わったのでもう処理が走らないようにする
                            isLoop = false;


                            Toast toast = Toast.makeText(MainActivity.this, "データ送信完了", Toast.LENGTH_SHORT);
                            toast.show();
                        }

                        fbCount++;

                    }

                    @Override
                    public void onCancelled(@NonNull DatabaseError databaseError) {

                    }
                });


            } else {
                Toast toast = Toast.makeText(this, "データが登録されていません", Toast.LENGTH_SHORT);
                toast.show();
            }
        } else {
            Toast toast = Toast.makeText(this,"ネットワークに接続されていません",Toast.LENGTH_SHORT);
            toast.show();
        }

    }


    @Override
    public void onClick(View v) {
        if(v.getId() == R.id.main_start_btn) {

            scanBarcode();

        }
        if(v.getId() == R.id.main_send_btn) {

            writeNewUser();
        }
    }



    //QRコードリーダーを呼び出すための処理
    public void scanBarcode() {

        IntentIntegrator intentIntegrator = new IntentIntegrator(this);
        intentIntegrator.setOrientationLocked(false);
        intentIntegrator.initiateScan();

    }


    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {

        IntentResult result = IntentIntegrator.parseActivityResult(requestCode, resultCode, data);
        if(result != null) {
            if(result.getContents() == null) {
                Log.d("MainActivity", "Cancelled scan");
                Toast.makeText(this, "Cancelled", Toast.LENGTH_LONG).show();
            } else {
                Log.d("MainActivity", "Scanned");
                Intent intent = new Intent(this, Location.class);
                intent.putExtra("sendText",result.getContents());
                startActivity(intent);
            }
        } else {
            super.onActivityResult(requestCode, resultCode, data);
        }
    }


    //ネットワークに接続されてどうか判断するメソッド
    public static boolean netWorkCheck(Context context){
        ConnectivityManager cm =  (ConnectivityManager)context.getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo info = cm.getActiveNetworkInfo();
        if( info != null ){
            return true;
        } else {
            return false;
        }
    }
}
