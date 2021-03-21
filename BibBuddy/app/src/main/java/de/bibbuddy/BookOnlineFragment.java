package de.bibbuddy;

import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
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
public class BookOnlineFragment extends Fragment implements BookFormFragment.ChangeBookListener {

  private View view;

  private Thread thread;
  private IsbnRetriever isbnRetriever;
  private EditText searchFieldText;

  private Long shelfId;
  private String shelfName;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_book_online, container, false);

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

    setupSearchInput();
    setupSearchButton();

    Bundle bundle = this.getArguments();
    shelfName = bundle.getString(LibraryKeys.SHELF_NAME);
    shelfId = bundle.getLong(LibraryKeys.SHELF_ID);

    ((MainActivity) getActivity()).setVisibilityImportShareButton(View.GONE, View.GONE);
    ((MainActivity) getActivity()).setVisibilitySortButton(false);

    return view;
  }

  private Bundle createBookBundle() {
    Bundle bundle = new Bundle();
    bundle.putString(LibraryKeys.SHELF_NAME, shelfName);
    bundle.putLong(LibraryKeys.SHELF_ID, shelfId);

    return bundle;
  }

  private void closeFragmentAfterAdding() {
    Fragment fragment =
        requireActivity().getSupportFragmentManager().findFragmentByTag(LibraryKeys.FRAGMENT_BOOK);
    requireActivity().getSupportFragmentManager().beginTransaction().remove(fragment).commit();

    FragmentManager fragmentManager = getParentFragmentManager();
    if (fragmentManager.getBackStackEntryCount() > 0) {
      fragmentManager.popBackStack();
    } else {
      requireActivity().onBackPressed();
    }
  }

  private void handleIsbnInput() {
    String textInput = searchFieldText.getText().toString().replaceAll("\\s", "");

    if (DataValidation.isValidIsbn10or13(textInput)) {
      isbnRetriever = new IsbnRetriever(searchFieldText.getText().toString());
      thread = new Thread(isbnRetriever);
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

    requireActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, bookFormFragment, LibraryKeys.FRAGMENT_BOOK)
        .addToBackStack(null)
        .commit();
  }

  @Override
  public void onBookAdded(Book book, List<Author> authorList) {
    BookDao bookDao = new BookDao(new DatabaseHelper(getContext()));
    bookDao.create(book, authorList, shelfId);

    requireActivity().runOnUiThread(new Runnable() {
      public void run() {
        Toast.makeText(requireActivity(), getString(R.string.added_book),
            Toast.LENGTH_SHORT).show();
      }
    });

    closeFragmentAfterAdding();
  }

  private void setupSearchButton() {
    ImageButton searchBtn = view.findViewById(R.id.search_btn);

    searchBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleIsbnInput();
      }
    });
  }

  private void setupSearchInput() {
    searchFieldText = view.findViewById(R.id.search_input);
    searchFieldText.setHint(R.string.add_book_online_text);

    searchFieldText.setOnKeyListener(new View.OnKeyListener() {
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        handleSearchInput(keyCode, event);
        return true;
      }
    });
  }

  private void handleSearchInput(int keyCode, KeyEvent event) {
    if (keyCode == KeyEvent.KEYCODE_ENTER) {
      if (event.getAction() != KeyEvent.ACTION_DOWN) {
        handleIsbnInput();
      }
    }
  }

}
