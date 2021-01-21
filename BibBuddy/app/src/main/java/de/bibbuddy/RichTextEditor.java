package de.bibbuddy;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.Layout;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.QuoteSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

import androidx.appcompat.widget.AppCompatEditText;

public class RichTextEditor extends AppCompatEditText implements TextWatcher {
    public static final int FORMAT_NORMAL = 0;
    public static final int FORMAT_BOLD = 1;
    public static final int FORMAT_ITALIC = 2;
    public static final int FORMAT_UNDERLINE = 3;
    public static final int FORMAT_STRIKETHROUGH = 4;
    public static final int FORMAT_BULLET = 5;
    public static final int FORMAT_QUOTE = 6;
    
    private final List<Editable> historyList = new LinkedList();
    private boolean historyEnable = true;
    private int historySize = 100;
    private boolean historyWorking = false;
    private int historyCursor = 0;
    private SpannableStringBuilder inputBefore;
    private Editable inputLast;
    private int lastCursorPosition;
    private int endOfString;

    private boolean bold = false;
    private boolean italic = false;
    private boolean underline = false;
    private boolean strikethrough = false;
    private boolean bullet = false;
    private boolean quote = false;
    private boolean alignmentLeft = false;
    private boolean alignmentRight = false;
    private boolean alignmentCenter = false;

    public RichTextEditor(Context context) {
        super(context);
        this.init(null);
    }

    public RichTextEditor(Context context, AttributeSet attrs) {
        super(context, attrs);
        this.init(attrs);
    }

    public RichTextEditor(Context context, AttributeSet attrs, int defStyleAttr) {
        super(context, attrs, defStyleAttr);
        this.init(attrs);
    }

    private void init(AttributeSet attrs) {
        TypedArray array = this.getContext().obtainStyledAttributes(attrs, R.styleable.RichTextEditor);

        this.historyEnable = array.getBoolean(R.styleable.RichTextEditor_historyEnable, true);
        this.historySize = array.getInt(R.styleable.RichTextEditor_historySize, 100);

        array.recycle();
        if (this.historyEnable && this.historySize <= 0) {
            throw new IllegalArgumentException("historySize must > 0");
        }
    }

