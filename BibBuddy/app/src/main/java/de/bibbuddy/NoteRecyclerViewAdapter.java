package de.bibbuddy;

import android.content.Context;
import android.graphics.drawable.Drawable;
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
import java.util.Comparator;
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
  private final NoteModel noteModel;
  private Drawable background;
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
    noteModel = new NoteModel(activity.getBaseContext());
    data.sort(new Comparator<NoteItem>() {
      @Override
      public int compare(NoteItem o1, NoteItem o2) {
        if (o1.getModDate() == null || o2.getModDate() == null) {
          return 0;
        }
        return o1.getModDate().compareTo(o2.getModDate());
      }
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
    background = itemView.getBackground();
    hiddenDeletePanel = parent.getRootView().findViewById(R.id.hidden_delete_panel);
    panelDelete = hiddenDeletePanel.findViewById(R.id.panel_delete);
    return new MyViewHolder(itemView);
  }

  /**
   * Method to setup the custom ViewHolder components
   * Consider: if text has more than 40 characters, only first 35 characters are displayed as title
   * and adding " ..." as indicator for a longer text
   *
   * @param holder   custom ViewHolder instance
   * @param position adapterPosition of the viewHolder-item
   */
  @Override
  public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
    Long id = data.get(position).getId();
    String text = noteModel.getNoteById(id).getText();
    setupCardView(holder, position);
    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        TextNoteEditorFragment nextFrag = new TextNoteEditorFragment();
        Bundle args = new Bundle();
        args.putLong(LibraryKeys.NOTE_ID, id);
        args.putString(LibraryKeys.NOTE_TEXT, text);
        nextFrag.setArguments(args);
        activity.getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container_view, nextFrag, "fragment_text_note_editor")
            .addToBackStack(null)
            .commit();
      }
    });
    holder.itemView.setOnLongClickListener(new View.OnLongClickListener() {

      @Override
      public boolean onLongClick(View v) {

        // Below line is just like a safety check, because sometimes holder could be null,
        // in that case, getAdapterPosition() will return RecyclerView.NO_POSITION
        if (position == RecyclerView.NO_POSITION) {
          return false;
        }
        if (!v.isSelected()) {
          v.setSelected(true);
          v.setBackgroundColor(activity.getColor(R.color.flirt_light));
        } else {
          v.setSelected(false);
          v.setBackground(background);
        }
        slideUpDown();
        return true;
      }
    });
    setupDeleteListener();
  }

  private void setupDeleteListener() {
    panelDelete.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        int itemNumber = parent.getChildCount();
        ArrayList<Integer> idCounter = new ArrayList<>();
        for (int i = 0; i < itemNumber; i++) {
          if (parent.getChildAt(i).isSelected()) {
            idCounter.add(i);
          }
        }
        removeBackendDataAndViewItems(idCounter);
        hidePanel();
      }
    });
  }

  private void removeBackendDataAndViewItems(ArrayList<Integer> idCounter) {
    for (int i = 0; i < idCounter.size(); i++) {
      noteModel.deleteNote(data.get(i).getId());
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
    // Get element from your dataset at this position and replace the contents of the view
    // with that element
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
  public void slideUpDown() {
    if (anyItemSelected() && !isPanelShown()) {
      // Show the panel
      Animation bottomUp = AnimationUtils.loadAnimation(activity.getBaseContext(),
          R.anim.bottom_up);

      hiddenDeletePanel.startAnimation(bottomUp);
      hiddenDeletePanel.setVisibility(View.VISIBLE);
    } else if (!anyItemSelected() && isPanelShown()) {
      hidePanel();
    }
  }

  private void hidePanel() {
    // Hide the Panel
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
