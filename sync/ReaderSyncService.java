package com.example.balen.reader.sync;

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;
import android.util.Log;

/**
 * Created by balen on 8/11/14.
 */
public class ReaderSyncService extends Service {
  private static final Object sSyncAdapterLock = new Object();
  private static ReaderSyncAdapter sReaderSyncAdapter = null;

  @Override
  public void onCreate() {
    Log.d("ReaderSyncService", "onCreate - ReaderSyncService");
    synchronized (sSyncAdapterLock) {
      if(sReaderSyncAdapter == null) {
        sReaderSyncAdapter = new ReaderSyncAdapter(getApplicationContext(), true);
      }
    }
  }

  @Override
  public IBinder onBind(Intent intent) {
    return sReaderSyncAdapter.getSyncAdapterBinder();
  }
}
