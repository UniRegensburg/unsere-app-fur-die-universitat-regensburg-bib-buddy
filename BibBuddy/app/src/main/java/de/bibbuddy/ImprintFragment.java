package de.bibbuddy;

import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * Fragment for the imprint of the app.
 *
 * @author Luis Moßburger
 */
public class ImprintFragment extends Fragment {

  private View view;

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

    ((MainActivity) getActivity()).setVisibilityImportShareButton(View.GONE, View.GONE);

    view = inflater.inflate(R.layout.fragment_imprint, container, false);

    ((MainActivity) getActivity()).updateHeaderFragment(getString(R.string.header_imprint));
    ((MainActivity) getActivity()).setVisibilitySortButton(false);

    // style text
    Spanned styledText =
        HtmlCompat.fromHtml(getString(R.string.imprint), HtmlCompat.FROM_HTML_MODE_LEGACY,
            null, null);

    TextView imprintView = view.findViewById(R.id.imprint_text);
    imprintView.setText(styledText);

    return view;
  }

  /**
   * Closes the HelpFragment.
   */
  public void closeFragment() {
    FragmentManager fragmentManager = getParentFragmentManager();
    if (fragmentManager.getBackStackEntryCount() > 0) {
      fragmentManager.popBackStack();
    } else {
      requireActivity().onBackPressed();
    }
  }

}
