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

public class SortActivity extends AppCompatActivity implements View.OnClickListener{

    String texts;
    private FirebaseDatabase database;
    //表示用
    ArrayList<String> result=new ArrayList<>();
    //地図上に表示する用
    ArrayList<String> toSendMap=new ArrayList<>();
    //MapsActivityにフォーマットを合わせるため定義
    String send="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_sort);
        super.onCreate(savedInstanceState);

        //結びつけ
        Button btn = (Button)findViewById(R.id.hoge);
        btn.setOnClickListener(this);

        //インテントの受け取り
        Intent intent =this.getIntent();
        texts = intent.getStringExtra("search");
        Log.v("hjkl",texts);

        //MainActivity同様firebase「info」の取得
        database = FirebaseDatabase.getInstance();
        DatabaseReference user = database.getReference("info");
        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot Snapshot) {
                int leng = (int) Snapshot.getChildrenCount();
                int i,j;
                String res="";
                String res2="";
                String resF="";


                for(i=1;i<leng;i++){
                    String year = (String) Snapshot.child(String.valueOf(i)+"/year").getValue();
                    Log.v("hjkl1",String.valueOf(year));
                    //MainActivityから受け取った西暦に引っ掛かったデータのみ各Listに格納
                    if(String.valueOf(year).equals(String.valueOf(texts))){
                        long ageS = (long) Snapshot.child(String.valueOf(i)+"/age").getValue();
                        String genS = (String) Snapshot.child(String.valueOf(i)+"/gen").getValue();
                        String yearS = (String) Snapshot.child(String.valueOf(i)+"/year").getValue();
                        String monthS = (String) Snapshot.child(String.valueOf(i)+"/month").getValue();
                        String dayS = (String) Snapshot.child(String.valueOf(i)+"/day").getValue();
                        res = "記録年月日 : "+yearS+"/"+monthS+"/"+dayS+"\n年齢："+ageS+"歳　　性別："+genS+"\n記録データ\n";

                        int roop = (int) Snapshot.child(String.valueOf(i)+"/lat").getChildrenCount();
                        for(j=1;j<roop;j++){
                            String lng = (String) Snapshot.child(String.valueOf(i)+"/lng/"+String.valueOf(j)).getValue();
                            String lat = (String) Snapshot.child(String.valueOf(i)+"/lat/"+String.valueOf(j)).getValue();
                            String time = (String) Snapshot.child(String.valueOf(i)+"/time/"+String.valueOf(j)).getValue();
                            res2 += "経度"+j+":"+lng+"                緯度"+j+":"+lat+"\t\t\t記録時間"+j+":"+time+"\n";
                            send=lat+","+lng;
                            toSendMap.add(send);
                        }
                        toSendMap.add("END");
                        resF=res+res2;
                        result.add(resF);
                        res2="";
                        resF="";
                    }
                }
                Log.v("asdfgh",String.valueOf(toSendMap));
            }
            public void onCancelled(@NonNull DatabaseError Error){

            }
        });
        //結びつけ
        ListView listView =(ListView)findViewById(R.id.listView);

        //ListViewに1データずつ格納
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, result);

        listView.setAdapter(arrayAdapter);
    }
    @Override
    //MapsActivityに遷移
    public void onClick(View v) {
        if(v.getId()==R.id.hoge) {
            Intent intent = new Intent(v.getContext(), MapsActivity.class);
            intent.putStringArrayListExtra("watasityauyo", toSendMap);
            startActivity(intent);
        }
    }
}