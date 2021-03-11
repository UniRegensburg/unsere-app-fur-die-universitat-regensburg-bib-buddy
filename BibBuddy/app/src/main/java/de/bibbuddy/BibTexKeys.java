package de.bibbuddy;

/**
 * Constants for parsing of the BibTex.
 *
 * @author Silvia Ivanova
 */
public class BibTexKeys {

  public static final String BOOK_TAG = "@book";
  public static final String ISBN = "isbn=";
  public static final String AUTHOR = "author=";
  public static final String BOOK_TITLE = "title=";
  public static final String SUBTITLE = "subtitle=";
  public static final String VOLUME = "volume=";
  public static final String PUBLISHER = "publisher=";
  public static final String EDITION = "edition=";
  public static final String ANNOTE = "annote=";
  public static final String YEAR = "year=";

  public static final String BOOK_TAG_REGEX = "(?=@book)";

  public static final String AND_MULTIPLE_AUTHORS = " and ";

  public static final String OPENING_CURLY_BRACKET = "{";
  public static final String CLOSING_CURLY_BRACKET = "}";
  public static final String COMMA_SEPARATOR = ",";

}