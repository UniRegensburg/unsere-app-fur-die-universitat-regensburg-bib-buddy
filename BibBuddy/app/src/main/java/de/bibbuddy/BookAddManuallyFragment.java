package de.bibbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.inputmethod.InputMethodManager;
import android.widget.Button;
import android.widget.EditText;
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
  List<Author> authorList;
  private boolean validInput;
  private Long shelfId;
  private String shelfName;
  private int redColor;
  private int greenColor;
  private Book book;

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

    redColor = getResources().getColor(R.color.alert_red);
    greenColor = getResources().getColor(R.color.green);

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
    Button cancelBtn = view.findViewById(R.id.btn_book_manually_cancel);

    cancelBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        closeFragment();
      }
    });

    setupAddBookBtnListener(view);
  }

  private void setupAddBookBtnListener(View view) {
    Button addShelfBtn = view.findViewById(R.id.btn_book_manually_confirm);

    addShelfBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleUserInput();
      }
    });
  }

  private void handleUserInput() {
    validInput = true;
    book = new Book();

    handleIsbn();
    handleTitle();
    handleSubtitle();
    handlePubYear();
    handlePublisher();
    handleVolume();
    handleEdition();
    handleAddInfos();

    handleAuthors();

    if (validInput) {
      handleValidInput();
    }
  }


  private void handleIsbn() {
    EditText isbnInput = getView().findViewById(R.id.book_form_isbn_input);
    String isbnStr = isbnInput.getText().toString();

    if (DataValidation.isValidIsbn10or13(isbnStr)) {
      isbnInput.setBackgroundColor(greenColor);
      book.setIsbn(isbnStr);
    } else if (!DataValidation.isValidIsbn10or13(isbnStr)) {
      isbnInput.setBackgroundColor(redColor);
      validInput = false;
    }
  }

  private void handleTitle() {
    EditText titleInput = getView().findViewById(R.id.book_form_title_input);
    String titleStr = titleInput.getText().toString();

    if (!DataValidation.isStringEmpty(titleStr)) {
      titleInput.setBackgroundColor(greenColor);
      book.setTitle(titleStr);
    } else if (DataValidation.isStringEmpty(titleStr)) {
      titleInput.setBackgroundColor(redColor);
      validInput = false;
    }
  }

  private void handleSubtitle() {
    EditText subtitleInput = getView().findViewById(R.id.book_form_subtitle_input);
    subtitleInput.setBackgroundColor(greenColor);

    String subtitleStr = subtitleInput.getText().toString();
    if (!DataValidation.isStringEmpty(subtitleStr)) {
      book.setSubtitle(subtitleStr);
    }
  }

  private void handlePubYear() {
    EditText pubYearInput = getView().findViewById(R.id.book_form_pub_year_input);
    String pubYearStr = pubYearInput.getText().toString();

    boolean validPubYear = DataValidation.isValidYear(pubYearStr);

    if (DataValidation.isStringEmpty(pubYearStr) || validPubYear) {
      if (validPubYear) {
        book.setPubYear(Integer.valueOf(pubYearStr));
      }
      pubYearInput.setBackgroundColor(greenColor);
    } else {
      pubYearInput.setBackgroundColor(redColor);
      validInput = false;
    }
  }


  private void handlePublisher() {
    EditText publisherInput = getView().findViewById(R.id.book_form_publisher_input);
    publisherInput.setBackgroundColor(greenColor);

    String publisherStr = publisherInput.getText().toString();
    if (!DataValidation.isStringEmpty(publisherStr)) {
      book.setPublisher(publisherStr);
    }
  }

  private void handleVolume() {
    EditText volumeInput = getView().findViewById(R.id.book_form_volume_input);
    volumeInput.setBackgroundColor(greenColor);

    String volumeStr = volumeInput.getText().toString();
    if (!DataValidation.isStringEmpty(volumeStr)) {
      book.setVolume(volumeStr);
    }
  }

  private void handleEdition() {
    EditText editionInput = getView().findViewById(R.id.book_form_edition_input);
    editionInput.setBackgroundColor(greenColor);

    String editionStr = editionInput.getText().toString();
    if (!DataValidation.isStringEmpty(editionStr)) {
      book.setEdition(editionStr);
    }
  }

  private void handleAddInfos() {
    EditText addInfosInput = getView().findViewById(R.id.book_form_add_infos_input);
    addInfosInput.setBackgroundColor(greenColor);

    String addInfosStr = addInfosInput.getText().toString();
    if (!DataValidation.isStringEmpty(addInfosStr)) {
      book.setAddInfo(addInfosStr);
    }
  }

  private void handleAuthors() {
    // TODO AUTHORS
    //  tmp only the main author can be added
    authorList = new ArrayList<>();
    Author author = new Author();
    boolean isAuthor = false;

    EditText authorTitleInput = getView().findViewById(R.id.book_form_author_title);
    authorTitleInput.setBackgroundColor(greenColor);
    String authorTitleStr = authorTitleInput.getText().toString();

    if (!DataValidation.isStringEmpty(authorTitleStr)) {
      author.setTitle(authorTitleStr);
    }

    EditText authorFirstNameInput = getView().findViewById(R.id.book_form_author_first_name);
    authorFirstNameInput.setBackgroundColor(greenColor);
    String authorFirstNameStr = authorFirstNameInput.getText().toString();

    if (!DataValidation.isStringEmpty(authorFirstNameStr)) {
      author.setFirstName(authorFirstNameStr);
      isAuthor = true;
    }

    EditText authorLastNameInput = getView().findViewById(R.id.book_form_author_last_name);
    authorLastNameInput.setBackgroundColor(greenColor);
    String authorLastNameStr = authorLastNameInput.getText().toString();

    if (!DataValidation.isStringEmpty(authorLastNameStr)) {
      author.setLastName(authorLastNameStr);
      isAuthor = true;
    }

    if (isAuthor) {
      authorList.add(author);
    }

  }

  private void handleValidInput() {
    listener.onBookAdded(book, authorList);
    closeFragment();
  }

  public interface AddBookManuallyListener { // create an interface
    void onBookAdded(Book book, List<Author> authorList); // create callback function
  }


}
