package de.bibbuddy;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public class LibraryModel {

  private final ShelfDao shelfDao;
  private final BookDao bookDao;

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

}
