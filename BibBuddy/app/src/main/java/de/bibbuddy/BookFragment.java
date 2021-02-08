package de.bibbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.ArrayList;
import java.util.List;

public class BookFragment extends Fragment implements BookRecyclerViewAdapter.BookListener {
    private Long shelfId;
    private View view;
    private BookModel bookModel;
    private BookRecyclerViewAdapter adapter;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_book, container, false);


        Bundle bundle = this.getArguments();
        shelfId = bundle.getLong(LibraryKeys.SHELF_ID);
        bookModel = new BookModel(getContext(), shelfId);
        List<BookItem> bookList = bookModel.getBookList(shelfId);

        RecyclerView recyclerView = view.findViewById(R.id.book_recycler_view);
        adapter = new BookRecyclerViewAdapter(bookList, this, getContext());
        recyclerView.setAdapter(adapter);

        createBackBtnListener();
        createAddBookListener();
        updateEmptyView(bookList);

        return view;
    }

    private Bundle createBookBundle(LibraryItem item) {
        Bundle bundle = new Bundle();

        Long currentBookId = item.getId();
        String currentBookTitle = item.getName();

        bundle.putLong(LibraryKeys.BOOK_ID, currentBookId);
        bundle.putString(LibraryKeys.BOOK_TITLE, currentBookTitle);

        return bundle;
    }

    @Override
    public void onItemClicked(int position) {
        BookItem bookItem = bookModel.getSelectedBookItem(position);

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
        addBookBtn.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                // TODO currently a test book is added to the database
                //  until a form for adding a new book is implemented and input is validated
                Book book = new Book("978-3-16-148410-0", "Buch mit Regal ID " + shelfId, "testen von Büchern", 2020, "Testverlag", "Volume 1", "Edition 1", "zusätzliche Infos");
                List<Author> authorList = new ArrayList<>();
                authorList.add(new Author("Vorname", "Nachname"));
                authorList.add(new Author("Autorvorname", "Autornachname", "Dr"));

                bookModel.addBook(book, authorList);
                Toast.makeText(getContext(), getString(R.string.added_book), Toast.LENGTH_SHORT).show();
                adapter.notifyDataSetChanged();
                updateEmptyView(bookModel.getCurrentBookList());
            }
        });
    }

    private void createBackBtnListener() {
        TextView backView = view.findViewById(R.id.text_view_back_to);

        backView.setOnClickListener(v -> {
            LibraryFragment fragment = new LibraryFragment();
            getActivity().getSupportFragmentManager().beginTransaction()
                  .replace(R.id.fragment_container_view, fragment, "fragment_library")
                  .addToBackStack(null)
                  .commit();
        });
    }
}
