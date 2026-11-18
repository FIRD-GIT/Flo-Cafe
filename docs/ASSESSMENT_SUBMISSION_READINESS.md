# CTEC3703 submission readiness

This checklist is aligned with the supplied
`CTEC3703_Assessment_Guide_2026.pptx`, the Thursday checklist and the
testing guidance supplied with the project. It records project evidence only;
the student must still assemble and submit the final ZIP/report through the
required LearningZone route.

## Programming specification and demo

| Guide requirement | Status | Evidence or action |
|---|---:|---|
| Complete Android Studio Kotlin project | IP | Project is buildable; create the final ZIP without `google-services.json` unless assessor access instructions require a controlled copy |
| Technical report with seven sections and references | IP | Existing PRD, code justification and implementation records provide source material; assemble into the required report template |
| PRD and concise supporting evidence as appendices | D/IP | `docs/PRD.md`, testing plans, traceability and change records exist; export final appendices |
| SQLite database file where applicable | N/A | Flo Cafe uses Firebase Authentication/Firestore rather than SQLite |
| GitHub repository with PRD, backlog and development evidence | D | Repository contains PRD, backlog, implementation log, changelog, GenAI declaration and testing evidence |
| Firebase project details and assessor access where used | IP | Firebase configuration works locally; prepare assessor-safe project details and rules/access instructions without exposing secrets |
| Login and role access | IP | Implemented; live customer/sub-admin evidence remains to capture |
| Menu, orders and notifications/status | IP | Implemented customer menu, basket, order history and status display; live Firebase workflow evidence remains |
| Feedback, if applicable | D/IP | Collected-order feedback is implemented and role-visible; live submission/admin evidence remains to capture |
| Storage and SDK/API use | D | Firebase Auth and Firestore usage is documented in the code and data model |
| Validation and testing | IP | 21 JVM tests pass; manual, Firebase, instrumented/UI and performance evidence remain partly outstanding |
| Security and ethics | IP | Rules, ownership, role checks and GenAI declaration exist; final report must explain privacy, data protection and limitations |
| Demo readiness | IP | Prepare a repeatable customer workflow, one rejected input/access attempt, stored data, code explanation and test evidence |

## Testing evidence required by the guide

The guide specifically names invalid input and boundaries, unit/UI tests,
use-case and user tests, expected/actual results, fixes and retests. The
current project maps these to:

- `docs/testing/MANUAL_TEST_PLAN.md` — executable cases MT-01 to MT-15.
- `docs/testing/TRACEABILITY_MATRIX.md` — requirement-to-code/test/evidence
  links.
- `docs/testing/ASSESSMENT_TEST_EVIDENCE.md` — D/IP/NS/N/A status.
- `app/src/test/java/com/firdgit/flocafe/` — local JUnit business-rule tests.

Unrun checks remain labelled pending. In particular, capture actual results for
Firebase persistence, role restriction, duplicate protection, failure/offline
behaviour, restart behaviour, UI/manual screenshots and Android Profiler
performance evidence before marking the relevant rows done.

## Other assessment components

The guide also lists a separate 20% ethical case study and a 30% phase test.
Those are not app implementation features. The final submission should
separately prepare:

1. The case study's seven sections, two ethical frameworks, five credible
   sources, references and GenAI declaration.
2. Phase-test revision across Android lifecycle/components, Kotlin, APIs,
   storage, validation/security, unit/UI testing, Big Data 8Vs and responsible
   AI/data use.
