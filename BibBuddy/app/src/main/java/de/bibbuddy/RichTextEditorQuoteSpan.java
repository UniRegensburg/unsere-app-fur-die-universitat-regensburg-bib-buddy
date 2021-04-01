package de.bibbuddy;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.text.Layout;
import android.text.style.QuoteSpan;


/**
 * The RichTextEditorQuoteSpan is responsible for styling quote spans in text notes.
 *
 * @author Sabrina Freisleben.
 */
public class RichTextEditorQuoteSpan extends QuoteSpan {

  private final int quoteColor;
  private final int quoteStripeWidth = 10;
  private final int quoteGapWidth = 10;

  public RichTextEditorQuoteSpan() {
    this.quoteColor = R.color.gray_dark;
  }

  /**
   * Put default quote settings to a parcel.
   *
   * @param dest  parcel to put the quote settings.
   * @param flags how the settings should be written.
   */
  public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeInt(this.quoteColor);
    dest.writeInt(this.quoteStripeWidth);
    dest.writeInt(this.quoteGapWidth);
  }

  /**
   * Fetch the leading margin between the quote-stripe and the first text character.
   *
   * @return the leading margin for the first character position.
   */
  public int getLeadingMargin(boolean first) {
    return this.quoteStripeWidth + this.quoteGapWidth;
  }

  /**
   * Put a margin between the quote-stripe and the first text character.
   *
   * @param p      paint to use for the margin.
   * @param x      x-start-position to set rect bounds.
   * @param dir    int of the x-direction (+ or -) to calculate the x-end-position to set rect
   *               bounds.
   * @param top    y-start-position to set rect bounds.
   * @param bottom y-end-position to set rect bounds.
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
