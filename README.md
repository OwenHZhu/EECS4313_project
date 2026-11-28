# QuickCart Software Testing Project — Starter Repository

This repository intentionally contains a small production codebase with imperfect behaviour. Your job is not to build a new application. Your job is to design, automate, evaluate, and communicate a defensible testing strategy.

## Technology
- Java 17+
- Maven
- JUnit 5
- Mockito
- jqwik (property-based testing)
- JaCoCo (coverage)
- PIT (mutation testing)

## Useful commands
- `mvn test`
- `mvn test jacoco:report`
- `mvn org.pitest:pitest-maven:mutationCoverage`

## Rules
- Treat PROJECT_SPECIFICATION.md as authoritative.
- Do not change production code merely to make a failing test pass.
- A behaviour-preserving refactor for testability is allowed only if clearly identified and justified.
- Add your own tests under `src/test/java`.
- Keep evidence needed for the final video/report in the repository.






## Testing Strategy (Pricing Section)


Characteristics
- C1: is a line Bulk?
  - b1: 1 <= line quantity < 10
  - b2: line quantity >= 10
- C2: does customer have GOLD?
  - b1: tier == CustomerTier.GOLD
  - b2: tier == CustomerTier.STANDARD
- C3: is coupon active in regards to date?
  - b1: start date <= order date <= end date
  - b2: order date before start date
  - b3: order date after end date
  - b4: no coupon
- C4: is discounted merchandise at least $50.00?
  - b1: < $50.00
  - b2: >= $50.00 
- C5: is merchandise negative or positive?
  - b1: coupon amount <= merchandise
  - b2: coupon amount > merchandise
- C6: is discounted merchandise $100.00 or more for shipping cost change?
  - b1: merchandise < $100.00
  - b2: merchandise >= $100.00
- C7: Which tax is applied based on province?
  - b1: ON
  - b2: BC
  - b3: AB
  - b4: unsupported
- C8: how many lines are in the cart?
  - b1: empty cart
  - b2: one line
  - b3: multiple lines


## Decision Table

| Conditions/Actions                      | R1 | R2 | R3 | R4 | R5 | R6 | R7 | R8 | R9 | R10 | R11 | R12 |
|-----------------------------------------|----|----|----|----|----|----|----|----|----|-----|-----|-----|
| is a line Bulk?                         | N  | N  | Y  | N  | Y  | N  | N  | N  | N  | N   | N   | Y   |
| customer has GOLD?                      | N  | N  | N  | Y  | Y  | N  | Y  | N  | N  | N   | N   | Y   |
| coupon: date valid?                     | N  | N  | N  | N  | N  | Y  | Y  | Y  | Y  | Y   | Y   | Y   |
| coupon: discounted merch >= $50.00?     | -  | -  | -  | -  | -  | N  | N  | Y  | Y  | Y   | Y   | Y   |
| coupon makes merch negative?            | -  | -  | -  | -  | -  | -  | -  | N  | N  | N   | Y   | N   |
| coupon: merch >= $100 before coupon?    | -  | -  | -  | -  | -  | -  | -  | N  | Y  | Y   | N   | Y   |
| shipping: discounted merch >= $100?     | N  | Y  | N  | N  | Y  | N  | N  | N  | N  | Y   | N   | Y   |
| apply 5% bulk discount                  |    |    | X  |    | X  |    |    |    |    |     |     | X   |
| apply 10% GOLD discount                 |    |    |    | X  | X  |    | X  |    |    |     |     | X   |
| subtract coupon                         |    |    |    |    |    |    |    | X  | X  | X   | X   | X   |
| set merch to $0                         |    |    |    |    |    |    |    |    |    |     | X   |     |
| shipping $8.99                          | X  |    | X  | X  |    | X  | X  | X  | X  |     | X   |     |
| shipping free                           |    | X  |    |    | X  |    |    |    |    | X   |     | X   |

Decision tables test the combinations of blocks that depend on each other. We have 12 tests to check relevant combinations.


| Rule | items       | tier     | coupon        | province | orderDate  | Expected (merch, shipping, tax, total) |
|------|-------------|----------|---------------|----------|------------|----------------------------------------|
| R1   | $20.00 X 1  | STANDARD | NULL          | ON       | 2026-11-15 | (20.00,8.99,3.77,32.76)                |
| R2   | $120.00 X 1 | STANDARD | $10, Nov 1-30 | ON       | 2026-12-15 | (120.00,0.00,15.60,135.60)             |
| R3   | $5.00 X 12  | STANDARD | $10, Nov 1-30 | ON       | 2026-10-15 | (57.00,8.99,8.58,74.57)                |
| R4   | $20.00 X 1  | GOLD     | NULL          | ON       | 2026-11-15 | (18.00,8.99,3.51,30.50)                |
| R5   | $10.00 X 12 | GOLD     | NULL          | ON       | 2026-11-15 | (102.60,0.00,13.34,115.94)             |
| R6   | $40.00 X 1  | STANDARD | $10, Nov 1-30 | ON       | 2026-11-15 | (40.00,8.99,6.37,55.36)                |
| R7   | $55.00 X 1  | GOLD     | $10, Nov 1-30 | ON       | 2026-11-15 | (49.50,8.99,7.60,66.09)                |
| R8   | $60.00 X 1  | STANDARD | $10, Nov 1-30 | ON       | 2026-11-15 | (50.00,8.99,7.67,66.66)                |
| R9   | $105.00 X 1 | STANDARD | $10, Nov 1-30 | ON       | 2026-11-15 | (95.00,8.99,13.52,117.51)              |
| R10  | $120.00 X 1 | STANDARD | $10, Nov 1-30 | ON       | 2026-11-15 | (110.00,0.00,14.30,124.30)             |
| R11  | $60.00 X 1  | STANDARD | $70, Nov 1-30 | ON       | 2026-11-15 | (0.00,8.99,1.17,10.16)                 |
| R12  | $15.00 X 12 | GOLD     | $10, Nov 1-30 | ON       | 2026-11-15 | (143.90,0.00,18.71,162.61)             |

