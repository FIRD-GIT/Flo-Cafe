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
