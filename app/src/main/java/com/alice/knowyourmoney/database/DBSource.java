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
    private String[] allBudgetColumns = {DBAdapter.COLUMN_ID, DBAdapter.COLUMN_NUMBER,
            DBAdapter.COLUMN_NAME, DBAdapter.COLUMN_DUE};

    public DBSource(Context context){
        Log.d("alice_debug", "I am in the DBSource stucturer");
        dbContext = new DBContext(context);
        dbAdp = new DBAdapter(dbContext);
    }

    public void Open() throws SQLException {
        //database = SQLiteDatabase.openOrCreateDatabase()
        database = dbAdp.getWritableDatabase();
        Log.d("alice_debug", "I am out of DB open");
    }

    public void Close(){
        dbAdp.close();
    }

    public BudgetComment CreateBudget(String name, int due, int number){
        ContentValues value = new ContentValues();
        value.put(DBAdapter.COLUMN_NUMBER, number);
        value.put(DBAdapter.COLUMN_NAME, name);
        value.put(DBAdapter.COLUMN_DUE, due);

        long insertId = database.insert(DBAdapter.TABLE_BUDGET, null, value);
        Cursor cursor = database.query(DBAdapter.TABLE_BUDGET, allBudgetColumns,
                DBAdapter.COLUMN_ID + "=" + insertId, null, null, null, null);
        cursor.moveToFirst();
        Log.d("alice", "Done for Create Name");
        return cursorToBudgetComment(cursor);
    }

    public void DeleteBudget(BudgetComment comment){
        long id = comment.getId();
        System.out.println("Comment delete with ID : " + id);
        database.delete(DBAdapter.TABLE_BUDGET, DBAdapter.COLUMN_ID + "=" + id, null);
    }

    public List<BudgetComment> getAllBudget(){
        List<BudgetComment> comments = new ArrayList<BudgetComment>();
        Cursor cursor = database.query(DBAdapter.TABLE_BUDGET, allBudgetColumns, null,
                null, null, null, null);
        cursor.moveToFirst();
        while(!cursor.isAfterLast()){
            BudgetComment comment = cursorToBudgetComment(cursor);
            comments.add(comment);
            cursor.moveToNext();
        }
        cursor.close();
        return comments;
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
        Log.d("alice", "Done for Create Account");
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
        System.out.println("Comment delete with ID : " + id);
        database.delete(DBAdapter.TABLE_ACCOUNT, DBAdapter.COLUMN_ID + "=" + id, null);
    }

    private BudgetComment cursorToBudgetComment(Cursor cursor) {

        BudgetComment comment = new BudgetComment();
        comment.setId(cursor.getLong(0));
        comment.setNumber(cursor.getInt(1));
        comment.setName(cursor.getString(2));
        comment.setDue(cursor.getInt(3));
        return comment;
    }

    private AccountComment cursorToAccountComment(Cursor cursor) throws ParseException {
        AccountComment comment = new AccountComment();
        comment.setId(cursor.getLong(0));
        comment.setDate(cursor.getString(1));
        comment.setReason(cursor.getString(2));
        comment.setPrice(cursor.getInt(3));
        return comment;
    }
}
