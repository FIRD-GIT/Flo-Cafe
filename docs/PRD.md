# Flo Cafe — Product Requirements Document

**Version:** 1.0  
**Status:** Draft for Lab 01 stakeholder review  
**Author:** Firdaos Alesinloye Abbas  
**Date:** 29 October 2026  
**Course:** CTEC3703 Mobile Application Development & Big Data  
**Assessment:** Smart Cafe programming coursework

## 1. Product summary

Flo Cafe is an Android mobile application that allows university café customers to browse available products, place an order, and follow its progress without relying on a queue or paper receipt. The first assessed release is an individual **Customer Application**. Café administration is represented only where necessary to demonstrate meaningful order-status changes; it is not a second complete application in this scope.

The product will be developed as a credible, maintainable coursework project rather than as a collection of disconnected screens. Each iteration will produce working evidence, tests, documentation, and a traceable GitHub commit.

## 2. Problem and opportunity

Customers need a quick and clear way to decide what is available, submit an accurate order, and know when it is ready for collection. A mobile workflow can reduce uncertainty and provide a better record of current and previous orders.

The opportunity is to demonstrate a complete, secure, data-backed mobile workflow with appropriate validation, error handling, testing, and one meaningful advanced integration.

## 3. Goals and success measures

### Goals

1. Let a customer create an account and sign in securely.
2. Let a customer discover available café products and prices.
3. Let a customer create, review, edit, and submit a basket.
4. Let a customer see order confirmation and status updates.
5. Let a customer review previous orders and submit feedback.
6. Demonstrate maintainable Android engineering practices and an advanced integration.

### Success measures

- A new customer can register, log in, browse the menu, place an order, and see confirmation in one demonstrable workflow.
- Invalid input and authentication failures keep the user on the current screen and show a useful message.
- A submitted order is persisted and can be retrieved after restarting the app.
- A status transition is visible to the customer through the order-history/current-order flow.
- Core requirements have repeatable test cases and documented results.
- Every implemented requirement can be traced to a backlog item and Git history.

## 4. Users and roles

### Primary user: Customer

A student or staff member who wants to order café products for collection.

### Supporting role: Simulated café processing

For the individual-project option, order processing can be demonstrated with seeded status changes, a controlled test interface, or an automated state progression. This role is not a full admin product and will be clearly labelled in the report and demonstration.

### Stakeholder: Lab tutor/client

The tutor provides feedback and may request changes during weekly development. Only real feedback and meetings will be recorded in `docs/CLIENT_MEETINGS.md`.

## 5. Scope

### In scope for the first complete customer release

- Registration and email/password login.
- Logout and persisted authenticated session handling.
- Menu categories such as drinks, cakes, and other café products.
- Product name, description, price, availability, and optional image.
- Menu search/filter.
- Add, update quantity, and remove basket items.
- Checkout validation and order confirmation.
- Current order status and previous order history.
- Status values: Pending, Preparing, Ready for Collection, Collected.
- Customer feedback and product/service rating after an order.
- User-facing order-status notification or in-app status indicator.
- Local persistence and data validation.
- One meaningful advanced integration, selected and recorded before implementation.

### Out of scope for the first release

- A complete production admin application.
- Real payment processing or storage of card details.
- Real-time delivery logistics.
- Live inventory procurement.
- Multiple cafés or multi-tenant administration.
- Unreviewed AI-generated content presented as a finished solution.

### Candidate later enhancements

- A separate admin dashboard.
- Push notifications.
- Loyalty points.
- Personalised recommendations.
- Sales analytics dashboard.
- Accessibility and theming refinements beyond the core acceptance criteria.

## 6. User journeys

### Registration and login

Welcome → Register or Login → validate input → authenticate → Customer Home/Menu.

Failed validation or service failure → remain on the current screen → show a meaningful error.

### Ordering

Customer Home → browse/search menu → view product → add to basket → review/edit basket → checkout → confirmation → current order.

### Order tracking

Current order → view status and order details → status changes are reflected when data refreshes or a notification is received.

### Feedback

Completed order → rate service/products → submit feedback → confirmation.

## 7. Functional requirements

| ID | Requirement | Priority |
|---|---|---|
| FR-01 | The customer can register with a valid email address and password. | Must |
| FR-02 | The customer can log in, remain signed in across app restarts, and log out. | Must |
| FR-03 | The customer can browse products with name, description, price, category, and availability. | Must |
| FR-04 | The customer can search or filter available products. | Must |
| FR-05 | The customer can add products to a basket and change quantities. | Must |
| FR-06 | The customer can remove products and see an updated total. | Must |
| FR-07 | The customer can submit a valid order and receive a confirmation reference. | Must |
| FR-08 | The customer can view the current order and its status. | Must |
| FR-09 | The customer can view previous orders and their details. | Should |
| FR-10 | The customer can submit a rating and written feedback for an eligible order. | Should |
| FR-11 | The customer can receive or view a clear status-change notification. | Should |
| FR-12 | The app provides one meaningful advanced integration that contributes to the customer experience or analysis. | Must |
| FR-13 | The app handles unavailable products, invalid input, network/database failures, and empty states clearly. | Must |

## 8. Non-functional requirements

