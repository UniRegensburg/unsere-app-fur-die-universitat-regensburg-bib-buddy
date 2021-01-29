package de.bibbuddy;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.text.Layout;
import android.text.style.QuoteSpan;

public class RichTextEditorQuoteSpan extends QuoteSpan {

  private final int quoteColor;
  private final int quoteStripeWidth;
  private final int quoteGapWidth;

  /**
   * This constructor create a custom quoteSpan with color, stripewidth and gapwidth set.
   *
   * @param quoteColor       Color of the quote text
   * @param quoteStripeWidth Width for the stripe at the start of each quote text line
   * @param quoteGapWidth    Width for the gap between the stripe and the text in line
   */
  public RichTextEditorQuoteSpan(int quoteColor, int quoteStripeWidth, int quoteGapWidth) {
    this.quoteColor = quoteColor != 0 ? quoteColor : -16776961;
    this.quoteStripeWidth = quoteStripeWidth != 0 ? quoteStripeWidth : 2;
    this.quoteGapWidth = quoteGapWidth != 0 ? quoteGapWidth : 2;
  }

  /**
   * This method is called to create a parcel for quote settings.
   *
   * @param dest  A parcel for the quote settings
   * @param flags Used Flags for quote settings
   */
  public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeInt(this.quoteColor);
    dest.writeInt(this.quoteStripeWidth);
    dest.writeInt(this.quoteGapWidth);
  }

  /**
   * This method is called to fetch the leading margin for the quote.
   *
   * @return Returns distance for leading margin by adding up stripe and gap width
   */
  public int getLeadingMargin(boolean first) {
    return this.quoteStripeWidth + this.quoteGapWidth;
  }

  /**
   * This method is called to draw the leading margin for quotes.
   *
   * @param c      Canvas to draw on
   * @param p      Paint used to draw
   * @param x      X-Coordinate to setup the canvas
   * @param dir    X-Coordinate to give the canvas a direction to draw (+ or -)
   * @param top    Y-Coordinate to set the top of the canvas
   * @param bottom Y-coordinate to set the bottom of the canvas
   */
  public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline,
                                int bottom, CharSequence text, int start, int end, boolean first,
                                Layout layout) {
    p.setStyle(Paint.Style.FILL);
    p.setColor(this.quoteColor);
    c.drawRect((float) x, (float) top, (float) (x + dir * this.quoteGapWidth), (float) bottom, p);
    Paint.Style style = p.getStyle();
    p.setStyle(style);
    int color = p.getColor();
    p.setColor(color);
  }

}
