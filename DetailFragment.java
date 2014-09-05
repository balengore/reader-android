package com.example.balen.reader;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Intent;
import android.content.Loader;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.webkit.WebView;
import android.widget.ImageView;
import android.widget.ShareActionProvider;
import android.widget.TextView;

import com.example.balen.reader.data.LinkContract;

/**
 * Created by balen on 8/8/14.
 */
public class DetailFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
  private static final String LOG_TAG = DetailFragment.class.getSimpleName();
  private static final int DETAIL_LOADER = 0;
  public static final String HREF_KEY = "href";

  private String mLinkStr;
  private WebView mWebView;
  private String mHrefStr;

  public DetailFragment() {
    setHasOptionsMenu(true);
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container,
                           Bundle savedInstanceState) {
    Bundle args = getArguments();
    if(args != null) {
      mHrefStr = args.getString(HREF_KEY);
    }

    Log.v(LOG_TAG, "ON CREATE VIEW: " + mHrefStr);

    View rootView = inflater.inflate(R.layout.fragment_detail, container, false);
    mWebView = (WebView) rootView.findViewById(R.id.webview);
    return rootView;
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater menuInflater) {
    // Inflate the menu; this adds items to the action bar if it is present.
    menuInflater.inflate(R.menu.detailfragment, menu);
    MenuItem menuItem = menu.findItem(R.id.action_share);
    if(menuItem != null) {
      ShareActionProvider mShareActionProvider = (ShareActionProvider) menuItem.getActionProvider();
      if(mShareActionProvider != null) {
        mShareActionProvider.setShareIntent(createShareForecastIntent());
      }
    }
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);

    if(null != savedInstanceState) {
      mHrefStr = savedInstanceState.getString(HREF_KEY);
    }

    getLoaderManager().initLoader(DETAIL_LOADER, null, this);

  }

  @Override
  public void onResume() {
    super.onResume();
    if(mHrefStr != null) {
      getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
    }
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    outState.putString(HREF_KEY, mHrefStr);
  }

  private Intent createShareForecastIntent() {
    Intent shareIntent = new Intent(Intent.ACTION_SEND);
    shareIntent.addFlags(Intent.FLAG_ACTIVITY_CLEAR_WHEN_TASK_RESET);
    shareIntent.setType("text/plain");
    shareIntent.putExtra(Intent.EXTRA_TEXT, mLinkStr);
    return shareIntent;
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    Log.v(LOG_TAG, "ON CREATE LOADER: " + id);

    String[] columns = {
        LinkContract.LinkEntry.TABLE_NAME + "." + LinkContract.LinkEntry._ID,
        LinkContract.LinkEntry.COLUMN_HREF,
        LinkContract.LinkEntry.COLUMN_TITLE,
        LinkContract.LinkEntry.COLUMN_HTML,
        LinkContract.LinkEntry.COLUMN_PUBLISHED_AT,
    };

    Uri linkUri = LinkContract.LinkEntry.buildLinkWithHref(mHrefStr);

    // Now create and return a CursorLoader that will take care of
    // creating a Cursor for the data being displayed.
    return new CursorLoader(
        getActivity(),
        linkUri,
        columns,
        null,
        null,
        null
    );
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    if (!data.moveToFirst()) { return; }

    String href = data.getString(data.getColumnIndex(LinkContract.LinkEntry.COLUMN_HREF));
    String title = data.getString(data.getColumnIndex(LinkContract.LinkEntry.COLUMN_TITLE));
    String html = data.getString(data.getColumnIndex(LinkContract.LinkEntry.COLUMN_HTML));

    mLinkStr = String.format("%s - %s", title, href);
    Log.v(LOG_TAG, "Link String: " + mLinkStr);

    mWebView.loadData(html, "text/html; charset=UTF-8", null);
    mWebView.getSettings().setUseWideViewPort(true);
    mWebView.getSettings().setLoadWithOverviewMode(true);
    mWebView.getSettings().setBuiltInZoomControls(true);
    mWebView.getSettings().setDisplayZoomControls(false);
//    mWebView.getSettings().setBlockNetworkLoads(true);
    this.getActivity().setTitle(title);
  }

  @Override
  public void onLoaderReset(Loader<Cursor> loader) {
    getLoaderManager().restartLoader(DETAIL_LOADER, null, this);
  }
}
