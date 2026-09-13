# Todo Feature — Requirement Analysis, Architecture & Pseudocode

> Kotlin / Modern Android Development. Written as an interview answer: every decision is
> stated with the reason behind it, because the reason is the part being assessed.

---

## Table of contents

1. [How I read the question](#1-how-i-read-the-question)
2. [Requirement breakdown — stated vs. implied](#2-requirement-breakdown--stated-vs-implied)
3. [Architecture: why MVI, and what sits under it](#3-architecture-why-mvi-and-what-sits-under-it)
4. [Requirement 1 — Data models + offline database](#4-requirement-1--data-models--offline-database)
5. [The SQL, in detail](#5-the-sql-in-detail)
6. [Requirement 2 — Create a todo and POST it](#6-requirement-2--create-a-todo-and-post-it)
7. [Requirement 3 — Fetch by conditions](#7-requirement-3--fetch-by-conditions)
8. [Requirement 4 — Screen rotation](#8-requirement-4--screen-rotation)
9. [Cross-cutting: threading, errors, testing](#9-cross-cutting-threading-errors-testing)
10. [Trade-offs, cuts, and questions for the interviewer](#10-trade-offs-cuts-and-questions-for-the-interviewer)

---

## 1. How I read the question

The four features are ordered by importance, and that ordering is itself the hint. Read
in order they describe **one** coherent problem, not four separate ones:

| Stated feature | What it is really testing |
|---|---|
| 1. Save models, view list in flight mode | Do you know that offline support is an *architecture*, not a cache you bolt on later? |
| 2. Create locally + POST to server | What happens to a write when there is no network? Do you block the user, or do you own the write locally and reconcile later? |
| 3. Fetch by conditions | Do the local filter and the remote filter stay consistent? A `q=` string is easy; making the cached list agree with it is the actual problem. |
| 4. Screen rotation | Do you know the difference between a config change and process death, and do you know `ViewModel` alone does not cover both? |

The sentence *"you may not finish all the features in time"* is asking for **priority
discipline** — a coherent partial system beats four half-wired ones. Section 10 states
exactly what I would cut and in what order.

Feature 1 and feature 2 together imply a constraint neither one states on its own:
**a todo can exist before the server knows about it.** That single fact drives most of
the design below (two ids, an outbox column, push-before-pull).

---

## 2. Requirement breakdown — stated vs. implied

### R1 — "Define the data models and save them in the DB so the user can view the todo list in flight mode"

- **Stated:** persist todos; render the list with no network.
- **Implied:** the list must render from disk on a *cold start* in flight mode, not just
  from an in-memory cache that a process death would wipe.
- **Implied:** the UI cannot read from the network directly, or "flight mode" becomes a
  branch in every screen. The database has to be the single source of truth.
- **Decision:** Room, exposed as `Flow`. UI subscribes to the DB and to nothing else.

### R2 — "Create a new todo and post it to server"

- **Stated:** `POST api/my/todos`.
- **Implied:** creating while offline must not fail or be lost. Given R1 ships first, the
  user is explicitly expected to be using the app offline.
- **Implied:** the upload has to survive process death — the user creates a todo on a
  plane, closes the app, lands, and the todo must eventually reach the server.
- **Decision:** write locally and return immediately; a durable background job uploads.

### R3 — "Only fetch some todos by conditions"

- **Stated:** `GET api/my/todos?q=isDone:true+startDate:1+endDate:2`.
- **Implied:** the filter must also apply *locally*, otherwise the filter silently stops
  working the moment the network drops — which contradicts R1.
- **Implied:** the conditions are composable — any subset of the three.
- **Decision:** one `TodoQuery` value object that renders to both the server `q` string
  and a Room `WHERE` clause. One predicate, two renderers.

### R4 — "Support the screen rotation"

- **Stated:** survive rotation.
- **Implied:** survive *process death* too — "Don't Keep Activities", or a background
  kill. A `ViewModel` handles the first and not the second.
- **Implied:** the active filter, the scroll position, and a half-typed create form are
  all state that a user would be annoyed to lose.
- **Decision:** `ViewModel` + `SavedStateHandle` + `rememberSaveable`, three layers.

---

## 3. Architecture: why MVI, and what sits under it

### MVVM or MVI?

Both are fine on Android. Here is the honest comparison for *this* feature:

| | MVVM (multiple `StateFlow`s) | **MVI (one immutable state + intents)** |
|---|---|---|
| State shape | `isLoading`, `todos`, `error`, `filter` as separate flows | one `TodoUiState` data class |
| Illegal states | `isLoading = true` **and** `error != null` are both representable | reducer is the only writer, so they are not |
| Rotation | must restore several pieces | one object to restore |
| Compose fit | several `collectAsState` calls, several recomposition scopes | one `collectAsStateWithLifecycle`, one snapshot |
| Debugging | "who set this flag?" | every change is an `Intent`, trivially loggable |
| Cost | less boilerplate up front | a sealed `Intent` hierarchy + a reducer |

**I choose MVI**, for two reasons specific to this feature:

1. This screen has a genuinely composite state — *filter* × *loading* × *offline* ×
   *list*. Those are not independent: refreshing changes the loading flag, the filter
   changes the list, going offline changes the banner but must **not** clear the list.
   Encoding them as one immutable object makes "offline **and** showing cached results"
   a normal, representable state instead of an accident.
2. Rotation (R4) is one of the four requirements. With MVI there is exactly one object
   to survive a config change and one string to survive process death.

I am **not** pulling in an MVI framework (Orbit, MVIKotlin, Mobius). Plain Kotlin —
a sealed `Intent` interface, one `StateFlow`, one reducer — gets all of the benefit with
none of the dependency. A third-party MVI library earns its place at maybe ten screens,
not one.

### Unidirectional data flow

```
        ┌──────────────────────────────────────────────┐
        │                  Compose UI                  │
        │  renders TodoUiState, emits TodoIntent       │
        └───────┬──────────────────────────────▲───────┘
      Intent    │                              │  State (StateFlow)
                ▼                              │
        ┌──────────────────────────────────────┴───────┐
        │              TodoViewModel                    │
        │  reduce(intent) -> new state, launch effects  │
        └───────┬──────────────────────────────▲───────┘
                │                              │  Flow<List<Todo>>
                ▼                              │
        ┌──────────────────────────────────────┴───────┐
        │              TodoRepository                   │
        │        (decides local vs. remote)             │
        └───────┬──────────────────────────────▲───────┘
                │                              │
        ┌───────▼────────┐            ┌────────┴────────┐
        │  Room  (TRUTH) │◄───writes──│  Retrofit API   │
        └───────▲────────┘            └────────▲────────┘
                │                              │
                └────────  WorkManager  ───────┘
                          (drains the outbox)
```

**The one rule that makes offline work:** data flows into the UI from Room and *only*
from Room. A network response is never returned to the ViewModel — it is written to the
database, and the database's `Flow` re-emits. This means the offline path and the online
path are the *same* code path, so offline cannot regress separately.

### Layers and libraries

| Layer | Contents | Library | Why this one |
|---|---|---|---|
| `ui` | Composables, `TodoViewModel`, `TodoUiState`, `TodoIntent` | Compose, Lifecycle | Declarative UI makes state-driven rendering the default |
| `domain` | `Todo`, `TodoQuery`, `SyncState`, repository *interface* | pure Kotlin, no Android | Testable on the JVM in milliseconds; keeps Room/Retrofit annotations out of the model the UI uses |
| `data/local` | `TodoEntity`, `TodoDao`, `TodoDatabase` | Room | Compile-time verified SQL, first-class `Flow`, migrations |
| `data/remote` | `TodoDto`, `TodoApi` | Retrofit + Moshi | `suspend` support, codegen adapters (no reflection at runtime) |
| `data/sync` | `TodoSyncWorker`, `SyncScheduler` | WorkManager | The only Android API that guarantees the upload survives process death |
| `di` | modules | Hilt | Standard, compile-time verified, `@HiltViewModel` / `@HiltWorker` integration |

**Why three model classes** (`TodoDto`, `TodoEntity`, `Todo`) instead of one: they change
for different reasons. The server adding a field should not force a database migration;
a new index should not change the JSON. One shared class couples all three, and in
practice leaks `@SerializedName` into the Composable.

---

## 4. Requirement 1 — Data models + offline database

### The domain model

```kotlin
data class Todo(
    val localId: Long,          // OUR id - exists the moment the user taps Save
    val remoteId: String?,      // server id - NULL until the POST has succeeded
    val title: String,
    val content: String,
    val startDate: Long,        // epoch millis, UTC
    val endDate: Long,          // epoch millis, UTC
    val isDone: Boolean,
    val syncState: SyncState,
)

enum class SyncState {
    PENDING_CREATE,   // created locally, server does not know about it
    PENDING_UPDATE,   // exists upstream, local copy has unsent edits
    SYNCED,           // local and server agree
}
```

**Decision: two identifiers.**
A todo created in flight mode has no server id. If `remoteId` were the primary key, an
offline create would have nothing to key on — the usual workaround is a fake negative id
or a UUID that later has to be rewritten everywhere it was referenced. Instead the app
owns `localId` from the first millisecond, and `remoteId` is a nullable attribute filled
in when the server confirms. Every screen, every foreign key, every navigation argument
uses `localId` and therefore never has to change.

**Decision: `syncState` on the row, not a separate operations table.**
The todos table *is* the outbox. A second `pending_operations` table would need to be
kept transactionally consistent with the todos table, and every read would have to join
the two to know whether a row is trustworthy. One enum column gives the same information
with no join and no consistency risk. The trade-off: this supports one pending operation
per row, not an ordered log of them. For create/toggle that is enough; if the product
later needs full offline edit history, the outbox becomes a real table and `syncState`
becomes a derived value.

**Decision: `Long` epoch millis for dates, not `LocalDate`.**
The server sends `123456` — a number. Storing anything else means converting on every
read and write, and SQLite has no date type anyway. Store UTC epoch millis; convert to
the user's zone only at the formatting layer, in the Composable. This also makes the
range comparison in SQL a plain integer comparison, which is index-friendly.

### The entity

```kotlin
@Entity(
    tableName = "todos",
    indices = [
        Index(value = ["remote_id"], unique = true),
        Index(value = ["is_done"]),
        Index(value = ["start_date", "end_date"]),
    ],
)
data class TodoEntity(
    @PrimaryKey(autoGenerate = true) @ColumnInfo("local_id") val localId: Long = 0L,
    @ColumnInfo("remote_id")  val remoteId: String?,
    @ColumnInfo("title")      val title: String,
    @ColumnInfo("content")    val content: String,
    @ColumnInfo("start_date") val startDate: Long,
    @ColumnInfo("end_date")   val endDate: Long,
    @ColumnInfo("is_done")    val isDone: Boolean,
    @ColumnInfo("sync_state") val syncState: SyncState,
    @ColumnInfo("updated_at") val updatedAt: Long,   // last-writer-wins tiebreak
)
```

`updatedAt` exists so that a future conflict-resolution policy has something to compare.
Right now the policy is "an unsynced local row always wins", which needs no timestamp —
but adding the column later is a migration, and adding it now is free.

### The merge rule — where offline apps lose data

```kotlin
@Transaction
suspend fun upsertFromServer(rows: List<TodoEntity>) {
    for (incoming in rows) {
        val existing = incoming.remoteId?.let { findByRemoteId(it) }
        when {
            existing == null              -> insert(incoming)
            existing.syncState != SYNCED  -> Unit   // local edit wins until it is pushed
            else -> update(incoming.copy(localId = existing.localId))
        }
    }
}
```

Three things are happening, and each one is a bug if left out:

1. **Match on `remote_id`, not on `local_id`.** A row created offline and later confirmed
   must be *updated*, not inserted a second time. Without this the user sees a duplicate
   the first time they reconnect.
2. **Never overwrite a `PENDING_*` row.** The server's copy is by definition older than
   an unsent local edit. Overwriting is exactly how an offline change silently vanishes.
3. **`@Transaction`.** The loop does a read then a write per row. Without the transaction,
   a concurrent write can slip between them, and every row is its own disk commit —
   dramatically slower for a page of results.

### What the UI reads

```kotlin
fun observeTodos(q: TodoQuery): Flow<List<Todo>> =
    dao.observeFiltered(q.toSupportQuery())
        .map { rows -> rows.map(TodoEntity::toDomain) }
        .flowOn(Dispatchers.IO)
```

That is the whole of "flight mode support". There is no `if (isOnline)` anywhere in the
UI, because the UI never asks about the network.

---

## 5. The SQL, in detail

Room generates the schema, but it is worth being explicit about what it produces and why
each piece is shaped that way.

### Generated DDL

```sql
CREATE TABLE IF NOT EXISTS `todos` (
    `local_id`   INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL,
    `remote_id`  TEXT,
    `title`      TEXT    NOT NULL,
    `content`    TEXT    NOT NULL,
    `start_date` INTEGER NOT NULL,
    `end_date`   INTEGER NOT NULL,
    `is_done`    INTEGER NOT NULL,   -- SQLite has no BOOLEAN; 0 / 1
    `sync_state` TEXT    NOT NULL,   -- enum stored by name, see below
    `updated_at` INTEGER NOT NULL
);

CREATE UNIQUE INDEX `index_todos_remote_id`             ON `todos` (`remote_id`);
CREATE        INDEX `index_todos_is_done`               ON `todos` (`is_done`);
CREATE        INDEX `index_todos_start_date_end_date`   ON `todos` (`start_date`, `end_date`);
```

**Type mapping notes:**

- `is_done INTEGER` — SQLite has no boolean type. Room maps `Boolean` to `0`/`1`
  automatically, which is why the generated `WHERE` clause below binds an `Int`, not a
  `Boolean`.
- `sync_state TEXT` — I store the enum **by name**, via a `@TypeConverter`, not by
  `ordinal()`. Ordinals are compact but they break the day someone inserts a new constant
  in the middle of the enum: every existing row silently changes meaning, with no
  compiler error and no migration to catch it. Names cost a few bytes and are readable in
  a database inspector, which matters when debugging a sync bug on a real device.
  The converter falls back to `SYNCED` on an unknown name rather than throwing, so a
  downgrade cannot crash the app on read.
- `remote_id` is **nullable and UNIQUE**. In SQLite, a `UNIQUE` index permits multiple
  `NULL`s, which is precisely what is needed: any number of not-yet-uploaded todos, but
  never two rows for the same server object.

### Why these three indexes

Each index maps to one of the filter conditions in R3.

- **`remote_id` (unique)** — used by `upsertFromServer` once per incoming row. Without
  it, a refresh of *N* rows against a table of *M* rows is O(N·M) full scans. It also
  enforces the "no duplicates after reconnect" invariant at the storage level rather than
  trusting application code.
- **`is_done`** — serves the "unfinished only" filter, the most common query in the app.
- **`(start_date, end_date)` composite** — serves the period filter.

**The composite-index caveat, stated explicitly because it is a real limitation:**
SQLite uses a composite index by *leftmost prefix*. So:

| Filter | Uses the composite index? |
|---|---|
| `start_date >= ?` | Yes — leftmost column |
| `start_date >= ? AND end_date <= ?` | Yes — index seeks on `start_date`, then filters `end_date` from the index rather than the table |
| `end_date <= ?` **alone** | **No** — `end_date` is not a prefix; this falls back to a scan |

If the product turns out to use an end-date-only filter often, the fix is a separate
`Index("end_date")`. I have not added it pre-emptively: every index costs write time and
disk, and I would rather add it against a measured query than a guessed one.

Verify with:

```sql
EXPLAIN QUERY PLAN
SELECT * FROM todos WHERE is_done = 0 AND start_date >= 100 ORDER BY start_date ASC;
-- expected: SEARCH todos USING INDEX index_todos_start_date_end_date (start_date>?)
-- bad:      SCAN todos
```

Note the `ORDER BY start_date` is deliberately aligned with the leading index column, so
SQLite can return rows already ordered instead of materialising a temporary B-tree to
sort them.

### A possible refinement: a partial index for the outbox

The outbox query runs on every sync:

```sql
SELECT * FROM todos WHERE sync_state != 'SYNCED' ORDER BY updated_at ASC;
```

In the steady state almost every row is `SYNCED`, so a normal index on `sync_state` would
be nearly useless (very low selectivity for the common value). A **partial index** stores
only the rows that matter:

```sql
CREATE INDEX index_todos_outbox ON todos (updated_at) WHERE sync_state != 'SYNCED';
```

This is typically a handful of entries regardless of table size. Room cannot express a
partial index with the `@Index` annotation, so it goes in a migration as raw SQL. I would
only add it once the table is genuinely large — mentioning it here mostly to show the
outbox query was considered rather than assumed cheap.

### Injection safety

Every value in the dynamic query is a **bound parameter** (`?`), never string
concatenation. Today the inputs are a boolean and two longs, so injection is not
reachable — but the first free-text search filter someone adds would make it reachable,
and by then the unsafe pattern is already established. Binding from the start costs
nothing.

---

## 6. Requirement 2 — Create a todo and POST it

### The principle

**The local write is the commit. The upload is a separate, retryable job.**
The user never waits on the radio, and the outcome does not depend on connectivity.

```kotlin
suspend fun createTodo(title: String, content: String, start: Long, end: Long): Long {
    val localId = dao.insert(
        TodoEntity(
            remoteId = null,
            title = title, content = content,
            startDate = start, endDate = end,
            isDone = false,
            syncState = SyncState.PENDING_CREATE,
            updatedAt = now(),
        )
    )
    // The row is already on screen at this point: Room's Flow has emitted.
    syncScheduler.requestSync()   // fire and forget
    return localId                // caller can navigate to the row, network or not
}
```

Returning `localId` matters: the caller can navigate straight to the detail screen for a
todo the server has never heard of.

### The API

```kotlin
// POST api/my/todos
// {"title": "...", "content": "...", "startDate": 1, "endDate": 2, "isDone": false}
@POST("api/my/todos")
suspend fun create(@Body body: TodoDto): TodoDto
```

### The outbox drain

```kotlin
suspend fun pushPending(): Result<Unit> = runCatching {
    for (row in dao.pendingSync()) {          // sync_state != 'SYNCED', oldest first
        when (row.syncState) {
            PENDING_CREATE -> {
                val created = api.create(row.toDto())
                val remoteId = created.id ?: error("Server accepted the todo but returned no id")
                dao.markSynced(row.localId, remoteId, SyncState.SYNCED)
            }
            PENDING_UPDATE -> { /* PUT - not in the supplied contract yet */ }
            SYNCED -> Unit
        }
    }
}
```

**Oldest first** because creation order is user-visible ordering, and a partial drain
should leave a prefix of the queue uploaded, not a random subset.

### The worker

```kotlin
@HiltWorker
class TodoSyncWorker @AssistedInject constructor(
    @Assisted ctx: Context,
    @Assisted params: WorkerParameters,
    private val repo: TodoRepository,
) : CoroutineWorker(ctx, params) {

    override suspend fun doWork(): Result {
        if (repo.pushPending().isFailure) return Result.retry()
        return if (repo.refresh(TodoQuery.ALL).isSuccess) Result.success() else Result.retry()
    }
}

// scheduling
OneTimeWorkRequestBuilder<TodoSyncWorker>()
    .setConstraints(Constraints.Builder().setRequiredNetworkType(CONNECTED).build())
    .setBackoffCriteria(BackoffPolicy.EXPONENTIAL, 30, SECONDS)
    .build()
    .let { WorkManager.getInstance(ctx).enqueueUniqueWork("todo-sync", KEEP, it) }
```

Four deliberate choices:

1. **WorkManager, not `applicationScope.launch`.** A coroutine dies with the process. The
   scenario in the requirements is a user on a plane — app closed, device possibly
   restarted, network back hours later. WorkManager persists the request to its own
   database and the OS re-runs it. This is the only mechanism that actually satisfies R2
   in the situation R1 describes.
2. **`NetworkType.CONNECTED` constraint.** The OS wakes the job when connectivity
   returns. No polling, no `ConnectivityManager` callback to leak, no battery cost while
   offline.
3. **`ExistingWorkPolicy.KEEP` on unique work.** Ten todos created offline enqueue ten
   sync requests; `KEEP` collapses them into one drain of the whole outbox. `REPLACE`
   would be wrong — it would cancel a drain already in progress.
4. **Push before pull.** If the pull ran first, a server page fetched before the local
   create could be merged over the top of it. Push first, then the refresh sees a server
   state that already includes the new row.

Exponential backoff from 30 s stops a server outage from becoming a retry storm across
every installed device.

### Failure modes considered

| Failure | Behaviour |
|---|---|
| No network | Row stays `PENDING_CREATE`; work is retried when the constraint is met. User sees the todo the whole time. |
| Server 5xx | `Result.retry()` with exponential backoff. |
| Server 4xx (invalid payload) | Retrying will never help. Should be marked as a permanent failure on the row and surfaced — **not implemented here**, called out in §10. |
| POST succeeds, response lost | The row stays `PENDING_CREATE` and is re-sent, creating a duplicate server-side. The fix is a client-generated idempotency key sent with the POST — needs a server contract change, see §10. |

That last row is the honest limitation of this design, and I would rather state it than
let it be found.

---

## 7. Requirement 3 — Fetch by conditions

### The core idea

One value object describes the filter, and it renders **two** ways: to the server's `q`
string, and to a local SQL `WHERE` clause. Two independently hand-written predicates
would drift, and then the cached list and the network list would quietly disagree —
which is the exact class of bug this requirement is probing for.

```kotlin
data class TodoQuery(
    val isDone: Boolean? = null,    // null = don't care
    val startDate: Long? = null,    // inclusive lower bound
    val endDate: Long? = null,      // inclusive upper bound
) {

    // ---- Renderer A: the server contract ----
    // "key:value" pairs joined with '+', e.g. isDone:true+startDate:1+endDate:2
    fun toServerQuery(): String? = buildList {
        isDone?.let    { add("isDone:$it") }
        startDate?.let { add("startDate:$it") }
        endDate?.let   { add("endDate:$it") }
    }.ifEmpty { null }?.joinToString("+")

    // ---- Renderer B: the same predicate, against Room ----
    fun toSupportQuery(): SupportSQLiteQuery {
        val clauses = mutableListOf<String>()
        val args = mutableListOf<Any>()
        isDone?.let    { clauses += "is_done = ?";     args += if (it) 1 else 0 }
        startDate?.let { clauses += "start_date >= ?"; args += it }
        endDate?.let   { clauses += "end_date <= ?";   args += it }

        val where = if (clauses.isEmpty()) "" else "WHERE " + clauses.joinToString(" AND ")
        return SimpleSQLiteQuery(
            "SELECT * FROM todos $where ORDER BY start_date ASC, local_id ASC",
            args.toTypedArray(),
        )
    }

    companion object {
        val ALL = TodoQuery()
        val UNFINISHED = TodoQuery(isDone = false)
        fun inPeriod(from: Long, to: Long) = TodoQuery(startDate = from, endDate = to)

        /** Inverse of toServerQuery. Used for deep links and for round-trip tests. */
        fun parse(raw: String?): TodoQuery { /* split '+', then split ':' */ }
    }
}
```

`ORDER BY start_date ASC, local_id ASC` — the second key is a **stable tiebreak**. Without
it, two todos with the same start date can swap positions between emissions, and a
`LazyColumn` will visibly reshuffle.

### The Retrofit call, and one easy bug

```kotlin
// GET api/my/todos?q=isDone:true+startDate:123456+endDate:123456
@GET("api/my/todos")
suspend fun getTodos(@Query("q", encoded = true) q: String?): List<TodoDto>
```

**`encoded = true` is load-bearing.** The contract uses a *literal* `+` as the separator.
Retrofit's default percent-encoding turns `+` into `%2B`, and a server splitting on `+`
will then see one long unparseable term and return everything (or nothing). This is the
single most likely bug in an implementation of this exact API, and it fails silently —
the request succeeds, the filter is just ignored.

A nullable `q` means the parameter is omitted entirely for `TodoQuery.ALL`, rather than
sent as `?q=`, which a strict parser might reject.

### Making dynamic SQL reactive

```kotlin
@RawQuery(observedEntities = [TodoEntity::class])
fun observeFiltered(query: SupportSQLiteQuery): Flow<List<TodoEntity>>
```

Two points worth stating:

- **Why `@RawQuery` at all.** A `@Query` cannot express "any subset of three conditions".
  The usual workaround is `WHERE (:isDone IS NULL OR is_done = :isDone) AND ...`, which
  works but defeats index usage — SQLite cannot plan an index seek through the `OR NULL`.
  Building the clause at runtime keeps every generated query index-friendly.
- **`observedEntities` is mandatory here.** Room's invalidation tracker normally infers
  which tables a query touches by parsing the SQL at compile time. With `@RawQuery` there
  is no SQL to parse, so without `observedEntities` the returned `Flow` **emits once and
  then goes silent** — the list would stop updating after inserts. It is a quiet failure
  and a classic review catch.

### Composing the three requested filters

```kotlin
TodoQuery.UNFINISHED                      // -> ?q=isDone:false
TodoQuery.inPeriod(jan1, jan31)           // -> ?q=startDate:...+endDate:...
TodoQuery(isDone = false,
          startDate = jan1, endDate = jan31)  // -> all three, ANDed
```

### An ambiguity I would raise, not silently resolve

"Fetching the todos during the given period" has two reasonable meanings:

- **Containment** — the todo starts after the window opens *and* ends before it closes:
  `start_date >= from AND end_date <= to`.
- **Overlap** — the todo intersects the window at all, including one that started
  earlier and is still running: `start_date <= to AND end_date >= from`.

Overlap is usually what a calendar user actually wants. But the server contract names the
parameters `startDate` and `endDate`, which most naturally reads as containment, and
**the local predicate must match the server's or the two lists disagree** — the exact
failure this whole design is built to avoid. So I implement containment to match the
contract, and flag it as the first question for the API owner (§10). If the server means
overlap, only the two comparison operators change; nothing else in the design moves.

---

## 8. Requirement 4 — Screen rotation

Rotation is three distinct problems. Solving only the first is the common mistake.

### The MVI state and intents

```kotlin
data class TodoUiState(
    val todos: List<Todo> = emptyList(),
    val query: TodoQuery = TodoQuery.ALL,
    val isRefreshing: Boolean = false,
    val isOffline: Boolean = false,   // last refresh failed; the list below is still valid
)

sealed interface TodoIntent {
    data class Create(val title: String, val content: String, val start: Long, val end: Long) : TodoIntent
    data class ToggleDone(val localId: Long, val isDone: Boolean) : TodoIntent
    data class FilterChanged(val query: TodoQuery) : TodoIntent
    data object Refresh : TodoIntent
}

/** One-shot, NOT part of the state - see below. */
sealed interface TodoEffect {
    data object SavedOffline : TodoEffect
    data class ShowError(val message: String) : TodoEffect
}
```

Note that `isOffline` and `todos` coexist. "Offline **and** showing cached results" is a
first-class state, not an error case — which is the whole point of R1.

### The ViewModel

```kotlin
@HiltViewModel
class TodoViewModel @Inject constructor(
    private val repo: TodoRepository,
    private val savedState: SavedStateHandle,
) : ViewModel() {

    // LAYER 2 - the filter is persisted as the SAME "q" string the server takes.
    private val query: Flow<TodoQuery> =
        savedState.getStateFlow(KEY_QUERY, TodoQuery.ALL.toServerQuery())
            .map(TodoQuery::parse)
            .distinctUntilChanged()

    private val refreshing = MutableStateFlow(false)
    private val offline    = MutableStateFlow(false)

    private val effects = Channel<TodoEffect>(Channel.BUFFERED)
    val effect = effects.receiveAsFlow()

    val state: StateFlow<TodoUiState> = combine(
        // flatMapLatest: a filter change cancels the previous DB subscription, so a
        // result for the OLD filter can never arrive after one for the new filter.
        query.flatMapLatest { q -> repo.observeTodos(q).map { q to it } },
        refreshing,
        offline,
    ) { (q, todos), isRefreshing, isOffline ->
        TodoUiState(todos, q, isRefreshing, isOffline)
    }.stateIn(
        scope = viewModelScope,
        // LAYER 3 - see below.
        started = SharingStarted.WhileSubscribed(5_000),
        initialValue = TodoUiState(),
    )

    fun onIntent(intent: TodoIntent) = when (intent) {
        is TodoIntent.FilterChanged -> savedState[KEY_QUERY] = intent.query.toServerQuery()
        is TodoIntent.ToggleDone    -> launch { repo.setDone(intent.localId, intent.isDone) }
        is TodoIntent.Create        -> launch {
            repo.createTodo(intent.title, intent.content, intent.start, intent.end)
            if (offline.value) effects.send(TodoEffect.SavedOffline)
        }
        TodoIntent.Refresh -> refresh()
    }

    private companion object { const val KEY_QUERY = "todo_query" }
}
```

### Layer 1 — `ViewModel` survives the configuration change

The `ViewModel` is retained across the Activity's destroy/recreate, so the list and any
in-flight refresh are simply **not** recreated. There is nothing to save and nothing to
restore, and a rotation mid-refresh does not restart the network call. This covers
rotation, and only rotation.

### Layer 2 — `SavedStateHandle` survives process death

A `ViewModel` does **not** survive the process being killed in the background, or the
"Don't Keep Activities" developer option. Those restore the Activity with a saved
`Bundle` and a brand-new `ViewModel`.

So the filter lives in `SavedStateHandle`, serialised as the **same `q` string the server
takes**. That reuse is the neat part: no `Parcelable`, no custom saver, no extra
serialization code — the wire format is already a compact, human-readable string, and
`TodoQuery.parse` is already needed for deep links. Cost: one `String` in the bundle.
(This matters: `onSaveInstanceState` has a hard TransactionTooLargeException limit, so
never put the todo *list* in there — it is re-read from Room in milliseconds anyway.)

### Layer 3 — `WhileSubscribed(5_000)` bridges the gap

During a rotation the old Activity unsubscribes and the new one subscribes a few
milliseconds later. With a plain `SharingStarted.WhileSubscribed()` (no timeout), that
gap tears down the upstream Room query and rebuilds it — pointless work on every single
rotation, and a visible flash of the initial empty state. The 5-second grace period keeps
the upstream alive across the gap while still cancelling it when the user genuinely
leaves the screen.

`Lazily` would be wrong in the other direction: the DB subscription would then stay alive
for the whole ViewModel lifetime, doing work while the screen is in the background.

### In the Composable

```kotlin
@Composable
fun TodoScreen(viewModel: TodoViewModel = hiltViewModel()) {
    // collectAsStateWithLifecycle, not collectAsState: stops collection in STOPPED,
    // so a backgrounded screen is not recomposing on every DB change.
    val state by viewModel.state.collectAsStateWithLifecycle()

    val listState = rememberLazyListState()                     // scroll position: saveable
    var draftTitle by rememberSaveable { mutableStateOf("") }    // half-typed form survives

    LaunchedEffect(Unit) {
        viewModel.effect.collect { /* show snackbar */ }
    }

    LazyColumn(state = listState) {
        items(state.todos, key = { it.localId }) { todo ->       // stable key = correct
            TodoRow(todo, onToggle = { viewModel.onIntent(ToggleDone(todo.localId, it)) })
        }
    }
}
```

`key = { it.localId }` — another reason the local id had to exist from creation. Keying on
`remoteId` would mean a null key for every offline-created row, and Compose would lose
item identity (breaking animations and recycling) the moment the row got synced.

### Why one-shot events are a `Channel`, not state

If "saved offline" were a field in `TodoUiState`, it would still be set after a rotation
and the snackbar would show **again** on every rotation until something cleared it. Events
are consumed exactly once; a `Channel` models that and state does not.

---

## 9. Cross-cutting: threading, errors, testing

### Threading

- Room and Retrofit `suspend` functions already move off the main thread; `flowOn(io)` in
  the repository makes the *mapping* work off-main too.
- The dispatcher is **injected** behind an `@IoDispatcher` qualifier rather than
  hard-coding `Dispatchers.IO`, so tests can substitute a `TestDispatcher` and run
  deterministically instead of sleeping.
- `viewModelScope` cancels everything on clear; nothing outlives the screen except the
  WorkManager job, which is meant to.

### Error handling

`refresh()` distinguishes two categories, because they deserve different UI:

```kotlin
repo.refresh(state.value.query)
    .onSuccess { offline.value = false }
    .onFailure { e ->
        offline.value = e is OfflineException          // banner; list stays on screen
        if (e !is OfflineException) {
            effects.send(TodoEffect.ShowError(e.message.orEmpty()))   // transient snackbar
        }
    }
```

Being offline is an **expected state** in this product, not an error — it gets a
persistent, non-blocking banner and the cached list stays. A 500 is an actual error and
gets a snackbar. Neither ever clears the list, because a failed refresh does not make the
cached data wrong.

### Testing

The layering makes most of this cheap JVM testing, no emulator:

| What | How |
|---|---|
| `toServerQuery()` / `parse()` | Pure functions. Round-trip property test: `parse(q.toServerQuery()) == q`. |
| `toSupportQuery()` | Assert the generated SQL string and the bound args array. No device. |
| Merge rule | In-memory Room + fake API. Assert a `PENDING_CREATE` row is not overwritten by a server page. |
| Offline create | Fake API that throws `IOException`. Assert the DB `Flow` still emits the new row and `syncState` stays `PENDING_CREATE`. |
| Duplicate-on-reconnect | Insert offline, sync, then refresh. Assert the row count is 1. |
| ViewModel | Inject `TestDispatcher`; feed intents; assert the emitted `TodoUiState` sequence with Turbine. |
| Rotation | `SavedStateHandle` is just a map in tests — write a query, build a new ViewModel from the same handle, assert the filter survived. |

The reason this list is short and boring is the architecture: because nothing depends on
the Android framework except the three thin edge classes, almost everything is a plain
unit test.

---

## 10. Trade-offs, cuts, and questions for the interviewer

### Priority order, and why each stage is shippable on its own

1. **Entity + DAO + Repository + Compose list.** The offline read works. Demoable.
2. **Create + `PENDING_CREATE` + WorkManager.** Offline create works end to end.
3. **`TodoQuery` rendering both ways.** Filters work, and work consistently offline.
4. **Rotation.** Mostly falls out of steps 1–3 — `ViewModel` + one saved string.

If I ran out of time at any point, what exists is coherent rather than half-wired.

### What I would cut first, in order

1. **`PENDING_UPDATE` push.** The supplied contract has no PUT/PATCH, so the outbox is
   shaped for it but the branch is a no-op. Adding it is one call once the endpoint exists.
2. **Paging.** `@RawQuery` can return a `PagingSource` instead of `Flow<List<>>`; it is a
   drop-in change and it is not needed until the list is long.
3. **The partial outbox index** (§5). Add it against a measured slow query, not a guess.
4. **Idempotency on create.** See the table in §6 — a lost response duplicates a row
   server-side. The fix is a client-generated key, which needs a contract change.
5. **4xx handling.** A permanently rejected create currently retries forever with backoff.
   It should get a terminal `FAILED` state and a visible "couldn't sync" affordance.

### Questions I would ask before writing production code

1. **Period semantics** — containment or overlap? (§7. Changes two operators, and the
   answer must match the server's.)
2. **Does the create response include the id?** The design depends on getting a
   `remoteId` back from the POST. If it returns `204 No Content`, reconciliation needs a
   client-generated id in the request instead.
3. **Is `q` an AND of all conditions?** The examples imply it. If `+` ever means OR, both
   renderers change together — which is the payoff of having exactly one predicate object.
4. **Pagination and total counts** — does `GET` return everything? That decides whether
   Paging 3 is needed now rather than later.
5. **Deletes and multi-device** — is there a delete endpoint, and can the same account
   edit from two devices? That is what decides whether "local unsynced wins" stays an
   adequate conflict policy or has to become real last-writer-wins with server timestamps.
