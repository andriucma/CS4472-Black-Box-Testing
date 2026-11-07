# Assignment 1: Black Box Testing

CS4472A, Group 10. System under test: the ATM console application, specifically `atm.session.Session` (PIN format and withdrawal amount) and `bank.utils.FeesCalculator` (withdrawal, deposit and transfer fees).

| Testing target | Technique | JUnit class | Tests | Passed | Failed |
|---|---|---|---|---|---|
| `Session`: PIN format and withdrawal amount | Robust Worst Case BVA, single fault assumption | `atm.session.SessionTest` | 14 | 6 | 8 |
| `FeesCalculator`: withdrawal | Robust Worst Case BVA | `bank.utils.FeesCalculatorWithdrawalTest` | 119 | 41 | 78 |
| `FeesCalculator`: deposit | Weak Robust Equivalence Class Analysis | `bank.utils.FeesCalculatorDepositTest` | 12 | 2 | 10 |
| `FeesCalculator`: transfer | Decision Table Analysis | `bank.utils.FeesCalculatorTransferTest` | 16 | 0 | 16 |
| **Total** | | | **161** | **49** | **112** |

The starter tests (`atm.ATMTest`, `bank.utils.FeesCalculatorTest`, 3 tests, all passing) are not counted above, so the full CI run reports 164 tests: 52 passed, 112 failed.

The document has four parts. Section 1 describes the process and assumptions behind each technique. Section 2 lists the test cases. Section 3 gives the result of each test case. Section 4 localizes the fault behind every failing test case.

---

## Section 1: Process and Assumptions

### 1.0 General assumptions

- The specification in the assignment handout is the oracle. Expected values come only from the specification, never from the code.
- Fees are compared as `double` values with a tolerance of `1e-9`, so floating point rounding is not reported as a failure.
- `FeesCalculator` receives the day of the week as a `java.util.Calendar` constant (`SUNDAY = 1`, `MONDAY = 2`, ..., `SATURDAY = 7`). Weekdays are Monday to Friday.
- Collaborators that are not under test (the UI `MainPanel` and the bank behind `MessageDispatcher`) are replaced with Mockito test doubles, so no database or GUI is needed.

### 1.1 ATM Session: Robust Worst Case Boundary Value Analysis (single fault assumption)

**Inputs.** For a withdrawal (transaction choice 1) the session takes two inputs: the PIN and the withdrawal amount.

**Account under test.** As required, the account has a daily limit of $1,000 and an available balance of $5,000. The daily limit is therefore the upper boundary of the amount. The balance is never the binding limit, because no valid withdrawal can exceed $1,000.

**Single fault assumption.** A failure is assumed to be caused by one fault at a time, so each test lets only one variable take a non-nominal value while the other stays at its nominal value (PIN `12345`, amount $500). The 7 × 7 robust worst-case cross product of the two variables is therefore reduced to the 7 robust values of each variable, giving 14 test cases (P1-P7, A1-A7).

**PIN.** A valid PIN is exactly 5 digits. The PIN is treated as a 5-digit number from `00000` to `99999`:
- min = `00000`, min+ = `00001`, nom = `12345`, max- = `99998`, max = `99999`.
- max+ = `100000`, which is 99999 + 1 and has 6 digits.
- min- = a 4-digit PIN (`1234`), the closest entry below the 5-digit range.
- Expected outcome: a valid PIN is accepted; an invalid PIN throws `InvalidPinFormatException`.
- `Session.addPin` does not check the format itself. In the session flow the format is checked by `FormatChecker.checkPinFormat`, which `ATM.checkPin` calls before handing the PIN to the session, so the PIN tests call `FormatChecker.checkPinFormat` directly.

**Withdrawal amount.** A valid amount can be dispensed in $20 and $50 bills and is at most the $1,000 daily limit. The smallest amount that can be dispensed is $20, so the valid range is [20, 1000]:
- min- = $10 (below the smallest bill), min = $20, min+ = $40 (the next dispensable amount; $30 cannot be dispensed), nom = $500.
- max- = $980 (one $20 bill below the limit), max = $1,000, max+ = $1,020 (one $20 bill above the limit).
- Expected outcome: a valid amount is accepted with no exception; an invalid amount throws `InvalidAmountException`.
- The amount tests call `Session.setAmount` on a real `Session` that holds a withdrawal transaction, with the UI and bank mocked. The amount is validated inside `setAmount`, before anything is sent to the bank.

### 1.2 Withdrawal fees: Robust Worst Case Boundary Value Analysis

Inputs: amount, account balance, student (boolean) and day of the week.

- **Amount** has no decision boundary in the fee rules; it only multiplies the rate. Its domain is taken as [0, 1000] (the daily limit). Robust values: -1 (min-), 0 (min), 1 (min+), 500 (nom), 999 (max-), 1000 (max), 1001 (max+). `FeesCalculator` does not validate amounts (the session does), so every amount, including the out-of-range ones, is expected to be charged the same percentage.
- **Balance** only matters for non-students, with boundaries at $1,000 and $5,000. Values: -1 (min-), 0 (min), 1 (min+), 999 / 1000 / 1001 (around $1,000), 2500 (nominal), 4999 / 5000 / 5001 (around $5,000).
- **Day of the week** only matters for students. The domain is the `Calendar` range [1, 7]. Robust values: 0 (min-, invalid), 1 = Sunday (min), 2 = Monday (min+), 4 = Wednesday (nom), 6 = Friday (max-), 7 = Saturday (max), 8 (max+, invalid). The invalid day values 0 and 8 are not Saturday or Sunday, so they are assumed to follow the weekday rule (no fee for students).
- **Student** is a boolean with no boundaries, so it splits the test set instead of being a BVA variable.

