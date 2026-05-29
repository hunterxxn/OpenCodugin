# AGENTS.md

## Build & test commands

```bash
./gradlew buildPlugin   # compile and package plugin ZIP into build/distributions/
./gradlew check         # run all checks including tests (runs on CI as a separate job)
./gradlew verifyPlugin  # run plugin structure verification + IntelliJ Plugin Verifier
./gradlew runIde        # launch a sandbox IDE with the plugin loaded for manual testing
```

CI (`.github/workflows/build.yml`) runs in this order: `buildPlugin` → parallel(`check`, `verifyPlugin`) → `releaseDraft` (main branch only, skips PRs). Release publishes to JetBrains Marketplace via `release.yml` (triggered on published GitHub release, requires signing secrets).

## Toolchain

- Java 21 (Zulu distribution on CI)
- Kotlin 2.1.20 (declared in `settings.gradle.kts`)
- IntelliJ Platform Gradle Plugin 2.16.0
- Target IDE: IntelliJ IDEA 2025.2.6.2 (`build.gradle.kts`)
- Plugin compatibility: `sinceBuild=242` (2024.2) to `untilBuild=261.*` (2026.1.x)
- Windows-native development — use `gradlew.bat` on the host machine, `./gradlew` in CI (Ubuntu runners)

## Architecture

Single-module IntelliJ Platform Plugin. Entry points and key packages:

- `plugin.xml` (`src/main/resources/META-INF/`) — extension registrations (tool windows, actions, notification groups). Actions are registered via `<action>` with explicit `<add-to-group>` entries; there are no startup activities or project listeners.
- `MyBundle.properties` (`src/main/resources/messages/`) — localizable strings with `{0}` placeholders, accessed via `MyBundle["key", args...]` (Kotlin operator overloading on `DynamicBundle`).
- Source root: `src/main/kotlin/com/github/hunterxxn/opencodugin/`
  - `terminal/` — `OpenCodeToolWindowFactory`, `OpenCodeSessionManager` (`@Service(Service.Level.PROJECT)`), `OpenCodeTerminalPanel` (PTY + jediterm UI), `OpenCodeTerminalRunner` (process launcher via pty4j), `OpenCodeTerminalSettings` (font & color config)
  - `commands/` — `OpenCodeFileReferenceAction` (AnAction), `OpenCodeContextInjector` (writes file refs into PTY stdin)
  - `update/` — `CheckUpdateAction`, `UpdateChecker` (GitHub Releases API client)
- Test root: `src/test/kotlin/com/github/hunterxxn/opencodugin/`
- Embedded font: `src/main/resources/fonts/MapleMono-NF-CN-Regular.ttf` (loaded at runtime by `OpenCodeTerminalSettings`)

Plugin ID: `com.github.hunterxxn.opencodugin`

## Service & component registration

- Lightweight services use the `@Service` annotation on the class (e.g. `@Service(Service.Level.PROJECT)`), retrieved via `project.service<T>()`. These do not need a `<projectService>` entry in `plugin.xml`.
- Tool windows, actions, and notification groups must still be declared in `plugin.xml` — IntelliJ Platform does not classpath-scan for these extension points.
- Stateless utilities (runners, injectors, checkers) use Kotlin `object` declarations, not services.

## Testing

- **JUnit 4** (not JUnit 5). Methods follow `fun testXxx()` convention — no `@Test` annotations.
- Test base class: `com.intellij.testFramework.fixtures.BasePlatformTestCase` (provides `Project` fixture for service testing).
- `org.opentest4j:opentest4j` is available as a test dependency for additional assertions.
- Test data lives under `src/test/testData/`.
- `check` task runs tests. The CI `test` job depends on `build` completing first.

## Important constraints

- Gradle Configuration Cache and Build Cache are both enabled (`gradle.properties`). If a build behaves unexpectedly, clear caches with `./gradlew --stop` or delete `~/.gradle/caches/`.
- Dependabot updates Gradle and GitHub Actions dependencies daily (`.github/dependabot.yml`). Review generated PRs promptly.
- Plugin signing secrets (`PUBLISH_TOKEN`, `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, `PRIVATE_KEY_PASSWORD`) are required for marketplace publishing via the `release.yml` workflow.
- The `org.jetbrains.changelog` Gradle plugin provides `getChangelog` and `patchChangelog` tasks used in CI — do not edit `CHANGELOG.md` format without understanding these tasks.
- `kotlin.stdlib.default.dependency = false` is set in `gradle.properties` (IntelliJ Platform bundles its own Kotlin stdlib).
