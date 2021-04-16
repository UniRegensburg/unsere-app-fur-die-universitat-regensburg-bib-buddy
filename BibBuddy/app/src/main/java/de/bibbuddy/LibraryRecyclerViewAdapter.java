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
    this.parent = parent;

    return new LibraryViewHolder(LayoutInflater.from(parent.getContext())
                                     .inflate(R.layout.list_view_item_library, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull LibraryViewHolder holder, int position) {
    ShelfItem shelfItem = libraryList.get(position);

    holder.getName().setText(shelfItem.getName());
    holder.getLibraryIcon().setImageResource(shelfItem.getImage());

    int bookCount = shelfItem.getBookCount();
    holder.getTextBookCount().setText(getBookString(bookCount));

    int noteCount = shelfItem.getNoteCount();
    holder.getTextNoteCount().setText(getNoteString(noteCount));

    holder.itemView.setOnClickListener(v -> {
      if (!getSelectedLibraryItems().isEmpty()) {
        listener.onShelfLongClicked(shelfItem, v);
      } else {
        listener.onShelfClicked(position);
      }
    });

    holder.itemView.setOnLongClickListener(v -> {
      if (position == RecyclerView.NO_POSITION) {
        return false;
      }

      listener.onShelfLongClicked(shelfItem, v);
      return true;
    });
  }

  @Override
  public int getItemCount() {
    return libraryList.size();
  }

  public void setLibraryList(List<ShelfItem> libraryList) {
    this.libraryList = libraryList;
  }

  public ShelfItem getLibraryItem(int position) {
    return libraryList.get(position);
  }

  /**
   * This method fetches the number of items selected in the recyclerView.
   *
   * @return the selected recyclerView items
   */
  public List<LibraryItem> getSelectedLibraryItems() {
    List<LibraryItem> selectedItems = new ArrayList<>();

    for (int i = 0; i < parent.getChildCount(); i++) {
      if (parent.getChildAt(i).isSelected()) {
        selectedItems.add(libraryList.get(i));
      }
    }

    return selectedItems;
  }

  public interface LibraryListener {
    void onShelfClicked(int position);

    void onShelfLongClicked(ShelfItem shelfItem, View view);
  }


  public static class LibraryViewHolder extends RecyclerView.ViewHolder {

    private final TextView name;
    private final ImageView libraryIcon;
    private final TextView textBookCount;
    private final TextView textNoteCount;

    /**
     * The LibraryViewHolder describes a shelf item view and metadata about its place
     * within the RecyclerView of the LibraryFragment.
     */
    public LibraryViewHolder(@NonNull View itemView) {
      super(itemView);

      name = itemView.findViewById(R.id.item_name);
      libraryIcon = itemView.findViewById(R.id.library_icon);
      textBookCount = itemView.findViewById(R.id.text_book);
      textNoteCount = itemView.findViewById(R.id.note_count);
    }

    public TextView getName() {
      return name;
    }

    public ImageView getLibraryIcon() {
      return libraryIcon;
    }

    public TextView getTextBookCount() {
      return textBookCount;
    }

    public TextView getTextNoteCount() {
      return textNoteCount;
    }
  }

}
