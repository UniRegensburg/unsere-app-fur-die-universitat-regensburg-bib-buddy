package de.bibbuddy;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class DataValidation {

    //checks if the input string is not null
    public static boolean isStringEmpty(String inputStr) {
        return inputStr == null || inputStr.isEmpty();
    }

    //checks for special characters that are not latin letters or numbers
    public static boolean hasSpecialChars(String inputStr) {
        Pattern pattern = Pattern.compile("[^a-z0-9 ]", Pattern.CASE_INSENSITIVE);
        Matcher match = pattern.matcher(inputStr);
        return match.find();
    }

    public static boolean validateISBN13(String isbnStr) {
        if (isStringEmpty(isbnStr))
            return false;

        //remove hyphens
        isbnStr = isbnStr.replaceAll("-", "");

        //ISBN must have length of 13
        if (isbnStr.length() != 13)
            return false;

        try {
            //sum of digit position * digit value of this position
            int totalSum = 0;
            for (int i = 0; i < 12; i++) {
                //get every digit in ISBN and convert it to Integer
                int digit = Integer.parseInt(isbnStr.substring(i, i + 1));
                totalSum += (i % 2 == 0 ? digit * 1 : digit * 3);
            }

            //checksum must be 0-9. If calculated as 10 then = 0
            int checksum = 10 - (totalSum % 10);
            if (checksum == 10) {
                checksum = 0;
            }

            return checksum == Integer.parseInt(isbnStr.substring(12));
        } catch (NumberFormatException nfe) {
            //to catch invalid ISBNs with non-numeric characters
            return false;
        }
    }

    public static boolean validateISBN10(String isbnStr) {
        if (isStringEmpty(isbnStr))
            return false;

        //remove hyphens
        isbnStr = isbnStr.replaceAll("-", "");

        //ISBN must have length of 10
        if (isbnStr.length() != 10)
            return false;
        
        try {
            int totalSum = 0;
            for (int i = 0; i < 9; i++) {
                //get every digit in ISBN and convert it to Integer
                int digit = Integer.parseInt(isbnStr.substring(i, i + 1));
                //sum of digit position * digit value of this position
                totalSum += ((10 - i) * digit);
            }

            String checksum = Integer.toString((11 - (totalSum % 11)) % 11);
            if ("10".equals(checksum)) {
                checksum = "X";
            }

            return checksum.equals(isbnStr.substring(9));
        } catch (NumberFormatException nfe) {
            //to catch invalid ISBNs with non-numeric characters
            return false;
        }
    }

    public static boolean isValidISBN10or13(String isbnStr) {
        return validateISBN10(isbnStr) || validateISBN13(isbnStr);
    }

}
