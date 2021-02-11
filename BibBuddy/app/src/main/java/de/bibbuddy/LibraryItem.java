package de.bibbuddy;

public class LibraryItem {

  private final String name;
  private final int image;
  private final Long id;
  private final Long parentId;

  public LibraryItem(String name, int image, Long id) {
    this(name, image, id, null);
  }

  public LibraryItem(String name, int image, Long id, Long parentId) {
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

  public Long getId() {
    return id;
  }

  public Long getParentId() {
    return parentId;
  }

}
