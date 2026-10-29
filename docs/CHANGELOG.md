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
