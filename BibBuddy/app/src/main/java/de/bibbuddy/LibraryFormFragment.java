package de.bibbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * The LibraryFormFragment is responsible for adding or renaming a shelf.
 *
 * @author Claudia Schönherr
 */
public class LibraryFormFragment extends BackStackFragment {

  private final ChangeShelfListener listener;

  private String[] shelfNames;
  private String oldShelfName;
  private Long shelfId;
  private View view;

  private int redColor;
  private int greenColor;

  private void setupMembersFromBundle(Bundle bundle) {
    shelfNames = bundle.getStringArray(LibraryKeys.SHELF_NAMES);
    oldShelfName = bundle.getString(LibraryKeys.SHELF_NAME, "");
    shelfId = bundle.getLong(LibraryKeys.SHELF_ID, 0);
  }

  private void setupMainActivity() {
    MainActivity mainActivity = (MainActivity) requireActivity();

    mainActivity.setVisibilityImportShareBtn(View.GONE, View.GONE);
    mainActivity.setVisibilitySortBtn(false);

    if (shelfId == 0) {
      mainActivity.updateHeaderFragment(getString(R.string.add_new_shelf));
    } else {
      mainActivity.updateHeaderFragment(getString(R.string.rename_shelf));

      EditText editShelfName = view.findViewById(R.id.library_form_shelf_name_input);
      editShelfName.setText(oldShelfName);
    }

    mainActivity.updateNavigationFragment(R.id.navigation_library);
  }

  private void setupUpdateShelfBtnListener() {
    FloatingActionButton updateShelfBtn = view.findViewById(R.id.confirm_btn);
    updateShelfBtn.setOnClickListener(v -> handleUserInput());
  }

  private void handleUserInput() {
    EditText editShelfName = view.findViewById(R.id.library_form_shelf_name_input);
    String shelfName = editShelfName.getText().toString().trim();

    Context context = requireContext();

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
      listener.onShelfAdded(shelfName);
    } else {
      listener.onShelfRenamed(shelfName);
    }

    closeFragment();
  }

  public LibraryFormFragment(ChangeShelfListener listener) {
    this.listener = listener;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    view = inflater.inflate(R.layout.fragment_library_form, container, false);

    Bundle bundle = this.getArguments();
    assert bundle != null;
    setupMembersFromBundle(bundle);

    setupMainActivity();

    setupUpdateShelfBtnListener();

    redColor = getResources().getColor(R.color.red, null);
    greenColor = getResources().getColor(R.color.green, null);

    return view;
  }

  public interface ChangeShelfListener {
    default void onShelfAdded(String name) {
    }

    default void onShelfRenamed(String name) {
    }
  }

}
