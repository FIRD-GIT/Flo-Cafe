# Flo Cafe

Flo Cafe is an Android Kotlin customer-ordering application for the CTEC3703 Smart Cafe coursework. The project is being developed incrementally and documented so that requirements, decisions, feedback, implementation, and testing remain traceable.

## Current milestone

**Milestone:** Lab 01 — PRD, backlog, architecture decisions, repository setup  
**Date:** 29 October 2026  
**Scope:** Customer Application with a limited Sub-admin workflow

The current milestone is intentionally limited to requirements and project foundations. Application implementation will be added in later iterations.

## Repository guide

| Location | Purpose |
|---|---|
| `docs/PRD.md` | Product requirements and acceptance criteria |
| `docs/BACKLOG.md` | Prioritised user stories and delivery status |
| `docs/CHANGELOG.md` | Dated record of project changes |
| `docs/IMPLEMENTATION_LOG.md` | Feature-by-feature record of purpose, code location, app behaviour, tests, and rationale |
| `docs/CLIENT_MEETINGS.md` | Tutor/client meeting records; only real meetings are recorded |
| `docs/decisions/` | Architecture and technical decision records |
| `docs/testing/` | Test plans and evidence added during implementation |
| `docs/GENAI_USAGE.md` | Truthful record of any generative-AI assistance |
| `app/` | Android application source, to be added in the implementation milestone |

## Coursework alignment

- Kotlin is the mandatory programming language.
- The first assessed product scope is the Customer role with a limited Sub-admin workflow supporting order and menu operations.
- The app will demonstrate registration, login, menu browsing, basket management, checkout, order tracking, feedback, and notifications.
- Order processing may be performed through the limited Sub-admin workflow, controlled test data, or a simple status-update mechanism because this is an individual project.
- Advanced integration will be implemented only after the core workflow is working and testable.

## Development rules

1. Do not commit passwords, API keys, Firebase configuration secrets, or personal data.
2. Record meaningful changes in `docs/CHANGELOG.md`.
3. Record tutor feedback only in `docs/CLIENT_MEETINGS.md` after the meeting has happened.
4. Link significant requirement changes to a backlog item and a commit or pull request.
5. Review and understand all code before using it in assessed work.

## Local Android Studio setup

1. Open `/Users/firdab/Desktop/CafeMobileApp` in Android Studio.
2. Allow Android Studio to generate or download the Gradle wrapper when prompted.
3. Sync Gradle and run the app on an Android emulator. The app can build in offline/demo mode without Firebase configuration.
4. To connect Firebase, add the project's `google-services.json` to `app/`. This file is intentionally ignored by Git; the Google Services plugin is enabled automatically when the file exists.
5. Deploy `firestore.rules` to the Firebase project or run them in the Firebase Emulator Suite before testing role access.
