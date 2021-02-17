package de.bibbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

/**
 * The BookFragment is responsible for the current books of a shelf in the library.
 *
 * @author Claudia Schönherr
 */
public class BookFragment extends Fragment implements BookRecyclerViewAdapter.BookListener {
  private Long shelfId;
  private String shelfName;
  private View view;

  private BookModel bookModel;
  private BookRecyclerViewAdapter adapter;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

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

    view = inflater.inflate(R.layout.fragment_book, container, false);

    Bundle bundle = this.getArguments();
    shelfName = bundle.getString(LibraryKeys.SHELF_NAME);
    shelfId = bundle.getLong(LibraryKeys.SHELF_ID);
    bookModel = new BookModel(getContext(), shelfId);
    List<BookItem> bookList = bookModel.getBookList(shelfId);

    RecyclerView recyclerView = view.findViewById(R.id.book_recycler_view);
    adapter = new BookRecyclerViewAdapter(bookList, this, getContext());
    recyclerView.setAdapter(adapter);

    //createBackBtnListener();
    createAddBookListener();
    updateEmptyView(bookList);
    ((MainActivity) getActivity()).updateHeaderFragment(shelfName);

    return view;
  }

  private Bundle createBookBundle(LibraryItem item) {
    Bundle bundle = new Bundle();

    Long currentBookId = item.getId();
    String currentBookTitle = item.getName();

    bundle.putLong(LibraryKeys.SHELF_ID, shelfId);
    bundle.putString(LibraryKeys.SHELF_NAME, shelfName);

    bundle.putLong(LibraryKeys.BOOK_ID, currentBookId);
    bundle.putString(LibraryKeys.BOOK_TITLE, currentBookTitle);

    return bundle;
  }

  private Bundle createBookBundle() {
    Bundle bundle = new Bundle();
    bundle.putString(LibraryKeys.SHELF_NAME, shelfName);
    bundle.putLong(LibraryKeys.SHELF_ID, shelfId);

    return bundle;
  }

  @Override
  public void onItemClicked(int position) {
    BookItem bookItem = bookModel.getSelectedBookItem(position);
    ((MainActivity) getActivity()).updateHeaderFragment(bookItem.getName());

    FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    BookNotesView fragment = new BookNotesView();
    fragmentTransaction.replace(R.id.fragment_container_view, fragment);
    fragmentTransaction.setReorderingAllowed(true);
    fragmentTransaction.addToBackStack(null);
    fragmentTransaction.commit();

    fragment.setArguments(createBookBundle(bookItem));
  }

  private void updateEmptyView(List<BookItem> bookList) {
    TextView emptyView = view.findViewById(R.id.list_view_book_empty);
    if (bookList.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
    } else {
      emptyView.setVisibility(View.GONE);
    }
  }

  private void createAddBookListener() {
    FloatingActionButton addBookBtn = view.findViewById(R.id.btn_add_book);
    PopupMenu pm = new PopupMenu(getContext(), addBookBtn);
    pm.getMenuInflater().inflate(R.menu.add_book_menu, pm.getMenu());

    pm.setOnMenuItemClickListener(new PopupMenu.OnMenuItemClickListener() {
      @Override
      public boolean onMenuItemClick(MenuItem item) {
        if (item.getItemId() == R.id.add_book_scan) {
          Toast.makeText(getContext(), getString(R.string.add_book_scan_text), Toast.LENGTH_SHORT)
              .show();
        } else if (item.getItemId() == R.id.add_book_online) {
          Toast.makeText(getContext(), getString(R.string.add_book_online_text), Toast.LENGTH_SHORT)
              .show();
        } else if (item.getItemId() == R.id.add_book_manually) {
          handleAddBookManually();
        }

        return true;
      }
    });


    addBookBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        pm.show();
      }
    });
  }

  private void handleAddBookManually() {
    BookFormFragment fragment = new BookFormFragment(
        new BookFormFragment.AddBookManuallyListener() {
          @Override
          public void onBookAdded(Book book, List<Author> authorList) {
            addBook(book, authorList);
            Toast.makeText(getContext(), getString(R.string.added_book), Toast.LENGTH_SHORT).show();
          }
        });

    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, fragment)
        .setReorderingAllowed(true)
        .addToBackStack(null)
        .commit();

    fragment.setArguments(createBookBundle());
  }

  private void addBook(Book book, List<Author> authorList) {
    bookModel.addBook(book, authorList);
    Toast.makeText(getContext(), getString(R.string.added_book), Toast.LENGTH_SHORT).show();
    adapter.notifyDataSetChanged();
    updateEmptyView(bookModel.getCurrentBookList());
  }

  /*private void createBackBtnListener() {
    TextView backView = view.findViewById(R.id.text_view_back_to);

    backView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        LibraryFragment fragment = new LibraryFragment();
        getActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container_view, fragment, LibraryKeys.FRAGMENT_LIBRARY)
            .addToBackStack(null)
            .commit();
      }
    });
  }*/
}
