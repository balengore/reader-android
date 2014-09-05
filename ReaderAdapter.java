package com.example.balen.reader;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Bitmap;
import android.graphics.BitmapFactory;
import android.media.Image;
import android.net.Uri;
import android.os.AsyncTask;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.ListView;

import com.example.balen.reader.data.LinkContract;

import java.io.InputStream;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * {@link ReaderAdapter} exposes a list of links
 * from a {@link Cursor} to a {@link ListView}.
 */
public class ReaderAdapter extends CursorAdapter {

  public ReaderAdapter(Context context, Cursor c, int flags) {
    super(context, c, flags);
  }

  @Override
  public View newView(Context context, Cursor cursor, ViewGroup parent) {
    View view = LayoutInflater.from(context).inflate(R.layout.list_item_link, parent, false);
    ViewHolder viewHolder = new ViewHolder(view);
    view.setTag(viewHolder);
    return view;
  }

  @Override
  public void bindView(View view, Context context, Cursor cursor) {

    ViewHolder viewHolder = (ViewHolder) view.getTag();
    int serverId = cursor.getInt(ReaderMainFragment.COL_LINK_SERVER_ID);
    int clientId = cursor.getInt(ReaderMainFragment.COL_LINK_TABLE_ID);

    viewHolder.titleView.setText(cursor.getString(ReaderMainFragment.COL_TITLE));
//    viewHolder.hrefView.setText(cursor.getString(ReaderMainFragment.COL_HREF));
    String date = cursor.getString(ReaderMainFragment.COL_PUBLISHED_AT);
    if(date == null) {
      date = cursor.getString(ReaderMainFragment.COL_CREATED_AT);
    }
    viewHolder.dateView.setText(Utility.formatServerDate(date));

    String articleImage = cursor.getString(ReaderMainFragment.COL_IMAGE_URL);
//    Log.v("BUILDING VIEW", "article image: " + articleImage);
    if(articleImage != null) {
//      viewHolder.imageView.setImageURI(Uri.parse(articleImage));
      new DownloadImageTask(viewHolder.imageView).execute(articleImage);
    }

    viewHolder.snippetView.setText(cursor.getString(ReaderMainFragment.COL_SNIPPET));
    String publisher = cursor.getString(ReaderMainFragment.COL_PUBLISHER);
//    Log.v("ReaderAdaper", "publisher is " + publisher);
    if(publisher == null || publisher.equals("null")) {
      String href = cursor.getString(ReaderMainFragment.COL_HREF);
//      Log.v("ReaderAdaper", "href is " + href);

      String patternStr = "http:\\/\\/(.*)\\/";
      Pattern p = Pattern.compile(patternStr);
      Matcher m = p.matcher(href);
      if(m.matches()) {
        publisher = m.group(1);
      } else {
        publisher = href.split("/")[2];
      }
    }
    viewHolder.publisherView.setText(publisher);
    viewHolder.idsView.setText("Server: " + serverId + ", Client: " + clientId);

    viewHolder.deleteBottomView.setVisibility(View.INVISIBLE);
    viewHolder.deleteTopView.setVisibility(View.INVISIBLE);

    String sourceString = cursor.getString(ReaderMainFragment.COL_SOURCE);
    if(sourceString != null && sourceString.startsWith("fb")) {
      String[] sourceParts = sourceString.split("::");
      String friendName = sourceParts[1];
      String friendId = sourceParts[2];
      viewHolder.sourceView.setText(friendName);
      new DownloadImageTask(viewHolder.friendImageView).execute("https://graph.facebook.com/" + friendId + "/picture?type=large");
    }
  }

  public static class ViewHolder {
//    public final TextView hrefView;
    public final TextView titleView;
    public final TextView dateView;
    public final ImageView imageView;
    public final TextView snippetView;
    public final TextView sourceView;
    public final ImageView friendImageView;
    public final TextView publisherView;
    public final TextView idsView;
    public final TextView deleteTopView;
    public final TextView deleteBottomView;


    public ViewHolder(View view) {
      titleView = (TextView) view.findViewById(R.id.list_item_title_textview);
//      hrefView = (TextView) view.findViewById(R.id.list_item_href_textview);
      dateView = (TextView) view.findViewById(R.id.list_item_date_textview);
      imageView = (ImageView) view.findViewById(R.id.list_item_icon);
      snippetView = (TextView) view.findViewById(R.id.list_item_snippet_textview);
      sourceView = (TextView) view.findViewById(R.id.list_item_source_textview);
      friendImageView = (ImageView) view.findViewById(R.id.list_item_friend_imageview);
      publisherView = (TextView) view.findViewById(R.id.list_item_publisher_textview);
      idsView = (TextView) view.findViewById(R.id.list_item_ids_textview);
      deleteTopView = (TextView) view.findViewById(R.id.list_item_deletetop_textview);
      deleteBottomView = (TextView) view.findViewById(R.id.list_item_deletebottom_textview);
    }
  }

  private class DownloadImageTask extends AsyncTask<String, Void, Bitmap> {
    ImageView bmImage;

    public DownloadImageTask(ImageView bmImage) {
      this.bmImage = bmImage;
    }

    protected Bitmap doInBackground(String... urls) {
      String urldisplay = urls[0];
      Bitmap mIcon11 = null;
      try {
        InputStream in = new java.net.URL(urldisplay).openStream();
        mIcon11 = BitmapFactory.decodeStream(in);
      } catch (Exception e) {
        Log.e("Error", e.getMessage());
        e.printStackTrace();
      }
      return mIcon11;
    }

    protected void onPostExecute(Bitmap result) {
      bmImage.setImageBitmap(result);
    }
  }
}
