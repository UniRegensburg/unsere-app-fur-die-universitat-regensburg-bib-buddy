package de.bibbuddy;

import org.jsoup.Jsoup;

/**
 * The NoteTextItem is responsible for holding the information of the note text items.
 * It is a child of NoteItem.
 *
 * @author Sarah Kurek
 */
public class NoteTextItem extends NoteItem {

  private final String displayName;

  /**
   * Constructor to set up a new noteTextItem.
   *
   * @param note   note object
   * @param bookId id of the related book
   */
  public NoteTextItem(Note note, Long bookId) {
    super(note, R.drawable.document, bookId);


    String name = note.getName();
    name = Jsoup.parse(name).text();

    if (name.length() > 20) {
      name = name.substring(0, 20) + " ...";
    }

    this.displayName = name;
  }

  @Override
  public String getDisplayName() {
    return displayName;
  }

}
