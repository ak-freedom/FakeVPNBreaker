[Back to README](../README.md) · [Next Page →](architecture.md)

# Getting Started

Эта страница описывает сборку, установку и первый запуск FakeVpnBreaker.

## Prerequisites

| Tool | Purpose |
|------|---------|
| Android Studio или Android SDK | Сборка и установка APK |
| JDK 17 | Gradle и Kotlin compilation |
| Android 8.0+ device/emulator | Минимальный SDK проекта: `26` |

Проект использует Gradle wrapper, поэтому отдельная установка Gradle не требуется.

## Build

```bash
./gradlew assembleDebug
```

Debug APK будет создан в:

```text
app/build/outputs/apk/debug/
```

## Install

Установите debug APK на устройство или эмулятор через Android Studio либо `adb install`.

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
