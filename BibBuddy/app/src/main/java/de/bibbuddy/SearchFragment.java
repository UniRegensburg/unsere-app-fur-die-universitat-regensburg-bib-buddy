package de.bibbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.KeyEvent;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.EditText;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * The SearchFragment is responsible for searching for books.
 *
 * @author Claudia Schönherr
 */
public class SearchFragment extends Fragment implements SearchRecyclerViewAdapter.SearchListener {
  private EditText searchInput;
  private View view;
  private Context context;
  private SearchModel searchModel;
  private SearchRecyclerViewAdapter adapter;
  private RecyclerView searchRecyclerView;
  private List<SearchItem> searchResultList;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_search, container, false);
    context = view.getContext();

    setupRecyclerView();
    setupSearchInput();
    setupSearchButton();

    return view;
  }

  private void setupRecyclerView() {
    searchRecyclerView = view.findViewById(R.id.search_recycler_view);

    searchModel = new SearchModel(context);

    searchResultList = new ArrayList<>();
    adapter = new SearchRecyclerViewAdapter(searchResultList, this);
    searchRecyclerView.setAdapter(adapter);

    updateEmptyView(searchResultList);
  }

  private void updateEmptyView(List<SearchItem> searchResultList) {
    TextView emptyView = view.findViewById(R.id.list_view_search_empty);

    if (searchResultList.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
    } else {
      emptyView.setVisibility(View.GONE);
    }
  }

  private void setupSearchButton() {
    ImageButton searchBtn = view.findViewById(R.id.search_btn);

    searchBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        searchItems();
      }
    });
  }

  private void setupSearchInput() {
    searchInput = view.findViewById(R.id.search_input);

    searchInput.setOnEditorActionListener(new TextView.OnEditorActionListener() {
      @Override
      public boolean onEditorAction(TextView v, int actionId, KeyEvent event) {
        searchItems();
        return false;
      }
    });
  }

  private void searchItems() {
    String searchInputStr = searchInput.getText().toString();

    if (DataValidation.isStringEmpty(searchInputStr)) {
      Toast.makeText(context, R.string.search_invalid_name, Toast.LENGTH_SHORT).show();
      return;
    }

    // TODO get filters
    // TODO sort Name, modDate
    // TODO search Results depending on filters
    Toast.makeText(context, "Suche ... " + searchInputStr, Toast.LENGTH_SHORT).show();

    searchResultList = searchModel.getSearchResultList(searchInputStr);
    // adapter.notifyDataSetChanged(); // it doesn't work
    adapter = new SearchRecyclerViewAdapter(searchResultList, this);
    searchRecyclerView.setAdapter(adapter);
    updateEmptyView(searchResultList);
  }


  @Override
  public void onItemClicked(int position) {
    // TODO open book in BookNotesView
    // TODO open note directly
    // TODO open shelf in BookFragment View
  }
}
