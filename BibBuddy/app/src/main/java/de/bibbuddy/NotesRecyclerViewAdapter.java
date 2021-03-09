package de.bibbuddy;

import android.app.AlertDialog;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import androidx.recyclerview.widget.RecyclerView;
import app.minimize.com.seek_bar_compat.SeekBarCompat;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
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
public class NotesRecyclerViewAdapter
    extends RecyclerView.Adapter<NotesRecyclerViewAdapter.NotesViewHolder> {

  private final MainActivity activity;
  private final List<NoteItem> data;
  private final ArrayList<MediaPlayer> mediaPlayers;
  private final ArrayList<ImageButton> playButtons;
  private final ArrayList<ImageButton> stopButtons;
  private final ArrayList<SeekBarListener> seekBarListeners;
  private ViewGroup parent;
  private int mediaPlayerPosition;
  private boolean paused;

  /**
   * Adapter constructor to connect a NoteList with the activity.
   *
   * @param data     List of notes as data content for the adapter
   * @param activity Base activity
   */
  public NotesRecyclerViewAdapter(List<NoteItem> data, MainActivity activity) {
    this.data = data;
    this.activity = activity;
    this.seekBarListeners = new ArrayList<>();
    this.mediaPlayers = new ArrayList<>();
    this.playButtons = new ArrayList<>();
    this.stopButtons = new ArrayList<>();
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
  public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    this.parent = parent;
    return new NotesViewHolder(
        LayoutInflater.from(activity)
            .inflate(R.layout.list_view_item_note, parent, false));
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

    holder.itemView.setOnLongClickListener(v -> {
      v.setSelected(!v.isSelected());
      return true;
    });

    setupAudioElements(holder, noteItem);
  }

  private void setupAudioElements(NotesViewHolder holder, NoteItem noteItem) {
    if (noteItem.getImage() == R.drawable.microphone) {

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
      TextView playedTime = holder.getPlayedTime();
      TextView totalTime = holder.getTotalTime();

      SeekBarListener seekBarListener =
          new SeekBarListener(activity, mediaPlayer, progressBar, playedTime);
      seekBarListeners.add(seekBarListener);

      setTotalTime(noteItem, totalTime);

      progressBar.setOnSeekBarChangeListener(seekBarListener);

      setupMediaPlayerListeners(mediaPlayer, playButton, stopButton, noteItem, seekBarListener);
    }
  }

  private void setTotalTime(NoteItem noteItem, TextView totalTime) {
    File file = createAudioFile(noteItem);
    Uri uri = Uri.parse(file.getPath());
    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    mmr.setDataSource(activity.getApplicationContext(), uri);
    String durationStr = mmr.extractMetadata(MediaMetadataRetriever.METADATA_KEY_DURATION);
    int millSeconds = Integer.parseInt(durationStr);
    String time = new SimpleDateFormat("mm:ss", Locale.getDefault())
        .format(new Date(millSeconds));
    totalTime.setText(time);
  }

  private void setupMediaPlayerListeners(MediaPlayer mediaPlayer, ImageButton playButton,
                                         ImageButton stopButton,
                                         NoteItem noteItem, SeekBarListener seekBarListener) {
    mediaPlayer.setOnCompletionListener(mp -> {
      mp.reset();
      paused = false;
      setSelection(playButton, false, R.drawable.icon_play);
      stopButton.setClickable(false);
      seekBarListener.reset();
    });

    playButton.setOnClickListener(v -> {
      ImageButton button = (ImageButton) v;
      if (v.isSelected()) {
        setSelection(button, false, R.drawable.icon_play);
        mediaPlayer.pause();
        if (paused) {
          mediaPlayerPosition = mediaPlayer.getCurrentPosition();
          mediaPlayer.seekTo(mediaPlayerPosition);
          mediaPlayer.start();
          setSelection(playButton, true, R.drawable.icon_pause);
        }
        paused = !paused;
      } else {
        resetPlayers();
        stopButton.setClickable(true);
        playAudio(mediaPlayer, button, noteItem, seekBarListener);
      }
    });

    stopButton.setOnClickListener(v -> {
      seekBarListener.reset();
      mediaPlayer.stop();
      paused = false;
      resetPlayers();
    });

  }

  private void resetPlayers() {
    for (int i = 0; i < mediaPlayers.size(); i++) {
      mediaPlayers.get(i).reset();
      setSelection(playButtons.get(i), false, R.drawable.icon_play);
      stopButtons.get(i).setClickable(false);
      seekBarListeners.get(i).reset();
    }
    paused = false;
  }

  private void setSelection(ImageButton button, boolean bool, int drawable) {
    button.setSelected(bool);
    button.setImageResource(drawable);
  }

  private void playAudio(MediaPlayer mediaPlayer, ImageButton button,
                         NoteItem noteItem, SeekBarListener seekBarListener) {
    File tempAudio = createAudioFile(noteItem);
    try {
      mediaPlayer.reset();
      mediaPlayer.setDataSource(tempAudio.getPath());
      mediaPlayer.prepareAsync();
      mediaPlayer.setOnPreparedListener(mp -> {
        setSelection(button, true, R.drawable.icon_pause);
        mp.start();
        seekBarListener.barUpdate();
      });
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private File createAudioFile(NoteItem noteItem) {
    byte[] bytes = NotesFragment.getNoteMedia(noteItem.getId());
    File tempAudio = null;
    try {
      tempAudio = File.createTempFile(String.valueOf(R.string.temporary_audio_file), String.valueOf(
          R.string.audio_file_suffix), activity.getCacheDir());
      tempAudio.deleteOnExit();
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Path path = Paths.get(tempAudio.getPath());
        Files.write(path, bytes);
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
    return tempAudio;
  }

  private Bundle createNoteBundle(NoteItem item) {
    Bundle bundle = new Bundle();
    bundle.putLong(LibraryKeys.BOOK_ID, item.getBookId());
    bundle.putLong(LibraryKeys.NOTE_ID, item.getId());

    return bundle;
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
   * This method shows on using delete-options an alertDialog, that deleting notes is not revertable
   * and deletes them on using the positive button or cancels the action on using the negative one.
   */
  public void handleDeleteNote() {
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
    });

    alertDeleteNote.setPositiveButton(R.string.delete, (dialog, which) -> {
      Toast.makeText(activity, R.string.deleted_notes,
          Toast.LENGTH_SHORT).show();
      int itemNumber = parent.getChildCount();
      ArrayList<Integer> idCounter = new ArrayList<>();
      for (int i = 0; i < itemNumber; i++) {
        if (parent.getChildAt(i).isSelected()) {
          idCounter.add(i);
        }
      }
      removeBackendDataAndViewItems(idCounter);
    });
    alertDeleteNote.show();
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
      modDate = itemView.findViewById(R.id.noteModDate);
      name = itemView.findViewById(R.id.noteName);
      type = itemView.findViewById(R.id.noteType);
      voiceNoteLayout = itemView.findViewById(R.id.voice_note_layout);
      play = itemView.findViewById(R.id.play_note);
      stop = itemView.findViewById(R.id.stop_note);
      progressBar = itemView.findViewById(R.id.progressBar);
      playedTime = itemView.findViewById(R.id.playedTime);
      totalTime = itemView.findViewById(R.id.totalTime);
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
