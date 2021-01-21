package de.bibbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LibraryFragment extends Fragment
      implements LibraryRecyclerViewAdapter.LibraryListener {

   private Context context;

   private LibraryModel libraryModel;
   private RecyclerView libraryRecyclerView;
   private List<LibraryItem> libraryList;


   @Override
   public void onCreate(@Nullable Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);

      createBackBtnListener();
   }

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      // Called to have the fragment instantiate its user interface view.
      View view = inflater.inflate(R.layout.fragment_library, container, false);
      context = view.getContext();

      libraryModel = new LibraryModel(getContext());
      libraryList = libraryModel.getLibraryList(null);

      libraryRecyclerView = view.findViewById(R.id.library_recycler_view);
      LibraryRecyclerViewAdapter adapter = new LibraryRecyclerViewAdapter(libraryList, this);
      libraryRecyclerView.setAdapter(adapter);

      ImageButton addShelfBtn = view.findViewById(R.id.btn_add_shelf);
      createAddShelfListener(addShelfBtn);

      return view;
   }

   private Bundle createAddShelfBundle() {
      Bundle bundle = new Bundle();

      Long currentShelfId = libraryModel.getShelfId();
      if (currentShelfId == null) {
         bundle.putLong(LibraryKeys.SHELF_ID, 0L);
      } else {
         bundle.putLong(LibraryKeys.SHELF_ID, currentShelfId);
      }

      List<LibraryItem> currentLibraryList = libraryModel.getCurrentLibraryList();
      String[] shelfNames = new String[currentLibraryList.size()];
      for (int i = 0; i < currentLibraryList.size(); i++) {
         shelfNames[i] = currentLibraryList.get(i).getName();
      }
      bundle.putStringArray(LibraryKeys.SHELF_NAMES, shelfNames);

      return bundle;
   }


   private void createAddShelfListener(ImageButton addShelfBtn) {
      addShelfBtn.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {

            Fragment fragment = new LibraryAddShelfFragment(new LibraryAddShelfFragment.AddShelfLibraryListener() {
               @Override
               public void onShelfAdded(String name, Long shelfId) {
                  libraryModel.addShelf(name, libraryModel.getShelfId());
                  updateLibraryListView(libraryModel.getCurrentLibraryList());
               }
            });

            fragment.setArguments(createAddShelfBundle());
            FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
            FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
            fragmentTransaction.replace(R.id.fragment_container_add_shelf, fragment);
            fragmentTransaction.setReorderingAllowed(true);
            fragmentTransaction.addToBackStack(null);
            fragmentTransaction.commit();
         }
      });
   }

   private void createBackBtnListener() {
      closeAddShelfFragment();

      Toolbar toolbar = getActivity().findViewById(R.id.toolbar);

      toolbar.setNavigationOnClickListener(v -> {
         Long shelfId = libraryModel.getShelfId(); // get parentId of the current shelf

         updateLibraryListView(libraryModel.getPreviousLibraryList(shelfId));
         updateHeader(libraryModel.getShelfName());
      });
   }

   private void closeAddShelfFragment() {
      FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
      Fragment addShelfFragment = fragmentManager.findFragmentById(R.id.fragment_container_add_shelf);

      if (addShelfFragment != null) {
         fragmentTransaction.remove(addShelfFragment);
      }

      fragmentTransaction.commit();
   }

   private void updateLibraryListView(List libraryList) {
      libraryRecyclerView.setAdapter(new LibraryRecyclerViewAdapter(libraryList, this));
      // libraryRecyclerView.getAdapter().notifyDataSetChanged(); // TODO make notify work
      TextView emptyView = getActivity().findViewById(R.id.list_view_library_empty);

      if (libraryList.isEmpty()) {
         emptyView.setVisibility(View.VISIBLE);
      } else {
         emptyView.setVisibility(View.GONE);
      }
   }

   private void updateHeader(String name) {
      TextView headerView = getActivity().findViewById(R.id.headerText);
      headerView.setText(name);
   }

   @Override
   public void onItemClicked(int position) {
      closeAddShelfFragment();
      LibraryItem libraryItem = libraryModel.getSelectedLibraryItem(position);

      if (libraryItem instanceof ShelfItem) {
         updateHeader(libraryItem.getName());
         Long shelfId = libraryItem.getId();
         libraryList = libraryModel.getLibraryList(shelfId);
         updateLibraryListView(libraryList);

      } else if (libraryItem instanceof BookItem) {
         Toast.makeText(context, "TODO öffne Buch ", Toast.LENGTH_SHORT).show();
      }

      // same for note item

   }

}
