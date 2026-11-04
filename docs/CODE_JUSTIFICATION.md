# Flo Cafe Code Justification

This document explains the main implementation choices in the Flo Cafe Android application and the reason each choice supports the coursework requirements. It is intended as a revision and demonstration aid: each section connects a requirement to the code, the visible behaviour, and the security or maintainability reason for the design.

## 1. Overall technical direction

### Kotlin and XML

The application uses Kotlin for behaviour and XML layouts for the interface. This matches the coursework direction and keeps the relationship between a screen, its view IDs, and its Kotlin event handlers easy to inspect in Android Studio.

- Layout: `app/src/main/res/layout/activity_main.xml`
- Screen controller: `app/src/main/java/com/firdgit/flocafe/MainActivity.kt`
- Resources: `app/src/main/res/values/strings.xml`

`MainActivity` is intentionally small enough for the current vertical slice. Repository classes contain Firebase operations, while the activity translates results into visible states and user actions.

### Firebase configuration

`app/google-services.json` is local configuration and is ignored by Git. The Google Services plugin reads it during the build and generates the Firebase resources needed by the SDKs. Keeping it out of the repository avoids publishing project-specific configuration and follows Firebase's recommended Android setup.

The app also checks `FirebaseApp.getApps(...)` before using Firebase. This produces a visible setup state instead of a misleading success screen when the configuration file is missing.

## 2. User identity and `isAdmin`

### Data model

Each authenticated user has a Firestore document at:

```text
users/{uid}
```

The important fields are:

```json
{
  "uid": "firebase-user-id",
  "email": "customer@example.com",
  "displayName": "Customer",
  "isAdmin": false
}
```

`UserProfile.kt` maps `isAdmin == true` to `UserRole.ADMIN`; false or missing values resolve to customer access. This fail-closed default is safer than treating missing data as elevated access.

### Why the role is stored in Firestore

Firebase Authentication proves who the user is, but it does not by itself describe the application's café role. The Firestore profile supplies that application-specific role and gives the rules engine a value it can check before allowing administrative operations.

The app uses `UserRepository.kt` as the single place for authentication, profile creation, role lookup, and sign-out. This avoids duplicating Firebase calls across screens.

## 3. Firestore security rules

The rules are in `firestore.rules` and are enforced by Firebase, not just by the Android UI. This matters because hiding an admin button would not prevent a malicious client from calling Firestore directly.

### `users/{userId}` reads

```text
allow read: if request.auth != null
  && (request.auth.uid == userId || isAdmin());
```

An authenticated customer can read only their own profile. An admin can read user profiles when the operational workflow requires it. Unauthenticated users cannot read profiles.

### Customer profile creation

```text
allow create: if request.auth != null
  && request.auth.uid == userId
  && request.resource.data.isAdmin == false;
```

The signed-in user may create only their own profile. The explicit `isAdmin == false` condition prevents a new customer from registering and assigning themselves admin access.

### Profile updates

```text
allow update: if request.auth != null
  && (request.auth.uid == userId || isAdmin())
  && request.resource.data.isAdmin == resource.data.isAdmin;
```

Users may update their own ordinary profile fields, and admins may update permitted profile data. The old and new `isAdmin` values must match, so this rule prevents promotion or demotion through a normal profile update. Role changes remain a trusted administrative operation.

### Profile deletion

```text
allow delete: if isAdmin();
```

Only an admin can delete a user profile document. This prevents customers from deleting their own or another user's role record.

### Admin detection

```text
function isAdmin() {
  return request.auth != null
    && get(/databases/$(database)/documents/users/$(request.auth.uid)).data.isAdmin == true;
}
```

The helper keeps the rule conditions consistent. It requires authentication and reads the signed-in user's own profile; a missing or false value does not grant admin access.

### `orders/{orderId}` creation

```text
allow create: if request.auth != null
  && request.resource.data.customerUid == request.auth.uid
  && request.resource.data.status == "Pending";
```

