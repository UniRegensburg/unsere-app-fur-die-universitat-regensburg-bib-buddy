package de.bibbuddy;

import android.content.Context;
import java.util.List;

/**
 * BookNotesModel manages all data of the BookNotesFragment.
 *
 * @author Sarah Kurek, Silvia Ivanova
 */
public class BookNotesModel {

  private final NoteModel noteModel;

  /**
   * BookNotesModel contains methods for managing the
   * data for the BookNotesFragment.
   *
   * @param context context for the BookNotesFragment
   */
  public BookNotesModel(Context context) {
    this.noteModel = new NoteModel(context);
  }

  public NoteModel getNoteModel() {
    return noteModel;
  }

  public List<NoteItem> getBookNoteList(Long bookId) {
    return noteModel.getNoteListForBook(bookId);
  }

  /**
   * Deletes all selected notes.
   *
   * @param selectedNoteItems selected note items
   */
  public void deleteNotes(List<NoteItem> selectedNoteItems) {
    if (selectedNoteItems == null) {
      return;
    }

    for (NoteItem note : selectedNoteItems) {
      noteModel.deleteNote(note.getId());
    }
  }

  public List<NoteItem> getSortedNoteList(SortCriteria sortCriteria, Long bookId) {
    return noteModel.getSortedNoteList(sortCriteria, bookId);
  }
}

