package de.bibbuddy;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.RecyclerView;
import app.minimize.com.seek_bar_compat.SeekBarCompat;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Locale;

/**
 * NoteRecyclerViewAdapter provides a binding from the noteList to the view
 * that is displayed within the RecyclerView of the NotesFragment.
 *
 * @author Sabrina Freisleben
 */
public class NoteRecyclerViewAdapter
    extends RecyclerView.Adapter<NoteRecyclerViewAdapter.NotesViewHolder> {

  private final MainActivity activity;
  private final NoteModel noteModel;
  private final ArrayList<MediaPlayer> mediaPlayers;
  private final ArrayList<ImageButton> playButtons;
  private final ArrayList<ImageButton> stopButtons;
  private final ArrayList<ProgressBar> progressBars;
  private final ArrayList<SeekBarListener> seekBarListeners;
  private List<NoteItem> noteList;
  private ViewGroup parent;
  private int mediaPlayerPosition;
  private boolean paused;
  private long bookId;


  /**
   * Adapter constructor to connect a NoteList with the activity.
   *
   * @param activity Base activity
   * @param noteList List of notes as noteList content for the adapter
   */
  public NoteRecyclerViewAdapter(MainActivity activity, List<NoteItem> noteList,
                                 NoteModel noteModel) {
    this.activity = activity;
    this.noteList = noteList;
    this.noteModel = noteModel;

    this.mediaPlayers = new ArrayList<>();
    this.playButtons = new ArrayList<>();
    this.stopButtons = new ArrayList<>();
    this.progressBars = new ArrayList<>();
    this.seekBarListeners = new ArrayList<>();

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
    NoteItem noteItem = noteList.get(position);
    int image = noteItem.getImage();

    setupBasicCardView(holder, position);
    if (noteItem.getImage() == R.drawable.microphone) {
      setupAudioElements(holder, noteItem);
    }

    holder.itemView.setOnClickListener(v -> {
      if (getSelectedNoteItems().size() > 0) {
        v.setSelected(!v.isSelected());
      } else {
        if (image == R.drawable.document) {
          TextNoteEditorFragment nextFrag = new TextNoteEditorFragment();

          nextFrag.setArguments(createNoteBundle(noteItem));

          activity.getSupportFragmentManager().beginTransaction()
              .replace(R.id.fragment_container_view, nextFrag,
                  LibraryKeys.FRAGMENT_TEXT_NOTE_EDITOR)
              .addToBackStack(null)
              .commit();
        }
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

  private Bundle createNoteBundle(NoteItem item) {
    Bundle bundle = new Bundle();
    bundle.putLong(LibraryKeys.BOOK_ID, item.getBookId());
    bundle.putLong(LibraryKeys.NOTE_ID, item.getId());

    return bundle;
  }

  private void setupBasicCardView(NotesViewHolder holder, int position) {
    NoteItem noteItem = noteList.get(position);
    holder.getModDateView().setText(noteItem.getModDateStr());
    holder.getNameView().setText(noteItem.getName());
    holder.getTypeView().setImageDrawable(ContextCompat.getDrawable(activity.getBaseContext(),
        noteItem.getImage()));
  }

  private void setupAudioElements(NotesViewHolder holder, NoteItem noteItem) {
    ImageButton playButton = holder.getPlayNoteButton();
    playButtons.add(playButton);
    playButton.setVisibility(View.VISIBLE);

    ImageButton stopButton = holder.getStopNoteButton();
    stopButtons.add(stopButton);
    stopButton.setVisibility(View.VISIBLE);

    MediaPlayer mediaPlayer = new MediaPlayer();
    mediaPlayers.add(mediaPlayer);

    RelativeLayout voiceNoteLayout = holder.getVoiceNoteLayout();
    voiceNoteLayout.setVisibility(View.VISIBLE);

    SeekBarCompat progressBar = holder.getProgressBar();
    progressBars.add(progressBar);
    progressBar.setEnabled(false);

    TextView playedTime = holder.getPlayedTime();
    TextView totalTime = holder.getTotalTime();

    SeekBarListener seekBarListener =
        new SeekBarListener(activity, mediaPlayer, progressBar, playedTime);
    seekBarListeners.add(seekBarListener);

    setTotalTimes(noteItem, totalTime);

    progressBar.setOnSeekBarChangeListener(seekBarListener);

    setupMediaPlayerListeners(mediaPlayer, progressBar, playButton, stopButton, noteItem,
        seekBarListener);
  }

  private void setTotalTimes(NoteItem noteItem, TextView totalTime) {
    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    mmr.setDataSource(noteModel.getNoteFilePath(noteItem.getId()));
    String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    int millis = Integer.parseInt(durationStr);
    String time = new SimpleDateFormat("mm:ss", Locale.getDefault())
        .format(new Date(millis));
    totalTime.setText(time);
  }

  private void setupMediaPlayerListeners(MediaPlayer mediaPlayer, SeekBarCompat progressBar,
                                         ImageButton playButton,
                                         ImageButton stopButton,
                                         NoteItem noteItem, SeekBarListener seekBarListener) {
    mediaPlayer.setOnCompletionListener(mp -> {
      mp.reset();
      seekBarListener.reset();
      stopButton.setClickable(false);
      setSelection(playButton, false, R.drawable.icon_play);
      paused = false;
    });

    playButton.setOnClickListener(v -> {
      ImageButton button = (ImageButton) v;
      if (v.isSelected()) {
        if (paused) {
          mediaPlayerPosition = mediaPlayer.getCurrentPosition();
          mediaPlayer.seekTo(mediaPlayerPosition);
          mediaPlayer.start();
          button.setImageResource(R.drawable.icon_pause);
        } else {
          mediaPlayer.pause();
          button.setImageResource(R.drawable.icon_play);
        }
        paused = !paused;
      } else {
        resetPlayers();
        startAudio(mediaPlayer, button, noteItem, seekBarListener);
        stopButton.setClickable(true);
      }
      progressBar.setEnabled(true);
    });

    stopButton.setOnClickListener(v -> {
      resetPlayers();
      paused = false;
    });
  }

  private void resetPlayers() {
    for (int i = 0; i < mediaPlayers.size(); i++) {
      mediaPlayers.get(i).reset();
      seekBarListeners.get(i).reset();
      progressBars.get(i).setEnabled(false);
      stopButtons.get(i).setClickable(false);
      setSelection(playButtons.get(i), false, R.drawable.icon_play);
    }
    paused = false;
  }

  private void setSelection(ImageButton button, boolean bool, int drawable) {
    button.setSelected(bool);
    button.setImageResource(drawable);
  }

  private void startAudio(MediaPlayer mediaPlayer, ImageButton button,
                          NoteItem noteItem, SeekBarListener seekBarListener) {
    try {
      mediaPlayer.setDataSource(noteModel.getNoteFilePath(noteItem.getId()));
    } catch (IOException e) {
      e.printStackTrace();
    }
    mediaPlayer.prepareAsync();
    mediaPlayer.setOnPreparedListener(mp -> {
      setSelection(button, true, R.drawable.icon_pause);
      mp.start();
      seekBarListener.updateProgress();
    });
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
   * This method fetches the items selected in the recyclerView.
   *
   * @return returns the selected recyclerView items.
   */
  public List<NoteItem> getSelectedNoteItems() {
    List<NoteItem> selectedNotes = new ArrayList<>();

    if (parent != null) {
      for (int i = 0; i < parent.getChildCount(); i++) {
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
    private final ImageButton play;
    private final ImageButton stop;
    private final RelativeLayout voiceNoteLayout;
    private final SeekBarCompat progressBar;
    private final TextView playedTime;
    private final TextView totalTime;

    /**
     * Custom ViewHolder constructor to setup its basic view.
     *
     * @param itemView View of the RecyclerView-item.
     */
    public NotesViewHolder(View itemView) {
      super(itemView);
      modDate = itemView.findViewById(R.id.note_mod_date);
      name = itemView.findViewById(R.id.note_name);
      type = itemView.findViewById(R.id.note_type);
      voiceNoteLayout = itemView.findViewById(R.id.voice_note_layout);
      play = itemView.findViewById(R.id.play_note);
      stop = itemView.findViewById(R.id.stop_note);
      progressBar = itemView.findViewById(R.id.seekBar);
      playedTime = itemView.findViewById(R.id.played_time);
      totalTime = itemView.findViewById(R.id.total_time);
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

    public RelativeLayout getVoiceNoteLayout() {
      return voiceNoteLayout;
    }

    public SeekBarCompat getProgressBar() {
      return progressBar;
    }

    public TextView getPlayedTime() {
      return playedTime;
    }

    public TextView getTotalTime() {
      return totalTime;
    }

  }

}
