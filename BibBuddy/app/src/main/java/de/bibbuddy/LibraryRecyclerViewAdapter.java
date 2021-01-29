package de.bibbuddy;

import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class LibraryRecyclerViewAdapter extends RecyclerView.Adapter<LibraryRecyclerViewAdapter.LibraryViewHolder> {

   private final List<LibraryItem> libraryList;
   private final LibraryListener listener;

   public LibraryRecyclerViewAdapter(List<LibraryItem> libraryList, LibraryListener listener) {
      this.libraryList = libraryList;
      this.listener = listener;
   }

   @NonNull
   @Override
   public LibraryRecyclerViewAdapter.LibraryViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
      // RecyclerView calls this method whenever it needs to create a new ViewHolder.
      // The method creates and initializes the ViewHolder and its associated View,
      // but does not fill in the view's contents—the ViewHolder has not yet been bound to specific data.

      return new LibraryViewHolder(LayoutInflater.from(parent.getContext())
                                         .inflate(R.layout.list_view_item_library, parent, false));
   }

   @Override
   public void onBindViewHolder(@NonNull LibraryViewHolder holder, int position) {
      // Get element from your dataset at this position and replace the contents of the view with that element
      holder.getTextView().setText(libraryList.get(position).getName());
      holder.getImageView().setImageResource(libraryList.get(position).getImage());

      holder.itemView.setOnClickListener(new View.OnClickListener() {
         @Override
         public void onClick(View v) {
            listener.onItemClicked(position);
         }
      });
   }

   @Override
   public int getItemCount() {
      // RecyclerView calls this method to get the size of the data set.
      // RecyclerView uses this to determine when there are no more items that can be displayed.
      return libraryList.size();
   }

   public interface LibraryListener { // create an interface
      void onItemClicked(int position); // create callback function
   }

   public static class LibraryViewHolder extends RecyclerView.ViewHolder {

      private final TextView textView;
      private final ImageView imageView;

      public LibraryViewHolder(@NonNull View itemView) {
         super(itemView);

         textView = itemView.findViewById(R.id.item_name);
         imageView = itemView.findViewById(R.id.library_icon);
      }

      public TextView getTextView() {
         return textView;
      }

      public ImageView getImageView() {
         return imageView;
      }
   }
}