Worst case means the Cartesian product of the variables that matter in each part:
- Non-students: amount (7) × balance (10) = **70 test cases** (WN01-WN70). The day is held at its nominal value (Wednesday) because it does not affect non-student fees.
- Students: amount (7) × day (7) = **49 test cases** (WS01-WS49). The balance is held at its nominal value ($2,500) because it does not affect student fees.

Expected fee = amount × rate from the specification. Students: 0% on weekdays, 0.1% on weekends. Non-students: 0.3% if balance < $1,000, 0.1% if $1,000 ≤ balance < $5,000, 0% if balance ≥ $5,000. The JUnit class generates the cases in this order, so WN*n* is the *n*-th `NS amount=… balance=…` case and WS*n* is the *n*-th `ST amount=… day=…` case.

### 1.3 Deposit fees: Weak Robust Equivalence Class Analysis

The deposit test cases were derived with Weak Robust Equivalence Class Analysis from the deposit fee specification. The inputs are the client type (student or not), the amount deposited, and the account balance. The amount and balance classes depend on the client type, and the balance classes also depend on the amount class, so each class only exists inside its branch of the specification.

- **Weak (valid part):** every valid class appears in at least one test. Because the balance classes are conditional on the amount class, covering every valid class takes one test per (client type, amount, balance) branch, which gives 8 valid test cases (DV01-DV08). Each uses a representative value from the middle of its class rather than a boundary value.
- **Robust (invalid part):** each invalid class gets its own test in which every other input stays valid (DI01-DI04): a non-positive amount and a negative balance, once for a student and once for a non-student.
- The deposit fee is implemented as `FeesCalculator.calculateDepositInterest`. `BankDeposit` subtracts its result from the new balance, so its return value is treated as the fee in the specification.
- Depositing a non-positive amount moves no money, so we assume the fee must be 0 and never negative.
- An account balance cannot normally be negative in this system, but the specification's "otherwise" rules still cover it. A negative balance is therefore expected to get the rate of the low-balance class.
- Fees are compared as `double` values with a tolerance of `1e-9`.

#### Equivalence Classes

| Variable | Valid classes | Invalid classes |
|---|---|---|
| Client type | S1 = student, S2 = non-student | none (boolean) |
| Amount (student) | SA1 = 0 < amount ≤ $50, SA2 = amount > $50 | AI = amount ≤ 0 |
| Amount (non-student) | NA1 = 0 < amount ≤ $250, NA2 = amount > $250 | AI = amount ≤ 0 |
| Balance (student, amount > $50) | SB1 = balance ≤ $500, SB2 = balance > $500 | BI = balance < 0 |
| Balance (student, amount ≤ $50) | SB3 = balance ≤ $5,000, SB4 = balance > $5,000 | BI = balance < 0 |
| Balance (non-student, amount > $250) | NB1 = balance ≤ $2,500, NB2 = balance > $2,500 | BI = balance < 0 |
| Balance (non-student, amount ≤ $250) | NB3 = balance ≤ $10,000, NB4 = balance > $10,000 | BI = balance < 0 |

### 1.4 Transfer fees: Decision Table Analysis

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

#### Decision Table Process and Assumptions

The decision tables were created using the transfer fee specifications provided in the assignment. Student and non-student transfers were separated because they have different thresholds. Each table contains 8 rules, giving a total of 16 rules. In this case, the test cases assume positive transfer amounts, sufficient sending balances, and that values equal to a threshold follow the greater-than-or-equal-to condition.

#### Student table

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

#### Non-Student Table

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

---

## Section 2: Test Cases

### 2.1 ATM Session (`SessionTest`)

#### PIN format (amount held at nominal $500)

| Test case | PIN | BVA value | Expected outcome |
|---|---|---|---|
| P1 | `1234` | min- (4 digits) | `InvalidPinFormatException` |
| P2 | `00000` | min | accepted |
| P3 | `00001` | min+ | accepted |
| P4 | `12345` | nominal | accepted |
| P5 | `99998` | max- | accepted |
| P6 | `99999` | max | accepted |
| P7 | `100000` | max+ (6 digits) | `InvalidPinFormatException` |

#### Withdrawal amount (PIN held at nominal; account with $1,000 daily limit and $5,000 balance)

| Test case | Amount ($) | BVA value | Expected outcome |
|---|---|---|---|
| A1 | 10 | min- | `InvalidAmountException` |
| A2 | 20 | min | accepted |
| A3 | 40 | min+ | accepted |
| A4 | 500 | nominal | accepted |
| A5 | 980 | max- | accepted (49 × $20, or 18 × $50 + 4 × $20) |
| A6 | 1000 | max (daily limit) | accepted |
| A7 | 1020 | max+ | `InvalidAmountException` (over the daily limit) |

