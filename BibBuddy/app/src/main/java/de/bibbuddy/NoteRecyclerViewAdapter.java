package de.bibbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
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
    extends RecyclerView.Adapter<NoteRecyclerViewAdapter.MyViewHolder> {

  private final MainActivity activity;
  private final List<NoteItem> data;
  private ImageButton panelDelete;
  private RelativeLayout hiddenDeletePanel;
  private ViewGroup parent;

  /**
   * Adapter constructor to connect a NoteList with the activity.
   *
   * @param data     List of notes as data content for the adapter
   * @param activity Base activity
   */
  public NoteRecyclerViewAdapter(List<NoteItem> data, MainActivity activity) {
    this.data = data;
    this.activity = activity;
    data.sort((o1, o2) -> {
      if (o1.getModDate() == null || o2.getModDate() == null) {
        return 0;
      }
      return o1.getModDate().compareTo(o2.getModDate());
    });
    Collections.reverse(data);
  }

  @NonNull
  @Override
  public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView;
    this.parent = parent;
    Context context = parent.getContext();
    itemView =
        LayoutInflater.from(context).inflate(R.layout.list_view_item_note, parent, false);
    hiddenDeletePanel = parent.getRootView().findViewById(R.id.hidden_delete_panel);
    panelDelete = hiddenDeletePanel.findViewById(R.id.panel_delete);
    return new MyViewHolder(itemView);
  }

  /**
   * Method to setup the custom ViewHolder components for notes.
   *
   * @param holder   custom ViewHolder instance
   * @param position adapterPosition of the viewHolder-item
   */
  @Override
  public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
    Long id = data.get(position).getId();
    String text = data.get(position).getNoteText();
    setupCardView(holder, position);
    holder.itemView.setOnClickListener(v -> {
      TextNoteEditorFragment nextFrag = new TextNoteEditorFragment();
      Bundle args = new Bundle();
      args.putLong(LibraryKeys.NOTE_ID, id);
      args.putString(LibraryKeys.NOTE_TEXT, text);
      nextFrag.setArguments(args);
      activity.getSupportFragmentManager().beginTransaction()
          .replace(R.id.fragment_container_view, nextFrag, "fragment_text_note_editor")
          .addToBackStack(null)
          .commit();
    });
    holder.itemView.setOnLongClickListener(v -> {
      if (position == RecyclerView.NO_POSITION) {
        return false;
      }
      v.setSelected(!v.isSelected());
      slideUpOrDown();
      return true;
    });
    setupDeleteListener();
  }

  private void setupDeleteListener() {
    panelDelete.setOnClickListener(v -> {
      int itemNumber = parent.getChildCount();
      ArrayList<Integer> idCounter = new ArrayList<>();
      for (int i = 0; i < itemNumber; i++) {
        if (parent.getChildAt(i).isSelected()) {
          idCounter.add(i);
        }
      }
      removeBackendDataAndViewItems(idCounter);
      hidePanel();
    });
  }

  private void removeBackendDataAndViewItems(ArrayList<Integer> idCounter) {
    for (int i = 0; i < idCounter.size(); i++) {
      NotesFragment.deleteNote(data.get(i).getId());
    }
    int removed = 0;
    for (int i = 0; i < idCounter.size(); i++) {
      if (removed == 0) {
        removeItem(idCounter.get(i));
      } else {
        removeItem(idCounter.get(i) - removed);
      }
      removed++;
    }
  }

  private void setupCardView(MyViewHolder holder, int position) {
    NoteItem noteItem = data.get(position);
    holder.getModDateView().setText(noteItem.getModDate());
    holder.getNameView().setText(noteItem.getName());
    holder.getTypeView().setImageDrawable(ContextCompat.getDrawable(activity.getBaseContext(),
        noteItem.getImage()));
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  public List<NoteItem> getData() {
    return data;
  }

  public void removeItem(int position) {
    data.remove(position);
    notifyItemRemoved(position);
  }

  /**
   * Method to perform an upside-down animation for the deletePanel.
   */
  public void slideUpOrDown() {
    if (anyItemSelected() && !isPanelShown()) {
      Animation bottomUp = AnimationUtils.loadAnimation(activity.getBaseContext(),
          R.anim.bottom_up);
      hiddenDeletePanel.startAnimation(bottomUp);
      hiddenDeletePanel.setVisibility(View.VISIBLE);
    } else if (!anyItemSelected() && isPanelShown()) {
      hidePanel();
    }
  }

  private void hidePanel() {
    Animation bottomDown = AnimationUtils.loadAnimation(activity.getBaseContext(),
        R.anim.bottom_down);
    hiddenDeletePanel.startAnimation(bottomDown);
    hiddenDeletePanel.setVisibility(View.GONE);
  }

  private boolean anyItemSelected() {
    int itemNumber = parent.getChildCount();
    for (int i = 0; i < itemNumber; i++) {
      if (parent.getChildAt(i).isSelected()) {
        return true;
      }
    }
    return false;
  }

  private boolean isPanelShown() {
    return hiddenDeletePanel.getVisibility() == View.VISIBLE;
  }

  /**
   * Custom ViewHolder to fit the RecyclerView's cardViews.
   */
  public static class MyViewHolder extends RecyclerView.ViewHolder {

    public final TextView modDate;
    private final TextView name;
    private final ImageView type;

    /**
     * Custom ViewHolder constructor to setup its basic view.
     *
     * @param itemView View of the RecyclerView-item.
     */
    public MyViewHolder(View itemView) {
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
