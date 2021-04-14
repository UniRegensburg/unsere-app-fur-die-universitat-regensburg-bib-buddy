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
 * @author Sabrina Freisleben.
 */
public class RichTextEditorBulletSpan extends BulletSpan {

  private static Path bulletPath = null;

  private final int bulletColor = Color.BLACK;
  private final int bulletRadius = 10;
  private final int bulletGapWidth = 20;

  /**
   * Put default bullet span settings to a parcel.
   *
   * @param dest  parcel to put the bullet settings.
   * @param flags how the settings should be written.
   */
  public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeInt(this.bulletColor);
    dest.writeInt(this.bulletRadius);
    dest.writeInt(this.bulletGapWidth);
  }

  /**
   * Fetch the margin between the bullet and the first character of the text.
   *
   * @return the distance between the bullet and the first character in pixels.
   */
  public int getLeadingMargin(boolean first) {
    return 2 * this.bulletRadius + this.bulletGapWidth;
  }

  /**
   * Draw the leading margin for bullets.
   *
   * @param c      canvas to draw on.
   * @param p      paint used to draw.
   * @param x      x-coordinate to setup the canvas.
   * @param dir    x-coordinate to give the canvas a direction to draw (+ or -).
   * @param top    y-coordinate to set the top of the canvas.
   * @param bottom y-coordinate to set the bottom of the canvas.
   * @param text   text content as charSequence.
   * @param start  first character position of the text.
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
