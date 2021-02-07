package com.chat.wechat.Utils;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import androidx.annotation.Nullable;


public class DatabaseHandler extends SQLiteOpenHelper {

    //database-version
    public static final int DATABASE_VERSION = 1;
    //database-name
    public static final String DATABASE_NAME = "userdata.db";
    //table-name
    public static final String TABLE_NAME = "userlist";
    //table-fields
    public static final String COLUMN_ID = "id";
    public static final String COLUMN_NAME = "name";
    public static final String COLUMN_PHONE_NUMBER = "phone_number";
    public static final String COLUMN_USER_UID = "uid";
    public static final String COLUMN_PROFILE_PICTURE = "profile_picture";


    public DatabaseHandler(@Nullable Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL("CREATE TABLE "+TABLE_NAME +" ( "+ COLUMN_ID +" INTEGER PRIMARY KEY AUTOINCREMENT, "+ COLUMN_NAME +" TEXT,"+ COLUMN_USER_UID + " TEXT," + COLUMN_PHONE_NUMBER+" TEXT," + COLUMN_PROFILE_PICTURE+" TEXT "+ ")");
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    }
}
