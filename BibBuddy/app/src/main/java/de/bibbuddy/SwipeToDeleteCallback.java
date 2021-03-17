package de.bibbuddy;

import android.app.AlertDialog;
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
import android.text.Html;
import android.view.View;
import android.widget.Toast;
import androidx.annotation.NonNull;
import androidx.core.content.ContextCompat;
import androidx.recyclerview.widget.ItemTouchHelper;
import androidx.recyclerview.widget.RecyclerView;

/**
 * Callback for swipe to delete.
 *
 * @author Sabrina Freisleben
 */
public class SwipeToDeleteCallback extends ItemTouchHelper.Callback {

  private final ColorDrawable background;
  private final int backgroundColor;
  private final NotesRecyclerViewAdapter adapter;
  private final Context context;
  private final NoteDao noteDao;
  private final Drawable icon;
  private final MainActivity activity;
  public boolean removed = false;
  private Canvas canvas;

  SwipeToDeleteCallback(Context context, NotesRecyclerViewAdapter adapter,
                        MainActivity activity) {
    this.context = context;
    this.adapter = adapter;
    this.activity = activity;
    DatabaseHelper databaseHelper = new DatabaseHelper(context);
    this.noteDao = new NoteDao(databaseHelper);
    background = new ColorDrawable();
    backgroundColor = context.getColor(R.color.alert_red);
    Paint clearPaint = new Paint();
    clearPaint.setXfermode(new PorterDuffXfermode(PorterDuff.Mode.CLEAR));
    Drawable d = ContextCompat.getDrawable(context, R.drawable.delete);
    Bitmap bm = null;
    if (d != null) {
      bm = ((BitmapDrawable) d).getBitmap();
    }
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
                          @NonNull RecyclerView.ViewHolder viewHolder, float dx, float dy,
                          int actionState, boolean isCurrentlyActive) {
    super.onChildDraw(c, recyclerView, viewHolder, dx, dy, actionState, isCurrentlyActive);
    this.canvas = c;
    View itemView = viewHolder.itemView;
    boolean isCancelled = dx == 0 && !isCurrentlyActive;
    if (isCancelled) {
      clearCanvas(c, itemView.getRight() + dx, (float) itemView.getTop(),
          (float) itemView.getRight(), (float) itemView.getBottom());
      super.onChildDraw(c, recyclerView, viewHolder, dx, dy, actionState, false);
      return;
    }

    background.setColor(backgroundColor);
    background.setBounds(itemView.getRight() + (int) dx, itemView.getTop(), itemView.getRight(),
        itemView.getBottom());
    background.draw(c);

    drawSwipeIcon(viewHolder);

    super.onChildDraw(c, recyclerView, viewHolder, dx, dy, actionState, isCurrentlyActive);
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
    icon.draw(canvas);
  }

  private void clearCanvas(Canvas c, Float left, Float top, Float right, Float bottom) {
    Paint p = new Paint();
    p.setColor(context.getColor(R.color.white));
    c.drawRect(left, top, right, bottom, p);
  }

  @Override
  public void onSwiped(RecyclerView.ViewHolder viewHolder, int direction) {
    final int position = viewHolder.getAdapterPosition();
    final NoteItem note = adapter.getData().get(position);
    removed = true;
    setupSnackbar(note, position);
  }

  private void setupSnackbar(NoteItem note, int position) {
    AlertDialog.Builder alertDeleteNote = new AlertDialog.Builder(activity);

    alertDeleteNote.setCancelable(false);
    alertDeleteNote.setTitle(R.string.delete_note);
    alertDeleteNote.setMessage(Html.fromHtml(activity.getString(R.string.delete_note_message),
        Html.FROM_HTML_MODE_COMPACT));

    alertDeleteNote.setNegativeButton(R.string.back, (dialog, which) -> {
      adapter.notifyDataSetChanged();
    });

    alertDeleteNote.setPositiveButton(R.string.delete, (dialog, which) -> {
      Toast.makeText(activity.getBaseContext(), activity.getString(R.string.deleted_notes),
          Toast.LENGTH_SHORT).show();
      noteDao.delete(note.getId());
      adapter.removeItem(position);
      adapter.notifyDataSetChanged();
    });
    alertDeleteNote.show();
  }

}

