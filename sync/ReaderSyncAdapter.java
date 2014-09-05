package com.example.balen.reader.sync;

import android.accounts.Account;
import android.accounts.AccountManager;
import android.app.Notification;
import android.app.NotificationManager;
import android.app.PendingIntent;
import android.app.TaskStackBuilder;
import android.content.AbstractThreadedSyncAdapter;
import android.content.ContentProviderClient;
import android.content.ContentResolver;
import android.content.ContentValues;
import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.content.SyncRequest;
import android.content.SyncResult;
import android.database.Cursor;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;

import com.example.balen.reader.MainActivity;
import com.example.balen.reader.R;

import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Vector;

import static com.example.balen.reader.data.LinkContract.LinkEntry;

/**
 * Created by balen on 8/11/14.
 */
public class ReaderSyncAdapter extends AbstractThreadedSyncAdapter {

  public final String LOG_TAG = ReaderSyncAdapter.class.getSimpleName();
  public static final int SYNC_INTERVAL = 60 * 180;
  public static final int SYNC_FLEXTIME = SYNC_INTERVAL / 3;

  private static final String[] NOTIFY_LINK_PROJECTION = new String[]{
      LinkEntry.COLUMN_HREF,
      LinkEntry.COLUMN_TITLE,
      LinkEntry.COLUMN_HTML
  };

  private static final int INDEX_HREF = 0;
  private static final int INDEX_TITLE= 1;
  private static final int INDEX_HTML = 2;

  private static final long DAY_IN_MILLIS = 1000 * 60 * 60 * 24;
  private static final long HOUR_IN_MILLIS = 1000 * 60 * 60;
  private static final int LINK_NOTIFICATION_ID = 3004;

  public ReaderSyncAdapter(Context context, boolean autoInitialize) {
    super(context, autoInitialize);
  }

