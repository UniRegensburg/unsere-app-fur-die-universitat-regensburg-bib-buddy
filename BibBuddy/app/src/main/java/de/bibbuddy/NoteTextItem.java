package de.bibbuddy;

public class NoteTextItem extends NoteItem {

  public NoteTextItem(String modDate, String name, Long id) {
    super(modDate, name, R.drawable.document, id);
  }

  public NoteTextItem(String modDate, String name, Long id, Long bookId) {
    super(modDate, name, R.drawable.document, id, bookId);
  }

}
