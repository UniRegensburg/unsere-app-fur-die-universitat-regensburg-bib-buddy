package de.bibbuddy;

import android.os.Bundle;
import android.text.Spanned;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.core.text.HtmlCompat;
import androidx.fragment.app.Fragment;

/**
 * Fragment for the imprint of the app.
 *
 * @author Luis Moßburger
 */
public class ImprintFragment extends Fragment {

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_imprint, container, false);

    setupMainActivity();

    Spanned styledText =
        HtmlCompat.fromHtml(getString(R.string.imprint), HtmlCompat.FROM_HTML_MODE_LEGACY,
                            null, null);

    TextView imprintView = view.findViewById(R.id.imprint_text);
    imprintView.setText(styledText);

    return view;
  }

  private void setupMainActivity() {
    MainActivity mainActivity = (MainActivity) requireActivity();

    mainActivity.setVisibilityImportShareBtn(View.GONE, View.GONE);
    mainActivity.setVisibilitySortBtn(false);

    mainActivity.updateHeaderFragment(getString(R.string.header_imprint));
  }

}
