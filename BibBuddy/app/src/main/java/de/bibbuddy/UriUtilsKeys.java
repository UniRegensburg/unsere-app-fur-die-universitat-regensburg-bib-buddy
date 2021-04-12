package de.bibbuddy;

/**
 * Constants used in the UriUtils class.
 *
 * @author Silvia Ivanova
 */
public class UriUtilsKeys {

  public static final String ENVIRONMENT_SECONDARY_STORAGE = "SECONDARY_STORAGE";
  public static final String ENVIRONMENT_EXTERNAL_STORAGE = "EXTERNAL_STORAGE";

  public static final String FILE = "file";
  public static final String CONTENT = "content";
  public static final String PRIMARY = "primary";
  public static final String RAW = "raw:";
  public static final String ID = "_id=?";
  //Media sources
  public static final String VIDEO = "video";
  public static final String AUDIO = "audio";
  public static final String IMAGE = "image";

  public static final String DATA_COLUMN = "_data";

  public static final String PUBLIC_DOWNLOADS = "content://downloads/public_downloads";
  public static final String MY_DOWNLOADS = "content://downloads/my_downloads";

  public static final String PREFIX_DOCUMENT_RAW = "^/document/raw:";
  public static final String PREFIX_CONTENT = "content://";
  public static final String PREFIX_RAW = "^raw:";

  public static final String PROVIDER_EXTERNAL_STORAGE_DOCUMENTS =
      "com.android.externalstorage.documents";
  public static final String PROVIDER_DOWNLOAD_DOCUMENTS =
      "com.android.providers.downloads.documents";
  public static final String PROVIDER_MEDIA_DOCUMENTS =
      "com.android.providers.media.documents";
  public static final String PROVIDER_PHOTOS_CONTENT =
      "com.google.android.apps.photos.content";

  public static final String AUTHORITY_DOCUMENT_STORAGE =
      "com.google.android.apps.docs.storage";
  public static final String AUTHORITY_DOCUMENT_STORAGE_LEGACY =
      "com.google.android.apps.docs.storage.legacy";

}
