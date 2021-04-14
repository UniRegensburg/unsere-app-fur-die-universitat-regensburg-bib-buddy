package de.bibbuddy;

import android.content.ContentResolver;
import android.content.Intent;
import android.net.Uri;
import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
import android.widget.Toast;
import androidx.appcompat.app.AppCompatActivity;
import androidx.appcompat.widget.Toolbar;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;
import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

  private final String homeFragmentTag = "home";
  private final String searchFragmentTag = "search";
  private final String libraryFragmentTag = "library";
  private final String notesFragmentTag = "notes";
  private final String imprintFragmentTag = "imprint";
  private final String defaultAppFragmentTag = "defaultAppSelected";

  public ImageButton importBtn;
  public ImageButton shareBtn;
  public ImageButton sortBtn;

  BottomNavigationView bottomNavigationView;
  FragmentManager fragmentManager;
  DatabaseHelper dbHelper;

  private ImageButton logoButton;
  private HomeFragment homeFragment;
  private SearchFragment searchFragment;
  private LibraryFragment libraryFragment;
  private NotesFragment notesFragment;
  private ImprintFragment imprintFragment;
  private AsDefaultAppFragment defaultAppFragment;

  private SortCriteria sortCriteria;
  private boolean[] filterCriteria;
  private String searchText;

  private boolean isDefaultSelected;
  private Uri uri;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    logoButton = findViewById(R.id.headerLogo);
    bottomNavigationView = findViewById(R.id.bottom_navigation);
    fragmentManager = getSupportFragmentManager();

    if (savedInstanceState == null) {
      homeFragment = new HomeFragment();
      updateFragment(R.id.fragment_container_view, homeFragment, homeFragmentTag);
    }

    setupLogoButton();
    setupBottomNavigationView();

    dbHelper = new DatabaseHelper(this);

    sortCriteria = SortCriteria.MOD_DATE_LATEST;
    sortBtn = findViewById(R.id.sort_btn);

    filterCriteria = new boolean[] {true, true, true};

    searchText = "";

    setupDefaultAppSelected();
  }

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

  /**
   * Checks if the BubBuddy-App is selected as
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

  private void switchToDefaultAppFragment() {
    if (defaultAppFragment == null) {
      defaultAppFragment = new AsDefaultAppFragment();
    }

    updateFragment(R.id.fragment_container_view, defaultAppFragment, defaultAppFragmentTag);
  }

  private void setupBottomNavigationView() {
    bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
      switch (item.getItemId()) {
        case R.id.navigation_home:
          if (homeFragment == null) {
            homeFragment = new HomeFragment();
          }
          resetIsDefaultApp();
          updateFragment(R.id.fragment_container_view, homeFragment, homeFragmentTag);
          break;

        case R.id.navigation_search:
          if (searchFragment == null) {
            searchFragment = new SearchFragment();
          }
          resetIsDefaultApp();
          updateFragment(R.id.fragment_container_view, searchFragment, searchFragmentTag);
          break;

        case R.id.navigation_library:
          if (libraryFragment == null) {
            libraryFragment = new LibraryFragment();
          }
          resetIsDefaultApp();
          updateFragment(R.id.fragment_container_view, libraryFragment, libraryFragmentTag);
          break;

        case R.id.navigation_notes:
          if (notesFragment == null) {
            notesFragment = new NotesFragment();
          }
          resetIsDefaultApp();
          updateFragment(R.id.fragment_container_view, notesFragment, notesFragmentTag);
          break;

        default:
          break;
      }

      return true;
    });
  }

  private void setupLogoButton() {
    logoButton.setOnClickListener(new View.OnClickListener() {
      @Override
      public void onClick(View v) {
        if (homeFragment == null) {
          homeFragment = new HomeFragment();
        }
        updateFragment(R.id.fragment_container_view, homeFragment, homeFragmentTag);
      }
    });
  }

  private void updateFragment(int id, Fragment fragment, String tag) {
    FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
    fragmentTransaction.replace(id, fragment, tag);
    fragmentTransaction.setReorderingAllowed(true);
    fragmentTransaction.addToBackStack(null);
    fragmentTransaction.commit();
    updateHeader(tag);
  }

  public void updateHeaderFragment(String name) {
    TextView headerView = findViewById(R.id.headerText);
    headerView.setText(name);
  }

  public void updateNavigationFragment(int item) {
    bottomNavigationView = findViewById(R.id.bottom_navigation);
    bottomNavigationView.getMenu().findItem(item).setChecked(true);
  }

  private void updateHeader(String tag) {
    // change header text according to fragment
    View headerTextView = findViewById(R.id.headerText);
    TextView headerText = (TextView) headerTextView;
    switch (tag) {
      case "home":
        headerText.setText(getString(R.string.navigation_home));
        break;
      case "search":
        headerText.setText(getString(R.string.navigation_search));
        break;
      case "library":
        headerText.setText(getString(R.string.navigation_library));
        break;
      case "notes":
        headerText.setText(getString(R.string.navigation_notes));
        break;
      case "imprint":
        headerText.setText(R.string.header_imprint);
        break;

      default:
        headerText.setText(getString(R.string.app_name));
    }
  }

  /**
   * Shows/ hides the import and share buttons on the toolbar.
   *
   * @param visibilityImport visibility of the import button
   * @param visibilityShare  visibility of the share button
   */
  public void setVisibilityImportShareButton(int visibilityImport, int visibilityShare) {
    importBtn = findViewById(R.id.import_btn);
    importBtn.setVisibility(visibilityImport);

    shareBtn = findViewById(R.id.share_btn);
    shareBtn.setVisibility(visibilityShare);
  }

  /**
   * Shows or hides the sort button of the toolbar.
   *
   * @param isVisible if the button should be visible or not
   */
  public void setVisibilitySortButton(boolean isVisible) {
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

    updateFragment(R.id.fragment_container_view, imprintFragment, imprintFragmentTag);
    updateHeader(imprintFragmentTag);
  }

  public SortCriteria getSortCriteria() {
    return sortCriteria;
  }

  public void setSortCriteria(SortCriteria sortCriteria) {
    this.sortCriteria = sortCriteria;
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
}
