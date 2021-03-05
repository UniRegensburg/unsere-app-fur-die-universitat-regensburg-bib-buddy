package de.bibbuddy;

import android.os.Environment;
import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.Writer;
import java.util.ArrayList;
import java.util.List;

/**
 * The ExportBibTex creates and writes a BibTex file.
 * It contains methods for generation of contents used
 * for the export of the BibTex file.
 *
 * @author Silvia Ivanova
 */

public class ExportBibTex {

  private final String folderName;
  private final String fileName;

  private final String bibFileType = ".bib";

  /**
   * The ExportBibTex is responsible for the creating, writing
   * and retrieving of contents, needed for the BibTex Export.
   *
   * @param folderName name of the folder in which the
   *                   BibTex file should be stored
   * @param fileName name of the BibTex file
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
        + folderName + File.separator + fileName + bibFileType;

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
   * Generates the BibTex format for all books in a given shelf.
   *
   * @param shelfId id of the shelf
   * @param bookDao object of the BookDao class
   *                Depends on the used fragment.
   *                Can be accessed through BookFragment or LibraryModel.
   * @param noteDao object of the class NoteDao
   *                Depends on the used fragment.
   *                Can be accessed through BookFragment, LibraryModel or
   *                NoteModel.
   */
  public String getBibFormatBook(Long shelfId, BookDao bookDao, NoteDao noteDao) {

    List<Long> bookIdsCurrentShelf =
        bookDao.getAllBookIdsForShelf(shelfId);
    String bibFormat = "";

    // for each book in the current shelf
    for (int i = 0; i < bookIdsCurrentShelf.size(); i++) {
      Book book;
      book = bookDao.findById(bookIdsCurrentShelf.get(i));
      Long bookId = bookIdsCurrentShelf.get(i);

      bibFormat =  "@book{" + getBibKey(book)
          + "isbn={" + book.getIsbn() + "}," + '\n'
          + getBibAuthorNames(bookId, bookDao)
          + "title={" + book.getTitle() + "}," + '\n'
          + "subtitle={" + book.getSubtitle() + "}," + '\n'
          + "volume={" + book.getVolume() + "}," + '\n'
          + "publisher={" + book.getPublisher() + "}," + '\n'
          + "edition={" + book.getEdition() + "}," + '\n'
          + getBibNotes(book, noteDao)
          + "year=" + book.getPubYear() + '\n' + "}" + '\n' + '\n';

    }
    return bibFormat;
  }

  private String getBibKey(Book book) {
    // remove whitespaces from book's title
    String bookTitle = book.getTitle().replaceAll("\\s+", "");
    return bookTitle + "," + '\n';
  }

  private String getBibNotes(Book book, NoteDao noteDao) {
    List<Long> notesList = noteDao.getAllNoteIdsForBook(book.getId());
    String allNotes = "";

    if (!notesList.isEmpty()) {
      for (int k = 0; k < notesList.size(); k++) {
        String bookTextNotes = noteDao.findTextById(notesList.get(k));
        allNotes +=  "annote={" + bookTextNotes + "}," + '\n';
      }
    }

    return allNotes;
  }

  private String getBibAuthorNames(Long bookId, BookDao bookDao) {
    Book book;
    book = bookDao.findById(bookId);

    List<Author> authorsList = new ArrayList<>();
    authorsList = bookDao.getAllAuthorsForBook(book.getId());
    String authorNames = "";

    if (authorsList.size() > 1) {

      for (int i = 0; i < authorsList.size(); i++) {
        authorNames = authorNames
            + authorsList.get(i).getFirstName()
            + " " + authorsList.get(i).getLastName();
        if (i < authorsList.size()) {
          authorNames = authorNames + " and ";
        }
      }

    } else {

      try {
        authorNames = authorsList.get(0).getFirstName()
            + " " + authorsList.get(0).getLastName();
      } catch (Exception e) {
        authorNames = "";
      }

    }
    return "author={" + authorNames + "}," + '\n';
  }

  /**
   * Writes a BibTex file.
   */
  public void writeBibFile(String bibContent) {
    try {
      File bibFile = new File(Environment.getExternalStorageDirectory()
          + File.separator + folderName + File.separator + fileName + bibFileType);

      FileOutputStream fos = new FileOutputStream(bibFile);
      OutputStreamWriter osw = new OutputStreamWriter(fos);
      Writer fileWriter = new BufferedWriter(osw);
      fileWriter.write(bibContent);
      fileWriter.close();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

}
