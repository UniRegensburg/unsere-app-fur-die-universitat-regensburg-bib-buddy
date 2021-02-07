package de.bibbuddy;

import android.content.Context;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.ImageView;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.RecyclerView;

import java.util.List;

public class BookRecyclerViewAdapter extends RecyclerView.Adapter<BookRecyclerViewAdapter.BookViewHolder> {
    private final List<BookItem> bookList;
    private final BookRecyclerViewAdapter.BookListener listener;
    private final Context context;

    public BookRecyclerViewAdapter(List<BookItem> bookList, BookListener listener, Context context) {
        this.bookList = bookList;
        this.listener = listener;
        this.context = context;
    }

    @NonNull
    @Override
    public BookRecyclerViewAdapter.BookViewHolder onCreateViewHolder(@NonNull ViewGroup parent, int viewType) {
        return new BookRecyclerViewAdapter.BookViewHolder(LayoutInflater.from(parent.getContext())
                                                                .inflate(R.layout.list_view_item_book, parent, false));
    }

    private String getNoteString(int noteCount) {
        if (noteCount == 1) {
            return noteCount + " " + context.getString(R.string.note);
        } else {
            return noteCount + " " + context.getString(R.string.navigation_notes);
        }
    }

    @Override
    public void onBindViewHolder(@NonNull BookRecyclerViewAdapter.BookViewHolder holder, int position) {
        BookItem bookItem = bookList.get(position);

        holder.getTextTitleView().setText(bookItem.getName());
        holder.getImageBookView().setImageResource(bookItem.getImage());
        holder.getTextBookAuthors().setText(bookItem.getAuthors());
        holder.getTextBookYear().setText(String.valueOf(bookItem.getYear()));
        holder.getTextNoteCount().setText(getNoteString(bookItem.getNoteCount()));

        holder.itemView.setOnClickListener(new View.OnClickListener() {
            @Override
            public void onClick(View v) {
                listener.onItemClicked(position);
            }
        });
    }

    @Override
    public int getItemCount() {
        return bookList.size();
    }


    public interface BookListener { // create an interface
        void onItemClicked(int position); // create callback function
    }

    public class BookViewHolder extends RecyclerView.ViewHolder {

        private final TextView textTitleView;
        private final ImageView imageBookView;
        private final TextView textBookAuthors;
        private final TextView textBookYear;
        private final TextView textNoteCount;

        public BookViewHolder(@NonNull View itemView) {
            super(itemView);
            textTitleView = itemView.findViewById(R.id.book_title);
            imageBookView = itemView.findViewById(R.id.book_icon);
            textBookAuthors = itemView.findViewById(R.id.book_authors);
            textBookYear = itemView.findViewById(R.id.book_year);
            textNoteCount = itemView.findViewById(R.id.note_count);
        }

        public TextView getTextTitleView() {
            return textTitleView;
        }

        public ImageView getImageBookView() {
            return imageBookView;
        }

        public TextView getTextBookAuthors() {
            return textBookAuthors;
        }

        public TextView getTextBookYear() {
            return textBookYear;
        }

        public TextView getTextNoteCount() {
            return textNoteCount;
        }
    }
}
