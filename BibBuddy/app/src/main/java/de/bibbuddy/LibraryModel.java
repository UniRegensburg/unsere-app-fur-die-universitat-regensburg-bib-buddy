package de.bibbuddy;

import java.util.ArrayList;
import java.util.List;

public class LibraryModel {

   private List<LibraryItem> libraryList;

   private Integer previousShelfId;

   private List<LibraryItem> getTopLevelLibraryList() {
      libraryList = new ArrayList<>();

      previousShelfId = null;

      // TODO get from db first level where parent_id is null
      libraryList.add(new ShelfItem("Regal 1", 1));
      libraryList.add(new ShelfItem("noch ein Regal", 2));
      libraryList.add(new ShelfItem("... Regal", 3));

      return libraryList;
   }

   public String getShelfName(Integer id) {
      // TODO DB get DB shelf name
      if (id == null) {
         return "Bibliothek";
      }

      switch (id) {
         case 4:
            return "... Regal";
         default:
            return "Bibliothek";
      }
   }

   public List<LibraryItem> getLibraryList(Integer parentId) {
      if (parentId == null) {
         libraryList = getTopLevelLibraryList();
         return libraryList;
      }

      libraryList = new ArrayList<>();

      // TODO get from db parent_id level
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

   public Integer getParentShelfId() {
      return previousShelfId;
   }

   public List<LibraryItem> getPreviousLibraryList(Integer id) {
      // TODO DB
      libraryList = new ArrayList<>();

      switch (id) {
         case 1:
         case 2:
         case 3:
            previousShelfId = null;
            libraryList.add(new ShelfItem("Regal 1", 1));
            libraryList.add(new ShelfItem("noch ein Regal", 2));
            libraryList.add(new ShelfItem("... Regal", 3));
            break;
         case 4:
            previousShelfId = 3;
            libraryList.add(new ShelfItem("Regal", 4, 3));
            break;
      }

      return libraryList;
   }

}
