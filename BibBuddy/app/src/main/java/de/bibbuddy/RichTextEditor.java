package de.bibbuddy;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.BulletSpan;
import android.text.style.CharacterStyle;
import android.text.style.QuoteSpan;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

public class RichTextEditor extends AppCompatEditText implements TextWatcher {
  public static final int FORMAT_BOLD = 1;
  public static final int FORMAT_ITALIC = 2;

  private final List<Editable> historyList = new LinkedList<>();
  private boolean historyEnable = true;
  private int historySize = 100;
  private boolean historyWorking = false;
  private int historyCursor = 0;
  private SpannableStringBuilder inputBefore;
  private Editable inputLast;

  private boolean bold = false;
  private boolean italic = false;
  private boolean underline = false;
  private boolean strikeThrough = false;
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
      throw new IllegalArgumentException("historySize must be > 0");
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

  /**
   * This method sets or removes text format bold depending on the toolbar icon is selected.
   *
   * @param valid boolean if the tool icon for format type bold is selected and format
   *              needs to be applied
   */
  public void bold(boolean valid) {
    bold = valid;
    if (valid) {
      this.styleValid(1, this.getSelectionStart(), this.getSelectionEnd());
    } else {
      this.styleInvalid(1, this.getSelectionStart(), this.getSelectionEnd());
    }
  }

  /**
   * This method sets or removes text format italic depending on the toolbar icon is selected.
   *
   * @param valid boolean if the tool icon for format type italic is selected and format
   *              needs to be applied
   */
  public void italic(boolean valid) {
    italic = valid;
    if (valid) {
      this.styleValid(2, this.getSelectionStart(), this.getSelectionEnd());
    } else {
      this.styleInvalid(2, this.getSelectionStart(), this.getSelectionEnd());
    }
  }

  protected void styleValid(int style, int start, int end) {
    if (style == 1 || style == 2) {
      this.getEditableText().setSpan(new StyleSpan(style), start, end, 33);
    }
  }

