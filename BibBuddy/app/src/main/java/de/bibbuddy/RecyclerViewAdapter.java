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
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import org.jsoup.Jsoup;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

  private final MainActivity activity;
  private final List<Note> data;
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
  public RecyclerViewAdapter(List<Note> data, MainActivity activity) {
    this.data = data;
    this.activity = activity;
    noteModel = new NoteModel(activity.getBaseContext());
    data.sort(new Comparator<Note>() {
      @Override
      public int compare(Note o1, Note o2) {
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
    setupCardView(holder, position);
    holder.itemView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        TextNoteEditorFragment nextFrag = new TextNoteEditorFragment();
        Bundle args = new Bundle();
        args.putLong("noteId", id);
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
    String text = data.get(position).getText();
    text = Jsoup.parse(text).text();
    if (text.contains("\n")) {
      text = text.substring(0, text.indexOf("\n")) + "...";
    }
    if (text.length() > 40) {
      text = text.substring(0, 35) + " ...";
    }
    holder.title.setText(text);
    String dateString = getDate(data.get(position).getModDate());
    holder.modDate.setText(dateString);
    holder.type.setImageDrawable(
        ContextCompat.getDrawable(activity.getApplicationContext(), R.drawable.document));
  }

  private String getDate(Long date) {
    Date d = new Date(date);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    String string = simpleDateFormat.format(d);
    String day = string.substring(8, 10);
    String month = string.substring(5, 7);
    String year = string.substring(0, 4);
    String time = string.substring(11, 16);

    string = day + "." + month + "." + year + " " + time + " Uhr";

    return string;
  }

  @Override
  public int getItemCount() {
    return data.size();
  }

  public List<Note> getData() {
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
    System.out.println(anyItemSelected());
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
    private final TextView title;
    private final ImageView type;

    /**
     * Custom ViewHolder constructor to setup its basic view.
     *
     * @param itemView View of the RecyclerView-item.
     */
    public MyViewHolder(View itemView) {
      super(itemView);
      title = itemView.findViewById(R.id.noteTitle);
      modDate = itemView.findViewById(R.id.noteModDate);
      type = itemView.findViewById(R.id.noteType);
    }

  }

}
