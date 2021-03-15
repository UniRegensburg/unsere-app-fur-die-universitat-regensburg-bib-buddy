package de.bibbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.util.ArrayList;
import java.util.List;

/**
 * The BookFormFragment is responsible for adding a book manually to a shelf.
 *
 * @author Claudia Schönherr, Sarah Kurek
 */
public class BookFormFragment extends Fragment {
  private final ChangeBookListener listener;
  private final List<Author> authorList = new ArrayList<>();
  private boolean validInput;
  private Long shelfId;
  private String shelfName;
  private Long bookId;
  private int redColor;
  private int greenColor;
  private Book book = new Book();

  public BookFormFragment(ChangeBookListener listener) {
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

    // Called to have the fragment instantiate its user interface view.
    View view = inflater.inflate(R.layout.fragment_book_form, container, false);

    Bundle bundle = this.getArguments();
    if (bundle != null) {
      shelfName = bundle.getString(LibraryKeys.SHELF_NAME);
      shelfId = bundle.getLong(LibraryKeys.SHELF_ID);
      bookId = bundle.getLong(LibraryKeys.BOOK_ID, 0);

      if (bookId == 0) { // add new book
        ((MainActivity) getActivity()).updateHeaderFragment(getString(R.string.add_book));
      } else { // edit existing book
        BookModel model = new BookModel(getContext(), shelfId);
        book = model.getBookById(bookId);

        if (authorList.isEmpty()) {
          authorList.addAll(model.getAuthorList(bookId));
        }

        ((MainActivity) getActivity())
            .setVisibilityImportShareButton(View.INVISIBLE, View.INVISIBLE);
        ((MainActivity) getActivity()).updateHeaderFragment(getString(R.string.change_book));
        
        Button addBookBtn = view.findViewById(R.id.btn_book_form_add);
        addBookBtn.setText(R.string.change);
      }

      setInputText(view);
      setupAddAuthorBtnListener(view);
    }

    redColor = getResources().getColor(R.color.alert_red, null);
    greenColor = getResources().getColor(R.color.green, null);

    setupButtons(view);

    return view;
  }

  private void setInputText(View view) {
    EditText isbnField = view.findViewById(R.id.book_form_isbn_input);
    isbnField.setText(book.getIsbn());

    EditText titleField = view.findViewById(R.id.book_form_title_input);
    titleField.setText(book.getTitle());

    EditText subtitleField = view.findViewById(R.id.book_form_subtitle_input);
    subtitleField.setText(book.getSubtitle());

    EditText pubYearField = view.findViewById(R.id.book_form_pub_year_input);
    Integer pubYear = book.getPubYear();
    pubYearField.setText(pubYear == null ? "" : pubYear.toString());

    EditText publisherField = view.findViewById(R.id.book_form_publisher_input);
    publisherField.setText(book.getPublisher());

    EditText volumeField = view.findViewById(R.id.book_form_volume_input);
    volumeField.setText(book.getVolume());

    EditText editionField = view.findViewById(R.id.book_form_edition_input);
    editionField.setText(book.getEdition());

    EditText addInfoField = view.findViewById(R.id.book_form_add_infos_input);
    addInfoField.setText(book.getAddInfo());
  }

  /**
   * Closes the BookFormFragment.
   */
  public void closeFragment() {
    FragmentManager fragmentManager = getParentFragmentManager();
    if (fragmentManager.getBackStackEntryCount() > 0) {
      fragmentManager.popBackStack();
    } else {
      requireActivity().onBackPressed();
    }
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

  private void setupAddAuthorBtnListener(View view) {
    Button addAuthorBtn = view.findViewById(R.id.book_form_add_author_btn);

    addAuthorBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        switchToAuthorFragment();
      }
    });
  }

  private void switchToAuthorFragment() { // TODO
    AuthorFragment authorFragment = new AuthorFragment(authorList,
        new AuthorFragment.ChangeAuthorListListener() {
          @Override
          public void onAuthorListChanged() {
            // TODO maybe not necessary
          }
        });

    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, authorFragment, LibraryKeys.FRAGMENT_AUTHOR)
        .addToBackStack(null)
        .commit();
  }

  private void handleUserInput() {
    validInput = true;

    handleIsbn();
    handleTitle();
    handleSubtitle();
    handlePubYear();
    handlePublisher();
    handleVolume();
    handleEdition();
    handleAddInfos();

    if (!validInput) {
      return;
    }
    if (book.getId() == 0) {
      listener.onBookAdded(book, authorList);
    } else {
      listener.onBookChanged(book, authorList);
    }

    closeFragment();
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


  public interface ChangeBookListener {
    default void onBookAdded(Book book, List<Author> authorList) {
    }

    default void onBookChanged(Book book, List<Author> authorList) {
    }
  }

}
