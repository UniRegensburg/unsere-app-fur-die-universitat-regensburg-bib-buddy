package de.bibbuddy;

import android.content.Context;
import android.graphics.Bitmap;
import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.PorterDuff;
import android.graphics.PorterDuffXfermode;
import android.graphics.Typeface;
import android.graphics.drawable.BitmapDrawable;
import android.graphics.drawable.ColorDrawable;
import android.graphics.drawable.Drawable;
import android.view.View;
import android.widget.TextView;
import androidx.annotation.NonNull;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;
import com.google.android.material.snackbar.Snackbar;


public class SwipeToDeleteCallback extends ItemTouchHelper.Callback {

  private final Paint clearPaint;
  private final ColorDrawable background;
  private final int backgroundColor;
  private final NoteRecyclerViewAdapter adapter;
  private final Context context;
  private final NoteDao noteDAO;
  private final Drawable icon;
  private final MainActivity activity;
  public boolean removed = false;
  private Canvas c;

  SwipeToDeleteCallback(Context context, NoteRecyclerViewAdapter adapter,
                        MainActivity activity) {
    this.context = context;
    this.adapter = adapter;
    this.activity = activity;
    DatabaseHelper databaseHelper = new DatabaseHelper(context);
    this.noteDAO = new NoteDao(databaseHelper);
    background = new ColorDrawable();
    backgroundColor = context.getColor(R.color.alert_red);
    clearPaint = new Paint();
    clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    Drawable d = context.getDrawable(R.drawable.delete);
    Bitmap bm = ((BitmapDrawable) d).getBitmap();
    icon = new BitmapDrawable(context.getResources(), Bitmap.createScaledBitmap(bm, 40, 40, true));
  }

  @Override
  public int getMovementFlags(@NonNull RecyclerView recyclerView,
                              @NonNull RecyclerView.ViewHolder viewHolder) {
    return makeMovementFlags(0, ItemTouchHelper.LEFT);
  }

  @Override
  public boolean onMove(@NonNull RecyclerView recyclerView,
                        @NonNull RecyclerView.ViewHolder viewHolder,
                        @NonNull RecyclerView.ViewHolder viewHolder1) {
    return false;
  }

  @Override
  public void onChildDraw(@NonNull Canvas c, @NonNull RecyclerView recyclerView,
                          @NonNull RecyclerView.ViewHolder viewHolder, float dX, float dY,
                          int actionState, boolean isCurrentlyActive) {
    super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
    this.c = c;
    View itemView = viewHolder.itemView;
    boolean isCancelled = dX == 0 && !isCurrentlyActive;
    if (isCancelled) {
      clearCanvas(c, itemView.getRight() + dX, (float) itemView.getTop(),
          (float) itemView.getRight(), (float) itemView.getBottom());
      super.onChildDraw(c, recyclerView, viewHolder, dX, dY, actionState, isCurrentlyActive);
      return;
    }

    background.setColor(backgroundColor);
    background.setBounds(itemView.getRight() + (int) dX, itemView.getTop(), itemView.getRight(),
        itemView.getBottom());
    background.draw(c);

    drawSwipeIcon(viewHolder);

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
    Paint p = new Paint();
    p.setColor(context.getColor(R.color.white));
    c.drawRect(left, top, right, bottom, p);
  }

  @Override
  public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
    NoteRecyclerViewAdapter.MyViewHolder myViewHolder =
        new NoteRecyclerViewAdapter.MyViewHolder(viewHolder.itemView);
    final int position = viewHolder.getAdapterPosition();
    final NoteItem note = adapter.getData().get(position);
    View itemView = viewHolder.itemView;
    removed = true;
    setupSnackbar(note, itemView, position);
  }

  private void setupSnackbar(NoteItem note, View itemView, int position) {
    Snackbar snackbar = Snackbar
        .make(activity.findViewById(R.id.fragment_notes), R.string.delete_notification,
            Snackbar.LENGTH_LONG)
        .setAction(R.string.undo, new View.OnClickListener() {
          @Override
          public void onClick(View view) {
            adapter.notifyDataSetChanged();
            removed = false;
          }
        });

    TextView snackbarActionTextView =
        (TextView) snackbar.getView().findViewById(R.id.snackbar_action);
    snackbarActionTextView.setTextSize(15);
    snackbarActionTextView.setTypeface(snackbarActionTextView.getTypeface(), Typeface.BOLD);

    snackbar.setAnchorView(itemView);
    View view = snackbar.getView();
    view.setTranslationY(itemView.getHeight());
    view.setX(itemView.getLeft() - 35);

    snackbar.show();
    snackbar.addCallback(new Snackbar.Callback() {

      @Override
      public void onDismissed(Snackbar snackbar, int event) {
        if (removed) {
          adapter.removeItem(position);
          adapter.notifyDataSetChanged();
          noteDAO.delete(note.getId());
        }
      }

      @Override
      public void onShown(Snackbar snackbar) {

      }

    });
  }

}

