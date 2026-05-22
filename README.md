# FakeVpnBreaker

> Android utility app, который кратковременно поднимает локальный dummy VPN, чтобы освободить текущий активный VPN-сеанс.

FakeVpnBreaker использует стандартный Android `VpnService`: приложение получает системное VPN-разрешение, на короткое время становится активным VPN и затем закрывает собственный VPN-интерфейс. Оно не требует root, Shizuku, разрешения `INTERNET` или интеграции с конкретными VPN-клиентами.

## Quick Start

Скачайте готовый APK из [latest GitHub Release](https://github.com/ak-freedom/FakeVPNBreaker/releases/latest).

Текущий релизный артефакт: `FakeVpnBreaker-v1.1.0.apk`.

```bash
adb install FakeVpnBreaker-v1.1.0.apk
```

Откройте приложение на Android 8.0+ устройстве и выдайте системное VPN-разрешение через кнопку `Request VPN permission`.

## Key Features

- **Manual break flow** — ручной запуск dummy VPN из `MainActivity`.
- **MacroDroid trigger** — экспортированный `TriggerReceiver` принимает Broadcast action `com.akfreedom.fakevpnbreaker.BREAK_VPN`; `TriggerActivity` остается вариантом запуска через экран, когда нужен системный VPN consent или Android блокирует фоновый старт.
- **Token check** — внешний intent должен передать `com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN`.
- **Sanitized trigger logs** — Broadcast и `TriggerActivity` логируют только категории action (`expected`, `missing`, `unsupported`), без raw action и токена.
- **Macro template export** — кнопка `Save MacroDroid macro` сохраняет импортируемый `VPN_OFF.macro` с текущим токеном триггера.
- **Configurable hold duration** — допустимые значения: `300`, `500`, `1000`, `2000`, `5000 ms`.
- **Routing modes** — `Full takeover` временно направляет весь трафик через FakeVpnBreaker, `Local only` запускает более узкий локальный VPN без общего маршрута.
- **UI language selector** — переключение интерфейса между English и Russian.
- **Local event log** — последние 50 событий в `SharedPreferences`.

## v1.1.0 Release Notes

- Версия приложения отображается рядом с названием на главном экране.
- Обновлена launcher icon, включая adaptive и monochrome варианты.
- Добавлены подсказки для длительности удержания, режима маршрутизации, закрытия `TriggerActivity` и настройки MacroDroid.
- Добавлен переключатель языка English/Russian; сохраненные сообщения локального журнала не переводятся при смене языка.
- Проверены release build, unit tests, отсутствие `android.permission.INTERNET`, ручной VPN flow и импорт `VPN_OFF.macro` в MacroDroid.

## Настройки на главном экране

- `Hold duration` задает, сколько времени временный dummy VPN остается активным перед закрытием. Более длинное значение может помочь Android надежнее переключиться с предыдущего VPN.
- `Routing mode` определяет, какой трафик Android на короткое время направит через FakeVpnBreaker: `Full takeover` перехватывает весь трафик устройства, `Local only` запускает более узкий локальный VPN.
- `Close TriggerActivity after service start` закрывает временный экран `TriggerActivity` после запуска сервиса. Для автоматизации через MacroDroid настройку лучше оставить включенной.
- `MacroDroid trigger setup` содержит сохранение импортируемого `.macro` и explicit intent поля для защищенного токеном внешнего триггера. Кнопка `Save MacroDroid macro` находится первой в этом разделе, чтобы основной сценарий настройки был виден сразу.

## Настройка MacroDroid

```text
MacroDroid explicit intent:
Target:  Broadcast
Package: com.akfreedom.fakevpnbreaker
Action:  com.akfreedom.fakevpnbreaker.BREAK_VPN
Extra name:  com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN
Extra value: token shown on the FakeVpnBreaker main screen
```

Перед Broadcast-запуском откройте FakeVpnBreaker один раз и выдайте VPN-разрешение. После принятого Broadcast приложение поднимает временный dummy VPN в фоне, удерживает его выбранное время и закрывает сервис.

Если Android блокирует фоновый запуск сервиса, используйте MacroDroid target `Activity` с теми же package, action и token extra. Такой запуск открывает `TriggerActivity`, может показать системный экран VPN-разрешения, а после завершения изолируется от основной `MainActivity`.

Чтобы не копировать intent-поля вручную, нажмите `Save MacroDroid macro` на главном экране FakeVpnBreaker. Android откроет системный выбор места сохранения, а приложение запишет `VPN_OFF.macro` с текущим `com.akfreedom.fakevpnbreaker.EXTRA_TRIGGER_TOKEN`. Ожидаемые записи локального журнала: `MacroDroid macro save requested`, `MacroDroid macro saved`, `MacroDroid macro export cancelled` или безопасные сообщения восстановления для picker/template/output/write/permission failure.

Импорт сохраненного `VPN_OFF.macro` проверен на реальном устройстве: MacroDroid принимает файл, сохраняет action с package/action/token extra и запускает FakeVpnBreaker после настройки нужных триггеров. Шаблон использует запуск через `TriggerActivity` и пример триггера запуска приложения `Пятёрочка`; после импорта добавьте или замените триггеры в MacroDroid под свой сценарий. Broadcast остается рекомендуемым фоновым вариантом для ручной настройки explicit intent.

Статический пример также доступен в [examples/VPN_OFF.macro](examples/VPN_OFF.macro), но в нем токен нужно заменить вручную.

---

## Documentation

| Guide | Description |
|-------|-------------|
| [Getting Started](docs/getting-started.md) | Релизный APK, установка, первый запуск |
| [Architecture](docs/architecture.md) | Структура и ключевые ограничения |
| [Manual Verification](docs/verification.md) | Проверка APK, VPN-сценариев и MacroDroid |

## Privacy Model

- Нет `android.permission.INTERNET`.
- Нет внешних серверов, аналитики или телеметрии.
- Нет чтения установленных приложений, истории трафика, доменов или пакетов.
- Лог хранится локально и ограничен последними 50 событиями.
- Логи не должны содержать токен триггера, raw unsupported action, generated macro JSON или пользовательский трафик.

## Recovery Diagnostics

Локальный журнал использует фиксированные безопасные категории. Типовые состояния: missing VPN permission с подсказкой открыть FakeVpnBreaker и выдать разрешение, invalid trigger token без значения токена, unsupported action без raw action, foreground/service start failure с рекомендацией запуска через `TriggerActivity`, VPN establish failure, repeated start replacement и MacroDroid export failures без URI, document path или generated `.macro` content.

## License

Лицензия не указана.
