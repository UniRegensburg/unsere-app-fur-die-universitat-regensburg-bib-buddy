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
  private final NoteDao noteDao;

  private final List<SearchItem> searchResultList;

  /**
   * Constructor for a SearchModel.
   *
   * @param context context for the SearchModel
   */
  public SearchModel(Context context) {
    DatabaseHelper databaseHelper = new DatabaseHelper(context);
    this.shelfDao = new ShelfDao(databaseHelper);
    this.bookDao = new BookDao(databaseHelper);
    this.noteDao = new NoteDao(databaseHelper);

    this.searchResultList = new ArrayList<>();
  }

  /**
   * Gets the searchResultList based on the user input.
   *
   * @param searchInput    input of the search
   * @param sortTypeLut    sortTypeLut for the searchResultList
   * @param filterCriteria filterCriteria for the searchResultList
   * @return returns the searchResultList
   */
  public List<SearchItem> getSearchResultList(String searchInput, SortTypeLut sortTypeLut,
                                              boolean[] filterCriteria) {
    searchResultList.clear();

    if (filterCriteria[0]) { // filter shelf
      searchShelves(searchInput);
    }

    if (filterCriteria[1]) { // filter book
      searchBooks(searchInput);
    }

    if (filterCriteria[2]) { // filter note
      searchNotes(searchInput);
    }

    sortSearchResultList(sortTypeLut);

    return searchResultList;
  }

  private void searchShelves(String searchInput) {
    List<Shelf> shelfList = shelfDao.findShelvesByName(searchInput);
    for (Shelf shelf : shelfList) {
      searchResultList.add(
          new SearchItem(shelf.getName(), R.drawable.books, shelf.getId(), shelf.getModDate(),
                         SearchTypeLut.SEARCH_SHELF));
    }
  }

  private void searchBooks(String searchInput) {
    List<Book> bookList = bookDao.findBooksByTitle(searchInput);
    for (Book book : bookList) {
      searchResultList.add(
          new SearchItem(book.getTitle(), R.drawable.ic_book, book.getId(), book.getModDate(),
                         SearchTypeLut.SEARCH_BOOK));
    }
  }

  private void searchNotes(String searchInput) {
    List<Note> noteList = noteDao.findTextNotesByName(searchInput);
    for (Note note : noteList) {
      searchResultList.add(
          new SearchItem(note.getName(), R.drawable.document, note.getId(), note.getModDate(),
                         SearchTypeLut.SEARCH_TEXT_NOTE));
    }
  }


  private void sortSearchResultList(SortTypeLut sortTypeLut) {
    switch (sortTypeLut) {

      case MOD_DATE_LATEST:
        searchResultList.sort(new SortDate());
        break;

      case MOD_DATE_OLDEST:
        searchResultList.sort(new SortDate().reversed());
        break;

      case NAME_ASCENDING:
        searchResultList.sort(new SortName());
        break;

      case NAME_DESCENDING:
        searchResultList.sort(new SortName().reversed());
        break;

      default:
        throw new IllegalArgumentException();
    }
  }


  /**
   * Gets the selected search item at the current position.
   *
   * @param position position of the clicked item
   * @return returns the clicked SearchItem
   */
  public SearchItem getSelectedSearchItem(int position) {
    return searchResultList.get(position);
  }

  public Long getBookIdByNoteId(Long noteId) {
    return noteDao.findBookIdByNoteId(noteId);
  }

  /**
   * Gets the sorted search result list by sortTypeLut.
   *
   * @param sortTypeLut sortTypeLut of the list
   * @return returns the sorted search results
   */
  public List<SearchItem> getSortedSearchResultList(SortTypeLut sortTypeLut) {
    sortSearchResultList(sortTypeLut);

    return searchResultList;
  }

  public Long getShelfIdByBook(Long bookId) {
    return bookDao.findShelfIdByBook(bookId);
  }

  public String getBookTitleByBookId(Long noteId) {
    return bookDao.findBookTitleByBookId(noteId);
  }

  public String getShelfNameByBook(Long searchItemId) {
    return bookDao.findShelfNameByBook(searchItemId);
  }

}
