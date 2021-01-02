package de.bibbuddy;

import android.content.Context;
import android.database.DataSetObserver;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.ListAdapter;
import android.widget.TextView;

import java.util.ArrayList;

public class LibraryAdapter implements ListAdapter {

   ArrayList<LibraryItem> libraryList;
   Context context;

   public LibraryAdapter(Context context, ArrayList<LibraryItem> libraryList) {
      this.libraryList = libraryList;
      this.context = context;
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
         name.setText(libraryData.mName);

         ImageView image = convertView.findViewById(R.id.library_icon);
         image.setImageResource(libraryData.mImage);

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
      if(libraryList.size() > 0) {
         return libraryList.size();
      }
      return 1; // without it an error appears when the list is empty
   }

   @Override
   public boolean isEmpty() {
      return false;
   }
}
