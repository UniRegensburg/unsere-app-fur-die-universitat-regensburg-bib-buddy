package de.bibbuddy;

import android.content.Context;
import android.content.res.TypedArray;
import android.text.Editable;
import android.text.Layout.Alignment;
import android.text.Spannable;
import android.text.SpannableStringBuilder;
import android.text.Spanned;
import android.text.TextUtils;
import android.text.TextWatcher;
import android.text.style.AlignmentSpan;
import android.text.style.BackgroundColorSpan;
import android.text.style.CharacterStyle;
import android.text.style.StrikethroughSpan;
import android.text.style.StyleSpan;
import android.text.style.UnderlineSpan;
import android.util.AttributeSet;
import android.view.Gravity;
import androidx.appcompat.widget.AppCompatEditText;
import androidx.core.content.ContextCompat;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedList;
import java.util.List;

/**
 * The RichTextEditor is responsible for the styling of the note text.
 *
 * @author Sabrina Freisleben
 */
public class RichTextEditor extends AppCompatEditText implements TextWatcher {

  private final int formatBold = 1;
  private final int formatItalic = 2;
  private final int formatAlignLeft = 1;
  private final int formatAlignRight = 2;
  private final int formatAlignCenter = 3;

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
    init(null);
  }

  public RichTextEditor(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
  }

  private void init(AttributeSet attrs) {
    TypedArray array = getContext().obtainStyledAttributes(attrs, R.styleable.RichTextEditor);
    historyEnable = array.getBoolean(R.styleable.RichTextEditor_historyEnable, true);
    historySize = array.getInt(R.styleable.RichTextEditor_historySize, historySize);
    array.recycle();
  }

  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    addTextChangedListener(this);
  }

  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    removeTextChangedListener(this);
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
      styleValid(formatBold, getSelectionStart(), getSelectionEnd());
    } else {
      styleInvalid(formatBold, getSelectionStart(), getSelectionEnd());
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
      styleValid(formatItalic, getSelectionStart(), getSelectionEnd());
    } else {
      styleInvalid(formatItalic, getSelectionStart(), getSelectionEnd());
    }
  }

  protected void styleValid(int style, int start, int end) {
    getEditableText().setSpan(new StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
  }

  protected void styleInvalid(int style, int start, int end) {
    StyleSpan[] spans = getEditableText().getSpans(start, end, StyleSpan.class);
    ArrayList<RichTextEditorPart> list = new ArrayList<>();
    for (StyleSpan span : spans) {
      if (span.getStyle() == style) {
        list.add(new RichTextEditorPart(getEditableText().getSpanStart(span),
            getEditableText().getSpanEnd(span)));
        getEditableText().removeSpan(span);
      }
    }
    for (RichTextEditorPart part : list) {
      if (part.isValid()) {
        if (part.getStart() < start) {
          styleValid(style, part.getStart(), start);
        }
        if (part.getEnd() > end) {
          styleValid(style, end, part.getEnd());
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
      underlineValid(getSelectionStart(), getSelectionEnd());
    } else {
      underlineInvalid(getSelectionStart(), getSelectionEnd());
    }
  }

  protected void underlineValid(int start, int end) {
    if (start < end) {
      getEditableText().setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
  }

  protected void underlineInvalid(int start, int end) {
    if (start == end) {
      return;
    }
    UnderlineSpan[] spans = getEditableText().getSpans(start, end, UnderlineSpan.class);
    ArrayList<RichTextEditorPart> list = new ArrayList<>();
    for (UnderlineSpan span : spans) {
      list.add(new RichTextEditorPart(getEditableText().getSpanStart(span),
          getEditableText().getSpanEnd(span)));
      getEditableText().removeSpan(span);
    }
    for (RichTextEditorPart part : list) {
      if (!part.isValid()) {
        continue;
      }
      if (part.getStart() < start) {
        underlineValid(part.getStart(), start);
      }

      if (part.getEnd() > end) {
        underlineValid(end, part.getEnd());
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
      strikeThroughValid(getSelectionStart(), getSelectionEnd());
    } else {
      strikeThroughInvalid(getSelectionStart(), getSelectionEnd());
    }
  }

  protected void strikeThroughValid(int start, int end) {
    if (start < end) {
      getEditableText()
          .setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
  }

  protected void strikeThroughInvalid(int start, int end) {
    if (start >= end) {
      return;
    }
    StrikethroughSpan[] spans =
        getEditableText().getSpans(start, end, StrikethroughSpan.class);
    List<RichTextEditorPart> list = new ArrayList<>();
    for (StrikethroughSpan span : spans) {
      list.add(new RichTextEditorPart(getEditableText().getSpanStart(span),
          getEditableText().getSpanEnd(span)));
      getEditableText().removeSpan(span);
    }

    for (RichTextEditorPart part : list) {
      if (part.isValid()) {
        if (part.getStart() < start) {
          strikeThroughValid(part.getStart(), start);
        }

        if (part.getEnd() > end) {
          strikeThroughValid(end, part.getEnd());
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
      bulletValid();
    } else {
      bulletInvalid();
    }
  }

  protected void bulletValid() {
    String[] lines = TextUtils.split(getEditableText().toString(), "\n");
    for (int i = 0; i < lines.length; i++) {
      if (containBullet()) {
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
        if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
          bulletStart = lineStart;
          bulletEnd = lineEnd;
        } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
          bulletStart = lineStart;
          bulletEnd = lineEnd;
        }
        if (bulletStart < bulletEnd) {
          getEditableText()
              .setSpan(new RichTextEditorBulletSpan(), bulletStart, bulletEnd,
                  Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
      }
    }
  }

  protected void bulletInvalid() {
    String[] lines = TextUtils.split(getEditableText().toString(), "\n");
    for (int i = 0; i < lines.length; i++) {
      if (containBullet()) {
        int lineStart = 0;
        int lineEnd;
        for (lineEnd = 0; lineEnd < i; lineEnd++) {
          lineStart = lineStart + lines[lineEnd].length() + 1;
        }
        removeBulletSpan(lineStart, lines[i].length());
      }
    }
  }

  private void removeBulletSpan(int lineStart, int stringLength) {
    int lineEnd = lineStart + stringLength;
    if (lineStart < lineEnd) {
      int bulletStart = 0;
      int bulletEnd = 0;
      if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
        bulletStart = lineStart;
        bulletEnd = lineEnd;
      } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
        bulletStart = lineStart;
        bulletEnd = lineEnd;
      }
      if (bulletStart < bulletEnd) {
        RichTextEditorBulletSpan[] spans =
            getEditableText()
                .getSpans(bulletStart, bulletEnd, RichTextEditorBulletSpan.class);
        for (RichTextEditorBulletSpan span : spans) {
          getEditableText().removeSpan(span);
        }
      }
    }
  }

  protected boolean containBullet() {
    String[] lines = TextUtils.split(getEditableText().toString(), "\n");
    List<Integer> list = new ArrayList<>();

    for (int i = 0; i < lines.length; i++) {
      int lineStart = 0;

      int lineEnd;
      for (lineEnd = 0; lineEnd < i; lineEnd++) {
        lineStart = lineStart + lines[lineEnd].length() + 1;
      }

      lineEnd = lineStart + lines[i].length();
      if (lineStart < lineEnd) {
        if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
          list.add(i);
        } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
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
    } while (containBullet(i));
    return false;
  }

  protected boolean containBullet(int index) {
    String[] lines = TextUtils.split(getEditableText().toString(), "\n");
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
      RichTextEditorBulletSpan[] spans =
          getEditableText().getSpans(start, end, RichTextEditorBulletSpan.class);
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
      quoteValid();
    } else {
      quoteInvalid();
    }
  }

  protected void quoteValid() {
    String[] lines = TextUtils.split(getEditableText().toString(), "\n");
    for (int i = 0; i < lines.length; i++) {
      if (containQuote()) {
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
        if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
          quoteStart = lineStart;
          quoteEnd = lineEnd;
        } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
          quoteStart = lineStart;
          quoteEnd = lineEnd;
        }
        if (quoteStart < quoteEnd) {
          getEditableText()
              .setSpan(new RichTextEditorQuoteSpan(),
                  quoteStart, quoteEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
          styleValid(formatItalic, quoteStart, quoteEnd);
          getEditableText().setSpan(
              new BackgroundColorSpan(ContextCompat.getColor(getContext(), R.color.gray_quote)),
              quoteStart, quoteEnd, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
      }
    }
  }

  protected void quoteInvalid() {
    String[] lines = TextUtils.split(getEditableText().toString(), "\n");
    for (int i = 0; i < lines.length; i++) {
      if (!containQuote()) {
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
      if (start <= getSelectionStart() && getSelectionEnd() <= end) {
        quoteStart = start;
        quoteEnd = end;
      } else if (getSelectionStart() <= start && end <= getSelectionEnd()) {
        quoteStart = start;
        quoteEnd = end;
      }
      if (quoteStart < quoteEnd) {
        RichTextEditorQuoteSpan[] spans =
            getEditableText().getSpans(quoteStart, quoteEnd, RichTextEditorQuoteSpan.class);
        for (RichTextEditorQuoteSpan span : spans) {
          getEditableText().removeSpan(span);
        }
        BackgroundColorSpan[] backgroundColorSpans =
            getEditableText().getSpans(quoteStart, quoteEnd, BackgroundColorSpan.class);
        for (BackgroundColorSpan backgroundColorSpan : backgroundColorSpans) {
          getEditableText().removeSpan(backgroundColorSpan);
        }
        styleInvalid(formatItalic, quoteStart, quoteEnd);
      }
    }
  }

  protected boolean containQuote() {
    String[] lines = TextUtils.split(getEditableText().toString(), "\n");
    List<Integer> list = new ArrayList<>();
    for (int i = 0; i < lines.length; i++) {
      int lineStart = 0;
      int lineEnd;
      for (lineEnd = 0; lineEnd < i; lineEnd++) {
        lineStart = lineStart + lines[lineEnd].length() + 1;
      }
      lineEnd = lineStart + lines[i].length();
      if (lineStart < lineEnd) {
        if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
          list.add(i);
        } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
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
    } while (containQuote(i));
    return false;
  }

  protected boolean containQuote(int index) {
    String[] lines = TextUtils.split(getEditableText().toString(), "\n");
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
      RichTextEditorQuoteSpan[] spans =
          getEditableText().getSpans(start, end, RichTextEditorQuoteSpan.class);
      return spans.length > 0;
    }
    return false;
  }

  protected void alignLeft() {
    alignmentValid(formatAlignLeft);
    alignmentLeft = !alignmentLeft;
  }

  protected void alignRight() {
    if (!alignmentRight) {
      alignmentRight = true;
      alignmentValid(formatAlignRight);
    } else {
      alignmentRight = false;
      alignmentValid(formatAlignLeft);
    }
  }

  protected void alignCenter() {
    alignmentValid(formatAlignCenter);
    if (!alignmentCenter) {
      alignmentCenter = true;
    } else {
      alignmentCenter = false;
      alignmentValid(formatAlignLeft);
    }
  }

  protected void alignmentValid(int style) {
    int start = getSelectionStart();
    int end = getSelectionEnd();
    if (!hasSelection()) {
      start = getLineBoundaries()[0];
      end = getLineBoundaries()[1];
    }
    adjustAlignment(style, start, end);
  }

  private int[] getLineBoundaries() {
    int start = 0;
    int end = 1;
    String[] lines = TextUtils.split(getEditableText().toString(), "\n");
    for (int i = 0; i < lines.length; i++) {
      int lineStart = 0;
      int lineEnd;
      for (lineEnd = 0; lineEnd < i; lineEnd++) {
        lineStart = lineStart + lines[lineEnd].length() + 1;
      }
      lineEnd = lineStart + lines[i].length();
      if(lineEnd == lineStart){
        if(alignmentRight){
          setGravity(Gravity.RIGHT);
        } else if (alignmentCenter){
          setGravity(Gravity.CENTER);
        } else {
          setGravity(Gravity.LEFT);
        }
      }
      if (lineStart < lineEnd) {
        if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd) {
          start = lineStart;
          end = lineEnd;
        } else if (getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
          start = lineStart;
          end = lineEnd;
        }
      }
    }
    int[] boundaries = new int[2];
    boundaries[0] = start;
    boundaries[1] = end;
    return boundaries;
  }

  private void adjustAlignment(int style, int start, int end) {
    if (start >= end) {
      return;
    }
    if (style == formatAlignLeft) {
      getEditableText().setSpan(new AlignmentSpan() {

        @Override
        public Alignment getAlignment() {
          return Alignment.ALIGN_NORMAL;
        }

      }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

    } else if (style == formatAlignRight) {
      getEditableText().setSpan(new AlignmentSpan() {

        @Override
        public Alignment getAlignment() {
          return Alignment.ALIGN_OPPOSITE;
        }

      }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

    } else if (style == formatAlignCenter) {
      getEditableText().setSpan(new AlignmentSpan() {

        @Override
        public Alignment getAlignment() {
          return Alignment.ALIGN_CENTER;
        }

      }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);

    }
  }

  /**
   * This method redoes the last user input action.
   */
  public void redo() {
    if (!redoValid()) {
      return;
    }
    historyWorking = true;
    if (historyCursor >= historyList.size() - 1) {
      historyCursor = historyList.size();
      setText(inputLast);
    } else {
      historyCursor++;
      setText(historyList.get(historyCursor));
    }
    setSelection(getEditableText().length());
    historyWorking = false;
  }

  /**
   * This method undoes the last user input action.
   */
  public void undo() {
    if (!undoValid()) {
      return;
    }
    historyWorking = true;
    historyCursor--;
    setText(historyList.get(historyCursor));
    setSelection(getEditableText().length());
    historyWorking = false;
  }

  /**
   * This method checks if there is a redo-able user input action.
   *
   * @return boolean if there is a valid user input action to redo.
   */
  public boolean redoValid() {
    if (historyEnable && historySize > 0 && historyList.size() > 0
        && !historyWorking) {
      return historyCursor < historyList.size() - 1
          || historyCursor >= historyList.size() - 1 && inputLast != null;
    }
    return false;
  }

  /**
   * This method checks if there is a undo-able user input action.
   *
   * @return boolean if there is a valid user input action to undo.
   */
  public boolean undoValid() {
    if (historyEnable && historySize > 0 && !historyWorking) {
      return historyList.size() > 0 && historyCursor > 0;
    }
    return false;
  }

  /**
   * This method saves the current text stat as a span before any user input to provide a history
   * for redo-able and undoable actions.
   *
   * @param text text content charSequence
   */
  public void beforeTextChanged(CharSequence text, int start, int count, int after) {
    if (historyEnable && !historyWorking) {
      inputBefore = new SpannableStringBuilder(text);
    }
  }

  /**
   * This method applies the chosen text format options as spans to new inserted text.
   *
   * @param text current text content charSequence
   */
  @Override
  public void onTextChanged(CharSequence text, int start, int lengthBefore, int lengthAfter) {
    Spannable str = getEditableText();
    if (inputBefore != null && inputBefore.toString().length() > str.length()) {
      return;
    }
    int endOfString = getSelectionStart();
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
      styleValid(formatBold, lastCursorPosition, endOfString);
    }
    if (italic) {
      styleValid(formatItalic, lastCursorPosition, endOfString);
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
      styleValid(formatItalic, lastCursorPosition, endOfString);
      str.setSpan(new BackgroundColorSpan(ContextCompat.getColor(getContext(), R.color.gray_quote)),
          lastCursorPosition, endOfString, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    if (alignmentLeft) {
      alignmentValid(formatAlignLeft);
    }
    if (alignmentRight) {
      alignmentValid(formatAlignRight);
    }
    if (alignmentCenter) {
      alignmentValid(formatAlignCenter);
    }
  }

  /**
   * This method saves the current text state after any user input to provide a history
   * for redo-able and undoable actions.
   *
   * @param text text content charSequence
   */
  public void afterTextChanged(Editable text) {
    if (historyEnable && !historyWorking) {
      inputLast = new SpannableStringBuilder(text);
      if (text == null || !text.toString().equals(inputBefore.toString())) {
        if (historyList.size() >= historySize) {
          historyList.remove(0);
        }
        historyList.add(inputBefore);
        historyCursor = historyList.size();
      }
    }
  }

}