### 2.2 Withdrawal fees (`FeesCalculatorWithdrawalTest`)

#### Non-students (day held at nominal Wednesday)

| Test case | Student? | Amount ($) | Amount value | Balance ($) | Balance value | Day | Expected fee | Expected fee ($) |
|---|---|---|---|---|---|---|---|---|
| WN01 | N | -1 | min- | -1 | min- | Wednesday (nom) | 0.3% | -0.003 |
| WN02 | N | -1 | min- | 0 | min | Wednesday (nom) | 0.3% | -0.003 |
| WN03 | N | -1 | min- | 1 | min+ | Wednesday (nom) | 0.3% | -0.003 |
| WN04 | N | -1 | min- | 999 | 1000- | Wednesday (nom) | 0.3% | -0.003 |
| WN05 | N | -1 | min- | 1000 | 1000 | Wednesday (nom) | 0.1% | -0.001 |
| WN06 | N | -1 | min- | 1001 | 1000+ | Wednesday (nom) | 0.1% | -0.001 |
| WN07 | N | -1 | min- | 2500 | nom | Wednesday (nom) | 0.1% | -0.001 |
| WN08 | N | -1 | min- | 4999 | 5000- | Wednesday (nom) | 0.1% | -0.001 |
| WN09 | N | -1 | min- | 5000 | 5000 | Wednesday (nom) | 0% | 0 |
| WN10 | N | -1 | min- | 5001 | 5000+ | Wednesday (nom) | 0% | 0 |
| WN11 | N | 0 | min | -1 | min- | Wednesday (nom) | 0.3% | 0 |
| WN12 | N | 0 | min | 0 | min | Wednesday (nom) | 0.3% | 0 |
| WN13 | N | 0 | min | 1 | min+ | Wednesday (nom) | 0.3% | 0 |
| WN14 | N | 0 | min | 999 | 1000- | Wednesday (nom) | 0.3% | 0 |
| WN15 | N | 0 | min | 1000 | 1000 | Wednesday (nom) | 0.1% | 0 |
| WN16 | N | 0 | min | 1001 | 1000+ | Wednesday (nom) | 0.1% | 0 |
| WN17 | N | 0 | min | 2500 | nom | Wednesday (nom) | 0.1% | 0 |
| WN18 | N | 0 | min | 4999 | 5000- | Wednesday (nom) | 0.1% | 0 |
| WN19 | N | 0 | min | 5000 | 5000 | Wednesday (nom) | 0% | 0 |
| WN20 | N | 0 | min | 5001 | 5000+ | Wednesday (nom) | 0% | 0 |
| WN21 | N | 1 | min+ | -1 | min- | Wednesday (nom) | 0.3% | 0.003 |
| WN22 | N | 1 | min+ | 0 | min | Wednesday (nom) | 0.3% | 0.003 |
| WN23 | N | 1 | min+ | 1 | min+ | Wednesday (nom) | 0.3% | 0.003 |
| WN24 | N | 1 | min+ | 999 | 1000- | Wednesday (nom) | 0.3% | 0.003 |
| WN25 | N | 1 | min+ | 1000 | 1000 | Wednesday (nom) | 0.1% | 0.001 |
| WN26 | N | 1 | min+ | 1001 | 1000+ | Wednesday (nom) | 0.1% | 0.001 |
| WN27 | N | 1 | min+ | 2500 | nom | Wednesday (nom) | 0.1% | 0.001 |
| WN28 | N | 1 | min+ | 4999 | 5000- | Wednesday (nom) | 0.1% | 0.001 |
| WN29 | N | 1 | min+ | 5000 | 5000 | Wednesday (nom) | 0% | 0 |
| WN30 | N | 1 | min+ | 5001 | 5000+ | Wednesday (nom) | 0% | 0 |
| WN31 | N | 500 | nom | -1 | min- | Wednesday (nom) | 0.3% | 1.5 |
| WN32 | N | 500 | nom | 0 | min | Wednesday (nom) | 0.3% | 1.5 |
| WN33 | N | 500 | nom | 1 | min+ | Wednesday (nom) | 0.3% | 1.5 |
| WN34 | N | 500 | nom | 999 | 1000- | Wednesday (nom) | 0.3% | 1.5 |
| WN35 | N | 500 | nom | 1000 | 1000 | Wednesday (nom) | 0.1% | 0.5 |
| WN36 | N | 500 | nom | 1001 | 1000+ | Wednesday (nom) | 0.1% | 0.5 |
| WN37 | N | 500 | nom | 2500 | nom | Wednesday (nom) | 0.1% | 0.5 |
| WN38 | N | 500 | nom | 4999 | 5000- | Wednesday (nom) | 0.1% | 0.5 |
| WN39 | N | 500 | nom | 5000 | 5000 | Wednesday (nom) | 0% | 0 |
| WN40 | N | 500 | nom | 5001 | 5000+ | Wednesday (nom) | 0% | 0 |
| WN41 | N | 999 | max- | -1 | min- | Wednesday (nom) | 0.3% | 2.997 |
| WN42 | N | 999 | max- | 0 | min | Wednesday (nom) | 0.3% | 2.997 |
| WN43 | N | 999 | max- | 1 | min+ | Wednesday (nom) | 0.3% | 2.997 |
| WN44 | N | 999 | max- | 999 | 1000- | Wednesday (nom) | 0.3% | 2.997 |
| WN45 | N | 999 | max- | 1000 | 1000 | Wednesday (nom) | 0.1% | 0.999 |
| WN46 | N | 999 | max- | 1001 | 1000+ | Wednesday (nom) | 0.1% | 0.999 |
| WN47 | N | 999 | max- | 2500 | nom | Wednesday (nom) | 0.1% | 0.999 |
| WN48 | N | 999 | max- | 4999 | 5000- | Wednesday (nom) | 0.1% | 0.999 |
| WN49 | N | 999 | max- | 5000 | 5000 | Wednesday (nom) | 0% | 0 |
| WN50 | N | 999 | max- | 5001 | 5000+ | Wednesday (nom) | 0% | 0 |
| WN51 | N | 1000 | max | -1 | min- | Wednesday (nom) | 0.3% | 3 |
| WN52 | N | 1000 | max | 0 | min | Wednesday (nom) | 0.3% | 3 |
| WN53 | N | 1000 | max | 1 | min+ | Wednesday (nom) | 0.3% | 3 |
| WN54 | N | 1000 | max | 999 | 1000- | Wednesday (nom) | 0.3% | 3 |
| WN55 | N | 1000 | max | 1000 | 1000 | Wednesday (nom) | 0.1% | 1 |
| WN56 | N | 1000 | max | 1001 | 1000+ | Wednesday (nom) | 0.1% | 1 |
| WN57 | N | 1000 | max | 2500 | nom | Wednesday (nom) | 0.1% | 1 |
| WN58 | N | 1000 | max | 4999 | 5000- | Wednesday (nom) | 0.1% | 1 |
| WN59 | N | 1000 | max | 5000 | 5000 | Wednesday (nom) | 0% | 0 |
| WN60 | N | 1000 | max | 5001 | 5000+ | Wednesday (nom) | 0% | 0 |
| WN61 | N | 1001 | max+ | -1 | min- | Wednesday (nom) | 0.3% | 3.003 |
| WN62 | N | 1001 | max+ | 0 | min | Wednesday (nom) | 0.3% | 3.003 |
| WN63 | N | 1001 | max+ | 1 | min+ | Wednesday (nom) | 0.3% | 3.003 |
| WN64 | N | 1001 | max+ | 999 | 1000- | Wednesday (nom) | 0.3% | 3.003 |
| WN65 | N | 1001 | max+ | 1000 | 1000 | Wednesday (nom) | 0.1% | 1.001 |
| WN66 | N | 1001 | max+ | 1001 | 1000+ | Wednesday (nom) | 0.1% | 1.001 |
| WN67 | N | 1001 | max+ | 2500 | nom | Wednesday (nom) | 0.1% | 1.001 |
| WN68 | N | 1001 | max+ | 4999 | 5000- | Wednesday (nom) | 0.1% | 1.001 |
| WN69 | N | 1001 | max+ | 5000 | 5000 | Wednesday (nom) | 0% | 0 |
| WN70 | N | 1001 | max+ | 5001 | 5000+ | Wednesday (nom) | 0% | 0 |

