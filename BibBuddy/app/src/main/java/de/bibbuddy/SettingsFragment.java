package de.bibbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

public class SettingsFragment extends Fragment {

  // public SearchFragment() {
  //      super(R.layout.fragment_settings);
  //   }
  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {
    requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {
        closeFragment();
      }
    });

    View view = inflater.inflate(R.layout.fragment_settings, container, false);

    ((MainActivity) getActivity()).setVisibilityImportShareButton(View.INVISIBLE, View.INVISIBLE);

    return view;
  }

  private void closeFragment() {
    FragmentManager manager = getParentFragmentManager();
    if (manager.getBackStackEntryCount() > 0) {
      manager.popBackStack();
    } else {
      requireActivity().onBackPressed();
    }
  }

}


