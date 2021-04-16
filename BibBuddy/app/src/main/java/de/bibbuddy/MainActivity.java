package de.bibbuddy;

import android.annotation.SuppressLint;
import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

  private final String homeFragmentTag = "home";
  private final String searchFragmentTag = "search";
  private final String libraryFragmentTag = "library";
  private final String notesFragmentTag = "notes";
  private final String imprintFragmentTag = "imprint";

  private String welcomeMessage = "";
  private HomeFragment homeFragment;
  private SearchFragment searchFragment;
  private LibraryFragment libraryFragment;
  private NotesFragment notesFragment;
  private ImprintFragment imprintFragment;
  private AsDefaultAppFragment defaultAppFragment;

  private SortTypeLut sortTypeLut;
  private boolean[] filterCriteria;
  private String searchText;

  private boolean isDefaultSelected;
  private Uri uri;

  private void setupDefaultAppSelected() {
    Intent defaultAppIntent = getIntent();
    String action = defaultAppIntent.getAction();

    resetIsDefaultApp();

    if (action.compareTo(Intent.ACTION_VIEW) == 0) {
      String scheme = defaultAppIntent.getScheme();

      if (scheme.compareTo(ContentResolver.SCHEME_CONTENT) == 0
          || scheme.compareTo(ContentResolver.SCHEME_FILE) == 0) {

        isDefaultSelected = true;
        uri = defaultAppIntent.getData();
        switchToDefaultAppFragment();
      }
    }

  }

  private void switchToDefaultAppFragment() {
    if (defaultAppFragment == null) {
      defaultAppFragment = new AsDefaultAppFragment();
    }

    String defaultAppFragmentTag = "defaultAppSelected";
    updateFragment(defaultAppFragment, defaultAppFragmentTag);
  }

  @SuppressLint("NonConstantResourceId")
  private void setupBottomNavigationView() {
    BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);

    bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
      switch (item.getItemId()) {
        case R.id.navigation_home:
          if (homeFragment == null) {
            homeFragment = new HomeFragment();
          }

          resetIsDefaultApp();
          updateFragment(homeFragment, homeFragmentTag);
          break;

        case R.id.navigation_search:
          if (searchFragment == null) {
            searchFragment = new SearchFragment();
          }

          resetIsDefaultApp();
          updateFragment(searchFragment, searchFragmentTag);
          break;

        case R.id.navigation_library:
          if (libraryFragment == null) {
            libraryFragment = new LibraryFragment();
          }

          resetIsDefaultApp();
          updateFragment(libraryFragment, libraryFragmentTag);
          break;

        case R.id.navigation_notes:
          if (notesFragment == null) {
            notesFragment = new NotesFragment();
          }

          resetIsDefaultApp();
          updateFragment(notesFragment, notesFragmentTag);
          break;

        default:
          throw new IllegalArgumentException();
      }

      return true;
    });
  }

  private void setupLogoBtn() {
    ImageButton logoBtn = findViewById(R.id.headerLogo);
    logoBtn.setOnClickListener(v -> {
      if (homeFragment == null) {
        homeFragment = new HomeFragment();
      }

      updateFragment(homeFragment, homeFragmentTag);
    });
  }

  private void updateFragment(Fragment fragment, String tag) {
    getSupportFragmentManager().beginTransaction()
        .replace(R.id.fragment_container_view, fragment, tag)
        .setReorderingAllowed(true)
        .addToBackStack(null)
        .commit();

    updateHeader(tag);
  }

  private void updateHeader(String tag) {
    // Changes header text according to fragment
    View headerTextView = findViewById(R.id.headerText);
    TextView headerText = (TextView) headerTextView;
    switch (tag) {
      case homeFragmentTag:
        headerText.setText(getString(R.string.navigation_home));
        break;
      case searchFragmentTag:
        headerText.setText(getString(R.string.navigation_search));
        break;
      case libraryFragmentTag:
        headerText.setText(getString(R.string.navigation_library));
        break;
      case notesFragmentTag:
        headerText.setText(getString(R.string.navigation_notes));
        break;
      case imprintFragmentTag:
        headerText.setText(R.string.header_imprint);
        break;

      default:
        headerText.setText(getString(R.string.app_name));
    }
  }

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    if (savedInstanceState == null) {
      homeFragment = new HomeFragment();
      updateFragment(homeFragment, homeFragmentTag);
      setupDefaultAppSelected();
    }

    setupLogoBtn();
    setupBottomNavigationView();

    sortTypeLut = SortTypeLut.MOD_DATE_LATEST;
    filterCriteria = new boolean[] {true, true, true}; // shelf, book, note
    searchText = "";
  }

  /**
   * Checks if the BibBuddy-App is selected as
   * default app for opening a certain files.
   *
   * @return true if BibBuddy-App is selected as default
   *         false if BibBuddy-App is not selected as default
   */
  public boolean isDefaultApp() {
    return isDefaultSelected;
  }

  /**
   * Gets the Uri from a file when the
   * BibBuddy-App is selected as default.
   *
   * @return Uri of selected file
   */
  public Uri getUriDefaultApp() {
    return uri;
  }

  public void resetIsDefaultApp() {
    isDefaultSelected = false;
  }

  public void updateHeaderFragment(String name) {
    TextView headerView = findViewById(R.id.headerText);
    headerView.setText(name);
  }

  public void updateNavigationFragment(int item) {
    BottomNavigationView bottomNavigationView = findViewById(R.id.bottom_navigation);
    bottomNavigationView.getMenu().findItem(item).setChecked(true);
  }

  /**
   * Shows/ hides the import and share buttons on the toolbar.
   *
   * @param visibilityImport visibility of the import button
   * @param visibilityShare  visibility of the share button
   */
  public void setVisibilityImportShareBtn(int visibilityImport, int visibilityShare) {
    ImageButton importBtn = findViewById(R.id.import_btn);
    importBtn.setVisibility(visibilityImport);

    ImageButton shareBtn = findViewById(R.id.share_btn);
    shareBtn.setVisibility(visibilityShare);
  }

  /**
   * Shows or hides the sort button of the toolbar.
   *
   * @param isVisible if the button should be visible or not
   */
  public void setVisibilitySortBtn(boolean isVisible) {
    ImageButton sortBtn = findViewById(R.id.sort_btn);

    if (isVisible) {
      sortBtn.setVisibility(View.VISIBLE);
    } else {
      sortBtn.setVisibility(View.GONE);
    }
  }

  /**
   * Opens the imprint fragment.
   */
  public void openImprint() {
    if (imprintFragment == null) {
      imprintFragment = new ImprintFragment();
    }

    updateFragment(imprintFragment, imprintFragmentTag);
    updateHeader(imprintFragmentTag);
  }

  public SortTypeLut getSortTypeLut() {
    return sortTypeLut;
  }

  public void setSortTypeLut(SortTypeLut sortTypeLut) {
    this.sortTypeLut = sortTypeLut;
  }

  public String getSearchText() {
    return searchText;
  }

  public void setSearchText(String searchText) {
    this.searchText = searchText;
  }

  public boolean[] getFilterCriteria() {
    return filterCriteria;
  }

  public void setFilterCriteria(int choice, boolean isChecked) {
    filterCriteria[choice] = isChecked;
  }

  public String getWelcomeMessage() {
    return welcomeMessage;
  }

  public void setWelcomeMessage(String welcomeMessage) {
    this.welcomeMessage = welcomeMessage;
  }

}
