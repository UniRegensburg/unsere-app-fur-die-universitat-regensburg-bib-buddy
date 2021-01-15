package de.bibbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collections;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private final ArrayList<NoteItem> data;
    private final MainActivity activity;

    public RecyclerViewAdapter(ArrayList<NoteItem> data, MainActivity activity) {
        this.data = data;
        Collections.reverse(this.data);
        this.activity = activity;
    }

    @NonNull
    @Override
    public MyViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        View itemView = LayoutInflater.from(parent.getContext()).inflate(R.layout.list_view_item_note, parent, false);
        return new MyViewHolder(itemView);
    }

    /*
        Set up item views by using note text as title
         -> if text has more than 40 characters, only the first 35 characters are displayed as title
            and adding " ..." as indicator for a longer text
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        String text = data.get(position).getText();
        if (text.length() > 40) {
            text = text.substring(0, 35) + " ...";
        }
        holder.title.setText(text);
        /*
            Set up onClick-listener to enable editing an item of the recyclerview
             by opening the text editor fragment given the item's text as default text
         */
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextNoteEditorFragment nextFrag = new TextNoteEditorFragment();
                Bundle args = new Bundle();
                args.putInt("noteId", position);
                nextFrag.setArguments(args);
                activity.getSupportFragmentManager().beginTransaction()
                        .replace(R.id.fragment_container_view, nextFrag, "fragment_text_note_editor")
                        .addToBackStack(null)
                        .commit();
            }
        });
    }

    @Override
    public int getItemCount() {
        return data.size();
    }

    public ArrayList<NoteItem> getData() {
        return data;
    }

    public void removeItem(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(NoteItem item, int position) {
        data.add(position, item);
        notifyItemInserted(position);
    }

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView title;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.noteTitle);
        }

    }

}
