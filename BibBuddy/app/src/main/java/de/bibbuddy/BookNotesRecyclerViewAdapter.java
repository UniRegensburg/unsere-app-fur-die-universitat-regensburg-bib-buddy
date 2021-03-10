package de.bibbuddy;

import android.content.Context;
import android.media.MediaMetadataRetriever;
import android.media.MediaPlayer;
import android.net.Uri;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.ProgressBar;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
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
 * BookNotesRecyclerViewAdapter provides a binding from the noteList to the view
 * that is displayed within the RecyclerView of the BookNotesView.
 *
 * @author Sarah Kurek
 */
public class BookNotesRecyclerViewAdapter
    extends RecyclerView.Adapter<BookNotesRecyclerViewAdapter.NotesViewHolder> {

  private final List<NoteItem> noteList;
  private final BookNotesViewListener listener;
  private final Context context;

  private final ArrayList<MediaPlayer> mediaPlayers;
  private final ArrayList<ImageButton> playButtons;
  private final ArrayList<ImageButton> stopButtons;
  private final ArrayList<ProgressBar> progressBars;
  private final ArrayList<SeekBarListener> seekBarListeners;

  private int mediaPlayerPosition;
  private boolean paused;

  /**
   * Constructor for BookNotesRecyclerViewAdapter.
   *
   * @param noteList list of notes
   * @param listener listener for bookNotesView
   * @param context  context
   */
  public BookNotesRecyclerViewAdapter(List<NoteItem> noteList, BookNotesViewListener listener,
                                      Context context) {
    this.noteList = noteList;
    this.listener = listener;
    this.context = context;

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
  public NotesViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
    return new NotesViewHolder(
        LayoutInflater.from(context)
            .inflate(R.layout.list_view_item_note, parent, false));
  }

  @Override
  public void onBindViewHolder(@NonNull NotesViewHolder holder, int position) {
    // Get element from your dataset at this position and replace the contents of the view
    // with that element
    NoteItem noteItem = noteList.get(position);

    holder.getModDateView().setText(noteItem.getModDate());
    holder.getNameView().setText(noteItem.getName());
    holder.getTypeView().setImageResource(noteItem.getImage());
    holder.itemView.setSelected(false);

    holder.itemView.setOnClickListener(v -> listener.onItemClicked(position));

    holder.itemView.setOnLongClickListener(v -> {
      if (position != RecyclerView.NO_POSITION) {
        return true;
      }
      listener.onLongItemClicked(position, noteItem, v);

      return false;
    });

    if (noteItem.getImage() == R.drawable.microphone) {
      setupAudioElements(holder, noteItem);
    }
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
        new SeekBarListener(context, mediaPlayer, progressBar, playedTime);
    seekBarListeners.add(seekBarListener);

    setTotalTime(noteItem, totalTime);

    progressBar.setOnSeekBarChangeListener(seekBarListener);

    setupMediaPlayerListeners(mediaPlayer, progressBar, playButton, stopButton, noteItem,
        seekBarListener);
  }

  private void setTotalTime(NoteItem noteItem, TextView totalTime) {
    File file = createAudioFile(noteItem);
    Uri uri = Uri.parse(file.getPath());
    MediaMetadataRetriever mmr = new MediaMetadataRetriever();
    mmr.setDataSource(context, uri);
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
      progressBars.get(i).setEnabled(false);
      seekBarListeners.get(i).reset();
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
    File tempAudio = createAudioFile(noteItem);
    try {
      mediaPlayer.setDataSource(tempAudio.getPath());
      mediaPlayer.prepareAsync();
      mediaPlayer.setOnPreparedListener(mp -> {
        setSelection(button, true, R.drawable.icon_pause);
        mp.start();
        seekBarListener.updateProgress();
      });
    } catch (IOException ex) {
      ex.printStackTrace();
    }
  }

  private File createAudioFile(NoteItem noteItem) {
    byte[] bytes = BookNotesViewModel.getNoteMedia(noteItem.getId());
    File tempAudio = null;
    try {
      tempAudio = File.createTempFile(String.valueOf(R.string.temporary_audio_file), String.valueOf(
          R.string.audio_file_suffix), context.getCacheDir());
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

  @Override
  public int getItemCount() {
    return noteList.size();
  }

  public interface BookNotesViewListener {

    void onItemClicked(int position); // callback function

    void onLongItemClicked(int position, NoteItem noteItem, View v);
  }

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
