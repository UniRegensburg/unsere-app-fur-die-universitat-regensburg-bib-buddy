package de.bibbuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * Custom RecyclerViewAdapter provides a binding from the bookList to the view
 * that is displayed within the RecyclerView of the BookFragment.
 *
 * @author Claudia Schönherr, Luis Moßburger
 */

public class BookRecyclerViewAdapter
    extends RecyclerView.Adapter<BookRecyclerViewAdapter.BookViewHolder> {

  private final BookRecyclerViewAdapter.BookListener listener;
  private final Context context;

  private List<BookItem> bookList;
  private ViewGroup parent;

  private String getNoteString(int noteCount) {
    if (noteCount == 1) {
      return noteCount + " " + context.getString(R.string.note);
    }

    return noteCount + " " + context.getString(R.string.navigation_notes);
  }

  /**
   * Constructor of the BookRecyclerViewAdapter.
   *
   * @param bookList bookList of the current books
   * @param listener listener for the interface and callback of the books
   * @param context  context is required for the BookRecyclerViewAdapter texts (getNoteString)
   */
  public BookRecyclerViewAdapter(List<BookItem> bookList, BookListener listener, Context context) {
    this.bookList = bookList;
    this.listener = listener;
    this.context = context;
  }

  public void setBookList(List<BookItem> bookList) {
    this.bookList = bookList;
  }

  @NonNull
  @Override
  public BookRecyclerViewAdapter.BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                   int viewType) {
    this.parent = parent;

    return new BookViewHolder(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.list_view_item_book, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull BookRecyclerViewAdapter.BookViewHolder holder,
                               int position) {
    BookItem bookItem = bookList.get(position);

    holder.getTextTitleView().setText(bookItem.getName());
    holder.getImageBookView().setImageResource(bookItem.getImage());
    holder.getTextBookAuthors().setText(bookItem.getAuthors());
    holder.getTextBookYear().setText(String.valueOf(bookItem.getYear()));
    holder.getTextNoteCount().setText(getNoteString(bookItem.getNoteCount()));

    if (holder.getTextBookAuthors().getText().equals("")) {
      holder.getTextBookAuthors().setVisibility(View.GONE);
    }

    if (holder.getTextBookYear().getText().equals("0")) {
      holder.getTextBookYear().setVisibility(View.GONE);
    }

    holder.itemView.setOnClickListener(v -> {
      if (!getSelectedBookItems().isEmpty()) {
        listener.onBookLongClicked(position, bookItem, v);
      } else {
        listener.onBookClicked(position);
      }
    });

    holder.itemView.setOnLongClickListener(v -> {
      if (position == RecyclerView.NO_POSITION) {
        return false;
      }

      listener.onBookLongClicked(position, bookItem, v);
      return true;
    });
  }

  @Override
  public int getItemCount() {
    return bookList.size();
  }

  public BookItem getBookItem(int position) {
    return bookList.get(position);
  }

  /**
   * Fetches the number of items selected in the recyclerView.
   *
   * @return the selected recyclerView items
   */
  public List<BookItem> getSelectedBookItems() {
    List<BookItem> selectedItems = new ArrayList<>();

    for (int i = 0; i < parent.getChildCount(); i++) {
      if (parent.getChildAt(i).isSelected()) {
        selectedItems.add(bookList.get(i));
      }
    }

    return selectedItems;
  }

  public interface BookListener {
    void onBookClicked(int position);

    void onBookLongClicked(int position, BookItem bookItem, View v);
  }

  public static class BookViewHolder extends RecyclerView.ViewHolder {

    private final TextView textTitleView;
    private final ImageView imageBookView;
    private final TextView textBookAuthors;
    private final TextView textBookYear;
    private final TextView textNoteCount;

    /**
     * Custom ViewHolder constructor to setup its basic view.
     *
     * @param itemView view of the BookRecyclerView-item
     */
    public BookViewHolder(@NonNull View itemView) {
      super(itemView);

      textTitleView = itemView.findViewById(R.id.book_title);
      imageBookView = itemView.findViewById(R.id.person_icon);
      textBookAuthors = itemView.findViewById(R.id.book_authors);
      textBookYear = itemView.findViewById(R.id.book_year);
      textNoteCount = itemView.findViewById(R.id.note_count);
    }

    public TextView getTextTitleView() {
      return textTitleView;
    }

    public ImageView getImageBookView() {
      return imageBookView;
    }

    public TextView getTextBookAuthors() {
      return textBookAuthors;
    }

    public TextView getTextBookYear() {
      return textBookYear;
    }

    public TextView getTextNoteCount() {
      return textNoteCount;
    }
  }

}
