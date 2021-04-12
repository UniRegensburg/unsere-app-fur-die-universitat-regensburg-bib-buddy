package de.bibbuddy;

import android.Manifest;
import android.app.AlertDialog;
import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.net.Uri;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageButton;
import android.widget.PopupMenu;
import android.widget.TextView;
import android.widget.Toast;
import androidx.activity.result.ActivityResultLauncher;
import androidx.activity.result.contract.ActivityResultContracts;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.app.ShareCompat;
import androidx.core.content.ContextCompat;
import com.tsuryo.swipeablerv.SwipeLeftRightCallback;
import com.tsuryo.swipeablerv.SwipeableRecyclerView;
import java.util.Collections;
import java.util.List;

/**
 * BookNotesView is responsible for the noteList of a certain book.
 *
 * @author Sarah Kurek, Silvia Ivanova, Luis Moßburger
 */
public class BookNotesView extends BackStackFragment implements SwipeLeftRightCallback.Listener {

  private View view;
  private Context context;
  private BookNotesViewModel bookNotesViewModel;
  private NoteRecyclerViewAdapter adapter;
  private Long bookId;
  private ActivityResultLauncher<String> requestPermissionLauncher;

  private BookModel bookModel;
  private NoteModel noteModel;

  private ExportBibTex exportBibTex;
  private SortCriteria sortCriteria;

  @Override
  protected void onBackPressed() {
    if (adapter.getSelectedNoteItems().isEmpty()) {
      closeFragment();
    } else {
      deselectNoteItems();
    }
  }

