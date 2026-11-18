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

## 2026-11-15 — Supplied logo integration and authentication polish

- **Status:** Implemented and build-verified.
- **Requirement links:** PRD NFR-01, NFR-02, NFR-08; customer login and branding acceptance criteria.
- **Purpose and user value:** make the authentication surface use the supplied brand assets consistently and improve field affordance, error hierarchy and button emphasis.
- **Decision and rationale:** `horizontal_logo.png` is used above the Flo Cafe title, while `app_icon.png` is registered as the application/round icon and Android 12+ splash icon. Android XML has no CSS `filter: drop-shadow`; a subtle rounded translucent backplate provides the equivalent contrast treatment without inventing a logo.
- **Code location:** `activity_main.xml`, `MainActivity.kt`, `AndroidManifest.xml`, `values-v31/themes.xml`, `auth_logo_backplate.xml`, `auth_warning_background.xml`, `auth_outline_button.xml`, `ic_email.xml`, `ic_lock.xml`, and the supplied resources under `drawable-nodpi`.
- **Validation:** debug APK build and emulator installation are required after this change; live Firebase configuration remains separate from the visual login validation.
- **Known limitations:** the supplied message attachments are JPEG transcodes of the named PNG assets, so the repository stores converted PNG resources with the visible checkerboard pixels preserved from the supplied files; no unsupported transparency cleanup was invented.

### Follow-up asset revision

The later supplied PNG attachments are now used directly as `horizontal_logo.png` and `app_icon.png`. The login header has no background or border; its shadow is a subtle tinted duplicate image layer, which is the closest native XML equivalent to CSS `drop-shadow`. The login form now includes person, envelope and padlock icons, a solid Sign In CTA, outlined Register and demo actions, and no redundant signed-out status sentence.

## 2026-11-15 — Stakeholder improvements: live status, profiles, feedback and honest prototypes

- **Status:** Implemented and build-verified; live Firebase/security and Android permission evidence remains environment-dependent.
- **Requirement links:** PRD FR-08, FR-10, FR-11, FR-12, FR-15, FR-16, NFR-03, NFR-05; CAFE-10, CAFE-11, CAFE-12, CAFE-17, CAFE-24, CAFE-25.
- **Purpose and user value:** reduce refresh friction, keep profile identity consistent, make order progress visible from Home, provide a truthful advanced-feature prototype, and expose customer feedback to the authorised operational role.
- **Decision and rationale:** use Firestore `addSnapshotListener` for the existing customer order query rather than polling; update the existing `users/{uid}` document rather than creating duplicate profile data; use local Android notifications plus an in-app message rather than claiming FCM push delivery; keep the AI builder as editable suggestion text because no provider or menu-availability service is configured.
- **Code location:** `OrderRepository.kt`, `UserRepository.kt`, `FeedbackRepository.kt`, `CustomerActivity.kt`, `AdminActivity.kt`, `firestore.rules`, `activity_main.xml`, `auth_gradient.xml`, `auth_input_background.xml`, `flo_cafe_branding.png`, `strings.xml`, and `AndroidManifest.xml`.
- **Code-to-screen translation:** customer order snapshots rebuild Home/Profile state and show the coffee cup/progress banner; status transitions update the in-app message and attempt a local Android notification. Collected orders expose a review dialog, while Admin displays feedback from the protected `feedback` collection. Login uses the supplied combined branding asset, rounded fields and a display-name-only registration mode.
- **Validation:** `ANDROID_HOME="$HOME/Library/Android/sdk" ./gradlew testDebugUnitTest assembleDebug --no-daemon --console=plain` passed. The initial validation without an SDK path was blocked by the missing `ANDROID_HOME`, not by source errors.
- **Known limitations:** external push delivery is not configured; Android 13+ notification permission may suppress the local notification, with the in-app status remaining available. The AI builder is a transparent non-provider prototype and does not guarantee menu availability. Live Firestore rules, review reads and listener transitions require a configured Firebase project and synthetic accounts. Basket cup colour is represented through the selected customisation summary and product image; no remote product-image recolouring is claimed.
- **Commit reference:** to be added after the feature commit.

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

