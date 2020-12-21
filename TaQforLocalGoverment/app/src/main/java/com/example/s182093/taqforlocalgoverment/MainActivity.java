package com.example.s182093.taqforlocalgoverment;

import android.content.Intent;
import android.support.annotation.NonNull;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;

import com.google.firebase.database.DataSnapshot;
import com.google.firebase.database.DatabaseError;
import com.google.firebase.database.DatabaseReference;
import com.google.firebase.database.FirebaseDatabase;
import com.google.firebase.database.ValueEventListener;

import java.util.ArrayList;


public class MainActivity extends AppCompatActivity {

    private FirebaseDatabase database;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_main);

        //結びつけ
        final TextView year = (TextView)findViewById(R.id.yearText);
        final TextView yearplus=(TextView)findViewById(R.id.yearText2);
        final TextView month=(TextView)findViewById(R.id.yearText3);
        final TextView forStyear=(TextView)findViewById(R.id.yearText4);
        final TextView forStmonth=(TextView)findViewById(R.id.yearText5);
        final TextView forFinyear=(TextView)findViewById(R.id.yearText6);
        final TextView forFinmonth=(TextView)findViewById(R.id.yearText7);
        //全データ表示用のArraylist「alldata」
        final ArrayList <String> alldata=new ArrayList<>();
        //MapsActivityへ遷移するときに持つべきデータの収集は
        //DataindicationのみMainActivity上で実行しているため遷移ようのArraylist「alldata」を用意
        final ArrayList <String> sendMap=new ArrayList<>();



        //firebase
        database = FirebaseDatabase.getInstance();
        //firebaseのinfoの項目を取得
        DatabaseReference user = database.getReference("info");
        user.addValueEventListener(new ValueEventListener() {
            @Override
            public void onDataChange(@NonNull DataSnapshot Snapshot) {

                int i,j;
                String res="";
                String res2="";
                String unk="";

                int leng = (int) Snapshot.getChildrenCount();
                Log.v("TESTs1",String.valueOf(leng));
                //ユーザーのデータを入れる配列「Person」
                String Person[]=new String[leng];


                for(i=1;i<leng;i++){                //入れ子のないデータ収集後、Personに順に格納
                    long age = (long) Snapshot.child(String.valueOf(i)+"/age").getValue();
                    String gen = (String) Snapshot.child(String.valueOf(i)+"/gen").getValue();
                    String year = (String) Snapshot.child(String.valueOf(i)+"/year").getValue();
                    String month = (String) Snapshot.child(String.valueOf(i)+"/month").getValue();
                    String day = (String) Snapshot.child(String.valueOf(i)+"/day").getValue();
                    res = "記録年月日 : "+year+"/"+month+"/"+day+"\n年齢："+age+"歳　　性別："+gen+"\n記録データ\n";
                    Person[i-1]=res;
                }


                for(i=1;i<leng;i++){               ////入れ子ありデータ収集
                    int roop = (int) Snapshot.child(String.valueOf(i)+"/lat").getChildrenCount();
                    for(j=1;j<roop;j++){
                        String lng = (String) Snapshot.child(String.valueOf(i)+"/lng/"+String.valueOf(j)).getValue();
                        String lat = (String) Snapshot.child(String.valueOf(i)+"/lat/"+String.valueOf(j)).getValue();
                        String time = (String) Snapshot.child(String.valueOf(i)+"/time/"+String.valueOf(j)).getValue();
                        res2 += "lng"+j+":"+lng+"/lat"+j+":"+lat+"\t\t記録時間"+j+":"+time+"\n";
                        unk=lat+","+lng;
                        sendMap.add(unk);
                    }
                    //MapsActivity上のフォーマットに合わせて一件ずつの終端に「END」
                    sendMap.add("END");
                    Person[i-1] += res2;
                    res2="";
                }

                for(i=0;i<Person.length-1;i++){       //配列の渡し方よくわからんかったからArraylistに変えた
                    Log.v("unk2",Person[i]);
                    alldata.add(Person[i]);
                }
            }

            @Override
            public void onCancelled(@NonNull DatabaseError databaseError) {
            }
        });

        //全データの表示「Dataindication」へ遷移
        findViewById(R.id.databutton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v){
                Intent intent =new Intent(v.getContext(),Dataindication.class);
                //表示用
                intent.putStringArrayListExtra("seni",alldata);
                //MapsActivity上のフォーマットに合わせたやーつ
                intent.putStringArrayListExtra("seni2",sendMap);
                startActivity(intent);
            }
        });

        //西暦で絞り込む「SortActivity」に遷移
        findViewById(R.id.searchbutton).setOnClickListener(new View.OnClickListener(){
            @Override
            public void onClick(View v) {
                String s;
                //入力された西暦をsに格納後遷移
                s=year.getText().toString();
                Intent intent = new Intent(v.getContext(), SortActivity.class);
                intent.putExtra("search", s);
                Log.v("hjkl",s);
                startActivity(intent);
            }
        });

        //西暦、月で絞り込む「DatailSort」に遷移
        findViewById(R.id.searchbutton2).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //入力された西暦及び月を各々格納後遷移
                String[] watasuyo=new String[2];
                watasuyo[0]=yearplus.getText().toString();
                watasuyo[1]=month.getText().toString();

                Intent intent = new Intent(v.getContext(),DetailSort.class);
                intent.putExtra("search",watasuyo);
                startActivity(intent);
            }
        });

        //期間で絞り込む「ForDetailActivity」に遷移
        findViewById(R.id.searchbutton3).setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                //上とほぼ一緒
                String[] watasuyo2=new String[4];
                watasuyo2[0]=forStyear.getText().toString();
                watasuyo2[1]=forStmonth.getText().toString();
                watasuyo2[2]=forFinyear.getText().toString();
                watasuyo2[3]=forFinmonth.getText().toString();

                Intent intent = new Intent(v.getContext(),ForDetailActivity.class);
                intent.putExtra("search",watasuyo2);
                startActivity(intent);
            }
        });
    }
}