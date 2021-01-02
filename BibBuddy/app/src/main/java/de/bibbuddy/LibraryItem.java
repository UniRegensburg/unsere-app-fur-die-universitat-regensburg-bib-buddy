package de.bibbuddy;

public class LibraryItem {
   String mName;
   Integer mImage;
   Integer mId;
   Integer mParentId;

   public LibraryItem(String name, Integer image, Integer id)
   {
      this.mName = name;
      this.mImage = image;
      this.mId = id;
   }

   public LibraryItem(String name, Integer image, Integer id, int parentId) {
      this.mName = name;
      this.mImage = image;
      this.mId = id;
      this.mParentId = parentId;
   }
}
