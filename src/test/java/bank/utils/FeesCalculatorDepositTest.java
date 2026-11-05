package bank.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Deposit fee tests for FeesCalculator.calculateDepositInterest,
 * derived with Weak Robust Equivalence Class Analysis
 * (docs/assignment1.md, "Deposit: Equivalence Classes").
 *
 * DV01-DV08 cover every valid equivalence class (one test per
 * student / amount / balance class combination in the deposit specification).
 * DI01-DI04 each contain exactly one invalid class (non-positive amount or
 * negative balance) while every other input stays in a valid class.
 *
 * Columns: test case, amount, balance, student, expected fee.
 */
class FeesCalculatorDepositTest {
	private static final double DELTA = 1e-9;
	private final FeesCalculator calculator = new FeesCalculator();

	@ParameterizedTest(name = "{0}: amount={1}, balance={2}, student={3} -> fee={4}")
	@CsvSource({
		// Student, amount <= $50: balance > $5,000 -> 0.5%, otherwise no fee
		"DV01, 25, 6000, true, 0.125",
		"DV02, 25, 2000, true, 0",
		// Student, amount > $50: balance > $500 -> 0.5%, otherwise 0.25%
		"DV03, 200, 2500, true, 1.0",
		"DV04, 200, 250, true, 0.5",
		// Non-student, amount > $250: balance > $2,500 -> 0.8%, otherwise 0.4%
		"DV05, 1000, 7500, false, 8.0",
		"DV06, 1000, 1000, false, 4.0",
		// Non-student, amount <= $250: balance > $10,000 -> no fee, otherwise 0.1%
		"DV07, 100, 20000, false, 0",
		"DV08, 100, 5000, false, 0.1",
		// Invalid classes (one invalid input per test)
		"DI01, -100, 2500, true, 0",
		"DI02, -100, 5000, false, 0",
		"DI03, 25, -500, true, 0",
		"DI04, 1000, -500, false, 4.0",
	})
	void depositFee(String id, double amount, double balance, boolean student, double expectedFee) {
		double actualFee = calculator.calculateDepositInterest(amount, balance, student);
		Assertions.assertEquals(expectedFee, actualFee, DELTA, id);
	}
}
