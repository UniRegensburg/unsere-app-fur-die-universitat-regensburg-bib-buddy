package de.bibbuddy;

import android.annotation.SuppressLint;
import android.app.Activity;
import android.content.ContentUris;
import android.content.Context;
import android.database.Cursor;
import android.net.Uri;
import android.os.Build;
import android.os.Environment;
import android.provider.DocumentsContract;
import android.provider.MediaStore;
import android.provider.OpenableColumns;
import android.text.TextUtils;
import android.util.Log;
import androidx.annotation.NonNull;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;

/**
 * The UriUtils is a Helper class for handling of imported files.
 * It mainly extracts the full path of imported Uniform Resource Identifier (URI).
 * This class contains static methods and cannot be instantiated.
 * Different resources (Documents, Media files) from various sources
 * (Document-Folder, Google Drive, External storage) are considered
 * and implemented.
 *
 * @author Silvia Ivanova
 */

public class UriUtils {

  private static final String TAG = UriUtils.class.getSimpleName();

  /**
   * Gets the full path from URI.
   *
   * @param context current context
   * @param uri     Uniform Resource Identifier (URI)
   * @return the full path of URI as String
   */

  @SuppressLint("API29")
  public static String getFullUriPath(final Context context, final Uri uri) {
    if (DocumentsContract.isDocumentUri(context, uri)) { // Document Provider
      return handleUriPathDocuments(context, uri);
    }

    if (UriUtilsKeys.CONTENT.equalsIgnoreCase(uri.getScheme())) { // Google Drive Provider
      return handleUriPathGoogleDrive(context, uri);
    }

    if (UriUtilsKeys.FILE.equalsIgnoreCase(uri.getScheme())) { // File
      return uri.getPath();
    }

    return null;
  }

  /**
   * Extracts the filename of the URI.
   *
   * @param activity the Activity
   * @param uri      Uniform Resource Identifier (URI)
   * @return the filename of URI as String
   */
  public static String getUriFileName(Activity activity, Uri uri) {
    String pickedFilename = null;
    String uriString = uri.toString();
    File uriFile = new File(uriString);

    if (uriString.startsWith(UriUtilsKeys.PREFIX_CONTENT)) {

      try (Cursor cursor = activity.getContentResolver().query(uri,
                                                               null, null, null, null)) {
        if (cursor.moveToFirst()) {
          pickedFilename = cursor.getString(cursor
                                                .getColumnIndex(OpenableColumns.DISPLAY_NAME));
        }

      }
    } else if (uriString.startsWith("file://")) {
      pickedFilename = uriFile.getAbsolutePath();
    }

    return pickedFilename;
  }

  private static String handleUriPathDocuments(Context context, Uri uri) {
    if (isExternalStorageDocument(uri)) {
      return getPathFromExternalStorage(uri);
    }

    if (isDownloadsDocument(uri)) { // DownloadsProvider
      return getPathFromDownloadDocument(context, uri);
    }

    if (isMediaDocument(uri)) { // MediaProvider
      return getPathFromMediaDocument(context, uri);
    }

    if (isGoogleDriveUri(uri)) {
      return getDriveFilePath(context, uri);
    }

    return null;
  }

  private static String handleUriPathGoogleDrive(Context context, Uri uri) {
    if (isGooglePhotosUri(uri)) {
      return uri.getLastPathSegment();
    }

    if (isGoogleDriveUri(uri)) {
      return getDriveFilePath(context, uri);
    }

    if (Build.VERSION.SDK_INT == Build.VERSION_CODES.N) {
      return getMediaFilePathForVersionN(context, uri);
    }

    return getDataColumn(context, uri, null, null);
  }

  @NonNull
  private static String getMediaFilePathForVersionN(Context context, Uri uri) {
    @SuppressLint("Recycle") Cursor cursor =
        context.getContentResolver().query(uri,
                                           null, null, null, null);

    return getFilePath(
        new File(context.getFilesDir(), getFileChildName(cursor)),
        context, uri);
  }

  private static String getPathFromExternalStorage(Uri uri) {
    final String[] pathUriData = DocumentsContract
        .getDocumentId(uri).split(File.pathSeparator);

    final String relativeUriPath = File.separator + pathUriData[1];
    String fullUriPath;

    if (UriUtilsKeys.PRIMARY.equalsIgnoreCase(pathUriData[0])) {
      fullUriPath = Environment.getExternalStorageDirectory()
          + relativeUriPath;

      if (fileExists(fullUriPath)) {
        return fullUriPath;
      }
    }

    fullUriPath = System.getenv(UriUtilsKeys.ENVIRONMENT_SECONDARY_STORAGE)
        + relativeUriPath;

    if (fileExists(fullUriPath)) {
      return fullUriPath;
    }

    fullUriPath = System.getenv(UriUtilsKeys.ENVIRONMENT_EXTERNAL_STORAGE)
        + relativeUriPath;

    fileExists(fullUriPath);

    if (!fullUriPath.isEmpty()) {
      return fullUriPath;
    }

    return null;
  }

  private static String getPathFromDownloadDocument(Context context, Uri uri) {
    try (Cursor cursor = context.getContentResolver()
        .query(uri, new String[] {MediaStore.MediaColumns.DISPLAY_NAME},
               null, null, null)) {

      if (cursor.moveToFirst()) {
        return getExternalStoragePath(cursor);
      }

    }

    final String documentId = DocumentsContract.getDocumentId(uri);
    if (!TextUtils.isEmpty(documentId)) {

      if (documentId.startsWith(UriUtilsKeys.RAW)) {
        return documentId.replaceFirst(UriUtilsKeys.RAW, "");
      }

      return handleUriPrefixes(context, uri, documentId);
    }

    return null;
  }

