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
import androidx.fragment.app.DialogFragment;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;

/**
 * Fragment for the user manuals.
 *
 * @author Sarah Kurek, Luis Moßburger
 */
public class HelpFragment extends DialogFragment {
  private View view;
  private String manualText;

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

    Bundle bundle = this.getArguments();
    manualText = bundle.getString(LibraryKeys.MANUAL_TEXT);

    view = inflater.inflate(R.layout.fragment_help, container, false);

    // style text
    Spanned styledText = HtmlCompat.fromHtml(manualText, HtmlCompat.FROM_HTML_MODE_LEGACY,
                                             null, null);

    TextView manualView = view.findViewById(R.id.manual_text);
    manualView.setText(styledText);

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
