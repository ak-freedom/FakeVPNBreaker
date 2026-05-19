# FakeVpnBreaker

> Android utility app, который кратковременно поднимает локальный dummy VPN, чтобы освободить текущий активный VPN-сеанс.

FakeVpnBreaker использует стандартный Android `VpnService`: приложение получает системное VPN-разрешение, на короткое время становится активным VPN и затем закрывает собственный VPN-интерфейс. Оно не требует root, Shizuku, `INTERNET` permission или интеграции с конкретными VPN-клиентами.

## Quick Start

Скачайте готовый APK из [latest GitHub Release](https://github.com/ak-freedom/FakeVPNBreaker/releases/latest).

Текущий релизный артефакт: `FakeVpnBreaker-v1.0.1.apk`.

```bash
adb install FakeVpnBreaker-v1.0.1.apk
```

Откройте приложение на Android 8.0+ устройстве и выдайте системное VPN-разрешение через кнопку `Request VPN permission`.

## Key Features

- **Manual break flow** — ручной запуск dummy VPN из `MainActivity`.
- **MacroDroid trigger** — exported `TriggerActivity` принимает action `com.akfreedom.fakevpnbreaker.BREAK_VPN`.
- **Token check** — внешний intent должен передать `com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN`.
- **Configurable hold duration** — допустимые значения: `300`, `500`, `1000`, `2000`, `5000 ms`.
- **Routing modes** — `Full takeover` для default route и `Local only` без default route.
- **Local event log** — последние 50 событий в `SharedPreferences`.

## Example

```text
MacroDroid explicit intent:
Package: com.akfreedom.fakevpnbreaker
Action:  com.akfreedom.fakevpnbreaker.BREAK_VPN
Extra name:  com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN
Extra value: token, shown on the FakeVpnBreaker main screen
```

После запуска FakeVpnBreaker поднимает временный dummy VPN, удерживает его выбранное время и закрывает сервис.

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
