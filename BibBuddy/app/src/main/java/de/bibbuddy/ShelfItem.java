package de.bibbuddy;

public class ShelfItem extends LibraryItem {
   public ShelfItem(String name, Integer id) {
      super(name, R.drawable.ic_shelf, id);

   }

   public ShelfItem(String name, Integer id, Integer parentId) {
      super(name, R.drawable.ic_shelf, id, parentId);
   }
}
