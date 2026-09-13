# Process vs Thread vs Coroutine in Android

A practical guide to the three units of execution an Android developer works with, what each
one actually is at the OS and runtime level, and how each one affects the performance,
memory footprint, and responsiveness of an Android app.

---

## Table of Contents

1. [Quick Comparison](#quick-comparison)
2. [Process](#1-process)
   - [What it is](#what-a-process-is)
   - [Example](#process-example)
   - [Impact on performance and efficiency](#process-impact-on-performance-and-efficiency)
3. [Thread](#2-thread)
   - [What it is](#what-a-thread-is)
   - [Example](#thread-example)
   - [Impact on performance and efficiency](#thread-impact-on-performance-and-efficiency)
4. [Coroutine](#3-coroutine)
   - [What it is](#what-a-coroutine-is)
   - [Example](#coroutine-example)
   - [Impact on performance and efficiency](#coroutine-impact-on-performance-and-efficiency)
5. [How the Three Layers Stack Up](#how-the-three-layers-stack-up)
6. [Choosing the Right Tool](#choosing-the-right-tool)
7. [Common Mistakes](#common-mistakes)

---

## Quick Comparison

| Aspect | Process | Thread | Coroutine |
|---|---|---|---|
| Managed by | Linux kernel / Android `Zygote` | Linux kernel, exposed via JVM `Thread` | Kotlin compiler + library, runs *on* threads |
| Memory isolation | Full — separate virtual address space | Shared heap inside one process | Shared heap, same thread as siblings |
| Typical cost to create | ~tens of MB, tens of ms | ~1 MB stack (default 512 KB–1 MB on ART), ~0.1–1 ms | Tens to hundreds of **bytes**, sub-microsecond |
| How many can you have | A handful | Hundreds before pain | Hundreds of thousands |
| Blocking cost | Whole app blocked | That thread's stack parked, kernel context switch | Suspends, releases the thread, no kernel involvement |
| Crash blast radius | Only that process dies | Whole process dies (uncaught exception) | Structured concurrency cancels the scope |
| Scheduling | Preemptive, by kernel | Preemptive, by kernel | Cooperative, at `suspend` points |
| Android relevance | `:remote` services, WebView, multi-process apps | `HandlerThread`, `ExecutorService`, main/UI thread | Default async model for modern Android |

---

## 1. Process

### What a process is

A process is an **operating-system-level container for a running program**. On Android — which
is Linux underneath — every process gets:

- its own **virtual address space** (its own heap, its own view of memory),
- its own **Linux UID** (Android assigns each installed app a unique UID),
- its own **ART (Android Runtime) instance**, with its own garbage collector, JIT caches, and
  loaded copy of your classes,
- its own **file descriptors**, sockets, and open files.

Two processes cannot read each other's memory. That is the whole point: isolation is a
security and stability boundary enforced by the kernel, not by your code.

Android does not fork processes from scratch. A template process called **Zygote** boots at
device start, preloads the framework classes and shared resources, and then forks itself for
each new app. The fork uses copy-on-write pages, so the framework code is shared physically
in RAM across every app on the device. This is why launching an Android app is much cheaper
than launching a fresh JVM.

By default your entire app — all activities, services, broadcast receivers, content
providers — runs inside **one** process with **one** main thread. You only get more processes
if you ask for them, or if the platform gives you one (a WebView renderer, for instance).

Crucially, Android's low-memory killer works at **process** granularity. When RAM runs short,
`lmkd` picks a process based on its importance (foreground activity > visible > service >
cached background) and kills it. Your app's lifecycle callbacks, saved state, and
`onSaveInstanceState` exist precisely because your process can be destroyed at any time.

### Process example

Declare a second process for a component with `android:process` in the manifest. A leading
`:` makes the name private to your app:

```xml
<manifest xmlns:android="http://schemas.android.com/apk/res/android"
    package="com.example.composelearning">

    <application>

        <!-- Runs in the default process: com.example.composelearning -->
        <activity android:name=".MainActivity" />

        <!-- Runs in its own process: com.example.composelearning:sync
             Separate heap, separate ART, isolated crash domain. -->
        <service
            android:name=".sync.HeavySyncService"
            android:process=":sync" />

        <!-- A globally named process, shareable with another app
             that has the same signature and sharedUserId. -->
        <service
            android:name=".media.PlaybackService"
            android:process="com.example.shared:media" />

    </application>
</manifest>
```

Because the two processes share nothing, you cannot pass an object reference between them.
A static field written in `:sync` is invisible to the UI process — each process has its own
copy of the class and therefore its own static. Crossing the boundary means **IPC**, and IPC
means serializing to a `Parcel` and going through the kernel's Binder driver:

```kotlin
// AIDL-generated stub, or Messenger, or a ContentProvider — all of these are Binder underneath.
class HeavySyncService : Service() {

    private val binder = object : ISyncService.Stub() {
        override fun syncNow(accountId: String): Int {
            // Runs on a Binder thread pool thread inside the :sync process.
            return repository.sync(accountId)
        }
    }

    override fun onBind(intent: Intent): IBinder = binder
}
```

```kotlin
// In the UI process
private val connection = object : ServiceConnection {
    override fun onServiceConnected(name: ComponentName, service: IBinder) {
        val remote = ISyncService.Stub.asInterface(service)
        // This call marshals arguments into a Parcel, traverses Binder into the
        // other process, and blocks the caller until the reply comes back.
        val changed = remote.syncNow(accountId)
    }

    override fun onServiceDisconnected(name: ComponentName) { /* remote process died */ }
}

bindService(Intent(this, HeavySyncService::class.java), connection, Context.BIND_AUTO_CREATE)
```

Detecting which process you are in — needed because `Application.onCreate()` runs **once per
process**, so naive SDK initialization runs multiple times:

```kotlin
class App : Application() {
    override fun onCreate() {
        super.onCreate()

        val processName = if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.P) {
            getProcessName()
        } else {
            // Fallback: read /proc/self/cmdline
            File("/proc/self/cmdline").readText().trim { it <= ' ' }
        }

        if (processName == packageName) {
            // Main process only: analytics, crash reporting, image loader, DI graph.
            initAnalytics()
            initCrashReporting()
        }
        // Otherwise keep the child process lean — do not pay for the full graph twice.
    }
}
```

### Process: impact on performance and efficiency

**Costs**

| Cost | Why it happens |
|---|---|
| **Memory duplication** | Every extra process gets its own ART heap, its own loaded copy of your DEX/classes, its own GC, and its own thread stacks. Expect a floor of roughly 10–30 MB of private dirty memory per process even before your code allocates anything. Framework pages are shared via Zygote copy-on-write, but *your* app's classes and objects are not. |
| **Higher kill probability** | More processes means a larger total memory footprint for your app, which raises your `oom_score_adj` and makes the low-memory killer more likely to target you. Being killed means cold starts, lost state, and a worse perceived experience. |
| **IPC latency and marshaling** | Every cross-process call serializes arguments into a `Parcel`, crosses the kernel Binder driver, and deserializes on the other side. A simple round trip is on the order of tens to hundreds of microseconds — orders of magnitude slower than a direct method call. Binder transactions are also capped (roughly 1 MB per transaction *shared across the whole process*), so bulk data must move through `ParcelFileDescriptor`, a shared-memory `MemoryFile`, or a `ContentProvider`, not through the call arguments. |
| **Startup cost** | Forking, ART initialization, class loading, and running `Application.onCreate()` again. A child process being spun up during a critical user flow shows up directly as jank or a spinner. |
| **Duplicated initialization** | Without a process check, every SDK in `Application.onCreate()` initializes once per process — duplicate analytics sessions, duplicate database handles, extra memory, extra CPU, extra battery. |

**Benefits, when they apply**

- **Crash and OOM isolation.** A native crash or an `OutOfMemoryError` in a separate process
  does not take down the UI. This is why the platform runs WebView renderers out of process,
  and why apps that decode huge media or run untrusted native code often do the same.
- **Escaping the per-process heap limit.** Each process gets its own Dalvik/ART heap cap
  (device-dependent, commonly 192–512 MB). A memory-hungry subsystem in its own process does
  not consume the UI process's budget.
- **Independent lifetimes.** A long-running playback or sync process can survive the UI
  process being killed, and a heavy background process can be torn down without disturbing
  the UI.

**Rule of thumb:** extra processes buy *isolation*, and you pay for it in *RAM and latency*.
Add one only when you need a real boundary — untrusted code, native crash containment, a
genuinely huge heap, or a lifetime independent of the UI. Never add one "for background work";
that is what threads and coroutines are for.

---

## 2. Thread

### What a thread is

A thread is a **single sequential path of execution inside a process**. Threads in the same
process share the heap, static fields, loaded classes, and file descriptors; each thread has
its own **program counter, registers, and call stack**.

On Android, a JVM `Thread` is a thin wrapper over a real Linux thread (`pthread`). The kernel
schedules it preemptively — it can be suspended between any two instructions — and each thread
gets a fixed-size stack reserved when it starts.

Every Android app has one thread that matters above all others: the **main thread**, also
called the UI thread. It runs a `Looper` — an infinite loop pulling `Message` objects off a
`MessageQueue` — and that loop is what dispatches:

- lifecycle callbacks (`onCreate`, `onResume`, …),
- input events (touch, key),
- measure / layout / draw, and in Compose, recomposition and the frame's draw pass,
- `Handler.post` work and `View.post` work.

Two hard rules follow from the main thread's design:

1. **The UI toolkit is not thread-safe.** Touching views or Compose state from another thread
   without confining it to the main thread is a bug (`CalledFromWrongThreadException` at best,
   silent corruption at worst).
2. **Anything slow on the main thread is visible jank.** A 60 Hz display gives you 16.6 ms per
   frame; a 120 Hz display gives you 8.3 ms. Exceeding that budget drops frames. Exceeding
   ~5 s on an input event triggers an **ANR** (Application Not Responding) dialog.

### Thread example

**Raw thread — almost always the wrong choice, but instructive:**

```kotlin
// Manual thread: fire and forget. No lifecycle awareness, no cancellation,
// no way to get a result back except by hand.
Thread {
    val bitmap = decodeLargeBitmap(file)   // heavy CPU work, off the main thread
    runOnUiThread {                        // hop back to update the UI
        imageView.setImageBitmap(bitmap)
    }
}.start()
```

Problems: it allocates a fresh ~1 MB stack, the thread is unnamed in traces, nothing cancels
it when the Activity is destroyed, and if the user scrolls a list you create one such thread
per row.

**Pooled threads — the classic correct answer before coroutines:**

```kotlin
object Io {
    // Fixed pool: bounded, reused, named for traces. Threads are created once
    // and handed work repeatedly instead of being created per task.
    val executor: ExecutorService = Executors.newFixedThreadPool(4) { runnable ->
        Thread(runnable, "app-io").apply { priority = Thread.NORM_PRIORITY - 1 }
    }
}

val future = Io.executor.submit<List<User>> { repository.loadUsers() }
```

**A dedicated looper thread — the right shape for a serial work queue:**

```kotlin
// One thread, one Looper, one MessageQueue: work runs strictly in order,
// so no locking is needed between tasks on this thread.
private val handlerThread = HandlerThread("db-writer").apply { start() }
private val dbHandler = Handler(handlerThread.looper)

fun persist(entity: Entity) {
    dbHandler.post { database.insert(entity) }
}

fun shutdown() {
    handlerThread.quitSafely()   // always tear it down, or the thread leaks
}
```

**Shared mutable state — the tax you pay for a shared heap:**

```kotlin
// Broken: two threads incrementing the same field can interleave between
// the read and the write, so updates are lost.
class Counter {
    var value = 0
    fun increment() { value++ }   // read, add, write — not atomic
}

// Correct, with a lock: cheap to write, but contention serializes callers.
class LockedCounter {
    private val lock = Any()
    private var value = 0
    fun increment() = synchronized(lock) { value++ }
}

// Correct, lock-free: a single CAS instruction, no blocking, no context switch.
class AtomicCounter {
    private val value = AtomicInteger(0)
    fun increment() = value.incrementAndGet()
}
```

**Confirming you are not on the main thread — worth asserting in repositories:**

```kotlin
fun requireBackgroundThread() {
    check(Looper.myLooper() != Looper.getMainLooper()) {
        "Blocking call on the main thread"
    }
}
```

### Thread: impact on performance and efficiency

**Where threads help**

- **They keep the main thread free.** Moving disk I/O, network, JSON parsing, bitmap decoding,
  and database queries off the main thread is the single most important thing you can do for
  frame times and for avoiding ANRs.
- **They use multiple cores.** A phone with 8 cores can genuinely run 8 CPU-bound tasks at
  once. Parallelism is only available through threads — coroutines get it by running *on*
  multiple threads.

**Where threads cost you**

| Cost | Detail |
|---|---|
| **Memory per thread** | Each thread reserves a stack (typically 512 KB–1 MB of virtual address space on ART, with resident pages growing as the stack is used) plus kernel bookkeeping. 200 threads is a real memory problem; on 32-bit devices it is an address-space problem too. |
| **Creation latency** | Starting a thread is a syscall plus stack allocation — roughly 0.1–1 ms. Doing it per item in a scrolling list is measurable jank. This is exactly why thread pools exist. |
| **Context-switch overhead** | A kernel context switch costs on the order of a microsecond of CPU plus the invisible cost of trashing the CPU caches and TLB. Once runnable threads outnumber cores, the scheduler spends real time shuffling them instead of doing your work. |
| **Blocking wastes a whole thread** | A thread waiting on a socket or a lock occupies its entire stack and its pool slot while doing nothing. Pool exhaustion — all threads blocked, queue growing, nothing progressing — is the classic failure mode. |
| **Synchronization** | Shared mutable state needs locks; locks bring contention, priority inversion, and deadlock. Every `synchronized` block is a potential serialization point that erases the parallelism you added the threads for. |
| **Battery** | Waking cores, keeping them out of deep idle, and spinning up extra threads all draw power. More concurrency than the device can usefully execute burns battery for no throughput gain. |
| **Priority matters on Android** | A background thread created with default priority competes with the UI thread. Android recommends setting `Process.setThreadPriority(Process.THREAD_PRIORITY_BACKGROUND)` so the scheduler puts it in the background cgroup, where it is capped to a small share of CPU and cannot starve rendering. |

**Sizing guidance**

- CPU-bound pool: about `Runtime.getRuntime().availableProcessors()` threads. More than that
  only adds context switches.
- I/O-bound pool: more than the core count is fine, because the threads are mostly parked —
  but each one still costs a stack, which is precisely the limitation coroutines remove.

---

## 3. Coroutine

### What a coroutine is

A coroutine is a **suspendable computation** — a block of code that can pause at well-defined
points and resume later, without blocking the thread it was running on. It is a *language and
library* construct, not an OS construct. The kernel has never heard of your coroutines.

The mechanism is compiler-driven. The Kotlin compiler transforms every `suspend` function into
a **state machine** (a `Continuation`): the function body is split at each suspension point,
and local variables that must survive a suspension are stored as fields on a generated class.
When a coroutine suspends, its state lives in a small object on the **heap** and the thread is
returned to the pool to run something else. When the awaited work completes, the continuation
is resumed — possibly on a different thread.

Consequences of that design:

- **No stack is reserved.** A suspended coroutine is a heap object of tens to hundreds of
  bytes, not a megabyte of stack. Hundreds of thousands of them fit comfortably in memory.
- **Suspension is not a context switch.** Resuming a coroutine is a normal method call
  dispatched through the coroutine's dispatcher — no kernel involvement, no TLB flush.
- **Scheduling is cooperative.** A coroutine yields only at a `suspend` call. A tight
  CPU-bound loop with no suspension point never yields, and cannot even be cancelled, unless
  you call `yield()` or check `isActive`.

Two library concepts do the practical work:

**Dispatchers** decide which thread(s) a coroutine runs on:

| Dispatcher | Backing threads | Use for |
|---|---|---|
| `Dispatchers.Main` | The Android main/UI thread | UI updates, state assignment, navigation |
| `Dispatchers.Main.immediate` | Main thread, no re-post if already there | Avoiding an extra message-queue hop |
| `Dispatchers.Default` | Shared pool sized to the core count | CPU work: parsing, sorting, image processing |
| `Dispatchers.IO` | Elastic pool, up to 64 threads by default, shared with `Default` | Blocking I/O: files, sockets, JDBC-style APIs |
| `Dispatchers.Unconfined` | Whatever thread resumes it | Test and library internals; avoid in app code |

**Structured concurrency** ties every coroutine to a `CoroutineScope`. A scope cannot complete
until its children complete, cancelling a scope cancels every child, and a failure propagates
to the parent (unless a `SupervisorJob` isolates siblings). That is what makes leaks and
orphaned work hard to write by accident — the opposite of a raw `Thread`.

### Coroutine example

**The shape you write every day in Android:**

```kotlin
class UserViewModel(
    private val repository: UserRepository
) : ViewModel() {

    private val _state = MutableStateFlow<UiState>(UiState.Loading)
    val state: StateFlow<UiState> = _state.asStateFlow()

    fun load(userId: String) {
        // viewModelScope is cancelled automatically in onCleared(),
        // so this work cannot outlive the ViewModel.
        viewModelScope.launch {
            _state.value = UiState.Loading
            try {
                // Suspends here. The main thread is NOT blocked — it goes back to
                // the Looper and keeps drawing frames while the network call runs.
                val user = repository.fetchUser(userId)
                _state.value = UiState.Success(user)
            } catch (e: CancellationException) {
                throw e                     // never swallow cancellation
            } catch (e: IOException) {
                _state.value = UiState.Error(e.message)
            }
        }
    }
}
```

**Confining blocking work with `withContext` — the repository's job, not the caller's:**

```kotlin
class UserRepository(
    private val api: ApiService,
    private val dao: UserDao,
    private val io: CoroutineDispatcher = Dispatchers.IO   // injected for tests
) {
    // A well-behaved suspend function is main-safe: callable from Dispatchers.Main
    // without blocking it, because it switches dispatchers internally.
    suspend fun fetchUser(id: String): User = withContext(io) {
        val cached = dao.findById(id)          // blocking SQLite call, safe here
        if (cached != null && cached.isFresh()) return@withContext cached

        val remote = api.getUser(id)           // blocking or suspending HTTP call
        dao.insert(remote)
        remote
    }
}
```

**Real concurrency, in three lines:**

```kotlin
suspend fun loadDashboard(): Dashboard = coroutineScope {
    // Both requests start immediately and run concurrently.
    // coroutineScope waits for both; if either fails, the other is cancelled.
    val profile = async { repository.fetchProfile() }
    val feed    = async { repository.fetchFeed() }
    Dashboard(profile.await(), feed.await())
}
```

**Parallel CPU work across cores, with a bounded fan-out:**

```kotlin
suspend fun thumbnails(files: List<File>): List<Bitmap> = withContext(Dispatchers.Default) {
    files.map { file ->
        async { decodeThumbnail(file) }   // Default pool == core count, so this
    }.awaitAll()                          // saturates the CPU without oversubscribing it
}
```

**Why coroutines scale where threads do not:**

```kotlin
// 100_000 threads: impossible. ~100 GB of stacks.
// 100_000 coroutines: a few tens of MB of continuation objects. Runs fine.
runBlocking {
    val jobs = List(100_000) {
        launch { delay(1_000) }   // delay() suspends; it does not block a thread
    }
    jobs.joinAll()
}
```

**Cancellation you get for free — and the trap:**

```kotlin
// Cooperative: delay() and other suspend functions check for cancellation,
// so leaving the screen stops the loop.
viewModelScope.launch {
    while (true) {
        refresh()
        delay(5_000)
    }
}

// NOT cooperative: no suspension point, so cancellation is ignored and
// this keeps burning a Default thread after the scope is cancelled.
viewModelScope.launch(Dispatchers.Default) {
    while (true) {
        crunch()                  // fix: check isActive, or call yield()
    }
}
```

**Lifecycle-aware collection in the UI layer:**

```kotlin
// Compose: collection stops when the composable leaves composition,
// and flowWithLifecycle stops it when the app goes to the background.
@Composable
fun UserScreen(viewModel: UserViewModel = viewModel()) {
    val state by viewModel.state.collectAsStateWithLifecycle()

    when (state) {
        UiState.Loading    -> CircularProgressIndicator()
        is UiState.Success -> UserContent((state as UiState.Success).user)
        is UiState.Error   -> ErrorMessage((state as UiState.Error).message)
    }
}
```

### Coroutine: impact on performance and efficiency

**Where coroutines win**

| Benefit | Detail |
|---|---|
| **Tiny memory footprint** | A suspended coroutine is a heap object, typically well under a kilobyte, versus a thread's ~1 MB stack. Thousands of in-flight operations become affordable — per-item image loads in a list, one coroutine per WebSocket subscription, one per row of a paged feed. |
| **No thread blocked while waiting** | The dominant Android workload is waiting — on the network, on disk, on a timer. A blocked thread wastes a full stack and a pool slot; a suspended coroutine wastes neither. The same 4-thread pool can serve thousands of concurrent I/O operations. |
| **Cheap switching** | Resumption is a method dispatch, not a kernel context switch. Fine-grained concurrency stops being expensive, so you can decompose work naturally instead of batching it to avoid overhead. |
| **Main-thread safety by construction** | `withContext(Dispatchers.IO)` inside the repository makes every `suspend` function main-safe. Callers never have to know which thread to be on, which removes the single most common source of accidental main-thread I/O and therefore of jank and ANRs. |
| **Automatic cancellation** | `viewModelScope`, `lifecycleScope`, and `collectAsStateWithLifecycle` cancel work when the user navigates away. Cancelled work stops using CPU, stops holding memory, and stops making network calls — a direct, measurable battery and data win that manual threading rarely achieves in practice. |
| **Structured error propagation** | Failures surface at the `coroutineScope`/`await` boundary as ordinary exceptions. No callback pyramids, no dropped errors, and no `Future.get()` deadlocks. |
| **Backpressure and streams** | `Flow` gives lazy, cancellable, backpressure-aware streams with operators (`debounce`, `conflate`, `flatMapLatest`) that would each be a hand-rolled, bug-prone thread coordination problem. `conflate()` on a UI flow drops intermediate values you were never going to render — less work, fewer recompositions. |

**Where coroutines cost you, and the pitfalls**

| Cost / pitfall | Detail |
|---|---|
| **Not magic parallelism** | Coroutines are a *concurrency* tool. Parallelism still comes from the threads underneath. Ten coroutines on `Dispatchers.Main` run one at a time. CPU work needs `Dispatchers.Default`. |
| **Blocking inside a coroutine still blocks a thread** | `Thread.sleep`, a synchronous `File.readText`, or a blocking `Call.execute()` occupies the underlying thread for its full duration. Do that on `Dispatchers.Main` and you jank; do it 64 times on `Dispatchers.IO` and you exhaust the pool. Suspend, or confine it with `withContext`. |
| **Non-cooperative loops ignore cancellation** | A tight loop with no suspension point cannot be cancelled. Insert `yield()` or check `isActive`. |
| **Allocation and state-machine overhead** | Each suspension allocates continuation state, and each `launch`/`async` allocates a `Job`. Negligible against I/O, but in an extremely hot inner loop a `suspend` call is measurably more expensive than a plain one. Don't make a nanosecond-scale function `suspend`. |
| **`GlobalScope` reintroduces leaks** | Work launched in `GlobalScope` (or a scope you never cancel) outlives the screen that started it — the exact problem structured concurrency exists to solve. Use a lifecycle-bound scope. |
| **Swallowed `CancellationException`** | `catch (e: Exception)` around suspending code catches cancellation too and breaks the cancellation protocol. Rethrow `CancellationException`, or catch specific types. |
| **Dispatcher misuse** | `Dispatchers.IO` on CPU work oversubscribes the cores (up to 64 threads fighting over 8 cores); `Dispatchers.Default` on blocking I/O starves genuine CPU work. Match the dispatcher to the workload. |
| **Larger APK / debugging shape** | The `kotlinx-coroutines` dependency adds a modest amount of code, and stack traces cross suspension boundaries. Enable `kotlinx-coroutines-debug` or the IDE's coroutine debugger, and name your scopes with `CoroutineName` for readable traces. |
| **A process kill beats every scope** | Coroutines live and die with the process. Work that must survive the process being killed belongs in `WorkManager`, not in a coroutine. |

---

## How the Three Layers Stack Up

They are not three competing options; they are three nested levels of the same system:

```
┌───────────────────────────────────────────────────────────────────────┐
│ PROCESS  com.example.composelearning   (own heap, own ART, own UID)   │
│                                                                       │
│  ┌─────────────────────────┐  ┌──────────────────┐  ┌──────────────┐  │
│  │ main thread (Looper)    │  │ Default pool     │  │ IO pool      │  │
│  │  ┌────┐ ┌────┐ ┌────┐   │  │  ┌────┐ ┌────┐   │  │ ┌────┐ …     │  │
│  │  │ co │ │ co │ │ co │   │  │  │ co │ │ co │   │  │ │ co │       │  │
│  │  └────┘ └────┘ └────┘   │  │  └────┘ └────┘   │  │ └────┘       │  │
│  │  UI state, recomposition│  │ parse, sort,     │  │ network,     │  │
│  │                         │  │ decode           │  │ disk, DB     │  │
│  └─────────────────────────┘  └──────────────────┘  └──────────────┘  │
│                                                                       │
│  Threads share this process's heap. Coroutines share their thread.    │
└───────────────────────────────────────────────────────────────────────┘
            ▲  Binder IPC (Parcel, ~1 MB transaction budget)
            ▼
┌───────────────────────────────────────────────────────────────────────┐
│ PROCESS  com.example.composelearning:sync   (isolated heap and crash) │
└───────────────────────────────────────────────────────────────────────┘
```

- A **process** holds threads. Isolation boundary. Expensive — measured in tens of MB.
- A **thread** executes instructions. Parallelism boundary. Moderately expensive — measured
  in MB of stack and microseconds of switching.
- A **coroutine** is scheduled onto a thread. Concurrency boundary. Nearly free — measured in
  bytes and nanoseconds.

The efficiency argument in one line: **coroutines let you express thousands of concurrent
operations at the cost of a handful of threads inside a single process.**

---

## Choosing the Right Tool

| Need | Use | Not |
|---|---|---|
| Network call, database read, file I/O | `suspend` function with `withContext(Dispatchers.IO)` | A raw `Thread`, or main-thread I/O |
| JSON parsing, sorting, image decoding | `withContext(Dispatchers.Default)` | `Dispatchers.IO` (oversubscribes cores) |
| Update UI state after async work | `Dispatchers.Main` / assign to `StateFlow` | Touching views off the main thread |
| Several independent requests at once | `coroutineScope { async { … } }` + `awaitAll()` | Sequential `await` calls, or a thread per request |
| Strictly serial background queue | Single-threaded dispatcher, or `HandlerThread` | A pool plus locks |
| Observable stream of values | `Flow` / `StateFlow` | A callback registry guarded by `synchronized` |
| Work that must survive process death | `WorkManager` | Any coroutine or thread |
| Long-running playback or sync independent of the UI | Foreground `Service` (same process is usually fine) | An extra process by default |
| Containing native crashes or a huge heap | Separate process via `android:process` | Threads |
| Running untrusted or third-party native code | `android:isolatedProcess` | Same-process execution |

---

## Common Mistakes

1. **Main-thread I/O.** Even a "small" SQLite read or `SharedPreferences.commit()` on the main
   thread can blow the frame budget. Enable `StrictMode` with `detectDiskReads`,
   `detectDiskWrites`, and `detectNetwork` in debug builds and fix every violation.
2. **A thread per item.** Creating a thread inside `onBindViewHolder` or per list item. Use a
   pool, or a coroutine in the item's scope.
3. **`GlobalScope.launch` for screen-scoped work.** It leaks past the screen and keeps doing
   network calls the user will never see. Use `viewModelScope` or `lifecycleScope`.
4. **`Dispatchers.IO` for CPU work.** Up to 64 threads contending for a handful of cores:
   more context switching, less throughput, worse battery.
5. **Blocking calls inside coroutines without `withContext`.** The coroutine machinery cannot
   un-block a blocking call; it can only move it off the thread that matters.
6. **Swallowing `CancellationException`** in a broad `catch (e: Exception)`, which silently
   breaks cancellation for the whole scope.
7. **Adding a process to "make things faster."** A second process makes the app *bigger* and
   more kill-prone. It buys isolation, never speed.
8. **Ignoring per-process initialization.** `Application.onCreate()` runs in every process;
   guard SDK setup with a process-name check.
9. **Unbounded fan-out.** `files.map { async { … } }` over 10,000 files creates 10,000
   coroutines all contending for the same pool. Bound it with a `Semaphore`, `chunked()`, or a
   `Flow` with `flatMapMerge(concurrency = n)`.
10. **Measuring nothing.** Frame drops, thread counts, and process memory are all observable.
    Use Macrobenchmark for startup and frame timing, the Android Studio Profiler for memory
    and threads, Perfetto/systrace for scheduling, and `adb shell dumpsys meminfo <package>`
    for per-process footprint. Optimize what you measured, not what you assumed.

---

## Further Reading

- [Background work overview](https://developer.android.com/guide/background)
- [Processes and app components](https://developer.android.com/guide/components/processes-and-threads)
- [Kotlin coroutines on Android](https://developer.android.com/kotlin/coroutines)
- [Coroutines best practices](https://developer.android.com/kotlin/coroutines/coroutines-best-practices)
- [Kotlin coroutines guide](https://kotlinlang.org/docs/coroutines-guide.html)
- [Overview of memory management](https://developer.android.com/topic/performance/memory-overview)
