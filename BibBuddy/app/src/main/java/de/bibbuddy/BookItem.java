package de.bibbuddy;

public class BookItem extends LibraryItem {
   public BookItem(String name, Integer id) {
      super(name, R.drawable.ic_book, id);
   }

   public BookItem(String name, Integer id, Integer parentId) {
      super(name, R.drawable.ic_book, id, parentId);
   }
}
