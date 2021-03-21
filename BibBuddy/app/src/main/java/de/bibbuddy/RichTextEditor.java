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

  private static final int FORMAT_BOLD = 1;
  private static final int FORMAT_ITALIC = 2;
  private static final int FORMAT_ALIGN_LEFT = 1;
  private static final int FORMAT_ALIGN_RIGHT = 2;
  private static final int FORMAT_ALIGN_CENTER = 3;

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

  @Override
  protected void onAttachedToWindow() {
    super.onAttachedToWindow();
    addTextChangedListener(this);
  }

  @Override
  protected void onDetachedFromWindow() {
    super.onDetachedFromWindow();
    removeTextChangedListener(this);
  }

  /**
   * This method sets or removes text format bold depending on the toolbar icon is selected.
   *
   * @param valid boolean if icon for format type bold is selected
   */
  public void bold(boolean valid) {
    bold = valid;
    if (valid) {
      styleValid(FORMAT_BOLD, getSelectionStart(), getSelectionEnd());
    } else {
      styleInvalid(FORMAT_BOLD, getSelectionStart(), getSelectionEnd());
    }
  }

  /**
   * This method sets or removes text format italic depending on the toolbar icon is selected.
   *
   * @param valid boolean if icon for format type italic is selected
   */
  public void italic(boolean valid) {
    italic = valid;
    if (valid) {
      styleValid(FORMAT_ITALIC, getSelectionStart(), getSelectionEnd());
    } else {
      styleInvalid(FORMAT_ITALIC, getSelectionStart(), getSelectionEnd());
    }
  }

  private void styleValid(int style, int start, int end) {
    getEditableText().setSpan(new StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
  }

  private void styleInvalid(int style, int start, int end) {
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
   * @param valid boolean if icon for format type underline is selected
   */
  public void underline(boolean valid) {
    underline = valid;
    if (valid) {
      underlineValid(getSelectionStart(), getSelectionEnd());
    } else {
      underlineInvalid(getSelectionStart(), getSelectionEnd());
    }
  }

  private void underlineValid(int start, int end) {
    if (start < end) {
      getEditableText().setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
  }

  private void underlineInvalid(int start, int end) {
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
   * @param valid boolean if icon for format type strikeThrough is selected
   */
  public void strikeThrough(boolean valid) {
    strikeThrough = valid;
    if (valid) {
      strikeThroughValid(getSelectionStart(), getSelectionEnd());
    } else {
      strikeThroughInvalid(getSelectionStart(), getSelectionEnd());
    }
  }

  private void strikeThroughValid(int start, int end) {
    if (start < end) {
      getEditableText()
          .setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
  }

  private void strikeThroughInvalid(int start, int end) {
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
   * @param valid boolean if icon for format type bullet is selected
   */
  public void bullet(boolean valid) {
    bullet = valid;
    if (valid) {
      valid(containFormat(RichTextEditorBulletSpan.class), new RichTextEditorBulletSpan());
    } else {
      invalid(containFormat(RichTextEditorBulletSpan.class), RichTextEditorBulletSpan.class);
    }
  }

  private void valid(Boolean contain, Object span) {
    String[] lines = TextUtils.split(getEditableText().toString(), "\n");
    int i = 0;
    while (i < lines.length) {
      if (contain) {
        return;
      }
      int lineStart = getLineBoundaries()[0];
      int lineEnd = getLineBoundaries()[1];
      applyLineSpan(lineStart, lineEnd, span);
      i++;
    }
  }

  private int[] getLineBoundaries() {
    String[] lines = TextUtils.split(getEditableText().toString(), "\n");
    int start = 0;
    int end = 1;
    int i = 0;
    while (i < lines.length) {
      int lineStart = 0;
      int lineEnd;
      for (lineEnd = 0; lineEnd < i; lineEnd++) {
        lineStart = lineStart + lines[lineEnd].length() + 1;
      }
      lineEnd = lineStart + lines[i].length();
      adjustCursor(lineStart, lineEnd);
      if (lineStart < lineEnd && lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd
          || getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
        start = lineStart;
        end = lineEnd;
      }
      i++;
    }
    int[] boundaries = new int[2];
    boundaries[0] = start;
    boundaries[1] = end;
    return boundaries;
  }

  private void adjustCursor(int lineStart, int lineEnd) {
    if (lineStart == lineEnd) {
      if (alignmentRight) {
        setGravity(Gravity.END);
      } else if (alignmentCenter) {
        setGravity(Gravity.CENTER);
      } else {
        setGravity(Gravity.START);
      }
    }
  }

  private void applyLineSpan(int lineStart, int lineEnd, Object span) {
    if (lineStart < lineEnd) {
      int start = 0;
      int end = 0;
      if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd
          || getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
        start = lineStart;
        end = lineEnd;
      }
      if (start < end) {
        getEditableText()
            .setSpan(span, start, end,
                Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        if (span.equals(RichTextEditorQuoteSpan.class)) {
          styleValid(FORMAT_ITALIC, start, end);
          getEditableText().setSpan(
              new BackgroundColorSpan(ContextCompat.getColor(getContext(), R.color.gray_medium)),
              start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
      }
    }
  }

  private void invalid(Boolean contain, Object span) {
    String[] lines = TextUtils.split(getEditableText().toString(), "\n");
    for (int i = 0; i < lines.length; i++) {
      if (contain) {
        int lineStart = 0;
        int lineEnd;
        for (lineEnd = 0; lineEnd < i; lineEnd++) {
          lineStart = lineStart + lines[lineEnd].length() + 1;
        }
        lineEnd = lineStart + lines[i].length();
        removeLineSpan(lineStart, lineEnd, span);
      }
    }
  }

  private void removeLineSpan(int lineStart, int stringLength, Object sp) {
    int lineEnd = lineStart + stringLength;
    if (lineStart < lineEnd) {
      int start = 0;
      int end = 0;
      if (lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd
          || getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
        start = lineStart;
        end = lineEnd;
      }
      if (start < end) {
        if (sp.equals(RichTextEditorBulletSpan.class)) {
          RichTextEditorBulletSpan[] spans =
              getEditableText()
                  .getSpans(start, end, RichTextEditorBulletSpan.class);
          for (RichTextEditorBulletSpan span : spans) {
            getEditableText().removeSpan(span);
          }
        } else {
          RichTextEditorQuoteSpan[] spans =
              getEditableText().getSpans(start, end, RichTextEditorQuoteSpan.class);
          for (RichTextEditorQuoteSpan span : spans) {
            getEditableText().removeSpan(span);
          }
          BackgroundColorSpan[] backgroundColorSpans =
              getEditableText().getSpans(start, end, BackgroundColorSpan.class);
          for (BackgroundColorSpan backgroundColorSpan : backgroundColorSpans) {
            getEditableText().removeSpan(backgroundColorSpan);
          }
          styleInvalid(FORMAT_ITALIC, start, end);
        }
      }
    }
  }

  private boolean containFormat(Object span) {
    String[] lines = TextUtils.split(getEditableText().toString(), "\n");
    List<Integer> list = new ArrayList<>();
    for (int i = 0; i < lines.length; i++) {
      int lineStart = 0;
      int lineEnd;
      for (lineEnd = 0; lineEnd < i; lineEnd++) {
        lineStart = lineStart + lines[lineEnd].length() + 1;
      }
      lineEnd = lineStart + lines[i].length();
      if (lineStart < lineEnd && lineStart <= getSelectionStart() && getSelectionEnd() <= lineEnd
          || getSelectionStart() <= lineStart && lineEnd <= getSelectionEnd()) {
        list.add(i);
      }
    }
    Iterator<Integer> iterator = list.iterator();
    Integer i;
    do {
      if (!iterator.hasNext()) {
        return true;
      }
      i = iterator.next();
    } while (containFormat(i, span));
    return false;
  }

  private boolean containFormat(int index, Object span) {
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
      if (span.equals(RichTextEditorBulletSpan.class)) {
        RichTextEditorBulletSpan[] spans =
            getEditableText().getSpans(start, end, RichTextEditorBulletSpan.class);
        return spans.length > 0;
      } else {
        RichTextEditorQuoteSpan[] spans =
            getEditableText().getSpans(start, end, RichTextEditorQuoteSpan.class);
        return spans.length > 0;
      }
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
      valid(containFormat(RichTextEditorQuoteSpan.class), new RichTextEditorQuoteSpan());
    } else {
      invalid(containFormat(RichTextEditorQuoteSpan.class), RichTextEditorQuoteSpan.class);
    }
  }

  /**
   * This method aligns the text left.
   */
  public void alignLeft() {
    alignmentValid(FORMAT_ALIGN_LEFT);
    alignmentLeft = !alignmentLeft;
  }

  /**
   * This method aligns the text right.
   */
  public void alignRight() {
    if (!alignmentRight) {
      alignmentRight = true;
      alignmentValid(FORMAT_ALIGN_RIGHT);
    } else {
      alignmentRight = false;
      alignmentValid(FORMAT_ALIGN_LEFT);
    }
  }

  /**
   * This method aligns the text central.
   */
  public void alignCenter() {
    alignmentValid(FORMAT_ALIGN_CENTER);
    if (!alignmentCenter) {
      alignmentCenter = true;
    } else {
      alignmentCenter = false;
      alignmentValid(FORMAT_ALIGN_LEFT);
    }
  }

  private void alignmentValid(int style) {
    int start = getSelectionStart();
    int end = getSelectionEnd();
    if (!hasSelection()) {
      start = getLineBoundaries()[0];
      end = getLineBoundaries()[1];
    }
    adjustAlignment(style, start, end);
  }

  private void adjustAlignment(int style, int start, int end) {
    if (start >= end) {
      return;
    }
    getEditableText().setSpan((AlignmentSpan) () -> {
      if (style == FORMAT_ALIGN_RIGHT) {
        return Alignment.ALIGN_OPPOSITE;
      } else if (style == FORMAT_ALIGN_CENTER) {
        return Alignment.ALIGN_CENTER;
      }
      return Alignment.ALIGN_NORMAL;
    }, start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
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
   * This method checks if there is a redo-able user input action.
   *
   * @return boolean if there is a valid user input action to redo.
   */
  public boolean redoValid() {
    if (historyEnable && historySize > 0 && !historyList.isEmpty()
        && !historyWorking) {
      return historyCursor < historyList.size() - 1
          || historyCursor >= historyList.size() - 1 && inputLast != null;
    }
    return false;
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
   * This method checks if there is a undo-able user input action.
   *
   * @return boolean if there is a valid user input action to undo.
   */
  public boolean undoValid() {
    if (historyEnable && historySize > 0 && !historyWorking) {
      return !historyList.isEmpty() && historyCursor > 0;
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
    int endOfString = getSelectionStart();
    int lastCursorPosition = endOfString;
    if (endOfString != 0) {
      lastCursorPosition = endOfString - 1;
    }
    if (inputBefore != null && inputBefore.toString().length() > str.length()) {
      return;
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
      valid(containFormat(RichTextEditorBulletSpan.class), new RichTextEditorBulletSpan());
    }
    if (quote) {
      valid(containFormat(RichTextEditorQuoteSpan.class), new RichTextEditorQuoteSpan());
      styleValid(FORMAT_ITALIC, lastCursorPosition, endOfString);
      str.setSpan(new BackgroundColorSpan(ContextCompat.getColor(getContext(), R.color.gray_medium)),
          lastCursorPosition, endOfString, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
    if (alignmentLeft) {
      alignmentValid(FORMAT_ALIGN_LEFT);
    }
    if (alignmentRight) {
      alignmentValid(FORMAT_ALIGN_RIGHT);
    }
    if (alignmentCenter) {
      alignmentValid(FORMAT_ALIGN_CENTER);
    }
  }

  /**
   * This method saves current text after user input to provide a history for input actions.
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
