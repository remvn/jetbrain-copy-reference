# Copy Reference

An IntelliJ Platform plugin that copies project-relative file and directory references for use in prompts, documentation, and code reviews.

## Usage

Right-click a file in the editor and select **Copy Reference**. The copied value includes the caret line or selected line range:

```text
@src/main/kotlin/Main.kt:12
@src/main/kotlin/Main.kt:1-12
```

Right-click a file or directory in the Project tool window to copy a reference without a line number:

```text
@src/main/kotlin/Main.kt
@src/main/kotlin
```

References always use paths relative to the project root and `/` path separators. The action is unavailable for multiple Project view selections and items outside the project.

## Development

Run the tests and build the plugin:

```shell
./gradlew check buildPlugin
```

Launch a sandbox IDE with the plugin installed:

```shell
./gradlew runIde
```
