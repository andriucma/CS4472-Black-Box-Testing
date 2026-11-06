package bank.utils;

import java.util.Calendar;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;


class FeesCalculatorWithdrawalTest {

    private static final double DELTA = 1e-9;

    private static final int[] AMOUNTS = {-1, 0, 1, 500, 999, 1000, 1001};
    private static final double[] BALANCES = {-1, 0, 1, 2500, 4999, 5000, 5001};

    private final FeesCalculator calc = new FeesCalculator();

    private static double specFee(double amount, double balance, boolean student, int dayOfWeek) {
        double pct;
        if (student) {
            boolean weekend = (dayOfWeek == Calendar.SATURDAY) || (dayOfWeek == Calendar.SUNDAY);
            pct = weekend ? 0.001 : 0.0;
        } else {
            if (balance < 1000.0) {
                pct = 0.003;
            } else if (balance < 5000.0) {
                pct = 0.001;
            } else {
                pct = 0.0;
            }
        }
        return amount * pct;
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("withdrawalCases")
    void withdrawalFee(String id, int amount, double balance, boolean student, int dayOfWeek) {
        double expected = specFee(amount, balance, student, dayOfWeek);
        double actual = calc.calculateWithdrawalFee(amount, balance, student, dayOfWeek);
        assertEquals(expected, actual, DELTA);
    }
}