#### Students (balance held at nominal $2,500)

| Test case | Student? | Amount ($) | Amount value | Balance ($) | Day | Day value | Expected fee | Expected fee ($) |
|---|---|---|---|---|---|---|---|---|
| WS01 | Y | -1 | min- | 2,500 (nom) | 0 (invalid) | min- | 0% | 0 |
| WS02 | Y | -1 | min- | 2,500 (nom) | Sunday | min | 0.1% | -0.001 |
| WS03 | Y | -1 | min- | 2,500 (nom) | Monday | min+ | 0% | 0 |
| WS04 | Y | -1 | min- | 2,500 (nom) | Wednesday | nom | 0% | 0 |
| WS05 | Y | -1 | min- | 2,500 (nom) | Friday | max- | 0% | 0 |
| WS06 | Y | -1 | min- | 2,500 (nom) | Saturday | max | 0.1% | -0.001 |
| WS07 | Y | -1 | min- | 2,500 (nom) | 8 (invalid) | max+ | 0% | 0 |
| WS08 | Y | 0 | min | 2,500 (nom) | 0 (invalid) | min- | 0% | 0 |
| WS09 | Y | 0 | min | 2,500 (nom) | Sunday | min | 0.1% | 0 |
| WS10 | Y | 0 | min | 2,500 (nom) | Monday | min+ | 0% | 0 |
| WS11 | Y | 0 | min | 2,500 (nom) | Wednesday | nom | 0% | 0 |
| WS12 | Y | 0 | min | 2,500 (nom) | Friday | max- | 0% | 0 |
| WS13 | Y | 0 | min | 2,500 (nom) | Saturday | max | 0.1% | 0 |
| WS14 | Y | 0 | min | 2,500 (nom) | 8 (invalid) | max+ | 0% | 0 |
| WS15 | Y | 1 | min+ | 2,500 (nom) | 0 (invalid) | min- | 0% | 0 |
| WS16 | Y | 1 | min+ | 2,500 (nom) | Sunday | min | 0.1% | 0.001 |
| WS17 | Y | 1 | min+ | 2,500 (nom) | Monday | min+ | 0% | 0 |
| WS18 | Y | 1 | min+ | 2,500 (nom) | Wednesday | nom | 0% | 0 |
| WS19 | Y | 1 | min+ | 2,500 (nom) | Friday | max- | 0% | 0 |
| WS20 | Y | 1 | min+ | 2,500 (nom) | Saturday | max | 0.1% | 0.001 |
| WS21 | Y | 1 | min+ | 2,500 (nom) | 8 (invalid) | max+ | 0% | 0 |
| WS22 | Y | 500 | nom | 2,500 (nom) | 0 (invalid) | min- | 0% | 0 |
| WS23 | Y | 500 | nom | 2,500 (nom) | Sunday | min | 0.1% | 0.5 |
| WS24 | Y | 500 | nom | 2,500 (nom) | Monday | min+ | 0% | 0 |
| WS25 | Y | 500 | nom | 2,500 (nom) | Wednesday | nom | 0% | 0 |
| WS26 | Y | 500 | nom | 2,500 (nom) | Friday | max- | 0% | 0 |
| WS27 | Y | 500 | nom | 2,500 (nom) | Saturday | max | 0.1% | 0.5 |
| WS28 | Y | 500 | nom | 2,500 (nom) | 8 (invalid) | max+ | 0% | 0 |
| WS29 | Y | 999 | max- | 2,500 (nom) | 0 (invalid) | min- | 0% | 0 |
| WS30 | Y | 999 | max- | 2,500 (nom) | Sunday | min | 0.1% | 0.999 |
| WS31 | Y | 999 | max- | 2,500 (nom) | Monday | min+ | 0% | 0 |
| WS32 | Y | 999 | max- | 2,500 (nom) | Wednesday | nom | 0% | 0 |
| WS33 | Y | 999 | max- | 2,500 (nom) | Friday | max- | 0% | 0 |
| WS34 | Y | 999 | max- | 2,500 (nom) | Saturday | max | 0.1% | 0.999 |
| WS35 | Y | 999 | max- | 2,500 (nom) | 8 (invalid) | max+ | 0% | 0 |
| WS36 | Y | 1000 | max | 2,500 (nom) | 0 (invalid) | min- | 0% | 0 |
| WS37 | Y | 1000 | max | 2,500 (nom) | Sunday | min | 0.1% | 1 |
| WS38 | Y | 1000 | max | 2,500 (nom) | Monday | min+ | 0% | 0 |
| WS39 | Y | 1000 | max | 2,500 (nom) | Wednesday | nom | 0% | 0 |
| WS40 | Y | 1000 | max | 2,500 (nom) | Friday | max- | 0% | 0 |
| WS41 | Y | 1000 | max | 2,500 (nom) | Saturday | max | 0.1% | 1 |
| WS42 | Y | 1000 | max | 2,500 (nom) | 8 (invalid) | max+ | 0% | 0 |
| WS43 | Y | 1001 | max+ | 2,500 (nom) | 0 (invalid) | min- | 0% | 0 |
| WS44 | Y | 1001 | max+ | 2,500 (nom) | Sunday | min | 0.1% | 1.001 |
| WS45 | Y | 1001 | max+ | 2,500 (nom) | Monday | min+ | 0% | 0 |
| WS46 | Y | 1001 | max+ | 2,500 (nom) | Wednesday | nom | 0% | 0 |
| WS47 | Y | 1001 | max+ | 2,500 (nom) | Friday | max- | 0% | 0 |
| WS48 | Y | 1001 | max+ | 2,500 (nom) | Saturday | max | 0.1% | 1.001 |
| WS49 | Y | 1001 | max+ | 2,500 (nom) | 8 (invalid) | max+ | 0% | 0 |

