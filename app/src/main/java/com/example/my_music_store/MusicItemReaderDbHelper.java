package com.example.my_music_store;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.provider.BaseColumns;

public class MusicItemReaderDbHelper extends SQLiteOpenHelper {
    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "MusicItemReader.db";

    static class MusicItemEntry implements BaseColumns {
        static final String TABLE_NAME = "music_shop_list";
        static final String COLUMN_NAME = "name";
    }

    private static final String SQL_CREATE_ENTRIES =
        "CREATE TABLE " + MusicItemEntry.TABLE_NAME + " (" +
            MusicItemEntry._ID + " INTEGER PRIMARY KEY," +
            MusicItemEntry.COLUMN_NAME + " TEXT)";

    private static final String SQL_DELETE_ENTRIES =
            "DROP TABLE IF EXISTS " + MusicItemEntry.TABLE_NAME;

    MusicItemReaderDbHelper(Context context) {
        super(context, DATABASE_NAME, null, DATABASE_VERSION);
    }
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(SQL_CREATE_ENTRIES);
    }
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL(SQL_DELETE_ENTRIES);
        onCreate(db);
    }
    public void onDowngrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        onUpgrade(db, oldVersion, newVersion);
    }
}