## 2026-11-12 — PRD testing traceability

- **Status:** Documentation aligned with supplied PRD guidance.
- **Requirement links:** PRD NFR-08, NFR-09; CAFE-13, CAFE-18.
- **Purpose and user value:** make it possible for a marker to follow each important requirement from the backlog to source code, automated/manual test coverage and remaining evidence.
- **Source used:** supplied `Flo-Cafe-PRD-Explanation.pdf`, plus the repository PRD and backlog.
- **Evidence:** `docs/testing/TRACEABILITY_MATRIX.md` maps automated and manual cases, reports the current results honestly, and lists the remaining Firebase evidence.
- **Known limitation:** no separate marking-rubric document was present in the supplied folders; the matrix therefore does not invent rubric criteria or claim live Firebase scenarios that have not been executed.

## 2026-11-12 — Assessment checklist testing alignment

- **Status:** Testing documentation aligned with the supplied teaching presentation, JUnit guide and Thursday assessment checklist.
- **Requirement links:** PRD NFR-01, NFR-05, NFR-07, NFR-08, NFR-09; CAFE-13, CAFE-18.
- **Evidence:** `docs/testing/ASSESSMENT_TEST_EVIDENCE.md` records unit, integration, UI/manual, boundary, unauthorised, failure, restart, accessibility and performance status using the checklist's D/IP/NS/N/A vocabulary.
- **Decision:** unrun Firebase, Espresso/instrumented and Android Profiler checks remain explicitly pending rather than being represented as passed.

## 2026-11-12 — Assessment guide submission alignment

- **Status:** The supplied `CTEC3703_Assessment_Guide_2026.pptx` was reviewed and converted into `docs/ASSESSMENT_SUBMISSION_READINESS.md`.
- **Evidence:** The guide's programming submission, testing, security/ethics and demo expectations now link to the repository's PRD, backlog, code justification, Firebase documentation, test plan and traceability matrix.
- **Decision:** SQLite is marked N/A because the app uses Firebase Auth/Firestore; the separate ethical case study and phase test are recorded as assessment deliverables rather than falsely treated as app features.

## 2026-11-15 — Ethics case study template alignment

- **Status:** Created `docs/CTEC3703_Flo_Cafe_Ethics_Report_Template_Aligned.docx` from the supplied 2026/27 case-study template.
- **Evidence:** The report contains the required seven numbered sections and template subheadings, a focused customer-role dilemma, Big Data factors, external/local/private AI comparison, inference-versus-training distinction, surveillance/autonomy analysis, two ethical frameworks, competing perspectives, UK GDPR/privacy/permission analysis, prioritised safeguards, conclusion, seven references, GenAI declaration and Appendices A–D.
- **Verification:** Main report prose was later revised to 1,100 words excluding section labels, references, appendices and the GenAI declaration, at the upper limit of the required 1,000 words ±10%. Implemented, proposed and simulated features are explicitly distinguished.
- **Limitation:** Student number, final personal wording, source verification and genuine tutor feedback must be completed by the student before submission; the fictional privacy contact must not be used for deployment.

## 2026-11-18 — Customer navigation and filling-cup UI refinement

- **Status:** Implemented and build/emulator verified.
- **Requirement links:** PRD FR-17, FR-22, NFR-01, NFR-02; CAFE-08, CAFE-16, CAFE-19.
- **Purpose and user value:** replace the text-only bottom bar with clear icon navigation, remove the basket purple placeholder, make the profile edit action less dominant and show active order progress at 25/50/75/100 without tinting the whole supplied cup asset.
- **Decision and rationale:** `BottomNavigationView` preserves the existing tab shell and selected state. The Home banner uses a standard Android horizontal `ProgressBar` with a custom layer-list track/fill beside the original cup asset. Basket cups no longer use `setColorFilter()`.
- **Code location:** `activity_customer.xml`, `CustomerActivity.kt`, `OrderProgress.kt`, `cup_progress_bar.xml`, `cup_progress_track.xml`, `cup_progress_fill.xml`, `customer_bottom_navigation.xml`, customer navigation icons and `activity_admin.xml`.
- **Validation:** the debug APK builds and 21 JVM tests pass. Emulator-5556 launched the updated app, completed customer login and showed the Pending state at 25%, icon bottom navigation and the supplied cup mark.
- **Known limitations:** detailed 50/75/100 screenshots require live order-status transitions; accessibility labels and formal contrast checks remain to be captured.

