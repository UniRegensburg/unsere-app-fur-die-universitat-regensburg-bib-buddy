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
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import com.skyfishjy.library.RippleBackground;
import java.io.File;
import java.io.IOException;
import java.util.Date;

/**
 * The VoiceNoteEditorFragment is responsible for creating and saving voice notes.
 *
 * @author Sabrina Freisleben.
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

    String filePath = requireActivity().getExternalCacheDir().getAbsolutePath();
    if (noteModel.getVoiceNoteList().isEmpty()) {
      filePath += "/audio_record_1.mp4";
    } else {
      filePath += "/audio_record_" + noteModel.getVoiceNoteList().size() + 1 + ".mp4";
    }
    newAudio = new File(filePath);

    setupOnClickListener();

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

  //Show the VoiceNoteEditorFragment help-element.
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

  private void setupOnClickListener() {
    View.OnClickListener recordClickListener = v -> {
      v.setSelected(!v.isSelected());
      record(v.isSelected());
    };
    recordButton.setOnClickListener(recordClickListener);
  }

  //Start or stop recording depending on the record-button selection.
  private void record(boolean start) {
    if (start) {
      pulse.startRippleAnimation();
      recordButton.setBackgroundTintList(
          ContextCompat.getColorStateList(requireContext(), R.color.gray_light));
      isRecording = true;
      startRecording();
    } else {
      pulse.stopRippleAnimation();
      recordButton
          .setBackgroundTintList(ContextCompat.getColorStateList(requireContext(), R.color.white));
      isRecording = false;
      stopRecording();
    }
  }

  private void startRecording() {
    recorder = new MediaRecorder();
    recorder.setAudioSource(MediaRecorder.AudioSource.MIC);
    recorder.setOutputFormat(MediaRecorder.OutputFormat.MPEG_4);
    recorder.setAudioEncoder(MediaRecorder.AudioEncoder.AAC);
    recorder.setAudioSamplingRate(44100);
    recorder.setAudioEncodingBitRate(56000);
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
    Long currentTime = new Date().getTime();
    String fileName =
        getString(R.string.voice_note_name) + DateConverter.convertDateToString(currentTime);
    noteModel.createNote(fileName, 1, "", newAudio.getPath());
    noteModel.linkNoteWithBook(bookId, noteModel.getLastNote().getId());
    Toast.makeText(requireActivity(), getString(R.string.voice_note_saved), Toast.LENGTH_SHORT)
        .show();
  }

  @Override
  public void onPause() {
    super.onPause();
    if (isRecording) {
      stopRecording();
    }
  }

}
