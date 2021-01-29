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
import org.hamcrest.Description;
import org.hamcrest.TypeSafeMatcher;
import org.junit.Before;
import org.junit.Test;

public class NotesFragmentUITest {

    static NotesFragment nF;
    private static View itemView;
    private final Long exampleLong = 2147483649L;
    private final String exampleText = "text";
    private int idText;
    private int idVoice;
    private int idPicture;

    @Before
    public void init() {
        ActivityScenario scenario = ActivityScenario.launch(MainActivity.class);
        onView(withId(R.id.navigation_notes)).perform(click());
        onView(withId(R.id.fragment_notes)).check(matches(isDisplayed()));
        scenario.onActivity(
                new ActivityScenario.ActivityAction<MainActivity>() {
                    @Override
                    public void perform(MainActivity activity) {
                        nF = (NotesFragment) activity.getSupportFragmentManager().getFragments().get(0);
                        NotesFragment.notes.clear();

                        // TODO:  Needs to be adjusted as soon as more note types are added
                        Note note =
                                new Note(exampleText, 0, exampleText, exampleLong, exampleLong, exampleLong);
                        nF.noteDao.create(note);
                        NotesFragment.notes.add(note);
                        nF.adapter.notifyDataSetChanged();
                    }
                });

        onView(ViewMatchers.withId(R.id.recyclerView))
                .perform(RecyclerViewActions.scrollTo(hasDescendant(withText(exampleText))));
        RecyclerView recyclerView = nF.getView().findViewById(R.id.recyclerView);
        itemView = recyclerView.getChildAt(0);
        idText = itemView.getId();

    /*
    nF.recyclerView.getAdapter().notifyDataSetChanged();
    itemView = nF.recyclerView.getChildAt(0);
    id = itemView.getId();
    itemView = nF.recyclerView.getChildAt(1);
    id2 = itemView.getId();
    itemView = nF.recyclerView.getChildAt(2);
    id3 = itemView.getId();
    */

    }

    @Test
    public void NotesFragment_IsShown() {
        onView(withId(R.id.fragment_notes)).check(matches(isDisplayed()));
    }

    // TODO: Needs to be adjusted as soon as more note types are added
    @Test
    public void RecyclerViewList_IsShown() {
        onView(withId(idText)).check(matches(isDisplayed()));
        onView(withId(R.id.noteTitle)).check(matches(isDisplayed()));
        onView(withId(R.id.noteType)).check(matches(isDisplayed()));

        // onView(withId(R.id.noteType)).check(matches(withId(R.drawable.microphone));
        // onView(withId(R.id.noteType)).check(matches(withId(R.drawable.picture)));

    }

    // TODO: Needs to be adjusted as soon as more note types are added
    @Test
    public void addButtonAndAddMenu_AreShown() {
        onView(withId(R.id.addButton)).check(matches(isDisplayed()));
        onView(withId(R.id.addButton)).perform(click());
        onView(withText(R.string.create_text_note)).check(matches(isDisplayed()));
        onView(withText(R.string.create_voice_note)).check(matches(isDisplayed()));
        onView(withText(R.string.take_picture_note)).check(matches(isDisplayed()));
    }

    // TODO: Needs to be adjusted as soon as more note types are added
    @Test
    public void Editor_IsOpened_OnItemClick() {
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

    // TODO: Needs to be adjusted as soon as more note types are added
    @Test
    public void Editor_IsOpened_OnAddButtonClick() {
        onView(withId(R.id.addButton)).perform(click());
        onView(withText(R.string.create_text_note)).perform(click());
        onView(withId(R.id.fragment_text_note_editor)).check(matches(isDisplayed()));
        ViewActions.closeSoftKeyboard();

    /*
    onView(withText(nF.getString(R.string.create_voice_note))).perform(click());
    onView(withId(nF.getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_voice_note_editor).getId())).check(matches(isDisplayed()));
    onView(withText(nF.getString(R.string.create_picture_note))).perform(click());
    onView(withId(nF.getActivity().getSupportFragmentManager().findFragmentById(R.id.fragment_picture_note_editor).getId())).check(matches(isDisplayed()));
     */

    }

    @Test
    public void Background_Changed_AndDeleteIcon_IsShown_OnSwipeLeft() {
        onView(withId(idText)).perform(swipeLeft());
        new DrawableMatcher(R.color.alert_red).matchesSafely(itemView);
        new DrawableMatcher(R.drawable.delete).matchesSafely(itemView);
    }

    @Test
    public void DeleteSnackbar_IsShown_OnSwipeLeft() {
        onView(withId(idText)).perform(swipeLeft());
        onView(withText(R.string.delete_notification)).check(matches(isDisplayed()));
        onView(withText(R.string.undo)).check(matches(isDisplayed()));
    }

    @Test
    public void OnUndo_RestoreItemView() {
        onView(withId(idText)).perform(swipeLeft());
        onView(withText(R.string.undo)).perform(click());
        onView(withId(R.id.noteType)).check(matches(isDisplayed()));
        onView(withId(R.id.noteTitle)).check(matches(isDisplayed()));
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
            Drawable expectedDrawable = resources.getDrawable(expectedId, nF.getContext().getTheme());
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
