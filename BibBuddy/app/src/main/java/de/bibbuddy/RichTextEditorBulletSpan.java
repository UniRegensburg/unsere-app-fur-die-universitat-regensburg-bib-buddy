package de.bibbuddy;

import android.graphics.Canvas;
import android.graphics.Color;
import android.graphics.Paint;
import android.graphics.Path;
import android.os.Parcel;
import android.text.Layout;
import android.text.Spanned;
import android.text.style.BulletSpan;

public class RichTextEditorBulletSpan extends BulletSpan {

    private static Path bulletPath = null;
    private final int bulletColor = Color.BLACK;
    private final int bulletRadius = 10;
    private final int bulletGapWidth = 20;

    public RichTextEditorBulletSpan() {
    }

    public void writeToParcel(Parcel dest, int flags) {
        super.writeToParcel(dest, flags);
        dest.writeInt(this.bulletColor);
        dest.writeInt(this.bulletRadius);
        dest.writeInt(this.bulletGapWidth);
    }

    public int getLeadingMargin(boolean first) {
        return 2 * this.bulletRadius + this.bulletGapWidth;
    }

    public void drawLeadingMargin(Canvas c, Paint p, int x, int dir, int top, int baseline, int bottom, CharSequence text, int start, int end, boolean first, Layout l) {
        if (((Spanned) text).getSpanStart(this) == start) {
            Paint.Style style = p.getStyle();
            int oldColor = p.getColor();
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
                c.drawCircle((float) (x + dir * this.bulletRadius), (float) (top + bottom) / 2.0F, (float) this.bulletRadius, p);
            }

            p.setColor(oldColor);
            p.setStyle(style);
        }

    }

}