  protected void styleInvalid(int style, int start, int end) {
    StyleSpan[] spans = this.getEditableText().getSpans(start, end, StyleSpan.class);
    ArrayList<RichTextEditorPart> list = new ArrayList<>();
    for (StyleSpan span : spans) {
      if (span.getStyle() == style) {
        list.add(new RichTextEditorPart(this.getEditableText().getSpanStart(span),
            this.getEditableText().getSpanEnd(span)));
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

  }

  /**
   * This method sets or removes text format underline depending on the toolbar icon is selected.
   *
   * @param valid boolean if the tool icon for format type underline is selected and format
   *              needs to be applied
   */
  public void underline(boolean valid) {
    underline = valid;
    if (valid) {
      this.underlineValid(this.getSelectionStart(), this.getSelectionEnd());
    } else {
      this.underlineInvalid(this.getSelectionStart(), this.getSelectionEnd());
    }
  }

  protected void underlineValid(int start, int end) {
    if (start < end) {
      this.getEditableText().setSpan(new UnderlineSpan(), start, end, 33);
    }
  }

  protected void underlineInvalid(int start, int end) {
    if (start == end) {
      return;
    }
    UnderlineSpan[] spans = this.getEditableText().getSpans(start, end, UnderlineSpan.class);
    ArrayList<RichTextEditorPart> list = new ArrayList<>();
    for (UnderlineSpan span : spans) {
      list.add(new RichTextEditorPart(this.getEditableText().getSpanStart(span),
          this.getEditableText().getSpanEnd(span)));
      this.getEditableText().removeSpan(span);
    }
    for (RichTextEditorPart part : list) {
      if (!part.isValid()) {
        continue;
      }
      if (part.getStart() < start) {
        this.underlineValid(part.getStart(), start);
      }

      if (part.getEnd() > end) {
        this.underlineValid(end, part.getEnd());
      }
    }
  }

  /**
   * This method sets or removes text format strikeThrough
   * depending on the toolbar icon is selected.
   *
   * @param valid boolean if the tool icon for format type strikeThrough is selected and format
   *              needs to be applied
   */
  public void strikeThrough(boolean valid) {
    strikeThrough = valid;
    if (valid) {
      this.strikeThroughValid(this.getSelectionStart(), this.getSelectionEnd());
    } else {
      this.strikeThroughInvalid(this.getSelectionStart(), this.getSelectionEnd());
    }
  }

  protected void strikeThroughValid(int start, int end) {
    if (start < end) {
      this.getEditableText().setSpan(new StrikethroughSpan(), start, end, 33);
    }
  }

  protected void strikeThroughInvalid(int start, int end) {
    if (start >= end) {
      return;
    }
    StrikethroughSpan[] spans =
        this.getEditableText().getSpans(start, end, StrikethroughSpan.class);
    List<RichTextEditorPart> list = new ArrayList<>();
    for (StrikethroughSpan span : spans) {
      list.add(new RichTextEditorPart(this.getEditableText().getSpanStart(span),
          this.getEditableText().getSpanEnd(span)));
      this.getEditableText().removeSpan(span);
    }

    for (RichTextEditorPart part : list) {
      if (part.isValid()) {
        if (part.getStart() < start) {
          this.strikeThroughValid(part.getStart(), start);
        }

        if (part.getEnd() > end) {
          this.strikeThroughValid(end, part.getEnd());
        }
      }
    }
  }

  /**
   * This method sets or removes text format bullet depending on the toolbar icon is selected.
   *
   * @param valid boolean if the tool icon for format type bullet is selected and format
   *              needs to be applied
   */
  public void bullet(boolean valid) {
    bullet = valid;
    if (valid) {
      this.bulletValid();
    } else {
      this.bulletInvalid();
    }
  }

  protected void bulletValid() {
    String[] lines = TextUtils.split(this.getEditableText().toString(), "\n");
    for (int i = 0; i < lines.length; i++) {
      if (this.containBullet()) {
        return;
      }
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
          this.getEditableText()
              .setSpan(new RichTextEditorBulletSpan(), bulletStart, bulletEnd, 33);
        }
      }
    }
  }

