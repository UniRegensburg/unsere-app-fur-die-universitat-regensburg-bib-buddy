package de.bibbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * The AuthorFormFragment is responsible for adding authors to a book.
 *
 * @author Sarah Kurek
 */
public class AuthorFormFragment extends BackStackFragment {

  private final AuthorFormFragment.ChangeAuthorListener listener;
  private final Author author;
  private final boolean isNewAuthor;

  private int redColor;
  private int greenColor;

  private void setupMainActivity() {
    MainActivity mainActivity = (MainActivity) requireActivity();

    mainActivity.setVisibilityImportShareBtn(View.GONE, View.GONE);
    mainActivity.setVisibilitySortBtn(false);

    mainActivity.updateHeaderFragment(getString(R.string.add_author_btn));
    mainActivity.updateNavigationFragment(R.id.navigation_library);
  }

  private void setInputText(View view) {
    EditText authorTitleInput = view.findViewById(R.id.author_form_title_input);
    authorTitleInput.setText(author.getTitle());

    EditText authorFirstNameInput = view.findViewById(R.id.author_form_first_name_input);
    authorFirstNameInput.setText(author.getFirstName());

    EditText authorLastNameInput = view.findViewById(R.id.author_form_last_name_input);
    authorLastNameInput.setText(author.getLastName());
  }

  private void setupAddAuthorBtnListener(View view) {
    FloatingActionButton addAuthorBtn = view.findViewById(R.id.confirm_btn);
    addAuthorBtn.setOnClickListener(v -> handleAuthorInput());
  }

  private void handleAuthorInput() {
    boolean validInput = true;
    View view = requireView();

    EditText authorTitleInput = view.findViewById(R.id.author_form_title_input);
    authorTitleInput.setBackgroundColor(greenColor);
    author.setTitle(authorTitleInput.getText().toString());

    EditText authorFirstNameInput = view.findViewById(R.id.author_form_first_name_input);
    String authorFirstName = authorFirstNameInput.getText().toString();
    if (authorFirstName.isEmpty()) {
      authorFirstNameInput.setBackgroundColor(redColor);
      validInput = false;
    } else {
      authorFirstNameInput.setBackgroundColor(greenColor);
      author.setFirstName(authorFirstName);
    }

    EditText authorLastNameInput = view.findViewById(R.id.author_form_last_name_input);
    String authorLastName = authorLastNameInput.getText().toString();
    if (authorLastName.isEmpty()) {
      authorLastNameInput.setBackgroundColor(redColor);
      validInput = false;
    } else {
      authorLastNameInput.setBackgroundColor(greenColor);
      author.setLastName(authorLastName);
    }

    if (validInput) {
      listener.onAuthorChanged(author, isNewAuthor);
      closeFragment();
    }
  }

  /**
   * Constructor that sets the author to empty when it is newly created.
   *
   * @param author   author object
   * @param listener change listener
   */
  public AuthorFormFragment(Author author, AuthorFormFragment.ChangeAuthorListener listener) {
    this.author = author.clone();
    this.author.setCache();
    this.listener = listener;
    this.isNewAuthor = author.isEmpty();
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_author_form, container, false);

    setInputText(view);
    setupMainActivity();

    redColor = getResources().getColor(R.color.red, null);
    greenColor = getResources().getColor(R.color.green, null);

    setupAddAuthorBtnListener(view);

    return view;
  }

  public interface ChangeAuthorListener {
    void onAuthorChanged(Author author, boolean isNew);
  }

}