    protected void onAttachedToWindow() {
        super.onAttachedToWindow();
        this.addTextChangedListener(this);
    }

    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        this.removeTextChangedListener(this);
    }

    public void bold(boolean valid) {
        if (valid) {
            this.styleValid(1, this.getSelectionStart(), this.getSelectionEnd());
            bold = !bold;
        } else {
            this.styleInvalid(1, this.getSelectionStart(), this.getSelectionEnd());
        }
        lastCursorPosition = this.getSelectionStart();
    }

    public void italic(boolean valid) {
        if (valid) {
            this.styleValid(2, this.getSelectionStart(), this.getSelectionEnd());
            italic = !italic;
        } else {
            this.styleInvalid(2, this.getSelectionStart(), this.getSelectionEnd());
        }
        lastCursorPosition = this.getSelectionStart();
    }

    protected void styleValid(int style, int start, int end) {
        switch (style) {
            case 0:
            case 1:
            case 2:
            case 3:
                if (start >= end) {
                    return;
                }
                this.getEditableText().setSpan(new StyleSpan(style), start, end, 33);
                return;
            default:
        }
    }

    protected void styleInvalid(int style, int start, int end) {
        switch (style) {
            case 0:
            case 1:
            case 2:
            case 3:
                if (start >= end) {
                    return;
                } else {
                    StyleSpan[] spans = this.getEditableText().getSpans(start, end, StyleSpan.class);
                    ArrayList<RichTextEditorPart> list = new ArrayList();
                    StyleSpan[] arrSpan = spans;
                    int spanLength = spans.length;

                    for (int i = 0; i < spanLength; i++) {
                        StyleSpan span = arrSpan[i];
                        if (span.getStyle() == style) {
                            list.add(new RichTextEditorPart(this.getEditableText().getSpanStart(span), this.getEditableText().getSpanEnd(span)));
                            this.getEditableText().removeSpan(span);
                        }
                    }

                    for (RichTextEditorPart part : list) {
                        if (part.isValid()) {
                            if (part.getStart() < start) {
                                this.styleValid(style, part.getStart(), start);
                            }

                            if (part.getEnd() > end) {
                                this.styleValid(style, end, part.getEnd());
                            }
                        }
                    }

                    return;
                }
            default:
        }
    }

    protected boolean containStyle(int style, int start, int end) {
        switch (style) {
            case 0:
            case 1:
            case 2:
            case 3:
                if (start > end) {
                    return false;
                } else {
                    if (start == end) {
                        if (start - 1 >= 0 && start + 1 <= this.getEditableText().length()) {
                            StyleSpan[] before = this.getEditableText().getSpans(start - 1, start, StyleSpan.class);
                            StyleSpan[] after = this.getEditableText().getSpans(start, start + 1, StyleSpan.class);
                            return before.length > 0 && after.length > 0 && before[0].getStyle() == style && after[0].getStyle() == style;
                        }

                        return false;
                    }

                    StringBuilder builder = new StringBuilder();
                    int i = start;

                    for (; i < end; i++) {
                        StyleSpan[] spans = this.getEditableText().getSpans(i, i + 1, StyleSpan.class);
                        StyleSpan[] arrSpan = spans;
                        int spanLength = spans.length;

                        for (int j = 0; j < spanLength; j++) {
                            StyleSpan span = arrSpan[i];
                            if (span.getStyle() == style) {
                                builder.append(this.getEditableText().subSequence(i, i + 1).toString());
                                break;
                            }
                        }
                    }

                    return this.getEditableText().subSequence(start, end).toString().equals(builder.toString());
                }
            default:
                return false;
        }
    }

    public void underline(boolean valid) {
        if (valid) {
            this.underlineValid(this.getSelectionStart(), this.getSelectionEnd());
            underline = !underline;
        } else {
            this.underlineInvalid(this.getSelectionStart(), this.getSelectionEnd());
        }
        lastCursorPosition = getSelectionStart();
    }

    protected void underlineValid(int start, int end) {
        if (start < end) {
            this.getEditableText().setSpan(new UnderlineSpan(), start, end, 33);
        }
    }

    protected void underlineInvalid(int start, int end) {
        if (start < end) {
            UnderlineSpan[] spans = this.getEditableText().getSpans(start, end, UnderlineSpan.class);
            List<RichTextEditorPart> list = new ArrayList();
            UnderlineSpan[] arrSpan = spans;
            int spanLength = spans.length;

            for (int i = 0; i < spanLength; i++) {
                UnderlineSpan span = arrSpan[i];
                list.add(new RichTextEditorPart(this.getEditableText().getSpanStart(span), this.getEditableText().getSpanEnd(span)));
                this.getEditableText().removeSpan(span);
            }

            Iterator i = list.iterator();

            while (i.hasNext()) {
                RichTextEditorPart part = (RichTextEditorPart) i.next();
                if (part.isValid()) {
                    if (part.getStart() < start) {
                        this.underlineValid(part.getStart(), start);
                    }

                    if (part.getEnd() > end) {
                        this.underlineValid(end, part.getEnd());
                    }
                }
            }

        }
    }

    protected boolean containUnderline(int start, int end) {
        if (start > end) {
            return false;
        } else if (start != end) {
            StringBuilder builder = new StringBuilder();

            for (int i = start; i < end; i++) {
                if (this.getEditableText().getSpans(i, i + 1, UnderlineSpan.class).length > 0) {
                    builder.append(this.getEditableText().subSequence(i, i + 1).toString());
                }
            }

            return this.getEditableText().subSequence(start, end).toString().equals(builder.toString());
        } else if (start - 1 >= 0 && start + 1 <= this.getEditableText().length()) {
            UnderlineSpan[] before = this.getEditableText().getSpans(start - 1, start, UnderlineSpan.class);
            UnderlineSpan[] after = this.getEditableText().getSpans(start, start + 1, UnderlineSpan.class);
            return before.length > 0 && after.length > 0;
        } else {
            return false;
        }
    }

    public void strikethrough(boolean valid) {
        if (valid) {
            this.strikethroughValid(this.getSelectionStart(), this.getSelectionEnd());
            strikethrough = !strikethrough;
        } else {
            this.strikethroughInvalid(this.getSelectionStart(), this.getSelectionEnd());
        }
        lastCursorPosition = getSelectionStart();
    }

    protected void strikethroughValid(int start, int end) {
        if (start < end) {
            this.getEditableText().setSpan(new StrikethroughSpan(), start, end, 33);
        }
    }

    protected void strikethroughInvalid(int start, int end) {
        if (start < end) {
            StrikethroughSpan[] spans = this.getEditableText().getSpans(start, end, StrikethroughSpan.class);
            List<RichTextEditorPart> list = new ArrayList();
            StrikethroughSpan[] arrSpan = spans;
            int spanLength = spans.length;

            for (int i = 0; i < spanLength; i++) {
                StrikethroughSpan span = arrSpan[i];
                list.add(new RichTextEditorPart(this.getEditableText().getSpanStart(span), this.getEditableText().getSpanEnd(span)));
                this.getEditableText().removeSpan(span);
            }

            Iterator i = list.iterator();

            while (i.hasNext()) {
                RichTextEditorPart part = (RichTextEditorPart) i.next();
                if (part.isValid()) {
                    if (part.getStart() < start) {
                        this.strikethroughValid(part.getStart(), start);
                    }

                    if (part.getEnd() > end) {
                        this.strikethroughValid(end, part.getEnd());
                    }
                }
            }

        }
    }

    protected boolean containStrikethrough(int start, int end) {
        if (start > end) {
            return false;
        } else if (start != end) {
            StringBuilder builder = new StringBuilder();

            for (int i = start; i < end; i++) {
                if (this.getEditableText().getSpans(i, i + 1, StrikethroughSpan.class).length > 0) {
                    builder.append(this.getEditableText().subSequence(i, i + 1).toString());
                }
            }

            return this.getEditableText().subSequence(start, end).toString().equals(builder.toString());
        } else if (start - 1 >= 0 && start + 1 <= this.getEditableText().length()) {
            StrikethroughSpan[] before = this.getEditableText().getSpans(start - 1, start, StrikethroughSpan.class);
            StrikethroughSpan[] after = this.getEditableText().getSpans(start, start + 1, StrikethroughSpan.class);
            return before.length > 0 && after.length > 0;
        } else {
            return false;
        }
    }

    public void bullet(boolean valid) {
        if (valid) {
            this.bulletValid();
            bullet = !bullet;
        } else {
            this.bulletInvalid();
        }
        lastCursorPosition = getSelectionStart();
    }

    protected void bulletValid() {
        String[] lines = TextUtils.split(this.getEditableText().toString(), "\n");

        for (int i = 0; i < lines.length; i++) {
            if (!this.containBullet(i)) {
                int lineStart = 0;

                int lineEnd;
                for (lineEnd = 0; lineEnd < i; lineEnd++) {
                    lineStart = lineStart + lines[lineEnd].length() + 1;
                }

                lineEnd = lineStart + lines[i].length();
                if (lineStart < lineEnd) {
                    int bulletStart = 0;
                    int bulletEnd = 0;
                    if (lineStart <= this.getSelectionStart() && this.getSelectionEnd() <= lineEnd) {
                        bulletStart = lineStart;
                        bulletEnd = lineEnd;
                    } else if (this.getSelectionStart() <= lineStart && lineEnd <= this.getSelectionEnd()) {
                        bulletStart = lineStart;
                        bulletEnd = lineEnd;
                    }

                    if (bulletStart < bulletEnd) {
                        this.getEditableText().setSpan(new RichTextEditorBulletSpan(), bulletStart, bulletEnd, 33);
                    }
                }
            }
        }

    }

    protected void bulletInvalid() {
        String[] lines = TextUtils.split(this.getEditableText().toString(), "\n");

        for (int i = 0; i < lines.length; i++) {
            if (this.containBullet(i)) {
                int lineStart = 0;

                int lineEnd;
                for (lineEnd = 0; lineEnd < i; lineEnd++) {
                    lineStart = lineStart + lines[lineEnd].length() + 1;
                }

                lineEnd = lineStart + lines[i].length();
                if (lineStart < lineEnd) {
                    int bulletStart = 0;
                    int bulletEnd = 0;
                    if (lineStart <= this.getSelectionStart() && this.getSelectionEnd() <= lineEnd) {
                        bulletStart = lineStart;
                        bulletEnd = lineEnd;
                    } else if (this.getSelectionStart() <= lineStart && lineEnd <= this.getSelectionEnd()) {
                        bulletStart = lineStart;
                        bulletEnd = lineEnd;
                    }

                    if (bulletStart < bulletEnd) {
                        BulletSpan[] spans = this.getEditableText().getSpans(bulletStart, bulletEnd, BulletSpan.class);
                        BulletSpan[] arrSpan = spans;
                        int spanLength = spans.length;

                        for (int j = 0; j < spanLength; j++) {
                            BulletSpan span = arrSpan[i];
                            this.getEditableText().removeSpan(span);
                        }
                    }
                }
            }
        }
    }

    protected boolean containBullet() {
        String[] lines = TextUtils.split(this.getEditableText().toString(), "\n");
        List<Integer> list = new ArrayList();

        for (int i = 0; i < lines.length; i++) {
            int lineStart = 0;

            int lineEnd;
            for (lineEnd = 0; lineEnd < i; lineEnd++) {
                lineStart = lineStart + lines[lineEnd].length() + 1;
            }

            lineEnd = lineStart + lines[i].length();
            if (lineStart < lineEnd) {
                if (lineStart <= this.getSelectionStart() && this.getSelectionEnd() <= lineEnd) {
                    list.add(i);
                } else if (this.getSelectionStart() <= lineStart && lineEnd <= this.getSelectionEnd()) {
                    list.add(i);
                }
            }
        }

        Iterator iterator = list.iterator();

        Integer i;
        do {
            if (!iterator.hasNext()) {
                return true;
            }

            i = (Integer) iterator.next();
        } while (this.containBullet(i));

        return false;
    }

    protected boolean containBullet(int index) {
        String[] lines = TextUtils.split(this.getEditableText().toString(), "\n");
        if (index >= 0 && index < lines.length) {
            int start = 0;

            int end;
            for (end = 0; end < index; end++) {
                start = start + lines[end].length() + 1;
            }

            end = start + lines[index].length();
            if (start >= end) {
                return false;
            } else {
                BulletSpan[] spans = this.getEditableText().getSpans(start, end, BulletSpan.class);
                return spans.length > 0;
            }
        } else {
            return false;
        }
    }

    public void quote(boolean valid) {
        if (valid) {
            this.quoteValid();
            quote = !quote;
        } else {
            this.quoteInvalid();
        }
        lastCursorPosition = getSelectionStart();
    }

    protected void quoteValid() {
        String[] lines = TextUtils.split(this.getEditableText().toString(), "\n");
        for (int i = 0; i < lines.length; i++) {
            if (!this.containQuote(i)) {
                int lineStart = 0;
                int lineEnd;

                for (lineEnd = 0; lineEnd < i; lineEnd++) {
                    lineStart = lineStart + lines[lineEnd].length() + 1;
                }

                lineEnd = lineStart + lines[i].length();
                if (lineStart < lineEnd) {
                    int quoteStart = 0;
                    int quoteEnd = 0;
                    if (lineStart <= this.getSelectionStart() && this.getSelectionEnd() <= lineEnd) {
                        quoteStart = lineStart;
                        quoteEnd = lineEnd;
                    } else if (this.getSelectionStart() <= lineStart && lineEnd <= this.getSelectionEnd()) {
                        quoteStart = lineStart;
                        quoteEnd = lineEnd;
                    }
                    if (quoteStart < quoteEnd) {
                        int quoteColor = 0;
                        int quoteStripeWidth = 0;
                        int quoteGapWidth = 0;
                        this.getEditableText().setSpan(new RichTextEditorQuoteSpan(quoteColor, quoteStripeWidth, quoteGapWidth), quoteStart, quoteEnd, 33);
                        styleValid(FORMAT_ITALIC, quoteStart, quoteEnd);
                        this.getEditableText().setSpan(new BackgroundColorSpan(getResources().getColor(R.color.gray_quote)), quoteStart, quoteEnd, 33);
                    }
                }
            }
        }
    }

    protected void quoteInvalid() {
        String[] lines = TextUtils.split(this.getEditableText().toString(), "\n");
        for (int i = 0; i < lines.length; i++) {
            if (this.containQuote(i)) {

                int lineStart = 0;
                int lineEnd = 1;

                for (lineEnd = 0; lineEnd < i; lineEnd++) {
                    lineStart = lineStart + lines[lineEnd].length() + 1;
                }

                lineEnd = lineStart + lines[i].length();

                if (lineStart < lineEnd) {
                    int quoteStart = 0;
                    int quoteEnd = 0;

                    if (lineStart <= this.getSelectionStart() && this.getSelectionEnd() <= lineEnd) {
                        quoteStart = lineStart;
                        quoteEnd = lineEnd;
                    } else if (this.getSelectionStart() <= lineStart && lineEnd <= this.getSelectionEnd()) {
                        quoteStart = lineStart;
                        quoteEnd = lineEnd;
                    }

                    if (quoteStart < quoteEnd) {
                        QuoteSpan[] spans = this.getEditableText().getSpans(quoteStart, quoteEnd, QuoteSpan.class);
                        int spanLength = spans.length;
                        for (int j = 0; j < spanLength; j++) {
                            QuoteSpan span = spans[j];
                            this.getEditableText().removeSpan(span);
                        }
                    }
                }
            }
        }

    }

    protected boolean containQuote() {
        String[] lines = TextUtils.split(this.getEditableText().toString(), "\n");
        List<Integer> list = new ArrayList();

        for (int i = 0; i < lines.length; i++) {
            int lineStart = 0;

            int lineEnd;
            for (lineEnd = 0; lineEnd < i; lineEnd++) {
                lineStart = lineStart + lines[lineEnd].length() + 1;
            }

            lineEnd = lineStart + lines[i].length();
            if (lineStart < lineEnd) {
                if (lineStart <= this.getSelectionStart() && this.getSelectionEnd() <= lineEnd) {
                    list.add(i);
                } else if (this.getSelectionStart() <= lineStart && lineEnd <= this.getSelectionEnd()) {
                    list.add(i);
                }
            }
        }

        Iterator iterator = list.iterator();

        Integer i;
        do {
            if (!iterator.hasNext()) {
                return true;
            }

            i = (Integer) iterator.next();
        } while (this.containQuote(i));

        return false;
    }

    protected boolean containQuote(int index) {
        String[] lines = TextUtils.split(this.getEditableText().toString(), "\n");
        if (index >= 0 && index < lines.length) {
            int start = 0;
            int end;
            for (end = 0; end < index; end++) {
                start = start + lines[end].length() + 1;
            }

            end = start + lines[index].length();
            if (start >= end) {
                return false;
            } else {
                QuoteSpan[] spans = this.getEditableText().getSpans(start, end, QuoteSpan.class);
                return spans.length > 0;
            }
        } else {
            return false;
        }
    }

    protected void alignLeft() {
        this.alignmentValid(1, this.getSelectionStart(), this.getSelectionEnd());
        alignmentLeft = !alignmentLeft;
        lastCursorPosition = this.getSelectionStart();
    }

    protected void alignRight() {
        if (!alignmentRight) {
            alignmentRight = true;
            this.alignmentValid(2, this.getSelectionStart(), this.getSelectionEnd());
        } else {
            alignmentRight = false;
            this.alignmentValid(1, this.getSelectionStart(), this.getSelectionEnd());
        }
        lastCursorPosition = this.getSelectionStart();
    }

    protected void alignCenter() {
        this.alignmentValid(3, this.getSelectionStart(), this.getSelectionEnd());
        if (!alignmentCenter) {
            alignmentCenter = true;
        } else {
            alignmentCenter = false;
            this.alignmentValid(1, this.getSelectionStart(), this.getSelectionEnd());
        }
        lastCursorPosition = this.getSelectionStart();
    }

    protected void alignmentValid(int style, int start, int end) {
        if (!hasSelection()) {
            String[] lines = TextUtils.split(this.getEditableText().toString(), "\n");
            for (int i = 0; i < lines.length; i++) {
                int lineStart = 0;
                int lineEnd;
                for (lineEnd = 0; lineEnd < i; lineEnd++) {
                    lineStart = lineStart + lines[lineEnd].length() + 1;
                }

                lineEnd = lineStart + lines[i].length();

                if (lineStart < lineEnd) {
                    if (lineStart <= this.getSelectionStart() && this.getSelectionEnd() <= lineEnd) {
                        start = lineStart;
                        end = lineEnd;
                    } else if (this.getSelectionStart() <= lineStart && lineEnd <= this.getSelectionEnd()) {
                        start = lineStart;
                        end = lineEnd;
                    }
                }

            }

        }
        adjustAlignment(style, start, end);
    }

    private void adjustAlignment(int style, int start, int end){
        switch (style) {
            case 0:
            case 1:
                if (start >= end) {
                    this.getEditableText().setSpan(new AlignmentSpan() {

                        @Override
                        public Layout.Alignment getAlignment() {
                            Alignment alignment = Alignment.ALIGN_NORMAL;
                            return alignment;
                        }
                    }, start, end, 33);
                }
                this.getEditableText().setSpan(new AlignmentSpan() {

                    @Override
                    public Layout.Alignment getAlignment() {
                        Alignment alignment = Alignment.ALIGN_NORMAL;
                        return alignment;
                    }
                }, start, end, 33);
                break;
            case 2:
                if (start >= end) {
                    return;
                }
                this.getEditableText().setSpan(new AlignmentSpan() {

                    @Override
                    public Layout.Alignment getAlignment() {
                        Alignment alignment = Alignment.ALIGN_OPPOSITE;
                        return alignment;
                    }
                }, start, end, 33);
                break;
            case 3:
                if (start >= end) {
                    return;
                }
                this.getEditableText().setSpan(new AlignmentSpan() {

                    @Override
                    public Layout.Alignment getAlignment() {
                        Alignment alignment = Alignment.ALIGN_CENTER;
                        return alignment;
                    }
                }, start, end, 33);
                break;
            default:
                break;
        }
    }

    public void redo() {
        if (this.redoValid()) {
            this.historyWorking = true;
            if (this.historyCursor >= this.historyList.size() - 1) {
                this.historyCursor = this.historyList.size();
                this.setText(this.inputLast);
            } else {
                this.historyCursor++;
                this.setText(this.historyList.get(this.historyCursor));
            }
            this.setSelection(this.getEditableText().length());
            this.historyWorking = false;
        } else {
            return;
        }
    }

    public void undo() {
        if (this.undoValid()) {
            this.historyWorking = true;
            --this.historyCursor;
            this.setText(this.historyList.get(this.historyCursor));
            this.setSelection(this.getEditableText().length());
            this.historyWorking = false;
        }
    }

    public boolean redoValid() {
        if (this.historyEnable && this.historySize > 0 && this.historyList.size() > 0 && !this.historyWorking) {
            return this.historyCursor < this.historyList.size() - 1 || this.historyCursor >= this.historyList.size() - 1 && this.inputLast != null;
        } else {
            return false;
        }
    }

    public boolean undoValid() {
        if (this.historyEnable && this.historySize > 0 && !this.historyWorking) {
            return this.historyList.size() > 0 && this.historyCursor > 0;
        } else {
            return false;
        }
    }

    public boolean contains(int format) {
        switch (format) {
            case 1:
                return this.containStyle(1, this.getSelectionStart(), this.getSelectionEnd());
            case 2:
                return this.containStyle(2, this.getSelectionStart(), this.getSelectionEnd());
            case 3:
                return this.containUnderline(this.getSelectionStart(), this.getSelectionEnd());
            case 4:
                return this.containStrikethrough(this.getSelectionStart(), this.getSelectionEnd());
            case 5:
                return this.containBullet();
            case 6:
                return this.containQuote();
            default:
                return false;
        }
    }

    public void beforeTextChanged(CharSequence text, int start, int count, int after) {
        if (this.historyEnable && !this.historyWorking) {
            this.inputBefore = new SpannableStringBuilder(text);
        }
    }

    @Override
    public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
        Spannable str = this.getText();
        endOfString = text.toString().length();
            /* Avoid crash when user is backspacing the entire text after text format has been changed
                by adjusting lastCursorPosition to avoid span start < span end
             */
        if (endOfString < lastCursorPosition) {
            lastCursorPosition = 0;
        }
        if (bold) {
            styleValid(FORMAT_BOLD, lastCursorPosition, endOfString);
        }
        if (italic) {
            styleValid(FORMAT_ITALIC, lastCursorPosition, endOfString);
        }
        if (underline) {
            underlineValid(lastCursorPosition, endOfString);
        }
        if (strikethrough) {
            strikethroughValid(lastCursorPosition, endOfString);
        }
        if (bullet) {
            bulletValid();
        }
        if (quote) {
            quoteValid();
            styleValid(FORMAT_ITALIC, lastCursorPosition, endOfString);
            str.setSpan(new BackgroundColorSpan(getResources().getColor(R.color.gray_quote)), lastCursorPosition, endOfString, 33);
        }
        if (alignmentLeft) {
            alignmentValid(1, lastCursorPosition, endOfString);
        }
        if (alignmentRight) {
            alignmentValid(2, lastCursorPosition, endOfString);
        }
        if (alignmentCenter) {
            alignmentValid(3, lastCursorPosition, endOfString);
        }
        str.setSpan(FORMAT_NORMAL, lastCursorPosition, endOfString, Spannable.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    public void afterTextChanged(Editable text) {
        if (this.historyEnable && !this.historyWorking) {
            this.inputLast = new SpannableStringBuilder(text);
            if (text == null || !text.toString().equals(this.inputBefore.toString())) {
                if (this.historyList.size() >= this.historySize) {
                    this.historyList.remove(0);
                }
                this.historyList.add(this.inputBefore);
                this.historyCursor = this.historyList.size();
            }
        }
    }

}
