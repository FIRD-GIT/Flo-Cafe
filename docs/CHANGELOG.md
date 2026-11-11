# Flo Cafe Change Log

This file records meaningful project changes. It is separate from the PRD: the PRD describes intended product behaviour, while this log records what actually changed over time.

## 2026-10-29

### Added

- Created the Flo Cafe repository structure.
- Added PRD v1 for an individual Customer Application.
- Added a prioritised backlog with authentication, menu, basket, checkout, order tracking, feedback, advanced integration, testing, and traceability stories.
- Added initial technical direction: Kotlin, XML UI, Firebase Authentication, Room/SQLite, and a separated application structure.
- Added a repository README explaining coursework alignment and documentation rules.
- Added a policy that client meeting notes contain only real tutor discussions.
- Expanded the PRD to include a limited Sub-admin workflow, coffee-cup order progress, completion notifications, and implementation evidence requirements.

### Decisions

- The first assessed scope is the Customer role rather than two complete applications.
- Admin processing is represented only through a controlled status simulation or test mechanism where needed to demonstrate the customer workflow.
- Advanced integration is deliberately not fixed to an unvalidated technology before the core workflow is understood.
- XML is retained over Jetpack Compose because it is directly inspectable, aligns with the supplied lab material, and makes the code-to-screen relationship easier to demonstrate.

### Evidence

- Source: CTEC3703 coursework specification and Lab 01 PRD guidance supplied on 29 October 2026.
- Initial commit: `docs: establish Flo Cafe PRD and project foundations`.

## Change record template

Future entries should include:

- Date
- Change request or stakeholder source
- Requirement/backlog IDs affected
- Decision and implementation summary
- Tests/evidence
- Commit or pull request reference

## 2026-11-04

### Added

- Added the initial Android application foundation using Kotlin, XML layouts, AndroidX, and compileSdk 33.
- Added the Firebase user profile model with an `isAdmin` boolean and role mapping to `ADMIN` or `CUSTOMER`.
- Added Firestore rules that prevent users from self-promoting to admin.
- Added a visible Firebase configuration state so the app does not claim Firebase is connected before `google-services.json` is supplied.

### Evidence

- Backlog: CAFE-01, CAFE-02, CAFE-03, CAFE-13, CAFE-15, CAFE-18.
- Source: `app/src/main/java/com/firdgit/flocafe/UserProfile.kt`, `UserRepository.kt`, and `firestore.rules`.
## 2026-11-04

- Made Google Services integration conditional on `app/google-services.json`, so the Android project remains buildable before Firebase project setup while enabling live Firebase configuration automatically once the file is added.
- Aligned AndroidX and Firebase dependency versions with the project's compile SDK 33 requirement so the debug build can validate without requiring a newer SDK.
- Added the first authentication flow: customer registration, email/password sign-in, persisted customer profiles, and role-aware status messaging.
- Added session restoration, sign-out, and explicit customer/sub-admin landing states for the Thursday demonstration flow.
- Added the first customer ordering slice with controlled menu data, add-to-basket actions, live totals, and checkout preview.
- Added Firestore order submission with customer ownership rules, pending status, immutable item snapshots, and failure feedback.
- Added customer order tracking and admin-only controlled status progression through Pending, Preparing, Ready for Collection, and Collected.
- Split authentication, customer ordering, and admin order management into separate Android pages.

## 2026-11-11

- Added the customer redesign with Home, Menu, Basket and Profile tabs, expanded menu categories, product imagery, item-specific customisation and labelled prototype nutrition estimates.
- Added consistent currency formatting and direct add-to-basket behaviour for fixed sweet treats.
- Replaced clipped category scrolling with a 2×2 category grid and category-to-filtered-menu navigation.
- Reduced product-action and basket-control dominance, added compact basket thumbnails and concise customisation summaries, and introduced consistent spacing across basket, order-status and Profile order-history cards.
- Updated the coursework evidence, backlog statuses, code justification and GenAI declaration to match the implemented customer experience.

### Evidence

- Commits: `a877530`, `106c99a`, `e7ce939`, `55b8afd`, `518bb4b`, `d766c46`, `3c4f2b1`.
- Validation: Firebase-enabled debug build and unit tests pass; the APK installs on `emulator-5554`.
