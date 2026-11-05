
## Transfer: Decision tables

**1st Condition: Is the client a student?**
- Action if True (Y): Follow the student transfer fee rules
- Action if False (N): Follow the non-student transfer fee rules

**2nd Condition: Is the transfer amount below the specified threshold?**
- Action if True (Y): Student amount is less than $200, or non-student amount is less than $100
- Action if False (N): Student amount is $200 or more, or non-student amount is $100 or more

**3rd Condition: Is the sending account balance below the specified threshold?**
- In this case, action if True:
  - Student: Sending balance is less than $2,000.
  - Non-student, amount less than $100: Sending balance is less than $4,000.
  - Non-student, amount $100 or more: Sending balance is less than $2,000.
- In this case, action if False:
  - The sending balance is greater than or equal to the applicable threshold.

**4th Condition: Is the receiving account balance below the specified threshold?**
- In this case, action if True:
  - Student: Receiving balance is less than $1,000.
  - Non-student, amount less than $100: Receiving balance is less than $2,000.
  - Non-student, amount $100 or more: Receiving balance is less than $1,000.
- In this case, action if False:
  - The receiving balance is greater than or equal to the applicable threshold.

### Decision Table Process and Assumptions

The decision tables were created using the transfer fee specifications provided in the assignment. Student and non-student transfers were separated because they have different thresholds. Each table contains 8 rules, giving a total of 16 rules. In this case, the test cases assume positive transfer amounts, sufficient sending balances, and that values equal to a threshold follow the greater-than-or-equal-to condition.

### Student table

| Conditions / Actions | R1 | R2 | R3 | R4 | R5 | R6 | R7 | R8 |
|---|---|---|---|---|---|---|---|---|
| Student? | Y | Y | Y | Y | Y | Y | Y | Y |
| Amount < $200? | Y | Y | Y | Y | N | N | N | N |
| From Balance < $2,000? | Y | Y | N | N | Y | N | Y | N |
| To Balance < $1,000? | Y | N | Y | N | Y | N | N | Y |
| **Actions** | | | | | | | | |
| Fee = 0.1% | X | | | | | | | |
| Fee = 0.05% | | X | X | | X | | | |
| Fee = 0.025% | | | | X | | | X | |
| Fee = 0.25% | | | | | | | | X |
| Fee = 0.125% | | | | | | X | | |

### Non-Student Table

| Conditions / Actions | R1 | R2 | R3 | R4 | R5 | R6 | R7 | R8 |
|---|---|---|---|---|---|---|---|---|
| Student? | N | N | N | N | N | N | N | N |
| Amount < $100? | N | N | N | N | Y | Y | Y | Y |
| From Balance below applicable threshold? | N | N | Y | Y | N | N | Y | Y |
| To Balance below applicable threshold? | N | Y | N | Y | N | Y | N | Y |
| **Actions** | | | | | | | | |
| Fee = 0.2% | | | | X | | | | X |
| Fee = 0.1% | | | X | | | | X | |
| Fee = 1% | | | | | | X | | |
| Fee = 0.5% | | X | | | X | | | |
| Fee = 0.25% | X | | | | | | | |

## Transfer: Test Cases

The expected transfer fee for each test case is calculated by multiplying the transfer amount by the applicable fee percentage divided by 100. The fee percentage is determined using the conditions from the decision tables

### Student Transfer Test Cases (TC1–TC8)

| Test Case | Student? | Amount ($) | From Balance ($) | To Balance ($) | Expected Fee | Expected Fee ($) |
|---|---|---|---|---|---|---|
| TC1 (R1) | Y | 148 | 1,675 | 642 | 0.1% | 0.148 |
| TC2 (R2) | Y | 148 | 1,329 | 1,236 | 0.05% | 0.074 |
| TC3 (R3) | Y | 148 | 2,780 | 873 | 0.05% | 0.074 |
| TC4 (R4) | Y | 148 | 3,455 | 1,450 | 0.025% | 0.037 |
| TC5 (R5) | Y | 401 | 1,812 | 725 | 0.05% | 0.2005 |
| TC6 (R6) | Y | 401 | 3,670 | 1,825 | 0.125% | 0.50125 |
| TC7 (R7) | Y | 401 | 1,543 | 1,375 | 0.025% | 0.10025 |
| TC8 (R8) | Y | 401 | 2,930 | 518 | 0.25% | 1.0025 |

### Non-Student Transfer Test Cases (TC9–TC16)

| Test Case | Student? | Amount ($) | From Balance ($) | To Balance ($) | Expected Fee | Expected Fee ($) |
|---|---|---|---|---|---|---|
| TC9 (R1) | N | 808 | 5,720 | 3,180 | 0.25% | 2.02 |
| TC10 (R2) | N | 808 | 4,650 | 725 | 0.5% | 4.04 |
| TC11 (R3) | N | 808 | 1,480 | 2,340 | 0.1% | 0.808 |
| TC12 (R4) | N | 808 | 1,625 | 685 | 0.2% | 1.616 |
| TC13 (R5) | N | 10 | 6,250 | 3,750 | 0.5% | 0.05 |
| TC14 (R6) | N | 10 | 4,875 | 1,450 | 1% | 0.10 |
| TC15 (R7) | N | 10 | 2,650 | 2,830 | 0.1% | 0.01 |
| TC16 (R8) | N | 10 | 1,725 | 920 | 0.2% | 0.02 |

## Transfer: Test Results

