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
  BottomNavigationView bottomNavigationView;
  FragmentManager fragmentManager;
  DatabaseHelper dbHelper;

  private HomeFragment homeFragment;
  private SearchFragment searchFragment;
  private LibraryFragment libraryFragment;
  private NotesFragment notesFragment;

  private SortCriteria sortCriteria;
  private String searchText;

  public ImageButton importBtn;
  public ImageButton shareBtn;
  public ImageButton sortBtn;

  @Override
  protected void onCreate(Bundle savedInstanceState) {
    super.onCreate(savedInstanceState);
    setContentView(R.layout.activity_main);

    Toolbar toolbar = findViewById(R.id.toolbar);
    setSupportActionBar(toolbar);
    getSupportActionBar().setDisplayShowTitleEnabled(false);

    bottomNavigationView = findViewById(R.id.bottom_navigation);
    fragmentManager = getSupportFragmentManager();

    if (savedInstanceState == null) {
      homeFragment = new HomeFragment();
      updateFragment(R.id.fragment_container_view, homeFragment, homeFragmentTag);
    }

    setupBottomNavigationView();

    dbHelper = new DatabaseHelper(this);

    sortCriteria = SortCriteria.MOD_DATE_LATEST;
    sortBtn = findViewById(R.id.sort_btn);
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
      case "settings":
        headerText.setText(getString(R.string.navigation_settings));
        break;

      default:
        headerText.setText(getString(R.string.app_name));
    }
  }

  /**
   * Shows/ hides the import and share buttons on the toolbar.
   *
   * @param visibilityImport  visibility of the import button
   * @param visibilityShare   visibility of the share button
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
   * @param isVisible  if the button should be visible or not
   */
  public void setVisibilitySortButton(boolean isVisible) {
    if (isVisible) {
      sortBtn.setVisibility(View.VISIBLE);
    } else {
      sortBtn.setVisibility(View.GONE);
    }
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

}
