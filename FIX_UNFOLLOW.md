# Fix Unfollow Issue

## Problem Identified
The backend was throwing a `400 Bad Request` error with the message:
`Expecting a SELECT query : DELETE FROM Follow ...`

This is a common JPA error when a custom `DELETE` query in a Repository interface is missing the `@Modifying` annotation.

## The Fix
I have added the `@Modifying` annotation to the `deleteByFollowerIdAndFollowedId` method in `FollowRepository.kt`.

## Instructions

1. **Restart the Backend**
   - Stop the running backend process.
   - Run the following command in the `spring_boot_backend` directory:
     ```bash
     ./gradlew clean bootRun
     ```
   - Wait for the application to start completely.

2. **Restart the Flutter App**
   - Hot restart or full restart the app.

3. **Test Unfollow**
   - Go to a user profile you are following.
   - Click the "Following" button.
   - It should now change to "Follow" and the stats should update.
   - You should see a success message in the logs/snackbar.

## Technical Details
- Modified `src/main/kotlin/com/androidinsta/Repository/User/FollowRepository.kt`: Added `@Modifying` annotation to the delete query.
