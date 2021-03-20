package de.bibbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * NoteRecyclerViewAdapter provides a binding from the noteList to the view
 * that is displayed within the RecyclerView of the NotesFragment.
 *
 * @author Sabrina Freisleben
 */
public class NoteRecyclerViewAdapter
    extends RecyclerView.Adapter<NoteRecyclerViewAdapter.NotesViewHolder> {

  private final MainActivity activity;
  private List<NoteItem> noteList;
  private ViewGroup parent;

  /**
   * Adapter constructor to connect a NoteList with the activity.
   *
   * @param activity Base activity
   * @param noteList List of notes as noteList content for the adapter
   */
  public NoteRecyclerViewAdapter(MainActivity activity, List<NoteItem> noteList) {
    this.activity = activity;
    this.noteList = noteList;

    noteList.sort((o1, o2) -> {
      if (o1.getModDate() == null || o2.getModDate() == null) {
        return 0;
      }
      return o1.getModDate().compareTo(o2.getModDate());
    });
    Collections.reverse(noteList);
  }

  @NonNull
  @Override
  public NotesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    this.parent = parent;
    Context context = parent.getContext();

    View itemView =
        LayoutInflater.from(context).inflate(R.layout.list_view_item_note, parent, false);

    return new NotesViewHolder(itemView);
  }


  /**
   * Method to setup the custom ViewHolder components for notes.
   *
   * @param holder   custom ViewHolder instance
   * @param position adapterPosition of the viewHolder-item
   */
  @Override
  public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
    Long id = noteList.get(position).getId();
    String text = noteList.get(position).getNoteText();
    setupCardView(holder, position);

    holder.itemView.setOnClickListener(v -> {
      if (getSelectedNoteItems().size() > 0) {
        v.setSelected(!v.isSelected());
      } else {
        TextNoteEditorFragment nextFrag = new TextNoteEditorFragment();
        Bundle args = new Bundle();
        args.putLong(LibraryKeys.NOTE_ID, id);
        args.putString(LibraryKeys.NOTE_TEXT, text);
        nextFrag.setArguments(args);
        activity.getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container_view, nextFrag, LibraryKeys.FRAGMENT_TEXT_NOTE_EDITOR)
            .addToBackStack(null)
            .commit();
      }
    });

    holder.itemView.setOnLongClickListener(v -> {
      if (position == RecyclerView.NO_POSITION) {
        return false;
      }
      v.setSelected(!v.isSelected());

      return true;
    });

  }

  private void setupCardView(NotesViewHolder holder, int position) {
    NoteItem noteItem = noteList.get(position);
    holder.getModDateView().setText(noteItem.getModDateStr());
    holder.getNameView().setText(noteItem.getName());
    holder.getTypeView().setImageDrawable(ContextCompat.getDrawable(activity.getBaseContext(),
        noteItem.getImage()));
  }

  @Override
  public int getItemCount() {
    return noteList.size();
  }

  public List<NoteItem> getNoteList() {
    return noteList;
  }

  public void setNoteList(List<NoteItem> noteList) {
    this.noteList = noteList;
    notifyDataSetChanged();
  }

  public void removeItem(int position) {
    noteList.remove(position);
    notifyItemRemoved(position);
  }

  /**
   * This method fetches the number of items selected in the recyclerView.
   *
   * @return returns the number of selected recyclerView items.
   */
  public List<NoteItem> getSelectedNoteItems() {
    List<NoteItem> selectedNotes = new ArrayList<>();
    if (parent != null) {
      int itemNumber = parent.getChildCount();
      for (int i = 0; i < itemNumber; i++) {
        if (parent.getChildAt(i).isSelected()) {
          selectedNotes.add(noteList.get(i));
        }
      }
    }
    return selectedNotes;
  }

  /**
   * Custom ViewHolder to fit the RecyclerView's cardViews.
   */
  public static class NotesViewHolder extends RecyclerView.ViewHolder {

    public final TextView modDate;
    private final TextView name;
    private final ImageView type;

    /**
     * Custom ViewHolder constructor to setup its basic view.
     *
     * @param itemView View of the RecyclerView-item.
     */
    public NotesViewHolder(View itemView) {
      super(itemView);
      modDate = itemView.findViewById(R.id.noteModDate);
      name = itemView.findViewById(R.id.noteName);
      type = itemView.findViewById(R.id.noteType);
    }

    public TextView getModDateView() {
      return modDate;
    }

    public TextView getNameView() {
      return name;
    }

    public ImageView getTypeView() {
      return type;
    }

  }

}
