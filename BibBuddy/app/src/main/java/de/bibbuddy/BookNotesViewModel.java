package de.bibbuddy;

import android.content.Context;
import java.util.List;

/**
 * BookNotesViewModel manages all data of the BookNotesFragment.
 *
 * @author Sarah Kurek, Silvia Ivanova
 */
public class BookNotesViewModel {

  private static NoteModel noteModel;
  private final Context context;
  private final BookDao bookDao;
  private final NoteDao noteDao;
  private List<NoteItem> noteList;

  /**
   * BookNotesViewModel contains methods for managing the
   * data for the BookNotesFragment.
   *
   * @param context context for the BookNotesFragment
   */
  public BookNotesViewModel(Context context) {
    this.context = context;
    noteModel = new NoteModel(context);

    DatabaseHelper databaseHelper = new DatabaseHelper(context);

    this.bookDao = new BookDao(databaseHelper);
    this.noteDao = new NoteDao(databaseHelper);
  }

  public static byte[] getNoteMedia(Long noteId) {
    Note note = noteModel.getNoteById(noteId);
    return noteModel.getNoteMedia(note.getNoteFileId());
  }

  public NoteModel getNoteModel() {
    return noteModel;
  }

  public BookDao getBookDao() {
    return bookDao;
  }

  public NoteDao getNoteDao() {
    return noteDao;
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

  public List<NoteItem> getSortedNoteList(SortCriteria sortCriteria, Long bookId) {
    return noteModel.getSortedNoteList(sortCriteria, bookId);
  }

}

