package de.bibbuddy;

public class BookItem extends LibraryItem {
   public BookItem(String name, Long id) {
      super(name, R.drawable.ic_book, id);
   }

   public BookItem(String name, Long id, Long parentId) {
      super(name, R.drawable.ic_book, id, parentId);
   }
}