  protected void bulletInvalid() {
    String[] lines = TextUtils.split(this.getEditableText().toString(), "\n");
    for (int i = 0; i < lines.length; i++) {
      if (this.containBullet()) {
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
            BulletSpan[] spans =
                this.getEditableText().getSpans(bulletStart, bulletEnd, BulletSpan.class);
            int spanLength = spans.length;
            for (int j = 0; j < spanLength; j++) {
              BulletSpan span = spans[i];
              this.getEditableText().removeSpan(span);
            }
          }
        }
      }
    }
  }

  protected boolean containBullet() {
    String[] lines = TextUtils.split(this.getEditableText().toString(), "\n");
    List<Integer> list = new ArrayList<>();

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

    Iterator<Integer> iterator = list.iterator();
    Integer i;
    do {
      if (!iterator.hasNext()) {
        return true;
      }
      i = iterator.next();
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
      }
      BulletSpan[] spans = this.getEditableText().getSpans(start, end, BulletSpan.class);
      return spans.length > 0;
    }
    return false;

  }

  /**
   * This method sets or removes text format quote depending on the toolbar icon is selected.
   *
   * @param valid boolean if the tool icon for format type quote is selected and format
   *              needs to be applied
   */
  public void quote(boolean valid) {
    quote = valid;
    if (valid) {
      this.quoteValid();
    } else {
      this.quoteInvalid();
    }
  }

  protected void quoteValid() {
    String[] lines = TextUtils.split(this.getEditableText().toString(), "\n");
    for (int i = 0; i < lines.length; i++) {
      if (this.containQuote()) {
        return;
      }
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
          int quoteColor = R.color.gray_background;
          int quoteStripeWidth = 10;
          int quoteGapWidth = 10;
          this.getEditableText()
              .setSpan(new RichTextEditorQuoteSpan(quoteColor, quoteStripeWidth, quoteGapWidth),
                  quoteStart, quoteEnd, 33);
          styleValid(FORMAT_ITALIC, quoteStart, quoteEnd);
          this.getEditableText().setSpan(
              new BackgroundColorSpan(ContextCompat.getColor(getContext(), R.color.gray_quote)),
              quoteStart, quoteEnd, 33);
        }
      }
    }
  }

  protected void quoteInvalid() {
    String[] lines = TextUtils.split(this.getEditableText().toString(), "\n");
    for (int i = 0; i < lines.length; i++) {
      if (!this.containQuote()) {
        return;
      }
      int lineStart = 0;
      int lineEnd;

      for (lineEnd = 0; lineEnd < i; lineEnd++) {
        lineStart = lineStart + lines[lineEnd].length() + 1;
      }

      lineEnd = lineStart + lines[i].length();

      removeQuote(lineStart, lineEnd);
    }
  }

  private void removeQuote(int start, int end) {
    if (start < end) {
      int quoteStart = 0;
      int quoteEnd = 0;
      if (start <= this.getSelectionStart() && this.getSelectionEnd() <= end) {
        quoteStart = start;
        quoteEnd = end;
      } else if (this.getSelectionStart() <= start && end <= this.getSelectionEnd()) {
        quoteStart = start;
        quoteEnd = end;
      }
      if (quoteStart < quoteEnd) {
        QuoteSpan[] spans = this.getEditableText().getSpans(quoteStart, quoteEnd, QuoteSpan.class);
        for (QuoteSpan span : spans) {
          this.getEditableText().removeSpan(span);
        }
        BackgroundColorSpan[] spans1 =
            this.getEditableText().getSpans(quoteStart, quoteEnd, BackgroundColorSpan.class);
        for (BackgroundColorSpan span1 : spans1) {
          this.getEditableText().removeSpan(span1);
        }
        styleInvalid(FORMAT_ITALIC, quoteStart, quoteEnd);
      }
    }
  }

  protected boolean containQuote() {
    String[] lines = TextUtils.split(this.getEditableText().toString(), "\n");
    List<Integer> list = new ArrayList<>();
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

    Iterator<Integer> iterator = list.iterator();
    Integer i;
    do {
      if (!iterator.hasNext()) {
        return true;
      }
      i = iterator.next();
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
      }
      QuoteSpan[] spans = this.getEditableText().getSpans(start, end, QuoteSpan.class);
      return spans.length > 0;
    }
    return false;
  }

  protected void alignLeft() {
    this.alignmentValid(1, this.getSelectionStart(), this.getSelectionEnd());
    alignmentLeft = !alignmentLeft;
  }

  protected void alignRight() {
    if (!alignmentRight) {
      alignmentRight = true;
      this.alignmentValid(2, this.getSelectionStart(), this.getSelectionEnd());
    } else {
      alignmentRight = false;
      this.alignmentValid(1, this.getSelectionStart(), this.getSelectionEnd());
    }
  }

  protected void alignCenter() {
    this.alignmentValid(3, this.getSelectionStart(), this.getSelectionEnd());
    if (!alignmentCenter) {
      alignmentCenter = true;
    } else {
      alignmentCenter = false;
      this.alignmentValid(1, this.getSelectionStart(), this.getSelectionEnd());
    }
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

  private void adjustAlignment(int style, int start, int end) {
    if (style == 0 || style == 1) {
      if (start >= end) {
        this.getEditableText().setSpan(new AlignmentSpan() {

          @Override
          public Alignment getAlignment() {
            return Alignment.ALIGN_NORMAL;
          }
        }, start, end, 33);
      }
      this.getEditableText().setSpan(new AlignmentSpan() {

        @Override
        public Alignment getAlignment() {
          return Alignment.ALIGN_NORMAL;
        }
      }, start, end, 33);
    } else if (style == 2) {
      if (start >= end) {
        return;
      }
      this.getEditableText().setSpan(new AlignmentSpan() {

        @Override
        public Alignment getAlignment() {
          return Alignment.ALIGN_OPPOSITE;
        }
      }, start, end, 33);
    } else if (style == 3) {
      if (start >= end) {
        return;
      }
      this.getEditableText().setSpan(new AlignmentSpan() {

        @Override
        public Alignment getAlignment() {
          return Alignment.ALIGN_CENTER;
        }
      }, start, end, 33);
    }
  }

  /**
   * This method redoes the last user input action.
   */
  public void redo() {
    if (!this.redoValid()) {
      return;
    }
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
  }

  /**
   * This method undoes the last user input action.
   */
  public void undo() {
    if (!this.undoValid()) {
      return;
    }
    this.historyWorking = true;
    this.historyCursor--;
    this.setText(this.historyList.get(this.historyCursor));
    this.setSelection(this.getEditableText().length());
    this.historyWorking = false;
  }

  /**
   * This method checks if there is a redoable user input action.
   *
   * @return boolean if there is a valid user input action to redo.
   */
  public boolean redoValid() {
    if (this.historyEnable && this.historySize > 0 && this.historyList.size() > 0
        && !this.historyWorking) {
      return this.historyCursor < this.historyList.size() - 1
          || this.historyCursor >= this.historyList.size() - 1 && this.inputLast != null;
    }
    return false;
  }

  /**
   * This method checks if there is a undoable user input action.
   *
   * @return boolean if there is a valid user input action to unddo.
   */
  public boolean undoValid() {
    if (this.historyEnable && this.historySize > 0 && !this.historyWorking) {
      return this.historyList.size() > 0 && this.historyCursor > 0;
    }
    return false;
  }

  /**
   * This method saves the current text stat as a span before any user input to provide a history
   * for redoable and undoable actions.
   *
   * @param text text content charSequence
   */
  public void beforeTextChanged(CharSequence text, int start, int count, int after) {
    if (this.historyEnable && !this.historyWorking) {
      this.inputBefore = new SpannableStringBuilder(text);
    }
  }

  /**
   * This method applies the chosen text format options as spans to the text
   * - if there is none chosen, it applies the default span.
   *
   * @param text current text content charSequence
   */
  @Override
  public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
    Spannable str = this.getEditableText();
    if (this.inputBefore != null && this.inputBefore.toString().length() > str.length()) {
      return;
    }
    int endOfString = this.getSelectionStart();
    int lastCursorPosition = endOfString;
    if (endOfString != 0) {
      lastCursorPosition = endOfString - 1;
    }
    Object[] spansToRemove = str.getSpans(endOfString - 1, endOfString, Object.class);
    for (Object span : spansToRemove) {
      if (span instanceof CharacterStyle) {
        str.removeSpan(span);
      }
    }
    applyChosenTextFormats(str, lastCursorPosition, endOfString);
  }

  private void applyChosenTextFormats(Spannable str, int lastCursorPosition, int endOfString) {
    if (bold) {
      styleValid(FORMAT_BOLD, lastCursorPosition, endOfString);
    }
    if (italic) {
      styleValid(FORMAT_ITALIC, lastCursorPosition, endOfString);
    }
    if (underline) {
      underlineValid(lastCursorPosition, endOfString);
    }
    if (strikeThrough) {
      strikeThroughValid(lastCursorPosition, endOfString);
    }
    if (bullet) {
      bulletValid();
    }
    if (quote) {
      quoteValid();
      styleValid(FORMAT_ITALIC, lastCursorPosition, endOfString);
      str.setSpan(new BackgroundColorSpan(ContextCompat.getColor(getContext(), R.color.gray_quote)),
          lastCursorPosition, endOfString, 33);
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
  }

  /**
   * This method saves the current text state after any user input to provide a history
   * for redoable and undoable actions.
   *
   * @param text text content charSequence
   */
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
