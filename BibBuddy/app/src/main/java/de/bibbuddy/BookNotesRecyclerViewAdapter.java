package de.bibbuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;
import java.util.List;

/**
 * BookNotesRecyclerViewAdapter provides a binding from the noteList to the view
 * that is displayed within the RecyclerView of the BookNotesView.
 *
 * @author Sarah Kurek
 */
public class BookNotesRecyclerViewAdapter extends
    RecyclerView.Adapter<BookNotesRecyclerViewAdapter.BookNotesViewHolder> {

  private final List<NoteItem> noteList;
  private final BookNotesViewListener listener;
  private final Context context;

  /**
   * Constructor for BookNotesRecyclerViewAdapter.
   *
   * @param noteList list of notes
   * @param listener listener for bookNotesView
   * @param context context
   */
  public BookNotesRecyclerViewAdapter(List<NoteItem> noteList, BookNotesViewListener listener,
                                      Context context) {
    this.noteList = noteList;
    this.listener = listener;
    this.context = context;
  }

  @NonNull
  @Override
  public BookNotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new BookNotesRecyclerViewAdapter.BookNotesViewHolder(
        LayoutInflater.from(parent.getContext())
            .inflate(R.layout.list_view_item_book_notes_view, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull BookNotesViewHolder holder, int position) {
    // Get element from your dataset at this position and replace the contents of the view
    // with that element
    NoteItem noteItem = noteList.get(position);

    holder.getTextView().setText(noteItem.getName());
    holder.getImageView().setImageResource(noteItem.getImage());

    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        listener.onItemClicked(position);
      }
    });

    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        if (position == RecyclerView.NO_POSITION) {
          return false;
        }

        listener.onLongItemClicked(position, noteItem, v);
        return true;
      }
    });
  }

  @Override
  public int getItemCount() {
    return noteList.size();
  }

  public interface BookNotesViewListener {
    void onItemClicked(int position); // callback function

    void onLongItemClicked(int position, NoteItem noteItem, View v);
  }

  public static class BookNotesViewHolder extends RecyclerView.ViewHolder {

    private final TextView textView;
    private final ImageView imageView;

    /**
     * Custom ViewHolder constructor to setup basic view.
     *
     * @param itemView current item of the view
     */
    public BookNotesViewHolder(@NonNull View itemView) {
      super(itemView);

      textView = itemView.findViewById(R.id.item_name_book_notes);
      imageView = itemView.findViewById(R.id.note_icon);
      // TODO: Differentiation Text, audio, image
    }

    public TextView getTextView() {
      return textView;
    }

    public ImageView getImageView() {
      return imageView;
    }
  }

}