Customers can create an order only for themselves, and every new order must start as `Pending`. The client cannot create an order for another user or skip directly to `Collected`.

### Order reads

```text
allow read: if request.auth != null
  && (resource.data.customerUid == request.auth.uid || isAdmin());
```

Customers can see their own orders. Admins can see incoming orders so they can perform the limited operational workflow. Other customers cannot inspect someone else's order.

### Admin order updates

Admin updates preserve the customer, total, and item snapshots:

```text
allow update: if isAdmin()
  && request.resource.data.customerUid == resource.data.customerUid
  && request.resource.data.total == resource.data.total
  && request.resource.data.items == resource.data.items
  && request.resource.data.status in [
    "Pending", "Preparing", "Ready for Collection", "Collected"
  ];
```

This allows the status to change while preventing an admin update from silently changing who owns the order, its price, or its historical item data. The explicit status list also prevents arbitrary or misspelled states.

### Order deletion

```text
allow delete: if isAdmin();
```

Only an admin can delete an order. Customers retain an auditable order record rather than being able to remove it from the database.

## 4. Authentication flow

`UserRepository.signIn` uses Firebase email/password authentication. `registerCustomer` creates the Firebase account and immediately writes a profile with `isAdmin = false`.

The activity:

1. Checks whether Firebase is configured.
2. Restores an existing Firebase session on launch.
3. Loads the user's Firestore role.
4. Shows customer or sub-admin content.
5. Shows a useful error without entering protected content when authentication fails.

This supports FR-01 and FR-02 while keeping password handling inside Firebase rather than storing passwords in the app.

## 5. Customer menu and basket

`MenuRepository` currently supplies controlled local menu data so the Thursday demonstration remains usable before a menu administration screen exists. `BasketLine` stores the selected item snapshot and quantity.

The basket total is calculated from:

```text
unit price × quantity
```

The interface updates after every add action. An empty basket cannot be submitted. This gives a visible working slice for menu browsing and basket calculation while leaving Firestore menu management as a later enhancement.

## 6. Order submission

`OrderRepository.submitOrder`:

1. Rejects an empty basket.
2. Requires an authenticated Firebase user.
3. Copies item name and price into the order.
4. Writes the customer UID, total, `Pending` status, and item list.
5. Returns the generated order ID.
6. Clears the basket only after Firestore confirms success.

Copying item names and prices into the order is deliberate: historical orders should remain understandable if the menu changes later. Clearing the basket only after success prevents a network failure from appearing to have created a successful order.

## 7. Order tracking and sub-admin workflow

Customers query orders matching their own UID and see the order ID, total, and status. Admins query incoming orders and can advance the status through the controlled list:

```text
Pending → Preparing → Ready for Collection → Collected
```

The UI restricts the visible controls by role, while `firestore.rules` provides the actual security boundary. Both layers are needed: the UI supports usability, and the rules protect the data if the client is modified or bypassed.

## 8. Thursday 5 November scope

### Completed implementation

- Firebase Android configuration and build integration
- Email/password registration and sign-in
- Session restoration and sign-out
- `isAdmin` role model and Firestore protection
- Customer menu and basket total
- Firestore order submission
- Customer order tracking
- Admin-controlled order status progression
- Emulator build/install verification
- Feature-specific Git commits and implementation documentation

### Remaining live validation

The Firebase Console setup is now at the point where the following must be tested with real accounts:

1. Register a customer and confirm `users/{uid}` has `isAdmin: false`.
2. Create a separate trusted admin account and profile with `isAdmin: true`.
3. Submit an order as the customer.
4. Advance its status as the admin.
5. Sign back in as the customer and confirm the updated status.
6. Capture screenshots or a short recording for Thursday's evidence.

AI analysis and previous-order suggestions are not required for this Thursday slice and should be scheduled after the core flow has been demonstrated.
