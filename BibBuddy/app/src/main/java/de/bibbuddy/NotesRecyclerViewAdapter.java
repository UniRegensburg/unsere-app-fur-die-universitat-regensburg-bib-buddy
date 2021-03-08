package de.bibbuddy;

import android.app.AlertDialog;
import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
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
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import com.chibde.visualizer.LineBarVisualizer;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * NoteRecyclerViewAdapter provides a binding from the noteList to the view
 * that is displayed within the RecyclerView of the NotesFragment.
 *
 * @author Sabrina Freisleben
 */
public class NotesRecyclerViewAdapter
    extends RecyclerView.Adapter<NotesRecyclerViewAdapter.NotesViewHolder> {

  private final MainActivity activity;
  private final List<NoteItem> data;
  private final ArrayList<MediaPlayer> mediaPlayers;
  private final ArrayList<ImageButton> playButtons;
  private final ArrayList<LineBarVisualizer> visualizers;
  private ViewGroup parent;
  private RelativeLayout hiddenDeletePanel;
  private boolean paused;
  private int mediaPlayerPosition;

  /**
   * Adapter constructor to connect a NoteList with the activity.
   *
   * @param data     List of notes as data content for the adapter
   * @param activity Base activity
   */
  public NotesRecyclerViewAdapter(List<NoteItem> data, MainActivity activity) {
    this.data = data;
    this.activity = activity;
    this.mediaPlayers = new ArrayList<>();
    this.playButtons = new ArrayList<>();
    this.visualizers = new ArrayList<>();
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
  public NotesViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
    View itemView;
    this.parent = parent;
    Context context = parent.getContext();
    itemView =
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
    setupCardView(holder, position);
    NoteItem noteItem = data.get(position);
    holder.itemView.setOnClickListener(v -> {
      if (noteItem.getImage() == R.drawable.document) {
        Fragment nextFrag = new TextNoteEditorFragment();
        nextFrag.setArguments(createNoteBundle(noteItem));
        activity.getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container_view, nextFrag, LibraryKeys.FRAGMENT_TEXT_NOTE_EDITOR)
            .addToBackStack(null)
            .commit();
      }
      /*} else {
        nextFrag = new ImageNoteEditorFragment();
       */
    });

    setupMediaButtons(holder, noteItem);

    holder.itemView.setOnLongClickListener(v -> {
      v.setSelected(!v.isSelected());
      slideUpOrDown();
      return true;
    });

    hiddenDeletePanel = parent.getRootView().findViewById(R.id.hidden_delete_panel);
    setupDeleteListener();
  }

  private void setupMediaButtons(NotesViewHolder holder, NoteItem noteItem) {
    if (noteItem.getImage() == R.drawable.microphone) {
      ImageButton playButton = holder.getPlayNoteButton();
      playButtons.add(playButton);
      playButton.setVisibility(View.VISIBLE);
      ImageButton stopButton = holder.getStopNoteButton();
      stopButton.setVisibility(View.VISIBLE);

      MediaPlayer mediaPlayer = new MediaPlayer();
      mediaPlayers.add(mediaPlayer);
      LineBarVisualizer visualizer = holder.getVisualizer();
      visualizers.add(visualizer);
      setupMediaPlayerListeners(mediaPlayer, playButton, stopButton, visualizer, noteItem);
    }
  }

  private void setupMediaPlayerListeners(MediaPlayer mediaPlayer, ImageButton playButton,
                                         ImageButton stopButton, LineBarVisualizer visualizer,
                                         NoteItem noteItem) {
    visualizer.setColor(ContextCompat.getColor(activity, R.color.design_default_color_secondary));
    visualizer.setDensity(30);
    visualizer.setPlayer(mediaPlayer.getAudioSessionId());
    mediaPlayer.setOnCompletionListener(mp -> {
      mp.reset();
      paused = false;
      setSelection(playButton, false, R.drawable.icon_play);
      visualizer.setVisibility(View.INVISIBLE);
    });

    playButton.setOnClickListener(v -> {
      if (v.isSelected()) {
        ImageButton button = (ImageButton) v;
        button.setImageResource(R.drawable.icon_play);
        mediaPlayer.pause();
        if (paused) {
          mediaPlayerPosition = mediaPlayer.getCurrentPosition();
          mediaPlayer.seekTo(mediaPlayerPosition);
          mediaPlayer.start();
        }
        paused = !paused;
      } else {
        for (int i = 0; i < mediaPlayers.size(); i++) {
          mediaPlayers.get(i).stop();
          setSelection(playButtons.get(i), false, R.drawable.icon_play);
          visualizers.get(i).setVisibility(View.INVISIBLE);
        }
        byte[] bytes = NotesFragment.getNoteMedia(noteItem.getId());
        playAudio(mediaPlayer, visualizer, bytes);
        setSelection((ImageButton) v, true, R.drawable.icon_pause);
        paused = false;
      }
    });

    stopButton.setOnClickListener(v -> {
      mediaPlayer.stop();
      visualizer.setVisibility(View.INVISIBLE);
      paused = false;
    });
  }

  private void useVisualizer(MediaPlayer mediaPlayer, LineBarVisualizer visualizer) {
    int audioSessionId = mediaPlayer.getAudioSessionId();
    if (audioSessionId != -1) {
      visualizer.setVisibility(View.VISIBLE);
      visualizer.setPlayer(mediaPlayer.getAudioSessionId());
    }
  }

  private void setSelection(ImageButton button, boolean bool, int drawable) {
    button.setSelected(bool);
    button.setImageResource(drawable);
  }

  private void playAudio(MediaPlayer mediaPlayer, LineBarVisualizer visualizer, byte[] bytes) {
    try {
      File tempAudio =
          File.createTempFile(String.valueOf(R.string.temporary_audio_file), String.valueOf(
              R.string.audio_file_suffix), activity.getCacheDir());
      tempAudio.deleteOnExit();
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Path path = Paths.get(tempAudio.getPath());
        Files.write(path, bytes);
      }
      mediaPlayer.reset();
      mediaPlayer.setDataSource(tempAudio.getPath());
      mediaPlayer.prepareAsync();
      mediaPlayer.setOnPreparedListener(mp -> {
        useVisualizer(mp, visualizer);
        mp.start();
      });
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private Bundle createNoteBundle(NoteItem item) {
    Bundle bundle = new Bundle();
    bundle.putLong(LibraryKeys.BOOK_ID, item.getBookId());
    bundle.putLong(LibraryKeys.NOTE_ID, item.getId());

    return bundle;
  }

  private void setupDeleteListener() {
    hiddenDeletePanel.setOnClickListener(v -> {
      handleDeleteNote();
    });
  }

  private void hidePanel() {
    Animation bottomDown = AnimationUtils.loadAnimation(activity,
        R.anim.bottom_down);
    hiddenDeletePanel.startAnimation(bottomDown);
    hiddenDeletePanel.setVisibility(View.GONE);
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

  private void setupCardView(NotesViewHolder holder, int position) {
    NoteItem noteItem = data.get(position);
    holder.getModDateView().setText(noteItem.getModDate());
    holder.getNameView().setText(noteItem.getName());
    holder.getTypeView().setImageDrawable(ContextCompat.getDrawable(activity,
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
      Animation bottomUp = AnimationUtils.loadAnimation(activity,
          R.anim.bottom_up);
      hiddenDeletePanel.startAnimation(bottomUp);
      hiddenDeletePanel.setVisibility(View.VISIBLE);
    } else if (!anyItemSelected() && isPanelShown()) {
      hidePanel();
    }
  }

  private void handleDeleteNote() {
    AlertDialog.Builder alertDeleteNote = new AlertDialog.Builder(activity);

    alertDeleteNote.setCancelable(false);
    alertDeleteNote.setTitle(R.string.delete_note);
    alertDeleteNote.setMessage(R.string.delete_note_message);

    alertDeleteNote.setNegativeButton(R.string.back, (dialog, which) -> {
      RecyclerView recyclerView = activity.findViewById(R.id.notesRecyclerView);
      for (int i = 0; i < data.size(); i++) {
        recyclerView.getChildAt(i).setSelected(false);
      }
      notifyDataSetChanged();
      hidePanel();
    });

    alertDeleteNote.setPositiveButton(R.string.delete, (dialog, which) -> {
      Toast.makeText(activity, String.valueOf(R.string.deleted_notes),
          Toast.LENGTH_SHORT).show();
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
    alertDeleteNote.show();
  }

  private boolean anyItemSelected() {
    RecyclerView recyclerView = activity.findViewById(R.id.notesRecyclerView);
    int itemNumber = recyclerView.getChildCount();
    for (int i = 0; i < itemNumber; i++) {
      if (recyclerView.getChildAt(i).isSelected()) {
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
  public static class NotesViewHolder extends RecyclerView.ViewHolder {

    public final TextView modDate;
    private final TextView name;
    private final ImageView type;
    private final ImageButton play;
    private final ImageButton stop;
    private final LineBarVisualizer visualizer;

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
      play = itemView.findViewById(R.id.play_note);
      stop = itemView.findViewById(R.id.stop_note);
      visualizer = itemView.findViewById(R.id.notesVisualizer);
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

    public ImageButton getPlayNoteButton() {
      return play;
    }

    public ImageButton getStopNoteButton() {
      return stop;
    }

    public LineBarVisualizer getVisualizer() {
      return visualizer;
    }
  }

}
