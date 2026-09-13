# Code Review — Todo Skeleton (`com.lycorp.interview`)

Review of the todo web skeleton: data model, list UI, mock API client.
Requirement under test: **the UI must not be updated while the Activity is in the background.**
XML layouts assumed correct.

Each section lists the problems found in one file, then the change made in
`interview-solution/`.

---

## Severity summary

| # | Problem | File | Impact |
| - | ------- | ---- | ------ |
| 1 | Flow collected outside a lifecycle scope | `MainActivity.kt` | **Violates the stated requirement** — UI updated in background |
| 2 | `by viewModels()` on a ViewModel with a constructor argument | `MainActivity.kt` | Crash on launch |
| 3 | `fetchTodos()` exceptions never caught | `TodoViewModel.kt` | Crash on any network error |
| 4 | Checkbox listener not detached before `isChecked` | `TodoAdapter.kt` | Wrong row silently toggled on scroll |
| 5 | `object` singleton holds a `Context` | `TodoApiClient.kt` | Activity memory leak |
| 6 | Adapter mutates the model directly | `TodoAdapter.kt` | State lost on refresh / rotation |
| 7 | `refreshTodos()` and `attachContext()` never called | `MainActivity.kt` | Screen never loads anything |
| 8 | New adapter instance per emission | `MainActivity.kt` | Scroll position lost, no recycling, full rebind |
| 9 | Missing and glued imports | all files | Does not compile |
| 10 | `var` data class, `Date` + `Instant` mixed | `Todo.kt` | Unstable `equals`/`hashCode`, breaks DiffUtil |

---

## 1. `MainActivity.kt`

### Problems

**1.1 — UI updated while the Activity is in the background (the core requirement)**

```kotlin
lifecycleScope.launch {
    viewModel.todos.collect { todos ->
        todo_list.adapter = TodoAdapter(todos)
    }
}
```

`lifecycleScope` is cancelled only at `ON_DESTROY`. Between `onStop()` and
`onDestroy()` the coroutine is still alive, so any emission from the ViewModel
reaches the RecyclerView while the screen is not visible. That is wasted work
at best, and it is exactly what the spec forbids.

**1.2 — `by viewModels()` cannot construct this ViewModel**

`TodoViewModel(private val apiClient: TodoApiClient)` has no no-argument
constructor. The default factory reflects on a zero-arg constructor and fails
with `InstantiationException` the first time `viewModel` is touched.

**1.3 — Nothing ever triggers a load**

`refreshTodos()` has no caller anywhere in the codebase, and
`MockTodoApiClient.attachContext()` is never called either. Even if the
ViewModel could be constructed, the list would stay empty and every mock string
would fall through to `?: ""`.

**1.4 — A new adapter on every emission**

`todo_list.adapter = TodoAdapter(todos)` discards the view pool, resets the
scroll position to the top, and rebinds every visible row, on every single
state change.

**1.5 — Errors are invisible**

`fetchTodos()` is documented to throw `IOException` and `TimeoutException`.
Nothing in this screen can distinguish "loading", "empty" and "failed" — all
three look like an empty list.

**1.6 — Compile and hygiene issues**

- Missing imports: `androidx.activity.viewModels`, `androidx.lifecycle.lifecycleScope`.
- Unused imports: `Toast`, `Dispatchers`, `withContext`, `MockTodoApiClient`.
- `private lateinit var todo_list` — snake_case is not Kotlin style, and the
  reference does not need to be a field at all; it is only used in `onCreate`.

### Suggested solution

- Wrap collection in `repeatOnLifecycle(Lifecycle.State.STARTED)` so collection
  starts at `ON_START` and is cancelled at `ON_STOP`. This is the direct fix for
  the requirement.
- Supply `TodoViewModel.Factory` to `by viewModels { ... }`, built with
  `MockTodoApiClient(applicationContext.resources)` — application resources, so
  nothing that outlives the screen is captured.
- Create the adapter once as a field and feed it with `submitList()`.
- Pass a lambda into the adapter that forwards checkbox changes to
  `viewModel.setDone(id, isDone)`.
- Render a sealed `TodoUiState` in a `when`, so loading and error have a place
  to go.
- Make the RecyclerView a local `val todoList`, fix the imports.

```kotlin
lifecycleScope.launch {
    repeatOnLifecycle(Lifecycle.State.STARTED) {
        viewModel.uiState.collect(::render)
    }
}
```

---

## 2. `model/Todo.kt`

### Problems

**2.1 — Every field is `var`**

The adapter writes `todoItem.isDone = isChecked` straight onto the instance the
ViewModel is holding. The list mutates underneath its owner, no new value is
emitted, and nobody is notified.

**2.2 — A `data class` with `var` fields has an unstable `hashCode()`**

Once the object is mutated, its `equals`/`hashCode` change. That breaks use in
a `HashSet`/`HashMap` key, and it breaks `DiffUtil.areContentsTheSame`: the
"old" and "new" item are literally the same instance, so the diff always
reports no change and the row never redraws.

**2.3 — `java.util.Date`**

