# Flo Cafe Implementation and Evidence Log

This log records how each implemented feature moves from a requirement to code and then to visible mobile behaviour. I will update it from the first implementation commit onward.

## Required entry format

Each feature entry should explain:

- **Date and status:** when the work was started or completed.
- **Requirement links:** PRD and backlog IDs.
- **Purpose and user value:** why the feature exists and who uses it.
- **Decision and rationale:** why the implementation approach was selected.
- **Code location:** package, class, layout, resource, or configuration files.
- **Code-to-screen translation:** how the Kotlin/data logic produces the visible Android screen or interaction.
- **Validation:** unit tests, UI tests, manual test steps, screenshots, or other evidence.
- **Known limitations:** incomplete behaviour, test-only controls, seeded data, or follow-up work.
- **Commit reference:** the commit that introduced or changed the feature.

## Initial documentation decisions

### XML user interface

- **Status:** Selected for the initial implementation.
- **Requirement links:** PRD technical direction; CAFE-18.
- **Purpose and user value:** provide a clear, inspectable customer and sub-admin interface for the coursework demonstration.
- **Rationale:** XML is demonstrated in the supplied lab material and makes the relationship between layout files, view IDs, Kotlin event handling, and rendered screens straightforward to explain. Jetpack Compose remains a possible future revision only if evidence supports changing the route.
- **Code location:** Android layout and resource files to be added under `app/src/main/res/layout/` and `app/src/main/res/values/`.
- **Code-to-screen translation:** layout XML defines the view hierarchy and IDs; Kotlin screen controllers and state holders find those views, bind data, respond to actions, and update the displayed state.
- **Validation:** to be recorded when the Android project is created.

### Customer and limited Sub-admin roles

- **Status:** Included in PRD scope; implementation pending.
- **Requirement links:** FR-01, FR-02, FR-14, PRD users and roles; CAFE-01, CAFE-02, CAFE-15.
- **Purpose and user value:** allow customers to order while giving authorised café staff only the controls needed to maintain availability and progress orders.
- **Rationale:** the limited role supports a believable end-to-end workflow without expanding the coursework into a complete administration platform.
- **Code location:** to be recorded for authentication, role data, navigation guards, sub-admin screens, and repository operations.
- **Code-to-screen translation:** authenticated role state will determine the destination screen and permitted actions; repository updates will drive the customer order status.
- **Validation:** role access, rejected customer access, status updates, and persistence tests to be recorded.

### Coffee-cup preparation progress and completion notification

- **Status:** Included in PRD scope; implementation pending.
- **Requirement links:** FR-15, FR-16; CAFE-16, CAFE-17.
- **Purpose and user value:** give the customer an immediate visual explanation of order progress and a clear signal when collection is possible.
- **Rationale:** a filling cup is a simple, recognisable visual metaphor that maps directly to the controlled order states.
- **Code location:** to be recorded for the order-state mapping, progress drawable/layout, notification code, and current-order screen.
- **Code-to-screen translation:** the stored order status maps to a progress value and visual cup state; a transition to the completion threshold updates the cup to full and triggers a notification or documented in-app fallback.
- **Validation:** each status mapping, full-cup completion state, notification delivery/failure path, and manual screen evidence to be recorded.

## Feature entries

Add dated entries below as implementation begins. Do not mark a feature complete until the code location, visible behaviour, and validation evidence are recorded.

## 2026-11-11 — Spaced categories and product-specific customisation

- **Status:** Implemented and build-verified.
- **Requirement links:** PRD FR-03, FR-18, NFR-01; CAFE-04, CAFE-21.
- **Purpose and user value:** improve category readability and ensure customers only see customisation choices that make sense for the selected item.
- **Decision and rationale:** The first refinement used separated Home actions and spaced Menu chips so long labels did not overlap. That interim layout was superseded by the later 2×2 category grid documented below. Each customisable product declares its own groups: drinks can expose size, milk, shots, sweetness, syrup, ice or toppings; sandwiches expose preparation and ingredient choices such as extra cheese or no tomato; fixed sweet treats continue to add directly without a customisation prompt.
- **Code location:** `MenuItem.kt`, `MenuRepository.kt`, `CustomerActivity.kt`, and `MenuRepositoryTest.kt`.
- **Code-to-screen translation:** the product dialog is generated from the selected item's `customisationGroups`, rather than displaying irrelevant fields such as milk or espresso shots for a sandwich.
- **Validation:** `assembleDebug` and `testDebugUnitTest` pass; emulator UI inspection confirmed the spaced category labels and expanded item-specific menu model.
- **Known limitations:** customisation options are currently local coursework data and use single-choice selectors; multi-select ingredient editing can be added if the product requirements expand.

