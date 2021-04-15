package de.bibbuddy;

import android.app.AlertDialog;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import com.tsuryo.swipeablerv.SwipeLeftRightCallback;
import com.tsuryo.swipeablerv.SwipeableRecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * The AuthorFragment is responsible for the author of a book.
 *
 * @author Sarah Kurek, Luis Moßburger
 */
public class AuthorFragment extends BackStackFragment
    implements AuthorRecyclerViewAdapter.AuthorListener, SwipeLeftRightCallback.Listener {

  private final ChangeAuthorListListener listener;
  private final List<Author> authorList;
  private final List<AuthorItem> selectedAuthorItems = new ArrayList<>();

  private View view;
  private AuthorRecyclerViewAdapter adapter;

  public AuthorFragment(List<Author> authorList, ChangeAuthorListListener listener) {
    this.authorList = new ArrayList<>(authorList);
    this.listener = listener;
  }

  @Override
  protected void onBackPressed() {
    if (selectedAuthorItems.isEmpty()) {
      closeFragment();
    } else {
      deselectAuthorItems();
    }
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    enableBackPressedHandler();

    view = inflater.inflate(R.layout.fragment_author, container, false);

    setupRecyclerView();
    setupMainActivity();

    selectedAuthorItems.clear();
    updateEmptyView();

    setHasOptionsMenu(true);
    createAddAuthorListener();
    confirmAuthorsBtnListener(view);

    return view;
  }

  private void setupRecyclerView() {
    SwipeableRecyclerView recyclerView = view.findViewById(R.id.author_recycler_view);
    adapter = new AuthorRecyclerViewAdapter(this, authorList);
    recyclerView.setAdapter(adapter);
    recyclerView.setListener(this);
  }

  private void setupMainActivity() {
    MainActivity mainActivity = (MainActivity) requireActivity();

    mainActivity.setVisibilityImportShareButton(View.GONE, View.GONE);
    mainActivity.setVisibilitySortButton(false);

    mainActivity.updateHeaderFragment(getString(R.string.add_author_btn));
    mainActivity.updateNavigationFragment(R.id.navigation_library);
  }

  private void updateEmptyView() {
    TextView emptyView = view.findViewById(R.id.list_view_author_empty);
    if (adapter.getItemCount() == 0) {
      emptyView.setVisibility(View.VISIBLE);
    } else {
      emptyView.setVisibility(View.GONE);
    }
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_author_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    switch (item.getItemId()) {
      case R.id.menu_delete_author:
        deleteAuthors();
        break;

      case R.id.menu_help_author:
        authorManualFragment();
        break;

      case R.id.menu_imprint:
        ((MainActivity) requireActivity()).openImprint();
        break;

      default:
        throw new IllegalArgumentException();
    }

    return super.onOptionsItemSelected(item);
  }

  private void deleteAuthors() {
    AlertDialog.Builder alertDeleteAuthor = new AlertDialog.Builder(requireContext());
    alertDeleteAuthor.setCancelable(false);

    if (selectedAuthorItems.size() > 1) {
      alertDeleteAuthor.setTitle(R.string.delete_authors);
      alertDeleteAuthor.setMessage(
          getString(R.string.delete_authors_message) + assembleAlertString());
    } else {
      alertDeleteAuthor.setTitle(R.string.delete_author);
      alertDeleteAuthor.setMessage(
          getString(R.string.delete_author_message) + assembleAlertString());
    }

    alertDeleteAuthor.setNegativeButton(R.string.cancel, (dialog, which) -> deselectAuthorItems());

    alertDeleteAuthor.setPositiveButton(R.string.delete, (dialog, which) -> performDeleteAuthors());

    alertDeleteAuthor.show();
  }

  private String assembleAlertString() {
    return convertAuthorListToString(selectedAuthorItems)
        + getString(R.string.finally_delete) + " "
        + getString(R.string.delete_warning);
  }

  private String convertAuthorListToString(List<AuthorItem> authorList) {
    StringBuilder authors = new StringBuilder();

    int counter = 1;
    for (AuthorItem author : authorList) {
      authors.append(" \"");

      if (author.getTitle() != null && !author.getTitle().isEmpty()) {
        authors.append(author.getTitle()).append(" ");
      }

      authors.append(author.getFirstName()).append(" ").append(author.getLastName()).append("\"");
      if (counter != authorList.size()) {
        authors.append(",");
      }

      authors.append(" ");
      ++counter;
    }

    return authors.toString();
  }

  private void performDeleteAuthors() {
    assert (!selectedAuthorItems.isEmpty());
    final int authorsNumber = selectedAuthorItems.size();

    for (AuthorItem authorItem : selectedAuthorItems) {
      authorList.remove(authorItem.getAuthor());
    }

    deselectAuthorItems();
    adapter.notifyDataSetChanged();

    if (authorsNumber > 1) {
      Toast.makeText(requireContext(), getString(R.string.deleted_authors), Toast.LENGTH_SHORT)
          .show();
    } else {
      Toast.makeText(requireContext(), getString(R.string.deleted_author), Toast.LENGTH_SHORT)
          .show();
    }

    updateEmptyView();
  }

  private void authorManualFragment() {
    String htmlAsString = getString(R.string.author_help_text);

    Bundle bundle = new Bundle();
    bundle.putString(LibraryKeys.MANUAL_TEXT, htmlAsString);

    HelpFragment helpFragment = new HelpFragment();
    helpFragment.setArguments(bundle);

    helpFragment
        .show(requireActivity().getSupportFragmentManager(), LibraryKeys.FRAGMENT_HELP_VIEW);
  }

  private void deselectAuthorItems() {
    SwipeableRecyclerView authorListView = requireView().findViewById(R.id.author_recycler_view);
    for (int i = 0; i < authorListView.getChildCount(); i++) {
      authorListView.getChildAt(i).setSelected(false);
    }

    selectedAuthorItems.clear();
  }


  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    MenuItem deleteAuthors = menu.findItem(R.id.menu_delete_author);

    deleteAuthors.setVisible(selectedAuthorItems != null && !selectedAuthorItems.isEmpty());
  }

  private void createAddAuthorListener() {
    FloatingActionButton addAuthorBtn = view.findViewById(R.id.add_btn);

    addAuthorBtn.setOnClickListener(v -> handleEditAuthor(new Author()));
  }

  private void handleEditAuthor(Author author) {
    AuthorFormFragment authorFormFragment
        = new AuthorFormFragment(author,
                                 (author1, isNew) -> {
                                   if (!isNew) {
                                     authorList.remove(author1.getCache());
                                   }

                                   authorList.add(author1);
                                   adapter.notifyDataSetChanged();

                                   Toast.makeText(requireContext(),
                                                  getString(R.string.changed_author),
                                                  Toast.LENGTH_SHORT)
                                       .show();
                                 });

    showFragment(authorFormFragment);
  }

  private void confirmAuthorsBtnListener(View view) {
    FloatingActionButton addBookBtn = view.findViewById(R.id.confirm_btn);

    addBookBtn.setOnClickListener(v -> {
      listener.onAuthorListChanged(authorList);
      closeFragment();
    });
  }

  @Override
  public void onAuthorClicked(int position) {
    handleEditAuthor(authorList.get(position));
  }

  @Override
  public void onAuthorLongClicked(int position, AuthorItem authorItem, View view) {
    if (view.isSelected()) {
      view.setSelected(false);
      selectedAuthorItems.remove(authorItem);
    } else {
      view.setSelected(true);
      selectedAuthorItems.add(authorItem);
    }
  }

  @Override
  public void onSwipedLeft(int position) {
    deselectAuthorItems();
    selectedAuthorItems.add(adapter.getAuthorItem(position));
    deleteAuthors();
    adapter.notifyDataSetChanged();
  }

  @Override
  public void onSwipedRight(int position) {
    onAuthorClicked(position);
  }

  public interface ChangeAuthorListListener {
    void onAuthorListChanged(List<Author> authorList);
  }

}
