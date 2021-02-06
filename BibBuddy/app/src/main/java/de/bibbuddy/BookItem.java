package de.bibbuddy;

public class BookItem extends LibraryItem {
   private int year;
   private String authors;

   public BookItem(String title, Long id) {
      super(title, R.drawable.ic_book, id);
   }

   public BookItem(String title, Long id, Long shelfId, int year, String authors) {
      super(title, R.drawable.ic_book, id, shelfId);
      this.year = year;
      this.authors = authors;
   }

   public int getYear() {
      return year;
   }

   public String getAuthors() {
      return authors;
   }
}
