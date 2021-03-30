package de.bibbuddy;

/**
 * The NoteTextItem is responsible for holding the information of the note text items.
 * It is a child of NoteItem.
 *
 * @author Sarah Kurek
 */
public class NoteTextItem extends NoteItem {

  public NoteTextItem(Long modDate, String name, Long id, Long bookId) {
    super(modDate, name, R.drawable.document, id, bookId);
  }

}
