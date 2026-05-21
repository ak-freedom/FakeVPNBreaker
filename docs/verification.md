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
| Latest tag at documentation update | `v1.0.3` |
| APK asset | `FakeVpnBreaker-v1.0.3.apk` |
| Release page | <https://github.com/ak-freedom/FakeVPNBreaker/releases/tag/v1.0.3> |

## Release APK Smoke Test

1. Install `FakeVpnBreaker-v1.0.3.apk` from GitHub Releases.
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
2. Tap `Save MacroDroid macro`.
3. Save the suggested `VPN_OFF.macro` through the Android document picker.
4. Verify the local log records `MacroDroid macro save requested` and `MacroDroid macro saved`.
5. Repeat the save flow and cancel the picker; verify the local log records `MacroDroid macro export cancelled` without an error state or crash.
6. Import the saved `VPN_OFF.macro` into MacroDroid.
7. Confirm the imported intent contains package `com.akfreedom.fakevpnbreaker`, action `com.akfreedom.fakevpnbreaker.BREAK_VPN`, extra name `com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN`, and an extra value matching the current app token.
8. Confirm the imported action target is `Activity`; the generated macro is the fallback-ready import path, not the recommended manual Broadcast setup.
9. Add the needed triggers for the target scenario, or edit the bundled example trigger app/package if reusing that trigger.
10. Run the imported macro with another VPN active and verify the dummy VPN starts.
11. Confirm the local log includes save requested/saved, sanitized trigger receipt, permission decision, service start delegated or service-start failure, dummy VPN closure, and no token, URI, raw exception message, or generated macro JSON.

## MacroDroid Broadcast Trigger

1. Open FakeVpnBreaker once and grant VPN permission manually.
2. In MacroDroid, create an action that sends an explicit intent with target `Broadcast`.
3. Set package `com.akfreedom.fakevpnbreaker`.
4. Use action `com.akfreedom.fakevpnbreaker.BREAK_VPN`.
5. Add a string extra named `com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN`.
6. Set the extra value to the token shown on the FakeVpnBreaker main screen.
7. Run the MacroDroid action.
8. Verify FakeVpnBreaker does not come to the foreground while the dummy VPN starts.
9. Send the same Broadcast without the token and verify the local log records `Trigger rejected: missing or invalid token; update the MacroDroid extra from FakeVpnBreaker` without starting the service.
10. Send a Broadcast with no action and verify the local log records `Broadcast trigger received: actionState=missing` and a safe missing-action recovery message.
11. Send a Broadcast with any unsupported action and verify the local log records `Broadcast trigger received: actionState=unsupported` and a safe unsupported-action recovery message without printing the raw action.
12. Revoke or clear VPN permission where possible, send the valid Broadcast again, and verify the local log records `VPN permission missing; open FakeVpnBreaker and grant VPN permission before triggering again` without opening UI.
13. On Android 12+ / targetSdk 35 devices, if background foreground-service start is blocked, verify the local log records `VPN break service start failed; try the Activity fallback` or `Foreground VPN service could not start; try the Activity fallback`.

## MacroDroid Activity Fallback

1. Configure MacroDroid with target `Activity`, package `com.akfreedom.fakevpnbreaker`, action `com.akfreedom.fakevpnbreaker.BREAK_VPN`, and the same token extra.
2. Run the fallback action.
3. If VPN permission is already granted, verify `TriggerActivity` delegates the service start and finishes.
4. If VPN permission is missing, verify Android shows the system VPN consent flow.
5. While the consent flow is visible, send the fallback action again and verify the local log records duplicate handling without launching another consent request.
6. After `TriggerActivity` finishes, verify Android does not bring an existing `MainActivity` task to the foreground.
7. Confirm the local log contains sanitized trigger receipt with `actionState=expected`, permission decision, service start delegated or failure, and `TriggerActivity finished` when close-after-trigger is enabled.
8. Confirm missing-action, unsupported-action, rejected-token, and service-start failure paths are logged without printing the raw action or token value.

## Permission and Picker Launch Failures

1. From the main screen, trigger VPN permission request on a device/profile where Android cannot open the consent UI, if available.
2. Verify the local log records `VPN permission screen could not be opened; retry from FakeVpnBreaker` and does not claim the user denied consent.
3. From Activity fallback, repeat the same permission-launch failure scenario and verify the fallback finishes according to the close-after-trigger setting.
4. Disable or restrict the document picker if the test environment allows it, tap `Save MacroDroid macro`, and verify the local log records `MacroDroid document picker could not be opened; retry export from FakeVpnBreaker`.
5. Confirm none of these logs include intent payloads, URI values, token values, or raw exception messages.

## Repeated Triggers and Cleanup

1. Send the MacroDroid trigger multiple times within one selected hold interval.
2. Verify the app replaces the active dummy session without crashing.
3. Confirm the local log includes `Repeated start received; replacing the active dummy VPN session` and later records VPN closure.
4. Force stop or swipe away the app during a dummy session where possible.
5. Verify the service cleanup path closes the VPN interface and stops the foreground notification.

## Edge Cases

1. Deny the VPN consent dialog and verify the app logs permission missing without starting the service.
2. Test on Android 13+ without notification permission and verify the VPN service still starts or the app logs a foreground-service start error.
3. Enable Android Always-on VPN or Block connections without VPN and verify behavior manually; Android policy may prevent interruption or immediate release.
4. Confirm the local log stores only the latest 50 entries and never includes installed app lists, IP/domain history, packet data, or personal data.
5. Inspect Logcat with tag `FakeVpnBreaker` during Broadcast and Activity fallback tests. Expected safe categories include `actionState=expected`, `actionState=missing`, and `actionState=unsupported`; token values, generated macro JSON, installed app lists, domains, packet data, and personal data must not appear.

## See Also

- [Getting Started](getting-started.md) — релизный APK, установка и первый запуск.
- [Architecture](architecture.md) — структура модулей и ограничения.
