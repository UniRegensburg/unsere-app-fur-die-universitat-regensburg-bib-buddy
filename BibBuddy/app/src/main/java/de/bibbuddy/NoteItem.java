package de.bibbuddy;

/**
 * The NoteItem is responsible for holding the information of the note items.
 * It is the parent of NoteAudioItem, NoteImageItem and NoteTextItem.
 *
 * @author Sarah Kurek
 */
public class NoteItem implements SortableItem {

  private final Long modDate;
  private final String name;
  private final String text;
  private final int image;
  private final Long id;
  private final Long bookId;

  private String modDateStr;

  /**
   * Constructor to set up a NoteItem for view/UI usages.
   *
   * @param modDate string-value of the modification date of the note
   * @param name    name of the note
   * @param image   id for the drawable resource of the note type icon
   * @param id      id of the note
   * @param bookId  id of the book that the note is connected to
   */
  public NoteItem(Long modDate, String name, String text, int image, Long id, Long bookId) {
    this.modDate = modDate;
    this.modDateStr = DateConverter.convertDateToString(modDate);

    this.name = name;
    this.text = text;
    this.image = image;
    this.id = id;
    this.bookId = bookId;
  }

  /**
   * Constructor to set up a NoteItem for view/UI usages.
   *
   * @param modDate string-value of the modification date of the note
   * @param name    name of the note
   * @param image   id for the drawable resource of the note type icon
   * @param id      id of the note
   */
  public NoteItem(Long modDate, String name, int image, Long id) {
    this(modDate, name, null, image, id, null);

    this.modDateStr = DateConverter.convertDateToString(modDate);
  }

  @Override
  public Long getModDate() {
    return modDate;
  }

  @Override
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

  public String getNoteText() {
    return text;
  }

  public String getModDateStr() {
    return modDateStr;
  }

}
