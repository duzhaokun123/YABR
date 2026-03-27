# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Overview

YABR - Android Xposed/hook framework targeting Bilibili app. Multi-loader, multi-hooker architecture.

## Build Commands

```bash
./gradlew assembleDebug      # Build debug APK
./gradlew assembleRelease    # Build release APK
./gradlew :app:assembleDebug # Build only app module
./gradlew :core:compileDebugKotlin  # Compile core module only
./gradlew clean              # Clean build artifacts
./gradlew dependencies       # Show dependencies tree
```

No test suite exists in this project.

## Architecture (Non-Obvious)

### Module Registration (KSP Auto-generated)
- New modules are **auto-discovered** via KSP annotation processor in `annotation/`
- The processor generates `io.github.duzhaokun123.codegen.ModuleEntries` at build time
- Every module class **must** have `@ModuleEntry(id = "...")` annotation — ID must be globally unique; use full class name
- All `@ModuleEntry` classes are Kotlin `object` (singleton) — `INSTANCE` is used by codegen

### Module Loading Order
- `Core` interface modules load first, unconditionally, cannot unload by default
- `SwitchModule` modules are **disabled by default** and skipped unless user-enabled
- Within same priority group, topological sort resolves `dependencies` field of `@ModuleEntry`
- Dependencies across different priority groups are **NOT** resolved

### Critical Patterns
- `onLoad()` **must return `true`** for success; returning non-`true` logs a warning but `loaded = true` is still set
- Hook exceptions inside `BaseModule.hook/hookBefore/hookAfter/hookReplace` are swallowed and logged — no need for extra try/catch
- Use `multiLoadAllSuccess { }` / `multiLoadAnySuccess { }` for multiple independent load steps instead of sequential code
- Use `loadSkip()` (returns `true`) as semantic alias when a module intentionally skips work
- Per-module config: `ConfigStore.ofModule(this)` — stored as SharedPreferences `yabr_config_<module.id>`
- Unhookers added via `Member.hook(...)` extension are **auto-collected** in `unhookers` list and auto-invoked on `onUnload()`

### Package Isolation Rule
- Module code in `dev.o0kam1.*` or `com.example.*` packages **must NOT** import from other modules' packages
- Only framework packages (`io.github.duzhaokun123.yabr.*`) are allowed as cross-module imports

### Dependency Quirk
- `io.github.libxposed:api` must be installed to **mavenLocal** — it is NOT on any public Maven repo
- `de.robv.android.xposed:api` comes from `https://api.xposed.info` (custom Maven repo declared in `settings.gradle.kts`)

## UI Interfaces for Modules
- `UISwitch` — simple toggle switch in settings
- `UIComplex` — complex settings UI
- `UIActivity` — launches a separate Activity for settings
- `UIClick` — clickable item (no toggle)

## Code Style Guidelines

### Kotlin Code Style
- Uses **official** Kotlin code style (configured in `gradle.properties`)
- 4-space indentation, no tabs
- No semicolons
- Single expression functions can use expression syntax: `override fun onLoad() = true`
- Use `val` by default; `var` only when mutation is necessary

### Naming Conventions
| Element | Convention | Example |
|---------|------------|---------|
| Classes | PascalCase | `BaseModule`, `HookCallback` |
| Functions | camelCase | `hookBefore`, `loadClass` |
| Properties | camelCase | `canUnload`, `unhookers` |
| Constants | SCREAMING_SNAKE_CASE | `COVER_ID = 529357L` |
| Packages | lowercase | `io.github.duzhaokun123.yabr.utils` |
| Module IDs | Full class name | `dev.o0kam1.tools.Cover` |
| Interfaces | PascalCase | `HookerContext`, `Core` |
| Type aliases | PascalCase | `Unhooker = () -> Unit` |

### Import Order
1. `android.*` / `java.*` / `javax.*`
2. `kotlin.*`
3. `androidx.*`
4. `org.jetbrains.*`
5. Third-party libraries (sorted alphabetically)
6. `io.github.duzhaokun123.*` (framework packages)
7. Internal project imports

### Module Class Structure
```kotlin
@ModuleEntry(
    id = "full.package.ClassName",
    targets = [ModuleEntryTarget.MAIN]  // optional
)
object ModuleName : BaseModule(), SwitchModule, UISwitch {
    override val name = "Display Name"
    override val description = "What this module does"
    override val category = UICategory.TOOL  // TOOL, UI, ABOUT, FUN, DEBUG

    override fun onLoad(): Boolean {
        // hook setup here
        return true
    }

    override fun onUnload(): Boolean {
        // cleanup here
        return super.onUnload()
    }
}
```

### Error Handling
- Hook callbacks already wrapped in `runCatching` — don't add extra try/catch
- Use `.logError()` extension for `Result<*>` error handling
- Use `runCatching` for reflection-based code
- Log errors with `logger.e(t)` or `logger.e(message, t)`
- Use `logger.w(t)` for non-critical failures in `onLoad`

### Reflection Utilities
Use extension functions from `io.github.duzhaokun123.yabr.utils`:
- `loadClass(signature: String)` — load class by signature
- `Class.new(*args)` — instantiate via constructor
- `obj.getFieldValue(name)` / `obj.setFieldValue(name, value)`
- `obj.invokeMethod(name, *args)` / `obj.invokeMethodAs<T>(name, *args)`
- `Class.findMethod { it.name == "x" }` — find methods via DexKit

### Debug Build Modules
Modules in `modules/src/debug/java/` (like `SSLUnpin.kt`) are debug-only and not included in release builds. Use for development testing.

### Gradle Configuration
- Java target: 11
- Kotlin JVM toolchain: 11
- Android compile SDK: 36
- Min SDK: 23
- Compose enabled only in `core` module
