package de.bibbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.EditText;
import android.widget.Toast;

import androidx.annotation.NonNull;
import androidx.annotation.Nullable;
import androidx.fragment.app.Fragment;
import androidx.fragment.app.FragmentManager;
import androidx.fragment.app.FragmentTransaction;


public class LibraryAddShelfFragment extends Fragment {

   private final AddShelfLibraryListener listener;

   public LibraryAddShelfFragment(AddShelfLibraryListener listener) {
      this.listener = listener;
   }

   @Nullable
   @Override
   public View onCreateView(@NonNull LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {
      // Called to have the fragment instantiate its user interface view.
      View view = inflater.inflate(R.layout.fragment_library_add_shelf, container, false);

      Bundle bundle = this.getArguments();
      setupButtons(view, bundle);

      return view;
   }

   private void closeFragment() {
      FragmentManager fragmentManager = getActivity().getSupportFragmentManager();
      FragmentTransaction fragmentTransaction = fragmentManager.beginTransaction();
      fragmentTransaction.remove(fragmentManager.findFragmentById(R.id.fragment_container_add_shelf));
      fragmentTransaction.commit();
   }

   private void setupButtons(View view, Bundle bundle) {
      Context context = view.getContext();
      Button cancelBtn = view.findViewById(R.id.btn_shelf_cancel);

      cancelBtn.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            closeFragment();
         }
      });

      setupAddShelfBtnListener(context, view, bundle);
   }

   private void setupAddShelfBtnListener(Context context, View view, Bundle bundle) {
      Button addShelfBtn = view.findViewById(R.id.btn_confirm_shelf);

      addShelfBtn.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            EditText editShelfName = view.findViewById(R.id.input_shelf_name);
            String shelfName = editShelfName.getText().toString();

            if (shelfName.isEmpty() || shelfName.trim().isEmpty()) {
               Toast.makeText(context, "Bitte einen gültigen Namen angeben." +
                     "\nReine Whitespaces sind nicht erlaubt.", Toast.LENGTH_SHORT).show();
               return;
            }

            String[] shelfNames = bundle.getStringArray(LibraryKeys.SHELF_NAMES);
            for (String name : shelfNames) {
               if (shelfName.equals(name)) {
                  Toast.makeText(context, "Name existiert bereits in diesem Regal", Toast.LENGTH_SHORT).show();
                  return;
               }
            }

            Toast.makeText(context, "Regal wurde hinzugefügt", Toast.LENGTH_SHORT).show();
            Long shelfId = bundle.getLong(LibraryKeys.SHELF_ID);
            listener.onShelfAdded(shelfName, shelfId);
            closeFragment();
         }
      });
   }

   public interface AddShelfLibraryListener { // create an interface
      void onShelfAdded(String name, Long shelfId); // create callback function
   }

}