| ID | Requirement | Measure or evidence |
|---|---|---|
| NFR-01 | Usability | A first-time user can complete the core order journey without undocumented steps. |
| NFR-02 | Accessibility | Controls have readable labels, useful content descriptions, and sufficient contrast. |
| NFR-03 | Security | Authentication is delegated to a suitable provider; passwords and tokens are never stored in plaintext or logged. |
| NFR-04 | Privacy | Only necessary customer data is collected; test data is synthetic; secrets are excluded from GitHub. |
| NFR-05 | Reliability | Failed requests show an error state and do not silently create a false successful order. |
| NFR-06 | Maintainability | UI, state/business logic, and persistence responsibilities are separated. |
| NFR-07 | Performance | Menu and basket interactions remain responsive on the coursework emulator/device. |
| NFR-08 | Traceability | Requirements, changes, tests, and decisions are linked through project documentation. |

## 9. Data model

The initial model will be refined during implementation and documented before schema changes.

| Entity | Key fields |
|---|---|
| User | `uid`, `email`, `displayName`, `createdAt` |
| MenuItem | `id`, `name`, `description`, `category`, `price`, `imageUrl`, `available` |
| BasketItem | `menuItemId`, `nameSnapshot`, `unitPriceSnapshot`, `quantity` |
| Order | `id`, `customerUid`, `createdAt`, `status`, `total`, `collectionCode` |
| OrderItem | `orderId`, `menuItemId`, `nameSnapshot`, `unitPrice`, `quantity` |
| Feedback | `id`, `orderId`, `customerUid`, `rating`, `comment`, `createdAt` |

Prices and product names are copied into order items at checkout so that historical orders remain understandable if the menu changes later.

## 10. Technical direction

- **Platform:** Android Studio.
- **Language:** Kotlin.
- **UI:** XML layouts with AndroidX components, selected for clarity and alignment with the lab material.
- **Authentication:** Firebase Authentication email/password, subject to project setup and tutor review.
- **Persistence:** Room/SQLite for structured local café data and cache; the exact synchronisation boundary will be documented before implementation.
- **Architecture:** A lightweight MVVM-style separation using Activities/Fragments or equivalent screen controllers, ViewModels/state holders, repositories, and data sources.
- **Testing:** Unit tests for validation and calculations, repository/database tests where practical, and manual emulator test cases for navigation and failure states.
- **Advanced integration:** To be selected after the core workflow is understood. The candidate must contribute meaningful product value, include error handling, and be testable; simply displaying an AI response is not acceptable.

These are initial decisions, not permission to implement every technology at once. The project will prefer a working vertical slice over speculative complexity.

## 11. Acceptance criteria

### Authentication

- Given valid registration details, when the customer submits the form, an account is created and the customer reaches the authenticated destination.
- Given a duplicate email or invalid credentials, the app does not navigate to protected content and shows a useful error.
- Given an authenticated customer who restarts the app, the session is restored or the customer is asked to sign in again according to the provider result.

### Menu and basket

- Given available menu data, the customer can browse categories and see price and availability.
- Given a search term, only matching products are shown, with a clear empty state if there are no matches.
- Given an available item, adding it updates the basket quantity and total; removing the final item returns the basket to an empty state.

### Checkout and tracking

- Given a non-empty basket and valid checkout data, an order is persisted once and a confirmation reference is shown.
- Given an unavailable item or persistence failure, checkout is blocked or reported as failed without showing a false confirmation.
- Given a status change, the current-order view displays the new status and the customer can find the order in history after completion.

### Feedback and integration

- Given an eligible completed order, a customer can submit a rating within the permitted range and receives confirmation.
- Given the advanced integration is unavailable, the core customer workflow remains usable and shows a recoverable fallback.

## 12. Delivery plan

| Iteration | Date/target | Deliverable |
|---|---|---|
| 0 | 29 Oct 2026 | PRD v1, prioritised backlog, architecture decisions, repository, initial commit |
| 1 | 30 Oct–5 Nov | Authentication screens and working validation; initial menu/data layer; tutor review |
| 2 | 6–12 Nov | Menu, basket, checkout, order persistence/status flow; advanced integration; tests |
| 3 | 13–16 Nov | Feedback, notifications/status presentation, refinement, evaluation, evidence, demonstration |

The plan is subject to real stakeholder feedback and will be updated through the backlog and changelog.

## 13. Risks and mitigations

| Risk | Mitigation |
|---|---|
| Scope becomes too large for an individual project | Keep the Customer Application as the assessed boundary and stage admin features. |
| Authentication or cloud configuration blocks progress | Build behind a repository interface and use clearly labelled local test data where permitted. |
| Advanced integration becomes a disconnected demo | Define its user value and acceptance test before implementation. |
| Data inconsistency between basket and order | Snapshot item details at checkout and test totals/status transitions. |
| Coursework evidence is incomplete | Commit incrementally and update the change log, backlog, meeting records, and tests at each milestone. |
| GenAI use is not understood or disclosed | Maintain `docs/GENAI_USAGE.md` and independently review all incorporated work. |

## 14. Open decisions for stakeholder review

1. Confirm that the individual Customer Application scope is appropriate.
2. Confirm XML rather than Jetpack Compose for the UI.
3. Confirm Firebase Authentication plus Room as the initial technical route.
4. Select the advanced integration after the core workflow and available APIs are assessed.
5. Confirm whether the tutor prefers a seeded status simulator or a small test-only status control.

