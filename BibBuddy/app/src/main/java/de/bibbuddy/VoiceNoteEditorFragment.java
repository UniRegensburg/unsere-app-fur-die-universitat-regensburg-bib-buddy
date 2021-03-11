package de.bibbuddy;

import android.Manifest;
import android.content.pm.PackageManager;
import android.media.MediaPlayer;
import android.media.MediaRecorder;
import android.os.Build;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.RelativeLayout;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ActivityCompat;
import androidx.fragment.app.Fragment;
import app.minimize.com.seek_bar_compat.SeekBarCompat;
import com.skyfishjy.library.RippleBackground;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * The VoiceNoteEditorFragment is responsible for the note in the voice editor.
 *
 * @author Sabrina Freisleben
 */
public class VoiceNoteEditorFragment extends Fragment {

  private NoteModel noteModel;
  private Long bookId;

  private RelativeLayout voiceNotePlayer;
  private TextView playedTime;
  private TextView totalTime;
  private RippleBackground pulse;
  private ImageButton recordButton;
  private ImageButton playButton;
  private ImageButton stopButton;

  private MediaRecorder recorder;
  private MediaPlayer mediaPlayer;
  private File tempAudio;
  private MediaPlayer.OnCompletionListener completionListener;
  private SeekBarCompat seekBar;
  private SeekBarListener seekBarListener;
  private int mediaPlayerPosition;

