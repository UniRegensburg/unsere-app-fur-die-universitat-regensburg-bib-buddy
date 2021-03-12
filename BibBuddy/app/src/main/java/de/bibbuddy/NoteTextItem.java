package de.bibbuddy;

/**
 * The NoteTextItem is responsible for holding the information of the note text items.
 * It is a child of NoteItem.
 *
 * @author Sarah Kurek
 */
public class NoteTextItem extends NoteItem {

  private String text;

  public NoteTextItem(Long modDate, String name, String text, Long id) {
    super(modDate, name, R.drawable.document, id);
    this.text = text;
  }

  public NoteTextItem(Long modDate, String name, String text, Long id, Long bookId) {
    super(modDate, name, text, R.drawable.document, id, bookId);
  }

}
