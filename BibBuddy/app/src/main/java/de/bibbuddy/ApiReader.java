package de.bibbuddy;

import android.util.Log;
import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
import java.util.stream.Collectors;

/**
 * ApiReader reads and returns data from the API.
 *
 * @author Luis Moßburger
 */
public class ApiReader implements Runnable {

  private static final String TAG = ApiReader.class.getSimpleName();

  private final String url;

  private String metadata;

  /**
   * Constructor of an ApiReader that can be used to read and return data from the API.
   *
   * @param url that should be sent to API
   */
  ApiReader(String url) {
    this.url = url;
  }

  /**
   * Reads from API and return.
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
