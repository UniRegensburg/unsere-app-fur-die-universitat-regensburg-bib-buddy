package de.bibbuddy;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.action.ViewActions;
import androidx.test.espresso.contrib.RecyclerViewActions;
import androidx.test.espresso.matcher.ViewMatchers;
import java.util.Objects;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

public class NotesFragmentTest {

  static NotesFragment nF;
  private static View itemView;
  private final String exampleText = "text";
  private String modDate;
  private String name;
  private int idText;

  /**
   * This method initiates the basic NotesFragment setup before every test-case.
   */
  @Before
  public void init() {
    ActivityScenario scenario = ActivityScenario.launch(MainActivity.class);
    onView(withId(R.id.navigation_notes)).perform(click());
    onView(withId(R.id.fragment_notes)).check(matches(isDisplayed()));
    ActivityScenario activityScenario = scenario.onActivity(
        (ActivityScenario.ActivityAction<MainActivity>) activity -> {
          nF = (NotesFragment) activity.getSupportFragmentManager().getFragments().get(0);
          NotesFragment.noteList.clear();
          NotesFragment.noteList.size();
          Note textNote =
              new Note(exampleText, 0, exampleText);
          name = textNote.getName();
          NoteTextItem noteTextItem =
              new NoteTextItem(textNote.getModDate(), name, textNote.getId());
          NotesFragment.noteList.add(noteTextItem);
        });

    onView(ViewMatchers.withId(R.id.note_list_recycler_view))
        .perform(RecyclerViewActions.scrollTo(hasDescendant(withText(exampleText))));
    RecyclerView recyclerView =
        Objects.requireNonNull(nF.getView()).findViewById(R.id.note_list_recycler_view);
    itemView = recyclerView.getChildAt(0);
    idText = itemView.getId();
  }

  @Test
  public void notesFragmentIsDisplayed_Test() {
    onView(withId(R.id.fragment_notes)).check(matches(isDisplayed()));
  }

  @Test
  public void noteListIsDisplayed_Test() {
    ViewActions.closeSoftKeyboard();
    onView(withId(idText)).check(matches(isDisplayed()));
    onView(withId(R.id.noteModDate)).check(matches(isDisplayed()));
    onView(withId(R.id.noteName)).check(matches(isDisplayed()));
    onView(withId(R.id.noteType)).check(matches(isDisplayed()));
    new DrawableMatcher(R.drawable.document)
        .matchesSafely(itemView);
  }

  @Test
  public void editorIsOpenedOnItemClick_Test() {
    ViewActions.closeSoftKeyboard();
    onView(withId(idText)).perform(click());
    onView(withId(R.id.fragment_text_note_editor)).check(matches(isDisplayed()));
  }

  @Test
  public void itemIsDeletedOnItemSwipeLeft_Test() {
    onView(withId(idText)).perform(swipeLeft());
    new DrawableMatcher(R.color.alert_red).matchesSafely(itemView);
    new DrawableMatcher(R.drawable.delete).matchesSafely(itemView);
  }

  @Test
  public void deleteSnackbarIsDisplayedOnSwipeLeft_Test() {
    onView(withId(idText)).perform(swipeLeft());
    onView(withText(R.string.delete_notification)).check(matches(isDisplayed()));
    onView(withText(R.string.undo)).check(matches(isDisplayed()));
  }

  @Test
  public void itemViewIsRestoredOnUndoClick_Test() {
    onView(withId(idText)).perform(swipeLeft());
    onView(withText(R.string.undo)).perform(click());
    onView(withId(R.id.noteType)).check(matches(isDisplayed()));
    onView(withId(R.id.noteName)).check(matches(isDisplayed()));
    onView(withId(R.id.noteType)).check(matches(isDisplayed()));
    ViewActions.closeSoftKeyboard();
  }

  public static class DrawableMatcher extends TypeSafeMatcher<View> {

    private final int expectedId;
    String resourceName;

    public DrawableMatcher(int expectedId) {
      super(View.class);
      this.expectedId = expectedId;
    }

    @Override
    protected boolean matchesSafely(View target) {
      if (!(target instanceof ImageView)) {
        return false;
      }
      ImageView imageView = (ImageView) target;
      if (expectedId < 0) {
        return imageView.getDrawable() == null;
      }
      Resources resources = target.getContext().getResources();
      Drawable expectedDrawable = resources.getDrawable(expectedId, Objects
          .requireNonNull(nF.getContext()).getTheme());
      resourceName = resources.getResourceEntryName(expectedId);

      if (expectedDrawable == null) {
        return false;
      }

      Drawable drawable = imageView.getDrawable();
      return drawable.equals(expectedDrawable);
    }

    @Override
    public void describeTo(Description description) {
      description.appendText("with drawable from resource id: ");
      description.appendValue(expectedId);
      if (resourceName != null) {
        description.appendText("[");
        description.appendText(resourceName);
        description.appendText("]");
      }
    }
  }
}

