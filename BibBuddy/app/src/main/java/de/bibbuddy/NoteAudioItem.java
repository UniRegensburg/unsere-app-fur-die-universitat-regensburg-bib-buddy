package de.bibbuddy;

/**
 * The NoteAudioItem is responsible for holding the information of the note audio items.
 * It is a child of NoteItem.
 *
 * @author Sarah Kurek
 */
public class NoteAudioItem extends NoteItem {

  public NoteAudioItem(Note note, Long bookId) {
    super(note, R.drawable.microphone, bookId);
  }

  @Override
  public String getDisplayName() {
    return "";
  }

}
