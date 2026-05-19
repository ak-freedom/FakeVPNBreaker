[← Previous Page](getting-started.md) · [Back to README](../README.md) · [Next Page →](verification.md)

# Architecture

FakeVpnBreaker построен как небольшой Android/Kotlin utility app с техническими слоями. Главная граница: Android entry points инициируют запуск, а VPN lifecycle и cleanup остаются внутри `vpn/`.

## Project Structure

```text
app/
  src/main/
    AndroidManifest.xml
    java/com/akfreedom/fakevpnbreaker/
      MainActivity.kt
      TriggerActivity.kt
      SimpleItemSelectedListener.kt
      vpn/
        FakeVpnService.kt
        VpnBreakController.kt
        VpnNotification.kt
      settings/
        BreakDuration.kt
        RoutingMode.kt
        SettingsRepository.kt
        TriggerToken.kt
      logging/
        EventLog.kt
        EventLogRepository.kt
    res/
      layout/activity_main.xml
      values/strings.xml
  src/test/
    java/com/akfreedom/fakevpnbreaker/
```

## Entry Points

| Entry point | Responsibility |
|-------------|----------------|
| `MainActivity` | Main UI, permission request, manual test break |
| `TriggerActivity` | Exported MacroDroid entry point |
| `FakeVpnService` | Dummy VPN lifecycle and cleanup |

`TriggerActivity` is exported intentionally because MacroDroid needs an external intent target. `FakeVpnService` remains non-exported and requires `android.permission.BIND_VPN_SERVICE`.

## VPN Flow

```text
MainActivity or TriggerActivity
  -> VpnBreakController
  -> FakeVpnService
  -> VpnService.Builder.establish()
  -> hold selected duration
  -> close ParcelFileDescriptor
  -> stop foreground service
```

`FakeVpnService` creates a dummy interface with local address `10.255.0.1/32`. In `Full takeover` mode it also adds route `0.0.0.0/0`; in `Local only` mode it does not add the default route.

## Settings

| Setting | Allowed values |
|---------|----------------|
| Hold duration | `300`, `500`, `1000`, `2000`, `5000 ms` |
| Routing mode | `Full takeover`, `Local only` |
| Trigger token | Generated local token for MacroDroid intent validation |

Settings are stored in `SharedPreferences` through `SettingsRepository`.

## Logging

The local event log stores the latest 50 events in `SharedPreferences`.

Allowed log content:

- lifecycle events;
- permission decisions;
- trigger validation result;
- service start and cleanup events.

Disallowed log content:

- installed app lists;
- traffic history;
- packet data;
- domains, IP history, or personal identifiers.

## Security and Privacy Boundaries

- Do not add `android.permission.INTERNET`.
- Do not connect to external servers.
- Do not add analytics SDKs or telemetry.
- Do not detect or special-case specific VPN clients.
- Do not route user traffic to a remote endpoint.
- Do not keep `FakeVpnService` active after the selected hold interval.

## See Also

- [Getting Started](getting-started.md) — релизный APK и первый запуск.
- [Manual Verification](verification.md) — проверка поведения на устройстве.
