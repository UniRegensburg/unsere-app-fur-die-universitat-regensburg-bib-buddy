package de.bibbuddy;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.Locale;

/**
 * The DateConverter is responsible for converting the date.
 *
 * @author Claudia Schönherr
 */
public class DateConverter {

  /**
   * Converts the date to a String.
   *
   * @param date that should be converted
   * @return Returns the converted date as a String
   */
  public static String convertDateToString(Long date) {
    Date newDate = new Date(date);
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("dd.MM.yyyy' 'HH:mm",
        Locale.getDefault());
    return simpleDateFormat.format(newDate) + " Uhr";
  }

}
