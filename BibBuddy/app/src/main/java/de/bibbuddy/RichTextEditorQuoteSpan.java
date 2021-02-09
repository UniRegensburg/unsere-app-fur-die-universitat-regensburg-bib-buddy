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
     * @param quoteColor id for the color to apply to the quote stripe
     * @param quoteStripeWidth width of the quote stripe
     * @param quoteGapWidth wifth between quote stripe end and first character in the same line
     */
  public RichTextEditorQuoteSpan(int quoteColor, int quoteStripeWidth, int quoteGapWidth) {
    this.quoteColor = quoteColor;
    this.quoteStripeWidth = quoteStripeWidth != 0 ? quoteStripeWidth : 2;
    this.quoteGapWidth = quoteGapWidth != 0 ? quoteGapWidth : 2;
  }

    /**
     * This method puts the default quote component settings to a parcel
     *
     * @param dest parcel to put the component settings in
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
     * @param first
     * @return
     */
  public int getLeadingMargin(boolean first) {
    return this.quoteStripeWidth + this.quoteGapWidth;
  }

  public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline,
                                int bottom, CharSequence text, int start, int end, boolean first,
                                Layout layout) {
    Paint.Style style = p.getStyle();
    int color = p.getColor();
    p.setStyle(Paint.Style.FILL);
    p.setColor(this.quoteColor);
    c.drawRect((float) x, (float) top, (float) (x + dir * this.quoteGapWidth), (float) bottom, p);
    p.setStyle(style);
    p.setColor(color);
  }

}
