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

## Testing Strategy (Ordering Section)

## Characteristics and Blocks

| ID |           Characteristic           |                       Blocks                       |
|:--:|:----------------------------------:|:--------------------------------------------------:|
| C1 | Stock vs. requested qty (per item) | available < qty, available == qty, available > qty |
| C2 | Number of items                    | 1, 2+                                              |
| C3 | Which item is short (multi-item)   | none, first only, last only, several               |
| C4 | Payment outcome                    | approved, declined                                 |
| C5 | Quote preconditions                | valid, invalid (empty cart, unsupported province)  |
| C6 | Same product on two lines          | no, yes                                            |

## Decision table

| Rule | Stock sufficient (all items) |                  Payment approved                 |      Status      |             authorize called            |                   reserve called                   |
|:----:|:----------------------------:|:-------------------------------------------------:|:----------------:|:---------------------------------------:|:--------------------------------------------------:|
| R1   | No                           | n/a                                               | OUT_OF_STOCK     | never                                   | never                                              |
| R2   | Yes                          | Yes                                               | ACCEPTED         | once, with customerId and quote.total() | once per item, with the exact qty, after authorize |
| R3   | Yes                          | No                                                | PAYMENT_DECLINED | once                                    | never                                              |

## Methodology
Boundary value testing: use it for C1. The stock check is a relational comparison, so this is where off-by-one defects live.
All-combinations (ACoC) over BCC: the ordering space is tiny (stock outcome × payment outcome, which gives the three rules above). 

Base choice would add nothing. Use ACoC on C1 × C4 at the boundary, and base choice only if you add C2, C3 and C6.
Interaction and state-based assertions: these are the main oracle, because the rules are about side effects.
Use Mockito InOrder to verify that authorize comes before reserve.
Use verify(inventory, never()).reserve(...) for R1 and R3. Don't use verifyNoInteractions, because available() is a legitimate query.

### Test doubles:
- Payment is stubbed or mocked to force a decline and verify amount.
- Inventory is best as a stateful in-memory fake. It lets us assert "inventory unchanged" as state, not just call counts. We can also make the payment stub snapshot the fake's stock when authorize is called, which shows directly whether reservation happened before payment.
- Pricing can be mocked to return a fixed OrderQuote, which isolates our ordering tests from the pricing work done by our partner. PricingService is a non-final class, so Mockito can mock it.
### Integration test: 
- Use real PricingService + OrderService + the fake inventory and a stub payment. Assert that the amount sent to payment equals the real quote total. This tests the quote-to-payment handoff, which a mocked pricing service can't.
### Property-based candidates (jqwik):
- For any stock/qty pair, OUT_OF_STOCK holds iff available < qty, and no reserve occurs.
- For any declined payment, inventory is unchanged.

## Concrete test cases

| Test |                Setup               |                      Expected                     |
|:----:|:----------------------------------:|:-------------------------------------------------:|
| T1   | qty 5, available 4                 | OUT_OF_STOCK, no authorize, no reserve            |
| T2   | qty 5, available 5                 | ACCEPTED, reserve(5)                              |
| T3   | qty 5, available 6                 | ACCEPTED                                          |
| T4   | qty 1, available 0 and 1           | OUT_OF_STOCK, then ACCEPTED                       |
| T5   | two items, first short             | OUT_OF_STOCK, no reserve                          |
| T6   | two items, last short              | OUT_OF_STOCK, no reserve on the first item either |
| T7   | sufficient stock, declined         | PAYMENT_DECLINED, stock unchanged                 |
| T8   | sufficient stock, approved         | InOrder: authorize, then reserve                  |
| T9   | authorize arguments                | customerId and quote.total() match                |
| T10  | empty cart or unsupported province | exception, no inventory or payment calls          |