package de.bibbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * The LibraryFormFragment is responsible for adding or renaming a shelf.
 *
 * @author Claudia Schönherr
 */
public class LibraryFormFragment extends Fragment {
  private final ChangeShelfListener listener;
  private int redColor;
  private int greenColor;
  private String[] shelfNames;
  private String oldShelfName;
  private Long shelfId;
  private View view;
  private Context context;

  public LibraryFormFragment(ChangeShelfListener listener) {
    this.listener = listener;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        closeFragment();
      }
    });

    view = inflater.inflate(R.layout.fragment_library_form, container, false);
    context = view.getContext();

    Bundle bundle = this.getArguments();

    if (bundle != null) {
      shelfNames = bundle.getStringArray(LibraryKeys.SHELF_NAMES);
      oldShelfName = bundle.getString(LibraryKeys.SHELF_NAME, "");
      shelfId = bundle.getLong(LibraryKeys.SHELF_ID, 0);

      setupShelfTextAndHeader();

      ((MainActivity) requireActivity())
          .setVisibilityImportShareButton(View.GONE, View.GONE);

      ((MainActivity) requireActivity()).setVisibilitySortButton(false);

      ((MainActivity) requireActivity()).updateNavigationFragment(R.id.navigation_library);

    }

    setupUpdateShelfBtnListener();

    redColor = getResources().getColor(R.color.red, null);
    greenColor = getResources().getColor(R.color.green, null);

    return view;
  }

  private void setupShelfTextAndHeader() {
    TextView shelfText = view.findViewById(R.id.library_form_shelf_name);

    if (shelfId == 0) {
      shelfText.setText(getString(R.string.add_new_shelf));
      ((MainActivity) requireActivity()).updateHeaderFragment(getString(R.string.add_new_shelf));
    } else {
      String oldShelfInfo =
          getString(R.string.shelf) + " \"" + oldShelfName + "\" "
              + getString(R.string.rename_small);

      shelfText.setText(oldShelfInfo);
      ((MainActivity) requireActivity()).updateHeaderFragment(getString(R.string.rename_shelf));

      EditText editShelfName = view.findViewById(R.id.library_form_shelf_name_input);
      editShelfName.setText(oldShelfName);
    }
  }

  private void setupUpdateShelfBtnListener() {
    FloatingActionButton updateShelfBtn = view.findViewById(R.id.confirm_btn);

    updateShelfBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleUserInput();
      }
    });
  }

  private void handleUserInput() {
    EditText editShelfName = view.findViewById(R.id.library_form_shelf_name_input);
    String shelfName = editShelfName.getText().toString().trim();

    if (DataValidation.isStringEmpty(shelfName)) {
      Toast.makeText(context, getString(R.string.invalid_name), Toast.LENGTH_SHORT).show();
      editShelfName.setBackgroundColor(redColor);
      return;
    }

    for (String name : shelfNames) {
      if (shelfName.equals(name) && !shelfName.equals(oldShelfName)) {
        Toast.makeText(context, getString(R.string.name_exists), Toast.LENGTH_SHORT).show();
        editShelfName.setBackgroundColor(redColor);
        return;
      }
    }

    editShelfName.setBackgroundColor(greenColor);

    if (shelfId == 0) {
      listener.onShelfAdded(shelfName, shelfId);
    } else {
      listener.onShelfRenamed(shelfName);
    }

    closeFragment();
  }

  /**
   * Closes the LibraryFormFragment.
   */
  public void closeFragment() {
    FragmentManager fragmentManager = getParentFragmentManager();
    if (fragmentManager.getBackStackEntryCount() > 0) {
      fragmentManager.popBackStack();
    } else {
      requireActivity().onBackPressed();
    }
  }

  public interface ChangeShelfListener {
    default void onShelfAdded(String name, Long shelfId) {
    }

    default void onShelfRenamed(String name) {
    }
  }

}
