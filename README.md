# FakeVpnBreaker

> Android utility app, который кратковременно поднимает локальный dummy VPN, чтобы освободить текущий активный VPN-сеанс.

FakeVpnBreaker использует стандартный Android `VpnService`: приложение получает системное VPN-разрешение, на короткое время становится активным VPN и затем закрывает собственный VPN-интерфейс. Оно не требует root, Shizuku, `INTERNET` permission или интеграции с конкретными VPN-клиентами.

## Quick Start

Скачайте готовый APK из [latest GitHub Release](https://github.com/ak-freedom/FakeVPNBreaker/releases/latest).

Текущий релизный артефакт: `FakeVpnBreaker-v1.0.3.apk`.

```bash
adb install FakeVpnBreaker-v1.0.3.apk
```

Откройте приложение на Android 8.0+ устройстве и выдайте системное VPN-разрешение через кнопку `Request VPN permission`.

## Key Features

- **Manual break flow** — ручной запуск dummy VPN из `MainActivity`.
- **MacroDroid trigger** — exported `TriggerReceiver` принимает Broadcast action `com.akfreedom.fakevpnbreaker.BREAK_VPN`; `TriggerActivity` остается fallback для consent flow.
- **Token check** — внешний intent должен передать `com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN`.
- **Macro template export** — кнопка `Save MacroDroid macro` сохраняет импортируемый `VPN_OFF.macro` с текущим trigger token.
- **Configurable hold duration** — допустимые значения: `300`, `500`, `1000`, `2000`, `5000 ms`.
- **Routing modes** — `Full takeover` для default route и `Local only` без default route.
- **Local event log** — последние 50 событий в `SharedPreferences`.

## Настройка MacroDroid

```text
MacroDroid explicit intent:
Target:  Broadcast
Package: com.akfreedom.fakevpnbreaker
Action:  com.akfreedom.fakevpnbreaker.BREAK_VPN
Extra name:  com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN
Extra value: token, shown on the FakeVpnBreaker main screen
```

Перед Broadcast-запуском откройте FakeVpnBreaker один раз и выдайте VPN permission. После принятого Broadcast приложение поднимает временный dummy VPN в фоне, удерживает его выбранное время и закрывает сервис.

Если Android блокирует background service start, используйте MacroDroid target `Activity` как fallback с теми же package, action и token extra. Activity fallback может показать системный VPN consent flow, а после завершения изолируется от основной `MainActivity`.

Чтобы не копировать intent-поля вручную, нажмите `Save MacroDroid macro` на главном экране FakeVpnBreaker. Android откроет системный выбор места сохранения, а приложение запишет `VPN_OFF.macro` с текущим `com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN`. Ожидаемые локальные логи: `MacroDroid macro save requested`, `MacroDroid macro saved`, `MacroDroid macro save cancelled` или безопасное сообщение `MacroDroid macro save failed: ...`.

Импорт сохраненного `VPN_OFF.macro` проверен на реальном устройстве: MacroDroid принимает файл, сохраняет action с package/action/token extra и запускает FakeVpnBreaker после настройки нужных триггеров. Шаблон использует Activity fallback и пример триггера запуска приложения `Пятёрочка`; после импорта добавьте или замените триггеры в MacroDroid под свой сценарий. Broadcast остается рекомендуемым фоновым вариантом для ручной настройки explicit intent.

Статический пример также доступен в [examples/VPN_OFF.macro](examples/VPN_OFF.macro), но в нем токен нужно заменить вручную.

---

## Documentation

| Guide | Description |
|-------|-------------|
| [Getting Started](docs/getting-started.md) | Релизный APK, установка, первый запуск |
| [Architecture](docs/architecture.md) | Структура и ключевые ограничения |
| [Manual Verification](docs/verification.md) | Проверка APK, VPN flow и MacroDroid |

## Privacy Model

- Нет `android.permission.INTERNET`.
- Нет внешних серверов, аналитики или телеметрии.
- Нет чтения установленных приложений, истории трафика, доменов или пакетов.
- Лог хранится локально и ограничен последними 50 событиями.

## License

Лицензия не указана.
