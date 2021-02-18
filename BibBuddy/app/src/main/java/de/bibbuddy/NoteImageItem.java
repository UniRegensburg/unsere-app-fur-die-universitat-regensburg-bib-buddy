package de.bibbuddy;

/**
 * The NoteImageItem is responsible for holding the information of the note image items.
 * It is a child of NoteItem.
 *
 * @author Sarah Kurek
 */
public class NoteImageItem extends NoteItem {
  public NoteImageItem(String modDate, String name, Long id) {
    super(modDate, name, R.drawable.picture, id);
  }

  public NoteImageItem(String modDate, String name, Long id, Long bookId) {
    super(modDate, name, R.drawable.picture, id, bookId);
  }
}
