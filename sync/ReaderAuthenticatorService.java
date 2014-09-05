package com.example.balen.reader.sync;

/**
 * Created by balen on 8/11/14.
 */

import android.app.Service;
import android.content.Intent;
import android.os.IBinder;

/**
 * A bound Service that instantiates the authenticator
 * when started.
 */
public class ReaderAuthenticatorService extends Service {

  // Instance field that stores the authenticator object
  private ReaderAuthenticator mAuthenticator;
  @Override
  public void onCreate() {
    // Create a new authenticator object
    mAuthenticator = new ReaderAuthenticator(this);
  }
  /*
   * When the system binds to this Service to make the RPC call
   * return the authenticator's IBinder.
   */
  @Override
  public IBinder onBind(Intent intent) {
    return mAuthenticator.getIBinder();
  }
}
