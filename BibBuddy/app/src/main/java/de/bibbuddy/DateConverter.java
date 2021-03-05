package de.bibbuddy;

import java.text.SimpleDateFormat;
import java.util.Date;

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
    SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss.SSSXXX");

    String dateStr = simpleDateFormat.format(newDate);
    String day = dateStr.substring(8, 10);
    String month = dateStr.substring(5, 7);
    String year = dateStr.substring(0, 4);
    String time = dateStr.substring(11, 16);

    dateStr = day + "." + month + "." + year + " " + time + " Uhr";

    return dateStr;
  }

}
