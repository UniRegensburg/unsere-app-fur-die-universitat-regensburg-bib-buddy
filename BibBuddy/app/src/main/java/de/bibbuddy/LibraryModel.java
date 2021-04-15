package de.bibbuddy;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

/**
 * The LibraryModel contains all the shelf data for the LibraryFragment.
 *
 * @author Claudia Schönherr, Silvia Ivanova
 */
public class LibraryModel {

  private final ShelfDao shelfDao;
  private final BookDao bookDao;
  private final AuthorDao authorDao;
  private final NoteDao noteDao;

  private List<ShelfItem> libraryList;
  private Long currentShelfId;

  /**
   * The LibraryModel contains all the shelf data for the LibraryFragment.
   *
   * @param context context for the LibraryModel
   */
  public LibraryModel(Context context) {
    DatabaseHelper databaseHelper = new DatabaseHelper(context);
    this.shelfDao = new ShelfDao(databaseHelper);
    this.bookDao = new BookDao(databaseHelper);
    this.authorDao = new AuthorDao(databaseHelper);
    this.noteDao = new NoteDao(databaseHelper);
  }

  /**
   * Adds a new book to the bookList and database.
   *
   * @param name     name of the new shelf
   * @param parentId parentId of the new shelf (is deprecated)
   */
  public void addShelf(String name, Long parentId) {
    Shelf shelf = new Shelf(name, parentId);
    shelfDao.create(shelf);

    shelf = shelfDao.findById(shelfDao.findLatestId());

    libraryList.add(new ShelfItem(shelf, 0, 0));
  }

  /**
   * Gets the current libraryList.
   *
   * @return the current libraryList
   */
  public List<ShelfItem> getCurrentLibraryList() {
    return libraryList;
  }

  /**
   * Gets the libraryList of the current parentId of the shelf.
   *
   * @param parentId parentId of the shelf (is deprecated)
   * @return the libraryList of the given id
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

      libraryList.add(new ShelfItem(shelf, bookNum, noteNum));
    }

    return libraryList;
  }

  /**
   * Gets the selected shelf at the current position.
   *
   * @param position position of the clicked item
   * @return the clicked ShelfItem
   */
  public ShelfItem getSelectedLibraryItem(int position) {
    return libraryList.get(position);
  }

  /**
   * Gets the current id of the shelf.
   *
   * @return the currentShelfId
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
    List<Long> authorIds = bookDao.getAllAuthorIdsForBook(bookId);

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

  private void sortLibraryList(SortTypeLut sortTypeLut) {
    switch (sortTypeLut) {

      case MOD_DATE_LATEST:
        libraryList.sort(new SortDate());
        break;

      case MOD_DATE_OLDEST:
        libraryList.sort(new SortDate().reversed());
        break;

      case NAME_ASCENDING:
        libraryList.sort(new SortName());
        break;

      case NAME_DESCENDING:
        libraryList.sort(new SortName().reversed());
        break;

      default:
        break;
    }
  }

  /**
   * Gets the sorted search result list by sortTypeLut.
   *
   * @param sortTypeLut sortTypeLut of the list
   * @return the sorted shelves
   */
  public List<ShelfItem> getSortedLibraryList(SortTypeLut sortTypeLut) {
    sortLibraryList(sortTypeLut);

    return libraryList;
  }

  /**
   * Gets the sorted libraryList by sortTypeLut.
   *
   * @param sortTypeLut sortTypeLut of the list
   * @return the sorted libraryList
   */
  public List<ShelfItem> getSortedLibraryList(SortTypeLut sortTypeLut,
                                              List<ShelfItem> libraryList) {
    this.libraryList = libraryList;
    sortLibraryList(sortTypeLut);

    return libraryList;
  }

}
