package de.bibbuddy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * The DataValidation is responsible for checking if the input is valid or not.
 *
 * @author Silvia Ivanova, Claudia Schönherr
 */
public class DataValidation {

  /**
   * Checks if the input string is not null.
   *
   * @param inputStr input String of a user
   * @return true if the String is empty
   */
  public static boolean isStringEmpty(String inputStr) {
    return inputStr == null || inputStr.isEmpty() || inputStr.trim().isEmpty();
  }

  /**
   * Checks for special characters that are not latin letters or numbers.
   *
   * @param inputStr input String of a user
   * @return true if the String has special characters
   */
  public static boolean hasSpecialChars(String inputStr) {
    Pattern pattern = Pattern.compile("[^a-z0-9äöüß ]", Pattern.CASE_INSENSITIVE);
    Matcher match = pattern.matcher(inputStr);

    return match.find();
  }

  /**
   * Checks if the String is a valid ISBN13 String.
   *
   * @param isbnStr ISBN String of the input
   * @return true if the String is a valid ISBN13 String
   */
  public static boolean validateIsbn13(String isbnStr) {
    // ISBN is optional and could be empty
    if (isStringEmpty(isbnStr)) {
      return true;
    }

    isbnStr = isbnStr.replaceAll("-", "");

    // ISBN must have length of 13
    if (isbnStr.length() != 13) {
      return false;
    }

    try {
      // sum of digit position * digit value of this position
      int totalSum = 0;

      for (int i = 0; i < 12; i++) {
        // get every digit in ISBN and convert it to Integer
        int digit = Integer.parseInt(isbnStr.substring(i, i + 1));
        totalSum += (i % 2 == 0 ? digit : digit * 3);
      }

      // checksum must be 0-9. If calculated as 10 then = 0
      int checksum = 10 - (totalSum % 10);
      if (checksum == 10) {
        checksum = 0;
      }

      return checksum == Integer.parseInt(isbnStr.substring(12));
    } catch (NumberFormatException nfe) {
      // to catch invalid ISBNs with non-numeric characters
      return false;
    }
  }

  /**
   * Checks if the String is a valid ISBN10 String.
   *
   * @param isbnStr ISBN String of the input
   * @return true if the String is a valid ISBN10 String
   */
  public static boolean validateIsbn10(String isbnStr) {
    // ISBN is optional and could be empty
    if (isStringEmpty(isbnStr)) {
      return true;
    }

    isbnStr = isbnStr.replaceAll("-", "");

    // ISBN must have length of 10
    if (isbnStr.length() != 10) {
      return false;
    }

    try {
      int totalSum = 0;
      for (int i = 0; i < 9; i++) {
        // get every digit in ISBN and convert it to Integer
        int digit = Integer.parseInt(isbnStr.substring(i, i + 1));
        // sum of digit position * digit value of this position
        totalSum += ((10 - i) * digit);
      }

      String checksum = Integer.toString((11 - (totalSum % 11)) % 11);
      if ("10".equals(checksum)) {
        checksum = "X";
      }

      return checksum.equals(isbnStr.substring(9));
    } catch (NumberFormatException nfe) {
      // to catch invalid ISBNs with non-numeric characters
      return false;
    }
  }

  /**
   * Checks if the String is a valid ISBN10 or ISBN13 String.
   *
   * @param isbnStr ISBN String of the input
   * @return true if the String is a valid ISBN10 or ISBN13 String
   */
  public static boolean isValidIsbn10or13(String isbnStr) {
    return validateIsbn10(isbnStr) || validateIsbn13(isbnStr);
  }

  /**
   * Checks if the String is a valid year.
   *
   * @param numberStr String of the input
   * @return true if the String is a valid year
   */
  public static boolean isValidYear(String numberStr) {
    if (isStringEmpty(numberStr)) {
      return false;
    }

    boolean isNumber = numberStr.chars().allMatch(Character::isDigit);

    return isNumber && Integer.parseInt(numberStr) >= 0 && numberStr.length() < 5;
  }

}
