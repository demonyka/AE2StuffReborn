# Публикация на Modrinth и CurseForge

Проекты на площадках создаются вручную один раз. После этого каждый релиз на GitHub сам загружает мод на обе площадки через `.github/workflows/publish.yml`.

## 1. Проект на Modrinth

На https://modrinth.com/dashboard/projects нажми **Create a project**.

| Поле | Значение |
| --- | --- |
| Name | AE2 Stuff Reforged |
| URL | `ae2stuff-reforged` |
| Summary | Pattern Encoder, Crystal Growth Chamber, Advanced Inscriber and Wireless Connector for Applied Energistics 2 on Minecraft 1.16.5. A port of bdew's AE2 Stuff. |
| Icon | `docs/icon.png` |
| Description | текст из `docs/description.md` |
| Categories | Technology, Storage, Utility |
| Client side / Server side | Required / Required |
| License | MIT |
| Source code | https://github.com/demonyka/AE2StuffReforged |
| Issue tracker | https://github.com/demonyka/AE2StuffReforged/issues |
| Gallery | `docs/screenshots/`: encoder, jei-transfer, inscriber, wireless, visualiser, grower-hwyla, creative-tab |

ID проекта (8 символов) показан в настройках проекта. Он понадобится в шаге 3.

## 2. Проект на CurseForge

На https://authors.curseforge.com/#/projects нажми **Create project**, игра Minecraft, класс Mods.

| Поле | Значение |
| --- | --- |
| Name | AE2 Stuff Reforged |
| Summary | то же, что на Modrinth |
| Main category | Technology |
| Additional categories | Applied Energistics 2, Storage |
| Logo | `docs/icon.png` (нужно не меньше 400×400, у иконки 512×512) |
| Description | `docs/description.md`, редактор переключи на Markdown |
| License | MIT License |
| Source | https://github.com/demonyka/AE2StuffReforged |
| Issues | https://github.com/demonyka/AE2StuffReforged/issues |
| Images | те же скриншоты из `docs/screenshots/` |

Модерация CurseForge обычно занимает от нескольких часов до пары дней. Project ID виден справа на странице проекта в блоке About Project.

## 3. Доступ для автопубликации

Токены:

- Modrinth: https://modrinth.com/settings/pats, создай токен с правами **Create versions** и **Write projects**.
- CurseForge: https://authors.curseforge.com/account/api-tokens, создай токен.

Добавь их в репозиторий:

```
gh variable set MODRINTH_ID --repo demonyka/AE2StuffReforged --body <ID с Modrinth>
gh variable set CURSEFORGE_ID --repo demonyka/AE2StuffReforged --body <Project ID с CurseForge>
gh secret set MODRINTH_TOKEN --repo demonyka/AE2StuffReforged
gh secret set CURSEFORGE_TOKEN --repo demonyka/AE2StuffReforged
```

Площадка без токена просто пропускается, так что можно выкладывать и на одну из них.

## 4. Релиз

Опубликуй черновик релиза `v1.0.0` на https://github.com/demonyka/AE2StuffReforged/releases.
Тег должен быть `v` плюс `mod_version` из `gradle.properties`, иначе workflow остановится до сборки.
Workflow **publish** соберёт jar и загрузит его так:

| | Modrinth | CurseForge |
| --- | --- | --- |
| Версия | `1.0.0+1.16.5`, Release | то же |
| Загрузчик и игра | Forge, 1.16.5 | Forge, 1.16.5, Java 8 |
| Changelog | раздел из `CHANGELOG.md` | то же |
| Applied Energistics 2 | Required | Required |
| JEI, Hwyla, Jade | Optional | Optional |

Если проекты ещё на модерации или токены не заданы, workflow упадёт. Его можно повторить вручную: вкладка Actions, **publish**, Run workflow.

У AE2 на Modrinth нет файлов для 1.16.5, поэтому в описании стоит ссылка на CurseForge.

## Следующие версии

1. Поменять `mod_version` в `gradle.properties` и добавить раздел `## <версия> - <дата>` в `CHANGELOG.md`.
2. Закоммитить, создать тег и опубликовать релиз на GitHub. Остальное сделает workflow.
