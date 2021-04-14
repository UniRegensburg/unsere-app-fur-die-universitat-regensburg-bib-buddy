package de.bibbuddy;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

public class ApiReader implements Runnable {

  private static final String TAG = ApiReader.class.getSimpleName();
  private final String url;
  private String metadata = null;

  /**
   * The ApiReader class reads and returns data from the API.
   *
   * @param url concrete URL that should be sent to API
   * @author Luis Moßburger
   */
  ApiReader(String url) {
    this.url = url;
  }

  /**
   * Main method for the Runnable to start - read from API and return.
   *
   * @author Luis Moßburger
   */
  public void run() {
    try {
      URL metadataUrl = new URL(url);

      try (
          BufferedReader in = new BufferedReader(new InputStreamReader(metadataUrl.openStream()))) {

        this.metadata = in.lines().collect(Collectors.joining());
      }
    } catch (Exception ex) {
      Log.e(TAG, ex.toString(), ex);
    }
  }

  public String getMetadata() {
    return metadata;
  }

}
