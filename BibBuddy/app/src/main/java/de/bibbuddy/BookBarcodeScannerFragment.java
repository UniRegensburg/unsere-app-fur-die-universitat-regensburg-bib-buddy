package de.bibbuddy;

import android.Manifest;
import android.content.Context;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.Log;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import java.io.IOException;
import java.util.List;

/**
 * BookBarcodeScannerFragment is responsible for scanning the ISBN of a book
 * that a user wants to add to a shelf.
 *
 * @author Claudia Schönherr, Luis Moßburger
 */
public class BookBarcodeScannerFragment extends BackStackFragment
    implements BookFormFragment.ChangeBookListener {

  private static final String TAG = BookBarcodeScannerFragment.class.getSimpleName();

  private SurfaceView surfaceView;
  private CameraSource cameraSource;
  private BarcodeDetector barcodeDetector;
  private Long shelfId;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_barcode_scanner, container, false);

    surfaceView = view.findViewById(R.id.surface_view);

    Bundle bundle = requireArguments();
    shelfId = bundle.getLong(LibraryKeys.SHELF_ID);

    setupDetectorsAndSources(view);
    setupMainActivity();

    return view;
  }

  private void setupMainActivity() {
    MainActivity mainActivity = (MainActivity) requireActivity();

    mainActivity.setVisibilityImportShareBtn(View.GONE, View.GONE);
    mainActivity.setVisibilitySortBtn(false);

    mainActivity.updateHeaderFragment(getString(R.string.isbn_scan));
    mainActivity.updateNavigationFragment(R.id.navigation_library);
  }

  private void setupDetectorsAndSources(View view) {
    barcodeDetector = new BarcodeDetector.Builder(view.getContext())
        .setBarcodeFormats(Barcode.ISBN | Barcode.EAN_13)
        .build();

    cameraSource = new CameraSource.Builder(requireContext(), barcodeDetector)
        .setRequestedPreviewSize(1920, 1080)
        .setAutoFocusEnabled(true)
        .build();

    surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
      @Override
      public void surfaceCreated(SurfaceHolder holder) {
        MainActivity mainActivity = (MainActivity) requireActivity();
        try {
          if (ActivityCompat.checkSelfPermission(mainActivity, Manifest.permission.CAMERA)
              == PackageManager.PERMISSION_GRANTED) {
            cameraSource.start(surfaceView.getHolder());
          } else {
            closeFragment();
          }
        } catch (IOException ex) {
          Log.e(TAG, ex.toString(), ex);
        }
      }

      @Override
      public void surfaceChanged(SurfaceHolder holder, int format, int width, int height) {
      }

      @Override
      public void surfaceDestroyed(SurfaceHolder holder) {
        cameraSource.stop();
      }
    });


    barcodeDetector.setProcessor(new Detector.Processor<Barcode>() {
      @Override
      public void release() {
      }

      /**
       * Receives a barcode ISBN, hands the ISBN over to the API.
       *
       * @param detections that were received from the camera
       */
      @Override
      public void receiveDetections(@NonNull Detector.Detections<Barcode> detections) {
        final SparseArray<Barcode> barcodes = detections.getDetectedItems();
        if (barcodes.size() != 0) {
          handleIsbnInput(barcodes.valueAt(0).displayValue);
        }
      }
    });
  }

  private void handleIsbnInput(String isbn) {
    String cleanIsbn = isbn.replaceAll("\\s", "");

    MainActivity mainActivity = (MainActivity) requireActivity();

    if (DataValidation.isValidIsbn10or13(cleanIsbn)) {
      IsbnRetriever isbnRetriever = new IsbnRetriever(cleanIsbn);
      Thread thread = new Thread(isbnRetriever);
      thread.start();

      try {
        thread.join();
      } catch (Exception ex) {
        Log.e(TAG, ex.toString(), ex);
      }

      // Retrieves metadata that was saved
      Book book = isbnRetriever.getBook();
      List<Author> authors = isbnRetriever.getAuthors();

      if (book != null) {
        handleAddBook(book, authors);
      } else {
        mainActivity.runOnUiThread(
            () -> Toast.makeText(mainActivity, getString(R.string.isbn_not_found),
                                 Toast.LENGTH_SHORT).show());
      }
    } else {
      mainActivity
          .runOnUiThread(() -> Toast.makeText(mainActivity, getString(R.string.isbn_not_valid),
                                              Toast.LENGTH_SHORT).show());
    }
  }

  private void handleAddBook(Book book, List<Author> authors) {
    BookFormFragment bookFormFragment = new BookFormFragment(this, book, authors);
    showFragment(bookFormFragment, LibraryKeys.FRAGMENT_BOOK);
  }

  @Override
  public void onBookAdded(Book book, List<Author> authorList) {
    BookAddModel bookAddModel = new BookAddModel(requireContext());
    bookAddModel.addBook(book, authorList, shelfId);

    MainActivity mainActivity = (MainActivity) requireActivity();
    mainActivity
        .runOnUiThread(() -> Toast.makeText(mainActivity, getString(R.string.added_book),
                                            Toast.LENGTH_SHORT).show());

    closeFragment();
  }

  @Override
  public void onPause() {
    super.onPause();
    cameraSource.release();
    barcodeDetector.release();
  }

  @Override
  public void onResume() {
    super.onResume();
    setupDetectorsAndSources(requireView());
  }

}
