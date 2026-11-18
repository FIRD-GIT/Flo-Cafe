# Flo Cafe manual test plan

This plan records emulator and Firebase checks that complement the JVM unit
tests. It follows the supplied `Flo-Cafe-PRD-Explanation.pdf` guidance that
requirements must be connected to tests and visible evidence. Test data must be
synthetic and no credentials should be committed.

## Environment

- Android Studio emulator: `emulator-5554`
- Build: `debug`
- Firebase: local `app/google-services.json` with Authentication and Firestore enabled

## Test cases

| ID | Area | Steps | Expected result | Evidence/status |
|---|---|---|---|---|
| MT-01 | Registration validation | Submit registration with blank email and a password shorter than six characters. | The user remains on the authentication screen and sees a validation error. | To capture with screenshot |
| MT-02 | Sign-in validation | Submit sign-in with blank email or password. | The user remains on the authentication screen and sees a validation error. | To capture with screenshot |
| MT-03 | Customer navigation | Sign in as a synthetic customer and open Home, Menu, Basket and Profile. | Each tab opens its own destination and the basket remains available. | Emulator verified |
| MT-04 | Menu categories | Open Home and Menu category grids; select each category. | All four categories are visible without horizontal scrolling and Menu opens filtered to the selected category. | Emulator verified |
| MT-04A | Menu search | Enter `brownie` in the Menu search field. | Only Fudge brownie/Sweet Treats matching content remains and no unrelated item dominates the result. | Emulator verified |
| MT-05 | Product choices | Open a drink, sandwich and fixed sweet treat. | Drinks/sandwiches show relevant options; cookies/brownies add directly without a customisation dialog. | Emulator verified |
| MT-06 | Basket editing | Add products, change quantity, remove the final item. | Images, concise customisation text, total and empty state update correctly. | Emulator verified |
| MT-07 | Duplicate protection | Submit the same basket twice within ten minutes. | The second submission asks for explicit confirmation. | Firebase end-to-end to capture |
| MT-08 | Customer order persistence | Submit a non-empty basket and restart the app. | The order remains visible in customer order status/history. | Firebase end-to-end to capture |
| MT-09 | Sub-admin restriction | Sign in as customer, then attempt admin access; sign in as synthetic sub-admin. | Customer cannot use admin actions; sub-admin can progress an order through the allow-listed statuses. | Firebase end-to-end to capture |
| MT-10 | Failure handling | Disable network or use an invalid Firebase operation during sign-in/order submission. | A useful error is shown and no false successful order is displayed. | Firebase end-to-end to capture |
| MT-11 | Boundary values | Submit an empty basket; reduce a quantity to one and remove the final item; attempt a five-character registration password. | Empty checkout is blocked, the final removal shows the empty state, and the password boundary is rejected. | Unit coverage for password/empty total; emulator evidence to capture |
| MT-12 | Unauthorised access | Sign in as a customer and attempt the sub-admin workflow; inspect Firestore access with the customer identity. | Customer cannot perform admin operations or read another customer's order. | Rules/code reviewed; live Firebase evidence to capture |
| MT-13 | Restart persistence | Submit an order, close/reopen the app and revisit Profile. | The authenticated state and stored order are restored according to Firebase results. | Firebase end-to-end to capture |
| MT-14 | Accessibility/device | Use the supported emulator, inspect content descriptions, labels, contrast and scrolling on Home, Menu, Basket and Profile. | Controls remain understandable and usable without clipped content. | Emulator inspection partly verified; final evidence to capture |
| MT-15 | Regression | Re-run the Gradle command after a UI or repository change and compare the affected workflow with the previous evidence. | Existing unit tests remain green and the changed workflow still behaves as expected. | Build/unit regression verified; screenshot set to capture |

## Automated command

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest --no-daemon
```

The HTML report is generated at
`app/build/reports/tests/testDebugUnitTest/index.html`.

See `docs/testing/TRACEABILITY_MATRIX.md` for the requirement-to-test mapping.
See `docs/testing/ASSESSMENT_TEST_EVIDENCE.md` for checklist-aligned status and
the current assessment gaps.
