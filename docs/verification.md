[← Previous Page](architecture.md) · [Back to README](../README.md)

# Manual Verification

Эта страница содержит ручной чеклист для проверки APK, VPN flow, MacroDroid trigger и privacy boundaries.

## Build and Install

1. Download the APK from [latest GitHub Release](https://github.com/ak-freedom/FakeVPNBreaker/releases/latest).
2. Install the APK on a device or emulator running Android 8.0+.
3. For local development builds, build a debug APK from Android Studio or with `./gradlew assembleDebug`.
4. If building locally, confirm the merged manifest does not contain `android.permission.INTERNET`.

Current release reference:

| Field | Value |
|-------|-------|
| Latest tag at documentation update | `v1.0.2` |
| APK asset | `FakeVpnBreaker-v1.0.2.apk` |
| Release page | <https://github.com/ak-freedom/FakeVPNBreaker/releases/tag/v1.0.2> |

## Release APK Smoke Test

1. Install `FakeVpnBreaker-v1.0.2.apk` from GitHub Releases.
2. Open Android app info for FakeVpnBreaker and confirm no internet permission is listed.
3. Open the app and continue with the permission and VPN flow checks below.

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

1. Open FakeVpnBreaker once and grant VPN permission manually.
2. In MacroDroid, create an action that sends an explicit intent with target `Broadcast`.
3. Set package `com.akfreedom.fakevpnbreaker`.
4. Use action `com.akfreedom.fakevpnbreaker.BREAK_VPN`.
5. Add a string extra named `com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN`.
6. Set the extra value to the token shown on the FakeVpnBreaker main screen.
7. Run the MacroDroid action.
8. Verify FakeVpnBreaker does not come to the foreground while the dummy VPN starts.
9. Send the same Broadcast without the token and verify the local log records `Broadcast trigger rejected: missing or invalid token` without starting the service.
10. Revoke or clear VPN permission where possible, send the Broadcast again, and verify the local log records `Broadcast trigger ignored: VPN permission missing` without opening UI.
11. On Android 12+ / targetSdk 35 devices, if background foreground-service start is blocked, verify the local log records `Broadcast trigger failed to start VPN service`.

## MacroDroid Activity Fallback

1. Configure MacroDroid with target `Activity`, package `com.akfreedom.fakevpnbreaker`, action `com.akfreedom.fakevpnbreaker.BREAK_VPN`, and the same token extra.
2. Run the fallback action.
3. If VPN permission is already granted, verify `TriggerActivity` delegates the service start and finishes.
4. If VPN permission is missing, verify Android shows the system VPN consent flow.
5. After `TriggerActivity` finishes, verify Android does not bring an existing `MainActivity` task to the foreground.
6. Confirm the local log contains trigger received, permission decision, service start delegated or failure, and `TriggerActivity finished` when close-after-trigger is enabled.

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

- [Getting Started](getting-started.md) — релизный APK, установка и первый запуск.
- [Architecture](architecture.md) — структура модулей и ограничения.
