package de.bibbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import com.google.android.material.floatingactionbutton.FloatingActionButton;

/**
 * The AuthorFormFragment is responsible for adding author to a book.
 *
 * @author Sarah Kurek
 */
public class AuthorFormFragment extends Fragment {
  private final AuthorFormFragment.ChangeAuthorListener listener;
  private final Author author;
  private final boolean isNewAuthor;
  private boolean validInput;
  private int redColor;
  private int greenColor;

  /**
   * Constructor that sets the author to empty when it is newly created.
   *
   * @param author   author object
   * @param listener change listener
   */
  public AuthorFormFragment(Author author, AuthorFormFragment.ChangeAuthorListener listener) {
    this.author = author;
    this.listener = listener;
    this.isNewAuthor = author.isEmpty();
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
    View view = inflater.inflate(R.layout.fragment_author_form, container, false);

    setInputText(view);

    redColor = getResources().getColor(R.color.alert_red, null);
    greenColor = getResources().getColor(R.color.green, null);
    setupAddAuthorBtnListener(view);

    return view;
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
    FloatingActionButton addAuthorBtn = view.findViewById(R.id.author_form_add_author_btn);

    addAuthorBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleAuthorInput();
      }
    });
  }

  private void handleAuthorInput() {
    validInput = true;

    EditText authorTitleInput = getView().findViewById(R.id.author_form_title_input);
    authorTitleInput.setBackgroundColor(greenColor);
    author.setTitle(authorTitleInput.getText().toString());

    EditText authorFirstNameInput = getView().findViewById(R.id.author_form_first_name_input);
    String authorFirstName = authorFirstNameInput.getText().toString();
    if (authorFirstName.isEmpty()) {
      authorFirstNameInput.setBackgroundColor(redColor);
      validInput = false;
    } else {
      authorFirstNameInput.setBackgroundColor(greenColor);
      author.setFirstName(authorFirstName);
    }

    EditText authorLastNameInput = getView().findViewById(R.id.author_form_last_name_input);
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

  private void closeFragment() {
    FragmentManager fragmentManager = getParentFragmentManager();
    if (fragmentManager.getBackStackEntryCount() > 0) {
      fragmentManager.popBackStack();
    } else {
      requireActivity().onBackPressed();
    }
  }

  public interface ChangeAuthorListener {
    void onAuthorChanged(Author author, boolean isNew);
  }

}
