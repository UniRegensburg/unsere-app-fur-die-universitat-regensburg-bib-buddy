package de.bibbuddy;

/**
 * Look up table for the note type.
 *
 * @author Sarah Kurek
 */
public enum NoteTypeLut {

  TEXT(1), IMAGE(2), AUDIO(3), OTHER(4);

  private final int id;

  NoteTypeLut(int id) {
    this.id = id;
  }

  public int getId() {
    return id;
  }
}