## 2026-11-18 — Basket, admin and drink-builder refinement

- **Status:** Implemented and build/emulator verified.
- **Purpose and user value:** remove the remaining purple basket placeholder, make sub-admin analytics and order actions easier to scan, and replace the outdated drink-builder dialog with a mobile-native bottom sheet.
- **Code location:** `basketLineCard` in `CustomerActivity.kt`, `showAiBuilderPrototype()` in `CustomerActivity.kt`, `activity_admin.xml`, `AdminActivity.kt`, and `strings.xml`.
- **Visible behavior:** Basket rows now show the product artwork without an extra colored cup block. Admin analytics appear in a two-column metric card and incoming orders appear in individual status cards with one next-action button. The drink builder opens as a bottom sheet with a live deterministic recommendation preview.
- **Validation:** Debug build and 21 JVM tests pass. Emulator screenshots verify the Basket, Profile, Menu, sub-admin analytics and bottom-sheet builder surfaces.

## 2026-11-18 — Menu search, Firebase analytics and rule-based drink builder

- **Status:** Implemented and unit/build verified; live Firebase analytics and admin-workflow screenshots remain to be captured.
- **Requirement links:** PRD FR-04, FR-12, NFR-05, NFR-07; CAFE-05, CAFE-12, CAFE-13.
- **Purpose and user value:** make menu discovery faster, give authorised sub-admin users a direct operational dashboard, and provide a transparent second advanced-feature prototype.
- **Decision and rationale:** menu search combines name, description and category filtering in pure repository logic. Firebase analytics is calculated from the existing Firestore order and feedback collections; it does not add unconfigured external services. The drink builder uses deterministic Kotlin rules with editable spinners and is explicitly described as non-AI.
- **Code location:** `MenuRepository.kt`, `CustomerActivity.kt`, `AnalyticsCalculator.kt`, `DrinkRecommendation.kt`, `AdminActivity.kt`, `activity_admin.xml`, `strings.xml`, and the focused JVM test classes.
- **Validation:** 21 JVM tests plus the debug build pass. The admin shows live order count, sales, collected count, average order, average rating and top item from Firestore.
- **Known limitations:** external push delivery is not configured; Firebase analytics currently reads stored orders and reviews rather than server-maintained counters; live multi-account screenshots and offline/restart evidence remain assessment tasks.

## 2026-11-15 — Stakeholder feedback incorporated into ethics evidence

- **Status:** The aligned ethics report was rewritten in a more natural, direct style and Appendix E now separates completed features, current limitations and proposed work.
- **Relevant implemented items:** Firestore order listeners for active customer sessions, persisted profile-name edits, the Home cup-progress banner, collected-order reviews with authorised-admin visibility, and local notification attempts.
- **Relevant limitations/proposals:** external push delivery while the app is closed is not configured, and the AI drink builder remains an editable prototype without an AI provider.
- **Ethical treatment:** status transparency, profile accuracy/correction, notification purpose, review access/retention, profiling/autonomy and human review are discussed. Logo, visual styling, basket cup colour and emulator inspection remain implementation/evidence tasks rather than separate ethics claims.
- **Verification:** Main report prose is now 1,100 words excluding section labels, references, appendices and the GenAI declaration. The report was rewritten in a more direct student voice after reviewing the attached detector feedback. The appendix labels implemented, limited and proposed behaviour explicitly.

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