### 2.3 Deposit fees (`FeesCalculatorDepositTest`)

| Test case | Student? | Amount ($) | Balance ($) | Classes covered | Expected fee | Expected fee ($) |
|---|---|---|---|---|---|---|
| DV01 | Y | 25 | 6,000 | S1, SA1, SB4 | 0.5% | 0.125 |
| DV02 | Y | 25 | 2,000 | S1, SA1, SB3 | 0% | 0 |
| DV03 | Y | 200 | 2,500 | S1, SA2, SB2 | 0.5% | 1.0 |
| DV04 | Y | 200 | 250 | S1, SA2, SB1 | 0.25% | 0.5 |
| DV05 | N | 1,000 | 7,500 | S2, NA2, NB2 | 0.8% | 8.0 |
| DV06 | N | 1,000 | 1,000 | S2, NA2, NB1 | 0.4% | 4.0 |
| DV07 | N | 100 | 20,000 | S2, NA1, NB4 | 0% | 0 |
| DV08 | N | 100 | 5,000 | S2, NA1, NB3 | 0.1% | 0.1 |
| DI01 | Y | -100 | 2,500 | **AI**, S1 | none (no deposit) | 0 |
| DI02 | N | -100 | 5,000 | **AI**, S2 | none (no deposit) | 0 |
| DI03 | Y | 25 | -500 | **BI**, S1, SA1 | 0% | 0 |
| DI04 | N | 1,000 | -500 | **BI**, S2, NA2 | 0.4% | 4.0 |

