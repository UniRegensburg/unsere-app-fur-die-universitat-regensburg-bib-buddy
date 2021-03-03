package de.bibbuddy;

import android.content.Context;
import java.util.ArrayList;
import java.util.Collections;
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
  public List<SearchItem> getSearchResultList(String searchInput, SearchSortCriteria sortCriteria) {
    searchResultList.clear();

    // TODO filter

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
          new SearchItem(note.getName(), R.drawable.document, note.getId(), note.getModDate(),
                         SearchItemType.SEARCH_TEXT_NOTE));
    }

    sortSearchResultList(sortCriteria);

    return searchResultList;
  }

  private void sortSearchResultList(SearchSortCriteria sortCriteria) {
    if (sortCriteria == SearchSortCriteria.NAME_ASCENDING) {
      sortSearchResultListByName(false);
    } else if (sortCriteria == SearchSortCriteria.NAME_DESCENDING) {
      sortSearchResultListByName(true);
    } else if (sortCriteria == SearchSortCriteria.MOD_DATE_OLDEST) {
      sortSearchResultListByModDate(false);
    } else if (sortCriteria == SearchSortCriteria.MOD_DATE_LATEST) {
      sortSearchResultListByModDate(true);
    }
  }

  private void sortSearchResultListByName(boolean isSortNameDescending) {
    searchResultList.sort((o1, o2) -> { // sort name ascending
      return o1.getName().compareToIgnoreCase(o2.getName());
    });

    if (isSortNameDescending) {
      Collections.reverse(searchResultList);  // sort name descending
    }
  }


  private void sortSearchResultListByModDate(boolean isSortModDateLatest) {
    searchResultList.sort((o1, o2) -> { // oldest modDate is at the beginning
      return o1.getModDate().compareTo(o2.getModDate());
    });

    if (isSortModDateLatest) {
      Collections.reverse(searchResultList); // latest modDate is at the beginning
    }
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

  public Long getBookIdByNoteId(Long noteId) {
    return noteDao.findBookIdByNoteId(noteId);
  }

  /**
   * Gets the sorted search result list by sortCriteria.
   *
   * @param sortCriteria sortCriteria of the list
   * @return Returns the sorted search results
   */
  public List<SearchItem> getSortedSearchResultList(SearchSortCriteria sortCriteria) {
    sortSearchResultList(sortCriteria);

    return searchResultList;
  }
}