  private static String getPathFromMediaDocument(Context context, Uri uri) {
    final String[] documentIds = DocumentsContract.getDocumentId(uri)
        .split(File.pathSeparator);

    return getDataColumn(context, getContentMediaUri(documentIds[0]),
                         UriUtilsKeys.ID, new String[] {documentIds[1]});
  }

  private static Uri getContentMediaUri(String type) {
    Uri contentMediaUri = null;

    if (UriUtilsKeys.VIDEO.equals(type)) {
      contentMediaUri = MediaStore.Video.Media.EXTERNAL_CONTENT_URI;

    } else if (UriUtilsKeys.AUDIO.equals(type)) {
      contentMediaUri = MediaStore.Audio.Media.EXTERNAL_CONTENT_URI;

    } else if (UriUtilsKeys.IMAGE.equals(type)) {
      contentMediaUri = MediaStore.Images.Media.EXTERNAL_CONTENT_URI;
    }

    return contentMediaUri;
  }

  @NonNull
  private static String getDriveFilePath(@NonNull Context context, Uri uri) {
    @SuppressLint("Recycle") Cursor cursor =
        context.getContentResolver().query(uri,
                                           null, null, null, null);

    return getFilePath(
        new File(context.getCacheDir(), getFileChildName(cursor)),
        context, uri);
  }

  private static boolean fileExists(String filePath) {
    File file = new File(filePath);
    return file.exists();
  }

  private static String handleUriPrefixes(Context context, Uri uri, String id) {
    String[] contentUriPrefixes = new String[] {
        UriUtilsKeys.PUBLIC_DOWNLOADS,
        UriUtilsKeys.MY_DOWNLOADS};

    for (String contentUriPrefix : contentUriPrefixes) {

      try {
        return getDataColumn(context, ContentUris.withAppendedId(Uri.parse(contentUriPrefix),
                                                                 Long.parseLong(id)), null, null);

      } catch (NumberFormatException ex) {
        Log.e(TAG, ex.toString(), ex);

        return uri.getPath().replaceFirst(UriUtilsKeys.PREFIX_DOCUMENT_RAW, "")
            .replaceFirst(UriUtilsKeys.PREFIX_RAW, "");
      }
    }

    return null;
  }

  private static String getFileChildName(@NonNull Cursor cursor) {
    cursor.getColumnIndex(OpenableColumns.SIZE);
    cursor.moveToFirst();

    return cursor.getString(cursor
                                .getColumnIndex(OpenableColumns.DISPLAY_NAME));
  }

  private static String getDataColumn(@NonNull Context context, Uri uri, String selection,
                                      String[] selectionArgs) {
    Cursor cursor = null;
    final String dataColumn = UriUtilsKeys.DATA_COLUMN;
    final String[] projection = {dataColumn};

    try {
      cursor = context.getContentResolver().query(uri, projection,
                                                  selection, selectionArgs, null);
      if (cursor.moveToFirst()) {
        return cursor.getString(cursor.getColumnIndexOrThrow(dataColumn));
      }
    } finally {

      if (cursor != null) {
        cursor.close();
      }

    }

    return null;
  }

  private static String getExternalStoragePath(@NonNull Cursor cursor) {
    String fileName = cursor.getString(0);
    String path = Environment.getExternalStorageDirectory().toString()
        + File.separator + StorageKeys.DOWNLOAD_FOLDER + File.separator + fileName;

    if (!TextUtils.isEmpty(path)) {
      return path;
    }

    return null;
  }

  @NonNull
  private static String getFilePath(File file, @NonNull Context context, Uri uri) {
    try {
      InputStream inputStream = context.getContentResolver().openInputStream(uri);
      FileOutputStream outputStream = new FileOutputStream(file);

      int maxBufferSize = 1024 * 1024;
      int bufferSize = Math.min(inputStream.available(), maxBufferSize);
      final byte[] buffers = new byte[bufferSize];

      int read;
      while ((read = inputStream.read(buffers)) != -1) {
        outputStream.write(buffers, 0, read);
      }

      inputStream.close();
      outputStream.close();

    } catch (IOException ex) {
      Log.e(TAG, ex.toString(), ex);
    }

    return file.getPath();
  }

  private static boolean isExternalStorageDocument(@NonNull Uri uri) {
    return UriUtilsKeys.PROVIDER_EXTERNAL_STORAGE_DOCUMENTS
        .equals(uri.getAuthority());
  }

  private static boolean isDownloadsDocument(@NonNull Uri uri) {
    return UriUtilsKeys.PROVIDER_DOWNLOAD_DOCUMENTS
        .equals(uri.getAuthority());
  }

  private static boolean isMediaDocument(@NonNull Uri uri) {
    return UriUtilsKeys.PROVIDER_MEDIA_DOCUMENTS
        .equals(uri.getAuthority());
  }

  private static boolean isGooglePhotosUri(@NonNull Uri uri) {
    return UriUtilsKeys.PROVIDER_PHOTOS_CONTENT
        .equals(uri.getAuthority());
  }

  private static boolean isGoogleDriveUri(@NonNull Uri uri) {
    return UriUtilsKeys.AUTHORITY_DOCUMENT_STORAGE.equals(uri.getAuthority())
        || UriUtilsKeys.AUTHORITY_DOCUMENT_STORAGE_LEGACY.equals(uri.getAuthority());
  }

}
