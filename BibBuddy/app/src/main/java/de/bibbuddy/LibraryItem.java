package de.bibbuddy;

public class LibraryItem {
   private final String name;
   private final int image;
   private final Integer id;
   private final Integer parentId;

   public LibraryItem(String name, Integer image, Integer id) {
      this(name, image, id, null);
   }

   public LibraryItem(String name, int image, Integer id, Integer parentId) {
      this.name = name;
      this.image = image;
      this.id = id;
      this.parentId = parentId;
   }

   public String getName() {
      return name;
   }

   public int getImage() {
      return image;
   }

   public Integer getId() {
      return id;
   }

   public Integer getParentId() {
      return parentId;
   }
}
