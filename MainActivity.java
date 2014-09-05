package com.example.balen.reader;

import android.app.Activity;
import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.Menu;
import android.view.MenuItem;

import com.example.balen.reader.sync.ReaderSyncAdapter;


public class MainActivity extends Activity implements ReaderMainFragment.Callback {

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    Log.v("READER", "ON CREATE");
    setContentView(R.layout.activity_main);
    if (findViewById(R.id.reader_detail_container) != null) {
      if(savedInstanceState == null) {
        getFragmentManager().beginTransaction().replace(R.id.reader_detail_container, new DetailFragment()).commit();
      }
    }

    ReaderSyncAdapter.initializeSyncAdapter(this);
  }


  @Override
  public boolean onCreateOptionsMenu(Menu menu) {
    // Inflate the menu; this adds items to the action bar if it is present.
    getMenuInflater().inflate(R.menu.main, menu);
    return true;
  }

  @Override
  public void onItemSelected(String href) {
    Intent intent = new Intent(this, DetailActivity.class).putExtra(DetailFragment.HREF_KEY, href);
    startActivity(intent);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    // Handle action bar item clicks here. The action bar will
    // automatically handle clicks on the Home/Up button, so long
    // as you specify a parent activity in AndroidManifest.xml.
    int id = item.getItemId();
    if (id == R.id.action_settings) {
      Intent settingsIntent = new Intent(this, SettingsActivity.class);
      startActivity(settingsIntent);
      return true;
    }
    return super.onOptionsItemSelected(item);
  }

  @Override
  protected void onStop() {
    super.onStop();
    Log.v("READER", "ON STOP");
  }

  @Override
  protected void onDestroy() {
    super.onDestroy();
    Log.v("READER", "ON DESTROY");
  }

  @Override
  protected void onStart() {
    super.onStart();
    Log.v("READER", "ON START");
  }

  @Override
  protected void onRestart() {
    super.onRestart();
    Log.v("READER", "ON RESTART");
  }

  @Override
  protected void onResume() {
    super.onResume();
    Log.v("READER", "ON RESUME");
  }

  @Override
  protected void onPause() {
    super.onPause();
    Log.v("READER", "ON PAUSE");
  }
}
