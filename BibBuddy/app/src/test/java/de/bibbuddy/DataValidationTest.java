package de.bibbuddy;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;


import org.junit.Test;

/**
 * Tests for the DataValidation class.
 *
 * @author Claudia Schönherr
 */
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
  public void validateIsbn13_Test() {
    assertTrue(DataValidation.validateIsbn13("978-0-123456-47-2"));
    assertFalse(DataValidation.validateIsbn13("0-12356-47-9"));
  }

  @Test
  public void validateIsbn10_Test() {
    assertTrue(DataValidation.validateIsbn10("0-123456-47-9"));
    assertFalse(DataValidation.validateIsbn10("0-123456-9"));
  }

  @Test
  public void isValidIsbn10or13_Test() {
    assertTrue(DataValidation.isValidIsbn10or13("0-123456-47-9"));
    assertFalse(DataValidation.isValidIsbn10or13("0-1252456-9"));
    assertFalse(DataValidation.validateIsbn13("0-12323-47-9"));
  }

  @Test
  public void isValidPositveNumber_Test() {
    assertTrue(DataValidation.isValidPositiveNumber("213214"));
    assertFalse(DataValidation.isValidPositiveNumber("213214f"));
    assertFalse(DataValidation.isValidPositiveNumber("2 "));
    assertFalse(DataValidation.isValidPositiveNumber("number"));
  }
}
