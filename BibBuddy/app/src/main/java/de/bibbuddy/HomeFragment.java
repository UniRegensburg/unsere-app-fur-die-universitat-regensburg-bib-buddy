package de.bibbuddy;

import android.content.Context;
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

/**
 * The HomeFragment is responsible for welcoming the user and displaying recently used books.
 *
 * @author Claudia Schönherr, Luis Moßburger
 */
public class HomeFragment extends BackStackFragment
    implements BookRecyclerViewAdapter.BookListener {

  private View view;
  private Context context;

  private BookDao bookDao;
  private BookModel bookModel;
  private DatabaseHelper dbHelper;

  private int bookAmount;
  List<BookItem> bookItemList;
  private BookRecyclerViewAdapter adapter;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    view = inflater.inflate(R.layout.fragment_home, container, false);
    context = view.getContext();

    MainActivity mainActivity = (MainActivity) requireActivity();
    mainActivity.setVisibilityImportShareButton(View.GONE, View.GONE);
    mainActivity.setVisibilitySortButton(false);

    mainActivity.updateHeaderFragment(getString(R.string.navigation_home));
    mainActivity.updateNavigationFragment(R.id.navigation_home);

    bookModel = new BookModel(context, 1L);
    dbHelper = new DatabaseHelper(context);
    bookDao = new BookDao(dbHelper);

    bookAmount = 3;
    List<Book> bookList = bookDao.findModifiedBooks(bookAmount);
    bookItemList = getBookItems(bookList);

    adapter = new BookRecyclerViewAdapter(bookItemList, this, getContext());

    setupBooksRecyclerView(bookItemList);

    return view;
  }

  private void setupBooksRecyclerView(List<BookItem> bookItemList) {
    RecyclerView recyclerView = view.findViewById(R.id.home_books_list);
    adapter = new BookRecyclerViewAdapter(bookItemList, this, getContext());
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

  @Override
  public void onBookClicked(int position) {
    BookItem bookItem = bookItemList.get(position);

    BookNotesView fragment = new BookNotesView();
    fragment.setArguments(createBookBundle(bookItem));

    showFragment(fragment);
  }

  @Override
  public void onBookLongClicked(int position, BookItem bookItem, View v) {
  }

  private Bundle createBookBundle(LibraryItem item) {
    Bundle bundle = new Bundle();

    Long currentBookId = item.getId();
    String currentBookTitle = item.getName();

    bundle.putLong(LibraryKeys.SHELF_ID, bookDao.findShelfIdByBook(item.getId()));
    bundle.putString(LibraryKeys.SHELF_NAME, bookDao.findShelfNameByBook(item.getId()));

    bundle.putLong(LibraryKeys.BOOK_ID, currentBookId);
    bundle.putString(LibraryKeys.BOOK_TITLE, currentBookTitle);

    return bundle;
  }

  private List<BookItem> getBookItems(List<Book> bookList) {
    List<BookItem> bookItemList = new ArrayList<BookItem>();

    for (Book book : bookList) {
      bookModel.setShelfId(bookDao.findShelfIdByBook(book.getId()));

      bookItemList.add(
          new BookItem(
              book,
              bookDao.findShelfIdByBook(book.getId()),
              bookModel.getAuthorString(book.getId()),
              bookDao.countAllNotesForBook(book.getId())
          )
      );
    }

    return bookItemList;
  }

}

