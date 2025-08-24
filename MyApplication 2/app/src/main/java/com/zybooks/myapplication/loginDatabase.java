package com.zybooks.myapplication;

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

public class loginDatabase extends SQLiteOpenHelper {

    private static final String DATABASE_NAME = "login.db";
    private static final int VERSION = 1;

    public loginDatabase(Context context) {
        super(context, DATABASE_NAME, null, VERSION);
    }

    private static final class loginTable {
        private static final String TABLE = "appUsers";
        private static final String COL_ID = "_id";
        private static final String COL_username = "Username";
        private static final String COL_password = "password";
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("create table " + loginTable.TABLE + " (" +
                loginTable.COL_ID + " integer primary key autoincrement, " +
                loginTable.COL_username + " text, " +
                loginTable.COL_password + " text " +
                ")");

        // create admin account
        ContentValues values = new ContentValues();
        values.put(loginTable.COL_username, "admin");
        values.put(loginTable.COL_password, "1234");
        db.insert(loginTable.TABLE, null, values);

    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion,
                          int newVersion) {
        db.execSQL("drop table if exists " + loginTable.TABLE);
        onCreate(db);
    }

    // add new users
    public void addUser(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(loginTable.COL_username, username);
        values.put(loginTable.COL_password, password);

        db.insert(loginTable.TABLE, null, values);
    }

    // update username
    public void updateUser(String oldUsername, String username) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(loginTable.COL_username, username);
        db.update(loginTable.TABLE, values, loginTable.COL_username + " = ?", new String[]{oldUsername});
    }

    // update password
    public void updatePassword(String username, String password) {
        SQLiteDatabase db = getWritableDatabase();
        ContentValues values = new ContentValues();
        values.put(loginTable.COL_password, password);
        db.update(loginTable.TABLE, values, loginTable.COL_username + " = ?", new String[]{username});
    }

    // delete users
    public void deleteUser(String username) {
        SQLiteDatabase db = getWritableDatabase();
        db.delete(loginTable.TABLE, loginTable.COL_username + " = ?", new String[]{username});
    }

    // query database
    private boolean queryDatabase(String selection, String[] selectionArgs) {
        SQLiteDatabase db = getReadableDatabase();
        String[] columns = {loginTable.COL_ID};

        // check if query exist
        Cursor cursor = db.query(loginTable.TABLE, columns, selection, selectionArgs, null, null, null);
        boolean exists = cursor.getCount() > 0;
        db.close();
        cursor.close();
        return exists;
    }
    public boolean checkUsername(String username) {

        // query for username
        String selection = loginTable.COL_username + " = ?";
        String[] selectionArgs = {username};

        return queryDatabase(selection, selectionArgs);
    }

    //validate login
    public boolean validateLogin(String username, String password) {

        // query for username and password
        String selection = loginTable.COL_username + " = ? AND " + loginTable.COL_password + " = ?";
        String[] selectionArgs = {username, password};

        return queryDatabase(selection, selectionArgs);
    }


}
