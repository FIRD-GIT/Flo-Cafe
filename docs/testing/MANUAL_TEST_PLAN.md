# Flo Cafe manual test plan

This plan records emulator and Firebase checks that complement the JVM unit
tests. Test data must be synthetic and no credentials should be committed.

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
| MT-05 | Product choices | Open a drink, sandwich and fixed sweet treat. | Drinks/sandwiches show relevant options; cookies/brownies add directly without a customisation dialog. | Emulator verified |
| MT-06 | Basket editing | Add products, change quantity, remove the final item. | Images, concise customisation text, total and empty state update correctly. | Emulator verified |
| MT-07 | Duplicate protection | Submit the same basket twice within ten minutes. | The second submission asks for explicit confirmation. | Firebase end-to-end to capture |
| MT-08 | Customer order persistence | Submit a non-empty basket and restart the app. | The order remains visible in customer order status/history. | Firebase end-to-end to capture |
| MT-09 | Sub-admin restriction | Sign in as customer, then attempt admin access; sign in as synthetic sub-admin. | Customer cannot use admin actions; sub-admin can progress an order through the allow-listed statuses. | Firebase end-to-end to capture |
| MT-10 | Failure handling | Disable network or use an invalid Firebase operation during sign-in/order submission. | A useful error is shown and no false successful order is displayed. | Firebase end-to-end to capture |

## Automated command

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest --no-daemon
```

The HTML report is generated at
`app/build/reports/tests/testDebugUnitTest/index.html`.
