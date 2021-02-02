package de.bibbuddy;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertSame;


import android.content.Context;
import android.view.Menu;
import android.view.View;
import android.widget.PopupMenu;
import androidx.fragment.app.testing.FragmentScenario;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import java.util.Date;
import java.util.List;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.robolectric.RobolectricTestRunner;
import org.robolectric.shadows.ShadowPopupMenu;


@RunWith(RobolectricTestRunner.class)

public class NotesFragmentTest {

	private List<Note> notes;
	private NoteDAO noteDao;
	private RecyclerViewAdapter adapter;
	private RecyclerView recyclerView;
	private MainActivity mainActivity;
	private NotesFragment notesFragment;
	private Context context;

	@Before
	public void setUp() {
		ActivityScenario.launch(MainActivity.class).onActivity(
			activity -> mainActivity = activity);
		FragmentScenario<NotesFragment> frScenario = FragmentScenario.launch(NotesFragment.class);
		frScenario.onFragment(fragment -> {
			notesFragment = fragment;
			context = fragment.getContext();
			recyclerView = notesFragment.getView().findViewById(R.id.recyclerView);
			Long currentDate = new Date().getTime();
			Note note = new Note("", 0, "", currentDate, currentDate, (long) 1);
			DatabaseHelper databaseHelper = new DatabaseHelper(context);
			noteDao = new NoteDAO(databaseHelper);
			noteDao.create(note);
			notes = noteDao.findAll();
		});
	}

	@Test
	public void Database_IsConnected() {
		Assert.assertNotNull(noteDao);
		assertEquals(1, noteDao.findAll().size());
		assertSame(noteDao.findAll().get(0).getId(), notes.get(0).getId());
	}

	@Test
	public void RecyclerViewAdapter_Works() {
		adapter = new RecyclerViewAdapter(notes, mainActivity);
		recyclerView.setAdapter(adapter);
		Assert.assertSame(recyclerView.getAdapter(), adapter);
		assertEquals(adapter.getData(), notes);
	}

	@Test
	public void SwipeToDeleteCallback_IsSetup() {
		SwipeToDeleteCallback swipeToDeleteCallback =
			new SwipeToDeleteCallback(context, adapter, recyclerView);
		assertNotNull(swipeToDeleteCallback);
		ItemTouchHelper itemTouchhelper = new ItemTouchHelper(swipeToDeleteCallback);
		itemTouchhelper.attachToRecyclerView(recyclerView);
		assertNotNull(itemTouchhelper);
	}

	@Test
	public void AddButton_Works() {
		View addButtonView = recyclerView.getRootView().findViewById(R.id.addButton);
		Assert.assertNotNull(addButtonView);
		addButtonView.performClick();
		PopupMenu latestPopupMenu = ShadowPopupMenu.getLatestPopupMenu();
		Menu menu = latestPopupMenu.getMenu();
		Assert.assertNotNull(menu);
		addButtonView.performClick();
		verifyMenuContent(menu);
		menuItemsClicks_Work(menu);
	}

	private void verifyMenuContent(Menu menu) {
		assertEquals(3, menu.size());
		assertEquals(R.id.add_text_note, menu.getItem(0).getItemId());
		assertEquals(R.id.add_voice_note, menu.getItem(1).getItemId());
		assertEquals(R.id.add_picture_note, menu.getItem(2).getItemId());
	}


	private void menuItemsClicks_Work(Menu menu) {
		menu.performIdentifierAction(R.id.add_text_note, 0);
		menu.performIdentifierAction(R.id.add_voice_note, 0);
		menu.performIdentifierAction(R.id.add_picture_note, 0);
	}

}