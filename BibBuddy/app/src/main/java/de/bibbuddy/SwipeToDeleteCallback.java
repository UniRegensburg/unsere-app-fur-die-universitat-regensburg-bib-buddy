package de.bibbuddy;

import android.annotation.SuppressLint;
import android.content.Context;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Rect;
import android.graphics.RectF;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.os.Handler;
import android.view.MotionEvent;
import android.view.View;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
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
    private Drawable undo;

    SwipeToDeleteCallback(Context context, RecyclerViewAdapter adapter, RecyclerView recyclerView) {
        this.context = context;
        this.adapter = adapter;
        DatabaseHelper databaseHelper = new DatabaseHelper(context);
        this.noteDAO = new NoteDAO(databaseHelper);
        background = new ColorDrawable();
        backgroundColor = context.getColor(R.color.gray_background);
        clearPaint = new Paint();
        clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));

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

        drawSwipedElements(c, viewHolder);

        setTouchListener(c,recyclerView,viewHolder, dX, dY, actionState, isCurrentlyActive);


        super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    }

    /*
        Added this suppressLint, because it wants to force to override performClick in OnTouchListener
            -> Custom TextView-class has to be created, but does not seem necessary here
     */
    @SuppressLint("ClickableViewAccessibility")
    private void setTouchListener(Canvas c, RecyclerView recyclerView, RecyclerView.ViewHolder viewHolder, float dX, float dY, int actionState, boolean isCurrentlyActive){
        recyclerView.setOnTouchListener(new View.OnTouchListener() {

            final int position = viewHolder.getAdapterPosition();

            @Override
            public boolean onTouch(View v, MotionEvent event) {
                if(event.getAction() == MotionEvent.ACTION_DOWN && undo != null){
                    final int x = (int) event.getX();
                    final int y = (int) event.getY();
                    final Rect bounds = undo.getBounds();
                    if(x >= (bounds.left) && x <= (bounds.right) &&
                            y >= (bounds.top) && y <= (bounds.bottom)){
                        adapter.notifyDataSetChanged();
                        recyclerView.scrollToPosition(position);
                        removed = false;
                    }
                }
                return false;
            }

        });
    }

    private void drawSwipedElements(Canvas c, RecyclerView.ViewHolder viewHolder) {
        float buttonWidthWithoutPadding =  400;
        float corners = 16;

        View itemView = viewHolder.itemView;
        Paint p = new Paint();

        RectF leftText = new RectF(itemView.getLeft() + 250, itemView.getTop(), itemView.getLeft() + buttonWidthWithoutPadding, itemView.getBottom());
        p.setColor(Color.TRANSPARENT);
        c.drawRoundRect(leftText, corners, corners, p);
        drawText(context.getString(R.string.delete_notification), c, leftText, p);

        //TODO: Improve undo button visibility for better usability

        /*p.setColor(context.getColor(R.color.flirt_light));
         c.drawCircle(itemView.getRight()-100, itemView.getTop() + itemView.getHeight()/2, 50, p);
        undo = new ScaleDrawable(context.getDrawable(R.drawable.reverse), Gravity.CENTER,50,50);
         */

        undo = ContextCompat.getDrawable(context, R.drawable.reverse);
        undo.setBounds(itemView.getRight() - 150,itemView.getTop()+50,itemView.getRight()-50,itemView.getBottom()-50);
        undo.draw(c);

    }

    private void drawText(String text, Canvas c, RectF button, Paint p) {
        float textSize = 45;
        p.setColor(Color.BLACK);
        p.setAntiAlias(true);
        p.setTextSize(textSize);

        float textWidth = p.measureText(text);
        c.drawText(text, button.centerX()-(textWidth/2), button.centerY()+(textSize/2), p);
    }

    private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
        c.drawRect(left, top, right, bottom, clearPaint);
    }

    @Override
    public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {

        final int position = viewHolder.getAdapterPosition();
        final Note note = adapter.getData().get(position);
        removed = true;
        Handler handler = new Handler();
        handler.postDelayed(new Runnable() {
            public void run() {
                if (removed) {
                    noteDAO.delete(note.getId());
                    adapter.removeItem(position);
                    adapter.notifyDataSetChanged();
                }
            }

        }, 3000);

    }

}

