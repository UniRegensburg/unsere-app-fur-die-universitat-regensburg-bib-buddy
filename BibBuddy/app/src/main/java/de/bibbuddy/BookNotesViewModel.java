package de.bibbuddy;

import android.content.Context;
import java.util.List;

/**
 * BookNotesViewModel manages all data of the BookNotesView.
 *
 * @author Sarah Kurek
 */
public class BookNotesViewModel {


  private final Context context;
  private final NoteModel noteModel;
  private List<NoteItem> noteList;


  public BookNotesViewModel(Context context) {
    this.context = context;
    this.noteModel = new NoteModel(context);
  }

  public NoteModel getNoteModel() {
    return noteModel;
  }


  public List<NoteItem> getNoteList(Long bookId) {
    noteList = noteModel.getNoteListForBook(bookId);
    return noteList;
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
      Long noteId = note.getId();

      noteModel.deleteNote(noteId);
      deleteNoteFromNoteList(note);
    }
  }

  private void deleteNoteFromNoteList(NoteItem note) {
    for (int i = 0; i < noteList.size(); i++) {
      if (note.equals(noteList.get(i))) {
        noteList.remove(i);
      }
    }
  }

  public List<NoteItem> getCurrentNoteList() {
    return noteList;
  }

  public NoteItem getSelectedNoteItem(int position) {
    return noteList.get(position);
  }

}

