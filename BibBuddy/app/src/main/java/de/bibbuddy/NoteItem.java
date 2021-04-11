package de.bibbuddy;

/**
 * The NoteItem is responsible for holding the information of the note items.
 * It is the parent of NoteAudioItem and NoteTextItem.
 *
 * @author Sarah Kurek
 */
public abstract class NoteItem implements SortableItem {

  private final Note note;
  private final int image;
  private final Long bookId;

  /**
   * Constructor to set up a NoteItem for view/UI usages.
   *
   * @param note    the note
   * @param image   id for the drawable resource of the note type icon
   * @param bookId  id of the book that the note is connected to
   */
  public NoteItem(Note note, int image, Long bookId) {
    this.note = note;
    this.image = image;
    this.bookId = bookId;
  }

  @Override
  public Long getModDate() {
    return note.getModDate();
  }

  @Override
  public String getName() {
    return note.getName();
  }

  public int getImage() {
    return image;
  }

  public Long getId() {
    return note.getId();
  }

  public NoteTypeLut getType() {
    return note.getType();
  }

  public Long getBookId() {
    return bookId;
  }

  public String getModDateStr() {
    return DateConverter.convertDateToString(getModDate());
  }

  public abstract String getDisplayName();

}
