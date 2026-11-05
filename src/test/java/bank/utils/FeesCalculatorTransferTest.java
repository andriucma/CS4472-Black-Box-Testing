package bank.utils;

import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;

/**
 * Transfer fee tests for FeesCalculator.calculateTransferFee,
 * derived with Decision Table Analysis (docs/assignment1.md, "Transfer: Decision tables").
 *
 * TC1-TC8 trace to rules R1-R8 of the student table,
 * TC9-TC16 trace to rules R1-R8 of the non-student table.
 *
 * Columns: test case, amount, from balance, to balance, student, expected fee.
 */
class FeesCalculatorTransferTest {
	private static final double DELTA = 1e-9;
	private final FeesCalculator calculator = new FeesCalculator();

	@ParameterizedTest(name = "{0}: amount={1}, from={2}, to={3}, student={4} -> fee={5}")
	@CsvSource({
		// Student table (spec: student, amount < $200 / >= $200, from < $2,000, to < $1,000)
		"TC1, 148, 1675, 642, true, 0.148",
		"TC2, 148, 1329, 1236, true, 0.074",
		"TC3, 148, 2780, 873, true, 0.074",
		"TC4, 148, 3455, 1450, true, 0.037",
		"TC5, 401, 1812, 725, true, 0.2005",
		"TC6, 401, 3670, 1825, true, 0.50125",
		"TC7, 401, 1543, 1375, true, 0.10025",
		"TC8, 401, 2930, 518, true, 1.0025",
		// Non-student table (spec: amount >= $100: from < $2,000, to < $1,000;
		// amount < $100: from < $4,000, to < $2,000)
		"TC9, 808, 5720, 3180, false, 2.02",
		"TC10, 808, 4650, 725, false, 4.04",
		"TC11, 808, 1480, 2340, false, 0.808",
		"TC12, 808, 1625, 685, false, 1.616",
		"TC13, 10, 6250, 3750, false, 0.05",
		"TC14, 10, 4875, 1450, false, 0.10",
		"TC15, 10, 2650, 2830, false, 0.01",
		"TC16, 10, 1725, 920, false, 0.02",
	})
	void transferFee(String id, double amount, double fromBalance, double toBalance, boolean student,
			double expectedFee) {
		double actualFee = calculator.calculateTransferFee(amount, fromBalance, toBalance, student);
		Assertions.assertEquals(expectedFee, actualFee, DELTA, id);
	}
}
