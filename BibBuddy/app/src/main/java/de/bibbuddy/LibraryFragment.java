package de.bibbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;

import com.google.android.material.floatingactionbutton.FloatingActionButton;

import java.util.List;

public class LibraryFragment extends Fragment
      implements LibraryRecyclerViewAdapter.LibraryListener {

   private Context context;

   private LibraryModel libraryModel;
   private RecyclerView libraryRecyclerView;
   private List<LibraryItem> libraryList;

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

      FloatingActionButton addShelfBtn = view.findViewById(R.id.btn_add_shelf);
      createAddShelfListener(addShelfBtn);

      createBackBtnListener(view);

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


   private void createAddShelfListener(FloatingActionButton addShelfBtn) {

      addShelfBtn.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {

            LibraryAddShelfFragment fragment = new LibraryAddShelfFragment(new LibraryAddShelfFragment.AddShelfLibraryListener() {
               @Override
               public void onShelfAdded(String name, Long shelfId) {
                  libraryModel.addShelf(name, libraryModel.getShelfId());
                  updateLibraryListView(libraryModel.getCurrentLibraryList());
               }
            });

            fragment.setArguments(createAddShelfBundle());
            fragment.show(getActivity().getSupportFragmentManager(), LibraryKeys.DIALOG_FRAGMENT_ADD_NAME);
         }
      });
   }

   private void createBackBtnListener(View view) {
      TextView backView = view.findViewById(R.id.text_view_back_to);

      backView.setOnClickListener(v -> {
         Long shelfId = libraryModel.getShelfId(); // get parentId of the current shelf
         updateLibraryListView(libraryModel.getPreviousLibraryList(shelfId));
         updateHeader(libraryModel.getShelfName());
         updateBackBtn(backView);
         closeAddShelfFragment();
      });
   }

   private void updateBackBtn(TextView backView) {
      Long shelfId = libraryModel.getCurrentShelfId();

      if (shelfId == null) {
         backView.setVisibility(View.GONE);
      } else {
         backView.setVisibility(View.VISIBLE);
         String backText = getString(R.string.back_to) + " \"" + libraryModel.getPreviousShelfName() + "\"";
         backView.setText(backText);
      }
   }

   private void closeAddShelfFragment() {
      LibraryAddShelfFragment fragment = (LibraryAddShelfFragment) getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_container_add_shelf);
      if (fragment != null) {
         fragment.closeFragment();
      }
   }

   private void updateLibraryListView(List libraryList) {
      libraryRecyclerView.setAdapter(new LibraryRecyclerViewAdapter(libraryList, this));
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
         updateBackBtn(getView().findViewById(R.id.text_view_back_to));

      } else if (libraryItem instanceof BookItem) {
         Toast.makeText(context, "TODO öffne Buch ", Toast.LENGTH_SHORT).show();
      }

      // same for note item

   }

}
