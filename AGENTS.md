# AGENTS.md

This file provides guidance to agents when working with code in this repository.

## Project Overview
YABR - Android Xposed/hook framework targeting Bilibili app. Multi-loader, multi-hooker architecture.

## Build Commands
```bash
./gradlew assembleDebug      # Build debug APK
./gradlew assembleRelease    # Build release APK
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