Legacy and mutable. A caller that receives `startDate` can call
`setTime()` on it and change the todo from the outside.

**2.4 — Two time types in one model**

`java.time.Instant` is imported only to feed `Date.from(...)`. Pick one type;
carrying both invites conversion bugs.

**2.5 — `java.time` needs API 26 or desugaring**

On a lower `minSdk` without core library desugaring this fails at runtime on
older devices.

**2.6 — `Date.from(Instant.now())` as a default argument**

The default is evaluated at construction time, so the model has a hidden
dependency on the system clock. Tests cannot produce a deterministic `Todo`.

**2.7 — No invariant on the date range**

An `endDate` before `startDate` is constructible.

### Suggested solution

- All fields `val`; callers produce changes with `copy()`.
- `Instant` everywhere, `Date` dropped.
- `require(endDate == null || !endDate.isBefore(startDate))` in `init`.
- Document the desugaring requirement:

```groovy
compileOptions { coreLibraryDesugaringEnabled true }
dependencies { coreLibraryDesugaring "com.android.tools:desugar_jdk_libs:2.0.4" }
```

For full testability the clock should eventually be injected rather than read
via `Instant.now()` in a default argument.

---

## 3. `client/TodoApiClient.kt`

### Problems

**3.1 — A singleton holding a `Context` leaks**

```kotlin
object MockTodoApiClient : TodoApiClient {
    private var context: Context? = null
    fun attachContext(context: Context) { this.context = context }
}
```

The `object` lives for the whole process. If an Activity context is passed in
— the obvious call site — the Activity and its entire view tree can never be
collected.

**3.2 — `attachContext()` is temporal coupling**

The client only works if someone remembers to call it first. Forget, and
`context?.getString(...) ?: ""` returns empty strings: no crash, no log, just
a list of blank rows.

**3.3 — Global mutable state**

`private var context` is not thread safe, and in unit tests it persists across
test methods, so one test can change another test's behaviour.

**3.4 — An `object` cannot be substituted**

There is no way to inject a different implementation for a test. The whole
point of extracting the `TodoApiClient` interface is lost.

**3.5 — The mock depends on `R.string`**

It needs the Android framework, so it cannot run in a plain JVM `src/test`
unit test.

**3.6 — No dispatcher**

`fetchTodos()` runs on whatever the caller provides. The real implementation
would do blocking socket work; ownership of the dispatcher belongs to the
client, not the caller.

**3.7 — The error contract is undocumented in code**

The fact that `IOException` and `TimeoutException` can come out only appears
in a trailing comment, so no caller is prompted to handle them.

### Suggested solution

- `object` → `class MockTodoApiClient(private val resources: Resources)`.
  Constructor injection removes the leak, the global state and the temporal
  coupling in one change. Construct with `applicationContext.resources`.
- `attachContext()` deleted. A missing dependency is now a compile error
  instead of a silent blank row.
- Wrap the body in `withContext(Dispatchers.IO)`.
- Move `@throws IOException` / `@throws TimeoutException` onto the interface
  KDoc so the contract is visible at the call site.
- Split the file: `TodoApiClient.kt` (interface) and `MockTodoApiClient.kt`
  (implementation).
- Add `FakeTodoApiClient` — no Android dependency, optionally throws — so both
  the success and the error branch of the ViewModel are unit-testable.

---

## 4. `viewmodel/TodoViewModel.kt`

### Problems

**4.1 — Missing `import kotlinx.coroutines.launch`**

`kotlinx.coroutines.flow.*` does not cover `launch`. The file does not compile.

**4.2 — No error handling**

```kotlin
viewModelScope.launch { mutableTodos.value = apiClient.fetchTodos() }
```

`IOException` or `TimeoutException` propagates out of `viewModelScope` to the
default uncaught handler and takes the process down.

**4.3 — `refreshTodos()` is never invoked**

No `init` block, no caller. The screen loads nothing.

**4.4 — The mutable flow is exposed**

`val todos: StateFlow<List<Todo>> = mutableTodos` — a consumer can cast it back
to `MutableStateFlow` and write into it, so the ViewModel is no longer the only
writer.

**4.5 — Only a raw `List<Todo>` is exposed**

An empty list is ambiguous: still loading, loaded-and-empty, or failed.

**4.6 — Concurrent refreshes**

Two `refreshTodos()` calls start two requests. Whichever finishes last wins,
which may be the older one.

**4.7 — No factory**

The Activity has no way to construct it (see 1.2).

**4.8 — No path for a checkbox change**

The UI has nothing to call, which is why the adapter resorted to mutating the
model.

### Suggested solution

- Add the missing import.
- `try/catch` around `fetchTodos()` mapping both documented exceptions to
  `TodoUiState.Error`.
- Call `refreshTodos()` from `init`.
- Expose `mutableUiState.asStateFlow()`.
- Introduce `sealed interface TodoUiState { Loading, Success(todos), Error(cause) }`
  in its own file, which also makes the Activity's `when` exhaustive.
- Keep the running job in `refreshJob` and drop a call while one is active.
- Add `TodoViewModel.Factory : ViewModelProvider.Factory` (swap for
  `@HiltViewModel` once DI lands).
