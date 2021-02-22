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
import androidx.fragment.app.Fragment;
import java.util.ArrayList;
import java.util.List;

/**
 * The BookAddManuallyFragment is responsible for adding a book manually to a shelf.
 *
 * @author Claudia Schönherr
 */
public class BookAddManuallyFragment extends Fragment {
  private final AddBookManuallyListener listener;
  private Long shelfId;
  private String shelfName;

  public BookAddManuallyFragment(AddBookManuallyListener listener) {
    this.listener = listener;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    // Called to have the fragment instantiate its user interface view.
    View view = inflater.inflate(R.layout.fragment_book_add_manually, container, false);

    setupButtons(view);
    setupInput(view);

    Bundle bundle = this.getArguments();
    shelfName = bundle.getString(LibraryKeys.SHELF_NAME);
    shelfId = bundle.getLong(LibraryKeys.SHELF_ID);

    ((MainActivity) getActivity()).updateHeaderFragment(getString(R.string.add_book));

    return view;
  }

  private void setupInput(View view) {
    view.findViewById(R.id.book_form_isbn_input).requestFocus();
    InputMethodManager imm =
        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
  }

  /**
   * Closes the BookAddManuallyFragment.
   */
  public void closeFragment() {
    InputMethodManager imm =
        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    imm.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0);

    BookFragment fragment = new BookFragment();
    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, fragment)
        .setReorderingAllowed(true)
        .addToBackStack(null)
        .commit();

    fragment.setArguments(createBookBundle());
  }

  private Bundle createBookBundle() {
    Bundle bundle = new Bundle();
    bundle.putString(LibraryKeys.SHELF_NAME, shelfName);
    bundle.putLong(LibraryKeys.SHELF_ID, shelfId);

    return bundle;
  }

  private void setupButtons(View view) {
    Context context = view.getContext();
    Button cancelBtn = view.findViewById(R.id.btn_book_manually_cancel);

    cancelBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        closeFragment();
      }
    });

    setupAddShelfBtnListener(context, view);
  }

  private void setupAddShelfBtnListener(Context context, View view) {
    Button addShelfBtn = view.findViewById(R.id.btn_book_manually_confirm);

    addShelfBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleUserInput();
      }
    });
  }

  private void handleUserInput() {
    boolean validInput = true;

    int redColor = getResources().getColor(R.color.alert_red);
    int greenColor = getResources().getColor(R.color.green);

    EditText isbnInput = getView().findViewById(R.id.book_form_isbn_input);
    String isbnStr = isbnInput.getText().toString();
    if (DataValidation.isValidIsbn10or13(isbnStr)) {
      isbnInput.setBackgroundColor(greenColor);
    } else if (!DataValidation.isValidIsbn10or13(isbnStr)) {
      isbnInput.setBackgroundColor(redColor);
      validInput = false;
    }


    EditText titleInput = getView().findViewById(R.id.book_form_title_input);
    String titleStr = titleInput.getText().toString();
    if (!DataValidation.isStringEmpty(titleStr)) {
      titleInput.setBackgroundColor(greenColor);
    } else if (DataValidation.isStringEmpty(titleStr)) {
      titleInput.setBackgroundColor(redColor);
      validInput = false;
    }

    EditText subtitleInput = getView().findViewById(R.id.book_form_subtitle_input);
    subtitleInput.setBackgroundColor(greenColor);

    EditText pubYearInput = getView().findViewById(R.id.book_form_pub_year_input);
    String pubYearStr = pubYearInput.getText().toString();
    if (DataValidation.isStringEmpty(pubYearStr)
        || DataValidation.isValidPositiveNumber(pubYearStr)) {
      pubYearInput.setBackgroundColor(greenColor);
    } else {
      pubYearInput.setBackgroundColor(redColor);
      validInput = false;
    }

    EditText publisherInput = getView().findViewById(R.id.book_form_publisher_input);
    publisherInput.setBackgroundColor(greenColor);

    EditText volumeInput = getView().findViewById(R.id.book_form_volume_input);
    volumeInput.setBackgroundColor(greenColor);

    EditText editionInput = getView().findViewById(R.id.book_form_edition_input);
    editionInput.setBackgroundColor(greenColor);

    EditText addInfosInput = getView().findViewById(R.id.book_form_add_infos_input);
    addInfosInput.setBackgroundColor(greenColor);

    // TODO AUTHORS
    // tmp only the main author can be added
    // author can be optional
    EditText authorTitleInput = getView().findViewById(R.id.book_form_author_title);
    authorTitleInput.setBackgroundColor(greenColor);

    EditText authorFirstNameInput = getView().findViewById(R.id.book_form_author_first_name);
    authorFirstNameInput.setBackgroundColor(greenColor);

    EditText authorLastNameInput = getView().findViewById(R.id.book_form_author_last_name);
    authorLastNameInput.setBackgroundColor(greenColor);

    if (validInput) {
      handleValidInput();
    }
  }

  private void handleValidInput() {
    Toast.makeText(getContext(), getString(R.string.added_book), Toast.LENGTH_SHORT).show();

    Book book = new Book();
    // TODO book data
    List<Author> authorList = new ArrayList<>();

    // TODO author list
    // authors author author_first_name author_last_name author_title

    // listener.onBookAdded(book, authorList);
    closeFragment();
  }

  public interface AddBookManuallyListener { // create an interface
    void onBookAdded(Book book, List<Author> authorList); // create callback function
  }


}
