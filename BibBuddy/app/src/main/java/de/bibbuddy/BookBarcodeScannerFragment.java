package de.bibbuddy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.os.Bundle;
import android.util.SparseArray;
import android.view.LayoutInflater;
import android.view.SurfaceHolder;
import android.view.SurfaceView;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.gms.vision.CameraSource;
import com.google.android.gms.vision.Detector;
import com.google.android.gms.vision.barcode.Barcode;
import com.google.android.gms.vision.barcode.BarcodeDetector;
import java.io.IOException;
import java.util.List;

/**
 * The BookBarcodeScannerFragment is responsible to scan the ISBN of the book
 * that a user wants to add to a shelf.
 *
 * @author Claudia Schönherr, Luis Moßburger
 */
public class BookBarcodeScannerFragment extends Fragment
    implements BookFormFragment.ChangeBookListener {
  private static final int REQUEST_CAMERA_PERMISSION = 201;

  private SurfaceView surfaceView;
  private CameraSource cameraSource;
  private BarcodeDetector barcodeDetector;
  private IsbnRetriever isbnRetriever;
  private Thread thread;

  private Long shelfId;
  private String shelfName;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_barcode_scanner, container, false);

    requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {

        FragmentManager fm = getParentFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
          fm.popBackStack();
        } else {
          requireActivity().onBackPressed();
        }
      }
    });

    surfaceView = view.findViewById(R.id.surface_view);
    Bundle bundle = getArguments();

    shelfId = bundle.getLong(LibraryKeys.SHELF_ID);
    shelfName = bundle.getString(LibraryKeys.SHELF_NAME);

    setupDetectorsAndSources(view);
    ((MainActivity) requireActivity()).updateHeaderFragment(getString(R.string.isbn_scan));

    return view;
  }

  private void setupDetectorsAndSources(View view) {
    barcodeDetector = new BarcodeDetector.Builder(view.getContext())
        .setBarcodeFormats(Barcode.ISBN | Barcode.EAN_13)
        .build();

    cameraSource = new CameraSource.Builder(getContext(), barcodeDetector)
        .setRequestedPreviewSize(1920, 1080)
        .setAutoFocusEnabled(true)
        .build();

    surfaceView.getHolder().addCallback(new SurfaceHolder.Callback() {
      @Override
      public void surfaceCreated(SurfaceHolder holder) {
        try {
          if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.CAMERA)
              == PackageManager.PERMISSION_GRANTED) {
            cameraSource.start(surfaceView.getHolder());
          } else {
            ActivityCompat.requestPermissions(requireActivity(), new
                String[] {Manifest.permission.CAMERA}, REQUEST_CAMERA_PERMISSION);
          }

        } catch (IOException e) {
          e.printStackTrace();
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
       */
      @Override
      public void receiveDetections(Detector.Detections<Barcode> detections) {
        final SparseArray<Barcode> barcodes = detections.getDetectedItems();
        if (barcodes.size() != 0) {
          barcodeDetector.release();
          handleIsbnInput(barcodes.valueAt(0).displayValue);
        }
      }
    });
  }

  private void handleIsbnInput(String isbn) {
    String cleanIsbn = isbn.replaceAll("\\s", "");

    if (DataValidation.isValidIsbn10or13(cleanIsbn)) {
      isbnRetriever = new IsbnRetriever(cleanIsbn);
      thread = new Thread(isbnRetriever);
      thread.start();

      try {
        thread.join();
      } catch (Exception e) {
        e.printStackTrace();
      }

      // retrieve metadata that was saved
      Book book = isbnRetriever.getBook();
      List<Author> authors = isbnRetriever.getAuthors();
      closeFragment();
      if (book != null) {
        handleAddBook(book, authors);
      } else {

        requireActivity().runOnUiThread(new Runnable() {
          public void run() {
            Toast.makeText(requireActivity(), getString(R.string.isbn_not_found),
                Toast.LENGTH_SHORT).show();
          }
        });
      }
    } else {

      requireActivity().runOnUiThread(new Runnable() {
        public void run() {
          Toast.makeText(requireActivity(), getString(R.string.isbn_not_valid),
              Toast.LENGTH_SHORT).show();
        }
      });
    }
  }

  private void handleAddBook(Book book, List<Author> authors) {
    BookFormFragment bookFormFragment = new BookFormFragment(this, book, authors);

    requireActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, bookFormFragment, LibraryKeys.FRAGMENT_BOOK)
        .addToBackStack(null)
        .commit();
  }

  @Override
  public void onBookAdded(Book book, List<Author> authorList) {
    BookDao bookDao = new BookDao(new DatabaseHelper(getContext()));
    bookDao.create(book, authorList, shelfId);

    requireActivity().runOnUiThread(new Runnable() {
      public void run() {
        Toast.makeText(requireActivity(), getString(R.string.added_book),
            Toast.LENGTH_SHORT).show();
      }
    });

    closeFragmentAfterAdding();
  }

  private void closeFragment() {
    BookFragment fragment = new BookFragment();
    requireActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, fragment)
        .setReorderingAllowed(true)
        .addToBackStack(null)
        .commit();

    fragment.setArguments(createBookBundle());
  }

  private void closeFragmentAfterAdding() {
    FragmentManager fragmentManager = getParentFragmentManager();
    if (fragmentManager.getBackStackEntryCount() > 1) {
      fragmentManager.popBackStack();
      fragmentManager.popBackStack();
    } else {
      requireActivity().onBackPressed();
      requireActivity().onBackPressed();
    }
  }

  private Bundle createBookBundle() {
    Bundle bundle = new Bundle();
    bundle.putString(LibraryKeys.SHELF_NAME, shelfName);
    bundle.putLong(LibraryKeys.SHELF_ID, shelfId);

    return bundle;
  }

  @Override
  public void onPause() {
    super.onPause();
    cameraSource.release();
  }

  @Override
  public void onResume() {
    super.onResume();
    setupDetectorsAndSources(getView());
  }

}