### 2.4 Transfer fees (`FeesCalculatorTransferTest`)

The expected transfer fee for each test case is calculated by multiplying the transfer amount by the applicable fee percentage divided by 100. The fee percentage is determined using the conditions from the decision tables

#### Student Transfer Test Cases (TC1–TC8)

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

#### Non-Student Transfer Test Cases (TC9–TC16)

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

---

## Section 3: Test Results

Results from running the JUnit suite (`mvn -B test`); the same results appear in the CI run on the pull request. "Actual" is what the code returned.

### 3.1 ATM Session

Summary: 14 test cases, 6 passed, 8 failed.

| Test case | Expected outcome | Actual outcome | Result |
|---|---|---|---|
| P1 | `InvalidPinFormatException` | accepted (no exception) | **Fail** |
| P2 | accepted | `InvalidPinFormatException` | **Fail** |
| P3 | accepted | `InvalidPinFormatException` | **Fail** |
| P4 | accepted | `InvalidPinFormatException` | **Fail** |
| P5 | accepted | `InvalidPinFormatException` | **Fail** |
| P6 | accepted | `InvalidPinFormatException` | **Fail** |
| P7 | `InvalidPinFormatException` | `InvalidPinFormatException` | Pass |
| A1 | `InvalidAmountException` | `InvalidAmountException` | Pass |
| A2 | accepted | accepted | Pass |
| A3 | accepted | accepted | Pass |
| A4 | accepted | accepted | Pass |
| A5 | accepted | `InvalidAmountException` | **Fail** |
| A6 | accepted | accepted | Pass |
| A7 | `InvalidAmountException` | accepted (no exception) | **Fail** |

### 3.2 Withdrawal fees

Summary: 119 test cases, 41 passed, 78 failed (non-students: 34 passed, 36 failed; students: 7 passed, 42 failed).

#### Non-students

| Test case | Expected fee ($) | Actual fee ($) | Result |
|---|---|---|---|
| WN01 | -0.003 | -0.002 | **Fail** |
| WN02 | -0.003 | -0.002 | **Fail** |
| WN03 | -0.003 | -0.002 | **Fail** |
| WN04 | -0.003 | -0.002 | **Fail** |
| WN05 | -0.001 | -0.001 | Pass |
| WN06 | -0.001 | -0.001 | Pass |
| WN07 | -0.001 | -0.001 | Pass |
| WN08 | -0.001 | -0.001 | Pass |
| WN09 | 0 | -0.001 | **Fail** |
| WN10 | 0 | -0.001 | **Fail** |
| WN11 | 0 | 0 | Pass |
| WN12 | 0 | 0 | Pass |
| WN13 | 0 | 0 | Pass |
| WN14 | 0 | 0 | Pass |
| WN15 | 0 | 0 | Pass |
| WN16 | 0 | 0 | Pass |
| WN17 | 0 | 0 | Pass |
| WN18 | 0 | 0 | Pass |
| WN19 | 0 | 0 | Pass |
| WN20 | 0 | 0 | Pass |
| WN21 | 0.003 | 0.002 | **Fail** |
| WN22 | 0.003 | 0.002 | **Fail** |
| WN23 | 0.003 | 0.002 | **Fail** |
| WN24 | 0.003 | 0.002 | **Fail** |
| WN25 | 0.001 | 0.001 | Pass |
| WN26 | 0.001 | 0.001 | Pass |
| WN27 | 0.001 | 0.001 | Pass |
| WN28 | 0.001 | 0.001 | Pass |
| WN29 | 0 | 0.001 | **Fail** |
| WN30 | 0 | 0.001 | **Fail** |
| WN31 | 1.5 | 1 | **Fail** |
| WN32 | 1.5 | 1 | **Fail** |
| WN33 | 1.5 | 1 | **Fail** |
| WN34 | 1.5 | 1 | **Fail** |
| WN35 | 0.5 | 0.5 | Pass |
| WN36 | 0.5 | 0.5 | Pass |
| WN37 | 0.5 | 0.5 | Pass |
| WN38 | 0.5 | 0.5 | Pass |
| WN39 | 0 | 0.5 | **Fail** |
| WN40 | 0 | 0.5 | **Fail** |
| WN41 | 2.997 | 1.998 | **Fail** |
| WN42 | 2.997 | 1.998 | **Fail** |
| WN43 | 2.997 | 1.998 | **Fail** |
| WN44 | 2.997 | 1.998 | **Fail** |
| WN45 | 0.999 | 0.999 | Pass |
| WN46 | 0.999 | 0.999 | Pass |
| WN47 | 0.999 | 0.999 | Pass |
| WN48 | 0.999 | 0.999 | Pass |
| WN49 | 0 | 0.999 | **Fail** |
| WN50 | 0 | 0.999 | **Fail** |
| WN51 | 3 | 2 | **Fail** |
| WN52 | 3 | 2 | **Fail** |
| WN53 | 3 | 2 | **Fail** |
| WN54 | 3 | 2 | **Fail** |
| WN55 | 1 | 1 | Pass |
| WN56 | 1 | 1 | Pass |
| WN57 | 1 | 1 | Pass |
| WN58 | 1 | 1 | Pass |
| WN59 | 0 | 1 | **Fail** |
| WN60 | 0 | 1 | **Fail** |
| WN61 | 3.003 | 2.0020000000000002 | **Fail** |
| WN62 | 3.003 | 2.0020000000000002 | **Fail** |
| WN63 | 3.003 | 2.0020000000000002 | **Fail** |
| WN64 | 3.003 | 2.0020000000000002 | **Fail** |
| WN65 | 1.001 | 1.001 | Pass |
| WN66 | 1.001 | 1.001 | Pass |
| WN67 | 1.001 | 1.001 | Pass |
| WN68 | 1.001 | 1.001 | Pass |
| WN69 | 0 | 1.0010000000000001 | **Fail** |
| WN70 | 0 | 1.0010000000000001 | **Fail** |

