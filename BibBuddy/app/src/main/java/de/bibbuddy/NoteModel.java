package de.bibbuddy;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;
import org.jsoup.Jsoup;

/**
 * NoteModel manages all data of the NoteView.
 *
 * @author Sarah Kurek, Sabrina Freisleben.
 */
public class NoteModel {

  private final NoteDao noteDao;

  public NoteModel(Context context) {
    DatabaseHelper databaseHelper = new DatabaseHelper(context);
    this.noteDao = new NoteDao(databaseHelper);
  }

  /**
   * Create a note object and pass it to the noteDao to add it to the database as well.
   *
   * @param name         of the note object.
   * @param type         of the note object.
   * @param text         of the note object.
   * @param noteFilePath string-value representing the path to a linked noteFile-object.
   */
  public void createNote(String name, int type, String text, String noteFilePath) {
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
   * Fetch the entire noteItem-list from the database.
   *
   * @return a list of all NoteItems from the database.
   */
  public List<NoteItem> getNoteList() {
    List<Note> noteList = noteDao.findAll();
    return createItemList(noteList);
  }

  /**
   * Fetch all notes, that are connected to a specific book, from the database.
   *
   * @param bookId whose connected notes are searched for.
   * @return a list of NoteItems whose connected notes are connected to the specific book.
   */
  public List<NoteItem> getNoteListForBook(Long bookId) {
    List<Note> noteList = noteDao.getAllNotesForBook(bookId);
    return createItemList(noteList);
  }

  /**
   * Get the list of all existing voice notes (noteType=1) from the database.
   *
   * @return a list of NoteItems created from all voice type note objects from the database.
   */
  public List<Note> getVoiceNoteList() {
    List<Note> noteList = noteDao.findAll();

    for (int i = 0; i < noteList.size(); i++) {
      if (noteList.get(i).getType() != 1) {
        noteList.remove(noteList.get(i));
      }
    }

    return noteList;
  }

  public String getNoteFilePath(Long id) {
    return noteDao.getNoteFilePath(getNoteById(id).getNoteFileId());
  }

  private List<NoteItem> createItemList(List<Note> noteList) {
    List<NoteItem> noteItemList = new ArrayList<>();

    for (Note note : noteList) {
      Long noteId = note.getId();
      Long modDate = note.getModDate();
      String name = "";

      if (note.getType() == 0) {
        name = note.getName();
        name = Jsoup.parse(name).text();

        if (name.length() > 20) {
          name = name.substring(0, 20) + " ...";
        }
        noteItemList
            .add(new NoteTextItem(modDate, name, noteId, noteDao.findBookIdByNoteId(noteId)));
      } else if (note.getType() == 1) {
        noteItemList
            .add(new NoteAudioItem(modDate, name, noteId, noteDao.findBookIdByNoteId(noteId)));
      }
    }

    return noteItemList;
  }

  public void linkNoteWithBook(Long bookId, Long noteId) {
    noteDao.linkNoteWithBook(bookId, noteId);
  }

  /**
   * Get a list of all noteItems from the database sorted by given sortCriteria.
   *
   * @param sortCriteria chosen by the user to sort the list.
   * @param noteList     to be sorted.
   * @return the sorted noteList.
   */
  public List<NoteItem> sortNoteList(SortCriteria sortCriteria, List<NoteItem> noteList) {
    switch (sortCriteria) {

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
        break;
    }

    return noteList;
  }

  /**
   * Get the noteList of a book sorted by sortCriteria.
   *
   * @param sortCriteria currently applied to the list.
   * @param bookId       id of the book the noteList is linked to.
   * @return the sorted noteList.
   */
  public List<NoteItem> getSortedNoteList(SortCriteria sortCriteria, Long bookId) {
    List<Note> noteListDb = noteDao.getAllNotesForBook(bookId);
    List<NoteItem> noteList = createItemList(noteListDb);

    return sortNoteList(sortCriteria, noteList);
  }

  /**
   * Get the noteList for all notes in the database sorted by sortCriteria.
   *
   * @param sortCriteria currently applied to the list.
   * @return the sorted noteList.
   */
  public List<NoteItem> getAllSortedNoteList(SortCriteria sortCriteria) {
    List<Note> allNoteList = noteDao.findAll();
    List<NoteItem> noteList = createItemList(allNoteList);

    return sortNoteList(sortCriteria, noteList);
  }
}
