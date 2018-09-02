package com.alice.knowyourmoney.database;

import android.accounts.Account;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.SQLException;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.sql.Date;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.ConcurrentModificationException;
import java.util.List;

/**
 * Created by alice on 2016/6/18.
 */
public class DBSource {
    //Database part
    private SQLiteDatabase database;
    DBContext dbContext;
    private DBAdapter dbAdp;
    private String[] allAccountColumns = {DBAdapter.COLUMN_ID, DBAdapter.COLUMN_DATE,
            DBAdapter.COLUMN_REASON, DBAdapter.COLUMN_PRICE};
    private String[] allBudget = {DBAdapter.COLUMN_BUDGET};
    private String[] allLeft = {DBAdapter.COLUMN_LEFT};

    public DBSource(Context context){
        Log.d("alice_debug", "I am in the DBSource stucturer");
        dbContext = new DBContext(context);
        dbAdp = new DBAdapter(dbContext);
    }

    public void Open() throws SQLException {
        //database = SQLiteDatabase.openOrCreateDatabase();
        database = dbAdp.getWritableDatabase();
        Log.d("alice_debug", "I am out of DB open");
    }

    public void Close(){
        dbAdp.close();
    }

     public AccountComment CreateAccount(String date, String reason, float price) throws ParseException {
        ContentValues value = new ContentValues();
        value.put(DBAdapter.COLUMN_DATE, date);
        value.put(DBAdapter.COLUMN_REASON, reason);
        value.put(DBAdapter.COLUMN_PRICE, price);

        long insertId = database.insert(DBAdapter.TABLE_ACCOUNT, null, value);
        Cursor cursor = database.query(DBAdapter.TABLE_ACCOUNT, allAccountColumns,
                DBAdapter.COLUMN_ID + "=" + insertId, null, null, null, null);
        cursor.moveToFirst();
        Log.d("alice_debug", "Done for Create Account");
        return cursorToAccountComment(cursor);
    }

    public List<AccountComment> getAllAccount() throws ParseException {
        List<AccountComment> comments = new ArrayList<AccountComment>();
        Cursor cursor = database.query(DBAdapter.TABLE_ACCOUNT, allAccountColumns, null,
                null, null, null, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            AccountComment comment = cursorToAccountComment(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        cursor.close();
        return comments;
    }


    public void DeleteAccount(AccountComment comment){
        long id = comment.getId();
        Log.d("alice_debug","Comment delete with ID : " + id);

        database.delete(DBAdapter.TABLE_ACCOUNT, DBAdapter.COLUMN_ID + "=" + id, null);
    }

    private AccountComment cursorToAccountComment(Cursor cursor) throws ParseException {
        AccountComment comment = new AccountComment();
        comment.setId(cursor.getLong(0));
        comment.setDate(cursor.getString(1));
        comment.setReason(cursor.getString(2));
        comment.setPrice(cursor.getFloat(3));
        return comment;
    }

    public void UpdateAccount(AccountComment comment) throws ParseException {
        long id = comment.getId();
        Log.d("alice_debug", "Item need update with ID : " + id);
        ContentValues cv = new ContentValues();
        cv.put(DBAdapter.COLUMN_DATE, comment.getDate());
        cv.put(DBAdapter.COLUMN_PRICE, comment.getPrice());
        cv.put(DBAdapter.COLUMN_REASON, comment.getReason());
        database.update(DBAdapter.TABLE_ACCOUNT, cv, DBAdapter.COLUMN_ID + "=" + id, null);
        //Cursor cursor = database.query(DBAdapter.TABLE_ACCOUNT, allAccountColumns,
        //        DBAdapter.COLUMN_ID + "=" + id, null, null, null, null);
        //cursor.moveToFirst();
        Log.d("alice", "Done for Create Account");
        //return cursorToAccountComment(cursor);
    }

    public float GetLeft(String firstDay){
        Cursor cursor = database.rawQuery("select sum(price) from Account where record_date>=" + firstDay, null);
        cursor.moveToFirst();
        return cursor.getFloat(0);
    }


}
