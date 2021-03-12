package de.bibbuddy;

/**
 * The NoteAudioItem is responsible for holding the information of the note audio items.
 * It is a child of NoteItem.
 *
 * @author Sarah Kurek
 */
public class NoteAudioItem extends NoteItem {
  public NoteAudioItem(Long modDate, String name, Long id) {
    super(modDate, name, R.drawable.microphone, id);
  }

  public NoteAudioItem(Long modDate, String name, Long id, Long bookId) {
    super(modDate, name, null, R.drawable.microphone, id, bookId);
  }

}
