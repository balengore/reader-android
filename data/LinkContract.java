package com.example.balen.reader.data;

import android.content.ContentUris;
import android.net.Uri;
import android.provider.BaseColumns;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Created by balen on 8/7/14.
 */
public class LinkContract {
  public static final String CONTENT_AUTHORITY = "com.example.balen.reader.app";
  public static final Uri BASE_CONTENT_URI = Uri.parse("content://" + CONTENT_AUTHORITY);
  public static final String PATH_LINKS = "link";

  public static final class LinkEntry implements BaseColumns {

    public static final String CONTENT_TYPE = "vnd.android.cursor.dir/" + CONTENT_AUTHORITY + "/" + PATH_LINKS;
    public static final String CONTENT_ITEM_TYPE = "vnd.android.cursor.item/" + CONTENT_AUTHORITY + "/" + PATH_LINKS;
    public static final Uri CONTENT_URI = BASE_CONTENT_URI.buildUpon().appendPath(PATH_LINKS).build();

    public static final String TABLE_NAME = "link";
    public static final String COLUMN_LINK_SERVER_ID = "link_server_id";
    public static final String COLUMN_HREF = "href";
    public static final String COLUMN_TITLE = "title";
    public static final String COLUMN_HTML = "html";
    public static final String COLUMN_CREATED_AT = "created_at";
    public static final String COLUMN_IMAGE_URL = "image_url";
    public static final String COLUMN_SNIPPET = "snippet";
    public static final String COLUMN_SOURCE = "source";
    public static final String COLUMN_PUBLISHER = "publisher";
    public static final String COLUMN_STATS = "stats";
    public static final String COLUMN_PUBLISHED_AT = "published_at";

    public static Uri buildLinkUri(long id) {
      return ContentUris.withAppendedId(CONTENT_URI, id);
    }

    public static Uri buildLinkWithHref(String href) {
      return CONTENT_URI.buildUpon().appendPath(href).build();
    }
//
    public static Uri buildLinksWithStartDate(String startDate) {
      return CONTENT_URI.buildUpon().appendQueryParameter(COLUMN_PUBLISHED_AT, startDate).build();
    }
//
//    public static Uri buildWeatherLocationWithDate(String locationSetting, String date) {
//      return CONTENT_URI.buildUpon().appendPath(locationSetting).appendPath(date).build();
//    }

    public static String getHrefFromUri(Uri uri) {
      return uri.getPathSegments().get(1);
    }
//
//    public static String getDateFromUri(Uri uri) {
//      return uri.getPathSegments().get(2);
//    }
//
    public static String getStartDateFromUri(Uri uri) {
      return uri.getQueryParameter(COLUMN_CREATED_AT);
    }
  }

  // Format used for storing dates in the database.  ALso used for converting those strings
  // back into date objects for comparison/processing.
  public static final String DATE_FORMAT = "yyyyMMdd";

  /**
   * Converts Date class to a string representation, used for easy comparison and database lookup.
   * @param date The input date
   * @return a DB-friendly representation of the date, using the format defined in DATE_FORMAT.
   */
  public static String getDbDateString(Date date){
    // Because the API returns a unix timestamp (measured in seconds),
    // it must be converted to milliseconds in order to be converted to valid date.
    SimpleDateFormat sdf = new SimpleDateFormat(DATE_FORMAT);
    return sdf.format(date);
  }

  /**
   * Converts a dateText to a long Unix time representation
   * @param dateText the input date string
   * @return the Date object
   */
  public static Date getDateFromDb(String dateText) {
    SimpleDateFormat dbDateFormat = new SimpleDateFormat(DATE_FORMAT);
    try {
      return dbDateFormat.parse(dateText);
    } catch (ParseException e) {
      e.printStackTrace();
      return null;
    }
  }

}
