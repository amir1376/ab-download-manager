# Contributing to Xeton

Thank you for your interest in contributing to Xeton! We appreciate your help in making this the fastest and most elegant desktop download manager.

Since Xeton is built with a **Kotlin Compose Multiplatform frontend** and a **Rust core backend (`xeton_core`)** linked via **UniFFI**, please read the guidelines below to ensure a smooth contribution process.

---

## 🗺️ Project Architecture overview

Before writing code, it's important to understand where changes belong:

- **UI & Presentation (`desktop/`, `android/`, `shared/`):** Written in Kotlin using Compose Multiplatform. Contains view models, pages, themes, localizations, and platform-specific service wrappers.
- **Download Engine Core (`rust/xeton_core/`):** Written in Rust using Tokio, reqwest, suppaftp, and librqbit. Handles database persistence, download jobs, part splitting, speed limiting, P2P BitTorrent, platform extractors, and transcoding.
- **FFI Boundary (`rust/xeton_core/src/xeton_core.udl`):** Defines the bridge between Kotlin and Rust. UniFFI compiles the Rust types into a shared library (`cdylib` / `staticlib`) and generates Kotlin bindings.

---

## 🛠️ Developer Setup

To build and test the project locally, you need:
1. **JDK 17+** (JetBrains Runtime recommended).
2. **Rust Toolchain** (latest stable `rustc` and `cargo` installed via [rustup](https://rustup.rs/)).
3. **FFmpeg** installed and accessible in your system path (required for audio extraction/transcoding features).

To verify the Rust core compiles:
```bash
cd rust/xeton_core
cargo check --all-targets
cargo test
```

To build the FFI bindings and compile the shared libraries:
```bash
./gradlew buildRustCore
```

---

## 🤝 How to Contribute

### 1. Bug Reports & Feature Requests
- Check existing Issues and Discussions to ensure the topic hasn't already been addressed.
- Use the templates provided when opening a new issue, and include system details (OS, version, logs) and reproduction steps where applicable.

### 2. Translations
- All translations are managed via **Crowdin**.
- **Please DO NOT submit translations via GitHub Pull Requests.** PRs with translation updates will be closed.
- Translate or review languages on the [Xeton Crowdin Project](https://crowdin.com/project/xeton-download-manager).

### 3. Pull Requests (PRs)
- **Discuss First:** For any non-trivial changes, please open an issue or discussion first so we can align on design choices.
- **Branch Naming:** Use descriptive branch names: `feature/your-feature` or `fix/your-fix`.
- **Formatting & Linting:**
  - **Rust:** Run `cargo fmt` and `cargo clippy --all-targets` before committing.
  - **Kotlin:** Ensure code adheres to standard Kotlin style guidelines.

---

## 🔒 Code Guidelines

### Rust Core (`xeton_core`)
- **No Blocking Operations on Tokio Threads:** Use `tokio::fs` or delegate heavy blocking disk tasks to the actor channel (`DiskActor` in `destination/mod.rs`).
- **Database Schema & Serialization:** Persistence is handled by **SurrealDB v3** with **Surrealkv**. Make sure structs stored in the database implement `serde::Serialize`, `serde::Deserialize`, and derive `surrealdb::SurrealValue`.
- **Updating the FFI:** If you need to expose new functions, enums, or structs to the Kotlin UI:
  1. Add them to `rust/xeton_core/src/xeton_core.udl`.
  2. Implement/wrap them in `rust/xeton_core/src/uniffi_api.rs`.
  3. Run `./gradlew buildRustCore` to regenerate the Kotlin FFI classes.

### Kotlin UI
- Maintain unidirectional data flow. UI components should react to state flows emitted by the view models and backend engine.
- Keep UI components reusable and avoid platform-specific logic directly in common composables; use expect/actual declarations when needed.
