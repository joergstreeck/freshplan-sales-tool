package de.freshplan.domain.calculator.service;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.within;

import de.freshplan.domain.calculator.service.dto.CalculatorRequest;
import de.freshplan.domain.calculator.service.dto.CalculatorResponse;
import io.quarkus.test.junit.QuarkusTest;
import io.quarkus.test.security.TestSecurity;
import jakarta.inject.Inject;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/** Tests for CalculatorService based on FreshPlan business rules. */
@QuarkusTest
@TestSecurity(
    user = "testuser",
    roles = {"admin", "manager", "sales", "viewer"})
class CalculatorServiceTest {

  @Inject CalculatorService calculatorService;

  @Test
  void testCalculateWithNoDiscounts() {
    // Bestellwert unter 5.000 EUR, keine Rabatte
    CalculatorRequest request = new CalculatorRequest(4999.0, 5, false, false);

    CalculatorResponse response = calculatorService.calculate(request);

    assertThat(response.getBaseDiscount()).isEqualTo(0);
    assertThat(response.getEarlyDiscount()).isEqualTo(0);
    assertThat(response.getPickupDiscount()).isEqualTo(0);
    assertThat(response.getTotalDiscount()).isEqualTo(0);
    assertThat(response.getFinalPrice()).isEqualTo(4999.0);
  }

  @ParameterizedTest
  @CsvSource({
    "4999, 0", // unter 5.000 EUR: 0%
    "5000, 3", // 5.000 - 14.999 EUR: 3%
    "14999, 3", // 5.000 - 14.999 EUR: 3%
    "15000, 6", // 15.000 - 29.999 EUR: 6%
    "29999, 6", // 15.000 - 29.999 EUR: 6%
    "30000, 8", // 30.000 - 49.999 EUR: 8%
    "49999, 8", // 30.000 - 49.999 EUR: 8%
    "50000, 9", // 50.000 - 74.999 EUR: 9%
    "74999, 9", // 50.000 - 74.999 EUR: 9%
    "75000, 10", // ab 75.000 EUR: 10%
    "100000, 10" // ab 75.000 EUR: 10%
  })
  void testBaseDiscountCalculation(double orderValue, double expectedDiscount) {
    CalculatorRequest request = new CalculatorRequest(orderValue, 5, false, false);

    CalculatorResponse response = calculatorService.calculate(request);

    assertThat(response.getBaseDiscount()).isEqualTo(expectedDiscount);
  }

  @ParameterizedTest
  @CsvSource({
    "9, 0", // unter 10 Tage: 0%
    "10, 1", // 10 - 14 Tage: +1%
    "14, 1", // 10 - 14 Tage: +1%
    "15, 2", // 15 - 29 Tage: +2%
    "29, 2", // 15 - 29 Tage: +2%
    "30, 3", // ab 30 Tage: +3%
    "44, 3", // ab 30 Tage: +3% (nach oben offen!)
    "60, 3", // ab 30 Tage: +3%
    "90, 3" // ab 30 Tage: +3%
  })
  void testEarlyBookingDiscountCalculation(int leadTime, double expectedDiscount) {
    CalculatorRequest request = new CalculatorRequest(10000.0, leadTime, false, false);

    CalculatorResponse response = calculatorService.calculate(request);

    assertThat(response.getEarlyDiscount()).isEqualTo(expectedDiscount);
  }

  @ParameterizedTest
  @CsvSource({
    "4999, false, 0", // unter 5.000 EUR: kein Abholrabatt
    "4999, true, 0", // unter 5.000 EUR: kein Abholrabatt auch bei Abholung
    "5000, false, 0", // ab 5.000 EUR aber keine Abholung: 0%
    "5000, true, 2", // ab 5.000 EUR mit Abholung: +2%
    "10000, true, 2" // ab 5.000 EUR mit Abholung: +2%
  })
  void testPickupDiscountCalculation(double orderValue, boolean pickup, double expectedDiscount) {
    CalculatorRequest request = new CalculatorRequest(orderValue, 5, pickup, false);

    CalculatorResponse response = calculatorService.calculate(request);

    assertThat(response.getPickupDiscount()).isEqualTo(expectedDiscount);
  }

  @Test
  void testCombinedDiscounts() {
    // Beispiel aus der Dokumentation:
    // Großcaterer mit Lieferung
    // Bestellwert: 32.000 EUR netto
    // Vorlaufzeit: 16 Werktage
    // Basisrabatt: 8%
    // Frühbucherrabatt: +2%
    // Gesamtrabatt: 10%
    CalculatorRequest request = new CalculatorRequest(32000.0, 16, false, false);

    CalculatorResponse response = calculatorService.calculate(request);

    assertThat(response.getBaseDiscount()).isEqualTo(8);
    assertThat(response.getEarlyDiscount()).isEqualTo(2);
    assertThat(response.getPickupDiscount()).isEqualTo(0);
    assertThat(response.getTotalDiscount()).isEqualTo(10);
    assertThat(response.getDiscountAmount()).isEqualTo(3200.0);
    assertThat(response.getFinalPrice()).isEqualTo(28800.0);
  }

  @Test
  void testMaximumDiscountCap() {
    // Maximaler Gesamtrabatt: 15%
    // Bestellwert: 75.000 EUR (10% Basis)
    // Vorlaufzeit: 30 Tage (+3% Frühbucher)
    // Abholung: ja (+2%)
    // Summe: 10% + 3% + 2% = 15%
    CalculatorRequest request = new CalculatorRequest(75000.0, 30, true, false);

    CalculatorResponse response = calculatorService.calculate(request);

    assertThat(response.getBaseDiscount()).isEqualTo(10);
    assertThat(response.getEarlyDiscount()).isEqualTo(3);
    assertThat(response.getPickupDiscount()).isEqualTo(2);
    assertThat(response.getTotalDiscount()).isEqualTo(15);
  }

  @Test
  void testDiscountCapExceeded() {
    // Test dass Rabatte über 15% auf 15% begrenzt werden
    // Dies könnte in Zukunft mit Kettenrabatt relevant werden
    CalculatorRequest request = new CalculatorRequest(100000.0, 60, true, true);

    CalculatorResponse response = calculatorService.calculate(request);

    // Auch wenn theoretisch mehr möglich wäre, maximal 15%
    assertThat(response.getTotalDiscount()).isEqualTo(15);
    assertThat(response.getDiscountAmount()).isEqualTo(15000.0);
    assertThat(response.getFinalPrice()).isEqualTo(85000.0);
  }

  @Test
  void testPrecisionAndRounding() {
    // Test mit krummen Beträgen
    CalculatorRequest request = new CalculatorRequest(12345.67, 15, true, false);

    CalculatorResponse response = calculatorService.calculate(request);

    assertThat(response.getBaseDiscount()).isEqualTo(3); // 5.000-14.999: 3%
    assertThat(response.getEarlyDiscount()).isEqualTo(2); // 15-29 Tage: 2%
    assertThat(response.getPickupDiscount()).isEqualTo(2); // Abholung: 2%
    assertThat(response.getTotalDiscount()).isEqualTo(7);

    // 12345.67 * 0.07 = 864.1969
    assertThat(response.getDiscountAmount()).isCloseTo(864.1969, within(0.01));
    assertThat(response.getFinalPrice()).isCloseTo(11481.4731, within(0.01));
  }
}
