package de.bibbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;

import java.util.List;

public class LibraryFragment extends Fragment {

   private Context context;
   private LibraryModel libraryModel;
   private ListView libraryListView;

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
      libraryListView = view.findViewById(R.id.list_view_library);

      libraryModel = new LibraryModel();
      List<LibraryItem> libraryList = libraryModel.getLibraryList(null);

      libraryListView.setAdapter(new LibraryAdapter(context, libraryList));
      createLibraryListViewListener(context, libraryListView);

      libraryListView.setEmptyView(view.findViewById(R.id.list_view_library_empty));

      return view;
   }

   private void createBackBtnListener() {
      Toolbar toolbar = getActivity().findViewById(R.id.toolbar);

      toolbar.setNavigationOnClickListener(v -> {
         Integer shelfId = libraryModel.getParentShelfId();
         updateHeader(libraryModel.getShelfName(shelfId));

         if (shelfId == null) {
            Toast.makeText(context, "bereits die oberste Ebene", Toast.LENGTH_SHORT).show();
            return;
         }

         updateLibraryListView(libraryModel.getPreviousLibraryList(shelfId));
      });
   }


   private void createLibraryListViewListener(Context context, ListView libraryListView) {
      libraryListView.setOnItemClickListener((parent, view, position, id) -> {
         LibraryItem libraryItem = libraryModel.getSelectedLibraryItem(position);

         if (libraryItem instanceof ShelfItem) {
            updateHeader(libraryItem.getName());

            Integer previousShelfId = libraryItem.getId();
            libraryModel.setPreviousShelfId(previousShelfId);
            updateLibraryListView(previousShelfId);

         } else if (libraryItem instanceof BookItem) {
            Toast.makeText(context, "TODO öffne Buch ", Toast.LENGTH_SHORT).show();
         }

      });
   }

   private void updateLibraryListView(Integer parentId) {
      List<LibraryItem> currentLibraryList = libraryModel.getLibraryList(parentId);

      libraryListView.setAdapter(new LibraryAdapter(context, currentLibraryList));
      createLibraryListViewListener(context, libraryListView);
   }

   private void updateLibraryListView(List libraryList) {
      libraryListView.setAdapter(new LibraryAdapter(context, libraryList));
      createLibraryListViewListener(context, libraryListView);
   }

   private void updateHeader(String name) {
      TextView headerText = getActivity().findViewById(R.id.headerText);
      headerText.setText(name);
   }
}
