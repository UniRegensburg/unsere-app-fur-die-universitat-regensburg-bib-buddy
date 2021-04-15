package de.bibbuddy;

import java.util.stream.Stream;

/**
 * Looks up table for the note type.
 *
 * @author Sarah Kurek
 */
public enum NoteTypeLut {

  TEXT(0), AUDIO(1), IMAGE(2), OTHER(3);

  private final int id;

  NoteTypeLut(int id) {
    this.id = id;
  }

  static NoteTypeLut valueOf(int id) {
    return Stream.of(NoteTypeLut.values())
        .filter(e -> e.getId() == id)
        .findFirst()
        .orElseThrow(IllegalArgumentException::new);
  }

  public int getId() {
    return id;
  }

}
