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
 * RichTextEditor is responsible for text note formatting.
 *
 * @author Sabrina Freisleben.
 */
public class RichTextEditor extends AppCompatEditText implements TextWatcher {

  private static final int FORMAT_BOLD = 1;
  private static final int FORMAT_ITALIC = 2;
  private static final int FORMAT_ALIGN_LEFT = 1;
  private static final int FORMAT_ALIGN_RIGHT = 2;
  private static final int FORMAT_ALIGN_CENTER = 3;

  private final List<Editable> historyList = new LinkedList<>();

  private Context context;
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

  /**
   * Constructor for the basic RichTextEditor.
   *
   * @param context of Fragment or Activity that is including the RichTextEditor.
   * @param attrs   attributeSet of the RichTextEditor.
   */
  public RichTextEditor(Context context, AttributeSet attrs) {
    super(context, attrs);
    init(attrs);
    this.context = context;
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
   * Set or remove the text format bold depending on its related toolbar icon selection.
   *
   * @param valid if the icon for format type bold is not selected.
   */
  public void bold(boolean valid) {
    bold = valid;

    if (valid) {
      applyStyleSpan(FORMAT_BOLD, getSelectionStart(), getSelectionEnd());
    } else {
      removeStyleSpan(FORMAT_BOLD, getSelectionStart(), getSelectionEnd());
    }
  }

  /**
   * Set or remove the text format italic depending on its related toolbar icon selection.
   *
   * @param valid if the icon for format type italic is not selected.
   */
  public void italic(boolean valid) {
    italic = valid;

    if (valid) {
      applyStyleSpan(FORMAT_ITALIC, getSelectionStart(), getSelectionEnd());
    } else {
      removeStyleSpan(FORMAT_ITALIC, getSelectionStart(), getSelectionEnd());
    }
  }

  private void applyStyleSpan(int style, int start, int end) {
    getEditableText().setSpan(new StyleSpan(style), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
  }

  private void removeStyleSpan(int style, int start, int end) {
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
          applyStyleSpan(style, part.getStart(), start);
        }
        if (part.getEnd() > end) {
          applyStyleSpan(style, end, part.getEnd());
        }
      }
    }
  }

  /**
   * Set or remove the text format underline depending on its related toolbar icon selection.
   *
   * @param valid if icon for format type underline is not selected.
   */
  public void underline(boolean valid) {
    underline = valid;

    if (valid) {
      applyUnderlineSpan(getSelectionStart(), getSelectionEnd());
    } else {
      removeUnderlineSpan(getSelectionStart(), getSelectionEnd());
    }
  }

  private void applyUnderlineSpan(int start, int end) {
    if (start < end) {
      getEditableText().setSpan(new UnderlineSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
  }

  private void removeUnderlineSpan(int start, int end) {
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
        applyUnderlineSpan(part.getStart(), start);
      }
      if (part.getEnd() > end) {
        applyUnderlineSpan(end, part.getEnd());
      }
    }
  }

  /**
   * Set or remove the text format strikeThrough depending on its related toolbar icon selection.
   *
   * @param valid if the icon for format type strikeThrough is not selected.
   */
  public void strikeThrough(boolean valid) {
    strikeThrough = valid;

    if (valid) {
      applyStrikeThroughSpan(getSelectionStart(), getSelectionEnd());
    } else {
      removeStrikeThroughSpan(getSelectionStart(), getSelectionEnd());
    }
  }

  private void applyStrikeThroughSpan(int start, int end) {
    if (start < end) {
      getEditableText()
          .setSpan(new StrikethroughSpan(), start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }
  }

  private void removeStrikeThroughSpan(int start, int end) {
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
          applyStrikeThroughSpan(part.getStart(), start);
        }

        if (part.getEnd() > end) {
          applyStrikeThroughSpan(end, part.getEnd());
        }
      }
    }
  }

  /**
   * Set or remove the text format bullets depending on its related toolbar icon selection.
   *
   * @param valid if the icon for format type bullets is not selected.
   */
  public void bullet(boolean valid) {
    bullet = valid;

    if (valid) {
      checkNotContaining(lineContainsFormat(RichTextEditorBulletSpan.class),
          new RichTextEditorBulletSpan());
    } else {
      checkContaining(lineContainsFormat(RichTextEditorBulletSpan.class),
          RichTextEditorBulletSpan.class);
    }
  }

  /**
   * Check if the line does not contain a given span-Object yet and apply it, if that is true.
   *
   * @param contain boolean if the given span-Object is already contained in the relevant string.
   * @param span    that is checked for.
   */
  private void checkNotContaining(Boolean contain, Object span) {
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

  /**
   * Get the current line (either of selected text or the current cursor position) start and end.
   *
   * @return the current line start and end.
   */
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

  /**
   * Adjust the cursor position when alignment has been changed.
   *
   * @param lineStart position of the current line.
   * @param lineEnd   position of the current line.
   */
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

  /**
   * Apply a given span-Object to a text line.
   *
   * @param lineStart position of the current line.
   * @param lineEnd   position of the current line.
   * @param span      object that should be applied.
   */
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
          applyStyleSpan(FORMAT_ITALIC, start, end);
          getEditableText().setSpan(
              new BackgroundColorSpan(ContextCompat.getColor(context, R.color.gray)),
              start, end, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
        }
      }
    }
  }

  /**
   * Check if the line does contain a given span-Object yet and remove it, if that is true.
   *
   * @param contain boolean if the given span-object is already contained in the string.
   * @param span    object to check for.
   */
  private void checkContaining(Boolean contain, Object span) {
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

  /**
   * Remove a given span-object from a text line.
   *
   * @param lineStart    position of the current line.
   * @param stringLength of string the span should be removed of.
   * @param span         object that should be removed.
   */
  private void removeLineSpan(int lineStart, int stringLength, Object span) {
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

        if (span.equals(RichTextEditorBulletSpan.class)) {
          RichTextEditorBulletSpan[] spans =
              getEditableText()
                  .getSpans(start, end, RichTextEditorBulletSpan.class);
          for (RichTextEditorBulletSpan bulletSpan : spans) {
            getEditableText().removeSpan(bulletSpan);
          }
        } else {
          RichTextEditorQuoteSpan[] spans =
              getEditableText().getSpans(start, end, RichTextEditorQuoteSpan.class);
          for (RichTextEditorQuoteSpan quoteSpan : spans) {
            getEditableText().removeSpan(quoteSpan);
          }

          BackgroundColorSpan[] backgroundColorSpans =
              getEditableText().getSpans(start, end, BackgroundColorSpan.class);
          for (BackgroundColorSpan backgroundColorSpan : backgroundColorSpans) {
            getEditableText().removeSpan(backgroundColorSpan);
          }

          removeStyleSpan(FORMAT_ITALIC, start, end);
        }
      }
    }
  }

  /**
   * Return whether the editable text contains a given span-object.
   *
   * @param span object that should be checked for.
   * @return true if the text already contains the given span object.
   */
  private boolean lineContainsFormat(Object span) {
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
    } while (lineContainsFormat(i, span));

    return false;
  }

  /**
   * Return whether a line at given index from the entire editable text lines contains a given
   * span-object yet.
   *
   * @param index of the line.
   * @param span  object that should be checked for.
   * @return true if the line already contains the given span-object.
   */
  private boolean lineContainsFormat(int index, Object span) {
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
   * Set or remove the text format quote depending on its related toolbar icon selection.
   *
   * @param valid if the icon for format type quote is not selected.
   */
  public void quote(boolean valid) {
    quote = valid;

    if (valid) {
      checkNotContaining(lineContainsFormat(RichTextEditorQuoteSpan.class),
          new RichTextEditorQuoteSpan());
    } else {
      checkContaining(lineContainsFormat(RichTextEditorQuoteSpan.class),
          RichTextEditorQuoteSpan.class);
    }
  }

  /**
   * Align the text left.
   */
  public void alignLeft() {
    applyAlignment(FORMAT_ALIGN_LEFT);
    alignmentLeft = !alignmentLeft;
  }

  /**
   * Align the text right, if it is not yet, otherwise align the text left.
   */
  public void alignRight() {
    if (!alignmentRight) {
      alignmentRight = true;
      applyAlignment(FORMAT_ALIGN_RIGHT);
    } else {
      alignmentRight = false;
      applyAlignment(FORMAT_ALIGN_LEFT);
    }
  }

  /**
   * Align the text central, if it is not yet, otherwise align the text left.
   */
  public void alignCenter() {
    if (!alignmentCenter) {
      alignmentCenter = true;
      applyAlignment(FORMAT_ALIGN_CENTER);
    } else {
      alignmentCenter = false;
      applyAlignment(FORMAT_ALIGN_LEFT);
    }
  }

  /**
   * Apply a given alignment style.
   *
   * @param style of the alignment to apply.
   */
  private void applyAlignment(int style) {
    int start = getSelectionStart();
    int end = getSelectionEnd();
    if (!hasSelection()) {
      start = getLineBoundaries()[0];
      end = getLineBoundaries()[1];
    }

    adjustAlignment(style, start, end);
  }

  /**
   * Adjust the alignment of a given line to a given style.
   *
   * @param style of the alignment to adjust to.
   * @param start of the line.
   * @param end   of the line.
   */
  private void adjustAlignment(int style, int start, int end) {
    if (start >= end) {
      return;
    }

    //Clear the text from alignments to avoid double assignments
    Object[] spansToRemove = getEditableText().getSpans(start, end, AlignmentSpan.class);
    for (Object span : spansToRemove) {
      getEditableText().removeSpan(span);
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
   * Redo the last user input action.
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
   * Check if there is a redo-able user input action.
   *
   * @return true, if there is a valid user input action to redo.
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
   * Undo the last user input action.
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
   * Check if there is an undo-able user input action.
   *
   * @return true, if there is a valid user input action to undo.
   */
  public boolean undoValid() {
    if (historyEnable && historySize > 0 && !historyWorking) {
      return !historyList.isEmpty() && historyCursor > 0;
    }

    return false;
  }

  /**
   * Save the current text format as a span before any further user input action to provide a
   * history of redo-able and undo-able actions.
   *
   * @param text current editable text as charSequence.
   */
  public void beforeTextChanged(CharSequence text, int start, int count, int after) {
    if (historyEnable && !historyWorking) {
      inputBefore = new SpannableStringBuilder(text);
    }
  }

  /**
   * Apply selected text format options as spans on new inserted text.
   *
   * @param text current editable text as charSequence.
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

    applySpans(str, lastCursorPosition, endOfString);
  }

  /**
   * Apply the spans that represent the selected text format options.
   *
   * @param spannable          to apply.
   * @param lastCursorPosition current position of the cursor.
   * @param endOfString        end of the entire text string.
   */
  private void applySpans(Spannable spannable, int lastCursorPosition, int endOfString) {
    if (bold) {
      applyStyleSpan(FORMAT_BOLD, lastCursorPosition, endOfString);
    }

    if (italic) {
      applyStyleSpan(FORMAT_ITALIC, lastCursorPosition, endOfString);
    }

    if (underline) {
      applyUnderlineSpan(lastCursorPosition, endOfString);
    }

    if (strikeThrough) {
      applyStrikeThroughSpan(lastCursorPosition, endOfString);
    }

    if (bullet) {
      checkNotContaining(lineContainsFormat(RichTextEditorBulletSpan.class),
          new RichTextEditorBulletSpan());
    }

    if (quote) {
      checkNotContaining(lineContainsFormat(RichTextEditorQuoteSpan.class),
          new RichTextEditorQuoteSpan());
      applyStyleSpan(FORMAT_ITALIC, lastCursorPosition, endOfString);
      spannable.setSpan(new BackgroundColorSpan(ContextCompat.getColor(context, R.color.gray)),
          lastCursorPosition, endOfString, Spanned.SPAN_EXCLUSIVE_EXCLUSIVE);
    }

    if (alignmentLeft) {
      applyAlignment(FORMAT_ALIGN_LEFT);
    }

    if (alignmentRight) {
      applyAlignment(FORMAT_ALIGN_RIGHT);
    }

    if (alignmentCenter) {
      applyAlignment(FORMAT_ALIGN_CENTER);
    }
  }

  /**
   * Save current text state after any user input action to provide a history of input actions.
   *
   * @param text current text content as editable.
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
