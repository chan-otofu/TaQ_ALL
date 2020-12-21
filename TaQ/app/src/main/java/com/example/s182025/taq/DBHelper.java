package com.example.s182025.taq;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

//データベース作成、スキーマのバージョン管理等をするクラスです
//このクラスのインスタンスを生成することでデータベースの作成、テーブルの生成ができる。

//データベース、テーブルの構成を決める

//DBHelper 変数名 = new DBHelper(this);
//(SQLiteDatabase) = (DBHelper).getWritableDatabaseをコール (()内は変数名)
//getWritableDatabaseをコールすることでonCreate,onUpgradeが呼び出される
//何かしらのタイミング（画面遷移やアプリの終了時など）で、データベースはclose()する必要がある

public class DBHelper extends SQLiteOpenHelper {

    //データベース名
    private static final String DB_NAME = "MapDB";
    //データベースバージョン
    private static final int DB_VERSION = 1;


    //コンストラクタ
    public DBHelper(Context context) {

        //スーパークラスのコンストラクタを呼び出すと、データベースの存在チェックが行われる
        //存在しない場合はデータベース生成後にonCreate()メソッドが呼び出される
        //super(context,name,factory,version)
        //contextはDBを作る場所みたいなもの。 this を指定
        super(context,DB_NAME,null,DB_VERSION);

    }


    @Override
    public void onCreate(SQLiteDatabase db) {

        //複数のテーブルを作成するためのメソッド。SQL文を実行




        //SingleItemテーブルを作成するSQL文をtableCreateSQL1に格納
        String tableCreateSQL1 = "create table SingleItem(" +
                "_count integer primary key," +     //データの順番を格納する_count列。主キーとして設定

                "_age integer not null," +          //年齢を格納する_age列

                "_gender text not null," +          //性別を格納する_gender列

                "_year text not null," +            //年を格納する_year列

                "_month text not null," +           //月を格納する_month列

                "_day text not null," +             //日を格納する_day列

                "_llt text not null );";            //MultiItemテーブルに格納されているデータを取得するための_llt列


        //MultiItemテーブルを作成するSQL文をtableCreateSQL2に格納
        String tableCreateSQL2 = "create table MultiItem(" +

                "_count integer not null," +        //_llt列に格納されているデータ内での順番を格納する_count列。複合キーとして設定

                "_llt text not null," +             //_llt

                "_lat text not null," +             //緯度を格納する_lat列

                "_lng text not null," +             //経度を格納する_lng列

                "_time text not null," +            //時間を格納する_time列

                "PRIMARY KEY (_count, _llt));";     //_count列と_llt列を複合キーとして指定



        //CREATE TABLE文の実行
        db.execSQL(tableCreateSQL1); //実行するSQL文を実引数に指定する
        db.execSQL(tableCreateSQL2); //実行するSQL文を実引数に指定する


    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        //データベースのバージョンアップ処理
        //実際にはデータの退避や再構築が必要だが今は省略

    }


}
