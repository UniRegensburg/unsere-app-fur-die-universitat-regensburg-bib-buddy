package de.bibbuddy;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.util.List;

/**
 * The BookOnlineFragment is responsible for the online search of books.
 *
 * @author Claudia Schönherr, Luis Moßburger
 */
public class BookOnlineFragment extends BackStackFragment
    implements BookFormFragment.ChangeBookListener {

  private View view;

  private EditText searchFieldText;
  private Long shelfId;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    view = inflater.inflate(R.layout.fragment_book_online, container, false);
    setupSearchInput();

    Bundle bundle = this.getArguments();
    shelfId = bundle.getLong(LibraryKeys.SHELF_ID);

    MainActivity mainActivity = (MainActivity) requireActivity();
    mainActivity.setVisibilityImportShareButton(View.GONE, View.GONE);
    mainActivity.setVisibilitySortButton(false);
    mainActivity.updateNavigationFragment(R.id.navigation_library);

    return view;
  }

  private void handleIsbnInput() {
    String textInput = searchFieldText.getText().toString().replaceAll("\\s", "");

    if (DataValidation.isValidIsbn10or13(textInput)) {
      IsbnRetriever isbnRetriever = new IsbnRetriever(searchFieldText.getText().toString());
      Thread thread = new Thread(isbnRetriever);
      thread.start();

      try {
        thread.join();
      } catch (Exception e) {
        e.printStackTrace();
      }

      // retrieve metadata that was saved
      Book book = isbnRetriever.getBook();
      List<Author> authors = isbnRetriever.getAuthors();
      if (book != null) {
        handleAddBook(book, authors);
      } else {
        Toast.makeText(requireActivity(), getString(R.string.isbn_not_found),
                       Toast.LENGTH_LONG).show();
      }
    } else {
      Toast.makeText(requireActivity(), getString(R.string.isbn_not_valid),
                     Toast.LENGTH_LONG).show();
    }
  }

  private void handleAddBook(Book book, List<Author> authors) {
    BookFormFragment bookFormFragment = new BookFormFragment(this, book, authors);
    showFragment(bookFormFragment);
  }

  @Override
  public void onBookAdded(Book book, List<Author> authorList) {
    BookDao bookDao = new BookDao(new DatabaseHelper(requireContext()));
    bookDao.create(book, authorList, shelfId);

    requireActivity()
        .runOnUiThread(() -> Toast.makeText(requireActivity(), getString(R.string.added_book),
                                            Toast.LENGTH_SHORT).show());

    closeFragment();
  }

  private void setupSearchInput() {
    searchFieldText = view.findViewById(R.id.search_input);
    searchFieldText.setHint(R.string.add_book_online_text);

    searchFieldText.setOnKeyListener(new View.OnKeyListener() {
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        if (event.getAction() != KeyEvent.ACTION_UP) {
          return false;
        }

        switch (keyCode) {
          case KeyEvent.KEYCODE_NUMPAD_ENTER:
          case KeyEvent.KEYCODE_ENTER:
            handleIsbnInput();
            return true;

          default:
            return false;
        }
      }
    });
  }

}
