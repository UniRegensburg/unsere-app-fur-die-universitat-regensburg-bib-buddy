package de.bibbuddy;

import android.database.Cursor;
import android.os.Bundle;
import android.text.Editable;
import android.text.Html;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import java.util.Date;

public class TextNoteEditorFragment extends Fragment {

    private Long noteId;
    private View view;
    private RichTextEditor richTextEditor;
    private boolean highlighted = false;
    private Note note;
    private Long modDate;
    private DatabaseHelper databaseHelper;

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_text_note_editor, container, false);
        richTextEditor = view.findViewById(R.id.editor);
        databaseHelper = new DatabaseHelper(getContext());
        NoteDAO noteDAO = new NoteDAO(databaseHelper);
        if (getArguments() != null) {
            noteId = getArguments().getLong("noteId");
            note = noteDAO.findById(noteId);
            richTextEditor.setText(Html.fromHtml(noteDAO.findById(noteId).getText(), 33));
            System.out.println(Html.fromHtml(noteDAO.findById(noteId).getText()));
            modDate = note.getModDate();
        } else {
            Cursor c = databaseHelper.getReadableDatabase().query(DatabaseHelper.TABLE_NAME_NOTE, null, null, null, null, null, null);
            c.moveToLast();
            Long currentDate = new Date().getTime();

            note = new Note("", 0, "", currentDate, currentDate);
            c.close();
            noteDAO.create(note);
            noteId = note.getId();
        }
        richTextEditor.setSelection(richTextEditor.getEditableText().length());
        setupTextWatcher();
        setupUndo();
        setupRedo();
        setupBold();
        setupItalic();
        setupUnderline();
        setupStrikethrough();
        setupBullet();
        setupQuote();
        setupAlignment();
        initSlidingButtons();
        return view;
    }

    private void setupTextWatcher() {
        TextWatcher textWatcher = new TextWatcher() {

            public void afterTextChanged(Editable s) {
            }

            public void beforeTextChanged(CharSequence s, int start, int count, int after) {
            }

            public void onTextChanged(CharSequence s, int start, int before,
                                      int count) {
                modDate = new Date().getTime();
            }
        };
        richTextEditor.addTextChangedListener(textWatcher);
    }

    private void highlightSelectedItem(View view) {
        if (!highlighted) {
            view.setBackgroundColor(getActivity().getColor(R.color.flirt_light));
            highlighted = true;
        } else {
            view.setBackgroundColor(0);
            highlighted = false;
        }
    }

    private void setupUndo() {
        ImageButton undo = view.findViewById(R.id.action_undo);
        undo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richTextEditor.undo();
                highlightSelectedItem(undo);
            }
        });
    }

    private void setupRedo() {
        ImageButton redo = view.findViewById(R.id.action_redo);
        redo.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richTextEditor.redo();
                highlightSelectedItem(redo);
            }
        });
    }

    private void setupBold() {
        ImageButton bold = view.findViewById(R.id.action_bold);

        bold.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richTextEditor.bold(richTextEditor.contains(RichTextEditor.FORMAT_BOLD));
                highlightSelectedItem(bold);
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
                richTextEditor.italic(richTextEditor.contains(RichTextEditor.FORMAT_ITALIC));
                highlightSelectedItem(italic);
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
                richTextEditor.underline(richTextEditor.contains(RichTextEditor.FORMAT_UNDERLINE));
                highlightSelectedItem(underline);
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
        ImageButton strikethrough = view.findViewById(R.id.action_strikethrough);

        strikethrough.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                richTextEditor.strikethrough(richTextEditor.contains(RichTextEditor.FORMAT_STRIKETHROUGH));
                highlightSelectedItem(strikethrough);
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
                richTextEditor.bullet(richTextEditor.contains(RichTextEditor.FORMAT_BULLET));
                highlightSelectedItem(bullet);
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
                richTextEditor.quote(richTextEditor.contains(RichTextEditor.FORMAT_QUOTE));
                highlightSelectedItem(quote);
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

    private void initSlidingButtons() {
        View rightArrowView = view.findViewById(R.id.slidebar_right);
        View leftArrowView = view.findViewById(R.id.slidebar_left);
        View scrollView = view.findViewById(R.id.scroll_view);

        scrollView.getViewTreeObserver()
                .addOnScrollChangedListener(new ViewTreeObserver.OnScrollChangedListener() {
                    @Override
                    public void onScrollChanged() {
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

    @Override
    public void onPause() {
        super.onPause();
        String text = Html.toHtml(richTextEditor.getText(), Html.TO_HTML_PARAGRAPH_LINES_INDIVIDUAL);
        NoteDAO noteDAO = new NoteDAO(databaseHelper);
        if (text.length() != 0) {
            noteDAO.updateNote(noteId, text, note.getType(), String.valueOf(text), note.getCreateDate(),
                    modDate, note.getNoteFileId());
        } else {
            noteDAO.delete(noteId);
        }
    }

}
