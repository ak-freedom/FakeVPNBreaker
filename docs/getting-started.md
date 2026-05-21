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

Самый быстрый вариант - нажать `Save MacroDroid macro` на главном экране FakeVpnBreaker. Android откроет системный document picker; сохраните файл как `VPN_OFF.macro`, затем импортируйте его в MacroDroid. Сохраненный файл уже содержит текущий токен из FakeVpnBreaker в extra `com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN`, поэтому копировать токен вручную не нужно.

Импорт сохраненного `VPN_OFF.macro` проверен на реальном устройстве: MacroDroid принимает файл, а импортированный action сохраняет package `com.akfreedom.fakevpnbreaker`, action `com.akfreedom.fakevpnbreaker.BREAK_VPN` и token extra `com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN`.

Импортируемый шаблон использует Activity fallback и пример trigger app `Пятёрочка`. После импорта добавьте нужные триггеры или откройте существующий триггер в MacroDroid и замените приложение/пакет, если макрос должен запускаться для другого сценария. Intent action, package и token extra можно оставить как есть.

Ожидаемые локальные логи для export flow:

- save requested: `MacroDroid macro save requested`;
- saved: `MacroDroid macro saved`;
- cancelled picker: `MacroDroid macro export cancelled`;
- missing URI: `MacroDroid macro export did not return a document; choose a save location and retry`;
- picker launch failure: `MacroDroid document picker could not be opened; retry export from FakeVpnBreaker`;
- template/render failure: `MacroDroid macro export failed: bundled template could not be prepared`;
- output stream unavailable: `MacroDroid macro export target is unavailable; choose another save location`;
- write or permission failure: safe write/permission messages without URI, document path, token, generated `.macro` content, or raw exception message.

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

- received trigger: `Broadcast trigger received: actionState=expected`;
- accepted trigger: `Broadcast trigger accepted; starting dummy VPN if permission is already granted`, `VPN break service start delegated`;
- missing/unsupported action: safe messages beginning with `Trigger rejected: missing action` or `Trigger rejected: unsupported action`;
- rejected token: `Trigger rejected: missing or invalid token; update the MacroDroid extra from FakeVpnBreaker`;
- missing permission: `VPN permission missing; open FakeVpnBreaker and grant VPN permission before triggering again`;
- service start failure: `VPN break service start failed; try the Activity fallback`.

Для Activity fallback ожидаемые логи включают sanitized trigger receipt (`actionState=expected`, `actionState=missing` или `actionState=unsupported`), waiting-for-consent state, permission granted/denied state, duplicate trigger handling while consent is active, service start delegation или service-start failure, а также `TriggerActivity finished after trigger handling` при включенном закрытии fallback activity. Значение token, raw external action при отказе и содержимое `.macro` в лог не выводятся.

Manual flow diagnostics use the same recovery language: if VPN permission is missing, request it from the main screen; if Android cannot open the VPN consent UI, retry from FakeVpnBreaker; if foreground service startup or dummy VPN establishment fails, use the local log category to distinguish notification/start failure from VPN establish failure. Repeated starts are logged as replacement of the active dummy VPN session.

## Verify

Основной чеклист ручной проверки находится в [Manual Verification](verification.md).

Для unit tests pure Kotlin логики используйте:

```bash
./gradlew testDebugUnitTest
```

## See Also

- [Architecture](architecture.md) — структура модулей и ограничения.
- [Manual Verification](verification.md) — полный ручной чеклист.
