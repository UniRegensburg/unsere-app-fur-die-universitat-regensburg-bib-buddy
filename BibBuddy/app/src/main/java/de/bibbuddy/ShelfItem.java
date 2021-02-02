package de.bibbuddy;

public class ShelfItem extends LibraryItem {
   public ShelfItem(String name, Long id) {
      super(name, R.drawable.ic_shelf, id);

   }

   public ShelfItem(String name, Long id, Long parentId) {
      super(name, R.drawable.ic_shelf, id, parentId);
   }
}
