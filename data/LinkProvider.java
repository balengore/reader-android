package com.example.balen.reader.data;

import android.content.ContentProvider;
import android.content.ContentUris;
import android.content.ContentValues;
import android.content.UriMatcher;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteQueryBuilder;
import android.net.Uri;

import static com.example.balen.reader.data.LinkContract.LinkEntry;

/**
 * Created by balen on 8/7/14.
 */
public class LinkProvider extends ContentProvider {

  private static final int LINKS = 100;
  private static final int LINK_WITH_HREF = 101;
//  private static final int WEATHER_WITH_LOCATION_AND_DATE = 102;
//  private static final int LOCATION = 300;
//  private static final int LOCATION_ID = 301;

  public static UriMatcher sUriMatcher = buildUriMatcher();
  private LinkDbHelper mOpenHelper;
  public static final SQLiteQueryBuilder sLinkQueryBuilder;

  static {
    sLinkQueryBuilder = new SQLiteQueryBuilder();
    sLinkQueryBuilder.setTables(LinkContract.LinkEntry.TABLE_NAME);
  }

  private static final String sLinkSelectionByHref =
      LinkEntry.TABLE_NAME + "." + LinkEntry.COLUMN_HREF + " = ?";

  private Cursor getLinkByHref(Uri uri, String[] projection, String sortOrder) {
    String href = LinkEntry.getHrefFromUri(uri);

    String selectionArgs[] = new String[]{href};
    String selection = sLinkSelectionByHref;

    return sLinkQueryBuilder.query(mOpenHelper.getReadableDatabase(),
        projection,
        selection,
        selectionArgs,
        null,
        null,
        sortOrder);
  }


  private static UriMatcher buildUriMatcher() {
    final UriMatcher matcher = new UriMatcher(UriMatcher.NO_MATCH);
    final String authority = LinkContract.CONTENT_AUTHORITY;

    matcher.addURI(authority, LinkContract.PATH_LINKS, LINKS);
    matcher.addURI(authority, LinkContract.PATH_LINKS + "/*", LINK_WITH_HREF);
//    matcher.addURI(authority, LinkContract.PATH_WEATHER + "/*/*", WEATHER_WITH_LOCATION_AND_DATE);
//    matcher.addURI(authority, LinkContract.PATH_LOCATION, LOCATION);
//    matcher.addURI(authority, LinkContract.PATH_LOCATION + "/#", LOCATION_ID);
    return matcher;
  }

  @Override
  public boolean onCreate() {
    mOpenHelper = new LinkDbHelper(getContext());
    return true;
  }

  @Override
  public Cursor query(Uri uri, String[] projection, String selection, String[] selectionArgs,
                      String sortOrder) {
    // Here's the switch statement that, given a URI, will determine what kind of request it is,
    // and query the database accordingly.
    Cursor retCursor;
    switch (sUriMatcher.match(uri)) {
      // "link"
      case LINKS:
      {
        retCursor = mOpenHelper.getReadableDatabase().query(
            LinkEntry.TABLE_NAME,
            projection,
            selection,
            selectionArgs,
            null,
            null,
            sortOrder
        );
        break;
      }
      // "link/*"
      case LINK_WITH_HREF: {
        retCursor = getLinkByHref(uri, projection, sortOrder);
        break;
      }

      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
    retCursor.setNotificationUri(getContext().getContentResolver(), uri);
    return retCursor;
  }

  @Override
  public String getType(Uri uri) {
    // Use the Uri Matcher to determine what kind of URI this is.
    final int match = sUriMatcher.match(uri);

    switch (match) {
      case LINK_WITH_HREF:
        return LinkEntry.CONTENT_ITEM_TYPE;
      case LINKS:
        return LinkEntry.CONTENT_TYPE;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
  }

  @Override
  public Uri insert(Uri uri, ContentValues values) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    Uri returnUri;

    switch (match) {
      case LINKS: {
        long _id = db.insert(LinkEntry.TABLE_NAME, null, values);
        if ( _id > 0 )
          returnUri = LinkEntry.buildLinkUri(_id);
        else
          throw new android.database.SQLException("Failed to insert row into " + uri);
        break;
      }
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
    getContext().getContentResolver().notifyChange(uri, null);
    return returnUri;
  }

  @Override
  public int delete(Uri uri, String selection, String[] selectionArgs) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    int rowsDeleted;
    switch (match) {
      case LINKS:
        rowsDeleted = db.delete(LinkEntry.TABLE_NAME, selection, selectionArgs);
        break;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
    // Because a null deletes all rows
    if (selection == null || rowsDeleted != 0) {
      getContext().getContentResolver().notifyChange(uri, null);
    }
    return rowsDeleted;
  }

  @Override
  public int update(Uri uri, ContentValues values, String selection, String[] selectionArgs) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    int rowsUpdated;

    switch (match) {
      case LINKS:
        rowsUpdated = db.update(LinkEntry.TABLE_NAME, values, selection,
            selectionArgs);
        break;
      default:
        throw new UnsupportedOperationException("Unknown uri: " + uri);
    }
    if (rowsUpdated != 0) {
      getContext().getContentResolver().notifyChange(uri, null);
    }
    return rowsUpdated;
  }

  @Override
  public int bulkInsert(Uri uri, ContentValues[] values) {
    final SQLiteDatabase db = mOpenHelper.getWritableDatabase();
    final int match = sUriMatcher.match(uri);
    switch (match) {
      case LINKS:
        db.beginTransaction();
        int returnCount = 0;
        try {
          for (ContentValues value : values) {
            long _id = db.insert(LinkEntry.TABLE_NAME, null, value);
            if (_id != -1) {
              returnCount++;
            }
          }
          db.setTransactionSuccessful();
        } finally {
          db.endTransaction();
        }
        getContext().getContentResolver().notifyChange(uri, null);
        return returnCount;
      default:
        return super.bulkInsert(uri, values);
    }
  }
}