## 2026-11-11 — Friendly order history and visual flow

- **Status:** Implemented and build-verified.
- **Requirement links:** PRD FR-08, FR-09, NFR-01, NFR-02; CAFE-08, CAFE-09, CAFE-18.
- **Purpose and user value:** make previous orders understandable at a glance and keep technical identifiers out of the primary customer-facing summary.
- **Decision and rationale:** order cards now show the first ordered product image, a short `Order #ABC123` reference, item names/quantities, status, date and total. Tapping a card opens the detailed items/customisations, total and internal Firestore reference. Rounded cards and buttons create a more continuous visual hierarchy than the previous flat vertical blocks.
- **Code location:** `OrderRepository.kt`, `CustomerActivity.kt`, and `strings.xml`.
- **Code-to-screen translation:** Firestore item snapshots are parsed into order item summaries; the Profile order history and Home status cards render the customer summary, while the detail dialog exposes the underlying order information on demand.
- **Validation:** Firebase-enabled `assembleDebug` and `testDebugUnitTest` pass. The order card/detail code compiles against the existing Firestore order schema.
- **Known limitations:** a live order-history screenshot requires a Firebase account with at least one stored order.

## 2026-11-11 — Pricing consistency and conditional customisation

- **Status:** Implemented and build-verified.
- **Requirement links:** PRD FR-03, FR-05, FR-06, FR-18; CAFE-04, CAFE-06, CAFE-21.
- **Purpose and user value:** make price presentation consistent and prevent customers from being sent through an unnecessary customisation flow for fixed products.
- **Decision and rationale:** a shared formatter now displays fractional prices with exactly two decimal places (`£3.20`, `£2.60`) while whole-pound amounts display without redundant decimals (`£3`). Menu items explicitly declare whether meaningful customisation exists. Fixed sweet treats add directly to the basket; drinks and sandwiches retain the customisation form.
- **Code location:** `PriceFormatter.kt`, `MenuItem.kt`, `MenuRepository.kt`, `CustomerActivity.kt`, and `PriceFormatterTest.kt`.
- **Code-to-screen translation:** product cards use the shared formatter and conditionally show `Add to basket` or `Customise`. Sandwich options include `Extra cheese` and `No tomato`.
- **Validation:** pricing and menu customisation unit tests pass; the Firebase-enabled debug build succeeds and the emulator displays prices such as `£3.20` and `£2.60`.
- **Known limitations:** menu pricing remains local coursework data and is not yet remotely managed.

## 2026-11-11 — Customer tab redesign and expanded catalogue

- **Status:** Implemented and build-verified; live authenticated visual evidence remains to be captured.
- **Requirement links:** PRD FR-03, FR-05, FR-06, FR-17, FR-18, FR-19, FR-20; CAFE-19, CAFE-20, CAFE-21, CAFE-22, CAFE-23.
- **Purpose and user value:** move beyond the original prototype by separating the customer journey into understandable destinations, increasing menu choice, and making customisation and nutritional context visible before checkout.
- **Decision and rationale:** retain one customer activity and use a tab shell with independently rendered Home, Menu, Basket and Profile content. This keeps the current Firebase repository and duplicate-order safeguards stable while allowing the UI to evolve without introducing a navigation dependency. Product detail uses a controlled dialog so unsupported combinations cannot be submitted.
- **Code location:** `CustomerActivity.kt`, `MenuItem.kt`, `MenuRepository.kt`, `activity_customer.xml`, `strings.xml`, and the new product vector drawables.
- **Code-to-screen translation:** the Home tab displays `Welcome, {display name}`, featured items, category shortcuts, favourites and order status. Menu cards show original imagery, price, calories and a Customise action. The customisation form captures size, milk, shots, temperature, sweetness, syrups, toppings and whipped cream into a basket summary. Basket and Profile are separate destinations with quantity controls, totals, order history, preferences and safe sign out.
- **Validation:** Firebase-enabled `assembleDebug` and `testDebugUnitTest` passed. The APK was installed on the configured emulator and UI automation confirmed Home, Menu, Basket and Profile tabs, all four menu categories, the Customise dialog, all requested option groups, and the explicit nutrition-estimate notice. The no-Firebase build also exposed the local demo entry point. Authenticated checkout and duplicate-order evidence still requires a configured test account and deployed Firestore rules.
- **Known limitations:** nutrition values are AI-generated estimates and are not authoritative; profile editing and preferences are currently local UI state; menu data remains local rather than Firestore-managed.
- **Commit reference:** `a877530` (`Customer tab redesign`).

