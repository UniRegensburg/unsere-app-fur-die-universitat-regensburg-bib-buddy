package de.bibbuddy;

import android.content.Context;
import android.net.Uri;
import android.widget.Toast;
import androidx.annotation.NonNull;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Objects;
import java.util.stream.Collectors;

/**
 * The ImportBibTex is responsible for the import of a
 * BibTex file.
 * It contains methods for reading and parsing the
 * contents of the imported file.
 *
 * @author Silvia Ivanova
 */
public class ImportBibTex {

  private final String [] bibTags;
  private final Context context;
  private final HashMap<String, String> bibTagValue;

  /**
   * Constructor for the Import of BibTeX file.
   *
   * @param context      current context
   * @param bibTags      Array of BibTeX tags as String (e.g. "author=", "title=")
   * @param bibTagValue  a HashMap to store the parsed BibTeX data
   *                     Key is BibTeX tag.
   *                     Value is the parsed text content for the corresponding BibTex tag.
   */
  public ImportBibTex(Context context, String [] bibTags, HashMap<String, String> bibTagValue) {
    this.context = context;
    this.bibTags = bibTags;
    this.bibTagValue = bibTagValue;
  }

  /**
   * Checks if a file is from type BibTeX.
   *
   * @param filePath      path of the file
   */
  public boolean isBibFile(@NonNull String filePath) {
    String extension = filePath.substring(filePath.lastIndexOf("."));
    return extension.equals(StorageKeys.BIB_FILE_TYPE);
  }

  /**
   * Reads the URI line by line and appends the content to
   * a String Builder.
   * This method also removes initial unnecessary brackets from
   * the concatenated URI content.
   *
   * @param uri      Uniform Resource Identifier (URI)
   * @return         the URI content as String
   */
  @NonNull
  public String readTextFromUri(Uri uri) throws IOException {
    StringBuilder stringBuilder = new StringBuilder();

    try (InputStream inputStream =
             context.getContentResolver().openInputStream(uri);

         BufferedReader reader = new BufferedReader(
             new InputStreamReader(Objects.requireNonNull(inputStream)))) {

      String line;
      while ((line = reader.readLine()) != null) {
        removeEqualSignFromBibTag(line);
        stringBuilder.append(line).append('\n');
        stringBuilder.setLength(stringBuilder.length() - 1);
      }

      removeCurlyBracketsFromBibTag(stringBuilder,
          new String[] {BibTexKeys.CLOSING_CURLY_BRACKET, BibTexKeys.OPENING_CURLY_BRACKET});
    }

    return stringBuilder.toString();
  }

  /** Splits the String content of URI file and removes redundant
   * BibTeX elements.
   *
   * @param bibText  the BibTeX content as String
   * @return         list of unique BibTeX elements
   */
  public List<String> getNonRedundantBibItems(@NonNull String bibText) {

    return Arrays.stream(bibText.split(BibTexKeys.BOOK_TAG_REGEX))
        .distinct().collect(Collectors.toList());
  }

  private void removeCurlyBracketsFromBibTag(@NonNull StringBuilder stringBuilder,
                                             @NonNull String[] brackets) {
    for (String bracket : brackets) {
      if (stringBuilder.toString().contains(bracket)) {

        int bracketIndex = stringBuilder.indexOf(bracket);
        while (bracketIndex != -1) {
          stringBuilder.replace(bracketIndex, bracketIndex + bracket.length(), "");
          bracketIndex += "".length();
          bracketIndex = stringBuilder.indexOf(bracket, bracketIndex);
        }

      }

    }
  }

  private void removeEqualSignFromBibTag(String line) {
    for (String bibTag : bibTags) {

      if (line.contains("=") && line.contains(bibTag)) {
        line = line.replaceFirst("\\s*=\\s*", "=");
      }

    }
  }

  /**
   * Cleans the BibTeX content through removing the unnecessary
   * separators for a given BibTeX-element.
   *
   * @param bibItemsList      List with BibTeX elements as Strings
   * @param bibTag            current BibTeX tag
   */
  public void removeBibTexSeparators(@NonNull List<String> bibItemsList, String bibTag) {

    String parsedBibValue = "";

    for (int i = 0; i < bibItemsList.size(); i++) {
      String currentBibItem = bibItemsList.get(i);

      if (currentBibItem.contains(bibTag)) {
        parsedBibValue = replaceLast(currentBibItem, BibTexKeys.COMMA_SEPARATOR);
        parsedBibValue = replaceLast(parsedBibValue, "\n");
        parsedBibValue = parsedBibValue.replace(bibTag, "");
        bibItemsList.set(i, parsedBibValue);
      }

    }

    bibTagValue.put(bibTag, parsedBibValue);
  }


