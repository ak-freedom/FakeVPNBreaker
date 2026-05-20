# AGENTS.md

> Этот файл помогает агентам и разработчикам быстро понять структуру проекта. Обновляйте его при существенных изменениях архитектуры или дерева файлов.

## Обзор проекта

FakeVpnBreaker - Android/Kotlin utility app, который кратковременно поднимает локальный dummy VPN через `VpnService`, чтобы прервать текущий активный VPN-клиент, затем сам завершает VPN-сеанс.

## Tech Stack

- **Язык программирования:** Kotlin
- **Платформа:** Android
- **Сервисный API:** Android `VpnService`
- **UI:** native Android XML без Compose
- **Хранилище настроек:** `SharedPreferences`
- **Локальный лог:** `SharedPreferences`, последние 50 событий
- **База данных:** не используется
- **Сетевые интеграции:** отсутствуют

## Структура проекта

```text
.ai-factory/              # AI Factory контекст проекта
  DESCRIPTION.md          # описание продукта и требований
  ARCHITECTURE.md         # архитектурные правила
  rules/base.md           # базовые правила разработки
.agents/                  # локальные AI Factory skills
.codex/                   # локальная конфигурация Codex
app/                      # Android application module
  build.gradle.kts        # конфигурация Android/Kotlin module
  src/main/
    AndroidManifest.xml   # permissions, Activity и VpnService declarations
    java/com/akfreedom/fakevpnbreaker/
      MainActivity.kt     # основной UI и ручной запуск
      TriggerReceiver.kt  # recommended exported Broadcast entry point для MacroDroid
      TriggerActivity.kt  # isolated Activity fallback для MacroDroid
      vpn/                # dummy VPN service и orchestration
      settings/           # допустимые настройки и SharedPreferences repository
      logging/            # локальный ring buffer событий
    res/                  # XML layout/theme/string resources
  src/test/               # focused unit tests для pure Kotlin логики
docs/
  getting-started.md      # релизный APK, установка и первый запуск
  architecture.md         # структура приложения и ограничения
  verification.md         # manual Android verification checklist
AGENTS.md                 # карта проекта для агентов
```

## Ключевые точки входа

| Файл | Назначение |
|------|------------|
| `.ai-factory/DESCRIPTION.md` | Продуктовое и техническое описание FakeVpnBreaker |
| `.ai-factory/ARCHITECTURE.md` | Архитектурные решения и ограничения |
| `.ai-factory/rules/base.md` | Базовые правила реализации |
| `.codex/config.toml` | Локальная конфигурация Codex и MCP |
| `settings.gradle.kts` | Gradle project settings |
| `build.gradle.kts` | Root Gradle plugin versions |
| `app/build.gradle.kts` | Android app module configuration |
| `app/src/main/AndroidManifest.xml` | Android entry points and VPN service declaration |
| `app/src/main/java/com/akfreedom/fakevpnbreaker/MainActivity.kt` | Main UI and manual break flow |
| `app/src/main/java/com/akfreedom/fakevpnbreaker/TriggerReceiver.kt` | MacroDroid Broadcast entry point |
| `app/src/main/java/com/akfreedom/fakevpnbreaker/TriggerActivity.kt` | MacroDroid Activity fallback |
| `app/src/main/java/com/akfreedom/fakevpnbreaker/vpn/FakeVpnService.kt` | Dummy VPN lifecycle implementation |

## Документация

| Документ | Путь | Описание |
|----------|------|----------|
| README | `README.md` | Landing page проекта |
| Getting Started | `docs/getting-started.md` | Релизный APK и первый запуск |
| Architecture | `docs/architecture.md` | Структура и ограничения |
| Manual Verification | `docs/verification.md` | Ручной чеклист проверки |
| Project Description | `.ai-factory/DESCRIPTION.md` | Основные требования к Android-приложению |
| AI Architecture | `.ai-factory/ARCHITECTURE.md` | Правила структуры AI Factory |

## AI Context Files

| Файл | Назначение |
|------|------------|
| `AGENTS.md` | Карта проекта для AI-агентов |
| `.ai-factory/DESCRIPTION.md` | Описание проекта и ограничений |
| `.ai-factory/ARCHITECTURE.md` | Архитектурные рекомендации |
| `.ai-factory/rules/base.md` | Базовые правила разработки |

## Правила для агентов

- Не объединяйте несколько shell-команд в одну строку через `&&`, `;` или pipe, если их можно выполнить отдельно.
- Некорректно: `git checkout master && git pull`
- Корректно: сначала `git checkout master`, затем `git pull origin master`
- Не создавайте Android-код в рамках `$aif`; для реализации используйте `$aif-plan` и `$aif-implement` или явный запрос на implementation.
