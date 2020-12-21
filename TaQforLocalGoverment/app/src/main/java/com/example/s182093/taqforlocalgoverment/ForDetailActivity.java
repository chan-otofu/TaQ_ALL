package com.example.s182093.taqforlocalgoverment;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;
import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;
import java.util.Calendar;

public class ForDetailActivity extends AppCompatActivity implements View.OnClickListener {

    String texts;
    private FirebaseDatabase database;
    ArrayList<String> result = new ArrayList<>();
    //cl1：期間絞り込みの開始年月を格納する用
    Calendar cl1 = Calendar.getInstance();
    //cl2：期間絞り込みの終了年月を格納する用
    Calendar cl2 = Calendar.getInstance();
    //cl3：検索されるデータを格納してcl1以上cl2以下であるかを判定する用
    Calendar cl3 = Calendar.getInstance();
    String[] anser = new String[4];
    ArrayList<String> toSendMap = new ArrayList<>();
    String send = "";


    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_for_detail);
        super.onCreate(savedInstanceState);

        Button btn = (Button) findViewById(R.id.hogehoge);
        btn.setOnClickListener(this);

        Intent intent = this.getIntent();
        anser = intent.getStringArrayExtra("search");
        //受け取った西暦及び月をCalendar型に変換
        cl1.set(Calendar.YEAR, Integer.parseInt(anser[0]));
        cl1.set(Calendar.MONTH, Integer.parseInt(anser[1]));
        //anser[1]に開始月が格納されているのでその月の最終日を判断し付加
        if (Integer.parseInt(anser[1]) == 2) {
            cl1.set(Calendar.DATE, 28);
        } else if (Integer.parseInt(anser[1]) == 4 || Integer.parseInt(anser[1]) == 6 || Integer.parseInt(anser[1]) == 9 || Integer.parseInt(anser[1]) == 11) {
            cl1.set(Calendar.DATE, 30);
        } else {
            cl1.set(Calendar.DATE, 31);
        }
        //上と一緒　終了版
        cl2.set(Calendar.YEAR, Integer.parseInt(anser[2]));
        cl2.set(Calendar.MONTH, Integer.parseInt(anser[3]));
        if (Integer.parseInt(anser[3]) == 2) {
            cl2.set(Calendar.DATE, 28);
        } else if (Integer.parseInt(anser[3]) == 4 || Integer.parseInt(anser[3]) == 6 || Integer.parseInt(anser[3]) == 9 || Integer.parseInt(anser[3]) == 11) {
            cl2.set(Calendar.DATE, 30);
        } else {
            cl2.set(Calendar.DATE, 31);
        }

        Log.v("tyui1", String.valueOf(cl1));
        Log.v("tyui2", String.valueOf(cl2));

        //データベースにFairebase定義
        database = FirebaseDatabase.getInstance();
        //firebase「info」の値取得
        DatabaseReference user = database.getReference("info");
        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot Snapshot) {
                int leng = (int) Snapshot.getChildrenCount();
                int i, j;

                String res = "";
                String res2 = "";
                String resF = "";


                for (i = 1; i < leng; i++) {
                    String year = (String) Snapshot.child(String.valueOf(i) + "/year").getValue();
                    String month = (String) Snapshot.child(String.valueOf(i) + "/month").getValue();
                    String day = (String) Snapshot.child(String.valueOf(i) + "/day").getValue();

                    //当該データの年月日を取得
                    cl3.set(Integer.parseInt(year), Integer.parseInt(month), Integer.parseInt(day));

                    //X.compareTo（Y）でXはYより前か後かを判断できる
                    int diff1 = cl1.compareTo(cl3);
                    int diff2 = cl2.compareTo(cl3);

                    Log.v("kkkkk1", String.valueOf(diff1));
                    Log.v("kkkkk2", String.valueOf(diff2));

                    if (diff1 <= 0 && diff2 >= 0) {         //cl1～cl2であるかの判断
                        long ageS = (long) Snapshot.child(String.valueOf(i) + "/age").getValue();
                        String genS = (String) Snapshot.child(String.valueOf(i) + "/gen").getValue();
                        String yearS = (String) Snapshot.child(String.valueOf(i) + "/year").getValue();
                        String monthS = (String) Snapshot.child(String.valueOf(i) + "/month").getValue();
                        String dayS = (String) Snapshot.child(String.valueOf(i) + "/day").getValue();
                        //取得した年月日、年齢、性別を結合①
                        res = "記録年月日 : "+yearS+"/"+monthS+"/"+dayS+"\n年齢："+ageS+"歳　　性別："+genS+"\n記録データ\n";
                        //lat,lng,timeの子供（child）の数を把握したい
                        //3項目ともが対応しているため個数は変わらないからlatを代表値としてroopに代入
                        int roop = (int) Snapshot.child(String.valueOf(i) + "/lat").getChildrenCount();
                        //roop回取得
                        for (j = 1; j < roop; j++) {
                            String lng = (String) Snapshot.child(String.valueOf(i) + "/lng/" + String.valueOf(j)).getValue();
                            String lat = (String) Snapshot.child(String.valueOf(i) + "/lat/" + String.valueOf(j)).getValue();
                            String time = (String) Snapshot.child(String.valueOf(i) + "/time/" + String.valueOf(j)).getValue();
                            //1人あたりの座標、時間を結合②
                            res2 += "lng" + j + ":" + lng + "/lat" + j + ":" + lat + "\t\t記録時間" + j + ":" + time + "\n";
                            //MapsActivityのフォーマットに合わせる
                            send = lat + "," + lng;
                            //フォーマットを合わせた上でtoSendMapに付加
                            toSendMap.add(send);
                        }
                        //一件の終端に「END」を付加（MapsActivity上のフォーマット）
                        toSendMap.add("END");
                        //①+②を結合して1人当たりの全データをresF代入
                        resF = res + res2;
                        //上記をArraylistに付加
                        result.add(resF);
                        res2 = "";
                        resF = "";
                    }
                }
            }

            public void onCancelled(@NonNull DatabaseError Error) {

            }
        });


        ListView listView = (ListView) findViewById(R.id.listView);

        // simple_list_item_1 は、 もともと用意されている定義済みのレイアウトファイルのID
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, result);

        listView.setAdapter(arrayAdapter);
    }

    @Override
    public void onClick(View v) {
        if (v.getId() == R.id.hogehoge) {
            Intent intent = new Intent(v.getContext(), MapsActivity.class);
            intent.putStringArrayListExtra("watasityauyo", toSendMap);
            startActivity(intent);
        }
    }
}