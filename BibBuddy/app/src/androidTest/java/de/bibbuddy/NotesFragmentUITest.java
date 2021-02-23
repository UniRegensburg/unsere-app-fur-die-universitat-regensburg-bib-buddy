package de.bibbuddy;

import static androidx.test.espresso.Espresso.onView;
import static androidx.test.espresso.action.ViewActions.click;
import static androidx.test.espresso.action.ViewActions.longClick;
import static androidx.test.espresso.action.ViewActions.swipeLeft;
import static androidx.test.espresso.assertion.ViewAssertions.doesNotExist;
import static androidx.test.espresso.assertion.ViewAssertions.matches;
import static androidx.test.espresso.matcher.ViewMatchers.hasDescendant;
import static androidx.test.espresso.matcher.ViewMatchers.isDisplayed;
import static androidx.test.espresso.matcher.ViewMatchers.withId;
import static androidx.test.espresso.matcher.ViewMatchers.withText;
import static org.hamcrest.Matchers.not;


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

public class NotesFragmentUITest {

  static NotesFragment nF;
  private static View itemView;
  private final String exampleText = "text";
  private String modDate;
  private String name;
  private int idText;
  private int idVoice;
  private int idPicture;

  @Before
  public void init() {
    ActivityScenario scenario = ActivityScenario.launch(MainActivity.class);
    onView(withId(R.id.navigation_notes)).perform(click());
    onView(withId(R.id.fragment_notes)).check(matches(isDisplayed()));
    ActivityScenario activityScenario = scenario.onActivity(
        (ActivityScenario.ActivityAction<MainActivity>) activity -> {
          nF = (NotesFragment) activity.getSupportFragmentManager().getFragments().get(0);
          NotesFragment.notes.clear();
          if (NotesFragment.notes.size() == 0) {
            Note textNote =
                new Note(exampleText, 0, exampleText);
            modDate = String.valueOf(textNote.getModDate());
            name = textNote.getName();
            NoteTextItem noteTextItem = new NoteTextItem(modDate, name, textNote.getId());
            NotesFragment.notes.add(noteTextItem);
          }
        });

    onView(ViewMatchers.withId(R.id.recyclerView))
        .perform(RecyclerViewActions.scrollTo(hasDescendant(withText(exampleText))));
    RecyclerView recyclerView =
        Objects.requireNonNull(nF.getView()).findViewById(R.id.recyclerView);
    itemView = recyclerView.getChildAt(0);
    idText = itemView.getId();
  }

  @Test
  public void NotesFragmentDisplay_Test() {
    onView(withId(R.id.fragment_notes)).check(matches(isDisplayed()));
  }

  // TODO: Needs to be adjusted as soon as more note types are added
  @Test
  public void RecyclerViewListDisplay_Test() {
    ViewActions.closeSoftKeyboard();
    onView(withId(idText)).check(matches(isDisplayed()));
    onView(withId(R.id.noteModDate)).check(matches(isDisplayed()));
    onView(withId(R.id.noteName)).check(matches(isDisplayed()));
    onView(withId(R.id.noteType)).check(matches(isDisplayed()));
    new DrawableMatcher(R.drawable.document)
        .matchesSafely(itemView);
    // onView(withId(R.id.noteType)).check(matches(withId(R.drawable.microphone));
    // onView(withId(R.id.noteType)).check(matches(withId(R.drawable.picture)));
  }

  // TODO: Needs to be adjusted as soon as more note types are added
  @Test
  public void OpenEditorOnItemClick_Test() {
    ViewActions.closeSoftKeyboard();
    onView(withId(idText)).perform(click());
    onView(withId(R.id.fragment_text_note_editor)).check(matches(isDisplayed()));
      /*
      onView(withId(id2)).perform(click());
      onView(withId(nF.getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_voice_note_editor).getId())).check(matches(isDisplayed()));
      onView(withId(id3)).perform(click());
      onView(withId(nF.getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_picture_note_editor).getId())).check(matches(isDisplayed()));
      */
  }

  @Test
  public void DeletePanelDisplay_Test() {
    ViewActions.closeSoftKeyboard();
    onView(withId(idText)).perform(longClick());
    new DrawableMatcher(R.color.flirt_light).matchesSafely(itemView);
    onView(withId(R.id.hidden_delete_panel)).check(matches(isDisplayed()));
    new DrawableMatcher(R.color.alert_red)
        .matchesSafely(itemView.getRootView().findViewById(R.id.panel_delete));
    new DrawableMatcher(R.drawable.delete)
        .matchesSafely(itemView.getRootView().findViewById(R.id.panel_delete));
    onView(withId(R.id.hidden_delete_panel)).perform(click());
    onView(withId(idText)).check(doesNotExist());
  }

  @Test
  public void DeletePanelPerformDelete_Test() {
    ViewActions.closeSoftKeyboard();
    onView(withId(idText)).perform(longClick());
    onView(withId(R.id.hidden_delete_panel)).check(matches(isDisplayed()));
    onView(withId(idText)).perform(longClick());
    onView(withId(R.id.hidden_delete_panel)).check(matches(not(isDisplayed())));
  }


  @Test
  public void OnItemSwipeLeft_Test() {
    onView(withId(idText)).perform(swipeLeft());
    new DrawableMatcher(R.color.alert_red).matchesSafely(itemView);
    new DrawableMatcher(R.drawable.delete).matchesSafely(itemView);
  }

  @Test
  public void DeleteSnackbarDisplayOnSwipeLeft_Test() {
    onView(withId(idText)).perform(swipeLeft());
    onView(withText(R.string.delete_notification)).check(matches(isDisplayed()));
    onView(withText(R.string.undo)).check(matches(isDisplayed()));
  }

  @Test
  public void RestoreItemViewOnUndo_Test() {
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

