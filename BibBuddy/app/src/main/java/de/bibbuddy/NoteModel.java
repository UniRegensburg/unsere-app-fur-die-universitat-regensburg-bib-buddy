package de.bibbuddy;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

/**
 * NoteModel manages all data of the NoteView.
 *
 * @author Sarah Kurek, Sabrina Freisleben, Claudia Schönherr
 */
public class NoteModel {

  private final NoteDao noteDao;
  private final BookDao bookDao;

  private List<NoteItem> createItemList(List<Note> noteList) {
    List<NoteItem> noteItemList = new ArrayList<>();

    for (Note note : noteList) {
      if (note.getType() == NoteTypeLut.TEXT) {
        noteItemList
            .add(new NoteTextItem(note, noteDao.findBookIdByNoteId(note.getId())));
      } else if (note.getType() == NoteTypeLut.AUDIO) {
        noteItemList
            .add(new NoteAudioItem(note, noteDao.findBookIdByNoteId(note.getId())));
      }
    }

    return noteItemList;
  }

  /**
   * Constructor for a NoteModel.
   *
   * @param context context for the BookModel
   */
  public NoteModel(Context context) {
    DatabaseHelper databaseHelper = new DatabaseHelper(context);
    this.noteDao = new NoteDao(databaseHelper);
    this.bookDao = new BookDao(databaseHelper);
  }

  /**
   * Gets the Ids of all notes for a given book.
   *
   * @param id id of a book
   * @return a list with the Ids of all text notes
   */
  public List<Long> getTextNoteIdsForBook(Long id) {
    return noteDao.getTextNoteIdsForBook(id);
  }

  /**
   * Gets text string of a specific note without formatting xml tags.
   *
   * @param id id of the note to look for
   * @return returns the notes text value without formatting texts
   */
  public String findStrippedTextById(Long id) {
    return noteDao.findTextById(id).replaceAll(
        "(<p dir=\"ltr\"( style=\"margin-top:0; margin-bottom:0;\")?>|</p>|"
            + "<div align=\"right\" {2}<div align=\"center\" {2}> >|</div>|"
            + "<span style=\"text-decoration:line-through;\">|</span>|<(/)?i>|"
            + "<(/)?b>|<(/)?u>|<(/)?br>|<(/)?blockquote>)",
        "");
  }

  /**
   * Creates a note object and pass it to the noteDao to add it to the database as well.
   *
   * @param name         of the note object
   * @param type         of the note object
   * @param text         of the note object
   * @param noteFilePath string value representing the path to a linked noteFile-object
   */
  public void createNote(String name, NoteTypeLut type, String text, String noteFilePath) {
    Note note;
    if (noteFilePath.equals("")) {
      note = new Note(name, type, text);
    } else {
      note = new Note(name, type, text, noteFilePath);
    }

    noteDao.create(note);
  }

  public void updateNote(Note note, String name, String text) {
    noteDao.updateNote(note.getId(), name, text);
  }

  public void deleteNote(Long id) {
    noteDao.delete(id);
  }

  public Note getNoteById(Long id) {
    return noteDao.findById(id);
  }

  public Note getLastNote() {
    return noteDao.findAll().get(noteDao.findAll().size() - 1);
  }

  /**
   * Fetches the entire noteItem-list from the database.
   *
   * @return a list of all NoteItems from the database
   */
  public List<NoteItem> getNoteList() {
    return createItemList(noteDao.findAll());
  }

  /**
   * Fetches all notes, that are connected to a specific book, from the database.
   *
   * @param bookId whose connected notes are searched for
   * @return a list of NoteItems whose connected notes are connected to the specific book
   */
  public List<NoteItem> getNoteListForBook(Long bookId) {
    List<Note> noteList = noteDao.getAllNotesForBook(bookId);
    return createItemList(noteList);
  }

  /**
   * Gets the list of all existing voice notes (noteType=1) from the database.
   *
   * @return a list of NoteItems created from all voice type note objects from the database
   */
  public List<Note> getVoiceNoteList() {
    List<Note> noteList = noteDao.findAll();

    return noteList.stream()
        .filter(note -> note.getType() == NoteTypeLut.AUDIO)
        .collect(Collectors.toList());
  }

  public String getNoteFilePath(Long id) {
    return noteDao.getNoteFilePath(getNoteById(id).getNoteFileId());
  }

  public void linkNoteWithBook(Long bookId, Long noteId) {
    noteDao.linkNoteWithBook(bookId, noteId);
  }

  /**
   * Gets a list of all noteItems from the database sorted by given sortTypeLut.
   *
   * @param sortTypeLut chosen by the user to sort the list
   * @param noteList    to be sorted
   * @return the sorted noteList
   */
  public List<NoteItem> sortNoteList(SortTypeLut sortTypeLut, List<NoteItem> noteList) {
    switch (sortTypeLut) {

      case MOD_DATE_LATEST:
        noteList.sort(new SortDate());
        break;

      case MOD_DATE_OLDEST:
        noteList.sort(new SortDate().reversed());
        break;

      case NAME_ASCENDING:
        noteList.sort(new SortName());
        break;

      case NAME_DESCENDING:
        noteList.sort(new SortName().reversed());
        break;

      default:
        throw new IllegalArgumentException();
    }

    return noteList;
  }

  /**
   * Gets the noteList of a book sorted by sortTypeLut.
   *
   * @param sortTypeLut currently applied to the list
   * @param bookId      id of the book the noteList is linked to
   * @return the sorted noteList
   */
  public List<NoteItem> getSortedNoteList(SortTypeLut sortTypeLut, Long bookId) {
    List<Note> noteListDb = noteDao.getAllNotesForBook(bookId);
    List<NoteItem> noteList = createItemList(noteListDb);

    return sortNoteList(sortTypeLut, noteList);
  }

  /**
   * Gets the noteList for all notes in the database sorted by sortTypeLut.
   *
   * @param sortTypeLut currently applied to the list
   * @return the sorted noteList
   */
  public List<NoteItem> getAllSortedNoteList(SortTypeLut sortTypeLut) {
    List<Note> allNoteList = noteDao.findAll();
    List<NoteItem> noteList = createItemList(allNoteList);

    return sortNoteList(sortTypeLut, noteList);
  }

  public String getBookNameByBookId(Long bookId) {
    return bookDao.findBookTitleByBookId(bookId);
  }
}
