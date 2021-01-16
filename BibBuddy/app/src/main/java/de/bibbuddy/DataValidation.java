package de.bibbuddy;

import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;

public class DataValidation {
    private static final String[] FORMATS = {
            "yyyy-MM-dd'T'HH:mm:ss'Z'",   "yyyy-MM-dd'T'HH:mm:ssZ",
            "yyyy-MM-dd'T'HH:mm:ss",      "yyyy-MM-dd'T'HH:mm:ss.SSS'Z'",
            "yyyy-MM-dd'T'HH:mm:ss.SSSZ", "yyyy-MM-dd HH:mm:ss",
            "dd/MM/yyyy HH:mm:ss",        "MM/dd/yyyy'T'HH:mm:ss.SSS'Z'",
            "dd/MM/yyyy'T'HH:mm:ss.SSSZ", "MM/dd/yyyy'T'HH:mm:ss.SSS",
            "dd/MM/yyyy'T'HH:mm:ssZ",     "MM/dd/yyyy'T'HH:mm:ss",
            "yyyy:MM:dd HH:mm:ss",        "yyyyMMdd",
            "dd.MM.yyyy",        "yyyyMMdd", };

    //date format to which the input strings should be converted
    private static final String FORMAT_CONV="dd.MM.yyyy";
    
    //checks for the type of the input (String, Integer or Date)
    public boolean isInputString(Object inputObj){
        return inputObj instanceof String;
    }

    public boolean isInputInteger(Object inputObj){
        return inputObj instanceof Integer;

    }
    public boolean isInputDate(Object inputObj){
        return inputObj instanceof Date;

    }

    //checks if the input string is not null
    public boolean isStringNotNull(String inputStr){
        if(inputStr != null)
            return true;
        return false;
    }

    //checks if the input string is not empty
    public boolean isStringNotEmpty(String inputStr){
        return inputStr.isEmpty();

    }

    //checks if the input integer is not equal to null
    public boolean isIntegerNotNull(int inputInt) {
        if (inputInt != 0)
            return true;
        return false;
    }

    //checks if the input string has a valid date format
    //if yes: convert the current date format to "dd.MM.yyyy"
    //additional date formats can be added in the "FORMATS" array
    public String checkStrValidDateFormat(String inputStr) {
        if (inputStr != null) {
            for (String parse : FORMATS) {
                SimpleDateFormat sdf = new SimpleDateFormat(parse);
                try {
                    sdf.parse(inputStr);
                    SimpleDateFormat df = new SimpleDateFormat(parse);
                    Date date = df.parse(inputStr);
                    df.applyPattern(FORMAT_CONV);
                    String convertedStr = df.format(date);
                    return convertedStr;
                } catch (ParseException e) {
                }
            }
        }
        return null;
    }

    //converts a string with a valid date format to type Date
    public Date convertStrToDate(String inputStr) throws ParseException {
        String validDateFormatStr = checkStrValidDateFormat(inputStr);
        if(validDateFormatStr!=null){
            Date date = new SimpleDateFormat(FORMAT_CONV).parse(inputStr);
            return date;
        }
        else{
            return null;
        }
    }

    //checks if the input date is before the current date
    public boolean dateBeforeToday(String inputStr) throws ParseException {
        String validDateFormatStr = checkStrValidDateFormat(inputStr);
        if(validDateFormatStr!=null) {
            SimpleDateFormat sdf = new SimpleDateFormat(FORMAT_CONV);
            Date date = sdf.parse(validDateFormatStr);
            Date currDate = new Date();
            if (date.compareTo(currDate) <= 0)
                return true;
            return false;
        }
        return false;
    }
}
