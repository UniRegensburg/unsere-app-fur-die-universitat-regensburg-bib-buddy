package de.bibbuddy;

import android.os.Bundle;
import android.os.Handler;
import android.text.Html;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.ImageView;
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

  ImageView arrow;
  private View view;
  private RichTextEditor richTextEditor;
  private Note note;
  private NoteModel noteModel;
  private Long bookId;
  private View formatOptions;

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
    ImageButton formatIndicator = view.findViewById(R.id.formatIndicator);
    ImageButton formatArrow = view.findViewById(R.id.formatArrow);
    arrow = view.findViewById(R.id.formatArrow);
    noteModel = new NoteModel(getContext());
    bookId = getArguments().getLong(LibraryKeys.BOOK_ID);
    if (getArguments().size() == 2) {
      Long noteId = getArguments().getLong(LibraryKeys.NOTE_ID);
      note = noteModel.getNoteById(noteId);
      richTextEditor.setText(Html.fromHtml(note.getText(), 33));
    }
    richTextEditor.setSelection(richTextEditor.getEditableText().length());
    formatIndicator.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        formatOptions = view.findViewById(R.id.scroll_view);
        slideUpOrDown();
      }
    });
    formatArrow.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        formatOptions = view.findViewById(R.id.scroll_view);
        slideUpOrDown();
      }
    });
    return view;
  }

  /**
   * Method to perform an upside-down animation for the deletePanel.
   */
  public void slideUpOrDown() {
    if (!formatOptionsAreShown()) {
      formatOptions.setVisibility(View.VISIBLE);
      arrow.setImageResource(R.drawable.arrow_up);
      setupUndo();
      setupRedo();
      setupBold();
      setupItalic();
      setupUnderline();
      setupStrikethrough();
      setupBullet();
      setupQuote();
      setupAlignment();
    } else if (formatOptionsAreShown()) {
      hideFormatOptions();
    }
  }

  private void hideFormatOptions() {
    formatOptions.setVisibility(View.GONE);
    arrow.setImageResource(R.drawable.arrow_down);
  }

  private boolean formatOptionsAreShown() {
    return formatOptions.getVisibility() == View.VISIBLE;
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
    undo.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        hideFormatOptions();
        richTextEditor.undo();
        undo.setBackgroundColor(getActivity().getColor(R.color.flirt_light));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            undo.setBackgroundColor(0);
          }
        }, 1000);
      }
    });
  }

  private void setupRedo() {
    ImageButton redo = view.findViewById(R.id.action_redo);
    redo.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        hideFormatOptions();
        richTextEditor.redo();
        redo.setBackgroundColor(getActivity().getColor(R.color.flirt_light));
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
          @Override
          public void run() {
            redo.setBackgroundColor(0);
          }
        }, 1000);
      }
    });
  }

  private void setupBold() {
    ImageButton bold = view.findViewById(R.id.action_bold);

    bold.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        hideFormatOptions();
        highlightSelectedItem(bold);
        richTextEditor.bold(bold.isSelected());
      }
    });

    bold.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Toast.makeText(getContext(), R.string.toast_bold, Toast.LENGTH_SHORT).show();
        return true;
      }
    });
  }

  private void setupItalic() {
    ImageButton italic = view.findViewById(R.id.action_italic);

    italic.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        hideFormatOptions();
        highlightSelectedItem(italic);
        richTextEditor.italic(italic.isSelected());
      }
    });

    italic.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Toast.makeText(getContext(), R.string.toast_italic, Toast.LENGTH_SHORT).show();
        return true;
      }
    });
  }

  private void setupUnderline() {
    ImageButton underline = view.findViewById(R.id.action_underline);

    underline.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        hideFormatOptions();
        highlightSelectedItem(underline);
        richTextEditor.underline(underline.isSelected());
      }
    });

    underline.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Toast.makeText(getContext(), R.string.toast_underline, Toast.LENGTH_SHORT).show();
        return true;
      }
    });
  }

  private void setupStrikethrough() {
    ImageButton strikethrough = view.findViewById(R.id.action_strikeThrough);

    strikethrough.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        hideFormatOptions();
        highlightSelectedItem(strikethrough);
        richTextEditor.strikeThrough(strikethrough.isSelected());
      }
    });

    strikethrough.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Toast.makeText(getContext(), R.string.toast_strikethrough, Toast.LENGTH_SHORT).show();
        return true;
      }
    });
  }

  private void setupBullet() {
    ImageButton bullet = view.findViewById(R.id.action_insert_bullets);

    bullet.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        hideFormatOptions();
        highlightSelectedItem(bullet);
        richTextEditor.bullet(bullet.isSelected());
      }
    });


    bullet.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Toast.makeText(getContext(), R.string.toast_bullet, Toast.LENGTH_SHORT).show();
        return true;
      }
    });
  }

  private void setupQuote() {
    ImageButton quote = view.findViewById(R.id.action_quote);

    quote.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        hideFormatOptions();
        highlightSelectedItem(quote);
        richTextEditor.quote(quote.isSelected());
      }
    });

    quote.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Toast.makeText(getContext(), R.string.toast_quote, Toast.LENGTH_SHORT).show();
        return true;
      }
    });
  }

  private void setupAlignment() {
    ImageButton alignLeft = view.findViewById(R.id.action_alignLeft);

    alignLeft.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        hideFormatOptions();
        richTextEditor.alignLeft();
        highlightSelectedItem(alignLeft);
      }
    });

    alignLeft.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Toast.makeText(getContext(), R.string.toast_alignLeft, Toast.LENGTH_SHORT).show();
        return true;
      }
    });

    ImageButton alignRight = view.findViewById(R.id.action_alignRight);

    alignRight.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        hideFormatOptions();
        richTextEditor.alignRight();
        highlightSelectedItem(alignRight);
      }
    });

    alignRight.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Toast.makeText(getContext(), R.string.toast_alignRight, Toast.LENGTH_SHORT).show();
        return true;
      }
    });

    ImageButton alignCenter = view.findViewById(R.id.action_alignCenter);

    alignCenter.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        hideFormatOptions();
        richTextEditor.alignCenter();
        highlightSelectedItem(alignCenter);
      }
    });

    alignCenter.setOnLongClickListener(new View.OnLongClickListener() {
      @Override
      public boolean onLongClick(View v) {
        Toast.makeText(getContext(), R.string.toast_alignCenter, Toast.LENGTH_SHORT).show();
        return true;
      }
    });
  }

}
