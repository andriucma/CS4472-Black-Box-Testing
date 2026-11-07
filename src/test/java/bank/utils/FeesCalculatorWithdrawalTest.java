package bank.utils;

import java.util.ArrayList;
import java.util.Calendar;
import java.util.List;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.assertEquals;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;

class FeesCalculatorWithdrawalTest {

    private static final double DELTA = 1e-9;

    private static final int[] AMOUNTS = {-1, 0, 1, 500, 999, 1000, 1001};
    private static final double[] BALANCES = {-1, 0, 1, 999, 1000, 1001, 2500, 4999, 5000, 5001};

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

    static Stream<Arguments> withdrawalCases() {
        List<Arguments> cases = new ArrayList<>();
        // Non-student worst-case grid: amount x balance (dayOfWeek is inert for non-students).
        for (int amount : AMOUNTS) {
            for (double balance : BALANCES) {
                String id = String.format("NS amount=%d balance=%.0f", amount, balance);
                cases.add(Arguments.of(id, amount, balance, false, Calendar.WEDNESDAY));
            }
        }
        int[] days = {0, Calendar.SUNDAY, Calendar.MONDAY, Calendar.WEDNESDAY,
            Calendar.FRIDAY, Calendar.SATURDAY, 8};
        for (int amount : AMOUNTS) {
            for (int day : days) {
                boolean weekend = (day == Calendar.SATURDAY) || (day == Calendar.SUNDAY);
                String label = weekend ? "weekend" : "weekday";
                String id = String.format("ST amount=%d day=%d(%s)", amount, day, label);
                cases.add(Arguments.of(id, amount, 2500.0, true, day));
            }
        }
        return cases.stream();
    }

    @ParameterizedTest(name = "{0}")
    @MethodSource("withdrawalCases")
    void withdrawalFee(String id, int amount, double balance, boolean student, int dayOfWeek) {
        double expected = specFee(amount, balance, student, dayOfWeek);
        double actual = calc.calculateWithdrawalFee(amount, balance, student, dayOfWeek);
        assertEquals(expected, actual, DELTA);
    }
}
