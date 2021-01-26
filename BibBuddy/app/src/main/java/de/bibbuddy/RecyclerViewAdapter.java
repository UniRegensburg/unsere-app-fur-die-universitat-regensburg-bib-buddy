package de.bibbuddy;

import android.content.Context;
import android.os.Bundle;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import java.text.SimpleDateFormat;
import java.util.Collections;
import java.util.Comparator;
import java.util.Date;
import java.util.List;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

public class RecyclerViewAdapter extends RecyclerView.Adapter<RecyclerViewAdapter.MyViewHolder> {

    private final List<Note> data;
    private final MainActivity activity;

    public static class MyViewHolder extends RecyclerView.ViewHolder {

        private final TextView modDate;
        private final TextView title;

        public MyViewHolder(View itemView) {
            super(itemView);
            title = itemView.findViewById(R.id.noteTitle);
            modDate = itemView.findViewById(R.id.noteModDate);
        }

    }

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
        View itemView;
        Context context = parent.getContext();
        itemView = LayoutInflater.from(context).inflate(R.layout.list_view_item_note, parent, false);
        return new MyViewHolder(itemView);
    }

    /*
        Set up item views by using note text as title
         -> if text has more than 40 characters, only the first 35 characters are displayed as title
            and adding " ..." as indicator for a longer text
     */
    @Override
    public void onBindViewHolder(@NonNull MyViewHolder holder, int position) {
        Long id = data.get(position).getId();
        String text = data.get(position).getText();
        if(text.contains("\n")){
            text = text.substring(0, text.indexOf("\n")) + "...";
        }
        if (text.length() > 40) {
            text = text.substring(0, 35) + " ...";
        }
        holder.title.setText(text);
        String dateString = getDate(data.get(position).getModDate());
        holder.modDate.setText(dateString);

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

    private String getDate(Long date){
        Date d = new Date(date);
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

        String string = simpleDateFormat.format(d);
        String day = string.substring(8,10);
        String month = string.substring(5,7);
        String year = string.substring(0,4);
        String time = string.substring(11,16);

        string = day + "." + month + "." + year + " " + time + " Uhr";

        return string;
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

}
