Вкратце:
gradlew bootstrap:runDebug - сборка тл, бустрапа, их запуск в дебаг-режиме
gradlew bootstrap:minifiedJar - сборка бустрапа + минификация прогуардом
gradlew bootstrap:createExe - сборка бустрапа + минификация прогуардом + заворачивание в launch4j
gradlew launcher:jar - сборка джарника тл

gradlew launcher:buildLauncherRepo - сборка папки /lib для запуска бустрапа

при компиляции автоматически обновляются файлы meta.json
информация для meta.json задается с помощью переменных окружения BRAND, SHORT_BRAND и MAIN_CLASS (для launcher)
Если их нет - используются дефолты, которые, в свою очередь, зависят от имени ветки гита (если найден гит-репозиторий)
список библиотек, попадающих в meta.json launcher-а, задается конфигурацией tlRuntime в build.gradle

при bootstrap:runDebug автоматически выполняется launcher:buildLauncherRepo
при сборке минифицированного джарника автоматически ремапятся все библиотеки в их shaded-версии (можно легко отключить)

todo: update.json, сборка mcl-версии
