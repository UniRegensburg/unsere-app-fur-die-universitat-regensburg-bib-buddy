package de.bibbuddy;

import java.util.Date;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataValidation {

    //checks for the type of the input (String, Integer or Date)
    public boolean isInputString(Object inputObj) {
        return inputObj instanceof String;
    }

    public boolean isInputInteger(Object inputObj) {
        return inputObj instanceof Integer;
    }

    public boolean isInputDate(Object inputObj) {
        return inputObj instanceof Date;
    }

    //checks if the input string is not null
    public boolean isStringNull(String inputStr) {
        if(inputStr == null) return true;
        return false;
    }

    //checks if the input string is not empty
    public boolean isStringEmpty(String inputStr) {
        return inputStr.isEmpty();
    }

    //checks if the input integer is not equal to null
    public boolean isIntegerNull(int inputInt) {
        if(inputInt == 0) return true;
        return false;
    }

    //checks for special characters that are not latin letters or numbers
    public boolean hasSpecialChars(String inputStr) {
        Pattern my_pattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher my_match = my_pattern.matcher(inputStr);
        boolean check = my_match.find();
        if(check) return true;
        return false;
    }

    public boolean validateISBN13(String isbnStr) {
        if(isbnStr == null ) return false;

        //remove hyphens
        isbnStr = isbnStr.replaceAll("-", "");

        //ISBN must have length of 13
        if(isbnStr.length() != 13) return false;

        try {
            //sum of digit position * digit value of this position
            int totalSum = 0;
            for(int i = 0; i < 12; i++) {
                //get every digit in ISBN and convert it to Integer
                int digit = Integer.parseInt( isbnStr.substring( i, i + 1 ) );
                totalSum += (i % 2 == 0) ? digit * 1 : digit * 3;
            }
            //checksum must be 0-9. If calculated as 10 then = 0
            int checksum = 10-(totalSum % 10);
            if(checksum == 10) checksum = 0; return checksum == Integer.parseInt(isbnStr.substring(12));
        }
        catch (NumberFormatException nfe) {
            //to catch invalid ISBNs with non-numeric characters
            return false;
        }
    }

    public boolean validateISBN10(String isbnStr) {
        if(isbnStr == null) return false;

        //remove hyphens
        isbnStr = isbnStr.replaceAll("-", "");

        //ISBN must have length of 10
        if (isbnStr.length() != 10 ) return false;
        try {
            int totalSum = 0;
            for(int i = 0; i < 9; i++) {
                //get every digit in ISBN and convert it to Integer
                int digit = Integer.parseInt(isbnStr.substring(i, i + 1) );
                //sum of digit position * digit value of this position
                totalSum += ((10-i)*digit);
            }

            String checksum = Integer.toString((11-(totalSum % 11))%11);
            if ("10".equals(checksum)) checksum = "X";
            return checksum.equals(isbnStr.substring(9));
        }
        catch (NumberFormatException nfe) {
            //to catch invalid ISBNs with non-numeric characters
            return false;
        }
    }
    public boolean isValidISBN10or13(String isbnStr) {
        if(validateISBN10(isbnStr) || validateISBN13(isbnStr)) return true;
        return false;
    }
}
