package de.bibbuddy;

import java.util.ArrayList;
import java.util.List;

public class LibraryModel {

   private List<LibraryItem> libraryList;

   private Integer previousShelfId;

   private List<LibraryItem> getTopLevelLibraryList() {
      libraryList = new ArrayList<>();

      libraryList.add(new ShelfItem("Regal 1", 1));
      libraryList.add(new BookItem("Buch 1", 1));
      libraryList.add(new ShelfItem("noch ein Regal", 2));
      libraryList.add(new ShelfItem("... Regal", 3));

      return libraryList;
   }

   public List<LibraryItem> getLibraryList(Integer parentId) {
      if (parentId == null) {
         libraryList = getTopLevelLibraryList();
         return libraryList;
      }

      libraryList = new ArrayList<>();

      switch (parentId) {
         case 1:
            libraryList.add(new BookItem("Buch 2", 2, 1));
            libraryList.add(new BookItem("Buch 3", 3, 1));
            break;

         case 2:
            libraryList.add(new BookItem("Buch 4", 3, 2));
            break;

         case 3:
            libraryList.add(new ShelfItem("Regal", 4, 3));
            break;

      }

      return libraryList;
   }

   public void setPreviousShelfId(Integer id) {
      previousShelfId = id;
   }

   public LibraryItem getSelectedLibraryItem(int position) {
      return libraryList.get(position);
   }
}
