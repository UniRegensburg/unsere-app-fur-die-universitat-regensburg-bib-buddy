package de.bibbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.floatingactionbutton.FloatingActionButton;
import java.util.List;

/**
 * The LibraryFragment is responsible for the shelfs in the library.
 *
 * @author Claudia Schönherr
 */
public class LibraryFragment extends Fragment
    implements LibraryRecyclerViewAdapter.LibraryListener, BookRecyclerViewAdapter.BookListener {

  private View view;
  private Context context;

  private LibraryModel libraryModel;
  private LibraryRecyclerViewAdapter adapter;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    // Called to have the fragment instantiate its user interface view.
    view = inflater.inflate(R.layout.fragment_library, container, false);
    context = view.getContext();

    setupRecyclerView();
    setupAddShelfBtn();
    ((MainActivity) getActivity()).updateHeaderFragment(getString(R.string.navigation_library));

    return view;
  }

  private void setupRecyclerView() {
    libraryModel = new LibraryModel(getContext());
    List<ShelfItem> libraryList = libraryModel.getLibraryList(null);

    RecyclerView libraryRecyclerView = view.findViewById(R.id.library_recycler_view);
    adapter = new LibraryRecyclerViewAdapter(libraryList, this, context);
    libraryRecyclerView.setAdapter(adapter);

    updateEmptyView(libraryList);
  }

  private void setupAddShelfBtn() {
    FloatingActionButton addShelfBtn = view.findViewById(R.id.btn_add_shelf);
    createAddShelfListener(addShelfBtn);
  }

  private void createAddShelfListener(FloatingActionButton addShelfBtn) {
    addShelfBtn.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        handleAddShelf();
      }
    });
  }

  private Bundle createAddShelfBundle() {
    Bundle bundle = new Bundle();

    Long currentShelfId = libraryModel.getShelfId();
    if (currentShelfId == null) {
      bundle.putLong(LibraryKeys.SHELF_ID, 0L);
    } else {
      bundle.putLong(LibraryKeys.SHELF_ID, currentShelfId);
    }

    List<ShelfItem> currentLibraryList = libraryModel.getCurrentLibraryList();
    String[] shelfNames = new String[currentLibraryList.size()];
    for (int i = 0; i < currentLibraryList.size(); i++) {
      shelfNames[i] = currentLibraryList.get(i).getName();
    }
    bundle.putStringArray(LibraryKeys.SHELF_NAMES, shelfNames);

    return bundle;
  }

  private void handleAddShelf() {
    LibraryAddShelfFragment fragment =
        new LibraryAddShelfFragment(new LibraryAddShelfFragment.AddShelfLibraryListener() {
          @Override
          public void onShelfAdded(String name, Long shelfId) {
            libraryModel.addShelf(name, libraryModel.getShelfId());
            updateLibraryListView(libraryModel.getCurrentLibraryList());
          }
        });

    fragment.setArguments(createAddShelfBundle());
    fragment.show(getActivity().getSupportFragmentManager(), LibraryKeys.DIALOG_FRAGMENT_ADD_NAME);
  }

  private void closeAddShelfFragment() {
    LibraryAddShelfFragment fragment =
        (LibraryAddShelfFragment) getActivity().getSupportFragmentManager()
            .findFragmentById(R.id.fragment_container_add_shelf);
    if (fragment != null) {
      fragment.closeFragment();
    }
  }

  private void updateEmptyView(List<ShelfItem> libraryList) {
    TextView emptyView = view.findViewById(R.id.list_view_library_empty);

    if (libraryList.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
    } else {
      emptyView.setVisibility(View.GONE);
    }
  }

  private void updateLibraryListView(List<ShelfItem> libraryList) {
    adapter.notifyDataSetChanged();
    updateEmptyView(libraryList);
  }

  private void updateBookListView(LibraryItem libraryItem) {
    BookFragment fragment = new BookFragment();
    fragment.setArguments(createShelfBundle(libraryItem));

    getActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, fragment)
        .setReorderingAllowed(true)
        .addToBackStack(null)
        .commit();
  }

  private Bundle createShelfBundle(LibraryItem libraryItem) {
    Bundle bundle = new Bundle();

    Long shelfId = libraryItem.getId();
    String shelfName = libraryItem.getName();

    bundle.putLong(LibraryKeys.SHELF_ID, shelfId);
    bundle.putString(LibraryKeys.SHELF_NAME, shelfName);

    return bundle;
  }

  @Override
  public void onItemClicked(int position) {
    closeAddShelfFragment();
    LibraryItem libraryItem = libraryModel.getSelectedLibraryItem(position);
    ((MainActivity) getActivity()).updateHeaderFragment(libraryItem.getName());
    updateBookListView(libraryItem);
  }

}
