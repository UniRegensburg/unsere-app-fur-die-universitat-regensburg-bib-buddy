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
import androidx.fragment.app.DialogFragment;

/**
 * Fragment for the user manuals.
 *
 * @author Sarah Kurek, Luis Moßburger
 */
public class HelpFragment extends DialogFragment {

  @Nullable
  @Override
  public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container,
                           @Nullable Bundle savedInstanceState) {

    View view = inflater.inflate(R.layout.fragment_help, container, false);

    Bundle bundle = this.getArguments();
    assert bundle != null;
    String manualText = bundle.getString(LibraryKeys.MANUAL_TEXT);

    Spanned styledText = HtmlCompat.fromHtml(manualText, HtmlCompat.FROM_HTML_MODE_LEGACY,
                                             null, null);

    TextView manualView = view.findViewById(R.id.manual_text);
    manualView.setText(styledText);

    return view;
  }

}
