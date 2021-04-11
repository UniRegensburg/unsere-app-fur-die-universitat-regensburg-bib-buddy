package de.bibbuddy;

import android.content.Context;
import android.net.Uri;
import android.os.Environment;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.List;

/**
 * The ExportBibTex creates and writes a BibTex file.
 * It contains methods for generation of contents that are
 * used for the export of the BibTex file.
 *
 * @author Silvia Ivanova, Luis Moßburger
 */

public class ExportBibTex {

  private final String folderName;
  private final String fileName;


  /**
   * The ExportBibTex is responsible for the creating, writing
   * and retrieving of contents, needed for the BibTex Export.
   *
   * @param folderName name of the folder in which the
   *                   BibTex file should be stored
   * @param fileName   name of the BibTex file
   */
  public ExportBibTex(String folderName, String fileName) {
    this.folderName = folderName;
    this.fileName = fileName;
  }

  /**
   * Creates a BibTex file.
   */
  public void createBibFile() {
    String rootPathStr = Environment.getExternalStorageDirectory() + File.separator
        + folderName + File.separator + fileName
        + StorageKeys.BIB_FILE_TYPE;

    try {
      File file = new File(rootPathStr);

      if (file.exists()) {
        file.delete();
      }

      file.createNewFile();

      FileOutputStream out = new FileOutputStream(file);
      out.flush();
      out.close();

    } catch (Exception e) {
      e.printStackTrace();
    }

  }

  /**
   * Generates the BibTex format for all books inclusive their
   * notes from the entire library.
   *
   * @param libraryModel object of the LibraryModel class
   * @param bookDao      object of the class BookDao
   *                     Depends on the used fragment.
   *                     Can be accessed through BookModel, LibraryModel
   *                     or BookNotesViewModel.
   * @param noteDao      object of the class NoteDao
   *                     Depends on the used fragment.
   *                     Can be accessed through BookModel, LibraryModel
   *                     or BookNotesViewModel.
   * @return the BibTex format of a library as String
   */
  public String getBibDataLibrary(LibraryModel libraryModel,
                                  BookDao bookDao, NoteDao noteDao) {
    StringBuilder bibFormat = new StringBuilder();

    List<ShelfItem> shelfItem = libraryModel.getCurrentLibraryList();

    // for each shelf in the library
    for (int i = 0; i < shelfItem.size(); i++) {
      Long currentShelfId = shelfItem.get(i).getId();

      bibFormat.append(getBibDataFromShelf(currentShelfId, bookDao,
          noteDao));

    }

    return bibFormat.toString();
  }

  /**
   * Generates the BibTex format for all books in a given shelf.
   *
   * @param shelfId id of the shelf
   * @param bookDao object of the class BookDao
   *                Depends on the used fragment.
   *                Can be accessed through BookModel, LibraryModel
   *                or BookNotesViewModel.
   * @param noteDao object of the class NoteDao
   *                Depends on the used fragment.
   *                Can be accessed through BookModel, LibraryModel
   *                or BookNotesViewModel.
   * @return the BibTex format of a collection of books as String
   */
  public String getBibDataFromShelf(Long shelfId, BookDao bookDao, NoteDao noteDao) {
    List<Long> bookIdsCurrentShelf =
        bookDao.getAllBookIdsForShelf(shelfId);
    StringBuilder bibFormat = new StringBuilder();

    // for each book in the current shelf
    for (int i = 0; i < bookIdsCurrentShelf.size(); i++) {
      Long bookId = bookIdsCurrentShelf.get(i);

      bibFormat.append(getBibDataFromBook(bookId, bookDao, noteDao));

    }

    return bibFormat.toString();
  }

