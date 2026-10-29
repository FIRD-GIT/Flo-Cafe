# Flo Cafe Product Backlog

Status values: **To do**, **In progress**, **Blocked**, **Done**.  
Priorities follow the coursework lab convention: **Must**, **Should**, **Could**.

| ID | User story | Acceptance criteria | Priority | Owner | Status |
|---|---|---|---|---|---|
| CAFE-01 | As a customer, I want to register with email/password so that I can use the app securely. | Valid details create an account; invalid or duplicate details show an error; no protected navigation occurs on failure. | Must | Firdaos | To do |
| CAFE-02 | As a customer, I want to sign in and log out so that I can access my account safely. | Valid credentials reach the customer destination; invalid credentials remain on login; logout returns to login. | Must | Firdaos | To do |
| CAFE-03 | As a customer, I want my session handled when the app restarts so that I do not lose my authenticated state unexpectedly. | Auth state is checked on launch; protected content is not shown to unauthenticated users. | Must | Firdaos | To do |
| CAFE-04 | As a customer, I want to browse café products so that I can decide what to order. | Products show name, description, category, price, and availability; loading, empty, and error states are visible. | Must | Firdaos | To do |
| CAFE-05 | As a customer, I want to search or filter the menu so that I can find products quickly. | Search/filter results update correctly; no-match state is clear. | Must | Firdaos | To do |
| CAFE-06 | As a customer, I want to add, update, and remove basket items so that I can control my order. | Quantity and total recalculate correctly; removing the final item shows an empty basket. | Must | Firdaos | To do |
| CAFE-07 | As a customer, I want to submit my basket as an order so that the café can prepare it. | Empty baskets cannot be submitted; a successful submission creates one order and shows a reference; failures do not show false success. | Must | Firdaos | To do |
| CAFE-08 | As a customer, I want to see my current order status so that I know when to collect it. | Pending, Preparing, Ready for Collection, and Collected are represented clearly. | Must | Firdaos | To do |
| CAFE-09 | As a customer, I want to view previous orders so that I can review what I purchased. | Completed orders are listed with date, items, total, and status. | Should | Firdaos | To do |
| CAFE-10 | As a customer, I want to rate a completed order so that I can provide feedback. | Only eligible orders can be rated; rating range and comment validation work; confirmation is shown. | Should | Firdaos | To do |
| CAFE-11 | As a customer, I want status changes highlighted so that I notice when my order is ready. | A refreshed or simulated status change is visible without requiring a misleading success state. | Should | Firdaos | To do |
| CAFE-12 | As a customer, I want a useful advanced feature so that the app provides more than basic CRUD. | Selected integration has a defined user value, failure fallback, test case, and documentation. | Must | Firdaos | To do |
| CAFE-13 | As a developer, I want core validation and repository tests so that regressions are detected. | Test cases cover authentication validation, totals, order persistence, and failure paths. | Must | Firdaos | To do |
| CAFE-14 | As a stakeholder, I want changes recorded so that decisions remain traceable. | Meaningful changes appear in the changelog and are linked to backlog/meeting evidence. | Must | Firdaos | Done |
| CAFE-15 | As a sub-admin, I want to view incoming orders and update controlled statuses so that customers see accurate preparation progress. | Only a permitted sub-admin can access the workflow; status updates are validated, persisted, and visible to the customer. | Must | Firdaos | To do |
| CAFE-16 | As a customer, I want a coffee-cup indicator to fill while my order is prepared so that progress is understandable at a glance. | The cup maps to defined order statuses, fills progressively, and is full when the order is ready or complete. | Must | Firdaos | To do |
| CAFE-17 | As a customer, I want a completion notification so that I know when to collect my order. | A notification or documented in-app equivalent is triggered when the order reaches the completion threshold and remains usable if notification delivery fails. | Should | Firdaos | To do |
| CAFE-18 | As a developer, I want an implementation evidence log so that every feature can be explained from requirement through code to mobile behaviour. | Each entry records purpose, user value, requirement/backlog link, source location, app behaviour, tests/evidence, and decision rationale. | Must | Firdaos | To do |

## Definition of done

A backlog item is complete when the implementation is understandable, tested at an appropriate level, documented, and committed with a descriptive message. If stakeholder feedback changes the requirement, the original and revised intent must be traceable.
