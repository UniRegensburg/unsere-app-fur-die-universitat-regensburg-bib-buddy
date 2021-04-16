package de.bibbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * HomeFragment is responsible for welcoming the user and displaying recently used books.
 *
 * @author Luis Moßburger
 */
public class HomeFragment extends BackStackFragment
    implements BookRecyclerViewAdapter.BookListener {

  private View view;
  private BookModel bookModel;
  private List<BookItem> bookItemList;

  private void setupMainActivity() {
    MainActivity mainActivity = (MainActivity) requireActivity();

    mainActivity.setVisibilityImportShareBtn(View.GONE, View.GONE);
    mainActivity.setVisibilitySortBtn(false);

    mainActivity.updateHeaderFragment(getString(R.string.navigation_home));
    mainActivity.updateNavigationFragment(R.id.navigation_home);
  }

  private void setupBooksRecyclerView() {
    RecyclerView recyclerView = view.findViewById(R.id.home_books_list);

    bookModel = new BookModel(view.getContext(), 1L);
    int bookAmount = 3;
    List<Book> bookList = bookModel.findModifiedBooks(bookAmount);
    bookItemList = getBookItems(bookList);

    BookRecyclerViewAdapter adapter =
        new BookRecyclerViewAdapter(bookItemList, this, requireContext());
    recyclerView.setAdapter(adapter);

    updateEmptyView(bookItemList);
  }

  private void updateEmptyView(List<BookItem> bookList) {
    TextView emptyView = view.findViewById(R.id.home_books_list_empty);

    if (bookList.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
    } else {
      emptyView.setVisibility(View.GONE);
    }
  }


  private Bundle createBookBundle(LibraryItem item) {
    Bundle bundle = new Bundle();

    Long currentBookId = item.getId();
    String currentBookTitle = item.getName();

    bundle.putLong(LibraryKeys.SHELF_ID, bookModel.findShelfIdByBook(item.getId()));
    bundle.putString(LibraryKeys.SHELF_NAME, bookModel.findShelfNameByBook(item.getId()));

    bundle.putLong(LibraryKeys.BOOK_ID, currentBookId);
    bundle.putString(LibraryKeys.BOOK_TITLE, currentBookTitle);

    return bundle;
  }

  private List<BookItem> getBookItems(List<Book> bookList) {
    List<BookItem> bookItemList = new ArrayList<>();

    for (Book book : bookList) {
      bookModel.setShelfId(bookModel.findShelfIdByBook(book.getId()));

      bookItemList.add(
          new BookItem(
              book,
              bookModel.findShelfIdByBook(book.getId()),
              bookModel.getAuthorString(book.getId()),
              bookModel.countAllNotesForBook(book.getId())
          )
      );
    }

    return bookItemList;
  }

  private void updateWelcomeMessage() {
    TextView welcomeMessage = view.findViewById(R.id.welcome_msg);

    MainActivity mainActivity = (MainActivity) requireActivity();
    if (mainActivity.getWelcomeMessage().equals("")) {
      int randomMessage = new Random().nextInt(4);

      if (randomMessage == 1) {
        mainActivity.setWelcomeMessage(getString(R.string.welcome_2));
      } else if (randomMessage == 2) {
        mainActivity.setWelcomeMessage(getString(R.string.welcome_3));
      } else if (randomMessage == 3) {
        mainActivity.setWelcomeMessage(getString(R.string.welcome_4));
      } else {
        mainActivity.setWelcomeMessage(getString(R.string.welcome_1));
      }
    }

    welcomeMessage.setText(mainActivity.getWelcomeMessage());
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    view = inflater.inflate(R.layout.fragment_home, container, false);

    setupMainActivity();

    updateWelcomeMessage();
    setupBooksRecyclerView();

    return view;
  }

  @Override
  public void onBookClicked(int position) {
    BookItem bookItem = bookItemList.get(position);

    BookNotesFragment bookNotesFragment = new BookNotesFragment();
    bookNotesFragment.setArguments(createBookBundle(bookItem));

    showFragment(bookNotesFragment);
  }

  @Override
  public void onBookLongClicked(BookItem bookItem, View v) {
  }

}
