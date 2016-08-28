package com.alice.knowyourmoney.database;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.graphics.Matrix;
import android.util.Log;

/**
 * Created by alice on 2016/6/18.
 */
public class DBAdapter extends SQLiteOpenHelper{

    public static final String TABLE_ACCOUNT = "Account";  //database name without .db
    public static final String COLUMN_ID = "_id";  //column _id
    public static final String COLUMN_DATE = "record_date";  //column year
    public static final String COLUMN_REASON = "reason";
    public static final String COLUMN_PRICE = "price";

    public static final String TABLE_BUDGET = "Budget";  //databas
    public static final String COLUMN_NUMBER = "number";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_DUE = "due";

    public static final String DATABASE_NAME = "MyAccount";
    public static final int DATABASE_VERSION = 1;

    //SQL command
    public static final String DATABASE_ACCOUNT_CREATE = "CREATE TABLE " + TABLE_ACCOUNT +
            "(" + COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_DATE + " text not null, " +
            COLUMN_REASON + " text not null, " +
            COLUMN_PRICE + " float not null" +
            ");";
    public static final String DATABASE_BUDGET_CREATE = "CREATE TABLE " + TABLE_BUDGET +
            "(" + COLUMN_ID + " integer primary key autoincrement, " +
            COLUMN_NUMBER + " integer not null, " +
            COLUMN_NAME + " text not null, " +
            COLUMN_DUE + " integer not null" +
            ");";

    public DBAdapter(Context context, String name, SQLiteDatabase.CursorFactory factory, int version) {
        super(context, name, factory, version);
    }

    public DBAdapter(Context context){
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
        Log.d("alice_debug", "I am in the DBAdapter");
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        Log.d("alice_debug", "I am in the Database create function");
        Log.d("alice_debug", DATABASE_ACCOUNT_CREATE);
        db.execSQL(DATABASE_ACCOUNT_CREATE);
        Log.d("alice_debug", "craete budget");
        Log.d("alice_debug", DATABASE_BUDGET_CREATE);
        db.execSQL(DATABASE_BUDGET_CREATE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {

        Log.d("alice_debug", "I am in the upgrate function");
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_ACCOUNT);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BUDGET);
        onCreate(db);
    }
}
