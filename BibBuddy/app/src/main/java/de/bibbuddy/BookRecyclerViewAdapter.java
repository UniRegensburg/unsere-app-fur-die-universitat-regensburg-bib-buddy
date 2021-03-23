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

  /**
   * Constructor of the BookRecyclerViewAdapter.
   *
   * @param bookList bookList of the current books
   * @param listener listener for the interface and callback of the books
   * @param context  context is required for the BookRecyclerViewAdapter texts (getNoteString)
   * @author Claudia Schönherr
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

    return new BookRecyclerViewAdapter.BookViewHolder(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.list_view_item_book, parent, false));
  }

  private String getNoteString(int noteCount) {
    if (noteCount == 1) {
      return noteCount + " " + context.getString(R.string.note);
    }

    return noteCount + " " + context.getString(R.string.navigation_notes);
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

    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (getSelectedBookItems().size() > 0) {
          listener.onLongItemClicked(position, bookItem, v);
        } else {
          listener.onItemClicked(position);
        }
      }
    });

    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        if (position == RecyclerView.NO_POSITION) {
          return false;
        }

        listener.onLongItemClicked(position, bookItem, v);
        return true;
      }
    });
  }

  @Override
  public int getItemCount() {
    return bookList.size();
  }

  /**
   * This method fetches the number of items selected in the recyclerView.
   *
   * @return returns the selected recyclerView items.
   */
  public List<BookItem> getSelectedBookItems() {
    List<BookItem> selectedItems = new ArrayList<>();

    if (parent != null) {
      for (int i = 0; i < parent.getChildCount(); i++) {
        if (parent.getChildAt(i).isSelected()) {
          selectedItems.add(bookList.get(i));
        }
      }
    }

    return selectedItems;
  }

  public interface BookListener { // create an interface
    void onItemClicked(int position); // create callback function

    void onLongItemClicked(int position, BookItem bookItem, View v);
  }

  public class BookViewHolder extends RecyclerView.ViewHolder {

    private final TextView textTitleView;
    private final ImageView imageBookView;
    private final TextView textBookAuthors;
    private final TextView textBookYear;
    private final TextView textNoteCount;

    /**
     * Custom ViewHolder constructor to setup its basic view.
     *
     * @param itemView View of the BookRecyclerView-item.
     * @author Claudia Schönherr
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
