package de.bibbuddy;

import android.content.Context;

import java.util.ArrayList;
import java.util.List;

public class LibraryModel {

   private final Context context;
   private final ShelfDAO shelfDAO;

   private List<LibraryItem> libraryList;
   private Long currentShelfId;

   public LibraryModel(Context context) {
      this.context = context;
      DatabaseHelper databaseHelper = new DatabaseHelper(context);
      this.shelfDAO = new ShelfDAO(databaseHelper);
   }

   public void addShelf(String name, Long parentId) {
      Shelf shelf = new Shelf(name, parentId);
      shelfDAO.create(shelf);

      Long id = shelfDAO.findLatestId();
      libraryList.add(new ShelfItem(name, id, parentId));
   }

   public Long getCurrentShelfId() {
      return currentShelfId;
   }

   public List<LibraryItem> getCurrentLibraryList() {
      return libraryList;
   }

   public List<LibraryItem> getLibraryList(Long parentId) {
      currentShelfId = parentId;

      List<Shelf> list = shelfDAO.findAllByParentId(parentId);

      libraryList = new ArrayList<>();
      for (Shelf item : list) {
         libraryList.add(new ShelfItem(item.getName(), item.getId(), item.getShelfId()));
      }

      return libraryList;
   }

   public List<LibraryItem> getPreviousLibraryList(Long id) {
      if (id == null) {
         return libraryList;
      }

      Long parentId = shelfDAO.findById(id).getShelfId();
      return getLibraryList(parentId);
   }

   public LibraryItem getSelectedLibraryItem(int position) {
      return libraryList.get(position);
   }

   public Long getShelfId() {
      return currentShelfId;
   }

   public String getShelfName() {
      return getShelfName(currentShelfId);
   }

   private String getShelfName(Long id) {
      if (id == null) {
         return context.getString(R.string.navigation_library);
      }

      return shelfDAO.findById(id).getName();
   }

   public String getPreviousShelfName() {
      Long parentId = shelfDAO.findById(currentShelfId).getShelfId();

      if (parentId == null) {
         return context.getString(R.string.navigation_library);
      }

      return shelfDAO.findById(parentId).getName();
   }
}
