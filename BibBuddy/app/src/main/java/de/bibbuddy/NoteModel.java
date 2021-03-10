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

  private String getDate(Long date) {
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
      String modDate = getDate(note.getModDate());
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

}