Results from running `bank.utils.FeesCalculatorTransferTest` (JUnit 5, `mvn -B test`); the same results appear in the CI run on the pull request. Fees are compared as `double` values with a tolerance of `1e-9`, so floating point rounding is not reported as a failure. "Actual fee" is the value returned by `FeesCalculator.calculateTransferFee`. S-R*n* and N-R*n* refer to rule *n* of the student and non-student decision tables.

Summary: 16 test cases, 0 passed, 16 failed.

| Test case | Student | Amount | From balance | To balance | Expected fee (rate) | Actual fee | Result |
|---|---|---|---|---|---|---|---|
| TC1 (S-R1) | Yes | 148 | 1675 | 642 | 0.148 (0.1%) | 3.7 | **Fail** |
| TC2 (S-R2) | Yes | 148 | 1329 | 1236 | 0.074 (0.05%) | 1.85 | **Fail** |
| TC3 (S-R3) | Yes | 148 | 2780 | 873 | 0.074 (0.05%) | 3.7 | **Fail** |
| TC4 (S-R4) | Yes | 148 | 3455 | 1450 | 0.037 (0.025%) | 1.85 | **Fail** |
| TC5 (S-R5) | Yes | 401 | 1812 | 725 | 0.2005 (0.05%) | 10.025 | **Fail** |
| TC6 (S-R6) | Yes | 401 | 3670 | 1825 | 0.50125 (0.125%) | 5.0125 | **Fail** |
| TC7 (S-R7) | Yes | 401 | 1543 | 1375 | 0.10025 (0.025%) | 5.0125 | **Fail** |
| TC8 (S-R8) | Yes | 401 | 2930 | 518 | 1.0025 (0.25%) | 10.025 | **Fail** |
| TC9 (N-R1) | No | 808 | 5720 | 3180 | 2.02 (0.25%) | 44.44 | **Fail** |
| TC10 (N-R2) | No | 808 | 4650 | 725 | 4.04 (0.5%) | 40.4 | **Fail** |
| TC11 (N-R3) | No | 808 | 1480 | 2340 | 0.808 (0.1%) | 44.44 | **Fail** |
| TC12 (N-R4) | No | 808 | 1625 | 685 | 1.616 (0.2%) | 40.4 | **Fail** |
| TC13 (N-R5) | No | 10 | 6250 | 3750 | 0.05 (0.5%) | 0.5 | **Fail** |
| TC14 (N-R6) | No | 10 | 4875 | 1450 | 0.10 (1%) | 0.5 | **Fail** |
| TC15 (N-R7) | No | 10 | 2650 | 2830 | 0.01 (0.1%) | 0.5 | **Fail** |
| TC16 (N-R8) | No | 10 | 1725 | 920 | 0.02 (0.2%) | 1.0 | **Fail** |

## Transfer: Fault Localization

Failing test cases that expose the same fault are grouped into one entry. A test case can be listed under more than one fault when it passes through several faulty conditions.

| Fault | Exposed by | Location | Suspected fault | Justification |
|---|---|---|---|---|
| T1 | TC1-TC4 | `bank.utils.FeesCalculator.calculateTransferFee`, student branch, `if (amount < 100.00)` | The student amount threshold is $100 instead of $200. | $148 is below $200, so the small-amount rates (0.1% / 0.05% / 0.025%) apply, but the code uses its large-amount branch and charges 2.5% / 1.25%. |
| T2 | TC1, TC2, TC5, TC7 | `calculateTransferFee`, student branch, `if (fromAccountBalance < 1000.00)` (both amount sub-branches) | The student source-balance threshold is $1,000 instead of $2,000. | Source balances of $1,329-$1,812 are below $2,000 but get the high-balance rates (TC5 is charged 2.5%, the rate for a source balance of $2,000 or more). |
| T3 | TC6, TC8 (contributes to TC1-TC8) | `calculateTransferFee`, student rate constants (`0.0125`, `0.025`, and the others in the branch) | Every student rate is 10× the specification (for example 0.0125 instead of 0.00125, 0.025 instead of 0.0025). | TC6 and TC8 land in the correct rule and still return exactly 10× the expected fee. |
| T4 | TC11, TC12 | `calculateTransferFee`, non-student `amount >= 100` branch, `if (fromAccountBalance < 1000.00)` | The source-balance threshold is $1,000 instead of $2,000. | Source balances of $1,480 and $1,625 are treated as high balances and charged 5.5% / 5% instead of 0.1% / 0.2%. |
| T5 | TC9, TC10 (contributes to TC11, TC12) | `calculateTransferFee`, non-student `amount >= 100` branch, `feePercentage = 0.05` and `feePercentage = 0.055` | The rates are 0.05 and 0.055 instead of 0.005 and 0.0025. | TC10 returns 10× the expected fee, and TC9 is charged 5.5%, higher than any rate in the specification. |
| T6 | TC14, TC15, TC16 | `calculateTransferFee`, non-student `amount < 100` branch, `if (fromAccountBalance < 1000.00)` and `if (toAccountBalance < 1000.00)` | The thresholds are $1,000 instead of $4,000 (source) and $2,000 (destination). | TC15/TC16 (source $2,650 / $1,725, below $4,000) and TC14 (destination $1,450, below $2,000) are routed to the wrong rule. |
| T7 | TC13 (contributes to TC14-TC16) | `calculateTransferFee`, non-student `amount < 100` branch, rates `0.1` and `0.05` | The rates are 10× the specification (1% and 0.5% are written as 0.1 and 0.05). | TC13 lands in the correct rule and returns 0.5, exactly 10× the expected 0.05. |
