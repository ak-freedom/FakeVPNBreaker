[Back to README](../README.md) · [Next Page →](architecture.md)

# Getting Started

Эта страница описывает установку готового APK из GitHub Releases, локальную сборку для разработки и первый запуск FakeVpnBreaker.

## Prerequisites

| Tool | Purpose |
|------|---------|
| Android Studio или Android SDK | Сборка и установка APK |
| JDK 17 | Gradle и Kotlin compilation |
| Android 8.0+ device/emulator | Минимальный SDK проекта: `26` |

Для установки готового APK нужен только Android 8.0+ device/emulator. Android Studio, Android SDK и JDK 17 нужны, если вы собираете приложение локально из исходников.

Проект использует Gradle wrapper, поэтому отдельная установка Gradle не требуется.

## Recommended Install

1. Откройте [latest GitHub Release](https://github.com/ak-freedom/FakeVPNBreaker/releases/latest).
2. Скачайте APK из блока `Assets`.
3. Установите APK на Android 8.0+ устройство.
4. Откройте FakeVpnBreaker из launcher.

На момент обновления документации latest release - `v1.0.1`, APK asset - `FakeVpnBreaker-v1.0.1.apk`.

Если устройство подключено через Android Debug Bridge (ADB), APK можно установить командой:

```bash
adb install FakeVpnBreaker-v1.0.1.apk
```

## Local Debug Build

```bash
./gradlew assembleDebug
```

Debug APK будет создан в:

```text
app/build/outputs/apk/debug/
```

## Local Install

Установите готовый APK из GitHub Releases либо debug APK на устройство или эмулятор через Android Studio или `adb install`.

Перед ручной проверкой убедитесь, что итоговый manifest не содержит:

```text
android.permission.INTERNET
```

Приложению нужны только foreground-service permissions и системное VPN-разрешение через пользовательский consent.

## First Launch

1. Откройте FakeVpnBreaker из launcher.
2. Нажмите `Request VPN permission`.
3. Подтвердите системный Android VPN consent dialog.
4. Вернитесь в приложение и проверьте статус `VPN permission: granted`.

Разрешение выдается Android через `VpnService.prepare(...)`; приложение не обходит этот системный flow.

## Manual Break

1. Запустите другой VPN-клиент или Android VPN profile.
2. В FakeVpnBreaker выберите длительность, например `1000 ms`.
3. Выберите routing mode.
4. Нажмите `Run test break`.
5. Проверьте, что Android кратковременно переключает активный VPN на FakeVpnBreaker.
6. Дождитесь окончания выбранного интервала и убедитесь, что FakeVpnBreaker больше не активен как VPN.

## MacroDroid Setup

Создайте MacroDroid action, который отправляет explicit intent:

| Field | Value |
|-------|-------|
| Package | `com.akfreedom.fakevpnbreaker` |
| Action | `com.akfreedom.fakevpnbreaker.BREAK_VPN` |
| String extra name | `com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN` |
| String extra value | token, показанный на главном экране приложения |

Если VPN permission уже выдано, `TriggerActivity` быстро делегирует запуск `FakeVpnService` и закрывается. Если permission отсутствует, Android покажет системный VPN consent flow.

## Verify

Основной чеклист ручной проверки находится в [Manual Verification](verification.md).

Для unit tests pure Kotlin логики используйте:

```bash
./gradlew testDebugUnitTest
```

## See Also

- [Architecture](architecture.md) — структура модулей и ограничения.
- [Manual Verification](verification.md) — полный ручной чеклист.
