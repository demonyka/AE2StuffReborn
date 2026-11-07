# Публикация на Modrinth

## Проект (создаётся один раз)

На https://modrinth.com/dashboard/projects нажми **Create a project**.

| Поле | Значение |
| --- | --- |
| Name | AE2 Stuff Reforged |
| URL | `ae2stuff-reforged` (свободен, проверено) |
| Summary | Pattern Encoder, Crystal Growth Chamber, Advanced Inscriber and Wireless Connector for Applied Energistics 2 on Minecraft 1.16.5. A port of bdew's AE2 Stuff. |
| Visibility | Public |
| Icon | `docs/icon.png` |
| Description | текст из `docs/modrinth.md` |
| Categories | Technology, Storage, Utility |
| Client side | Required |
| Server side | Required |
| License | MIT |
| Source code | https://github.com/demonyka/AE2StuffReforged |
| Issue tracker | https://github.com/demonyka/AE2StuffReforged/issues |

Gallery: файлы из `docs/screenshots/`. Удачный порядок: `encoder.png`, `jei-transfer.png`, `inscriber.png`, `wireless.png`, `visualiser.png`, `grower-hwyla.png`, `creative-tab.png`.

Перед публикацией Modrinth отправляет новый проект на модерацию, обычно это занимает от нескольких часов до пары дней.

## Версия

| Поле | Значение |
| --- | --- |
| Файл | `AE2StuffReforged-1.16.5-1.0.0.jar` из релиза на GitHub или `build/libs/` |
| Version name | AE2 Stuff Reforged 1.0.0 |
| Version number | `1.0.0+1.16.5` |
| Channel | Release |
| Loaders | Forge |
| Game versions | 1.16.5 |
| Changelog | раздел 1.0.0 из `CHANGELOG.md` |

Зависимости:

| Проект | Тип |
| --- | --- |
| Applied Energistics 2 (`ae2`) | Required |
| Just Enough Items (`jei`) | Optional |
| Hwyla (`hwyla`) | Optional |
| Jade (`jade`) | Optional |

У AE2 на Modrinth нет файлов для 1.16.5, только на CurseForge. Зависимость всё равно стоит указать как проект, а в описании уже есть ссылка на CurseForge.

## Следующие версии

1. Поменять `mod_version` в `gradle.properties` и добавить раздел в `CHANGELOG.md`.
2. Собрать: `JAVA_HOME=<JDK 8> ./gradlew build`.
3. Закоммитить, поставить тег `v<версия>`, выложить jar в релиз GitHub и загрузить его на Modrinth с теми же полями.