## BCC Test table


| Test | items                     | tier     | coupon | province | orderDate  | Expected (merch, shipping, tax, total) |
|------|---------------------------|----------|--------|----------|------------|----------------------------------------|
| T1   | $20.00 X 1                | STANDARD | NULL   | ON       | 2026-11-25 | (20.00,8.99,3.77,32.76)                |
| T2   | $20.00 X 1                | STANDARD | NULL   | **BC**   | 2026-11-25 | (20.00,8.99,3.48,32.47)                |
| T3   | $20.00 X 1                | STANDARD | NULL   | **AB**   | 2026-11-25 | (20.00,8.99,1.45,30.44)                |
| T4   | $20.00 X 1                | STANDARD | NULL   | **QC**   | 2026-11-25 | IllegalArgumentException               |
| T5   | **[]**                    | STANDARD | NULL   | ON       | 2026-11-25 | IllegalArgumentException               |
| T6   | **$15.00 X 1, $5.00 X 1** | STANDARD | NULL   | ON       | 2026-11-25 | (20.00,8.99,3.77,32.76)                |

Independent blocks are tested with BCC to check that they individually work. We have 6 tests to check. 

## BVT 
| Test | Boundary              | Value            | items       | tier     | coupon           | province | orderDate      | Expected (merch, shipping, tax, total) |
|------|-----------------------|------------------|-------------|----------|------------------|----------|----------------|----------------------------------------|
| B1   | Bulk qty >= 10        | 9                | $5.00 X 9   | STANDARD | NULL             | ON       | 2026-11-15     | (45.00,8.99,7.02,61.01)                |
| B2   | Bulk qty >= 10        | **10**           | $5.00 X 10  | STANDARD | NULL             | ON       | 2026-11-15     | (47.50,8.99,7.34,63.83)                |
| B3   | Bulk qty >= 10        | 11               | $5.00 X 11  | STANDARD | NULL             | ON       | 2026-11-15     | (52.25,8.99,7.96,69.20)                |
| B4   | Coupon min >= $50     | 49.99            | $49.99 X 1  | STANDARD | $10, Nov 1-30    | ON       | 2026-11-15     | (49.99,8.99,7.67,66.65)                |
| B5   | Coupon min >= $50     | **50.00**        | $50.00 X 1  | STANDARD | $10, Nov 1-30    | ON       | 2026-11-15     | (40.00,8.99,6.37,55.36)                |
| B6   | Coupon min >= $50     | 50.01            | $50.01 X 1  | STANDARD | $10, Nov 1-30    | ON       | 2026-11-15     | (40.01,8.99,6.37,55.37)                |
| B7   | Merch not negative    | coupon 59.99     | $60.00 X 1  | STANDARD | $59.99, Nov 1-30 | ON       | 2026-11-15     | (0.01,8.99,1.17,10.17)                 |
| B8   | Merch not negative    | **coupon 60.00** | $60.00 X 1  | STANDARD | $60.00, Nov 1-30 | ON       | 2026-11-15     | (0.00,8.99,1.17,10.16)                 |
| B9   | Merch not negative    | coupon 60.01     | $60.00 X 1  | STANDARD | $60.01, Nov 1-30 | ON       | 2026-11-15     | (0.00,8.99,1.17,10.16)                 |
| B10  | Free shipping >= $100 | 99.99            | $99.99 X 1  | STANDARD | NULL             | ON       | 2026-11-15     | (99.99,8.99,14.17,123.15)              |
| B11  | Free shipping >= $100 | **100.00**       | $100.00 X 1 | STANDARD | NULL             | ON       | 2026-11-15     | (100.00,0.00,13.00,113.00)             |
| B12  | Free shipping >= $100 | 100.01           | $100.01 X 1 | STANDARD | NULL             | ON       | 2026-11-15     | (100.01,0.00,13.00,113.01)             |
| B13  | Coupon start date     | start - 1        | $60.00 X 1  | STANDARD | $10, Nov 1-30    | ON       | **2026-10-31** | (60.00,8.99,8.97,77.96)                |
| B14  | Coupon start date     | **start**        | $60.00 X 1  | STANDARD | $10, Nov 1-30    | ON       | **2026-11-01** | (50.00,8.99,7.67,66.66)                |
| B15  | Coupon end date       | **end**          | $60.00 X 1  | STANDARD | $10, Nov 1-30    | ON       | **2026-11-30** | (50.00,8.99,7.67,66.66)                |
| B16  | Coupon end date       | end + 1          | $60.00 X 1  | STANDARD | $10, Nov 1-30    | ON       | **2026-12-01** | (60.00,8.99,8.97,77.96)                |

BVT checks around boundaries. We have 16 tests to check.


## Use these commands
- `mvn test`

To run only the pricing tests:
- `mvn test -Dtest=PricingServiceTest`

To run only one of the three test groups (decision table, BCC or BVT) add the method name:
- `mvn test -Dtest=PricingServiceTest#boundary`

Use decisionTableRule or bccProvince instead of boundary for the other two
