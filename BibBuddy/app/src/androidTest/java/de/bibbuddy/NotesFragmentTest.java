package de.bibbuddy;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.Espresso.openActionBarOverflowOrOptionsMenu;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;


import android.content.res.Resources;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.ImageView;
import androidx.recyclerview.widget.RecyclerView;
import androidx.test.core.app.ActivityScenario;
import androidx.test.espresso.matcher.ViewMatchers;
import java.util.Objects;
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

/**
 * NotesFragmentTest is responsible for the UI-test of the NotesFragment-class.
 *
 * @author Sabrina.
 */
public class NotesFragmentTest {

  static NotesFragment nF;
  private static View itemView;
  private int itemViewId;

  /**
   * Initiate basic NotesFragment' setup before every test-case.
   */
  @Before
  public void init() {
    ActivityScenario scenario = ActivityScenario.launch(MainActivity.class);
    onView(withId(R.id.navigation_notes)).perform(click());
    scenario.onActivity(
        (ActivityScenario.ActivityAction<MainActivity>) activity -> {
          nF = (NotesFragment) activity.getSupportFragmentManager().getFragments().get(0);

          if (nF.noteList.size() != 0) {
            RecyclerView recyclerView = nF.getView().findViewById(R.id.note_list_recycler_view);
            itemView = recyclerView.getChildAt(0);
            itemViewId = itemView.getId();
          }
        });
  }

  @Test
  public void headerTextIsCorrect_Test() {
    onView(withId(R.id.headerText)).check(matches(withText(R.string.navigation_notes)));
  }

  @Test
  public void headerButtonsVisibilities_Test() {
    onView(withId(R.id.import_btn))
        .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    onView(withId(R.id.share_btn))
        .check(matches(ViewMatchers.withEffectiveVisibility(ViewMatchers.Visibility.GONE)));
    onView(withId(R.id.sort_btn)).check(matches(isDisplayed()));
  }

  @Test
  public void optionsMenuHelpWorks_Test() {
    openActionBarOverflowOrOptionsMenu(nF.requireActivity());
    onView(withText(R.string.help)).perform(click());
    onView(withId(R.id.fragment_help)).check(matches(isDisplayed()));
  }

  @Test
  public void optionsMenuImprintWorks_Test() {
    openActionBarOverflowOrOptionsMenu(nF.requireActivity());
    onView(withText(R.string.header_imprint)).perform(click());
    onView(withId(R.id.fragment_imprint)).check(matches(isDisplayed()));
  }

  @Test
  public void optionsMenuDeleteWorks_Test() {
    if (itemView != null) {
      itemView.setSelected(true);
      openActionBarOverflowOrOptionsMenu(nF.requireActivity());
      onView(withText(R.string.delete)).perform(click());
      onView(withText(R.string.delete_note)).check(matches(isDisplayed()));
    }
  }

  @Test
  public void notesFragmentIsDisplayed_Test() {
    onView(withId(R.id.fragment_notes)).check(matches(isDisplayed()));
  }

  @Test
  public void recyclerViewWorks_Test() {
    if (itemView != null) {
      onView(withId(R.id.note_list_recycler_view)).check(matches(isDisplayed()));
    }
  }

  @Test
  public void noteItemIsDisplayedCorrectly_Test() {
    if (itemView != null) {
      onView(withId(itemViewId)).check(matches(isDisplayed()));

      int modDateId = itemView.findViewById(R.id.note_mod_date).getId();
      onView(withId(modDateId)).check(matches(isDisplayed()));

      if (nF.noteList.get(nF.noteList.size() - 1).getType() == NoteTypeLut.TEXT) {
        int nameId = itemView.findViewById(R.id.note_name).getId();
        onView(withId(nameId)).check(matches(isDisplayed()));

        new DrawableMatcher(R.drawable.document)
            .matchesSafely(itemView);
      } else {
        new DrawableMatcher(R.drawable.microphone)
            .matchesSafely(itemView);

        onView(withId(R.id.seekBar)).check(matches(isDisplayed()));
        onView(withId(R.id.play_note)).check(matches(isDisplayed()));
        onView(withId(R.id.stop_note)).check(matches(isDisplayed()));
        onView(withId(R.id.played_time)).check(matches(isDisplayed()));
        onView(withId(R.id.total_time)).check(matches(isDisplayed()));
      }
    }
  }

  @Test
  public void emptyNoteListViewIsDisplayedCorrectly_Test() {
    if (itemView == null) {
      onView(withId(R.id.empty_notes_list_view)).check(matches(isDisplayed()));
    }
  }

  @Test
  public void textNoteEditorIsOpenedOnItemClick_Test() {
    if (itemView != null) {
      if (nF.noteList.get(nF.noteList.size() - 1).getType() == NoteTypeLut.TEXT) {
        onView(withId(itemViewId)).perform(click());
        onView(withId(R.id.fragment_text_note_editor)).check(matches(isDisplayed()));
      }
    }
  }

  @Test
  public void deleteSnackbarIsDisplayedOnSwipeLeft_Test() {
    if (itemView != null) {
      onView(withId(itemViewId)).perform(swipeLeft());
      onView(withText(R.string.delete_note)).check(matches(isDisplayed()));
    }
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


