package de.bibbuddy;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.textfield.TextInputLayout;
import java.util.ArrayList;
import java.util.List;

/**
 * The BookOnlineFragment is responsible for the online search of books.
 *
 * @author Claudia Schönherr, Luis Moßburger
 */
public class BookOnlineFragment extends Fragment {

  private TextInputLayout searchField;
  private EditText searchFieldText;
  private Thread thread;
  private IsbnRetriever isbnRetriever;

  private Long shelfId;
  private String shelfName;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_book_online, container, false);

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

    searchField = view.findViewById(R.id.search_field);
    searchFieldText = searchField.getEditText();

    Bundle bundle = this.getArguments();
    shelfName = bundle.getString(LibraryKeys.SHELF_NAME);
    shelfId = bundle.getLong(LibraryKeys.SHELF_ID);

    searchFieldText.setOnKeyListener(new View.OnKeyListener() {
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        handleIsbnInput(keyCode, event);
        return true;
      }
    });

    ((MainActivity) getActivity()).setVisibleImportShareButton(View.INVISIBLE, View.INVISIBLE);

    return view;
  }

  private Bundle createBookBundle() {
    Bundle bundle = new Bundle();
    bundle.putString(LibraryKeys.SHELF_NAME, shelfName);
    bundle.putLong(LibraryKeys.SHELF_ID, shelfId);

    return bundle;
  }

  /**
   * Closes the BookOnlineFragment.
   */
  public void closeFragment() {
    BookFragment fragment = new BookFragment();
    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, fragment)
        .setReorderingAllowed(true)
        .addToBackStack(null)
        .commit();

    fragment.setArguments(createBookBundle());
  }

  private void handleIsbnInput(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_ENTER) {
      if (event.getAction() != KeyEvent.ACTION_DOWN) {
        isbnRetriever = new IsbnRetriever(searchFieldText.getText().toString());
        thread = new Thread(isbnRetriever);
        thread.start();

        try {
          thread.join();
        } catch (Exception e) {
          System.out.println(e);
        }

        // retrieve metadata that was saved
        Book book = isbnRetriever.getBook();
        List<Author> authors = isbnRetriever.getAuthors();
        if (book != null) {
          handleAddBook(book, authors);
        } else {
          Toast.makeText(getActivity(), getString(R.string.isbn_not_found),
              Toast.LENGTH_LONG).show();
        }
      }
    }
  }

  private void handleAddBook(Book book, List<Author> authors) {
    try {
      BookDao bookDao = new BookDao(new DatabaseHelper(getContext()));
      bookDao.create(book, authors, shelfId);

      Toast.makeText(getActivity(), getString(R.string.added_book),
          Toast.LENGTH_LONG).show();

      closeFragment();
    } catch (Exception e) {
      System.out.println(e);
    }
  }

}
