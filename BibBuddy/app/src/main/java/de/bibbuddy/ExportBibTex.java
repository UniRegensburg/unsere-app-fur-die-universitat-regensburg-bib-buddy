package de.bibbuddy;

import android.content.Context;
import android.net.Uri;
import android.util.Log;
import android.widget.Toast;
import androidx.core.content.FileProvider;
import java.io.File;
import java.io.FileWriter;
import java.util.List;

/**
 * The ExportBibTex creates and writes a BibTex file.
 * It contains methods for generation of contents that are
 * used for the export of the BibTex file.
 *
 * @author Silvia Ivanova, Luis Moßburger
 */

public class ExportBibTex {

  private static final String TAG = ExportBibTex.class.getSimpleName();

  private final String fileName;


  /**
   * The ExportBibTex is responsible for the creating, writing
   * and retrieving of contents, needed for the BibTex Export.
   *
   * @param fileName   name of the BibTex file
   */
  public ExportBibTex(String fileName) {
    this.fileName = fileName;
  }

  /**
   * Generates the BibTex format for all books inclusive their
   * notes from the entire library.
   *
   * @param libraryModel object of the LibraryModel class
   * @param bookModel    object of the class BookModel
   *                     used to retrieve book data
   * @param noteModel    object of the class NoteModel
   *                     used to retrieve note data
   * @return             the BibTex format of a library as String
   */
  public String getBibDataLibrary(LibraryModel libraryModel,
                                  BookModel bookModel, NoteModel noteModel) {
    StringBuilder bibFormat = new StringBuilder();

    List<ShelfItem> shelfItem = libraryModel.getCurrentLibraryList();

    // for each shelf in the library
    for (int i = 0; i < shelfItem.size(); i++) {
      Long currentShelfId = shelfItem.get(i).getId();

      bibFormat.append(getBibDataFromShelf(currentShelfId, bookModel,
          noteModel));

    }

    return bibFormat.toString();
  }

  /**
   * Generates the BibTex format for all books in a given shelf.
   *
   * @param shelfId id of the shelf
   * @param bookModel  object of the class BookModel
   *                   used to retrieve book data from a selected shelf
   * @param noteModel  object of the class NoteModel
   *                   used to retrieve note data from a selected shelf
   * @return           the BibTex format of a collection of books as String
   */
  public String getBibDataFromShelf(Long shelfId, BookModel bookModel, NoteModel noteModel) {
    List<Long> bookIdsCurrentShelf =
        bookModel.getAllBookIdsForShelf(shelfId);
    StringBuilder bibFormat = new StringBuilder();

    // for each book in the current shelf
    for (int i = 0; i < bookIdsCurrentShelf.size(); i++) {
      Long bookId = bookIdsCurrentShelf.get(i);

      bibFormat.append(getBibDataFromBook(bookId, bookModel, noteModel));

    }

    return bibFormat.toString();
  }

  /**
   * Generates the BibTex format for a given book.
   *
   * @param bookId     id of the book
   * @param bookModel  object of the class BookModel
   *                   used to retrieve book data
   * @param noteModel  object of the class NoteModel
   *                   used to retrieve note data
   * @return           the BibTex format of a book as String
   */
  public String getBibDataFromBook(Long bookId, BookModel bookModel, NoteModel noteModel) {
    Book book = bookModel.getBookById(bookId);

    return BibTexKeys.BOOK_TAG + BibTexKeys.OPENING_CURLY_BRACKET + getBibKey(book)
        + BibTexKeys.COMMA_SEPARATOR + "\n"

        + BibTexKeys.ISBN + BibTexKeys.OPENING_CURLY_BRACKET + book.getIsbn()
        + BibTexKeys.CLOSING_CURLY_BRACKET + BibTexKeys.COMMA_SEPARATOR + "\n"

        + getBibAuthorNames(bookId, bookModel)

        + BibTexKeys.BOOK_TITLE + BibTexKeys.OPENING_CURLY_BRACKET + book.getTitle()
        + BibTexKeys.CLOSING_CURLY_BRACKET + BibTexKeys.COMMA_SEPARATOR + "\n"

        + BibTexKeys.SUBTITLE + BibTexKeys.OPENING_CURLY_BRACKET + book.getSubtitle()
        + BibTexKeys.CLOSING_CURLY_BRACKET + BibTexKeys.COMMA_SEPARATOR + "\n"

        + BibTexKeys.PUBLISHER + BibTexKeys.OPENING_CURLY_BRACKET + book.getPublisher()
        + BibTexKeys.CLOSING_CURLY_BRACKET + BibTexKeys.COMMA_SEPARATOR + "\n"

        + BibTexKeys.EDITION + BibTexKeys.OPENING_CURLY_BRACKET + book.getEdition()
        + BibTexKeys.CLOSING_CURLY_BRACKET + BibTexKeys.COMMA_SEPARATOR + "\n"

        + getBibNotesFromBook(book, noteModel)

        + BibTexKeys.YEAR + book.getPubYear() + "\n" + BibTexKeys.CLOSING_CURLY_BRACKET
        + "\n" + "\n";

  }

  private String getBibKey(Book book) {
    // remove whitespaces from book's title
    return book.getTitle().replaceAll("\\s+",
        "");
  }

  private String getBibNotesFromBook(Book book, NoteModel noteModel) {
    List<Long> notesList = noteModel.getTextNoteIdsForBook(book.getId());
    StringBuilder allNotes = new StringBuilder();

    for (int i = 0; i < notesList.size(); i++) {
      String bookTextNotes = noteModel.findStrippedTextById(notesList.get(i));
      allNotes.append(bookTextNotes);
    }

    return BibTexKeys.ANNOTE + BibTexKeys.OPENING_CURLY_BRACKET + allNotes
        + BibTexKeys.CLOSING_CURLY_BRACKET + BibTexKeys.COMMA_SEPARATOR + "\n";

  }

  private String getBibAuthorNames(Long bookId, BookModel bookModel) {
    Book book = bookModel.getBookById(bookId);

    List<Author> authorsList = bookModel.getAuthorList(book.getId());
    StringBuilder authorNames = new StringBuilder();

    for (int i = 0; i < authorsList.size(); i++) {
      authorNames.append(authorsList.get(i).getLastName())
          .append(BibTexKeys.COMMA_SEPARATOR + " ")
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
   * Writes a temporary BibTex file.
   *
   * @param context the context of the used fragment
   * @param content the content of temporary BibTeX file
   * @return the temporary file as URI
   */
  public Uri writeTemporaryBibFile(Context context, String content) {

    String fullFileName = fileName + StorageKeys.BIB_FILE_TYPE;
    File file = new File(context.getCacheDir(), fullFileName);

    try {
      FileWriter fileWriter = new FileWriter(file);
      fileWriter.append(content);
      fileWriter.flush();
      fileWriter.close();
    } catch (Exception ex) {
      Toast.makeText(context, context.getString(R.string.exception_failed_temp_file),
          Toast.LENGTH_LONG).show();

      Log.e(TAG, ex.toString(), ex);
    }

    File parentFile = new File(context.getCacheDir(), "");
    File tempFile = new File(parentFile, fullFileName);

    return FileProvider.getUriForFile(
        context,
        "de.bibbuddy.app.file_provider_paths",
        tempFile);
  }

}
