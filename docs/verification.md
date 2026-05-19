[← Previous Page](architecture.md) · [Back to README](../README.md)

# Manual Verification

Эта страница содержит ручной чеклист для проверки APK, VPN flow, MacroDroid trigger и privacy boundaries.

## Build and Install

1. Build a debug APK from Android Studio or with `./gradlew assembleDebug`.
2. Confirm the merged manifest does not contain `android.permission.INTERNET`.
3. Install the APK on a device or emulator running Android 8.0+.

## First Launch and Permission

1. Open FakeVpnBreaker from the launcher.
2. Verify the screen shows VPN permission as missing.
3. Tap `Request VPN permission`.
4. Accept the Android system VPN consent dialog.
5. Return to the app and verify the screen shows VPN permission as granted.
6. Confirm the local log contains permission request/grant events only, without app names, traffic, domains, or device identifiers.

## Manual Break Flow

1. Start another VPN app or Android VPN profile.
2. In FakeVpnBreaker, select `1000 ms` and `Full takeover`.
3. Tap `Run test break`.
4. Verify Android briefly switches the active VPN to FakeVpnBreaker.
5. Wait for the selected hold duration.
6. Verify FakeVpnBreaker closes the VPN session and no longer remains active.
7. Confirm the local log includes service start, dummy VPN established, dummy VPN closed, and service stopped events.

## Duration and Routing Settings

1. Select each supported duration: `300`, `500`, `1000`, `2000`, and `5000 ms`.
2. Run a manual break after each selection and verify the session roughly matches the selected hold duration.
3. Switch between `Full takeover` and `Local only`.
4. Verify settings persist after closing and reopening the app.

## MacroDroid Trigger

1. In MacroDroid, create an action that sends an explicit intent to package `com.akfreedom.fakevpnbreaker`.
2. Use action `com.akfreedom.fakevpnbreaker.BREAK_VPN`.
3. Add a string extra named `com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN`.
4. Set the extra value to the token shown on the FakeVpnBreaker main screen.
5. Run the MacroDroid action.
6. If VPN permission is already granted, verify FakeVpnBreaker starts the dummy VPN and closes `TriggerActivity` quickly.
7. If VPN permission is missing, verify Android shows the system VPN consent flow.
8. Send the same intent without the token and verify the local log records a rejected trigger without starting the service.
9. Confirm the local log contains trigger received, permission decision, service start delegated, and activity finished events.

## Repeated Triggers and Cleanup

1. Send the MacroDroid trigger multiple times within one selected hold interval.
2. Verify the app replaces the active dummy session without crashing.
3. Confirm the local log includes a repeated start warning and later records VPN closure.
4. Force stop or swipe away the app during a dummy session where possible.
5. Verify the service cleanup path closes the VPN interface and stops the foreground notification.

## Edge Cases

1. Deny the VPN consent dialog and verify the app logs permission missing without starting the service.
2. Test on Android 13+ without notification permission and verify the VPN service still starts or the app logs a foreground-service start error.
3. Enable Android Always-on VPN or Block connections without VPN and verify behavior manually; Android policy may prevent interruption or immediate release.
4. Confirm the local log stores only the latest 50 entries and never includes installed app lists, IP/domain history, packet data, or personal data.

## See Also

- [Getting Started](getting-started.md) — сборка, установка и первый запуск.
- [Architecture](architecture.md) — структура модулей и ограничения.
