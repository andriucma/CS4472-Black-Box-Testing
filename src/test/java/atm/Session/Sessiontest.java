package atm.session;

import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.junit.jupiter.api.Assertions.assertThrows;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.mockito.Mockito.mock;

import atm.dispatcher.MessageDispatcher;
import atm.exceptions.InvalidAmountException;
import atm.exceptions.InvalidPinFormatException;
import atm.session.transactions.ATMWithdrawal;
import atm.ui.panels.MainPanel;
import atm.utils.FormatChecker;

/**
 * ATM Session black-box tests: Robust (single-fault) Boundary Value Analysis
 * for the two session inputs - PIN format and withdrawal amount.
 *
 * Account under test (per the handout): daily limit $1,000, available balance
 * $5,000, so $1,000 is the amount's upper boundary. The amount tests drive the
 * real Session object (Session.setAmount) with a withdrawal transaction; the
 * UI panel and bank dispatcher are mocked so the test exercises only the
 * input validation. PIN format is validated by FormatChecker.checkPinFormat
 * (reached in the session flow via ATM.checkPin), since Session.addPin does not
 * validate format itself.
 *
 * Expected results are taken from the SPECIFICATION:
 *   - a valid PIN is exactly 5 digits;
 *   - a valid withdrawal amount is dispensable in $20/$50 bills and within the
 *     $1,000 daily limit.
 * Tests are expected to fail where the code contains a planted fault.
 */
class SessionTest {

    private final FormatChecker formatChecker = new FormatChecker();

    /** A Session set up for a withdrawal transaction, with UI/bank mocked. */
    private Session withdrawalSession() {
        Session session = new Session(mock(MainPanel.class), mock(MessageDispatcher.class));
        session.setTransaction(new ATMWithdrawal());
        return session;
    }

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

    // ----- Variable 2: Withdrawal amount via Session.setAmount (PIN nominal) -----
    // A valid amount is accepted (no exception); an invalid amount throws
    // InvalidAmountException. Expected outcomes come from the specification.

    @Test
    @DisplayName("A1 min-: $10 (below $20 min) -> InvalidAmountException")
    void amountBelowMin() {
        assertThrows(InvalidAmountException.class, () -> withdrawalSession().setAmount(10));
    }

    @Test
    @DisplayName("A2 min: $20 -> accepted")
    void amountMin() {
        assertDoesNotThrow(() -> withdrawalSession().setAmount(20));
    }

    @Test
    @DisplayName("A3 min+: $40 (next dispensable; $30 is not) -> accepted")
    void amountMinPlus() {
        assertDoesNotThrow(() -> withdrawalSession().setAmount(40));
    }

    @Test
    @DisplayName("A4 nom: $500 -> accepted")
    void amountNominal() {
        assertDoesNotThrow(() -> withdrawalSession().setAmount(500));
    }

    @Test
    @DisplayName("A5 max-: $980 (one $20 below limit; 20x49) -> accepted")
    void amountMaxMinus() {
        assertDoesNotThrow(() -> withdrawalSession().setAmount(980));
    }

    @Test
    @DisplayName("A6 max: $1000 (daily limit) -> accepted")
    void amountMax() {
        assertDoesNotThrow(() -> withdrawalSession().setAmount(1000));
    }

    @Test
    @DisplayName("A7 max+: $1020 (exceeds $1000 daily limit) -> InvalidAmountException")
    void amountAboveMax() {
        assertThrows(InvalidAmountException.class, () -> withdrawalSession().setAmount(1020));
    }
}