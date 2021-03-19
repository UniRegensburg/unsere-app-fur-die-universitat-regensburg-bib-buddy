package de.bibbuddy;

import android.content.Context;
import java.util.List;

/**
 * BookNotesViewModel manages all data of the BookNotesView.
 *
 * @author Sarah Kurek, Silvia Ivanova
 */
public class BookNotesViewModel {

  private final Context context;
  private final NoteModel noteModel;
  private final BookDao bookDao;
  private final NoteDao noteDao;

  private List<NoteItem> noteList;

  /**
   * BookNotesViewModel contains methods for managing the
   * data for the BookNotesView.
   *
   * @param context context for the BookNotesView
   */
  public BookNotesViewModel(Context context) {
    this.context = context;
    this.noteModel = new NoteModel(context);

    DatabaseHelper databaseHelper = new DatabaseHelper(context);

    this.bookDao = new BookDao(databaseHelper);
    this.noteDao = new NoteDao(databaseHelper);
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
    }
  }

  public List<NoteItem> getCurrentNoteList() {
    noteList = noteModel.getNoteList();
    return noteList;
  }

  public NoteItem getSelectedNoteItem(int position) {
    return noteList.get(position);
  }

  public List<NoteItem> getSortedNoteList(SortCriteria sortCriteria, Long bookId) {
    return noteModel.getSortedNoteList(sortCriteria, bookId);
  }
}

