package com.example.s182025.taq;


import android.content.ContentValues;
import android.database.Cursor;
import android.database.DatabaseUtils;
import android.database.sqlite.SQLiteDatabase;

//DAOクラスの中にDBの操作処理を一括して書きます
//テーブルのデータの挿入・更新はContentValuesクラスを使用する。

public class SingleItemDAO {

    //定数
    private static SQLiteDatabase db;

    //このブロック内で何回もテーブル名、列名を使用するため変数に格納して分かりやすくする------------
    //テーブル名
    private static final String TABLE_NAME = "SingleItem";

    //列名
    private static final String COLUMN_COUNT = "_count";
    private static final String COLUMN_AGE = "_age";
    private static final String COLUMN_GENDER = "_gender";
    private static final String COLUMN_YEAR = "_year";
    private static final String COLUMN_MONTH = "_month";
    private static final String COLUMN_DAY = "_day";
    private static final String COLUMN_LLT = "_llt";

    //データを取得する際に、クラス型で返すので、そのための変数を宣言。
    int sendAge = 0;
    String sendGender = "";
    String sendYear = "";
    String sendMonth = "";
    String sendDay = "";
    String sendLLT = "";

    //----------------------------------------------------------------------------------------------

    //コンストラクタ
    public SingleItemDAO(SQLiteDatabase db) {
        this.db = db;                              //このクラスにはTSQLiteDatabaseクラスのインスタンスがないためコンストラクタで受け取る
    }

    //テーブル内のデータの個数を調べるためのメソッド
    public int countData() {

        //queryNumEntriesメソッドでデータの個数を取得するが、long型で帰ってくるためint型に変化してcountingに代入
        int counting = Integer.parseInt(String.valueOf(DatabaseUtils.queryNumEntries(db,TABLE_NAME)));

        return counting;

    }


    //データを取得するメソッド
    public SingleItemDAO findData(int num) { //仮引き数のnumは検索するメッセージの主キーが入る
        Cursor cursor = db.query(       //Cursorクラスのインスタンスを生成する(検索するテーブルの情報が格納される)
                //queryメソッドを使うと検索するテーブルを指定できる

                TABLE_NAME,            //検索するテーブル名

                new String[] {COLUMN_COUNT,COLUMN_AGE,COLUMN_GENDER,COLUMN_YEAR,COLUMN_MONTH,COLUMN_DAY,COLUMN_LLT},//検索する列名をString配列で指定
                null,         //以下のコンストラクタに渡す値は、検索方法を表している。(whereやgroupByの役割)
                null,     //今回は使わないのでnullを渡す
                null,
                null,
                null
        );

        SingleItemDAO dao = new SingleItemDAO(db);

        cursor.moveToFirst();          //テーブルの探索を一番最初からするように設定する

        do{
            if (num == cursor.getInt(0)) {     //もし今選択している行の主キーと、引数で渡された主キーが同じならブロック内の処理をする
                //getIntはInt型のデータを取ってくる(実引数の0はそのInt型のデータがその行の1番最初にあることを示している)


                dao.sendAge = cursor.getInt(1);
                dao.sendGender = cursor.getString(2);
                dao.sendYear = cursor.getString(3);
                dao.sendMonth = cursor.getString(4);
                dao.sendDay = cursor.getString(5);
                dao.sendLLT = cursor.getString(6);

                return dao;

            }
        }while (cursor.moveToNext());                       //moveToNextメソッドは次の行があるかを真偽値で返し、あるのであれば移動する、つまり次の行がないのであれば、このWhileを抜ける
        //なぜdo Whileなのかというと、ただのWhileやforだと最初の判定の時にmoveToNextで次の行に移動してしまい、一番最初の行を参照できないからである

        cursor.close();                                     //探索したテーブルを閉じる

        return dao;

    }


    //データを追加するメソッド
    public void insert( int count, int age, String gender, String year, String month, String day, String llt){

        ContentValues values = new ContentValues();

        values.put(COLUMN_COUNT,count);
        values.put(COLUMN_AGE,age);
        values.put(COLUMN_GENDER,gender);
        values.put(COLUMN_YEAR,year);
        values.put(COLUMN_MONTH,month);
        values.put(COLUMN_DAY,day);
        values.put(COLUMN_LLT,llt);

        db.insert(TABLE_NAME,null,values);      //insertメソッドは新しい行を追加してくれる
        //最後には追加するデータの準備をしたContentValuesクラスのインスタンスを指定する
    }


}
