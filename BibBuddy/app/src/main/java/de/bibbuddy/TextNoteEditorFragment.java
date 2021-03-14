package de.bibbuddy;

import android.app.AlertDialog;
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
import android.widget.ImageView;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.content.ContextCompat;
import androidx.fragment.app.Fragment;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import org.jsoup.Jsoup;

/**
 * The TextNoteEditorFragment is responsible for the note in the text editor.
 *
 * @author Sabrina Freisleben
 */
public class TextNoteEditorFragment extends Fragment {

  ImageView formatArrow;
  private View view;
  private RichTextEditor richTextEditor;
  private Note note;
  private NoteModel noteModel;
  private Long bookId;
  private View formatOptions;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    ((MainActivity) getActivity()).setVisibilityImportShareButton(View.INVISIBLE, View.INVISIBLE);
    setHasOptionsMenu(true);
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_text_note_editor, container, false);
    richTextEditor = view.findViewById(R.id.editor);
    ImageButton formatIndicator = view.findViewById(R.id.formatIndicator);
    formatArrow = view.findViewById(R.id.formatArrow);
    formatIndicator.setOnClickListener(v -> {
      formatOptions = view.findViewById(R.id.scroll_view);
      slideUpOrDown();
    });
    formatArrow.setOnClickListener(v -> {
      formatOptions = view.findViewById(R.id.scroll_view);
      slideUpOrDown();
    });
    noteModel = new NoteModel(getContext());
    if (getArguments() != null) {
      bookId = getArguments().getLong(LibraryKeys.BOOK_ID);
      if (getArguments().size() == 2) {
        Long noteId = getArguments().getLong(LibraryKeys.NOTE_ID);
        note = noteModel.getNoteById(noteId);
        richTextEditor.setText(Html.fromHtml(note.getText(), 33));
      }
    }
    richTextEditor.setSelection(richTextEditor.getEditableText().length());
    return view;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_text_editor_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    if (item.getItemId() == R.id.menu_help_text_editor) {
      handleManualTextEditor();
    } else {
      Toast.makeText(getContext(), String.valueOf(R.string.error), Toast.LENGTH_SHORT).show();
    }

    return super.onOptionsItemSelected(item);
  }

  private void handleManualTextEditor() {
    Spanned htmlAsString = Html.fromHtml(getString(R.string.text_editor_help_text), Html.FROM_HTML_MODE_COMPACT);

    android.app.AlertDialog.Builder helpAlert = new AlertDialog.Builder(requireActivity());
    helpAlert.setCancelable(false);
    helpAlert.setTitle(R.string.help);
    helpAlert.setMessage(htmlAsString);
    helpAlert.setPositiveButton(R.string.ok, (dialog, which) -> {});
    helpAlert.show();
  }

  private void saveNote() {
    String text = Html.toHtml(richTextEditor.getText(), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL);
    String rawText = Jsoup.parse(text).text();
    String name = "";
    BufferedReader bufferedReader = new BufferedReader(new StringReader(rawText));
    try {
      name = bufferedReader.readLine();
    } catch (IOException e) {
      e.printStackTrace();
    }
    if (rawText.length() != 0) {
      if (getArguments() != null && getArguments().size() == 2) {
        noteModel.updateNote(note, name, text);
      } else {
        noteModel.addNote(name, 0, text, null);
        noteModel.linkNoteWithBook(bookId, noteModel.getLastNote().getId());
      }
    }
  }

  /**
   * Method to perform an upside-down animation for the deletePanel.
   */
  public void slideUpOrDown() {
    if (!formatOptionsAreShown()) {
      formatOptions.setVisibility(View.VISIBLE);
      formatArrow.setImageResource(R.drawable.arrow_up);
      setupUndo();
      setupRedo();
      setupBold();
      setupItalic();
      setupUnderline();
      setupStrikeThrough();
      setupBullet();
      setupQuote();
      setupAlignment();
    } else if (formatOptionsAreShown()) {
      hideFormatOptions();
    }
  }

  private void hideFormatOptions() {
    formatOptions.setVisibility(View.GONE);
    formatArrow.setImageResource(R.drawable.arrow_down);
  }

  private boolean formatOptionsAreShown() {
    return formatOptions.getVisibility() == View.VISIBLE;
  }

  private void highlightSelectedItem(View view) {
    hideFormatOptions();
    view.setSelected(!view.isSelected());
  }

  private void setupUndo() {
    ImageButton undo = view.findViewById(R.id.action_undo);
    undo.setOnClickListener(v -> {
      richTextEditor.undo();
      backgroundColorChange(undo);
    });
  }

  private void backgroundColorChange(ImageButton button) {
    button.setBackgroundColor(ContextCompat.getColor(requireContext(), R.color.flirt_light));
    ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();
    backgroundExecutor.schedule(() -> button.setBackgroundColor(0), 1, TimeUnit.SECONDS);
  }

  private void setupRedo() {
    ImageButton redo = view.findViewById(R.id.action_redo);
    redo.setOnClickListener(v -> {
      richTextEditor.redo();
      backgroundColorChange(redo);
    });
  }

  private void setupBold() {
    ImageButton bold = view.findViewById(R.id.action_bold);
    bold.setOnClickListener(v -> {
      highlightSelectedItem(bold);
      richTextEditor.bold(bold.isSelected());
    });

    bold.setOnLongClickListener(v -> {
      Toast.makeText(getContext(), R.string.toast_bold, Toast.LENGTH_SHORT).show();
      return true;
    });
  }

  private void setupItalic() {
    ImageButton italic = view.findViewById(R.id.action_italic);
    italic.setOnClickListener(v -> {
      highlightSelectedItem(italic);
      richTextEditor.italic(italic.isSelected());
    });
    italic.setOnLongClickListener(v -> {
      Toast.makeText(getContext(), R.string.toast_italic, Toast.LENGTH_SHORT).show();
      return true;
    });
  }

  private void setupUnderline() {
    ImageButton underline = view.findViewById(R.id.action_underline);
    underline.setOnClickListener(v -> {
      highlightSelectedItem(underline);
      richTextEditor.underline(underline.isSelected());
    });

    underline.setOnLongClickListener(v -> {
      Toast.makeText(getContext(), R.string.toast_underline, Toast.LENGTH_SHORT).show();
      return true;
    });
  }

  private void setupStrikeThrough() {
    ImageButton strikeThrough = view.findViewById(R.id.action_strikeThrough);
    strikeThrough.setOnClickListener(v -> {
      highlightSelectedItem(strikeThrough);
      richTextEditor.strikeThrough(strikeThrough.isSelected());
    });
    strikeThrough.setOnLongClickListener(v -> {
      Toast.makeText(getContext(), R.string.toast_strikethrough, Toast.LENGTH_SHORT).show();
      return true;
    });
  }

  private void setupBullet() {
    ImageButton bullet = view.findViewById(R.id.action_insert_bullets);
    bullet.setOnClickListener(v -> {
      highlightSelectedItem(bullet);
      richTextEditor.bullet(bullet.isSelected());
    });
    bullet.setOnLongClickListener(v -> {
      Toast.makeText(getContext(), R.string.toast_bullet, Toast.LENGTH_SHORT).show();
      return true;
    });
  }

  private void setupQuote() {
    ImageButton quote = view.findViewById(R.id.action_quote);
    quote.setOnClickListener(v -> {
      highlightSelectedItem(quote);
      richTextEditor.quote(quote.isSelected());
    });
    quote.setOnLongClickListener(v -> {
      Toast.makeText(getContext(), R.string.toast_quote, Toast.LENGTH_SHORT).show();
      return true;
    });
  }

  private void setupAlignment() {
    ImageButton alignLeft = view.findViewById(R.id.action_alignLeft);
    ImageButton alignRight = view.findViewById(R.id.action_alignRight);
    ImageButton alignCenter = view.findViewById(R.id.action_alignCenter);

    alignLeft.setOnClickListener(v -> {
      richTextEditor.alignLeft();
      backgroundColorChange(alignLeft);
      deselectOtherAlignment(alignRight);
      deselectOtherAlignment(alignCenter);
    });

    alignLeft.setOnLongClickListener(v -> {
      Toast.makeText(getContext(), R.string.toast_alignLeft, Toast.LENGTH_SHORT).show();
      return true;
    });

    alignRight.setOnClickListener(v -> {
      richTextEditor.alignRight();
      highlightSelectedItem(alignRight);
      deselectOtherAlignment(alignCenter);
    });

    alignRight.setOnLongClickListener(v -> {
      Toast.makeText(getContext(), R.string.toast_alignRight, Toast.LENGTH_SHORT).show();
      return true;
    });

    alignCenter.setOnClickListener(v -> {
      richTextEditor.alignCenter();
      highlightSelectedItem(alignCenter);
      deselectOtherAlignment(alignRight);
    });

    alignCenter.setOnLongClickListener(v -> {
      Toast.makeText(getContext(), R.string.toast_alignCenter, Toast.LENGTH_SHORT).show();
      return true;
    });
  }

  private void deselectOtherAlignment(ImageButton align) {
    if (align.isSelected()) {
      highlightSelectedItem(align);
    }
  }

  @Override
  public void onPause() {
    super.onPause();
    saveNote();
  }

}
