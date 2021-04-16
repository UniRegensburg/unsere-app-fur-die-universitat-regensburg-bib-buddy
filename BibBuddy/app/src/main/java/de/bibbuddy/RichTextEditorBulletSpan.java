package de.bibbuddy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.BulletSpan;

/**
 * RichTextEditorBulletSpan is responsible for styling bullet spans in text notes.
 *
 * @author Sabrina Freisleben
 */
public class RichTextEditorBulletSpan extends BulletSpan {

  private static Path bulletPath;

  private final int bulletColor = Color.BLACK;
  private final int bulletRadius = 10;
  private final int bulletGapWidth = 20;

  /**
   * Puts default bullet span settings to a parcel.
   *
   * @param dest  parcel to put the bullet settings
   * @param flags how the settings should be written
   */
  public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);

    dest.writeInt(this.bulletColor);
    dest.writeInt(this.bulletRadius);
    dest.writeInt(this.bulletGapWidth);
  }

  /**
   * Fetches the margin between the bullet and the first character of the text.
   *
   * @return the distance between the bullet and the first character in pixels
   */
  public int getLeadingMargin(boolean first) {
    return 2 * this.bulletRadius + this.bulletGapWidth;
  }

  /**
   * Draws the leading margin for bullets.
   *
   * @param canvas    canvas to draw on
   * @param paint     paint used to draw
   * @param x         x-coordinate to setup the canvas
   * @param direction x-coordinate to give the canvas a direction to draw (+ or -)
   * @param top       y-coordinate to set the top of the canvas
   * @param bottom    y-coordinate to set the bottom of the canvas
   * @param text      text content as charSequence
   * @param start     first character position of the text
   */
  public void drawLeadingMargin(final Canvas canvas, Paint paint, int x, int direction, int top,
                                int baseline, int bottom, CharSequence text, int start, int end,
                                boolean first, Layout layout) {

    if (((Spanned) text).getSpanStart(this) == start) {
      paint.setColor(this.bulletColor);
      paint.setStyle(Paint.Style.FILL);

      if (canvas.isHardwareAccelerated()) {
        if (bulletPath == null) {
          bulletPath = new Path();
          bulletPath.addCircle(0.0F, 0.0F, (float) this.bulletRadius, Path.Direction.CW);
        }

        canvas.save();
        canvas
            .translate((float) (x + direction * this.bulletRadius), (float) (top + bottom) / 2.0F);
        canvas.drawPath(bulletPath, paint);
        canvas.restore();
      } else {
        canvas
            .drawCircle((float) (x + direction * this.bulletRadius), (float) (top + bottom) / 2.0F,
                        (float) this.bulletRadius, paint);
      }

      int oldColor = paint.getColor();
      paint.setColor(oldColor);
      Paint.Style style = paint.getStyle();
      paint.setStyle(style);
    }

  }

}
