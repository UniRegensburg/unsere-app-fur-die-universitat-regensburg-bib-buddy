package de.bibbuddy;

import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private final List<Note> data;
    private final MainActivity activity;

    public RecyclerViewAdapter(List<Note> data, MainActivity activity) {
        this.data = data;
        Collections.sort(data, new Comparator<Note>() {
            @Override
            public int compare(Note o1, Note o2) {
                if (o1.getModDate() == null || o2.getModDate() == null){
                    return 0;
                }
                return o1.getModDate().compareTo(o2.getModDate());
            }
        });
        Collections.reverse(data);
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
        if(text.contains("\n")){
            text = text.substring(0, text.indexOf("\n")) + "...";
        }
        if (text.length() > 40) {
            text = text.substring(0, 35) + " ...";
        }
        holder.title.setText(text);
        Long id = data.get(position).getId();
        /*
            Set up onClick-listener to enable editing an item of the recyclerview
             by opening the text editor fragment given the item's text as default text
         */
        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                TextNoteEditorFragment nextFrag = new TextNoteEditorFragment();
                Bundle args = new Bundle();
                args.putLong("noteId", id);
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

    public List<Note> getData() {
        return data;
    }

    public void removeItem(int position) {
        data.remove(position);
        notifyItemRemoved(position);
    }

    public void restoreItem(Note note, int position) {
        data.add(position, note);
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
