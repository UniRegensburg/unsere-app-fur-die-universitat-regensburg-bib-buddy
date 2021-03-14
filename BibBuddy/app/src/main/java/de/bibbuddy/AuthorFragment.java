package de.bibbuddy;

import android.app.AlertDialog;
import android.content.Context;
import android.content.DialogInterface;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.ArrayList;
import java.util.List;

/**
 * The AuthorFragment is responsible for the author of a book.
 *
 * @author Sarah Kurek
 */
public class AuthorFragment extends Fragment implements AuthorRecyclerViewAdapter.AuthorListener {

  private final ChangeAuthorListListener listener;
  private final List<Author> authorList;
  private View view;
  private Context context;
  private AuthorRecyclerViewAdapter adapter;
  private List<AuthorItem> selectedAuthorItems;
  private int redColor;
  private int greenColor;

  public AuthorFragment(List<Author> authorList, ChangeAuthorListListener listener) {
    this.authorList = authorList;
    this.listener = listener;
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

    view = inflater.inflate(R.layout.fragment_author, container, false);
    context = view.getContext();

    RecyclerView recyclerView = view.findViewById(R.id.author_recycler_view);
    adapter = new AuthorRecyclerViewAdapter(this, authorList, getContext());
    recyclerView.setAdapter(adapter);

    ((MainActivity) getActivity()).setVisibilityImportShareButton(View.INVISIBLE, View.INVISIBLE);
    ((MainActivity) getActivity()).updateHeaderFragment(getString(R.string.add_author_btn));

    selectedAuthorItems = new ArrayList<>();
    updateEmptyView();

    redColor = getResources().getColor(R.color.alert_red, null);
    greenColor = getResources().getColor(R.color.green, null);
    setHasOptionsMenu(true);
    createAddAuthorListener();

    return view;
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

      default:
        Toast.makeText(getContext(), "Fehler", Toast.LENGTH_SHORT).show();
    }

    return super.onOptionsItemSelected(item);
  }

  private void deleteAuthors() {
    AlertDialog.Builder alertDeleteAuthor = new AlertDialog.Builder(context);

    alertDeleteAuthor.setCancelable(false);
    alertDeleteAuthor.setTitle(R.string.delete_author);
    alertDeleteAuthor.setMessage(R.string.delete_author_message);

    alertDeleteAuthor.setNegativeButton(R.string.back, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
      }
    });

    alertDeleteAuthor.setPositiveButton(R.string.delete, new DialogInterface.OnClickListener() {
      @Override
      public void onClick(DialogInterface dialog, int which) {
        assert (!selectedAuthorItems.isEmpty());

        Author author = selectedAuthorItems.get(0).getAuthor();
        unselectAuthorItems();

        authorList.remove(author);
        adapter.notifyDataSetChanged();

        Toast.makeText(context, getString(R.string.deleted_author), Toast.LENGTH_SHORT).show();
        updateEmptyView();
      }
    });

    alertDeleteAuthor.show();
  }

  private void authorManualFragment() {
    HelpFragment helpFragment = new HelpFragment();
    String htmlAsString = getString(R.string.author_help_text);

    Bundle bundle = new Bundle();

    bundle.putString(LibraryKeys.MANUAL_TEXT, htmlAsString);
    helpFragment.setArguments(bundle);

    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, helpFragment,
            LibraryKeys.FRAGMENT_HELP_VIEW)
        .addToBackStack(null)
        .commit();
  }

  private void unselectAuthorItems() {
    RecyclerView authorListView = getView().findViewById(R.id.author_recycler_view);
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
    FloatingActionButton addAuthorBtn = view.findViewById(R.id.btn_add_author);

    addAuthorBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleEditAuthor(new Author());
      }
    });
  }

  private void handleEditAuthor(Author author) {
    AuthorFormFragment authorFormFragment = new AuthorFormFragment(author,
        new AuthorFormFragment.ChangeAuthorListener() {
          @Override
          public void onAuthorChanged(Author author, boolean isNew) {
            if (isNew) {
              authorList.add(author);
            }

            adapter.notifyDataSetChanged();
          }
        });

    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, authorFormFragment)
        .setReorderingAllowed(true)
        .addToBackStack(null)
        .commit();
  }

  private void closeFragment() {
    listener.onAuthorListChanged();

    FragmentManager manager = getParentFragmentManager();
    if (manager.getBackStackEntryCount() > 0) {
      manager.popBackStack();
    } else {
      requireActivity().onBackPressed();
    }
  }

  @Override
  public void onItemClicked(int position) {
    handleEditAuthor(authorList.get(position));
  }

  @Override
  public void onLongItemClicked(int position, AuthorItem authorItem, View view) {
    if (view.isSelected()) {
      view.setSelected(false);
      selectedAuthorItems.remove(authorItem);
    } else {
      view.setSelected(true);
      selectedAuthorItems.add(authorItem);
    }
  }

  public interface ChangeAuthorListListener {
    void onAuthorListChanged();
  }

}
