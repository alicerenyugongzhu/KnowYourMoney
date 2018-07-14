package com.alice.knowyourmoney.database;

import android.content.Context;
import android.content.ContextWrapper;
import android.database.DatabaseErrorHandler;
import android.database.sqlite.SQLiteDatabase;
import android.util.Log;

import java.io.File;
import java.io.IOException;

/**
 * Created by alice on 2016/6/18.
 */
public class DBContext extends ContextWrapper {

    public DBContext(Context base) {
        super(base);
        Log.d("alice_debug", "I am in the DBContext");
    }

    @Override
    public File getDatabasePath(String name) {
        File dbFile = null;
        boolean sdExist = android.os.Environment.MEDIA_MOUNTED.equals(android.os.Environment.getExternalStorageState());
        if (!sdExist) {
            Log.d("alice_debug", "No external storage");
            return null;
            //dbFile = new File(getBaseContext().getFilesDir(), name);

        } else {
            String dbDir = android.os.Environment.getExternalStorageDirectory().getAbsolutePath() + "knowyourmoney";
            //String dbDir = "/data/data/com.alice.knowyourmoney";//TODO: need enhance
            dbDir += "/database";
            String dbPath = dbDir + "/" + name;
            Log.d("alice_debug", "dir is " + dbDir);
            Log.d("alice_debug", "path is " + dbPath);

            File dirFile = new File(dbDir);
            if (!dirFile.exists()) {
                Log.d("alice_debug", "no database dir available");
                dirFile.mkdirs();
            }
            dbFile = new File(dbPath);
            //Log.d("alice_deubg", "external saving, name is " + name);
            //dbFile = new File(getBaseContext().getExternalFilesDir(""), name);
        }
        boolean isFileCreateSuccess = false;
        if (!dbFile.exists()) {
            Log.d("alice_debug", "no database path available");
            try {
                isFileCreateSuccess = dbFile.createNewFile();
            } catch (IOException e) {
                // TODO Auto-generated catch block
                Log.d("alice_debug", "cannot create new file");
                e.printStackTrace();
            }
        } else {
            Log.d("alice_debug", "the flie is available");
            isFileCreateSuccess = true;
        }
        if(isFileCreateSuccess)
                return dbFile;
            else
                return null;
    }

    @Override
    public SQLiteDatabase openOrCreateDatabase(String name, int mode,
                                               SQLiteDatabase.CursorFactory factory) {
        Log.d("alice_debug", "I am in the Database Context,");
        SQLiteDatabase result = SQLiteDatabase.openOrCreateDatabase(getDatabasePath(name), null);
        return result;
    }

}
