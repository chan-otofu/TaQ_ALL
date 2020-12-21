package com.example.s182025.taq;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;



import java.util.ArrayList;

//DAOクラスの中にDBの操作処理を一括して書きます
//テーブルのデータの挿入・更新はContentValuesクラスを使用する。

public class MultiItemDAO {

    //定数
    private static SQLiteDatabase db;

    //このブロック内で何回もテーブル名、列名を使用するため変数に格納して分かりやすくする------------
    //テーブル名
    private static final String TABLE_NAME = "MultiItem";
    //列名
    private static final String COLUMN_COUNT = "_count";
    private static final String COLUMN_LLT = "_llt";
    private static final String COLUMN_LAT = "_lat";
    private static final String COLUMN_LNG = "_lng";
    private static final String COLUMN_TIME = "_time";


    ArrayList<String> lat = new ArrayList<>();
    ArrayList<String> lng = new ArrayList<>();
    ArrayList<String> times = new ArrayList<>();

    //----------------------------------------------------------------------------------------------

    //コンストラクタ
    public MultiItemDAO(SQLiteDatabase db) {
        this.db = db;                              //このクラスにはTextEditActivityで生成したSQLiteDatabaseクラスのインスタンスがないためコンストラクタで受け取る
    }




    //データを取得するメソッド
    public MultiItemDAO findData(String num) { //仮引き数のnumは検索するメッセージの主キーが入る
        Cursor cursor = db.query(       //Cursorクラスのインスタンスを生成する(検索するテーブルの情報が格納される)
                //queryメソッドを使うと検索するテーブルを指定できる
                TABLE_NAME,            //検索するテーブル名
                new String[] {COLUMN_COUNT,COLUMN_LLT,COLUMN_LAT,COLUMN_LNG,COLUMN_TIME},//検索する列名をString配列で指定
                null,         //以下のコンストラクタに渡す値は、検索方法を表している。(whereやgroupByの役割)
                null,     //今回は使わないのでnullを渡す
                null,
                null,
                null
        );

        MultiItemDAO dao = new MultiItemDAO(db);

        cursor.moveToFirst();          //テーブルの探索を一番最初からするように設定する

        do{
            if (num.equals(cursor.getString(1))) {     //もし今選択している行の_llt列と、引数で渡された値が同じならブロック内の処理をする
                //getStringはString型のデータを取ってくる(実引数の0はそのString型のデータがその行の1番最初にあることを示している)
                dao.lat.add(cursor.getString(2));
                dao.lng.add(cursor.getString(3));
                dao.times.add(cursor.getString(4));
            }
        }while (cursor.moveToNext());                       //moveToNextメソッドは次の行があるかを真偽値で返し、あるのであれば移動する、つまり次の行がないのであれば、このWhileを抜ける
        //なぜdo Whileなのかというと、ただのWhileやforだと最初の判定の時にmoveToNextで次の行に移動してしまい、一番最初の行を参照できないからである

        cursor.close();                                     //探索したテーブルを閉じる

        return dao;

    }


    //データを追加するメソッド
    public void insert( int count, String llt, String lat, String lng, String time){
        ContentValues values = new ContentValues();
        values.put(COLUMN_COUNT,count);
        values.put(COLUMN_LLT,llt);
        values.put(COLUMN_LAT,lat);
        values.put(COLUMN_LNG,lng);
        values.put(COLUMN_TIME,time);

        db.insert(TABLE_NAME,null,values);      //insertメソッドは新しい行を追加してくれる
        //最後には追加するデータの準備をしたContentValuesクラスのインスタンスを指定する
    }

}

