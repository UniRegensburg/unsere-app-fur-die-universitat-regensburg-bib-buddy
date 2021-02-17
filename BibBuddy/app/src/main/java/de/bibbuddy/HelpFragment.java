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
 * Fragment for the user manuals.
 *
 * @author Sarah Kurek
 */
public class HelpFragment extends Fragment {
  private View view;
  private String manualText;

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    requireActivity().getOnBackPressedDispatcher().addCallback(new OnBackPressedCallback(true) {
      @Override
      public void handleOnBackPressed() {

        FragmentManager fm = getParentFragmentManager();
        if (fm.getBackStackEntryCount() > 0) {
          fm.popBackStack();
        } else {
          requireActivity().onBackPressed();
        }
      }
    });

    Bundle bundle = this.getArguments();
    manualText = bundle.getString(LibraryKeys.MANUAL_TEXT);

    // style text
    Spanned styledText = HtmlCompat.fromHtml(manualText, HtmlCompat.FROM_HTML_MODE_LEGACY,
        null, null);

    view = inflater.inflate(R.layout.fragment_help, container, false);

    ((MainActivity) getActivity()).updateHeaderFragment(getString(R.string.headerHelp));

    TextView manualView = view.findViewById(R.id.manual_text);
    manualView.setText(styledText);

    return view;
  }

}
