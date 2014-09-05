package com.example.balen.reader;

import android.app.Fragment;
import android.app.LoaderManager;
import android.content.CursorLoader;
import android.content.Loader;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.os.Parcelable;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.AdapterView;
import android.widget.ListView;

import com.example.balen.reader.data.LinkContract;
import com.example.balen.reader.sync.ReaderSyncAdapter;

/**
 * Created by balen on 8/6/14.
 */
public class ReaderMainFragment extends Fragment implements LoaderManager.LoaderCallbacks<Cursor> {
  private static final String LOG_TAG = ReaderMainFragment.class.getSimpleName();

  private static final int LINK_LOADER = 0;
  private static final String SELECTED_KEY = "selectedKey";

  public static final String[] LINK_COLUMNS = {
      LinkContract.LinkEntry.TABLE_NAME + "." + LinkContract.LinkEntry._ID,
      LinkContract.LinkEntry.COLUMN_LINK_SERVER_ID,
      LinkContract.LinkEntry.COLUMN_HREF,
      LinkContract.LinkEntry.COLUMN_TITLE,
      LinkContract.LinkEntry.COLUMN_CREATED_AT,
      LinkContract.LinkEntry.COLUMN_IMAGE_URL,
      LinkContract.LinkEntry.COLUMN_SNIPPET,
      LinkContract.LinkEntry.COLUMN_SOURCE,
      LinkContract.LinkEntry.COLUMN_PUBLISHER,
      LinkContract.LinkEntry.COLUMN_PUBLISHED_AT,
  };

  public static final int COL_LINK_TABLE_ID = 0;
  public static final int COL_LINK_SERVER_ID = 1;
  public static final int COL_HREF = 2;
  public static final int COL_TITLE = 3;
  public static final int COL_CREATED_AT = 4;
  public static final int COL_IMAGE_URL = 5;
  public static final int COL_SNIPPET = 6;
  public static final int COL_SOURCE = 7;
  public static final int COL_PUBLISHER = 8;
  public static final int COL_PUBLISHED_AT = 9;

  private ReaderAdapter mReaderAdapter;
  private int mPosition;
  private ListView mListView;
  private int mIndex;
  private int mTop;

  public interface Callback {
    public void onItemSelected(String item);
  }

  public ReaderMainFragment() {
  }

  @Override
  public View onCreateView(LayoutInflater inflater, ViewGroup container, final Bundle savedInstanceState) {
    Log.v(LOG_TAG, "ON CREATE VIEW");
    View rootView = inflater.inflate(R.layout.fragment_main, container, false);
    mListView = (ListView) rootView.findViewById(R.id.listview_reader);

    mReaderAdapter = new ReaderAdapter(getActivity(), null, 0);
    mListView.setAdapter(mReaderAdapter);

    mListView.setOnItemClickListener(new AdapterView.OnItemClickListener() {
      @Override
      public void onItemClick(AdapterView<?> parent, View view, int position, long id) {
        Cursor cursor = mReaderAdapter.getCursor();
        mPosition = position;

        Log.v(LOG_TAG, "ON CLICK: mIndex: " + mIndex + ", mTop: " + mTop + ", mPos: " + mPosition);
        if(null != cursor && cursor.moveToPosition(position)) {
          String href = cursor.getString(COL_HREF);
//          getActivity().getContentResolver().delete(LinkContract.LinkEntry.CONTENT_URI, LinkContract.LinkEntry._ID + " = ?", new String[]{String.valueOf(id)});
          ((Callback)getActivity()).onItemSelected(href);
        }
      }
    });

    // Create a ListView-specific touch listener. ListViews are given special treatment because
    // by default they handle touches for their list items... i.e. they're in charge of drawing
    // the pressed state (the list selector), handling list item clicks, etc.
    SwipeDismissListViewTouchListener touchListener =
        new SwipeDismissListViewTouchListener(
            mListView,
            new SwipeDismissListViewTouchListener.DismissCallbacks() {
              @Override
              public boolean canDismiss(int position) {
                return true;
              }

              @Override
              public void onDismiss(ListView listView, int[] reverseSortedPositions) {
                Cursor cursor = mReaderAdapter.getCursor();

                for (int position : reverseSortedPositions) {
                  if(null != cursor && cursor.moveToPosition(position)) {
                    int id = cursor.getInt(COL_LINK_TABLE_ID);
                    getActivity().getContentResolver().delete(LinkContract.LinkEntry.CONTENT_URI, LinkContract.LinkEntry._ID + " = ?", new String[]{String.valueOf(id)});
                  }
                }
                mReaderAdapter.notifyDataSetChanged();
                // WHY DOES IT SCROLL BACK UP TO THE TOP WHEN YOU DELETE SOMETHING?
              }
            });
    mListView.setOnTouchListener(touchListener);
    // Setting this scroll listener is required to ensure that during ListView scrolling,
    // we don't look for swipes.
    mListView.setOnScrollListener(touchListener.makeScrollListener());
    if(savedInstanceState != null && savedInstanceState.containsKey(SELECTED_KEY)) {
      Log.v(LOG_TAG, "Restoring selectedKey: " + savedInstanceState.getInt(SELECTED_KEY));
      mPosition = savedInstanceState.getInt(SELECTED_KEY);
    }
    if(savedInstanceState != null && savedInstanceState.containsKey("mTop") && savedInstanceState.containsKey("mIndex")) {
      mTop = savedInstanceState.getInt("mTop");
      mIndex = savedInstanceState.getInt("mIndex");
      Log.v(LOG_TAG, "Restoring scroll position mTop: " + mTop + ", mIndex: " + mIndex);
      mListView.setSelectionFromTop(mIndex, mTop);
    }
    return rootView;
  }

