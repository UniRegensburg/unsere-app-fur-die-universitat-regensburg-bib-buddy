package de.bibbuddy;

import org.junit.Test;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

public class DataValidationTest {

    @Test
    public void isStringEmpty_Test() {
        assertTrue(DataValidation.isStringEmpty(""));
        assertFalse(DataValidation.isStringEmpty(" sdfdsf"));
    }

    @Test
    public void hasSpecialChars_Test() {
        assertTrue(DataValidation.hasSpecialChars("#äödfs"));
        assertFalse(DataValidation.hasSpecialChars("das Regal 1"));
        assertFalse(DataValidation.hasSpecialChars("das Regal über ß"));
    }

    @Test
    public void validateISBN13_Test() {
        assertTrue(DataValidation.validateISBN13("978-0-123456-47-2"));
        assertFalse(DataValidation.validateISBN13("0-12356-47-9"));
    }

    @Test
    public void validateISBN10_Test() {
        assertTrue(DataValidation.validateISBN10("0-123456-47-9"));
        assertFalse(DataValidation.validateISBN10("0-123456-9"));
    }

    @Test
    public void isValidISBN10or13_Test() {
        assertTrue(DataValidation.isValidISBN10or13("0-123456-47-9"));
        assertFalse(DataValidation.isValidISBN10or13("0-1252456-9"));
        assertFalse(DataValidation.validateISBN13("0-12323-47-9"));
    }
}
