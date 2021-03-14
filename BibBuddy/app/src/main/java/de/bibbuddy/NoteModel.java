package de.bibbuddy;

import android.content.Context;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import org.jsoup.Jsoup;

/**
 * NoteModel manages all data of the NoteView.
 *
 * @author Sarah Kurek, Sabrina Freisleben
 */
public class NoteModel {

  private final NoteDao noteDao;

  public NoteModel(Context context) {
    DatabaseHelper databaseHelper = new DatabaseHelper(context);
    this.noteDao = new NoteDao(databaseHelper);
  }

  /**
   * This method creates a new note object and passes it to the noteDao to add it to the database
   * as well.
   *
   * @param name  name of the note object
   * @param type  type of the note object
   * @param text  text of the note object
   * @param bytes byteArray that is representing the saved object binary
   */
  public void addNote(String name, int type, String text, byte[] bytes) {
    Note note;
    if (bytes != null) {
      note = new Note(name, type, text, bytes);
    } else {
      note = new Note(name, type, text);
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
   * This method is used to fetch the entire noteList from the database.
   *
   * @return a list of NoteItems (used for views)
   */
  public List<NoteItem> getCompleteNoteList() {
    List<Note> noteList = noteDao.findAll();
    return createItemList(noteList);
  }

  /**
   * This method gets the list of all existing notes of the voice-type (int=1).
   *
   * @return returns the list of voice type note objects.
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

  /**
   * This method creates a string representing the given date in a common readable format.
   *
   * @param date given date of type long representing millis since the start date in 1970.
   * @return returns a string in "dd.mm.yyyy hh:mm:ss"-format.
   */
  public String getDate(Long date) {
    Date d = new Date(date);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        Locale.getDefault());

    String string = simpleDateFormat.format(d);
    String day = string.substring(8, 10);
    String month = string.substring(5, 7);
    String year = string.substring(0, 4);
    String time = string.substring(11, 16);
    string = day + "." + month + "." + year + " " + time + " Uhr";

    return string;
  }

  /**
   * Method to fetch all notes, that are connected to a specific book, from the database.
   *
   * @param bookId id of the book, whose connected notes are searched for
   * @return returns a list of NoteItems that are connected to the specific book
   */
  public List<NoteItem> getNoteListForBook(Long bookId) {
    List<Note> noteList = noteDao.getAllNotesForBook(bookId);
    return createItemList(noteList);
  }

  private List<NoteItem> createItemList(List<Note> noteList) {
    List<NoteItem> noteItemList = new ArrayList<>();
    for (Note note : noteList) {
      Long noteId = note.getId();
      Long modDate = note.getModDate();
      String name = note.getName();
      if (note.getType() == 0) {
        name = Jsoup.parse(name).text();
        if (name.length() > 40) {
          name = name.substring(0, 35) + R.string.points;
        }
        noteItemList.add(new NoteTextItem(modDate, name, note.getText(), noteId,
            noteDao.findBookIdByNoteId(noteId)));
      } else if (note.getType() == 1) {
        noteItemList.add(new NoteAudioItem(modDate, name, noteId,
            noteDao.findBookIdByNoteId(noteId)));
      } else {
        noteItemList.add(new NoteImageItem(modDate, name, noteId,
            noteDao.findBookIdByNoteId(noteId)));
      }
    }
    return noteItemList;
  }

  public String getNoteText(Long noteId) {
    return noteDao.findTextById(noteId);
  }

  public byte[] getNoteMedia(Long noteFileId) {
    return noteDao.getNoteFileMedia(noteFileId);
  }

  public void linkNoteWithBook(Long bookId, Long noteId) {
    noteDao.linkNoteWithBook(bookId, noteId);
  }

  /**
   * Gets the sorted noteList by sortCriteria.
   *
   * @param sortCriteria sortCriteria of the list
   * @param noteList     noteList that should be sorted
   * @return Returns the sorted noteList
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
   * Gets the sorted noteList by sortCriteria.
   *
   * @param sortCriteria sortCriteria of the list
   * @param bookId       id of the book
   * @return Returns the sorted noteList
   */
  public List<NoteItem> getSortedNoteList(SortCriteria sortCriteria, Long bookId) {
    List<Note> noteListDb = noteDao.getAllNotesForBook(bookId);
    List<NoteItem> noteList = createItemList(noteListDb);

    return sortNoteList(sortCriteria, noteList);
  }

  /**
   * Gets the sorted noteList by sortCriteria.
   *
   * @param sortCriteria sortCriteria of the list
   * @return Returns the sorted noteList
   */
  public List<NoteItem> getAllSortedNoteList(SortCriteria sortCriteria) {
    List<Note> allNoteList = noteDao.findAll();
    List<NoteItem> noteList = createItemList(allNoteList);

    return sortNoteList(sortCriteria, noteList);
  }

}
