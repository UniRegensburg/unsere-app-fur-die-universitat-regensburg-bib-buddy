package de.bibbuddy;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.List;

public class LibraryAdapter implements ListAdapter {

   private final List<LibraryItem> libraryList;
   private final Context context;

   public LibraryAdapter(Context context, List<LibraryItem> libraryList) {
      this.context = context;
      this.libraryList = libraryList;
   }


   @Override
   public boolean areAllItemsEnabled() {
      return false;
   }

   @Override
   public boolean isEnabled(int position) {
      return true;
   }

   @Override
   public void registerDataSetObserver(DataSetObserver observer) {

   }

   @Override
   public void unregisterDataSetObserver(DataSetObserver observer) {

   }

   @Override
   public int getCount() {
      return libraryList.size();
   }

   @Override
   public Object getItem(int position) {
      return position;
   }

   @Override
   public long getItemId(int position) {
      return position;
   }

   @Override
   public boolean hasStableIds() {
      return false;
   }

   @Override
   public View getView(int position, View convertView, ViewGroup parent) {
      LibraryItem libraryData = libraryList.get(position);

      if (convertView == null) {
         LayoutInflater layoutInflater = LayoutInflater.from(context);

         convertView = layoutInflater.inflate(R.layout.list_view_item_library, parent, false);

         TextView name = convertView.findViewById(R.id.item_name);
         name.setText(libraryData.getName());

         ImageView image = convertView.findViewById(R.id.library_icon);
         image.setImageResource(libraryData.getImage());

         convertView.setTag(libraryData);

      }

      return convertView;
   }

   @Override
   public int getItemViewType(int position) {
      return position;
   }

   @Override
   public int getViewTypeCount() {
      if (libraryList.isEmpty())
         return 1;

      return libraryList.size();
   }

   @Override
   public boolean isEmpty() {
      return libraryList.isEmpty();
   }
}