  /** Checks for the position of the next BibTeX tag.
   * The BibTeX tags (without "@book") do not have fixed order, so this
   * method stores the positions of the BibTeX tags  in a List and then
   * through sorting of this list - determines which is the next BibTeX tag.
   *
   * @param bibItem  a single BibTeX item as String
   * @return         the next BibTeX element with a BibTeX tag
   */
  public List<String> checkNextBibTag(@NonNull String bibItem) {
    ArrayList<Integer> nextBibTagIndex = new ArrayList<>();
    ArrayList<String> bibItemsWithTags = new ArrayList<>();

    if (bibItem.contains(BibTexKeys.BOOK_TAG)) {
      nextBibTagIndex.add(bibItem.indexOf(BibTexKeys.BOOK_TAG));

      for (String bibTag : bibTags) {
        if (bibItem.contains(bibTag)) {
          nextBibTagIndex.add(bibItem.indexOf(bibTag));
        }
      }
    }

    nextBibTagIndex.add(bibItem.length());
    Arrays.sort(nextBibTagIndex.toArray());

    int currentBibTagPosition = 0;
    for (int i = 0; i < nextBibTagIndex.size(); i++) {
      int nextBibTagPosition = nextBibTagIndex.get(i);
      if (nextBibTagPosition > 0) {
        bibItemsWithTags.add(bibItem.substring(currentBibTagPosition,
            nextBibTagPosition));
        currentBibTagPosition = nextBibTagPosition;
      }
    }

    return bibItemsWithTags;
  }

  /** Parses the authors from the BibTeX content.
   * Since the author names in BibTeX are comma separated
   * (no matter how many authors there are), this method
   * considers parsing of one author and of multiple authors.
   *
   * @return         List of parsed author(s) name(s) and family
   *                 name(s).
   */
  public List<Author> parseAuthorNames() {

    String authorNames = (String) bibTagValue.get(BibTexKeys.AUTHOR);

    List<Author> authors = new ArrayList<>();

    //for multiple authors
    if (authorNames.contains(BibTexKeys.AND_MULTIPLE_AUTHORS)) {
      String[] names = authorNames.split(BibTexKeys.AND_MULTIPLE_AUTHORS);

      for (String name : names) {
        if (name.contains(", ")) {
          String[] currAuthorsNames = name.split(", ");
          authors.add(new Author(currAuthorsNames[1], currAuthorsNames[0], ""));
        }
      }

    }
    //for one author
    if (!authorNames.contains(BibTexKeys.AND_MULTIPLE_AUTHORS)
        && authorNames.contains(",")) {

      String[] authorName = authorNames.split(", ");
      authors.add(new Author(authorName[1], authorName[0], ""));

    }

    return authors;
  }

  /** Adds the parsed BibTeX value for one/ more notes into the
   * Database and links the created note with a book.
   *
   * @param noteDao  object of the NoteDao class
   *                 responsible for adding note into the Database
   * @param book     the book of the added note
   */
  public void importBibNote(NoteDao noteDao, Book book) {
    if (bibTagValue.containsKey(BibTexKeys.ANNOTE)
        && !bibTagValue.get(BibTexKeys.ANNOTE).equals("")) {

      Note note = new Note(bibTagValue.get(BibTexKeys.BOOK_TITLE)
          + " " + bibTagValue.get(BibTexKeys.AUTHOR), 0,
          bibTagValue.get(BibTexKeys.ANNOTE));

      noteDao.create(note);
      noteDao.linkNoteWithBook(book.getId(), note.getId());
    }
  }

  /** Sets the parsed BibTeX values for a book.
   *
   * @return a new Book object
   */
  public Book importBook() {

    return new Book(bibTagValue.get(BibTexKeys.ISBN), bibTagValue.get(BibTexKeys.BOOK_TITLE),
        "",  getParsedYear(), bibTagValue.get(BibTexKeys.PUBLISHER),
        bibTagValue.get(BibTexKeys.VOLUME), bibTagValue.get(BibTexKeys.EDITION), "");

  }

  private int getParsedYear() {
    String year = bibTagValue.get(BibTexKeys.YEAR);

    if (DataValidation.isValidYear(year)) {
      return Integer.parseInt(bibTagValue.get(BibTexKeys.YEAR));
    }

    return 0;
  }

  @NonNull
  private String replaceLast(String string, String substring) {
    int index = string.lastIndexOf(substring);
    if (index == -1) {
      return string;
    }

    return string.substring(0, index) + ""
        + string.substring(index + substring.length());
  }

}