## 2026-11-11 — Veracity and usability refinement

- **Status:** Implemented and build-verified.
- **Requirement links:** PRD FR-03, FR-06, FR-07, FR-08, NFR-01, NFR-04; CAFE-04, CAFE-06, CAFE-07, CAFE-08, CAFE-15, CAFE-18.
- **Purpose and user value:** respond to client feedback by making product discovery clearer, reducing accidental sign-out, supporting meaningful product choices, and preventing a recent matching order from being submitted without confirmation.
- **Decision and rationale:** group the local menu by category, show a stable product row with a product illustration, expose controlled customisation choices, and update quantity labels rather than creating duplicate product rows. A deterministic order fingerprint compares item, quantity and customisation values within a ten-minute window; a matching order triggers confirmation rather than silent rejection.
- **Code location:** `MenuItem.kt`, `MenuRepository.kt`, `CustomerActivity.kt`, `OrderRepository.kt`, `activity_customer.xml`, `strings.xml`, and product vector drawables.
- **Code-to-screen translation:** customers see Coffee and Bakery sections, product imagery, descriptions, customisation selectors, an add action, and a quantity indicator. The basket summary lists the selected customisations. Checkout checks for a recent matching order and asks the customer to confirm. Sign-out is visually secondary and requires confirmation. Customer orders now render status cards with a four-step preparation progress bar and a clear ready/collected message.
- **Validation:** `./gradlew :app:assembleDebug` succeeded; the APK installed successfully on the Android emulator; UI accessibility text was inspected with `uiautomator`.
- **Known limitations:** menu data remains local and product images are vector illustrations rather than remote photographs. The duplicate check depends on the stored fingerprint and skips legacy orders created before this field existed.
- **Commit reference:** to be added after the next feature commit.

## 2026-11-11 — Eight Vs of Big Data mapping

- **Volume:** the current prototype intentionally uses a small dataset; the order repository and Firestore structure provide a clear path to multi-order and multi-branch evidence without claiming production scale.
- **Velocity:** order status changes move through a controlled sequence and are reloaded for the customer, showing how operational data changes over time.
- **Variety:** account profiles, order item snapshots, categories, customisations and status values are handled as different data types.
- **Veracity:** item snapshots preserve what was ordered, status values are allow-listed, duplicate fingerprints flag a recent matching order, and Firestore rules protect ownership and totals.
- **Value:** the data directly supports accurate checkout, preparation and collection decisions rather than unnecessary profiling.
- **Variability:** menu availability, quantities, customisations and preparation states can differ between orders.
- **Visualisation:** category headings, product illustrations, quantity indicators, basket summaries and four-step cup-progress bars make data understandable to the user.
- **Vulnerability:** customer access is limited to owned orders, admin operations are role-protected, Firebase handles passwords, and optional location/photo/AI data is not collected by this release.

## 2026-11-11 — Category grid and compact customer actions

- **Status:** Implemented, tested and pushed.
- **Requirement links:** PRD FR-17, FR-21, FR-22, NFR-01, NFR-02; CAFE-19, CAFE-20, CAFE-21, CAFE-23.
- **Purpose and user value:** remove clipped horizontal category controls, make the main customer actions easier to scan, and reduce visual crowding in the basket.
- **Decision and rationale:** Home and Menu use a 2×2 category grid; category selection opens the Menu tab filtered to the selected section. Product cards use a primary add action with a smaller secondary customisation action. Basket rows use product thumbnails, concise detail text and a compact quantity stepper so controls support the item rather than dominating it.
- **Code location:** `CustomerActivity.kt`, `MenuItem.kt`, `MenuRepository.kt`, and `strings.xml`.
- **Code-to-screen translation:** category buttons are rendered in a `GridLayout`, selected categories are stored in `selectedMenuCategory`, and the Menu content is rebuilt from the matching catalogue group. Shared button helpers and spacing parameters create consistent primary, secondary and compact control treatments.
- **Validation:** `./gradlew :app:assembleDebug :app:testDebugUnitTest --no-daemon` passed; `git diff --check` passed; the APK installed successfully on `emulator-5554`.
- **Known limitations:** menu content and nutrition remain local coursework data; final visual judgement still depends on the target emulator dimensions.
- **Commit reference:** `518bb4b` (`Refine customer menu spacing`).

## 2026-11-11 — Basket, status and order-history spacing

