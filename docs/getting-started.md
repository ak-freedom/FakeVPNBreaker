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

На момент обновления документации latest release - `v1.0.3`, APK asset - `FakeVpnBreaker-v1.0.3.apk`.

Если устройство подключено через Android Debug Bridge (ADB), APK можно установить командой:

```bash
adb install FakeVpnBreaker-v1.0.3.apk
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

Сначала откройте FakeVpnBreaker вручную и выдайте VPN permission через `Request VPN permission`. Broadcast target не открывает UI и не может показать системный consent dialog.

Самый быстрый вариант - нажать `Save MacroDroid macro` на главном экране FakeVpnBreaker. Android откроет системный document picker; сохраните файл как `VPN_OFF.macro`, затем импортируйте его в MacroDroid. Сохраненный файл уже содержит текущий token из FakeVpnBreaker в extra `com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN`, поэтому копировать token вручную не нужно.

Импортируемый шаблон использует Activity fallback и пример trigger app `Пятёрочка`. После импорта откройте trigger в MacroDroid и замените приложение/пакет, если макрос должен запускаться для другого приложения. Intent action, package и token extra можно оставить как есть.

Ожидаемые локальные логи для export flow:

- save requested: `MacroDroid macro save requested`;
- saved: `MacroDroid macro saved`;
- cancelled picker: `MacroDroid macro save cancelled`;
- failed render/write: `MacroDroid macro save failed: ...`.

Для фонового Broadcast-варианта можно настроить MacroDroid action вручную.

Создайте MacroDroid action, который отправляет explicit intent:

| Field | Value |
|-------|-------|
| Target | `Broadcast` |
| Package | `com.akfreedom.fakevpnbreaker` |
| Action | `com.akfreedom.fakevpnbreaker.BREAK_VPN` |
| String extra name | `com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN` |
| String extra value | token, показанный на главном экране приложения |

Если VPN permission уже выдано, `TriggerReceiver` делегирует запуск `FakeVpnService` без вывода FakeVpnBreaker на передний план. Если permission отсутствует, Broadcast не открывает UI и пишет предупреждение в локальный лог.

Если Android ограничивает запуск foreground service из Broadcast на конкретном устройстве или сценарии, используйте MacroDroid target `Activity` как fallback с теми же package, action и token extra. Activity fallback может показать системный VPN consent flow, а после завершения изолируется от основной `MainActivity`.

Ожидаемые локальные логи:

- accepted trigger: `Broadcast trigger accepted`, `Broadcast trigger delegated service start`;
- rejected token: `Broadcast trigger rejected: missing or invalid token`;
- missing permission: `Broadcast trigger ignored: VPN permission missing`;
- service start failure: `Broadcast trigger failed to start VPN service`.

## Verify

Основной чеклист ручной проверки находится в [Manual Verification](verification.md).

Для unit tests pure Kotlin логики используйте:

```bash
./gradlew testDebugUnitTest
```

## See Also

- [Architecture](architecture.md) — структура модулей и ограничения.
- [Manual Verification](verification.md) — полный ручной чеклист.
