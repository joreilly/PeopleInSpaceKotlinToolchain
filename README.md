# PeopleInSpace (Kotlin Toolchain)

A minimal version of [PeopleInSpace](https://github.com/joreilly/PeopleInSpace) built with the
[Kotlin Toolchain](https://kotlin-toolchain.org/latest/) instead of Gradle. It shows who is in space right now and the
current position of the ISS, using Compose Multiplatform on Android, iOS and Desktop, with Ktor and
kotlinx.serialization in the shared module.

The whole build is a few `module.yaml` files (`shared`, `androidApp`, `iosApp`, `desktopApp`), a `project.yaml` and a
`libs.versions.toml`. There's no `build.gradle.kts`, `settings.gradle.kts` or Gradle wrapper.

- [/androidApp](./androidApp) contains the Android application.
- [/desktopApp](./desktopApp) contains the desktop (JVM) application.
- [/iosApp](./iosApp) contains the iOS application.
- [/shared](./shared) holds the code shared across your applications — Compose UI,
  business logic, and platform-specific implementations. [src](./shared/src) is for common code; the
  sibling `src@<platform>` folders (for example `src@android`) hold code compiled only for the platform named in the
  folder.

The `kotlin` (macOS/Linux) and `kotlin.bat` (Windows) scripts in the project root are self-bootstrapping wrappers for
the Kotlin Toolchain: they download the pinned toolchain version on first use, so no separate installation is required.
Build the whole project with `./kotlin build`.

### Running

Run each module from the command line:

- Android app: `./kotlin run -m androidApp`
- Desktop app: `./kotlin run -m desktopApp`
- iOS app: `./kotlin run -m iosApp`

### Testing

Run the project's tests with `./kotlin test`, or `./kotlin test -m <module>` for a single module.

---

Learn more
about [Kotlin Multiplatform](https://www.jetbrains.com/help/kotlin-multiplatform-dev/get-started.html), [Kotlin Toolchain](https://kotlin-toolchain.org/latest/), [Compose Multiplatform](https://kotlinlang.org/compose-multiplatform/), [Kotlin/Wasm](https://kotl.in/wasm/), [Ktor](https://ktor.io/).
