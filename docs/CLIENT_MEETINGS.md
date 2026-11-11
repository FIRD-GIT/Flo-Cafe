# Client and Tutor Meeting Records

Only meetings and feedback that actually occur are recorded here. No fictional client feedback is included.

## Meeting record template

### YYYY-MM-DD — [meeting or lab title]

| Field | Record |
|---|---|
| Features demonstrated | To be completed after the meeting |
| Feedback received | To be completed after the meeting |
| New or modified requirements | To be completed after the meeting |
| Issues or challenges | To be completed after the meeting |
| Agreed actions and priorities | To be completed after the meeting |
| Progress against previous actions | To be completed after the meeting |
| Evidence | Link to relevant issue, commit, test, or screenshot |

### 2026-11-05 — Thursday prototype review

| Field | Record |
|---|---|
| Features demonstrated | Customer registration/sign-in, menu, basket, checkout, Firebase order persistence, admin status progression, and the complete Pending → Preparing → Ready for Collection → Collected workflow. |
| Feedback received | Treat the current release as a functional prototype and elevate usability for the next checkpoint. Apply the Big Data **Veracity** concept by checking for a recent matching order before silently accepting a duplicate. Make sign-out harder to mis-press, categorise products logically, keep quantity changes compact, add product imagery, and support product customisation such as less sugar. Review established coffee apps such as Costa and Greggs for relevant interaction ideas. |
| New or modified requirements | Add a recent duplicate-order confirmation; improve customer menu structure and customisation; improve sign-out safety; document the eight Vs using implemented evidence rather than claiming unsupported scale. |
| Issues or challenges | The prototype screen was functional but visually dense and the local menu did not yet expose categories, images, or customisation. |
| Agreed actions and priorities | Implement the veracity check and customer-facing usability refinements before the next Thursday checkpoint; retain the existing Firebase/security boundary and XML/Kotlin conventions. |
| Progress against previous actions | Core workflow was completed and verified; refinement work started from this feedback. |
| Evidence | Customer feedback supplied in the project handoff; implementation recorded in `docs/IMPLEMENTATION_LOG.md`. |

