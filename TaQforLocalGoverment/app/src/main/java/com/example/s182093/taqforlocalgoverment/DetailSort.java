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

public class DetailSort extends AppCompatActivity implements View.OnClickListener {

    //データベース定義
    private FirebaseDatabase database;
    //表示する用
    ArrayList<String> result=new ArrayList<>();
    //西暦及び月を受け取る用
    String[] anser=new String[2];
    //MapsActivityに送る用
    ArrayList<String> toSendMap=new ArrayList<>();
    //MainActivityのフォーマット用
    String send="";

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_detail_sort);
        super.onCreate(savedInstanceState);

        Button btn =(Button)findViewById(R.id.foo);
        btn.setOnClickListener(this);

        Intent intent =this.getIntent();
        //受け取ったデータをanser格納
        anser = intent.getStringArrayExtra("search");
        Log.v("yuio", String.valueOf(anser[0]));
        Log.v("yuio", String.valueOf(anser[1]));

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
                    String month = (String)Snapshot.child(String.valueOf(i)+"/month").getValue();
                    Log.v("hjkl1",String.valueOf(year));
                    if(String.valueOf(year).equals(String.valueOf(anser[0])) && String.valueOf(month).equals(String.valueOf(anser[1]))){
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
                            res2 += "lng"+j+":"+lng+"/lat"+j+":"+lat+"\t\t記録時間"+j+":"+time+"\n";
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
            }
            public void onCancelled(@NonNull DatabaseError Error){

            }
        });


        ListView listView =(ListView)findViewById(R.id.listView);

        // simple_list_item_1 は、 もともと用意されている定義済みのレイアウトファイルのID
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, result);

        listView.setAdapter(arrayAdapter);
    }
    @Override
    public void onClick(View v) {
        if(v.getId()==R.id.foo) {
            Intent intent = new Intent(v.getContext(), MapsActivity.class);
            intent.putStringArrayListExtra("watasityauyo", toSendMap);
            startActivity(intent);
        }
    }
}