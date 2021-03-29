package de.bibbuddy;

import android.os.Bundle;
import android.view.View;
import android.widget.ImageButton;
import android.widget.TextView;
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

  private SortCriteria sortCriteria;
  private boolean[] filterCriteria;
  private String searchText;

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
  }

  private void setupBottomNavigationView() {
    bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
      switch (item.getItemId()) {
        case R.id.navigation_home:
          if (homeFragment == null) {
            homeFragment = new HomeFragment();
          }
          updateFragment(R.id.fragment_container_view, homeFragment, homeFragmentTag);
          break;

        case R.id.navigation_search:
          if (searchFragment == null) {
            searchFragment = new SearchFragment();
          }
          updateFragment(R.id.fragment_container_view, searchFragment, searchFragmentTag);
          break;

        case R.id.navigation_library:
          if (libraryFragment == null) {
            libraryFragment = new LibraryFragment();
          }
          updateFragment(R.id.fragment_container_view, libraryFragment, libraryFragmentTag);
          break;

        case R.id.navigation_notes:
          if (notesFragment == null) {
            notesFragment = new NotesFragment();
          }
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
        headerText.setText(R.string.headerImprint);
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
