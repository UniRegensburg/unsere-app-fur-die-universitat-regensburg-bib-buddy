package de.bibbuddy;

import android.content.Context;
import android.media.MediaPlayer;
import android.os.Build;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
 * BookNotesRecyclerViewAdapter provides a binding from the noteList to the view
 * that is displayed within the RecyclerView of the BookNotesView.
 *
 * @author Sarah Kurek
 */
public class BookNotesRecyclerViewAdapter
    extends RecyclerView.Adapter<BookNotesRecyclerViewAdapter.NotesViewHolder> {

  private List<NoteItem> noteList;
  private BookNotesViewListener listener;
  private Context context;
  private ArrayList<MediaPlayer> mediaPlayers;
  private ArrayList<ImageButton> playButtons;
  private ArrayList<ImageButton> stopButtons;
  private ArrayList<LineBarVisualizer> visualizers;
  private boolean paused;
  private int mediaPlayerPosition;

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
    this.visualizers = new ArrayList<>();
    noteList.sort((o1, o2) -> {
      if (o1.getModDate() == null || o2.getModDate() == null) {
        return 0;
      }
      return o1.getModDate().compareTo(o2.getModDate());
    });
    Collections.reverse(noteList);
  }

  /**
   * Custom ViewHolder constructor to setup its basic view.
   *
   * @param itemView View of the RecyclerView-item.
   */
  public BookNotesRecyclerViewAdapter(@NonNull View itemView) {
    super();
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

    setupMediaButtons(holder, noteItem);

    holder.itemView.setOnClickListener(v -> listener.onItemClicked(position));

    holder.itemView.setOnLongClickListener(v -> {
      if (position == RecyclerView.NO_POSITION) {
        return false;
      }

      listener.onLongItemClicked(position, noteItem, v);
      return true;
    });

    setupMediaButtons(holder, noteItem);
  }

  private void setupMediaButtons(NotesViewHolder holder, NoteItem noteItem) {
    if (noteItem.getImage() == R.drawable.microphone) {
      ImageButton playButton = holder.getPlayNoteButton();
      playButtons.add(playButton);
      playButton.setVisibility(View.VISIBLE);
      ImageButton stopButton = holder.getStopNoteButton();
      stopButtons.add(stopButton);
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
    visualizer.setColor(ContextCompat.getColor(context, R.color.design_default_color_secondary));
    visualizer.setDensity(30);
    visualizer.setPlayer(mediaPlayer.getAudioSessionId());

    mediaPlayer.setOnCompletionListener(mp -> {
      mp.reset();
      paused = false;
      setSelection(playButton, false, R.drawable.icon_play);
      visualizer.setVisibility(View.INVISIBLE);
      stopButton.setClickable(false);
    });

    playButton.setOnClickListener(v -> {
      ImageButton button = (ImageButton) v;
      if (v.isSelected()) {
        setSelection(button, false, R.drawable.icon_play);
        mediaPlayer.pause();
        if (paused) {
          mediaPlayerPosition = mediaPlayer.getCurrentPosition();
          mediaPlayer.seekTo(mediaPlayerPosition);
          useVisualizer(mediaPlayer, visualizer);
          mediaPlayer.start();
        }
        paused = !paused;
      } else {
        resetPlayers();
        stopButton.setClickable(true);
        byte[] bytes = BookNotesViewModel.getNoteMedia(noteItem.getId());
        playAudio(mediaPlayer, button, visualizer, bytes);
        paused = false;
      }
    });

    stopButton.setOnClickListener(v -> {
      mediaPlayer.stop();
      visualizer.setVisibility(View.INVISIBLE);
      paused = false;
      resetPlayers();
    });
  }

  private void resetPlayers() {
    for (int i = 0; i < mediaPlayers.size(); i++) {
      mediaPlayers.get(i).stop();
      setSelection(playButtons.get(i), false, R.drawable.icon_play);
      stopButtons.get(i).setClickable(false);
      visualizers.get(i).setVisibility(View.INVISIBLE);
    }
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

  private void playAudio(MediaPlayer mediaPlayer, ImageButton button, LineBarVisualizer visualizer,
                         byte[] bytes) {
    try {
      File tempAudio =
          File.createTempFile(String.valueOf(R.string.temporary_audio_file), String.valueOf(
              R.string.audio_file_suffix), context.getCacheDir());
      tempAudio.deleteOnExit();
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        Path path = Paths.get(tempAudio.getPath());
        Files.write(path, bytes);
      }
      mediaPlayer.reset();
      mediaPlayer.setOnPreparedListener(mp -> {
        useVisualizer(mp, visualizer);
        setSelection(button, true, R.drawable.icon_pause);
        mp.start();
      });
      mediaPlayer.setDataSource(tempAudio.getPath());
      mediaPlayer.prepareAsync();
    } catch (IOException ex) {
      ex.printStackTrace();
    }
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
