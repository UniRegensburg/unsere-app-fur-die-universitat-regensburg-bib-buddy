package de.bibbuddy;

import android.graphics.Canvas;
import android.graphics.Paint;
import android.os.Parcel;
import android.text.Layout;
import android.text.style.QuoteSpan;

/**
 * RichTextEditorQuoteSpan is responsible for styling quote spans in text notes.
 *
 * @author Sabrina Freisleben
 */
public class RichTextEditorQuoteSpan extends QuoteSpan {

  private final int quoteColor;
  private final int quoteStripeWidth = 10;
  private final int quoteGapWidth = 10;

  public RichTextEditorQuoteSpan() {
    this.quoteColor = R.color.gray_dark;
  }

  /**
   * Puts default quote settings to a parcel.
   *
   * @param dest  parcel to put the quote settings
   * @param flags how the settings should be written
   */
  public void writeToParcel(Parcel dest, int flags) {
    super.writeToParcel(dest, flags);
    dest.writeInt(this.quoteColor);
    dest.writeInt(this.quoteStripeWidth);
    dest.writeInt(this.quoteGapWidth);
  }

  /**
   * Fetches the leading margin between the quote-stripe and the first text character.
   *
   * @return the leading margin for the first character position
   */
  public int getLeadingMargin(boolean first) {
    return this.quoteStripeWidth + this.quoteGapWidth;
  }

  /**
   * Puts a margin between the quote-stripe and the first text character.
   *
   * @param paint     paint to use for the margin
   * @param x         x-start-position to set rect bounds
   * @param direction int of the x-direction (+ or -) to calculate the x-end-position to set rect
   *                  bounds
   * @param top       y-start-position to set rect bounds
   * @param bottom    y-end-position to set rect bounds
   */
  public void drawLeadingMargin(Canvas canvas, Paint paint, int x, int direction, int top,
                                int baseline, int bottom, CharSequence text, int start, int end,
                                boolean first, Layout layout) {
    paint.setStyle(Paint.Style.FILL);
    paint.setColor(this.quoteColor);
    canvas.drawRect((float) x, (float) top, (float) (x + direction * this.quoteGapWidth),
                    (float) bottom,
                    paint);

    Paint.Style style = paint.getStyle();
    paint.setStyle(style);
    int color = paint.getColor();
    paint.setColor(color);
  }

}