- **Status:** Implemented, tested and pushed.
- **Requirement links:** PRD FR-22, NFR-01, NFR-02; CAFE-06, CAFE-08, CAFE-09, CAFE-18.
- **Purpose and user value:** create a consistent visual rhythm across basket rows, order-status cards and Profile order history.
- **Decision and rationale:** use explicit vertical margins between repeated cards and sections, reduce primary button height, distinguish the clear-basket action as secondary, and replace wide quantity pills with compact square controls that retain a usable touch area.
- **Code location:** `CustomerActivity.kt`.
- **Code-to-screen translation:** `spacedParams` establishes reusable vertical rhythm; `compactControlButton` styles quantity actions; order cards and basket rows receive explicit separation; shared title/body/button helpers preserve hierarchy across Home, Basket and Profile.
- **Validation:** `./gradlew :app:assembleDebug :app:testDebugUnitTest --no-daemon` passed; `git diff --check` passed; the APK installed successfully on `emulator-5554`.
- **Commit references:** `d766c46` (`Improve basket spacing and controls`) and `3c4f2b1` (`Space order history cards`).

## 2026-11-12 — Testing evidence and manual test plan

- **Status:** Automated coverage expanded; Firebase end-to-end evidence remains to be captured.
- **Requirement links:** PRD NFR-01, NFR-05, NFR-08; CAFE-13, CAFE-18.
- **Purpose and user value:** make the testing position explicit and provide repeatable evidence for validation, calculations, navigation, persistence and failure handling.
- **Decision and rationale:** extract pure authentication validation and basket-total calculations so they can be tested without Firebase or Android UI dependencies. Keep emulator and live Firebase scenarios in a separate manual test plan because they require device state, configured services and synthetic accounts.
- **Code location:** `InputValidation.kt`, `BasketCalculatorTest.kt`, `InputValidationTest.kt`, existing menu/price/fingerprint tests, and `docs/testing/MANUAL_TEST_PLAN.md`.
- **Validation:** `assembleDebug` and `testDebugUnitTest` pass; the generated HTML report is under `app/build/reports/tests/testDebugUnitTest/`.
- **Known limitations:** repository persistence, role restrictions, duplicate protection and network failure paths still need captured live Firebase test evidence.

## 2026-11-04 — Firebase user-role foundation

- **Status:** Foundation implemented; Firebase project configuration and emulator verification remain pending.
- **Requirement links:** PRD FR-01, FR-02, FR-14; CAFE-01, CAFE-02, CAFE-03, CAFE-15, CAFE-18.
- **Purpose and user value:** establish the persisted user identity and admin boundary needed before customer and sub-admin features are built.
- **Decision and rationale:** store `isAdmin` in each Firestore `users/{uid}` document. Missing or false values resolve to customer access; Firestore rules prevent self-promotion.
- **Code location:** `UserProfile.kt`, `UserRepository.kt`, `MainActivity.kt`, `firestore.rules`.
- **Code-to-screen translation:** the launcher screen checks whether Firebase is configured and shows a configuration state rather than a false connected state. Once Firebase is configured, the repository maps `isAdmin` to the application role.
- **Validation:** The project is configured to build without `google-services.json`; Firestore rules require Firebase Emulator Suite or a configured project for runtime verification.
- **Known limitations:** authentication screens, admin navigation, Firebase project configuration, and emulator evidence are follow-up work.
- **Commit reference:** to be added after validation.

## 2026-11-04 — Authentication entry flow

- **Status:** Implemented; live Firebase testing remains pending until project configuration is supplied.
- **Requirement links:** PRD FR-01, FR-02; CAFE-01, CAFE-02.
- **Purpose and user value:** provide the first usable account entry point for customers and establish the role lookup used by later navigation.
- **Code location:** `MainActivity.kt`, `UserRepository.kt`, `activity_main.xml`, and `strings.xml`.
- **Code-to-screen translation:** the launcher shows email/password controls and sign-in/registration actions when Firebase is configured. Without configuration, the same controls remain visible but disabled with an explicit setup message.
- **Validation:** debug build and emulator launch verified; live account creation and role lookup require `google-services.json` and Firebase Authentication.
- **Known limitations:** admin assignment remains a trusted Firestore operation; customer navigation and logout are follow-up work.

## 2026-11-04 — Role landing and session controls

- **Status:** Implemented; live Firebase testing remains pending until project configuration is supplied.
- **Requirement links:** PRD FR-02, FR-14; CAFE-02, CAFE-15.
- **Purpose and user value:** make the authenticated customer/sub-admin distinction visible and provide a safe sign-out path.
- **Code location:** `MainActivity.kt`, `UserRepository.kt`, `activity_main.xml`, and `strings.xml`.
- **Code-to-screen translation:** an existing Firebase session is restored on launch, the user sees the role-specific landing message, and sign-out returns to the unauthenticated state.
- **Validation:** debug build and emulator launch verified; role-specific live validation requires Firebase accounts and Firestore data.
- **Known limitations:** full customer menu and sub-admin order controls are the next feature slice.