#### Students

| Test case | Expected fee ($) | Actual fee ($) | Result |
|---|---|---|---|
| WS01 | 0 | -0.001 | **Fail** |
| WS02 | -0.001 | 0 | **Fail** |
| WS03 | 0 | -0.001 | **Fail** |
| WS04 | 0 | -0.001 | **Fail** |
| WS05 | 0 | -0.001 | **Fail** |
| WS06 | -0.001 | 0 | **Fail** |
| WS07 | 0 | -0.001 | **Fail** |
| WS08 | 0 | 0 | Pass |
| WS09 | 0 | 0 | Pass |
| WS10 | 0 | 0 | Pass |
| WS11 | 0 | 0 | Pass |
| WS12 | 0 | 0 | Pass |
| WS13 | 0 | 0 | Pass |
| WS14 | 0 | 0 | Pass |
| WS15 | 0 | 0.001 | **Fail** |
| WS16 | 0.001 | 0 | **Fail** |
| WS17 | 0 | 0.001 | **Fail** |
| WS18 | 0 | 0.001 | **Fail** |
| WS19 | 0 | 0.001 | **Fail** |
| WS20 | 0.001 | 0 | **Fail** |
| WS21 | 0 | 0.001 | **Fail** |
| WS22 | 0 | 0.5 | **Fail** |
| WS23 | 0.5 | 0 | **Fail** |
| WS24 | 0 | 0.5 | **Fail** |
| WS25 | 0 | 0.5 | **Fail** |
| WS26 | 0 | 0.5 | **Fail** |
| WS27 | 0.5 | 0 | **Fail** |
| WS28 | 0 | 0.5 | **Fail** |
| WS29 | 0 | 0.999 | **Fail** |
| WS30 | 0.999 | 0 | **Fail** |
| WS31 | 0 | 0.999 | **Fail** |
| WS32 | 0 | 0.999 | **Fail** |
| WS33 | 0 | 0.999 | **Fail** |
| WS34 | 0.999 | 0 | **Fail** |
| WS35 | 0 | 0.999 | **Fail** |
| WS36 | 0 | 1 | **Fail** |
| WS37 | 1 | 0 | **Fail** |
| WS38 | 0 | 1 | **Fail** |
| WS39 | 0 | 1 | **Fail** |
| WS40 | 0 | 1 | **Fail** |
| WS41 | 1 | 0 | **Fail** |
| WS42 | 0 | 1 | **Fail** |
| WS43 | 0 | 1.0010000000000001 | **Fail** |
| WS44 | 1.001 | 0 | **Fail** |
| WS45 | 0 | 1.0010000000000001 | **Fail** |
| WS46 | 0 | 1.0010000000000001 | **Fail** |
| WS47 | 0 | 1.0010000000000001 | **Fail** |
| WS48 | 1.001 | 0 | **Fail** |
| WS49 | 0 | 1.0010000000000001 | **Fail** |

### 3.3 Deposit fees

Results from running `bank.utils.FeesCalculatorDepositTest` (JUnit 5, `mvn -B test`); the same results appear in the CI run on the pull request. "Actual fee" is the value returned by `FeesCalculator.calculateDepositInterest`.

Summary: 12 test cases, 2 passed, 10 failed.

| Test case | Expected fee ($) | Actual fee ($) | Result |
|---|---|---|---|
| DV01 | 0.125 | 0.125 | Pass |
| DV02 | 0 | 0.05 | **Fail** |
| DV03 | 1.0 | 2.0 | **Fail** |
| DV04 | 0.5 | 1.0 | **Fail** |
| DV05 | 8.0 | 10.0 | **Fail** |
| DV06 | 4.0 | 5.0 | **Fail** |
| DV07 | 0 | 0.5 | **Fail** |
| DV08 | 0.1 | 0.0 | **Fail** |
| DI01 | 0 | -0.2 | **Fail** |
| DI02 | 0 | -0.0 | Pass |
| DI03 | 0 | 0.05 | **Fail** |
| DI04 | 4.0 | 5.0 | **Fail** |

### 3.4 Transfer fees

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

---

## Section 4: Fault Localization

For every failing test case, the fault we believe it exposes. Failing test cases that expose the same fault are grouped into one entry, and a test case can appear under more than one fault when it passes through several faulty conditions.