  @Override
  public void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setupPermissionLauncher();
  }

  /**
   * Register permissions callback, which handles the user's response to the
   * system permissions dialog. Save the return value, an instance of
   * ActivityResultLauncher, as an instance variable.
   */
  private void setupPermissionLauncher() {
    requestPermissionLauncher =
        registerForActivityResult(new ActivityResultContracts.RequestPermission(), isGranted -> {

          if (isGranted) {
            Bundle bundle = new Bundle();
            bundle.putLong(LibraryKeys.BOOK_ID, bookId);
            VoiceNoteEditorFragment nextFrag = new VoiceNoteEditorFragment();
            nextFrag.setArguments(bundle);

            showFragment(nextFrag, LibraryKeys.FRAGMENT_VOICE_NOTE_EDITOR);
          }
        });
  }

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    enableBackPressedHandler();

    view = inflater.inflate(R.layout.fragment_book_notes, container, false);
    context = view.getContext();
    bookNotesViewModel = new BookNotesViewModel(context);

    MainActivity mainActivity = (MainActivity) requireActivity();
    sortCriteria = mainActivity.getSortCriteria();
    mainActivity.updateNavigationFragment(R.id.navigation_library);
    mainActivity.setVisibilityImportShareButton(View.GONE, View.VISIBLE);

    Bundle bundle = this.getArguments();
    if (bundle != null) {
      bookId = bundle.getLong(LibraryKeys.BOOK_ID);
    }

    bookModel = new BookModel(requireContext(), getShelfId());
    noteModel = new NoteModel(requireContext());

    String fileName = (bookModel.getBookById(bookId).getTitle()
        + bookModel.getBookById(bookId).getPubYear())
        .replaceAll("\\s+", "");
    exportBibTex = new ExportBibTex(fileName);

    setupRecyclerView(bookId);
    setupSortBtn();
    setHasOptionsMenu(true);
    setupAddButton();
    updateBookNoteList(adapter.getNoteList());
    setFunctionsToolbar();
    fillBookData();

    return view;
  }

  private Long getShelfId() {
    Bundle bundle = this.getArguments();
    return bundle.getLong(LibraryKeys.SHELF_ID);
  }

  private void setupSortBtn() {
    ImageButton sortBtn = requireActivity().findViewById(R.id.sort_btn);
    ((MainActivity) requireActivity()).setVisibilitySortButton(true);
    sortBtn.setOnClickListener(v -> handleSortNote());
  }

  private void setFunctionsToolbar() {
    ((MainActivity) requireActivity()).shareBtn.setOnClickListener(view -> checkEmptyNoteList());
  }

  @Override
  public void onCreateOptionsMenu(@NonNull Menu menu, MenuInflater inflater) {
    inflater.inflate(R.menu.fragment_book_note_menu, menu);
    super.onCreateOptionsMenu(menu, inflater);
  }

  @Override
  public boolean onOptionsItemSelected(MenuItem item) {
    int itemId = item.getItemId();

    if (itemId == R.id.menu_delete_note) {
      handleDeleteNote(adapter.getSelectedNoteItems());
    } else if (itemId == R.id.menu_help_book_note) {
      handleManualBookNotes();
    } else if (itemId == R.id.menu_imprint) {
      ((MainActivity) requireActivity()).openImprint();
    }

    return super.onOptionsItemSelected(item);
  }

  @Override
  public void onPrepareOptionsMenu(Menu menu) {
    MenuItem deleteNote = menu.findItem(R.id.menu_delete_note);
    deleteNote.setVisible(adapter.getSelectedNoteItems().size() > 0);
  }

  private void handleDeleteNote(List<NoteItem> selectedItems) {
    AlertDialog.Builder alertDeleteBookNote = new AlertDialog.Builder(context);
    alertDeleteBookNote.setCancelable(false);

    if (selectedItems.size() > 1) {
      alertDeleteBookNote.setTitle(R.string.delete_notes);
      alertDeleteBookNote.setMessage(
          getString(R.string.delete_notes_message) + assembleAlertString(selectedItems));
    } else {
      alertDeleteBookNote.setTitle(R.string.delete_note);
      alertDeleteBookNote.setMessage(
          getString(R.string.delete_note_message) + assembleAlertString(selectedItems));
    }

    alertDeleteBookNote.setNegativeButton(R.string.cancel, (dialog, which) -> deselectNoteItems());

    alertDeleteBookNote
        .setPositiveButton(R.string.delete, (dialog, which) -> performDeleteNotes(selectedItems));

    alertDeleteBookNote.show();
  }

  private String assembleAlertString(List<NoteItem> selectedItems) {
    return convertNoteListToString(selectedItems)
        + getString(R.string.finally_delete) + " "
        + getString(R.string.delete_warning);
  }

  private String convertNoteListToString(List<NoteItem> noteList) {
    StringBuilder notes = new StringBuilder();

    int counter = 1;
    for (NoteItem note : noteList) {
      notes.append(" \"").append(note.getName()).append("\"");

      if (counter != noteList.size()) {
        notes.append(",");
      }

      notes.append(" ");
      ++counter;
    }
    return notes.toString();
  }

  private void performDeleteNotes(List<NoteItem> itemsToDelete) {
    deselectNoteItems();

    bookNotesViewModel.deleteNotes(itemsToDelete);
    adapter.setNoteList(bookNotesViewModel.getBookNoteList(bookId));
    adapter.notifyDataSetChanged();

    if (itemsToDelete.size() > 1) {
      Toast.makeText(context, getString(R.string.deleted_notes), Toast.LENGTH_SHORT).show();
    } else {
      Toast.makeText(context, getString(R.string.deleted_note), Toast.LENGTH_SHORT).show();
    }

    updateBookNoteList(adapter.getNoteList());
  }

  private void deselectNoteItems() {
    SwipeableRecyclerView bookNotesListView =
        requireView().findViewById(R.id.book_notes_recycler_view);
    for (int i = 0; i < bookNotesListView.getChildCount(); i++) {
      bookNotesListView.getChildAt(i).setSelected(false);
    }
  }

  private void handleSortNote() {
    SortDialog sortDialog = new SortDialog(context, sortCriteria,
        newSortCriteria -> {
          sortCriteria = newSortCriteria;
          ((MainActivity) requireActivity()).setSortCriteria(newSortCriteria);
          sortNoteList();
        });

    sortDialog.show();
  }

  private void sortNoteList() {
    List<NoteItem> noteList = bookNotesViewModel.getSortedNoteList(sortCriteria, bookId);
    adapter.setNoteList(noteList);
  }

  private void checkEmptyNoteList() {
    if (bookNotesViewModel.getBookNoteList(bookId).isEmpty()) {
      AlertDialog.Builder alertDialogEmptyLib = new AlertDialog.Builder(getContext());
      alertDialogEmptyLib.setTitle(R.string.empty_note_list);
      alertDialogEmptyLib.setMessage(R.string.empty_note_list_description);

      alertDialogEmptyLib.setPositiveButton(R.string.ok,
          (dialog, which) -> {
          });

      alertDialogEmptyLib.create().show();

    } else {
      shareBookNoteBibIntent();
    }

  }

  private void handleManualBookNotes() {
    HelpFragment helpFragment = new HelpFragment();
    String htmlAsString = getString(R.string.book_note_help_text);

    Bundle bundle = new Bundle();

    bundle.putString(LibraryKeys.MANUAL_TEXT, htmlAsString);
    helpFragment.setArguments(bundle);

    showFragment(helpFragment, LibraryKeys.FRAGMENT_HELP_VIEW);
  }

  private void setupAddButton() {
    View addButtonView = view.findViewById(R.id.add_btn);
    PopupMenu pm = new PopupMenu(getContext(), addButtonView);
    pm.getMenuInflater().inflate(R.menu.add_note_menu, pm.getMenu());

    pm.setOnMenuItemClickListener(item -> {

      if (item.getItemId() == R.id.add_text_note) {
        Bundle bundle = new Bundle();
        bundle.putLong(LibraryKeys.BOOK_ID, bookId);
        TextNoteEditorFragment textFrag = new TextNoteEditorFragment();
        textFrag.setArguments(bundle);

        showFragment(textFrag, LibraryKeys.FRAGMENT_TEXT_NOTE_EDITOR);
      } else {
        checkRecordPermission();
      }

      return true;
    });

    addButtonView.setOnClickListener(v -> pm.show());
  }

  private void checkRecordPermission() {
    if (ContextCompat.checkSelfPermission(
        context, Manifest.permission.RECORD_AUDIO) == PackageManager.PERMISSION_GRANTED) {

      Bundle bundle = new Bundle();
      bundle.putLong(LibraryKeys.BOOK_ID, bookId);
      VoiceNoteEditorFragment voiceFrag = new VoiceNoteEditorFragment();
      voiceFrag.setArguments(bundle);

      showFragment(voiceFrag, LibraryKeys.FRAGMENT_VOICE_NOTE_EDITOR);
    } else if (shouldShowRequestPermissionRationale(Manifest.permission.RECORD_AUDIO)) {
      showAudioRecordRequest();

    } else {
      requestPermissionLauncher.launch(Manifest.permission.RECORD_AUDIO);
    }
  }

  private void showAudioRecordRequest() {
    AlertDialog.Builder reqAlertDialog = new AlertDialog.Builder(context);
    reqAlertDialog.setTitle(R.string.record_permission_needed);
    reqAlertDialog.setMessage(R.string.record_permission_alert_msg);

    reqAlertDialog.setPositiveButton(R.string.ok,
        (dialog, which) -> requestPermissionLauncher.launch(
            Manifest.permission.RECORD_AUDIO));

    reqAlertDialog.setNegativeButton(R.string.cancel,
        (dialog, which) -> dialog.dismiss());

    reqAlertDialog.create().show();
  }

  private void updateBookNoteList(List<NoteItem> noteList) {
    sortNoteList();
    updateEmptyView(noteList);
  }

  private void fillBookData() {
    Bundle bundle = this.getArguments();
    BookModel bookModel = new BookModel(context, bundle.getLong(LibraryKeys.SHELF_ID));
    Book book = bookModel.getBookById(bookId);

    TextView bookTitle = view.findViewById(R.id.book_title);
    bookTitle.setText(book.getTitle());

    TextView bookAuthors = view.findViewById(R.id.book_authors);
    bookAuthors.setText(bookModel.getAuthorString(bookId));

    TextView bookYear = view.findViewById(R.id.book_year);
    bookYear.setText(String.valueOf(book.getPubYear()));
  }

  private void setupRecyclerView(Long bookId) {
    SwipeableRecyclerView notesRecyclerView =
        view.findViewById(R.id.book_notes_recycler_view);
    adapter = new NoteRecyclerViewAdapter((MainActivity) requireActivity(),
        bookNotesViewModel.getBookNoteList(bookId),
        bookNotesViewModel.getNoteModel());

    notesRecyclerView.setAdapter(adapter);
    notesRecyclerView.setListener(this);
    updateEmptyView(adapter.getNoteList());
  }

  private void updateEmptyView(List<NoteItem> noteList) {
    TextView emptyView = view.findViewById(R.id.empty_note_list_view);

    if (noteList.isEmpty()) {
      emptyView.setVisibility(View.VISIBLE);
    } else {
      emptyView.setVisibility(View.GONE);
    }
  }

  private void shareBookNoteBibIntent() {
    String content =
        exportBibTex.getBibDataFromBook(bookId, bookModel, noteModel);
    Uri contentUri = exportBibTex.writeTemporaryBibFile(context, content);

    Intent shareBookNoteIntent =
        ShareCompat.IntentBuilder.from(requireActivity())
            .setStream(contentUri)
            .setType("text/*")
            .getIntent()
            .addFlags(Intent.FLAG_GRANT_READ_URI_PERMISSION);

    startActivity(Intent.createChooser(shareBookNoteIntent, "SEND"));

  }

  @Override
  public void onSwipedLeft(int position) {
    deselectNoteItems();
    handleDeleteNote(Collections.singletonList(adapter.getNoteList().get(position)));
    adapter.notifyDataSetChanged();

  }

  @Override
  public void onSwipedRight(int position) {
  }

}
