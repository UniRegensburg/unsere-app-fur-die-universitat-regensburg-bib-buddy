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
import android.widget.TextView;
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

/**
 * The BookBarcodeScannerFragment is responsible to scan the ISBN of the book
 * that a user wants to add to a shelf.
 *
 * @author Claudia Schönherr
 */
public class BookBarcodeScannerFragment extends Fragment {
  private static final int REQUEST_CAMERA_PERMISSION = 201;

  private SurfaceView surfaceView;
  private CameraSource cameraSource;
  private TextView barcodeText;
  private String barcodeData;
  private BarcodeDetector barcodeDetector;

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
    barcodeText = view.findViewById(R.id.barcode_value);
    Bundle bundle = getArguments();

    shelfId = bundle.getLong(LibraryKeys.SHELF_ID);
    shelfName = bundle.getString(LibraryKeys.SHELF_NAME);

    setupDetectorsAndSources(view);
    ((MainActivity) getActivity()).updateHeaderFragment(getString(R.string.isbn_scan));

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
          if (ActivityCompat.checkSelfPermission(getActivity(), Manifest.permission.CAMERA)
              == PackageManager.PERMISSION_GRANTED) {
            cameraSource.start(surfaceView.getHolder());
          } else {
            ActivityCompat.requestPermissions(getActivity(), new
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
       * Receives the ISBN of a book.
       */
      @Override
      public void receiveDetections(Detector.Detections<Barcode> detections) {
        final SparseArray<Barcode> barcodes = detections.getDetectedItems();
        if (barcodes.size() != 0) {

          barcodeText.post(new Runnable() {

            @Override
            public void run() {
              barcodeData = barcodes.valueAt(0).displayValue;
              String barcodeDisplay = getString(R.string.barcode_value) + " " + barcodeData;
              barcodeText.setText(barcodeDisplay);
              // TODO get display message with ISBN successfully scanned
              // TODO temporary stop the barcode scanner
              // TODO new fragment user wait for search
              // TODO check if book exists in API
              // TODO API get book data
              // if not exists: display error message and open create new book form alternative
              // TODO check if valid data / book information data display
              // TODO add book to shelf
              // TODO bundle for BookFragment with shelfId and shelfName
            }
          });

        }
      }
    });
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
