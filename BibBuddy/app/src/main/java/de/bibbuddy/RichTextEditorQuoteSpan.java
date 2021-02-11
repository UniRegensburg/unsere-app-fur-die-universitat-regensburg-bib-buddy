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
   * Constructor to setup default color, stripeWidth and gapWidth for quotes.
   *
   * @param quoteColor       id for the color to apply to the quote stripe
   * @param quoteStripeWidth width of the quote stripe
   * @param quoteGapWidth    wifth between quote stripe end and first character in the same line
   */
  public RichTextEditorQuoteSpan(int quoteColor, int quoteStripeWidth, int quoteGapWidth) {
    this.quoteColor = quoteColor;
    this.quoteStripeWidth = quoteStripeWidth != 0 ? quoteStripeWidth : 2;
    this.quoteGapWidth = quoteGapWidth != 0 ? quoteGapWidth : 2;
  }

  /**
   * This method puts the default quote component settings to a parcel.
   *
   * @param dest  parcel to put the component settings in
   * @param flags flags how the object should be written
   */
  public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeInt(this.quoteColor);
    dest.writeInt(this.quoteStripeWidth);
    dest.writeInt(this.quoteGapWidth);
  }

  /**
   * This method fetches the leading margin for first character of a quote line
   * from the parent view start.
   *
   * @return returns a leading margin for the first character position
   */
  public int getLeadingMargin(boolean first) {
    return this.quoteStripeWidth + this.quoteGapWidth;
  }

  /**
   * Method to draw a margin for the first character to start in quote line.
   *
   * @param p      paint to use for the margin
   * @param x      x-position to set drawing rect bounds
   * @param dir    int to define the x-direction for setting drawing rect bounds
   * @param top    y-start-position to set drawing rect bounds
   * @param bottom y-end-position to set drawing rect bounds
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
