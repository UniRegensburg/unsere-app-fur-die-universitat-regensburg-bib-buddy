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
 * LibraryRenameShelfFragment to rename a shelf name.
 *
 * @author Claudia Schönherr
 */
public class LibraryRenameShelfFragment extends DialogFragment {

  private final LibraryRenameShelfFragment.RenameShelfLibraryListener listener;

  public LibraryRenameShelfFragment(
      LibraryRenameShelfFragment.RenameShelfLibraryListener listener) {
    this.listener = listener;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    // Called to have the fragment instantiate its user interface view.
    View view = inflater.inflate(R.layout.fragment_library_rename_shelf, container, false);

    Bundle bundle = this.getArguments();

    setupButtons(view, bundle);
    setupEditText(view, bundle.getString(LibraryKeys.SHELF_NAME));
    setupInput(view);

    return view;
  }


  private void setupInput(View view) {
    view.findViewById(R.id.input_rename_shelf_name).requestFocus();
    InputMethodManager imm =
        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
  }

  /**
   * Closes the LibraryRenameShelfFragment.
   *
   * @author Claudia Schönherr
   */
  public void closeFragment() {
    onDestroyView();

    InputMethodManager imm =
        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0);
  }

  private void setupButtons(View view, Bundle bundle) {
    Context context = view.getContext();
    Button cancelBtn = view.findViewById(R.id.btn_rename_shelf_cancel);

    cancelBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        closeFragment();
      }
    });

    setupRenameShelfBtnListener(context, view, bundle);
  }

  private void setupEditText(View view, String shelfName) {
    EditText editShelfName = view.findViewById(R.id.input_rename_shelf_name);
    editShelfName.setText(shelfName);
  }

  private void setupRenameShelfBtnListener(Context context, View view, Bundle bundle) {
    Button addShelfBtn = view.findViewById(R.id.btn_rename_shelf_confirm);

    addShelfBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        EditText editShelfName = view.findViewById(R.id.input_rename_shelf_name);
        String shelfName = editShelfName.getText().toString();

        if (DataValidation.isStringEmpty(shelfName)) {
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
        Toast.makeText(context, getString(R.string.renamed_shelf), Toast.LENGTH_SHORT).show();
        listener.onShelfRenamed(shelfName);
        closeFragment();
      }
    });
  }

  public interface RenameShelfLibraryListener { // create an interface
    void onShelfRenamed(String name); // create callback function
  }
}
