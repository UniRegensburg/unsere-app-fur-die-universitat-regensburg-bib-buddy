package de.bibbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
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

    ((MainActivity) getActivity()).updateHeaderFragment(getString(R.string.add_author_btn));
    selectedAuthorItems = new ArrayList<AuthorItem>();
    updateEmptyView(adapter.GetAuthorItemList());

    redColor = getResources().getColor(R.color.alert_red);
    greenColor = getResources().getColor(R.color.green);
    setHasOptionsMenu(true);
    createAddAuthorListener();

    return view;
  }

  // TODO create Options menu with delete and change author and help

  private void updateEmptyView(List<AuthorItem> authorList) {
    TextView emptyView = view.findViewById(R.id.list_view_author_empty);
    if (authorList.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
    } else {
      emptyView.setVisibility(View.GONE);
    }
  }


  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    //MenuItem deleteAuthor= menu.findItem(R.id.menu_delete_author);

    if (selectedAuthorItems == null || selectedAuthorItems.isEmpty()) {
      //deleteAuthor.setVisible(false);
    } else if (selectedAuthorItems.size() == 1) {
      //deleteAuthor.setVisible(true);
    } else {
      //deleteAuthor.setVisible(true);
    }
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

    FragmentManager fm = getParentFragmentManager();
    if (fm.getBackStackEntryCount() > 0) {
      fm.popBackStack();
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
