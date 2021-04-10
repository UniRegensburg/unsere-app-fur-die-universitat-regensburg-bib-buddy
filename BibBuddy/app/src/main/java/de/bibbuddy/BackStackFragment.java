package de.bibbuddy;

import androidx.activity.OnBackPressedCallback;
import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;

public class BackStackFragment extends Fragment {

    private final OnBackPressedCallback backPressedCallback;

    protected BackStackFragment() {
        backPressedCallback = new OnBackPressedCallback(false) {
            @Override
            public void handleOnBackPressed() {
                onBackPressed();
            }
        };
    }

    protected void onBackPressed() {
        closeFragment();
    }

    protected void showFragment(@NonNull Fragment fragment) {
        showFragment(fragment, null);
    }

    protected void showFragment(@NonNull Fragment fragment, @Nullable String tag) {
        disableBackPressedHandler();

        requireActivity().getSupportFragmentManager().beginTransaction()
                .replace(R.id.fragment_container_view, fragment, tag)
                .setReorderingAllowed(true)
                .addToBackStack(null)
                .commit();
    }

    protected void closeFragment() {
        disableBackPressedHandler();
        requireActivity().onBackPressed();
    }

    protected void enableBackPressedHandler() {
        requireActivity().getOnBackPressedDispatcher().addCallback(backPressedCallback);
        backPressedCallback.setEnabled(true);
    }

    protected void disableBackPressedHandler() {
        backPressedCallback.setEnabled(false);
        backPressedCallback.remove();
    }

}
