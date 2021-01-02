package de.bibbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ListView;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

import java.util.List;

public class LibraryFragment extends Fragment {

   private Context context;
   private LibraryModel libraryModel;
   private ListView libraryListView;

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.fragment_library, container, false);

      context = view.getContext();
      libraryListView = view.findViewById(R.id.list_view_library);

      libraryModel = new LibraryModel();
      List<LibraryItem> libraryList = libraryModel.getLibraryList(null);

      libraryListView.setAdapter(new LibraryAdapter(context, libraryList));
      createLibraryListViewListener(context, libraryListView);

      // TODO find out why text is not displayed when list is empty
      libraryListView.setEmptyView(view.findViewById(R.id.list_view_library_empty));
      //  android:visibility="gone" for fragment_library.xml

      return view;
   }


   private void createLibraryListViewListener(Context context, ListView libraryListView) {
      libraryListView.setOnItemClickListener((parent, view, position, id) -> {
         LibraryItem libraryItem = libraryModel.getSelectedLibraryItem(position);

         Toast.makeText(context, position + " geklickt", Toast.LENGTH_SHORT).show();

         if (libraryItem instanceof ShelfItem) {
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

}