### 4.1 ATM Session

| Fault | Exposed by | Location | Suspected fault | Justification |
|---|---|---|---|---|
| S1 | P1-P6 | `atm.utils.FormatChecker.checkPinFormat`, `if (pin.length == 4)` and `for (int i = 0; i < 4; i++)` | The required PIN length is 4 instead of 5. | Every 5-digit PIN is rejected and the 4-digit PIN `1234` is accepted, which is exactly what a length check of 4 produces. |
| S2 | P5, P6 (masked by S1) | `atm.utils.FormatChecker.checkPinFormat`, `(pin[i] >= '9')` | The upper digit check uses `>=` instead of `>`, so the digit `9` is rejected. | P5 and P6 are the only valid PINs containing `9`. S1 already explains their failure, so S2 can only be confirmed once S1 is fixed; we expect P5 and P6 to keep failing after that. |
| S3 | A5 | `atm.utils.CashValidator.validateWithdrawal`, `amount % 50` followed by `% 20` | The validator only tries "as many $50 bills as possible, then $20 bills", so it rejects amounts that need fewer $50 bills. | 980 % 50 = 30 and 30 % 20 ≠ 0, so $980 is rejected even though it can be dispensed (49 × $20, or 18 × $50 + 4 × $20). |
| S4 | A7 | `atm.session.Session.setAmount`, withdrawal branch | The $1,000 daily limit is never checked. | $1,020 passes the bill check (1020 % 50 = 20) and nothing else rejects it, so no `InvalidAmountException` is thrown. |

### 4.2 Withdrawal fees

| Fault | Exposed by | Location | Suspected fault | Justification |
|---|---|---|---|---|
| W1 | WS01-WS07, WS15-WS49 (every student case with a non-zero amount) | `bank.utils.FeesCalculator.calculateWithdrawalFee`, student branch, `if ((dayOfWeek == Calendar.SATURDAY) \|\| (dayOfWeek == Calendar.SUNDAY)) feePercentage = 0.0; else feePercentage = 0.001;` | The weekday and weekend fees are swapped. | Students are charged 0.1% on Monday to Friday (and on the invalid days 0 and 8) and nothing on the weekend, the reverse of the specification. WS08-WS14 pass only because the amount is $0. |
| W2 | WN01-WN04, WN21-WN24, WN31-WN34, WN41-WN44, WN51-WN54, WN61-WN64 (non-student, balance < $1,000) | `calculateWithdrawalFee`, non-student branch, `if (accountBalance < 1000.00) feePercentage = 0.002;` | The rate is 0.2% instead of 0.3%. | Every non-student case with a balance of -1, 0, 1 or 999 returns amount × 0.002 instead of amount × 0.003. |
| W3 | WN09-WN10, WN29-WN30, WN39-WN40, WN49-WN50, WN59-WN60, WN69-WN70 (non-student, balance ≥ $5,000) | `calculateWithdrawalFee`, non-student branch, `else if (accountBalance < 10000.00)` | The upper balance boundary is $10,000 instead of $5,000. | Balances of 5000 and 5001 are still charged 0.1% when they should be free, while every balance from 1000 to 4999 is correct, which places the fault on the second boundary. |

### 4.3 Deposit fees

Failing test cases that expose the same fault are grouped into one entry.

| Fault | Exposed by | Location | Suspected fault | Justification |
|---|---|---|---|---|
| D1 | DV02, DI03 | `bank.utils.FeesCalculator.calculateDepositInterest`, student branch, `amount <= 100` sub-branch, `else interestPercentage = 0.002` | A student deposit of $50 or less into a balance of $5,000 or less is charged 0.2% instead of 0%. | Both tests fall in that branch and return 25 × 0.002 = 0.05 instead of 0. |
| D2 | DV03, DV04 | `calculateDepositInterest`, student branch, `if (amount > 100.00)` sub-branch, rates `0.01` / `0.005` | Rates for large student deposits are double the specified 0.5% / 0.25%. | Both tests return exactly twice the expected fee. The thresholds in this branch (100 and 1000 instead of 50 and 500) also differ from the specification, but these representative values do not separate them. |
| D3 | DV05, DV06, DI04 | `calculateDepositInterest`, non-student branch, `if (amount > 500.00)` sub-branch, rates `0.01` / `0.005` | Rates for large non-student deposits are 1% / 0.5% instead of 0.8% / 0.4%. | All three tests return amount × 0.01 or amount × 0.005 instead of amount × 0.008 or amount × 0.004. |
| D4 | DV07, DV08 | `calculateDepositInterest`, non-student branch, `amount <= 500` sub-branch, rates `0.005` / `0.0` | The two rates are swapped: a high balance is charged 0.5% and a low balance nothing, instead of 0% and 0.1%. | DV07 (balance $20,000) is charged and DV08 (balance $5,000) is free, the reverse of the specification. |
| D5 | DI01 | `calculateDepositInterest`, `interest = amount * interestPercentage` | There is no guard against a non-positive amount, so the result can be a negative fee. | A student deposit of -$100 returns -0.2. DI02 passes only because that branch's (faulty, see D4) rate is 0. |

### 4.4 Transfer fees

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
