package de.bibbuddy;

public class NoteItem {

  private final String modDate;
  private final String name;
  private final int image;
  private final Long id;
  private final Long bookId;

  /**
   * Constructor to set up a NoteItem for view/UI usages.
   *
   * @param modDate string-value of the modification date of the note
   * @param name    name of the note
   * @param image   id for the drawable resource of the note type icon
   * @param id      id of the note
   * @param bookId  id of the book that the note is connected to
   */
  public NoteItem(String modDate, String name, int image, Long id, Long bookId) {
    this.modDate = modDate;
    this.name = name;
    this.image = image;
    this.id = id;
    this.bookId = bookId;
  }

  public NoteItem(String modDate, String name, int image, Long id) {
    this(modDate, name, image, id, null);
  }

  public String getModDate() {
    return modDate;
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

  public Long getBookId() {
    return bookId;
  }

}
