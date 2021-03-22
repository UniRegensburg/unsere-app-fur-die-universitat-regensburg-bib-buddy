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
 * The LibraryRecyclerViewAdapter provides a binding from the libraryList to the view
 * that is displayed within the RecyclerView of the LibraryFragment.
 *
 * @author Claudia Schönherr, Luis Moßburger
 */
public class LibraryRecyclerViewAdapter
    extends RecyclerView.Adapter<LibraryRecyclerViewAdapter.LibraryViewHolder> {

  private final LibraryListener listener;
  private final Context context;
  private List<ShelfItem> libraryList;
  private ViewGroup parent;

  /**
   * Constructor of the LibraryRecyclerViewAdapter.
   *
   * @param libraryList libraryList of the current shelves
   * @param listener    listener for the interface and callback of the shelves
   * @param context     context is required for the LibraryRecyclerViewAdapter texts (getBookString)
   */
  public LibraryRecyclerViewAdapter(List<ShelfItem> libraryList, LibraryListener listener,
                                    Context context) {

    this.libraryList = libraryList;
    this.listener = listener;
    this.context = context;
  }

  @NonNull
  @Override
  public LibraryRecyclerViewAdapter.LibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent,
                                                                         int viewType) {
    // RecyclerView calls this method whenever it needs to create a new ViewHolder.
    // The method creates and initializes the ViewHolder and its associated View,
    // but does not fill in the view's contents—the ViewHolder
    // has not yet been bound to specific data.

    this.parent = parent;

    return new LibraryViewHolder(LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.list_view_item_library, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull LibraryViewHolder holder, int position) {
    // Get element from your dataset at this position and replace the contents of the view
    // with that element
    ShelfItem shelfItem = libraryList.get(position);

    holder.getTextView().setText(shelfItem.getName());
    holder.getImageView().setImageResource(shelfItem.getImage());

    int bookCount = shelfItem.getBookCount();
    holder.getTextBookCount().setText(getBookString(bookCount));

    int noteCount = shelfItem.getNoteCount();
    holder.getTextNoteCount().setText(getNoteString(noteCount));

    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (getSelectedLibraryItems().size() > 0) {
          listener.onLongItemClicked(position, shelfItem, v);
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

        listener.onLongItemClicked(position, shelfItem, v);
        return true;
      }
    });
  }

  private String getBookString(int bookCount) {
    if (bookCount == 1) {
      return bookCount + " " + context.getString(R.string.book);
    }

    return bookCount + " " + context.getString(R.string.books);
  }

  private String getNoteString(int noteCount) {
    if (noteCount == 1) {
      return noteCount + " " + context.getString(R.string.note);
    }

    return noteCount + " " + context.getString(R.string.navigation_notes);
  }

  @Override
  public int getItemCount() {
    return libraryList.size();
  }

  public void setLibraryList(List<ShelfItem> libraryList) {
    this.libraryList = libraryList;
  }

  /**
   * This method fetches the number of items selected in the recyclerView.
   *
   * @return returns the selected recyclerView items.
   */
  public List<LibraryItem> getSelectedLibraryItems() {
    List<LibraryItem> selectedItems = new ArrayList<>();
    if (parent != null) {
      int itemNumber = parent.getChildCount();
      for (int i = 0; i < itemNumber; i++) {
        if (parent.getChildAt(i).isSelected()) {
          selectedItems.add(libraryList.get(i));
        }
      }
    }
    return selectedItems;
  }

  public interface LibraryListener {
    void onItemClicked(int position);

    void onLongItemClicked(int position, ShelfItem shelfItem, View view);
  }


  public class LibraryViewHolder extends RecyclerView.ViewHolder {

    private final TextView textView;
    private final ImageView imageView;
    private final TextView textBookCount;
    private final TextView textNoteCount;

    /**
     * The LibraryViewHolder describes a shelf item view and metadata about its place
     * within the RecyclerView of the LibraryFragment.
     */
    public LibraryViewHolder(@NonNull View itemView) {
      super(itemView);

      textView = itemView.findViewById(R.id.item_name);
      imageView = itemView.findViewById(R.id.library_icon);
      textBookCount = itemView.findViewById(R.id.text_book);
      textNoteCount = itemView.findViewById(R.id.note_count);
    }

    public TextView getTextView() {
      return textView;
    }

    public ImageView getImageView() {
      return imageView;
    }

    public TextView getTextBookCount() {
      return textBookCount;
    }

    public TextView getTextNoteCount() {
      return textNoteCount;
    }
  }

}
