package atm.session;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import atm.exceptions.InvalidPinFormatException;
import atm.utils.CashValidator;
import atm.utils.FormatChecker;

class SessionTest {

    private final FormatChecker formatChecker = new FormatChecker();

    // ----- Variable 1: PIN format (amount held at nominal = $500) -----

    @Test
    @DisplayName("P1 min-: 4-digit PIN '1234' -> InvalidPinFormatException")
    void pinFourDigits() {
        assertThrows(InvalidPinFormatException.class,
                () -> formatChecker.checkPinFormat("1234".toCharArray()));
    }

    @Test
    @DisplayName("P2 min: 5-digit PIN '00000' -> accepted")
    void pinMin() {
        assertDoesNotThrow(() -> formatChecker.checkPinFormat("00000".toCharArray()));
    }

    @Test
    @DisplayName("P3 min+: 5-digit PIN '00001' -> accepted")
    void pinMinPlus() {
        assertDoesNotThrow(() -> formatChecker.checkPinFormat("00001".toCharArray()));
    }

    @Test
    @DisplayName("P4 nom: 5-digit PIN '12345' -> accepted")
    void pinNominal() {
        assertDoesNotThrow(() -> formatChecker.checkPinFormat("12345".toCharArray()));
    }

    @Test
    @DisplayName("P5 max-: 5-digit PIN '99998' -> accepted")
    void pinMaxMinus() {
        assertDoesNotThrow(() -> formatChecker.checkPinFormat("99998".toCharArray()));
    }

    @Test
    @DisplayName("P6 max: 5-digit PIN '99999' -> accepted")
    void pinMax() {
        assertDoesNotThrow(() -> formatChecker.checkPinFormat("99999".toCharArray()));
    }

    @Test
    @DisplayName("P7 max+: 6-digit PIN '100000' -> InvalidPinFormatException")
    void pinSixDigits() {
        assertThrows(InvalidPinFormatException.class,
                () -> formatChecker.checkPinFormat("100000".toCharArray()));
    }

    @Test
    @DisplayName("A1 min-: $10 (below $20 min) -> rejected")
    void amountBelowMin() {
        assertFalse(CashValidator.validateWithdrawal(10));
    }

    @Test
    @DisplayName("A2 min: $20 -> accepted")
    void amountMin() {
        assertTrue(CashValidator.validateWithdrawal(20));
    }

    @Test
    @DisplayName("A3 min+: $40 (next dispensable; $30 is not) -> accepted")
    void amountMinPlus() {
        assertTrue(CashValidator.validateWithdrawal(40));
    }

    @Test
    @DisplayName("A4 nom: $500 -> accepted")
    void amountNominal() {
        assertTrue(CashValidator.validateWithdrawal(500));
    }

    @Test
    @DisplayName("A5 max-: $980 (one $20 below limit; 20x49) -> accepted")
    void amountMaxMinus() {
        assertTrue(CashValidator.validateWithdrawal(980));
    }

    @Test
    @DisplayName("A6 max: $1000 (daily limit) -> accepted")
    void amountMax() {
        assertTrue(CashValidator.validateWithdrawal(1000));
    }

    @Test
    @DisplayName("A7 max+: $1020 (exceeds $1000 daily limit) -> rejected")
    void amountAboveMax() {
        assertFalse(CashValidator.validateWithdrawal(1020));
    }
}
