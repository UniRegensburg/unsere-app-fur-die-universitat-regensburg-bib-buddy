package de.bibbuddy;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

public class LibraryModel {

  private final ShelfDAO shelfDAO;
  private final BookDAO bookDAO;

  private List<ShelfItem> libraryList;
  private Long currentShelfId;

  public LibraryModel(Context context) {
    DatabaseHelper databaseHelper = new DatabaseHelper(context);
    this.shelfDAO = new ShelfDAO(databaseHelper);
    this.bookDAO = new BookDAO(databaseHelper);
  }

  public void addShelf(String name, Long parentId) {
    Shelf shelf = new Shelf(name, parentId);
    shelfDAO.create(shelf);

    Long id = shelfDAO.findLatestId();
    libraryList.add(new ShelfItem(name, id, parentId, 0, 0));
  }

  public List<ShelfItem> getCurrentLibraryList() {
    return libraryList;
  }

  public List<ShelfItem> getLibraryList(Long parentId) {
    currentShelfId = parentId;

    List<Shelf> list = shelfDAO.findAllByParentId(parentId);

    libraryList = new ArrayList<>();
    for (Shelf shelf : list) {
      Long shelfId = shelf.getId();
      List<Long> bookIds = bookDAO.getAllBookIdsForShelf(shelfId);

      int bookNum = shelfDAO.countAllBooksForShelf(shelfId);
      int noteNum = shelfDAO.countAllNotesForShelf(bookIds);
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
