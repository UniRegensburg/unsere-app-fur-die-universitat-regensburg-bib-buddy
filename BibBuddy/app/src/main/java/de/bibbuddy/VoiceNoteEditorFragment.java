package de.bibbuddy;

import android.app.AlertDialog;
import android.media.MediaRecorder;
import android.os.Bundle;
import android.text.Html;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.skyfishjy.library.RippleBackground;
import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * The VoiceNoteEditorFragment is responsible for the note in the voice editor.
 *
 * @author Sabrina Freisleben
 */
public class VoiceNoteEditorFragment extends Fragment {

  private NoteModel noteModel;
  private Long bookId;

  private RippleBackground pulse;
  private ImageButton recordButton;

  private MediaRecorder recorder;
  private File newAudio;

  private boolean isRecording = false;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((MainActivity) requireActivity())
        .setVisibilityImportShareButton(View.GONE, View.GONE);
    ((MainActivity) requireActivity()).setVisibilitySortButton(false);
    setHasOptionsMenu(true);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable
                               Bundle savedInstanceState) {
    View view = inflater.inflate(R.layout.fragment_voice_note_editor, container,
        false);

    noteModel = new NoteModel(requireContext());

    pulse = view.findViewById(R.id.ripple_animation);
    recordButton = view.findViewById(R.id.record_button);

    if (getArguments() != null) {
      bookId = getArguments().getLong(LibraryKeys.BOOK_ID);
    }

    // Record to the external cache directory for visibility
    String filePath = requireActivity().getExternalCacheDir().getAbsolutePath();
    if (noteModel.getVoiceNoteList().isEmpty()) {
      filePath += "/audio_record_1.mp4";
    } else {
      filePath += "/audio_record_" + noteModel.getVoiceNoteList().size() + 1 + ".mp4";
    }
    newAudio = new File(filePath);

    setupOnClickListeners();

    return view;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_voice_note_editor_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_help_voice_editor) {
      handleManualVoiceEditor();
    }

    return super.onOptionsItemSelected(item);
  }

  private void handleManualVoiceEditor() {
    Spanned htmlAsString =
        Html.fromHtml(getString(R.string.voice_editor_help_text), Html.FROM_HTML_MODE_COMPACT);

    android.app.AlertDialog.Builder helpAlert = new AlertDialog.Builder(requireActivity());
    helpAlert.setCancelable(false);
    helpAlert.setTitle(R.string.help);
    helpAlert.setMessage(htmlAsString);
    helpAlert.setPositiveButton(R.string.ok, (dialog, which) -> {
    });
    helpAlert.show();
  }

  private void setupOnClickListeners() {
    View.OnClickListener recordClickListener = v -> {
      v.setSelected(!v.isSelected());
      onRecord(v.isSelected());
    };
    recordButton.setOnClickListener(recordClickListener);
  }

  private void onRecord(boolean start) {
    if (start) {
      pulse.startRippleAnimation();
      recordButton.setImageResource(R.drawable.record_microphone_selected);
      isRecording = true;
      startRecording();
    } else {
      pulse.stopRippleAnimation();
      recordButton.setImageResource(R.drawable.record_microphone);
      isRecording = false;
      stopRecording();
    }
  }

  private void startRecording() {
    recorder = new MediaRecorder();
    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
    int samplingRate = 44100;
    recorder.setAudioSamplingRate(samplingRate);
    int encodingBitRate = 56000;
    recorder.setAudioEncodingBitRate(encodingBitRate);
    recorder.setOutputFile(newAudio.getPath());
    try {
      recorder.prepare();
    } catch (IOException e) {
      e.printStackTrace();
    }
    recorder.start();
  }

  private void stopRecording() {
    if (recorder != null) {
      recorder.release();
    }
    saveNote();
  }

  private void saveNote() {
    String fileName = getString(R.string.voice_note_name) + getDate();
    noteModel.createNote(fileName, 1, "", newAudio.getPath());
    noteModel.linkNoteWithBook(bookId, noteModel.getLastNote().getId());
    Toast.makeText(requireActivity(), getString(R.string.voice_note_saved), Toast.LENGTH_SHORT)
        .show();
  }

  private String getDate() {
    Date d = Calendar.getInstance().getTime();
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
    if (isRecording) {
      stopRecording();
    }
  }

}
