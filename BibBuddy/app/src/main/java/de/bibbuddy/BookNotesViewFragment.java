package de.bibbuddy;

import android.app.AlertDialog;
import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.view.animation.Animation;
import android.view.animation.AnimationUtils;
import android.widget.PopupMenu;
import android.widget.RelativeLayout;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.recyclerview.widget.RecyclerView;
import java.util.ArrayList;
import java.util.List;

/**
 * BookNotesView is responsible for the noteList of a certain book.
 *
 * @author Sarah Kurek
 */
public class BookNotesViewFragment extends Fragment
    implements BookNotesRecyclerViewAdapter.BookNotesViewListener {

  private View view;
  private Context context;
  private BookNotesViewModel bookNotesViewModel;
  private BookNotesRecyclerViewAdapter adapter;
  private Long bookId;
  private Long shelfId;
  private String shelfName;
  private List<NoteItem> noteList;
  private List<NoteItem> selectedNoteItems;
  private RelativeLayout hiddenDeletePanel;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        FragmentManager fm = getParentFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
          fm.popBackStack();
        } else {
          requireActivity().onBackPressed();
        }
      }
    });

    Bundle bundle = this.getArguments();
    String bookTitle = "";
    if (bundle != null) {
      bookId = bundle.getLong(LibraryKeys.BOOK_ID);
      shelfId = bundle.getLong(LibraryKeys.SHELF_ID);
      shelfName = bundle.getString(LibraryKeys.SHELF_NAME);
      bookTitle = bundle.getString(LibraryKeys.BOOK_TITLE);
    }
    view = inflater.inflate(R.layout.fragment_book_notes_view, container, false);
    context = view.getContext();

    setupRecyclerView(bookId);

    TextView bookTitleView = view.findViewById(R.id.text_view_book);

    bookTitleView.setText(bookTitle);

    hiddenDeletePanel = view.findViewById(R.id.hidden_delete_panel_book_notes);
    setupDeleteListener();
    setHasOptionsMenu(true);
    setupAddButton();
    updateEmptyView(noteList);
    ((MainActivity) requireActivity()).updateHeaderFragment(bookTitle);
    selectedNoteItems = new ArrayList<>();

    return view;
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_book_note_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();
    if (itemId == R.id.menu_export_note) {
      // TODO Silvia Export
      // handleExportLibrary();
      Toast.makeText(getContext(), String.valueOf(R.string.export_clicked), Toast.LENGTH_SHORT)
          .show();
    } else if (itemId == R.id.menu_help_book_note) {
      handleManualBookNotes();
    } else {
      Toast.makeText(getContext(), String.valueOf(R.string.error), Toast.LENGTH_SHORT).show();
    }

    return super.onOptionsItemSelected(item);
  }

  private void handleDeleteNote() {
    AlertDialog.Builder alertDeleteBookNote = new AlertDialog.Builder(context);

    alertDeleteBookNote.setCancelable(false);
    alertDeleteBookNote.setTitle(R.string.delete_note);
    alertDeleteBookNote.setMessage(R.string.delete_note_message);

    alertDeleteBookNote.setNegativeButton(R.string.back, (dialog, which) -> deselectNoteItems());

    alertDeleteBookNote.setPositiveButton(R.string.delete, (dialog, which) -> {
      bookNotesViewModel.deleteNotes(selectedNoteItems);
      adapter.notifyDataSetChanged();
      updateEmptyView(bookNotesViewModel.getCurrentNoteList());
      Toast.makeText(context, getString(R.string.deleted_notes), Toast.LENGTH_SHORT).show();
    });

    alertDeleteBookNote.show();
  }

  private void deselectNoteItems() {
    RecyclerView bookNotesListView = requireView().findViewById(R.id.book_notes_view_recycler_view);
    for (int i = 0; i < bookNotesListView.getChildCount(); i++) {
      bookNotesListView.getChildAt(i).setSelected(false);
    }
  }

  private void handleManualBookNotes() {
    HelpFragment helpFragment = new HelpFragment();
    String htmlAsString = getString(R.string.book_note_help_text);

    Bundle bundle = new Bundle();

    bundle.putString(LibraryKeys.MANUAL_TEXT, htmlAsString);
    helpFragment.setArguments(bundle);

    requireActivity().getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, helpFragment,
            LibraryKeys.FRAGMENT_HELP_VIEW)
        .addToBackStack(null)
        .commit();
  }

  private void setupAddButton() {
    View addButtonView = view.findViewById(R.id.btn_add_note);
    PopupMenu pm = new PopupMenu(getContext(), addButtonView);
    pm.getMenuInflater().inflate(R.menu.add_note_menu, pm.getMenu());

    pm.setOnMenuItemClickListener(item -> {
      if (item.getItemId() == R.id.add_text_note) {
        Bundle bundle = new Bundle();
        bundle.putLong(LibraryKeys.BOOK_ID, bookId);
        TextNoteEditorFragment nextFrag = new TextNoteEditorFragment();
        nextFrag.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container_view, nextFrag,
                LibraryKeys.FRAGMENT_TEXT_NOTE_EDITOR)
            .addToBackStack(null)
            .commit();

      } else if (item.getItemId() == R.id.add_voice_note) {
        Bundle bundle = new Bundle();
        bundle.putLong(LibraryKeys.BOOK_ID, bookId);
        VoiceNoteEditorFragment nextFrag = new VoiceNoteEditorFragment();
        nextFrag.setArguments(bundle);
        requireActivity().getSupportFragmentManager().beginTransaction()
            .replace(R.id.fragment_container_view, nextFrag,
                LibraryKeys.FRAGMENT_VOICE_NOTE_EDITOR)
            .addToBackStack(null)
            .commit();
      }
      /*
        else if (item.getItemId() == R.id.add_voice_note) {
            TODO: add features to add image notes
        }*/

      return true;
    });

    addButtonView.setOnClickListener(v -> pm.show());

  }

  private Bundle createBookBundle() {
    Bundle bundle = new Bundle();

    bundle.putLong(LibraryKeys.SHELF_ID, shelfId);
    bundle.putString(LibraryKeys.SHELF_NAME, shelfName);

    return bundle;
  }

  private Bundle createNoteBundle(NoteItem item) {
    Bundle bundle = new Bundle();
    Long currentNoteId = item.getId();
    bundle.putLong(LibraryKeys.BOOK_ID, bookId);
    bundle.putLong(LibraryKeys.NOTE_ID, currentNoteId);
    return bundle;
  }

  private void setupRecyclerView(Long bookId) {
    bookNotesViewModel = new BookNotesViewModel(getContext());
    noteList = bookNotesViewModel.getNoteList(bookId);

    RecyclerView bookNotesRecyclerView =
        view.findViewById(R.id.book_notes_view_recycler_view);
    adapter = new BookNotesRecyclerViewAdapter(noteList, this, context);
    bookNotesRecyclerView.setAdapter(adapter);

    updateEmptyView(noteList);
  }

  private void updateEmptyView(List<NoteItem> noteList) {
    TextView emptyView = view.findViewById(R.id.empty_notelist_view);
    if (noteList.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
    } else {
      emptyView.setVisibility(View.GONE);
    }
  }

  @Override
  public void onItemClicked(int position) {
    NoteItem noteItem = bookNotesViewModel.getSelectedNoteItem(position);
    Fragment nextFrag = new TextNoteEditorFragment();
    if (noteItem.getImage() == R.drawable.document) {
      nextFrag.setArguments(createNoteBundle(noteItem));
      requireActivity().getSupportFragmentManager().beginTransaction()
          .replace(R.id.fragment_container_view, nextFrag, LibraryKeys.FRAGMENT_TEXT_NOTE_EDITOR)
          .addToBackStack(null)
          .commit();
    }
  }

  @Override
  public void onLongItemClicked(int position, NoteItem noteItem, View v) {
    if (v.isSelected()) {
      v.setSelected(false);
      selectedNoteItems.remove(noteItem);
    } else {
      v.setSelected(true);
      selectedNoteItems.add(noteItem);
    }
    slideUpOrDown();
  }

  /**
   * Method to perform an upside-down animation for the deletePanel.
   */
  public void slideUpOrDown() {
    if (anyItemSelected() && !isPanelShown()) {
      Animation bottomUp = AnimationUtils.loadAnimation(context,
          R.anim.bottom_up);
      hiddenDeletePanel.startAnimation(bottomUp);
      hiddenDeletePanel.setVisibility(View.VISIBLE);
    } else if (!anyItemSelected() && isPanelShown()) {
      hidePanel();
    }
  }

  private void setupDeleteListener() {
    hiddenDeletePanel.setOnClickListener(v -> {
      handleDeleteNote();
    });
  }


  private void hidePanel() {
    Animation bottomDown = AnimationUtils.loadAnimation(context,
        R.anim.bottom_down);
    hiddenDeletePanel.startAnimation(bottomDown);
    hiddenDeletePanel.setVisibility(View.GONE);
  }

  private boolean anyItemSelected() {
    RecyclerView bookNotesRecyclerView =
        view.findViewById(R.id.book_notes_view_recycler_view);
    int itemNumber = bookNotesRecyclerView.getChildCount();
    for (int i = 0; i < itemNumber; i++) {
      if (bookNotesRecyclerView.getChildAt(i).isSelected()) {
        return true;
      }
    }
    return false;
  }

  private boolean isPanelShown() {
    return hiddenDeletePanel.getVisibility() == View.VISIBLE;
  }

}
