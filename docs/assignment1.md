
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

### Transfer Test Results

Table will be completed after running the JUnit tests

| Test Case | Expected Fee ($) | Actual Fee ($) | Result |
|---|---|---|---|
| TC1 | 0.148 | TBD | Not Run |
| TC2 | 0.074 | TBD | Not Run |
| TC3 | 0.074 | TBD | Not Run |
| TC4 | 0.037 | TBD | Not Run |
| TC5 | 0.2005 | TBD | Not Run |
| TC6 | 0.50125 | TBD | Not Run |
| TC7 | 0.10025 | TBD | Not Run |
| TC8 | 1.0025 | TBD | Not Run |
| TC9 | 2.02 | TBD | Not Run |
| TC10 | 4.04 | TBD | Not Run |
| TC11 | 0.808 | TBD | Not Run |
| TC12 | 1.616 | TBD | Not Run |
| TC13 | 0.05 | TBD | Not Run |
| TC14 | 0.10 | TBD | Not Run |
| TC15 | 0.01 | TBD | Not Run |
| TC16 | 0.02 | TBD | Not Run |
