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
 * The RichTextEditorBulletSpan is responsible for the bullets in the text note.
 *
 * @author Sabrina Freisleben
 */
public class RichTextEditorBulletSpan extends BulletSpan {

  private static Path bulletPath = null;
  private final int bulletColor = Color.BLACK;
  private final int bulletRadius = 10;
  private final int bulletGapWidth = 20;

  /**
   * This method is called to write a parcel for the bullet span.
   *
   * @param dest  A parcel for the bullet settings
   * @param flags Used Flags for bullet settings
   */
  public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeInt(this.bulletColor);
    dest.writeInt(this.bulletRadius);
    dest.writeInt(this.bulletGapWidth);
  }

  /**
   * This method is called to fetch the margin between bullet and text.
   *
   * @return Returns the distance between bullets and texts in line in pixels.
   */
  public int getLeadingMargin(boolean first) {
    return 2 * this.bulletRadius + this.bulletGapWidth;
  }

  /**
   * This method is called to draw the leading margin for the bullets.
   *
   * @param c      Canvas to draw on
   * @param p      Paint used to draw
   * @param x      X-Coordinate to setup the canvas
   * @param dir    X-Coordinate to give the canvas a direction to draw (+ or -)
   * @param top    Y-Coordinate to set the top of the canvas
   * @param bottom Y-coordinate to set the bottom of the canvas
   * @param text   Contained text
   * @param start  Start of the text
   */
  public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline,
                                int bottom, CharSequence text, int start, int end, boolean first,
                                Layout l) {
    if (((Spanned) text).getSpanStart(this) == start) {
      p.setColor(this.bulletColor);
      p.setStyle(Paint.Style.FILL);
      if (c.isHardwareAccelerated()) {
        if (bulletPath == null) {
          bulletPath = new Path();
          bulletPath.addCircle(0.0F, 0.0F, (float) this.bulletRadius, Path.Direction.CW);
        }

        c.save();
        c.translate((float) (x + dir * this.bulletRadius), (float) (top + bottom) / 2.0F);
        c.drawPath(bulletPath, p);
        c.restore();
      } else {
        c.drawCircle((float) (x + dir * this.bulletRadius), (float) (top + bottom) / 2.0F,
            (float) this.bulletRadius, p);
      }
      int oldColor = p.getColor();
      p.setColor(oldColor);
      Paint.Style style = p.getStyle();
      p.setStyle(style);
    }

  }

}
