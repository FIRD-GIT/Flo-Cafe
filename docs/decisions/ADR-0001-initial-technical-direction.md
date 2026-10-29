# ADR-0001: Initial technical direction

**Date:** 29 October 2026  
**Status:** Proposed for stakeholder review

## Context

The coursework requires an Android application written in Kotlin, a suitable user interface, database integration, authentication, testing, and at least one meaningful advanced integration. Lab 01 asks for a concise PRD, backlog, and an initial authentication journey.

## Decision

Flo Cafe will initially use:

- Kotlin in Android Studio.
- XML layouts and AndroidX components for the UI.
- Firebase Authentication for email/password identity.
- Room/SQLite for structured local café data and caching.
- A separated UI/state/repository/data-source structure.

The first delivery targets the individual Customer role. A complete admin application is not part of this initial boundary.

## Rationale

This route is understandable for me to explain, supports the lab's authentication requirements, avoids storing plaintext passwords, and keeps local café data separate from identity management. I selected XML because the supplied lab material demonstrates that route and it supports a clear, inspectable screen structure.

## Consequences

- Firebase configuration and security rules must be handled without committing secrets.
- Connectivity failures need explicit UI states.
- The repository boundary allows local test data while backend work is being configured.
- The advanced integration must be selected and justified separately; it must not become a disconnected demo.

## Revisit conditions

Revisit this decision if tutor feedback, available infrastructure, or testing shows that a different UI or persistence route is more appropriate. Any change must be recorded in a new ADR and the changelog.
