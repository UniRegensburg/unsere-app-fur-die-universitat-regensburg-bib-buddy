package de.bibbuddy;

import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.os.Bundle;
import android.text.Editable;
import android.text.TextWatcher;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.ViewTreeObserver;
import android.widget.ImageButton;
import android.widget.Toast;

import java.util.ArrayList;
import java.util.Calendar;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class TextNoteEditorFragment extends Fragment {

    private int noteId = 0;
    private View view;
    private RichTextEditor richTextEditor;
    private boolean highlighted = false;

    /*
        TODO: Delete when replaced by correct db methods
     */
    public static void deleteNote(int id) {
        SQLiteDatabase db = MainActivity.databaseHelper.getWritableDatabase();
        db.execSQL("DELETE FROM " + DatabaseHelper.TABLE_NAME_NOTE + " WHERE " + DatabaseHelper._ID + " = " + id);
        db.close();
    }

    public static NoteItem getNote(int id) {
        SQLiteDatabase db = MainActivity.databaseHelper.getReadableDatabase();
        String[] field = {DatabaseHelper._ID, DatabaseHelper.NAME, DatabaseHelper.TYPE, DatabaseHelper.TEXT, DatabaseHelper.CREATE_DATE, DatabaseHelper.MOD_DATE, DatabaseHelper.NOTE_FILE_ID};
        Cursor c = db.query(DatabaseHelper.TABLE_NAME_NOTE, field, DatabaseHelper._ID + " = " + id, null, null, null, null);
        c.moveToFirst();
        System.out.println(c.getColumnCount());
        int itemId = c.getColumnIndex(DatabaseHelper._ID);
        int name = c.getColumnIndex(DatabaseHelper.NAME);
        int type = c.getColumnIndex(DatabaseHelper.TYPE);
        int text = c.getColumnIndex(DatabaseHelper.TEXT);
        int createDate = c.getColumnIndex(DatabaseHelper.CREATE_DATE);
        int modDate = c.getColumnIndex(DatabaseHelper.MOD_DATE);
        int noteFileId = c.getColumnIndex(DatabaseHelper.NOTE_FILE_ID);

        int itemId1 = c.getInt(itemId);
        String itemName = c.getString(name);
        int itemType = c.getInt(type);
        String itemText = c.getString(text);
        String itemCreateDate = c.getString(createDate);
        String itemModDate = c.getString(modDate);
        int itemNoteFileId = c.getInt(noteFileId);

        NoteItem noteItem = new NoteItem(itemId1, itemName, itemType, itemText, itemCreateDate, itemModDate, itemNoteFileId);
        c.close();
        return noteItem;
    }

    public static ArrayList<NoteItem> getNotes() {
        SQLiteDatabase db = MainActivity.databaseHelper.getReadableDatabase();
        ArrayList<NoteItem> notes = new ArrayList<>();
        String[] field = {DatabaseHelper._ID, DatabaseHelper.NAME, DatabaseHelper.TYPE, DatabaseHelper.TEXT, DatabaseHelper.CREATE_DATE, DatabaseHelper.MOD_DATE, DatabaseHelper.NOTE_FILE_ID};
        Cursor c = db.query(DatabaseHelper.TABLE_NAME_NOTE, field, null, null, null, null, null);

        int id = c.getColumnIndex(DatabaseHelper._ID);
        int name = c.getColumnIndex(DatabaseHelper.NAME);
        int type = c.getColumnIndex(DatabaseHelper.TYPE);
        int text = c.getColumnIndex(DatabaseHelper.TEXT);
        int createDate = c.getColumnIndex(DatabaseHelper.CREATE_DATE);
        int modDate = c.getColumnIndex(DatabaseHelper.MOD_DATE);
        int noteFileId = c.getColumnIndex(DatabaseHelper.NOTE_FILE_ID);

        for (c.moveToFirst(); !c.isAfterLast(); c.moveToNext()) {
            int itemId = c.getInt(id);
            String itemName = c.getString(name);
            int itemType = c.getInt(type);
            String itemText = c.getString(text);
            String itemCreateDate = c.getString(createDate);
            String itemModDate = c.getString(modDate);
            int itemNoteFileId = c.getInt(noteFileId);
            notes.add(new NoteItem(itemId, itemName, itemType, itemText, itemCreateDate, itemModDate, itemNoteFileId));
        }
        c.close();
        return notes;
    }

    @Nullable
    @Override
    public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
        view = inflater.inflate(R.layout.fragment_text_note_editor, container, false);
        richTextEditor = view.findViewById(R.id.editor);
        // Fetch data that is passed from NotesFragment and accessing it using key and value
        if (getArguments() != null) {
            richTextEditor.setText(NotesFragment.notes.get(getArguments().getInt("noteId")).getText());
            noteId = NotesFragment.notes.get(getArguments().getInt("noteId")).getId();
        } else {
            MainActivity.databaseHelper.addNote("", 0, "", String.valueOf(Calendar.getInstance().getTime()), String.valueOf(Calendar.getInstance().getTime()), NotesFragment.notes.size());
            Cursor c = MainActivity.databaseHelper.getReadableDatabase().query(DatabaseHelper.TABLE_NAME_NOTE, null, null, null, null, null, null);
            c.moveToLast();
            noteId = c.getInt(c.getColumnIndex(DatabaseHelper._ID));
            c.close();
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
                richTextEditor.bold(!richTextEditor.contains(RichTextEditor.FORMAT_BOLD));
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
                richTextEditor.italic(!richTextEditor.contains(RichTextEditor.FORMAT_ITALIC));
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
                richTextEditor.underline(!richTextEditor.contains(RichTextEditor.FORMAT_UNDERLINE));
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
                richTextEditor.strikethrough(!richTextEditor.contains(RichTextEditor.FORMAT_STRIKETHROUGH));
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
                richTextEditor.bullet(!richTextEditor.contains(RichTextEditor.FORMAT_BULLET));
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
                richTextEditor.quote(!richTextEditor.contains(RichTextEditor.FORMAT_QUOTE));
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
        Editable text = richTextEditor.getText();
        if (text.length() != 0) {
            String date;
            if (getArguments() != null) {
                date = getNote(noteId).getCreateDate();
            } else {
                date = String.valueOf(Calendar.getInstance().getTime());
            }
            MainActivity.databaseHelper.updateNote(noteId, String.valueOf(text), 0, String.valueOf(text), date, String.valueOf(Calendar.getInstance().getTime()), noteId);
        } else {
            NoteItem noteItem = getNote(noteId);
            deleteNote(noteItem.getId());
        }
    }

}
