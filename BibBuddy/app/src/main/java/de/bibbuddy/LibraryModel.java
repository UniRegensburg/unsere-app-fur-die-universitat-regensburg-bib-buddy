package de.bibbuddy;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public class LibraryModel {

  private final ShelfDao shelfDao;
  private final BookDao bookDao;
  private final AuthorDao authorDao;
  private final NoteDao noteDao;

  private List<ShelfItem> libraryList;
  private Long currentShelfId;

  /**
   * DUMMY COMMENT - PLEASE ADJUST.
   *
   * @param context test
   */
  public LibraryModel(Context context) {
    DatabaseHelper databaseHelper = new DatabaseHelper(context);
    this.shelfDao = new ShelfDao(databaseHelper);
    this.bookDao = new BookDao(databaseHelper);
    this.authorDao = new AuthorDao(databaseHelper);
    this.noteDao = new NoteDao(databaseHelper);
  }

  /**
   * DUMMY COMMENT - PLEASE ADJUST.
   *
   * @param name     test
   * @param parentId test
   */
  public void addShelf(String name, Long parentId) {
    Shelf shelf = new Shelf(name, parentId);
    shelfDao.create(shelf);

    Long id = shelfDao.findLatestId();
    libraryList.add(new ShelfItem(name, id, parentId, 0, 0));
  }

  /**
   * DUMMY COMMENT - PLEASE ADJUST.
   *
   * @return test
   */
  public List<ShelfItem> getCurrentLibraryList() {
    return libraryList;
  }

  /**
   * DUMMY COMMENT - PLEASE ADJUST.
   *
   * @param parentId test
   * @return test
   */
  public List<ShelfItem> getLibraryList(Long parentId) {
    currentShelfId = parentId;

    List<Shelf> list = shelfDao.findAllByParentId(parentId);

    libraryList = new ArrayList<>();
    for (Shelf shelf : list) {
      Long shelfId = shelf.getId();
      List<Long> bookIds = bookDao.getAllBookIdsForShelf(shelfId);

      int bookNum = shelfDao.countAllBooksForShelf(shelfId);
      int noteNum = shelfDao.countAllNotesForShelf(bookIds);
      libraryList
          .add(new ShelfItem(shelf.getName(), shelf.getId(), shelf.getShelfId(), bookNum, noteNum));
    }

    return libraryList;
  }

  /**
   * DUMMY COMMENT - PLEASE ADJUST.
   *
   * @param position test
   * @return test
   */
  public ShelfItem getSelectedLibraryItem(int position) {
    return libraryList.get(position);
  }

  /**
   * DUMMY COMMENT - PLEASE ADJUST.
   *
   * @return test
   */
  public Long getShelfId() {
    return currentShelfId;
  }

  /**
   * Deletes all selected shelves and their respective books and notes.
   *
   * @param selectedShelfItems selected shelf items of the user
   */
  public void deleteShelves(List<ShelfItem> selectedShelfItems) {
    if (selectedShelfItems == null) {
      return;
    }

    for (ShelfItem shelf : selectedShelfItems
    ) {
      Long shelfId = shelf.getId();

      List<Long> bookIds = bookDao.getAllBookIdsForShelf(shelfId);

      for (Long bookId : bookIds) {
        deleteNotes(bookId);
        deleteAuthors(bookId);
        bookDao.delete(bookId, shelfId);
      }

      shelfDao.delete(shelfId);
      deleteShelfFromLibraryList(shelf);
    }
  }

  private void deleteShelfFromLibraryList(ShelfItem shelf) {
    for (int i = 0; i < libraryList.size(); i++) {
      if (shelf.equals(libraryList.get(i))) {
        libraryList.remove(i);
      }
    }
  }

  private void deleteAuthors(Long bookId) {
    List<Long> authorIds = bookDao.getAllAuthorsIdsForBook(bookId);

    for (Long authorId : authorIds) {
      authorDao.delete(authorId, bookId);
    }
  }

  private void deleteNotes(Long bookId) {
    List<Long> noteIds = noteDao.getAllNoteIdsForBook(bookId);

    for (Long noteId : noteIds) {
      noteDao.delete(noteId);
    }

  }

  /**
   * Renames the selected shelf.
   *
   * @param shelfItem selected shelf of the user
   */
  public void renameShelf(ShelfItem shelfItem, String shelfName) {
    if (shelfItem == null) {
      return;
    }

    shelfDao.renameShelf(shelfItem.getId(), shelfName);

    for (int i = 0; i < libraryList.size(); i++) {
      if (libraryList.get(i).equals(shelfItem)) {
        shelfItem.setName(shelfName);
        libraryList.set(i, shelfItem);
      }
    }
  }
}
