package de.freshplan.shared.util;

import static org.assertj.core.api.Assertions.assertThat;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Tag;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Unit Tests für HtmlUtils.
 *
 * <p>Sprint 2.1.4: Neu erstellt als Plain JUnit Test.
 *
 * <p>Testet HTML-Escaping für XSS-Prevention.
 *
 * @see TEST_DEBUGGING_GUIDE.md
 */
@Tag("unit")
@DisplayName("HtmlUtils Unit Tests")
class HtmlUtilsTest {

  @Test
  @DisplayName("escapeHtml should return empty string for null input")
  void escapeHtml_withNull_shouldReturnEmptyString() {
    // When
    String result = HtmlUtils.escapeHtml(null);

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("escapeHtml should return empty string for empty input")
  void escapeHtml_withEmptyString_shouldReturnEmptyString() {
    // When
    String result = HtmlUtils.escapeHtml("");

    // Then
    assertThat(result).isEmpty();
  }

  @Test
  @DisplayName("escapeHtml should not modify safe text")
  void escapeHtml_withSafeText_shouldReturnUnmodified() {
    // Given
    String safeText = "Hello World 123";

    // When
    String result = HtmlUtils.escapeHtml(safeText);

    // Then
    assertThat(result).isEqualTo("Hello World 123");
  }

  @ParameterizedTest
  @DisplayName("escapeHtml should escape HTML special characters")
  @CsvSource({
    "'<script>alert(1)</script>', '&lt;script&gt;alert(1)&lt;&#x2F;script&gt;'",
    "'<img src=x onerror=alert(1)>', '&lt;img src=x onerror=alert(1)&gt;'",
    "'\"onclick=\"alert(1)', '&quot;onclick=&quot;alert(1)'",
    "''onclick=''alert(1), '&#x27;&#x27;onclick=&#x27;&#x27;alert(1)'",
    "'A & B', 'A &amp; B'",
    "'</div>', '&lt;&#x2F;div&gt;'"
  })
  void escapeHtml_withSpecialCharacters_shouldEscapeProperly(String input, String expected) {
    // When
    String result = HtmlUtils.escapeHtml(input);

    // Then
    assertThat(result).isEqualTo(expected);
  }

  @Test
  @DisplayName("escapeHtml should escape all dangerous characters at once")
  void escapeHtml_withMultipleDangerousChars_shouldEscapeAll() {
    // Given
    String dangerous = "<>&\"'/";

    // When
    String result = HtmlUtils.escapeHtml(dangerous);

    // Then
    assertThat(result).isEqualTo("&lt;&gt;&amp;&quot;&#x27;&#x2F;");
  }

  @Test
  @DisplayName("escapeHtml with default should return escaped default for null")
  void escapeHtml_withNullAndDefault_shouldReturnEscapedDefault() {
    // When
    String result = HtmlUtils.escapeHtml(null, "Default <value>");

    // Then
    assertThat(result).isEqualTo("Default &lt;value&gt;");
  }

  @Test
  @DisplayName("escapeHtml with default should return escaped default for empty")
  void escapeHtml_withEmptyAndDefault_shouldReturnEscapedDefault() {
    // When
    String result = HtmlUtils.escapeHtml("   ", "Default");

    // Then
    assertThat(result).isEqualTo("Default");
  }

  @Test
  @DisplayName("escapeHtml with default should escape input when not empty")
  void escapeHtml_withValueAndDefault_shouldEscapeInput() {
    // When
    String result = HtmlUtils.escapeHtml("<script>", "Default");

    // Then
    assertThat(result).isEqualTo("&lt;script&gt;");
  }

  @Test
  @DisplayName("escapeHtml should prevent common XSS attack vectors")
  void escapeHtml_withXssVectors_shouldPreventAttack() {
    // Given - Common XSS attack patterns
    String xss1 = "<img src=x onerror=alert('XSS')>";
    String xss2 = "<script>document.cookie</script>";
    String xss3 = "javascript:alert('XSS')";
    String xss4 = "<iframe src='javascript:alert(1)'></iframe>";

    // When
    String result1 = HtmlUtils.escapeHtml(xss1);
    String result2 = HtmlUtils.escapeHtml(xss2);
    String result3 = HtmlUtils.escapeHtml(xss3);
    String result4 = HtmlUtils.escapeHtml(xss4);

    // Then - No executable code should remain
    assertThat(result1).doesNotContain("<img");
    assertThat(result2).doesNotContain("<script>");
    assertThat(result3).contains("&#x27;"); // ' escaped
    assertThat(result4).doesNotContain("<iframe");
  }

  @Test
  @DisplayName("escapeHtml should be idempotent when applied twice")
  void escapeHtml_appliedTwice_shouldEscapeAgain() {
    // Given
    String input = "<script>";

    // When
    String firstEscape = HtmlUtils.escapeHtml(input);
    String secondEscape = HtmlUtils.escapeHtml(firstEscape);

    // Then
    assertThat(firstEscape).isEqualTo("&lt;script&gt;");
    assertThat(secondEscape).isEqualTo("&amp;lt;script&amp;gt;");
  }

  @Test
  @DisplayName("escapeHtml should handle Unicode characters")
  void escapeHtml_withUnicode_shouldPreserveUnicode() {
    // Given
    String unicode = "Hello 世界 🌍";

    // When
    String result = HtmlUtils.escapeHtml(unicode);

    // Then
    assertThat(result).isEqualTo("Hello 世界 🌍");
  }

  @Test
  @DisplayName("escapeHtml should handle mixed content")
  void escapeHtml_withMixedContent_shouldEscapeOnlySpecialChars() {
    // Given
    String mixed = "Normal text <b>bold</b> & special chars: \"quotes\" 'apostrophes'";

    // When
    String result = HtmlUtils.escapeHtml(mixed);

    // Then
    assertThat(result)
        .contains("Normal text")
        .contains("&lt;b&gt;")
        .contains("&amp;")
        .contains("&quot;quotes&quot;")
        .contains("&#x27;apostrophes&#x27;");
  }
}
