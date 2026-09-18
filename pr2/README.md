## Business Logic

The service layer implements all domain logic for the `Book` entity.

### Operations

- **Create a book** — registers a new book in the system. The initial status is always `DRAFT`.
- **Update a book** — changes the descriptive fields (title, author, genre, publication year, price, pages). Status is intentionally not affected.
- **Change status** — moves a book through its lifecycle according to the allowed transitions.
- **Delete a book** — removes a book from the system.
- **Calculate price** — computes the effective price of a book using the currently selected pricing strategy.

### Variable Logic: Pricing

Pricing is the variable part of the business logic and is isolated behind the `BookPricingStrategy` interface.

- `StandardPricingStrategy` — returns the base price.
- `DiscountedPricingStrategy` — applies a discount to the base price.

The active strategy is chosen per request (case-insensitive name, default `STANDARD`).

The specific discount value is a demonstration; the specification does not define a concrete pricing policy.

---

## Business Rules

### Rule 1 — Unique domain identifier

A book is uniquely identified by the pair `(title, author)`.

- The check is case-insensitive.
- The check applies to all stored books, regardless of their status.
- Creating a book, or updating one so that its `(title, author)` pair matches another existing book, is rejected.
- Violation → `DuplicateBookException`.

### Rule 2 — Status lifecycle

A book can be in one of three states: `DRAFT`, `PUBLISHED`, `ARCHIVED`.

Allowed transitions:

| From        | To          |
| ----------- | ----------- |
| `DRAFT`     | `PUBLISHED` |
| `PUBLISHED` | `ARCHIVED`  |
| `ARCHIVED`  | *(none)*    |

- `ARCHIVED` is a terminal state — no further transitions are allowed.
- Any other transition — including self-transitions like `DRAFT → DRAFT` — is rejected.
- Status changes happen only through the dedicated status-update operation.
- Violation → `InvalidBookStatusTransitionException`.