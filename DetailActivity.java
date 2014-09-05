package com.example.balen.reader;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.preference.PreferenceManager;
import android.util.Log;
import android.view.MenuItem;

public class DetailActivity extends Activity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_detail);

        if (savedInstanceState == null) {
          String href = getIntent().getStringExtra(DetailFragment.HREF_KEY);

          Bundle args = new Bundle();
          args.putString(DetailFragment.HREF_KEY, href);

          DetailFragment fragment = new DetailFragment();
          fragment.setArguments(args);

          getFragmentManager().beginTransaction()
              .add(R.id.reader_detail_container, fragment)
              .commit();
        } else {
          Log.v("READER", "Detail activity - savedInstanceState is NOT NULL");

        }
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
//        } else if (id == R.id.action_map_location) {
//          Intent mapIntent = new Intent(Intent.ACTION_VIEW);
//          SharedPreferences prefs = PreferenceManager.getDefaultSharedPreferences(this);
//          String location = prefs.getString(getString(R.string.pref_location_key),
//              getString(R.string.pref_location_default));
//          mapIntent.setData(Uri.parse("geo:0,0?q=" + location));
//          if (mapIntent.resolveActivity(getPackageManager()) != null) {
//            startActivity(mapIntent);
//          }
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */

}