## 2026-11-04 — Customer menu and basket preview

- **Status:** Implemented; Firebase-backed menu and order persistence remain follow-up work.
- **Requirement links:** PRD FR-03, FR-05, FR-06; CAFE-04, CAFE-06, CAFE-07.
- **Purpose and user value:** demonstrate the core customer ordering interaction before introducing checkout persistence.
- **Code location:** `MenuItem.kt`, `MenuRepository.kt`, `MainActivity.kt`, and `activity_main.xml`.
- **Code-to-screen translation:** authenticated customers see available products, add items to a basket, receive a recalculated quantity/total, and can open a checkout preview. Admins do not receive customer ordering controls.
- **Validation:** debug build and emulator installation are required after this change; live Firebase menu/order persistence is not yet implemented.
- **Known limitations:** menu data is controlled local demo data; checkout does not yet create a Firestore order.

## 2026-11-04 — Firestore order submission

- **Status:** Implemented; live submission requires Firebase Authentication, Firestore, and deployed rules.
- **Requirement links:** PRD FR-07, FR-08; CAFE-07, CAFE-08, CAFE-15.
- **Purpose and user value:** allow an authenticated customer to submit a non-empty basket and receive a stable order reference.
- **Code location:** `OrderRepository.kt`, `UserRepository.kt`, `MainActivity.kt`, and `firestore.rules`.
- **Code-to-screen translation:** Submit order validates the basket, writes a customer-owned pending order with item snapshots and total, clears the basket only after success, and shows a recoverable error on failure.
- **Validation:** debug build and emulator installation are required after this change; live persistence requires Firebase Console setup and deployed rules.
- **Known limitations:** order status updates, customer order history, and admin controls are follow-up work.

## 2026-11-04 — Order tracking and admin status controls

- **Status:** Implemented; live validation requires Firebase Authentication, Firestore, and deployed rules.
- **Requirement links:** PRD FR-08, FR-14, FR-15; CAFE-08, CAFE-15, CAFE-16.
- **Purpose and user value:** connect customer order visibility to the limited sub-admin workflow needed for the Thursday demonstration.
- **Code location:** `OrderRepository.kt`, `MainActivity.kt`, `activity_main.xml`, and `firestore.rules`.
- **Code-to-screen translation:** customers see their order references, totals, and statuses; sub-admins see incoming orders and can advance each order through the controlled status list.
- **Validation:** debug build and emulator installation are required after this change; live role and rules testing remains pending Firebase Console activation.
- **Known limitations:** coffee-cup visual progress, notifications, previous-order suggestions, and AI analysis remain follow-up work.

## 2026-11-04 — Firebase Console validation

- **Status:** Firebase Authentication Email/Password and Firestore setup completed by the project owner; live account and order-flow evidence remains to be captured.
- **Requirement links:** PRD NFR-03, NFR-04, NFR-08; CAFE-13, CAFE-14, CAFE-18.
- **Purpose and user value:** move the application from build-only Firebase integration to a real project with enforceable authentication and Firestore rules.
- **Code location:** `app/google-services.json` (local/ignored), `firestore.rules`, `docs/CODE_JUSTIFICATION.md`.
- **Code-to-screen translation:** the configured app can now authenticate users, create protected profiles, submit customer-owned orders, and expose admin-only status operations when the live accounts exist.
- **Validation:** the Android build has passed with Google Services processing; the remaining evidence is a manual customer/admin end-to-end test in the emulator.
- **Known limitations:** no live account credentials or screenshots are stored in the repository.

## 2026-11-04 — Separate authenticated pages

- **Status:** Implemented; live role navigation still requires the configured test accounts.
- **Requirement links:** PRD FR-01, FR-02, FR-03, FR-14; CAFE-01, CAFE-02, CAFE-04, CAFE-15.
- **Purpose and user value:** keep authentication separate from the actual customer and sub-admin workflows so users see a normal post-login page rather than a combined login/menu screen.
- **Code location:** `MainActivity.kt`, `CustomerActivity.kt`, `AdminActivity.kt`, their XML layouts, and `AndroidManifest.xml`.
- **Code-to-screen translation:** successful authentication and restored sessions now open a customer menu page or admin orders page. Each role has its own logout action that returns to the authentication page.
- **Validation:** debug build, emulator installation, and role navigation must be verified after this change.
- **Known limitations:** visual styling remains intentionally basic; the focus is workflow separation.
