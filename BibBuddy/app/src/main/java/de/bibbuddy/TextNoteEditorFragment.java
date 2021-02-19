package de.bibbuddy;

import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.StringReader;
import org.jsoup.Jsoup;

/**
 * The TextNoteEditorFragment is responsible for the note in the text editor.
 *
 * @author Sabrina Freisleben
 */
public class TextNoteEditorFragment extends Fragment {

  private View view;
  private RichTextEditor richTextEditor;
  private Note note;
  private NoteModel noteModel;
  private Long bookId;

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        saveNote();
        FragmentManager fm = getParentFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
          fm.popBackStack();
        } else {
          requireActivity().onBackPressed();
        }
      }
    });
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
      if (getArguments().size() == 2) {
        noteModel.updateNote(note, name, text);
      } else {
        noteModel.addNote(name, 0, text);
        noteModel.linkNoteWithBook(bookId, noteModel.getLastNote().getId());
      }
    }
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    view = inflater.inflate(R.layout.fragment_text_note_editor, container, false);
    richTextEditor = view.findViewById(R.id.editor);
    noteModel = new NoteModel(getContext());
    bookId = getArguments().getLong(LibraryKeys.BOOK_ID);
    if (getArguments().size() == 2) {
      Long noteId = getArguments().getLong(LibraryKeys.NOTE_ID);
      note = noteModel.getNoteById(noteId);
      richTextEditor.setText(Html.fromHtml(note.getText(), 33));
    }
    richTextEditor.setSelection(richTextEditor.getEditableText().length());
    setupUndo();
    setupRedo();
    setupBold();
    setupItalic();
    setupUnderline();
    setupStrikeThrough();
    setupBullet();
    setupQuote();
    setupAlignment();
    initSlidingButtons();
    return view;
  }

  private void highlightSelectedItem(View view) {
    if (!view.isSelected()) {
      view.setBackgroundColor(getActivity().getColor(R.color.flirt_light));
      view.setSelected(true);
    } else {
      view.setBackgroundColor(0);
      view.setSelected(false);
    }
  }

  private void setupUndo() {
    ImageButton undo = view.findViewById(R.id.action_undo);
    undo.setOnClickListener(v -> {
      richTextEditor.undo();
      backgroundColorChange(undo);
    });
  }

  private void backgroundColorChange(ImageButton button) {
    button.setBackgroundColor(getActivity().getColor(R.color.flirt_light));
    Handler handler = new Handler();
    handler.postDelayed(() -> button.setBackgroundColor(0), 1000);
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
    ImageButton strikeThrough = view.findViewById(R.id.action_strikethrough);

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

  private void initSlidingButtons() {
    View rightArrowView = view.findViewById(R.id.slidebar_right);
    View leftArrowView = view.findViewById(R.id.slidebar_left);
    View scrollView = view.findViewById(R.id.scroll_view);
    scrollView.getViewTreeObserver()
        .addOnScrollChangedListener(() -> {
          if (scrollView.canScrollHorizontally(-1)) {
            leftArrowView.setVisibility(View.VISIBLE);
          } else {
            leftArrowView.setVisibility(View.GONE);
          }
          if (scrollView.canScrollHorizontally(1)) {
            rightArrowView.setVisibility(View.VISIBLE);
          } else {
            rightArrowView.setVisibility(View.GONE);
          }
        });
    rightArrowView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        scrollView.scrollTo((int) scrollView.getX() + 100, 0);
      }
    });

    leftArrowView.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        scrollView.scrollTo((int) (scrollView.getX() - 100), 0);
      }
    });

  }

}
