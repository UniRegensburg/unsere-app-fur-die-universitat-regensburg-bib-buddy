package de.bibbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import com.google.android.material.bottomnavigation.BottomNavigationView;

/**
 * The HomeFragment is responsible for TODO.
 *
 * @author Claudia Schönherr
 */
public class HomeFragment extends Fragment {

  private View view;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        requireActivity().onBackPressed();
      }
    });

    view = inflater.inflate(R.layout.fragment_home, container, false);

    ((MainActivity) getActivity()).setVisibilityImportShareButton(View.GONE, View.GONE);
    ((MainActivity) getActivity()).setVisibilitySortButton(false);

    ((MainActivity) requireActivity()).updateHeaderFragment(getString(R.string.navigation_home));
    ((MainActivity) requireActivity()).updateNavigationFragment(R.id.navigation_home);

    return view;
  }

}

