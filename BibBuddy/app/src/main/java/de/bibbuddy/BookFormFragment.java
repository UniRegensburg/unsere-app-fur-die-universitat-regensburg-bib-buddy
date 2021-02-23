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
 * The BookFormFragment is responsible for adding a book manually to a shelf.
 *
 * @author Claudia Schönherr
 */
public class BookFormFragment extends Fragment {
  private final AddBookManuallyListener listener;
  List<Author> authorList;
  private boolean validInput;
  private Long shelfId;
  private String shelfName;
  private int redColor;
  private int greenColor;
  private Book book;

  public BookFormFragment(AddBookManuallyListener listener) {
    this.listener = listener;
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    // Called to have the fragment instantiate its user interface view.
    View view = inflater.inflate(R.layout.fragment_book_form, container, false);

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
    InputMethodManager inputManager =
        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    inputManager.toggleSoftInput(InputMethodManager.SHOW_FORCED, 0);
  }

  /**
   * Closes the BookFormFragment.
   */
  public void closeFragment() {
    InputMethodManager inputManager =
        (InputMethodManager) getActivity().getSystemService(Context.INPUT_METHOD_SERVICE);
    inputManager.toggleSoftInput(InputMethodManager.RESULT_HIDDEN, 0);

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
    Button cancelBtn = view.findViewById(R.id.btn_book_form_cancel);

    cancelBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        closeFragment();
      }
    });

    setupAddBookBtnListener(view);
  }

  private void setupAddBookBtnListener(View view) {
    Button addBookBtn = view.findViewById(R.id.btn_book_form_add);

    addBookBtn.setOnClickListener(new View.OnClickListener() {
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
      listener.onBookAdded(book, authorList);
      closeFragment();
    }
  }


  private void handleIsbn() {
    EditText isbnInput = getView().findViewById(R.id.book_form_isbn_input);
    String isbn = isbnInput.getText().toString();

    if (DataValidation.isValidIsbn10or13(isbn)) {
      isbnInput.setBackgroundColor(greenColor);
      book.setIsbn(isbn);
    } else {
      isbnInput.setBackgroundColor(redColor);
      validInput = false;
    }
  }

  private void handleTitle() {
    EditText titleInput = getView().findViewById(R.id.book_form_title_input);
    String title = titleInput.getText().toString();

    if (!DataValidation.isStringEmpty(title)) {
      titleInput.setBackgroundColor(greenColor);
      book.setTitle(title);
    } else {
      titleInput.setBackgroundColor(redColor);
      validInput = false;
    }
  }

  private void handleSubtitle() {
    EditText subtitleInput = getView().findViewById(R.id.book_form_subtitle_input);
    subtitleInput.setBackgroundColor(greenColor);

    String subtitle = subtitleInput.getText().toString();
    if (!DataValidation.isStringEmpty(subtitle)) {
      book.setSubtitle(subtitle);
    }
  }

  private void handlePubYear() {
    EditText pubYearInput = getView().findViewById(R.id.book_form_pub_year_input);
    String pubYear = pubYearInput.getText().toString();

    boolean validPubYear = DataValidation.isValidYear(pubYear);

    if (DataValidation.isStringEmpty(pubYear) || validPubYear) {
      book.setPubYear(0);
      if (validPubYear) {
        book.setPubYear(Integer.valueOf(pubYear));
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

    String publisher = publisherInput.getText().toString();
    if (!DataValidation.isStringEmpty(publisher)) {
      book.setPublisher(publisher);
    }
  }

  private void handleVolume() {
    EditText volumeInput = getView().findViewById(R.id.book_form_volume_input);
    volumeInput.setBackgroundColor(greenColor);

    String volume = volumeInput.getText().toString();
    if (!DataValidation.isStringEmpty(volume)) {
      book.setVolume(volume);
    }
  }

  private void handleEdition() {
    EditText editionInput = getView().findViewById(R.id.book_form_edition_input);
    editionInput.setBackgroundColor(greenColor);

    String edition = editionInput.getText().toString();
    if (!DataValidation.isStringEmpty(edition)) {
      book.setEdition(edition);
    }
  }

  private void handleAddInfos() {
    EditText addInfosInput = getView().findViewById(R.id.book_form_add_infos_input);
    addInfosInput.setBackgroundColor(greenColor);

    String addInfos = addInfosInput.getText().toString();
    if (!DataValidation.isStringEmpty(addInfos)) {
      book.setAddInfo(addInfos);
    }
  }

  private void handleAuthors() {
    // TODO add more edit texts via plus button to add more than one author
    authorList = new ArrayList<>();
    Author author = new Author();
    boolean isAuthor = false;

    EditText authorTitleInput = getView().findViewById(R.id.book_form_author_title_input);
    authorTitleInput.setBackgroundColor(greenColor);
    String authorTitle = authorTitleInput.getText().toString();

    if (!DataValidation.isStringEmpty(authorTitle)) {
      author.setTitle(authorTitle);
    }

    EditText authorFirstNameInput = getView().findViewById(R.id.book_form_author_first_name_input);
    authorFirstNameInput.setBackgroundColor(greenColor);
    String authorFirstName = authorFirstNameInput.getText().toString();

    if (!DataValidation.isStringEmpty(authorFirstName)) {
      author.setFirstName(authorFirstName);
      isAuthor = true;
    }

    EditText authorLastNameInput = getView().findViewById(R.id.book_form_author_last_name_input);
    authorLastNameInput.setBackgroundColor(greenColor);
    String authorLastName = authorLastNameInput.getText().toString();

    if (!DataValidation.isStringEmpty(authorLastName)) {
      author.setLastName(authorLastName);
      isAuthor = true;
    }

    if (isAuthor) {
      authorList.add(author);
    }

  }

  public interface AddBookManuallyListener { // create an interface
    void onBookAdded(Book book, List<Author> authorList); // create callback function
  }


}
