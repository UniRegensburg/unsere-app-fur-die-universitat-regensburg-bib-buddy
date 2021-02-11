package de.bibbuddy;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public class LibraryModel {

  private final ShelfDao shelfDao;
  private final BookDao bookDao;

  private List<ShelfItem> libraryList;
  private Long currentShelfId;

  public LibraryModel(Context context) {
    DatabaseHelper databaseHelper = new DatabaseHelper(context);
    this.shelfDao = new ShelfDao(databaseHelper);
    this.bookDao = new BookDao(databaseHelper);
  }

  public void addShelf(String name, Long parentId) {
    Shelf shelf = new Shelf(name, parentId);
    shelfDao.create(shelf);

    Long id = shelfDao.findLatestId();
    libraryList.add(new ShelfItem(name, id, parentId, 0, 0));
  }

  public List<ShelfItem> getCurrentLibraryList() {
    return libraryList;
  }

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

  public ShelfItem getSelectedLibraryItem(int position) {
    return libraryList.get(position);
  }

  public Long getShelfId() {
    return currentShelfId;
  }

}
