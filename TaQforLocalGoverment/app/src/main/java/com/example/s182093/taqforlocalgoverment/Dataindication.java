package com.example.s182093.taqforlocalgoverment;

import android.content.Intent;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.view.View;
import android.widget.ArrayAdapter;
import android.widget.Button;
import android.widget.ListView;

import java.util.ArrayList;

public class Dataindication extends AppCompatActivity implements View.OnClickListener{

    //受け取る用のArraylist定義
    ArrayList<String> texts=new ArrayList<>();
    ArrayList<String> toSendMap=new ArrayList<>();

    @Override
    public void onCreate(Bundle savedInstanceState) {
        setContentView(R.layout.activity_dataindication);
        super.onCreate(savedInstanceState);
        Button btn=(Button)findViewById(R.id.foo_bar);
        btn.setOnClickListener(this);
        //受け取り
        Intent intent =this.getIntent();
        //受け取ったデータを各Arraylistに格納
        texts = intent.getStringArrayListExtra("seni");
        toSendMap = intent.getStringArrayListExtra("seni2");

        //結び付け
        ListView listView=(ListView)findViewById(R.id.listView);

        //ListViewに1つずつ格納
        ArrayAdapter<String> arrayAdapter =
                new ArrayAdapter<>(this, android.R.layout.simple_list_item_1, texts);

        listView.setAdapter(arrayAdapter);
    }
    @Override
    //MapsActivityに遷移
    public void onClick(View v) {
        if (v.getId() == R.id.foo_bar) {
            Intent intent = new Intent(v.getContext(), MapsActivity.class);
            intent.putStringArrayListExtra("watasityauyo", toSendMap);
            startActivity(intent);
        }
    }
}