- Add `setDone(id, isDone)` which rebuilds the list with `copy()` and emits —
  making the ViewModel the single source of truth for the done flag.

---

## 5. `ui/TodoAdapter.kt`

### Problems

**5.1 — The recycled-listener bug (worst bug in the adapter)**

```kotlin
holder.isDone.isChecked = todoItem.isDone          // fires the PREVIOUS row's listener
holder.isDone.setOnCheckedChangeListener { ... }   // attached too late
```

A recycled `TodoViewHolder` still carries the listener bound for the row it
previously displayed. Writing `isChecked` fires that stale listener, which
flips `isDone` on the wrong `Todo`. The user scrolls, and an unrelated item
silently becomes done.

**5.2 — The adapter mutates the domain model**

`todoItem.isDone = isChecked` writes into the ViewModel's list from the view
layer. No emission, no persistence: the change disappears on the next refresh
or configuration change.

**5.3 — Plain `Adapter` with a constructor list**

Combined with `MainActivity` building a new adapter per emission, there is no
diffing at all — every update is a full rebind.

**5.4 — A new lambda allocated on every bind**

`setOnCheckedChangeListener { ... }` inside `onBindViewHolder` allocates on
every scroll frame.

**5.5 — No stable ids**

Without `DiffUtil` or `setHasStableIds(true)` the RecyclerView cannot tell
which row is which, so it cannot animate or skip rebinds.

**5.6 — `import com.lycorp.interview.R` glued to the next import**

Does not compile.

### Suggested solution

- `TodoAdapter : ListAdapter<Todo, TodoViewHolder>(DIFF_CALLBACK)` with a
  `DiffUtil.ItemCallback` comparing `id` for identity and the whole object for
  contents (correct only because `Todo` is now immutable).
- Constructor takes `onDoneChanged: (id: Int, isDone: Boolean) -> Unit`
  instead of a list; the Activity creates the adapter once and calls
  `submitList()`.
- Binding moved into `TodoViewHolder.bind()`; the listener fix lives there.
- The callback is created once at adapter construction, not per bind.

---

## 6. `ui/TodoViewHolder.kt`

### Problems

**6.1 — The view fields are public**

`val title`, `val content`, `val isDone` let the adapter reach into the
holder's widgets, so binding logic ends up split across two classes — which is
how the listener-ordering bug got written in the first place.

**6.2 — Assignment in an `init` block**

Verbose; inline property initialisers say the same thing in one line each.

**6.3 — `findViewById`**

Unchecked at compile time. ViewBinding is the safer default once enabled.

### Suggested solution

- Make all three views `private` and add
  `fun bind(todo: Todo, onDoneChanged: (Int, Boolean) -> Unit)`.
- Inside `bind`, detach the listener before writing the state:

```kotlin
isDone.setOnCheckedChangeListener(null)
isDone.isChecked = todo.isDone
isDone.setOnCheckedChangeListener { _, checked -> onDoneChanged(todo.id, checked) }
```

- Inline the property initialisers, drop the `init` block.
- Note ViewBinding as the follow-up:
  `TodoViewHolder(ItemTodoBinding.inflate(inflater, parent, false))`.

---

## Resulting package layout

```
com/lycorp/interview/
├── MainActivity.kt              repeatOnLifecycle(STARTED), one adapter, factory wiring
├── client/
│   ├── TodoApiClient.kt         interface; error contract in KDoc
│   ├── MockTodoApiClient.kt     class taking Resources (was a leaky object)
│   └── FakeTodoApiClient.kt     pure-JVM double for unit tests
├── model/
│   └── Todo.kt                  immutable, Instant, date-range invariant
├── ui/
│   ├── TodoAdapter.kt           ListAdapter + DiffUtil, change reported upward
│   └── TodoViewHolder.kt        private views, bind(), listener detached first
└── viewmodel/
    ├── TodoUiState.kt           Loading / Success / Error
    └── TodoViewModel.kt         error handling, init refresh, Factory, setDone()
```

One layer per package, and — unlike the original — no layer reaching into
another: the adapter no longer writes to the model, the client no longer holds
a `Context`, and the Activity no longer constructs adapters on the fly.

---

## Follow-ups worth raising in the interview

These are beyond the skeleton's scope but are the natural next questions.

- **Dependency injection** — `TodoViewModel.Factory` is a stopgap. Hilt with
  `@HiltViewModel` removes the factory and the manual wiring in `MainActivity`.
- **Persistence** — `setDone()` currently updates in-memory state only. It
  should reach a repository, which writes to Room and the API.
- **Dispatcher injection** — the ViewModel should take a `CoroutineDispatcher`
  so tests can substitute a `TestDispatcher`.
- **`SavedStateHandle`** — survives process death, which `ViewModel` alone
  does not.
- **ViewBinding** — replaces `findViewById` in both the Activity and the holder.
- **Retry affordance** — `TodoUiState.Error` currently only shows a Toast; it
  should offer a retry that calls `refreshTodos()`.
