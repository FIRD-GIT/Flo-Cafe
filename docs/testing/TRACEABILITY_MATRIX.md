# Flo Cafe testing traceability matrix

This matrix applies the supplied `Flo-Cafe-PRD-Explanation.pdf` guidance:
requirements must be traceable to backlog items, implementation code, tests and
visible evidence. It is also aligned with `docs/PRD.md`, `docs/BACKLOG.md` and
`docs/IMPLEMENTATION_LOG.md`.

## Automated coverage

| Requirement area | Backlog | Code/evidence | Automated test | Result |
|---|---|---|---|---|
| Menu categories and product metadata | CAFE-04, CAFE-20, CAFE-22 | `MenuRepository.kt`, `MenuItem.kt` | `MenuRepositoryTest` | Pass |
| Product-specific customisation | CAFE-21 | `MenuRepository.kt`, `CustomerActivity.kt` | `MenuRepositoryTest` | Pass |
| Currency presentation | CAFE-04, CAFE-06 | `PriceFormatter.kt` | `PriceFormatterTest` | Pass |
| Basket totals and empty basket | CAFE-06, PRD FR-05/FR-06 | `BasketCalculator.kt`, `CustomerActivity.kt` | `BasketCalculatorTest` | Pass |
| Authentication input validation | CAFE-01, CAFE-02, PRD FR-01/FR-02 | `InputValidation.kt`, `UserRepository.kt` | `InputValidationTest` | Pass |
| Duplicate-order fingerprint | CAFE-07, NFR-04 | `OrderFingerprint.kt`, `OrderRepository.kt` | `OrderFingerprintTest` | Pass |

## Manual and Firebase coverage

| Requirement area | Manual case | Current evidence | Remaining evidence |
|---|---|---|---|
| Four-tab customer navigation | MT-03 | Emulator inspection recorded in implementation log | Capture final screenshot set |
| 2×2 category navigation | MT-04 | Emulator installation and UI inspection recorded | Capture final category-to-filter screenshot |
| Product customisation and fixed treats | MT-05 | Emulator inspection recorded | Capture final customisation screenshots |
| Basket editing and visual layout | MT-06 | Emulator inspection recorded; APK installed on `emulator-5554` | Capture final basket screenshot |
| Duplicate protection | MT-07 | Code and unit fingerprint coverage | Execute with configured Firebase account |
| Order persistence/history | MT-08 | Firestore code and order-history rendering compile | Execute with configured Firebase account |
| Customer/sub-admin access boundary | MT-09 | Firestore rules and role code reviewed | Execute with synthetic customer/admin accounts |
| Failure handling | MT-10 | `runCatching` paths and validation tests | Capture sign-in and order failure evidence |

## Commands and artefact locations

```bash
./gradlew :app:assembleDebug :app:testDebugUnitTest --no-daemon
```

- Unit tests: `app/src/test/java/com/firdgit/flocafe/`
- HTML report: `app/build/reports/tests/testDebugUnitTest/index.html`
- XML results: `app/build/test-results/testDebugUnitTest/`
- Manual plan: `docs/testing/MANUAL_TEST_PLAN.md`

The matrix does not mark live Firebase behaviour as passed until it has been
executed with synthetic accounts and recorded with reproducible evidence.
