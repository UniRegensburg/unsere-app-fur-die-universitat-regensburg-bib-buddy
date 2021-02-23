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
import com.google.android.material.textfield.TextInputLayout;

/**
 * The SearchFragment is responsible for the search of books.
 *
 * @author Claudia Schönherr, Luis Moßburger
 */
public class SearchFragment extends Fragment {

  private TextInputLayout searchField;
  private EditText searchFieldText;
  private Thread thread;
  private IsbnRetriever isbnRetriever;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_search, container, false);
    searchField = view.findViewById(R.id.searchField);
    searchFieldText = searchField.getEditText();
    searchFieldText.setOnKeyListener(new View.OnKeyListener() {
      public boolean onKey(View v, int keyCode, KeyEvent event) {
        handleIsbnInput(keyCode, event);
        return true;
      }
    });
    return view;
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
        //Retrieve metadata that was saved
        Book book = isbnRetriever.getBook();
        if (book != null) {
          // @ClaudiaSchönherr - continue here with book to UI implementation :)
          System.out.println(book.getTitle());
        } else {
          Toast.makeText(getActivity(), getString(R.string.isbn_not_found),
              Toast.LENGTH_LONG).show();
        }
      }
    }
  }
}