  /**
   * Generates the BibTex format for a given book.
   *
   * @param bookId  id of the book
   * @param bookDao object of the class BookDao
   *                Depends on the used fragment.
   *                Can be accessed through BookModel, LibraryModel
   *                or BookNotesViewModel.
   * @param noteDao object of the class NoteDao
   *                Depends on the used fragment.
   *                Can be accessed through BookModel, LibraryModel
   *                or BookNotesViewModel.
   * @return the BibTex format of a book as String
   */
  public String getBibDataFromBook(Long bookId, BookDao bookDao, NoteDao noteDao) {
    Book book = bookDao.findById(bookId);

    return BibTexKeys.BOOK_TAG + BibTexKeys.OPENING_CURLY_BRACKET + getBibKey(book)

        + BibTexKeys.ISBN + BibTexKeys.OPENING_CURLY_BRACKET + book.getIsbn()
        + BibTexKeys.CLOSING_CURLY_BRACKET + BibTexKeys.COMMA_SEPARATOR + "\n"

        + getBibAuthorNames(bookId, bookDao)

        + BibTexKeys.BOOK_TITLE + BibTexKeys.OPENING_CURLY_BRACKET + book.getTitle()
        + BibTexKeys.CLOSING_CURLY_BRACKET + BibTexKeys.COMMA_SEPARATOR + "\n"

        + BibTexKeys.SUBTITLE + BibTexKeys.OPENING_CURLY_BRACKET + book.getSubtitle()
        + BibTexKeys.CLOSING_CURLY_BRACKET + BibTexKeys.COMMA_SEPARATOR + "\n"

        + BibTexKeys.PUBLISHER + BibTexKeys.OPENING_CURLY_BRACKET + book.getPublisher()
        + BibTexKeys.CLOSING_CURLY_BRACKET + BibTexKeys.COMMA_SEPARATOR + "\n"

        + BibTexKeys.EDITION + BibTexKeys.OPENING_CURLY_BRACKET + book.getEdition()
        + BibTexKeys.CLOSING_CURLY_BRACKET + BibTexKeys.COMMA_SEPARATOR + "\n"

        + getBibNotesFromBook(book, noteDao)

        + BibTexKeys.YEAR + book.getPubYear() + "\n" + BibTexKeys.CLOSING_CURLY_BRACKET
        + "\n" + "\n";

  }

  private String getBibKey(Book book) {
    // remove whitespaces from book's title
    return book.getTitle().replaceAll("\\s+", "") + "," + "\n";
  }

  private String getBibNotesFromBook(Book book, NoteDao noteDao) {
    List<Long> notesList = noteDao.getTextNoteIdsForBook(book.getId());
    StringBuilder allNotes = new StringBuilder();

    for (int i = 0; i < notesList.size(); i++) {
      String bookTextNotes = noteDao.findStrippedTextById(notesList.get(i));
      allNotes.append(bookTextNotes);
    }

    return BibTexKeys.ANNOTE + BibTexKeys.OPENING_CURLY_BRACKET + allNotes
        + BibTexKeys.CLOSING_CURLY_BRACKET + BibTexKeys.COMMA_SEPARATOR + "\n";

  }

  private String getBibAuthorNames(Long bookId, BookDao bookDao) {
    Book book = bookDao.findById(bookId);

    List<Author> authorsList = bookDao.getAllAuthorsForBook(book.getId());
    StringBuilder authorNames = new StringBuilder();

    for (int i = 0; i < authorsList.size(); i++) {
      authorNames.append(authorsList.get(i).getLastName()).append(", ")
          .append(authorsList.get(i).getFirstName());

      if (i < authorsList.size() - 1) {
        authorNames.append(BibTexKeys.AND_MULTIPLE_AUTHORS);
      }
    }

    if (authorsList.isEmpty()) {
      authorNames = new StringBuilder();
    }

    return BibTexKeys.AUTHOR + BibTexKeys.OPENING_CURLY_BRACKET + authorNames
        + BibTexKeys.CLOSING_CURLY_BRACKET + BibTexKeys.COMMA_SEPARATOR + "\n";

  }

  /**
   * Writes a BibTex file.
   *
   * @param bibContent the content of the BibTex file as String
   */
  public void writeBibFile(String bibContent) {

    try {
      File bibFile = new File(Environment.getExternalStorageDirectory()
          + File.separator + folderName + File.separator + fileName
          + StorageKeys.BIB_FILE_TYPE);

      FileOutputStream fileOutputStream = new FileOutputStream(bibFile);
      OutputStreamWriter outputStreamWriter = new OutputStreamWriter(fileOutputStream);
      Writer fileWriter = new BufferedWriter(outputStreamWriter);
      fileWriter.write(bibContent);
      fileWriter.close();

    } catch (IOException e) {
      e.printStackTrace();
    }

  }

  /**
   * Writes a temporary BibTex file.
   *
   * @param context        the context of the used fragment
   * @param content        the content of temporary BibTeX file
   * @return               the temporary file as URI
   */
  public Uri writeTemporaryBibFile(Context context, String content) {

    String fullFileName = fileName + StorageKeys.BIB_FILE_TYPE;
    File file = new File(context.getCacheDir(), fullFileName);

    try {

      FileWriter fileWriter = new FileWriter(file);
      fileWriter.append(content);
      fileWriter.flush();
      fileWriter.close();

    } catch (Exception e) {
      Toast.makeText(context,
          context.getString(R.string.exception_failed_temp_file) + e.toString(),
          Toast.LENGTH_LONG).show();
    }

    File parentFile = new File(context.getCacheDir(), "");
    File tempFile = new File(parentFile, fullFileName);

    return FileProvider.getUriForFile(
        context,
        "de.bibbuddy.app.file_provider_paths",
        tempFile);
  }

}
