# Todo skeleton — reviewed solution

Reference implementation for the `com.lycorp.interview` todo skeleton review.

Not wired into Gradle: `settings.gradle.kts` includes only `:app`, so nothing
here is compiled. To build it, add `include(":interview-solution")`, give the
module a `build.gradle.kts` with the AndroidX + coroutines dependencies, and
supply `res/layout/activity_main.xml` and `res/layout/item_todo.xml`.

## Layout

```
com/lycorp/interview/
├── MainActivity.kt
├── client/
│   ├── TodoApiClient.kt        interface, documents the error contract
│   ├── MockTodoApiClient.kt    class, Resources injected (was a leaky object)
│   └── FakeTodoApiClient.kt    pure-JVM double for unit tests
├── model/
│   └── Todo.kt                 immutable, Instant instead of Date
├── ui/
│   ├── TodoAdapter.kt          ListAdapter + DiffUtil
│   └── TodoViewHolder.kt       private views, bind(), listener cleared first
└── viewmodel/
    ├── TodoUiState.kt          Loading / Success / Error
    └── TodoViewModel.kt        error handling + Factory + setDone()
```

## What each fix addresses

| # | Problem in the original | Fixed in |
| - | ----------------------- | -------- |
| 1 | `lifecycleScope.collect` updates the UI while the Activity is backgrounded | `MainActivity.kt` — `repeatOnLifecycle(STARTED)` |
| 2 | `by viewModels()` on a ViewModel with a constructor arg — runtime crash | `TodoViewModel.Factory` |
| 3 | `fetchTodos()` throws `IOException` / `TimeoutException` uncaught — app crash | `TodoViewModel.refreshTodos()` |
| 4 | Checkbox listener not cleared before `isChecked` — recycled holder flips the wrong row | `TodoViewHolder.bind()` |
| 5 | `object MockTodoApiClient` holds a `Context` — Activity leak, `attachContext()` never called | `MockTodoApiClient` constructor injection |
| 6 | Adapter mutates `todoItem.isDone`; ViewModel never learns, state lost on rotation | `TodoViewModel.setDone()` + immutable `Todo` |
| 7 | `refreshTodos()` never called — list always empty | `TodoViewModel.init` |
| 8 | New adapter per emission, no diffing — scroll jumps, full rebind | `TodoAdapter : ListAdapter` |
| 9 | Missing / glued imports (`launch`, `viewModels`, `lifecycleScope`) | all files |
| 10 | `var` data class, `Date` + `Instant` mixed | `Todo.kt` |
