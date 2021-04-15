package de.bibbuddy;

import android.os.Bundle;
import android.text.Html;
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
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import org.jsoup.Jsoup;

/**
 * TextNoteEditorFragment is responsible for creating, editing and saving text notes.
 *
 * @author Sabrina Freisleben
 */
public class TextNoteEditorFragment extends BackStackFragment {

  private ImageView formatArrow;
  private View view;
  private RichTextEditor richTextEditor;
  private Note note;
  private NoteModel noteModel;
  private Long bookId;
  private View formatOptions;

  @Override
  protected void onBackPressed() {
    saveNote();
    closeFragment();
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);

    setupMainActivity();

    setHasOptionsMenu(true);
  }

  private void setupMainActivity() {
    MainActivity mainActivity = (MainActivity) requireActivity();

    mainActivity.setVisibilityImportShareButton(View.GONE, View.GONE);
    mainActivity.setVisibilitySortButton(false);

    mainActivity.updateHeaderFragment(getString(R.string.navigation_notes));
    mainActivity.updateNavigationFragment(R.id.navigation_notes);
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_texteditor_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }


  @Override
  public boolean onOptionsItemSelected(MenuItem item) {

    if (item.getItemId() == R.id.menu_help_texteditor) {
      handleManualTextNoteEditor();
    } else if (item.getItemId() == R.id.menu_imprint) {
      MainActivity mainActivity = (MainActivity) requireActivity();
      mainActivity.openImprint();
    }

    return super.onOptionsItemSelected(item);
  }

  /**
   * Shows the TextNoteEditorFragment help element.
   */
  private void handleManualTextNoteEditor() {
    String htmlAsString = getString(R.string.text_editor_help_text);

    Bundle bundle = new Bundle();
    bundle.putString(LibraryKeys.MANUAL_TEXT, htmlAsString);

    HelpFragment helpFragment = new HelpFragment();
    helpFragment.setArguments(bundle);

    helpFragment
        .show(requireActivity().getSupportFragmentManager(), LibraryKeys.FRAGMENT_HELP_VIEW);
  }

  /**
   * Saves the current text as Note object.
   */
  private void saveNote() {
    String text = Html.toHtml(richTextEditor.getText(), Html.FROM_HTML_MODE_LEGACY);
    String rawText = Jsoup.parse(text).text();
    String[] lines = text.split("\\n");
    String name = "";

    Pattern pattern = Pattern.compile("\\S+");
    Matcher matcher;

    for (String line : lines) {
      String rawLine = Jsoup.parse(line).text();
      matcher = pattern.matcher(rawLine);
      if (matcher.find()) {
        name = rawLine;
        break;
      }
    }

    if (rawText.length() != 0) {
      if (requireArguments().size() == 2) {
        noteModel.updateNote(note, name, text);
      } else {
        noteModel.createNote(name, NoteTypeLut.TEXT, text, "");
        noteModel.linkNoteWithBook(bookId, noteModel.getLastNote().getId());
      }

      Toast.makeText(requireActivity(), getString(R.string.text_note_saved),
                     Toast.LENGTH_SHORT)
          .show();
    }
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    view = inflater.inflate(R.layout.fragment_text_note_editor, container, false);
    noteModel = new NoteModel(requireActivity());
    richTextEditor = view.findViewById(R.id.editor);

    enableBackPressedHandler();

    formatArrow = view.findViewById(R.id.formatArrow);
    formatArrow.setOnClickListener(v -> {
      formatOptions = view.findViewById(R.id.scroll_view);
      adjustFormatToolbarVisibility();
    });

    bookId = requireArguments().getLong(LibraryKeys.BOOK_ID);

    if (requireArguments().size() == 2) {
      Long noteId = requireArguments().getLong(LibraryKeys.NOTE_ID);
      note = noteModel.getNoteById(noteId);

      String text = note.getText();
      text = text.replace("align=\"center\"", "style=\"text-align:center;\"");
      text = text.replace("align=\"right\"", "style=\"text-align:end;\"");
      richTextEditor.setText(Html.fromHtml(text, Html.FROM_HTML_MODE_LEGACY));
    }

    richTextEditor.setSelection(richTextEditor.getEditableText().length());

    return view;
  }

  /**
   * Shows or hides the text format toolbar depending on if it is shown yet.
   */
  public void adjustFormatToolbarVisibility() {
    if (!formatToolbarIsShown()) {
      formatOptions.setVisibility(View.VISIBLE);
      formatArrow.setImageResource(R.drawable.format_up);
      setupUndoOption();
      setupRedoOption();
      setupBoldOption();
      setupItalicOption();
      setupUnderlineOption();
      setupStrikeThroughOption();
      setupBulletOption();
      setupQuoteOption();
      setupAlignmentOptions();
    } else {
      hideFormatToolbar();
    }
  }

  private void hideFormatToolbar() {
    formatOptions.setVisibility(View.GONE);
    formatArrow.setImageResource(R.drawable.format_down);
  }

  private boolean formatToolbarIsShown() {
    return formatOptions.getVisibility() == View.VISIBLE;
  }

  private void highlightSelectedToolbarItem(View view) {
    hideFormatToolbar();
    view.setSelected(!view.isSelected());
  }

  private void setupUndoOption() {
    ImageButton undo = view.findViewById(R.id.action_undo);

    undo.setOnClickListener(v -> {
      richTextEditor.undo();
      backgroundColorChange(undo);
    });
  }

  private void backgroundColorChange(ImageButton button) {
    button.setBackgroundColor(ContextCompat.getColor(requireActivity(), R.color.gray));
    ScheduledExecutorService backgroundExecutor = Executors.newSingleThreadScheduledExecutor();
    backgroundExecutor.schedule(() -> button.setBackgroundColor(0), 1, TimeUnit.SECONDS);
  }

  private void setupRedoOption() {
    ImageButton redo = view.findViewById(R.id.action_redo);

    redo.setOnClickListener(v -> {
      richTextEditor.redo();
      backgroundColorChange(redo);
    });
  }

  private void setupBoldOption() {
    ImageButton bold = view.findViewById(R.id.action_bold);

    bold.setOnClickListener(v -> {
      highlightSelectedToolbarItem(bold);
      richTextEditor.bold(bold.isSelected());
    });

    bold.setOnLongClickListener(v -> {
      Toast.makeText(requireActivity(), R.string.toast_bold, Toast.LENGTH_SHORT)
          .show();
      return true;
    });
  }

  private void setupItalicOption() {
    ImageButton italic = view.findViewById(R.id.action_italic);

    italic.setOnClickListener(v -> {
      highlightSelectedToolbarItem(italic);
      richTextEditor.italic(italic.isSelected());
    });

    italic.setOnLongClickListener(v -> {
      Toast.makeText(requireActivity(), R.string.toast_italic, Toast.LENGTH_SHORT)
          .show();
      return true;
    });
  }

  private void setupUnderlineOption() {
    ImageButton underline = view.findViewById(R.id.action_underline);

    underline.setOnClickListener(v -> {
      highlightSelectedToolbarItem(underline);
      richTextEditor.underline(underline.isSelected());
    });

    underline.setOnLongClickListener(v -> {
      Toast.makeText(requireActivity(), R.string.toast_underline, Toast.LENGTH_SHORT)
          .show();
      return true;
    });
  }

  private void setupStrikeThroughOption() {
    ImageButton strikeThrough = view.findViewById(R.id.action_strikeThrough);

    strikeThrough.setOnClickListener(v -> {
      highlightSelectedToolbarItem(strikeThrough);
      richTextEditor.strikeThrough(strikeThrough.isSelected());
    });

    strikeThrough.setOnLongClickListener(v -> {
      Toast.makeText(requireActivity(), R.string.toast_strikethrough,
                     Toast.LENGTH_SHORT).show();
      return true;
    });
  }

  private void setupBulletOption() {
    ImageButton bullet = view.findViewById(R.id.action_insert_bullets);

    bullet.setOnClickListener(v -> {
      highlightSelectedToolbarItem(bullet);
      richTextEditor.bullet(bullet.isSelected());
    });

    bullet.setOnLongClickListener(v -> {
      Toast.makeText(requireActivity(), R.string.toast_bullet, Toast.LENGTH_SHORT)
          .show();
      return true;
    });
  }

  private void setupQuoteOption() {
    ImageButton quote = view.findViewById(R.id.action_quote);

    quote.setOnClickListener(v -> {
      highlightSelectedToolbarItem(quote);
      richTextEditor.quote(quote.isSelected());
    });

    quote.setOnLongClickListener(v -> {
      Toast.makeText(requireActivity(), R.string.toast_quote, Toast.LENGTH_SHORT)
          .show();
      return true;
    });
  }

  private void setupAlignmentOptions() {
    ImageButton alignLeft = view.findViewById(R.id.action_alignLeft);
    ImageButton alignRight = view.findViewById(R.id.action_alignRight);
    ImageButton alignCenter = view.findViewById(R.id.action_alignCenter);

    alignLeft.setOnClickListener(v -> {
      richTextEditor.alignLeft();
      backgroundColorChange(alignLeft);
      deselectOtherAlignment(alignRight);
      deselectOtherAlignment(alignCenter);
    });

    MainActivity mainActivity = (MainActivity) requireActivity();
    alignLeft.setOnLongClickListener(v -> {
      Toast.makeText(mainActivity, R.string.toast_alignLeft, Toast.LENGTH_SHORT).show();
      return true;
    });

    alignRight.setOnClickListener(v -> {
      richTextEditor.alignRight();
      highlightSelectedToolbarItem(alignRight);
      deselectOtherAlignment(alignCenter);
    });

    alignRight.setOnLongClickListener(v -> {
      Toast.makeText(mainActivity, R.string.toast_alignRight, Toast.LENGTH_SHORT).show();
      return true;
    });

    alignCenter.setOnClickListener(v -> {
      richTextEditor.alignCenter();
      highlightSelectedToolbarItem(alignCenter);
      deselectOtherAlignment(alignRight);
    });

    alignCenter.setOnLongClickListener(v -> {
      Toast.makeText(mainActivity, R.string.toast_alignCenter, Toast.LENGTH_SHORT).show();
      return true;
    });
  }

  private void deselectOtherAlignment(ImageButton align) {
    if (align.isSelected()) {
      highlightSelectedToolbarItem(align);
    }
  }

}
