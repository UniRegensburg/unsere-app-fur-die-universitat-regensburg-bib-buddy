package de.bibbuddy;

import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

public class SwipeToDeleteCallback extends ItemTouchHelper.Callback {

    private final Paint clearPaint;
    private final ColorDrawable background;
    private final int backgroundColor;
    private final Drawable deleteDrawable;
    private final RecyclerViewAdapter adapter;
    private final RecyclerView recyclerView;
    Context context;

    SwipeToDeleteCallback(Context context, RecyclerViewAdapter adapter, RecyclerView recyclerView) {
        this.context = context;
        this.adapter = adapter;
        this.recyclerView = recyclerView;
        background = new ColorDrawable();
        backgroundColor = context.getColor(R.color.colorAccent);
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        deleteDrawable = ContextCompat.getDrawable(context, R.drawable.trashcan);
    }


    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.LEFT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }


    /*
        TODO: Clarify:
         might be more aesthetic/more usable to implement deletion note and the undo option here
         instead of an extra toast on the bottom of the display
     */
    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        View itemView = viewHolder.itemView;

        boolean isCancelled = dX == 0 && !isCurrentlyActive;

        if (isCancelled) {
            clearCanvas(c, itemView.getRight() + dX, (float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom());
            super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
            return;
        }
        background.setColor(backgroundColor);
        background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(), itemView.getBottom());
        background.draw(c);
        int deleteIconTop = itemView.getTop() + (itemView.getHeight() - deleteDrawable.getIntrinsicHeight()) / 2;
        int deleteIconMargin = (itemView.getHeight() - deleteDrawable.getIntrinsicHeight()) / 2;
        int deleteIconLeft = itemView.getRight() - deleteIconMargin - deleteDrawable.getIntrinsicWidth();
        int deleteIconRight = itemView.getRight() - deleteIconMargin;
        int deleteIconBottom = deleteIconTop + deleteDrawable.getIntrinsicHeight();

        deleteDrawable.setBounds(deleteIconLeft, deleteIconTop, deleteIconRight, deleteIconBottom);
        deleteDrawable.setTint(context.getColor(R.color.design_default_color_background));
        deleteDrawable.draw(c);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
        c.drawRect(left, top, right, bottom, clearPaint);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        final NoteItem item = adapter.getData().get(position);
        adapter.removeItem(position);
        adapter.notifyDataSetChanged();
        NoteItem noteItem = TextNoteEditorFragment.getNotes().get(position);
        TextNoteEditorFragment.deleteNote(noteItem.getId());
        Snackbar snackbar = Snackbar
                .make(NotesFragment.view, R.string.delete_snackbar, Snackbar.LENGTH_LONG);
        snackbar.setAction(R.string.undo, new View.OnClickListener() {
            @Override
            public void onClick(View view) {
                adapter.restoreItem(item, position);
                MainActivity.databaseHelper.addNote(noteItem.getName(), noteItem.getType(), noteItem.getText(), noteItem.getCreateDate(), noteItem.getModDate(), noteItem.getNoteFileId());
                recyclerView.scrollToPosition(position);
                adapter.notifyDataSetChanged();
            }
        });

        snackbar.setActionTextColor(context.getColor(R.color.colorAccent));
        snackbar.show();
    }

}

