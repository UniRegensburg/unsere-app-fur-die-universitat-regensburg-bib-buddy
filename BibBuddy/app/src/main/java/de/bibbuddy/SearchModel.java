package de.bibbuddy;

import android.content.Context;
import java.util.ArrayList;
import java.util.List;

/**
 * The SearchModel contains all the search data for the SearchFragment.
 *
 * @author Claudia Schönherr
 */
public class SearchModel {

  private final ShelfDao shelfDao;
  private final BookDao bookDao;
  private final AuthorDao authorDao;
  private final NoteDao noteDao;
  private List<SearchItem> searchResultList;

  /**
   * Constructor for a SearchModel.
   *
   * @param context context for the SearchModel
   */
  public SearchModel(Context context) {
    DatabaseHelper databaseHelper = new DatabaseHelper(context);
    this.shelfDao = new ShelfDao(databaseHelper);
    this.bookDao = new BookDao(databaseHelper);
    this.authorDao = new AuthorDao(databaseHelper);
    this.noteDao = new NoteDao(databaseHelper);
    this.searchResultList = new ArrayList<>();
  }

  /**
   * Gets the searchResultList based on the user input.
   *
   * @return Returns the searchResultList
   */
  public List<SearchItem> getSearchResultList(String searchInput) { // TODO filter, sort
    searchResultList.clear();

    List<Shelf> shelfList = shelfDao.findShelvesByName(searchInput);
    for (Shelf shelf : shelfList) {
      searchResultList.add(
          new SearchItem(shelf.getName(), R.drawable.books, shelf.getId(), shelf.getModDate(),
                         SearchItemType.SEARCH_SHELF));
    }

    List<Book> bookList = bookDao.findBooksByTitle(searchInput);
    for (Book book : bookList) {
      searchResultList.add(
          new SearchItem(book.getTitle(), R.drawable.ic_book, book.getId(), book.getModDate(),
                         SearchItemType.SEARCH_BOOK));
    }

    List<Note> noteList = noteDao.findNotesByName(searchInput);
    for (Note note : noteList) {
      searchResultList.add(
          new SearchItem(note.getName(), R.drawable.document, note.getId(),
                         note.getModDate().intValue(),
                         // TODO change modDate to Integer type in Note class in final release
                         SearchItemType.SEARCH_NOTE));
    }

    return searchResultList;
  }

  /**
   * Gets the selected search item at the current position.
   *
   * @param position position of the clicked item
   * @return Returns the clicked SearchItem
   */
  public SearchItem getSelectedSearchItem(int position) {
    return searchResultList.get(position);
  }

  /**
   * Gets the current searchResultList.
   *
   * @return Returns the current searchResultList
   */
  public List<SearchItem> getCurrentSearchResultList() {
    return searchResultList;
  }
}
