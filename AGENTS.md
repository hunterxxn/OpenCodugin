# AGENTS.md

## Build & test commands

```bash
./gradlew buildPlugin   # compile and package plugin ZIP into build/distributions/
./gradlew check         # run all checks including tests (runs on CI as a separate job)
./gradlew verifyPlugin  # run plugin structure verification + IntelliJ Plugin Verifier
./gradlew runIde        # launch a sandbox IDE with the plugin loaded for manual testing
```

CI (`.github/workflows/build.yml`) runs in this order: `buildPlugin` → parallel(`check`, `verifyPlugin`) → `releaseDraft` (main branch only, skips PRs).

## Toolchain

- Java 21 (Zulu distribution used on CI)
- Kotlin 2.1.20 (declared in `settings.gradle.kts`)
- IntelliJ Platform Gradle Plugin 2.16.0
- Target IDE: IntelliJ IDEA 2025.2.6.2 (set in `build.gradle.kts`)
- Windows-native development — use `gradlew.bat` on the host machine, `./gradlew` in CI (Ubuntu runners)

## Architecture

Single-module IntelliJ Platform Plugin (no monorepo). Entry points:

- `plugin.xml` (`src/main/resources/META-INF/`) — all extension registrations (tool windows, startup activities, services)
- `MyBundle.properties` (`src/main/resources/messages/`) — localizable strings, accessed via `MyBundle["key", args...]`
- Source root: `src/main/kotlin/com/github/hunterxxn/opencodugin/`
- Test root: `src/test/kotlin/com/github/hunterxxn/opencodugin/`

Plugin ID: `com.github.hunterxxn.opencodugin`

## Testing

- **JUnit 4** (not JUnit 5). Test class: `BasePlatformTestCase` from the IntelliJ Platform test framework.
- Test methods follow `fun testXxx()` convention — no `@Test` annotations.
- Test data lives under `src/test/testData/`.
- `check` task runs tests. The CI `test` job depends on `build` completing first.

## Important constraints

- All plugin contributions (services, tool windows, startup activities, listeners) must be registered in `plugin.xml` — IntelliJ Platform uses XML-based extension points, not automatic classpath scanning.
- Gradle Configuration Cache and Build Cache are both enabled (`gradle.properties`). If a build behaves unexpectedly, clear caches with `./gradlew --stop` or delete `~/.gradle/caches/`.
- Dependabot updates Gradle and GitHub Actions dependencies daily. Review generated PRs promptly.
- Plugin signing secrets (`PUBLISH_TOKEN`, `CERTIFICATE_CHAIN`, `PRIVATE_KEY`, `PRIVATE_KEY_PASSWORD`) are required for marketplace publishing via the release workflow.
