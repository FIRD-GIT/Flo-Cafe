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
