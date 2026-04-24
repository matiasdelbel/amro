# AMRO

A Jetpack Compose Android app that showcases the top-100 trending movies of the week (via
[TMDB](https://www.themoviedb.org/)), with filtering by genre, sorting, and a detailed movie
screen.

This README is written as a handover: it describes the architecture, libraries used, the
reasoning behind each choice, and the moving parts you should know about if you pick the project
up next.

---

## Running the app

1. Create a free TMDB account and grab a **v3 API key** from
   [settings → API](https://www.themoviedb.org/settings/api).
2. Open `local.properties` in the project root and add:

   ```properties
   TMDB_API_KEY=your_tmdb_v3_key
   ```

   `local.properties` is ignored by git. CI can instead pass `-PTMDB_API_KEY=…` or set it in
   `gradle.properties`.
3. Build and run:

   ```bash
   ./gradlew :app:assembleDebug
   # or open in Android Studio and hit Run
   ```

`minSdk 26`, `targetSdk/compileSdk 36`. Gradle uses the JVM 11 toolchain for all Kotlin
modules (Gradle itself needs a JDK 17+ to drive AGP 8.13).

---

## Architecture

AMRO is designed with **feature teams** in mind — each feature owns a folder of modules, and
inside a feature we follow **Clean Architecture** (domain / data / presentation). The app
module is the only place that knows about multiple features; it wires navigation and Hilt.

```text
AMRO/
├── app/                                    # Composition root — Application, MainActivity, NavHost
├── core/
│   ├── domain/                             # (JVM) DomainResult, DomainError + helpers
│   ├── coroutine/                          # (AAR) DispatcherProvider + its Hilt binding
│   ├── network/                            # (AAR) Ktor HttpClient + TMDB auth plumbing
│   ├── testing/                            # (AAR) Test utils shared by every module
│   └── design-system/                      # (AAR) Material3 theme, shared Composables + previews
└── features/
    └── movies/
        ├── domain/                         # (AAR) Entities, repository interface, use cases (pure Kotlin code)
        ├── data/                           # (AAR) DTOs, mappers, remote source, repo impl
        ├── ui-listing/                     # (AAR) Trending list screen + VM + nav destination
        └── ui-detail/                      # (AAR) Movie detail screen + VM + nav destination
```

The `design-system` module owns the theme (`AmroTheme`), reusable Composables
(`LoadingState`, `ErrorState`), and a `@ThemePreviews` multi-preview annotation that renders
any component in both light and dark schemes — every public component in the module ships a
Studio preview using it.

### Module dependency rules

- **`movies:domain`** depends on nothing except `core:domain`. No Android, no HTTP, no JSON.
  This is the contract layer — pure Kotlin, trivially testable.
- **`movies:data`** depends on `movies:domain`, `core:domain`, `core:coroutine`, `core:network`.
  It implements `MoviesRepository` and is the only module that knows about Ktor, DTOs, and TMDB URLs.
- **`movies:ui-listing` / `movies:ui-detail`** each depend on `movies:domain` + `design-system`.
  They do **not** depend on `movies:data`, `core:network`, or on each other. You can extract
  either screen into its own app or swap out the network layer without touching them.
- **`app`** is the only module that depends on everything. It owns the NavHost and the
  Hilt composition root.

### One screen, one module

The assignment hints at future feature teams picking up extra screens (actors, user profiles,
streaming). Splitting presentation at the **screen** level (not just the feature level) gives
us:

- **Build parallelism.** Listing and detail compile independently.
- **Strong encapsulation.** A screen's Composables, ViewModel and route literal live together.
  Nothing cross-cuts two screens unintentionally.
- **Easy deletion.** Deprecated screen → delete a single module.

If a future feature has many screens that share a lot of state (e.g. a multi-step onboarding),
it is perfectly fine to keep those screens inside a single module; the rule is "one module per
screen **by default**", not "at all costs".

---

## Tech stack & why

| Concern | Choice | Reason |
|---|---|---|
| UI | **Jetpack Compose + Material 3** | Required by assignment; also the current idiomatic Android UI stack. |
| Navigation | **androidx.navigation:navigation-compose** | First-party, works well with Hilt's `hiltViewModel()`. Each feature exposes its own `NavGraphBuilder` extension so the app module just stitches them. |
| DI | **Hilt** | Required by assignment. KSP processor (not kapt) for faster compile times. |
| HTTP | **Ktor 3 client** (OkHttp engine) | Required by assignment. Multiplatform-ready if we ever need KMM. Content negotiation with kotlinx.serialization. |
| JSON | **kotlinx.serialization** | Stays Kotlin-first, plays natively with Ktor, no reflection at runtime. |
| Async | **Kotlin Coroutines + Flow** | Structured concurrency, cooperative cancellation. Parallelising the 5 pages of trending data is a 5-liner with `async`. |
| Images | **Coil 2** | Lightweight Compose-friendly image loading. |
| Testing | **JUnit4 + MockK + Turbine + Google Truth + Ktor MockEngine** | JUnit4 remains the default for Android modules; MockK handles Kotlin idioms (final classes, coroutines); Turbine makes StateFlow assertions readable; Ktor's MockEngine lets us assert at the HTTP level without Retrofit-style interception hacks. |

---

## Error handling

Errors never cross the data/domain boundary as exceptions. The data layer wraps every
suspend call with a small `runCatchingDomain { … }` helper that translates Ktor/IO exceptions
into a `DomainError` sum type (`Network`, `Server(code)`, `Cancelled`, `Unknown`). ViewModels
map those to user-facing strings.

Benefits:

- Exhaustive `when` branches in the UI — no forgotten edge cases.
- Domain stays free of `IOException`, `ResponseException`, etc.
- Cancellation is explicitly re-thrown; it is never swallowed into a "generic error".

---

## The "top-100 trending" and "filter within what we have" rules

- TMDB paginates `/trending/movie/week` at 20 results per page. `MoviesRepositoryImpl` fetches
  **5 pages concurrently** with `async`, deduplicates by id, and takes the first 100. Genre
  ids are resolved via the cached `/genre/movie/list` endpoint so the UI never needs a
  per-item call.
- Filtering and sorting happen **entirely client-side** in `FilterAndSortMoviesUseCase`, on the
  list of movies already in memory. This matches the spec: filtering by "Comedy" surfaces the
  N comedies that exist in the current top 100, it does **not** paginate looking for more.
- Sort supports Popularity (default, descending), Title (case-insensitive), and Release date.
  Movies with an unknown release date always sort to the end regardless of direction, to avoid
  a block of "—" at the top of the list.

---

## Testing strategy

Tests are scoped to the module that owns the code under test.

| Layer | What we test | How |
|---|---|---|
| Domain | `FilterAndSortMoviesUseCase` – filter, sort, edge cases (blank dates, multi-genre match) | Plain JUnit, no mocks — it's a pure function. |
| Data | `MoviesRepositoryImpl` – error mapping, genre resolution | Ktor `MockEngine` for HTTP, `TestDispatcherProvider` so `withContext(io)` becomes deterministic. |
| Data | `MovieMappers` – DTO → domain model | JUnit, no Android framework. |
| Presentation | `MoviesListingViewModel`, `MovieDetailViewModel` | MockK for the repository, Turbine for StateFlow assertions, `Dispatchers.setMain` for the Main dispatcher. |

Run everything:

```bash
./gradlew test
```

### What's deliberately **not** here yet

- **Compose UI tests** (Robolectric / `createComposeRule`) — the scaffolding is in place (Compose
  test dependencies are wired in `app`) but no screen tests are committed. First candidate:
  `MoviesListingScreen` asserting the filter-chip / empty-state flow.
- **End-to-end instrumentation tests** hitting a real or faked HTTP server.
- **Screenshot tests** (Paparazzi / Roborazzi).

These are natural next steps — the architecture is already set up to make them cheap.

---

## Extending the app

### Adding a new screen inside Movies (e.g. actor list)

1. Create `features/movies/ui-actors/` as a new Compose library module.
2. `implementation(project(":features:movies:domain"))` — if you need the repo, add a
   new use case to `movies:domain` rather than reaching into `movies:data`.
3. Expose `ACTORS_ROUTE` + `actorsDestination(onBack = …)` from a `navigation` package.
4. In `app/.../AmroNavHost.kt`, call `actorsDestination(…)` inside the `NavHost`. That is the
   only place the app module touches the new screen.

### Adding an entirely new feature (e.g. user profile)

1. Create `features/profile/` with the same four-module shape as `movies`.
2. Add the routes to `AmroNavHost`.
3. Add any new Hilt bindings inside the feature's own `…-data/di` module — Hilt will discover
   them automatically at the application root.

### Swapping / adding data sources

- `MoviesRepository` is the seam. A future offline-first implementation can introduce a local
  source (Room) alongside the remote source, still returning the same `DomainResult<List<Movie>>`.
  None of the presentation code changes.
- Merging data from multiple APIs is similarly a data-module concern. The domain models stay
  unchanged.

---

## Code conventions

- Kotlin official code style (`kotlin.code.style=official` in `gradle.properties`).
- `internal` visibility for anything that should not leak outside its module (DTOs, remote
  data source, repository implementation).
- No reliance on Kotlin `Result` / runtime exceptions across architectural layers — use
  `DomainResult` + `DomainError` instead.
- Tests use Google Truth for assertions (better failure messages than JUnit's `assertEquals`).

---

## Known limitations / things I'd do with more time

- **No offline cache.** Spec mentions a future wish for offline support; a natural follow-up is
  to introduce Room in `movies:data` and wrap the remote + local sources behind the same
  repository.
- **No paging.** The top-100 endpoint is only 5 pages, so paging is overkill right now. If the
  requirement changes to "paged trending feed", Paging 3 integrates cleanly with Compose and
  can live alongside the current approach.
- **No API-level retry/backoff.** Ktor's `HttpRequestRetry` plugin can be dropped into
  `NetworkModule` in ~10 lines when we need it.
- **No analytics.** Would add a `core-analytics` module with a single interface so features
  depend on intent, not on a concrete SDK.
