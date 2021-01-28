package de.bibbuddy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.View;

import com.google.android.material.snackbar.Snackbar;

import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;


public class SwipeToDeleteCallback extends ItemTouchHelper.Callback {

    private final Paint clearPaint;
    private final ColorDrawable background;
    private final int backgroundColor;
    private final RecyclerViewAdapter adapter;
    public boolean removed = false;
    private final Context context;
    private final NoteDAO noteDAO;
    private final Drawable icon;
    private Canvas c;

    SwipeToDeleteCallback(Context context, RecyclerViewAdapter adapter, RecyclerView recyclerView) {
        this.context = context;
        this.adapter = adapter;
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        this.noteDAO = new NoteDAO(databaseHelper);
        background = new ColorDrawable();
        backgroundColor = context.getColor(R.color.alert_red);
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
        Drawable d = context.getDrawable(R.drawable.delete);
        Bitmap bm = ((BitmapDrawable) d).getBitmap();
        icon = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bm, 40, 40, true));
    }

    @Override
    public int getMovementFlags(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder) {
        return makeMovementFlags(0, ItemTouchHelper.LEFT);
    }

    @Override
    public boolean onMove(@NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, @NonNull RecyclerView.ViewHolder viewHolder1) {
        return false;
    }

    @Override
    public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView, @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive) {
        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
        this.c = c;
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

        drawSwipeIcon(viewHolder);

        itemView.setTag(1);

        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    private void drawSwipeIcon(RecyclerView.ViewHolder viewHolder) {
        View itemView = viewHolder.itemView;
        int iconTop = itemView.getTop() + (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconMargin = (itemView.getHeight() - icon.getIntrinsicHeight()) / 2;
        int iconLeft = itemView.getRight() - iconMargin - icon.getIntrinsicWidth();
        int iconRight = itemView.getRight() - iconMargin;
        int iconBottom = iconTop + icon.getIntrinsicHeight();
        icon.setBounds(iconLeft, iconTop, iconRight, iconBottom);
        icon.setTint(Color.WHITE);
        icon.draw(c);
    }

    private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
        c.drawRect(left, top, right, bottom, clearPaint);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
        final int position = viewHolder.getAdapterPosition();
        final Note note = adapter.getData().get(position);
        View itemView = viewHolder.itemView;
        removed = true;
        Paint clear = new Paint();
        clear.setColor(Color.WHITE);
        c.drawRect((float) 0,(float) itemView.getTop(), (float) itemView.getRight(), (float) itemView.getBottom(), clear);
        Handler handler = new Handler();
        Snackbar snackbar = Snackbar
                .make(viewHolder.itemView, R.string.delete_notification, Snackbar.LENGTH_LONG)
                .setAction(R.string.undo, new View.OnClickListener() {
                    @Override
                    public void onClick(View view) {
                        adapter.notifyDataSetChanged();
                        removed = false;
                    }
                });
        snackbar.show();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (removed) {
                    noteDAO.delete(note.getId());
                    adapter.notifyDataSetChanged();
                }
            }

        }, 5000);

    }

}