  public void notifyNewArticlesDownloaded() {
    Log.v(LOG_TAG, "IN NOTIFY HI");
    Context context = getContext();
    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(context);
    String displayNotificationsKey = context.getString(R.string.pref_enable_notifications_key);
//    boolean displayNotifications = prefs.getBoolean(displayNotificationsKey, Boolean.parseBoolean(context.getString(R.string.pref_enable_notifications_default)));
    boolean displayNotifications = true;
    if(displayNotifications) {
      String lastNotificationKey = context.getString(R.string.pref_last_notification);
      long lastSync = prefs.getLong(lastNotificationKey, 0);
      Log.v(LOG_TAG, "Last sync was at: " + lastSync);
      Log.v(LOG_TAG, "Sync diff is: " + (System.currentTimeMillis() - lastSync));

//      if(System.currentTimeMillis() - lastSync >= HOUR_IN_MILLIS) {
        if(true) {
        Uri linkUri = LinkEntry.CONTENT_URI;
        Cursor cursor = context.getContentResolver().query(linkUri, NOTIFY_LINK_PROJECTION, null, null, null);
        if(cursor.moveToFirst()) {
          String title = context.getString(R.string.app_name);
          String article_title = cursor.getString(INDEX_TITLE);
          String contentText = String.format(context.getString(R.string.format_notification), article_title);

          Notification.Builder builder = new Notification.Builder(getContext())
              .setSmallIcon(R.drawable.ic_launcher)
              .setContentTitle(title)
              .setContentText(contentText)
              .setAutoCancel(true);

          Log.v("ReaderSyncAdapter", "NOTIFICATION: " + contentText);

          if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.GINGERBREAD) {
            Intent resultIntent = new Intent(context, MainActivity.class);
            TaskStackBuilder stackBuilder = TaskStackBuilder.create(context);
            stackBuilder.addNextIntent(resultIntent);
            PendingIntent resultPendingIntent = stackBuilder.getPendingIntent(0, PendingIntent.FLAG_UPDATE_CURRENT);
            builder.setContentIntent(resultPendingIntent);

            NotificationManager notificationManager = (NotificationManager) getContext().getSystemService(Context.NOTIFICATION_SERVICE);
            notificationManager.notify(LINK_NOTIFICATION_ID, builder.build());
          } else {
            Log.v("ReaderSyncAdapter", "NO NOTIFICATION, build version is: " + Build.VERSION.SDK_INT);
          }

          SharedPreferences.Editor editor = prefs.edit();
          editor.putLong(lastNotificationKey, System.currentTimeMillis());
          editor.commit();
        }
      }
    }

  }

  public static void syncImmediately(Context context) {
    Bundle bundle = new Bundle();
    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_EXPEDITED, true);
    bundle.putBoolean(ContentResolver.SYNC_EXTRAS_MANUAL, true);
    ContentResolver.requestSync(getSyncAccount(context), context.getString(R.string.content_authority), bundle);
  }

  public static void configurePeriodicSync(Context context, int syncInterval, int flexTime) {
    Account account = getSyncAccount(context);
    String authority = context.getString(R.string.content_authority);
    if(Build.VERSION.SDK_INT >= Build.VERSION_CODES.KITKAT) {
      SyncRequest request = new SyncRequest.Builder().syncPeriodic(syncInterval, flexTime).setSyncAdapter(account, authority).build();
      ContentResolver.requestSync(request);
    } else {
      ContentResolver.addPeriodicSync(account, authority, new Bundle(), syncInterval);
    }
  }

  public static Account getSyncAccount(Context context) {
    AccountManager accountManager = (AccountManager) context.getSystemService(Context.ACCOUNT_SERVICE);

    Account newAccount = new Account(context.getString(R.string.app_name), context.getString(R.string.sync_account_type));

    // If the password doesn't exist, the account doesn't exist
    if (accountManager.getPassword(newAccount) == null) {
      if (!accountManager.addAccountExplicitly(newAccount, "", null)) {
        return null;
      }

            /* If you don't set android:syncable="true" in your <provider> element in the manifest
             * then call context.setIsSyncable(account, AUTHORITY) here
             */
      onAccountCreated(newAccount, context);
    }

    ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
    return newAccount;
  }

  @Override
  public void onPerformSync(Account account, Bundle extras, String authority, ContentProviderClient provider, SyncResult syncResult) {
    //Only sync on wifi
    ConnectivityManager connectivityManager = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
    NetworkInfo networkInfo = connectivityManager.getActiveNetworkInfo();

    if(networkInfo.getType() == ConnectivityManager.TYPE_WIFI) {
      Log.v(LOG_TAG, "USING WIFI - Will do the sync");
    } else {
      Log.v(LOG_TAG, "Not on WIFI - not syncing");
      return;
    }

    // These two need to be declared outside the try/catch
    // so that they can be closed in the finally block.
    HttpURLConnection urlConnection = null;
    BufferedReader reader = null;

    // Will contain the raw JSON response as a string.
    String responseJsonStr = null;

    try {
        final String FORECAST_BASE_URL = "http://boiling-waters-7748.herokuapp.com/linksjson";


      Uri builtUri = Uri.parse(FORECAST_BASE_URL).buildUpon().build();
      URL url = new URL(builtUri.toString());

      urlConnection = (HttpURLConnection) url.openConnection();
      urlConnection.setRequestMethod("GET");
      urlConnection.connect();

      // Read the input stream into a String
      InputStream inputStream = urlConnection.getInputStream();
      StringBuffer buffer = new StringBuffer();
      if (inputStream == null) {
        // Nothing to do.
        return;
      }
      reader = new BufferedReader(new InputStreamReader(inputStream));

      String line;
      while ((line = reader.readLine()) != null) {
        // Since it's JSON, adding a newline isn't necessary (it won't affect parsing)
        // But it does make debugging a *lot* easier if you print out the completed
        // buffer for debugging.
        buffer.append(line + "\n");
      }

      if (buffer.length() == 0) {
        // Stream was empty.  No point in parsing.
        return;
      }
      responseJsonStr = buffer.toString();
    } catch (IOException e) {
      Log.e(LOG_TAG, "Error ", e);
      Log.e(LOG_TAG, e.getMessage());
      System.err.println(e);
      e.printStackTrace();
      // If the code didn't successfully get the data, there's no point in attemping
      // to parse it.
      return;
    } finally {
      if (urlConnection != null) {
        urlConnection.disconnect();
      }
      if (reader != null) {
        try {
          reader.close();
        } catch (final IOException e) {
          Log.e(LOG_TAG, "Error closing stream", e);
        }
      }
    }

    try {
      getLinkDataFromJson(responseJsonStr);
      notifyNewArticlesDownloaded();
    } catch (JSONException e) {
      Log.e(LOG_TAG, e.getMessage(), e);
      e.printStackTrace();
    }

  }

  private void getLinkDataFromJson(String responseJsonStr)
      throws JSONException {

    JSONArray linkArray = new JSONArray(responseJsonStr);

    // Get and insert the new weather information into the database
    Vector<ContentValues> cVVector = new Vector<ContentValues>(linkArray.length());

    for(int i = 0; i < linkArray.length(); i++) {
      // These are the values that will be collected.

      // Get the JSON object representing the day
      JSONObject link = linkArray.getJSONObject(i);

      ContentValues linkValues = new ContentValues();
      linkValues.put(LinkEntry.COLUMN_LINK_SERVER_ID, link.getInt("id"));
      linkValues.put(LinkEntry.COLUMN_HREF, link.getString("href"));
      linkValues.put(LinkEntry.COLUMN_TITLE, link.getString("title"));
      linkValues.put(LinkEntry.COLUMN_HTML, link.getString("html"));
      linkValues.put(LinkEntry.COLUMN_CREATED_AT, link.getString("created_at"));
      linkValues.put(LinkEntry.COLUMN_IMAGE_URL, link.getString("image_url"));
      linkValues.put(LinkEntry.COLUMN_SNIPPET, link.getString("snippet"));
      linkValues.put(LinkEntry.COLUMN_SOURCE, link.getString("source"));
      linkValues.put(LinkEntry.COLUMN_PUBLISHER, link.getString("publisher"));
      linkValues.put(LinkEntry.COLUMN_STATS, link.getString("stats"));
      linkValues.put(LinkEntry.COLUMN_PUBLISHED_AT, link.getString("published_at"));

      cVVector.add(linkValues);
    }
    if(cVVector.size() > 0) {
      ContentValues[] cvArray = new ContentValues[cVVector.size()];
      cVVector.toArray(cvArray);
      getContext().getContentResolver().bulkInsert(LinkEntry.CONTENT_URI, cvArray);

      // Delete old data
//      Calendar cal = Calendar.getInstance();
//      cal.add(Calendar.DATE, -1);
//      String yesterdayDate = LinkContract.getDbDateString(cal.getTime());
//      getContext().getContentResolver().delete(LinkEntry.CONTENT_URI, LinkEntry.COLUMN_DATETEXT + " <= ?", new String[] {yesterdayDate});
    }
  }


  public static void initializeSyncAdapter(Context context) {
    getSyncAccount(context);
  }

  private static void onAccountCreated(Account newAccount, Context context) {
    ReaderSyncAdapter.configurePeriodicSync(context, SYNC_INTERVAL, SYNC_FLEXTIME);
    ContentResolver.setSyncAutomatically(newAccount, context.getString(R.string.content_authority), true);
    syncImmediately(context);
  }
}
