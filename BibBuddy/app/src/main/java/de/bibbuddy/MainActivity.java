package de.bibbuddy;

import android.os.Bundle;

import androidx.appcompat.app.AppCompatActivity;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;

import com.google.android.material.bottomnavigation.BottomNavigationView;

public class MainActivity extends AppCompatActivity {

   BottomNavigationView bottomNavigationView;
   FragmentManager fragmentManager;

   private HomeFragment homeFragment;
   private final String HOME_FRAGMENT_TAG = "home";

   private SearchFragment searchFragment;
   private final String SEARCH_FRAGMENT_TAG = "search";

   private LibraryFragment libraryFragment;
   private final String LIBRARY_FRAGMENT_TAG = "library";

   private NotesFragment notesFragment;
   private final String NOTES_FRAGMENT_TAG = "notes";


   @Override
   protected void onCreate(Bundle savedInstanceState) {
      super.onCreate(savedInstanceState);
      setContentView(R.layout.activity_main);

      bottomNavigationView = (BottomNavigationView) findViewById(R.id.navigation_bar);
      fragmentManager = getSupportFragmentManager();

      if (savedInstanceState == null) {
         homeFragment = new HomeFragment();
         updateFragment(R.id.fragment_container_view, homeFragment, HOME_FRAGMENT_TAG);
      }

      setupBottomNavigationView();
   }

   private void setupBottomNavigationView() {
      bottomNavigationView.setOnNavigationItemSelectedListener(item -> {
         switch (item.getItemId()) {
            case R.id.navigation_home:
               if (homeFragment == null) {
                  homeFragment = new HomeFragment();
               }
               updateFragment(R.id.fragment_container_view, homeFragment, HOME_FRAGMENT_TAG);
               break;

            case R.id.navigation_search:
               if (searchFragment == null) {
                  searchFragment = new SearchFragment();
               }
               updateFragment(R.id.fragment_container_view, searchFragment, SEARCH_FRAGMENT_TAG);
               break;

            case R.id.navigation_library:
               if (libraryFragment == null) {
                  libraryFragment = new LibraryFragment();
               }
               updateFragment(R.id.fragment_container_view, libraryFragment, LIBRARY_FRAGMENT_TAG);
               break;

            case R.id.navigation_notes:
               if (notesFragment == null) {
                  notesFragment = new NotesFragment();
               }
               updateFragment(R.id.fragment_container_view, notesFragment, NOTES_FRAGMENT_TAG);
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
   }
}
