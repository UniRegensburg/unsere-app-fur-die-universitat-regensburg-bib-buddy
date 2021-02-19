package de.bibbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.DialogFragment;

/**
 * The LibraryAddShelfFragment is responsible for adding a new shelf to the library.
 *
 * @author Claudia Schönherr
 */
public class LibraryAddShelfFragment extends DialogFragment {

  private final AddShelfLibraryListener listener;

  public LibraryAddShelfFragment(AddShelfLibraryListener listener) {
    this.listener = listener;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    // Called to have the fragment instantiate its user interface view.
    View view = inflater.inflate(R.layout.fragment_library_add_shelf, container, false);

    Bundle bundle = this.getArguments();
    setupButtons(view, bundle);
    setupInput(view);

    return view;
  }

  private void setupInput(View view) {
    view.findViewById(R.id.input_shelf_name).requestFocus();
    InputMethodManager imm =
        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
  }

  /**
   * Closes the LibraryAddShelfFragment.
   */
  public void closeFragment() {
    onDestroyView();

    InputMethodManager imm =
        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0);
  }

  private void setupButtons(View view, Bundle bundle) {
    Context context = view.getContext();
    Button cancelBtn = view.findViewById(R.id.btn_shelf_cancel);

    cancelBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        closeFragment();
      }
    });

    setupAddShelfBtnListener(context, view, bundle);
  }

  private void setupAddShelfBtnListener(Context context, View view, Bundle bundle) {
    Button addShelfBtn = view.findViewById(R.id.btn_confirm_shelf);

    addShelfBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        EditText editShelfName = view.findViewById(R.id.input_shelf_name);
        String shelfName = editShelfName.getText().toString();

        if (shelfName.isEmpty() || shelfName.trim().isEmpty()) {
          Toast.makeText(context, getString(R.string.invalid_name), Toast.LENGTH_SHORT).show();
          return;
        }

        String[] shelfNames = bundle.getStringArray(LibraryKeys.SHELF_NAMES);
        for (String name : shelfNames) {
          if (shelfName.equals(name)) {
            Toast.makeText(context, getString(R.string.name_exists), Toast.LENGTH_SHORT).show();
            return;
          }
        }

        Toast.makeText(context, getString(R.string.shelf_added), Toast.LENGTH_SHORT).show();
        Long shelfId = bundle.getLong(LibraryKeys.SHELF_ID);
        listener.onShelfAdded(shelfName, shelfId);
        closeFragment();
      }
    });
  }

  public interface AddShelfLibraryListener { // create an interface
    void onShelfAdded(String name, Long shelfId); // create callback function
  }

}
