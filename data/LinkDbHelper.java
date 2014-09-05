package com.example.balen.reader.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import static com.example.balen.reader.data.LinkContract.LinkEntry;

/**
 * Created by balen on 8/7/14.
 */
public class LinkDbHelper extends SQLiteOpenHelper {

  private static final int DATABASE_VERSION = 1;
  public static final String DATABASE_NAME = "link.db";

  public LinkDbHelper(Context context) {
    super(context, DATABASE_NAME, null, DATABASE_VERSION);
  }

  @Override
  public void onCreate(SQLiteDatabase db) {

    final String SQL_CREATE_LINK_TABLE = "CREATE TABLE " + LinkEntry.TABLE_NAME + " (" +
        LinkEntry._ID + " INTEGER PRIMARY KEY," +
        LinkEntry.COLUMN_LINK_SERVER_ID + " INTEGER UNIQUE ON CONFLICT IGNORE NOT NULL, " +
        LinkEntry.COLUMN_HREF + " TEXT UNIQUE ON CONFLICT IGNORE NOT NULL, " +
        LinkEntry.COLUMN_TITLE + " TEXT NOT NULL, " +
        LinkEntry.COLUMN_HTML + " TEXT NOT NULL, " +
        LinkEntry.COLUMN_CREATED_AT + " TEXT NOT NULL," +
        LinkEntry.COLUMN_IMAGE_URL + " TEXT, " +
        LinkEntry.COLUMN_SNIPPET + " TEXT, " +
        LinkEntry.COLUMN_SOURCE + " TEXT, " +
        LinkEntry.COLUMN_PUBLISHER + " TEXT," +
        LinkEntry.COLUMN_STATS + " TEXT," +
        LinkEntry.COLUMN_PUBLISHED_AT + " TEXT);";

    db.execSQL(SQL_CREATE_LINK_TABLE);
  }

  @Override
  public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
    db.execSQL("DROP TABLE IF EXISTS " + LinkEntry.TABLE_NAME);
    onCreate(db);
  }
}
