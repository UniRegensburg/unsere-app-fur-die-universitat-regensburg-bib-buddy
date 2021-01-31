package de.bibbuddy;

import org.junit.Test;

import static org.junit.Assert.*;

public class DataValidationTest {

   @Test
   public void isStringEmpty() {
      assertTrue(DataValidation.isStringEmpty(""));
      assertFalse(DataValidation.isStringEmpty(" sdfdsf"));
   }

   @Test
   public void hasSpecialChars() {
      assertTrue(DataValidation.hasSpecialChars("#äödfs"));
      assertFalse(DataValidation.hasSpecialChars("das Regal 1"));
   }

   @Test
   public void validateISBN13() {
      assertTrue(DataValidation.validateISBN13("978-0-123456-47-2"));
      assertFalse(DataValidation.validateISBN13("0-12356-47-9"));
   }

   @Test
   public void validateISBN10() {
      assertTrue(DataValidation.validateISBN10("0-123456-47-9"));
      assertFalse(DataValidation.validateISBN10("0-123456-9"));
   }

   @Test
   public void isValidISBN10or13() {
      assertTrue(DataValidation.isValidISBN10or13("0-123456-47-9"));
      assertFalse(DataValidation.isValidISBN10or13("0-1252456-9"));
      assertFalse(DataValidation.validateISBN13("0-12323-47-9"));
   }
}
