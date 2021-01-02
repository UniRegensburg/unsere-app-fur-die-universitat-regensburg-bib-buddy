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

import java.util.ArrayList;

public class LibraryFragment extends Fragment {

   ArrayList<LibraryItem> currentLibraryList;

   ArrayList<LibraryItem> libraryList;
   ArrayList<LibraryItem> subLibraryList;

   Integer previousShelfId;

   Context context;

   ListView libraryListView;

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      View view = inflater.inflate(R.layout.fragment_library, container, false);

      libraryListView = view.findViewById(R.id.list_view_library);

      libraryList = new ArrayList<>();
      // TODO create view from database data
      // name of item; type of item; id of item from type
      libraryList.add(new LibraryItem("Regal 1", R.drawable.ic_shelf, 1));
      libraryList.add(new LibraryItem("Buch 1", R.drawable.ic_book, 1));
      libraryList.add(new LibraryItem("noch ein Regal", R.drawable.ic_shelf, 2));

      context = view.getContext();

      LibraryAdapter libraryAdapter = new LibraryAdapter(context, libraryList);
      libraryListView.setAdapter(libraryAdapter);
      createLibraryListViewListener(context, libraryListView);

      // TODO find out why text is not displayed when list is empty
      libraryListView.setEmptyView(view.findViewById(R.id.list_view_library_empty));
      //  android:visibility="gone" for fragment_library.xml

      return view;
   }


   private void createLibraryListViewListener(Context context, ListView libraryListView) {
      libraryListView.setOnItemClickListener((parent, view, position, id) -> {
         LibraryItem libraryItem = libraryList.get(position);
         Integer itemType = libraryItem.mImage;

         Toast.makeText(context, position + " geklickt", Toast.LENGTH_SHORT).show();

         if (itemType == R.drawable.ic_shelf) {
            previousShelfId = libraryItem.mId;
            // TODO updateView with selected ID type
            updateLibraryListView(previousShelfId, itemType);

         }

//         else if (itemType == R.drawable.ic_book) {
//            // TODO
//         }

      });
   }

   private void updateLibraryListView(Integer parentId, Integer itemType) {
      // TODO create only ẃhen certain element is clicked with database data
      subLibraryList = new ArrayList<>();
      subLibraryList.add(new LibraryItem("Buch 2", R.drawable.ic_book, 2, 1));
      subLibraryList.add(new LibraryItem("Buch 3", R.drawable.ic_book, 3, 1));

      subLibraryList.add(new LibraryItem("Buch 4", R.drawable.ic_book, 3, 2));


      currentLibraryList = new ArrayList<>();

      for (int i = 0; i < subLibraryList.size(); i++) {
         if (subLibraryList.get(i).mParentId.equals(parentId)) {
            currentLibraryList.add(subLibraryList.get(i));
         }
      }

      // TODO better and remove bug
      LibraryAdapter libraryAdapter = new LibraryAdapter(context, currentLibraryList);
      libraryListView.setAdapter(libraryAdapter);
      createLibraryListViewListener(context, libraryListView);
   }

}