  @Override
  public void onActivityCreated(Bundle savedInstanceState) {
    super.onActivityCreated(savedInstanceState);
    getLoaderManager().initLoader(LINK_LOADER, null, this);
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setHasOptionsMenu(true);
  }

  @Override
  public void onCreateOptionsMenu(Menu menu, MenuInflater inflater) {
    // Inflate the menu; this adds items to the action bar if it is present.
    inflater.inflate(R.menu.readerfragment, menu);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_refresh) {
      updateLinks();
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onPause() {
    super.onPause();
  }

  @Override
  public void onResume() {
    super.onResume();
    Log.v(LOG_TAG, "ON RESUME MAIN FRAGMENT");
    getLoaderManager().restartLoader(LINK_LOADER, null, this);

    Log.v(LOG_TAG, "ON RESUME: mIndex: " + mIndex + ", mTop: " + mTop + ", mPos: " + mPosition);

    if(mListView != null) {
      Log.v(LOG_TAG, "Setting selection: mIndex: " + mIndex + ", mTop: " + mTop);
      mListView.setSelectionFromTop(mIndex, mTop);

    }
  }

  private void updateLinks() {
    ReaderSyncAdapter.syncImmediately(getActivity());
  }

  @Override
  public void onSaveInstanceState(Bundle outState) {
    super.onSaveInstanceState(outState);
    if (mPosition != ListView.INVALID_POSITION) {
      mIndex = mListView.getFirstVisiblePosition();
      View v = mListView.getChildAt(0);
      mTop = (v == null) ? 0 : v.getTop();

      Log.v(LOG_TAG, "SAVING mPos: " + mPosition + ", mIndex: " + mIndex + ", mTop: " + mTop);
      outState.putInt(SELECTED_KEY, mPosition);
      outState.putInt("mIndex", mIndex);
      outState.putInt("mTop", mTop);
      SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
      String positionKey = getActivity().getString(R.string.pref_last_read_article_position);
      SharedPreferences.Editor editor = prefs.edit();
      editor.putLong(positionKey, mPosition);
      editor.commit();
    }
  }

  @Override
  public Loader<Cursor> onCreateLoader(int id, Bundle args) {
    // This is called when a new Loader needs to be created.  This
    // fragment only uses one loader, so we don't care about checking the id.

//    String startDate = LinkContract.getDbDateString(new Date());

    // Sort order:  Ascending, by date.
    String sortOrder = LinkContract.LinkEntry.COLUMN_PUBLISHED_AT + " DESC";

    Uri linkUri = LinkContract.LinkEntry.CONTENT_URI;

    // Now create and return a CursorLoader that will take care of
    // creating a Cursor for the data being displayed.
    return new CursorLoader(
        getActivity(),
        linkUri,
        LINK_COLUMNS,
        null,
        null,
        sortOrder
    );
  }

  @Override
  public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
    mReaderAdapter.swapCursor(data);

    SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(getActivity());
    String positionKey = getActivity().getString(R.string.pref_last_read_article_position);
    long savedPosition = prefs.getLong(positionKey, 0L);
    Log.v(LOG_TAG, "ON LOAD FINISHED savedPosition: " + savedPosition);
    mPosition = (int)savedPosition;

    if(mPosition != ListView.INVALID_POSITION && mPosition != 0) {
      mListView.setSelection(mPosition);
    }
    Log.v(LOG_TAG, "ON LOAD FINISHED mPos: " + mPosition);
  }

  @Override
  public void onLoaderReset(Loader loader) {
    mReaderAdapter.swapCursor(null);
  }
}