  private boolean isRecording = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable
                               Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_voice_note_editor, container,
        false);

    noteModel = new NoteModel(getContext());

    voiceNotePlayer = view.findViewById(R.id.voice_note_editor_player);
    seekBar = voiceNotePlayer.findViewById(R.id.seekBar_voice_note_editor);
    playedTime = voiceNotePlayer.findViewById(R.id.played_time_voice_note_editor);
    totalTime = voiceNotePlayer.findViewById(R.id.total_time_voice_note_editor);

    recordButton = view.findViewById(R.id.record);
    playButton = view.findViewById(R.id.play);
    stopButton = view.findViewById(R.id.stop);
    pulse = view.findViewById(R.id.pulsator);


    if (getArguments() != null) {
      bookId = getArguments().getLong(LibraryKeys.BOOK_ID);
    }

    if (ActivityCompat.checkSelfPermission(requireActivity(), Manifest.permission.RECORD_AUDIO)
        == PackageManager.PERMISSION_GRANTED) {
      setupOnClickListeners();
    } else {
      int myPermissionsRecordAudio = 1;
      ActivityCompat
          .requestPermissions(requireActivity(), new String[] {Manifest.permission.RECORD_AUDIO},
              myPermissionsRecordAudio);
      boolean isAnswered = true;
      while (isAnswered) {
        if (ActivityCompat.checkSelfPermission(requireActivity(),
            Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {
          isAnswered = false;
          setupOnClickListeners();
        }
      }
    }

    return view;
  }

  private void switchSelection(View v, ImageButton button, int res, int resSel) {
    v.setSelected(!v.isSelected());
    if (v.isSelected()) {
      button.setImageResource(resSel);
    } else {
      button.setImageResource(res);
    }
  }

  private void resetButton(ImageButton button, int res) {
    button.setSelected(false);
    button.setImageResource(res);
  }

  private void saveNote() {
    String name = getString(R.string.voice_note_name);
    Long currentTime = new Date().getTime();
    name = name + " " + getDate(currentTime);
    int size = (int) tempAudio.length();
    byte[] bytes = new byte[size];
    try {
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        bytes = Files.readAllBytes(Paths.get(tempAudio.getPath()));
      }
      noteModel.addNote(name, 1, "", bytes);
      noteModel.linkNoteWithBook(bookId, noteModel.getLastNote().getId());
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void setupOnClickListeners() {
    View.OnClickListener recordClickListener = v -> {
      resetButton(playButton, R.drawable.play);
      resetButton(recordButton, R.drawable.record_microphone);
      switchSelection(v, recordButton, R.drawable.record_microphone,
          R.drawable.record_microphone_selected);
      isRecording = !isRecording;
      onRecord(isRecording);
    };

    completionListener = mp -> {
      resetButton(playButton, R.drawable.play);
      stopPlaying();
      stopRecording();
    };

    View.OnClickListener playClickListener = v -> {
      resetButton(recordButton, R.drawable.record_microphone);
      if (tempAudio != null) {
        switchSelection(v, playButton, R.drawable.play, R.drawable.pause);
        onPlay();
      }
    };

    View.OnClickListener stopClickListener = v -> {
      resetButton(playButton, R.drawable.play);
      resetButton(recordButton, R.drawable.record_microphone);
      stopButton.setImageResource(R.drawable.stop_selected);
      ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();
      backgroundExecutor
          .schedule(() -> stopButton.setImageResource(R.drawable.stop), 1, TimeUnit.SECONDS);
      stopRecording();
      stopPlaying();
    };

    recordButton.setOnClickListener(recordClickListener);
    playButton.setOnClickListener(playClickListener);
    stopButton.setOnClickListener(stopClickListener);
  }

  private void onRecord(boolean start) {
    stopPlaying();
    if (tempAudio != null) {
      tempAudio = null;
    }
    voiceNotePlayer.setVisibility(View.GONE);
    if (start) {
      startRecording();
    } else {
      stopRecording();
    }
  }

  private void onPlay() {
    stopRecording();
    if (!playButton.isSelected()) {
      mediaPlayer.pause();
      mediaPlayerPosition = mediaPlayer.getCurrentPosition();
    } else {
      if (mediaPlayer != null) {
        if (mediaPlayer.getCurrentPosition() == 0) {
          mediaPlayer.seekTo(mediaPlayerPosition);
        }
        mediaPlayer.start();
      } else {
        startPlaying();
      }
    }
  }

  private void startPlaying() {
    mediaPlayer = new MediaPlayer();
    try {
      if (tempAudio != null) {
        mediaPlayer.setDataSource(tempAudio.getPath());
        mediaPlayer.setOnCompletionListener(completionListener);
        mediaPlayer.prepare();
        mediaPlayer.start();
        setupVoiceNotePlayer();
      }
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void setupVoiceNotePlayer() {
    seekBarListener =
        new SeekBarListener(getActivity(), mediaPlayer, seekBar, playedTime);
    int millis = mediaPlayer.getDuration();
    String duration = new SimpleDateFormat("mm:ss", Locale.getDefault()).format(new Date(millis));
    totalTime.setText(duration);
    seekBar.setOnSeekBarChangeListener(seekBarListener);
    voiceNotePlayer.setVisibility(View.VISIBLE);
    seekBarListener.updateProgress();
  }

  private void startRecording() {
    pulse.startRippleAnimation();
    recorder = new MediaRecorder();
    try {
      tempAudio = File.createTempFile(String.valueOf(R.string.temporary_audio_file),
          String.valueOf(R.string.audio_file_suffix),
          requireActivity().getCacheDir());
      tempAudio.deleteOnExit();
      recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
      recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
      recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
      int samplingRate = 44100;
      recorder.setAudioSamplingRate(samplingRate);
      int encodingBitRate = 96000;
      recorder.setAudioEncodingBitRate(encodingBitRate);
      if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.O) {
        recorder.setOutputFile(tempAudio);
      }
      recorder.prepare();
      recorder.start();
    } catch (IOException e) {
      e.printStackTrace();
    }
  }

  private void stopRecording() {
    pulse.stopRippleAnimation();
    if (recorder != null) {
      recorder.release();
      saveNote();
      isRecording = false;
    }
  }

  private void stopPlaying() {
    if (mediaPlayer != null) {
      seekBarListener.reset();
      mediaPlayer = null;
      mediaPlayerPosition = 0;
      isRecording = false;
    }
  }

  private String getDate(Long date) {
    Date d = new Date(date);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX",
        Locale.getDefault());
    String string = simpleDateFormat.format(d);
    String day = string.substring(8, 10);
    String month = string.substring(5, 7);
    String year = string.substring(0, 4);

    string = day + "." + month + "." + year;

    return string;
  }

  @Override
  public void onPause() {
    super.onPause();
    stopRecording();
    stopPlaying();
  }

}
