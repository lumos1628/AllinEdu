# State modeling and performance

Covers: keeping pixel-dependent decorative state out of the ViewModel, handling density changes, drawing backgrounds that span `LazyColumn` items, and a performance checklist.

## Keep pixels out of the ViewModel

Creative UI needs values the business layer has no business knowing: a measured `Offset`, an animation progress between 0 and 1, which seasonal effect is active, how tall a connector stem must be. Putting those in the ViewModel couples domain logic to screen density and makes the ViewModel untestable without a device.

Split it in three:

- **ViewModel** — domain data only. Who sent the message, what it says, whether it's loaded.
- **UI state class** (for example `TranscriptState`) — pixel coordinates, animation progress, decorative flags. Created in composition where `Density` is available, remembered across recomposition.
- **Composables** — read the UI state and draw.

```kotlin
class TranscriptState(
    private val density: Density,
    val entries: List<TranscriptEntry>,
) {
    // Pixel geometry derived from density — valid only for this density.
    val stemHeightPx: Float = with(density) { 12.dp.toPx() }
}

@Composable
fun rememberTranscriptState(entries: List<TranscriptEntry>): TranscriptState {
    val density = LocalDensity.current
    // density is a key: a new instance is built when it changes.
    return remember(density, entries) { TranscriptState(density, entries) }
}
```

Passing `density` as a `remember` key is the load-bearing part. Density changes at runtime — unfolding a foldable, moving to an external display, the user changing display size in settings. State holding stale pixel values survives that change and silently renders wrong: stems the wrong length, shapes misaligned. Keying on density discards and rebuilds it, which is correct and cheap.

This also keeps the ViewModel free of `Dp` and `Offset` entirely, so it stays a plain unit-testable class.

## Drawing a background that spans list items

A connector line threading down a list looks like it should be one composable floating above the `LazyColumn`. That approach fights the framework and loses.

**Why the floating overlay fails.** To position an overlay you must read item positions from `LazyListState` during scroll. Those reads happen a composition behind the actual scroll, so the line lags visibly against the items it connects. It also knows nothing about Android's overscroll stretch effect, so the line stays rigid while content stretches — the exact moment the illusion breaks.

**What works.** Draw each item's slice of the connector in that item's own background, via `drawWithCache`. Each item already knows its own height and its neighbours' existence from the data, so it can compute where the line enters and exits. Because the drawing is part of the item, it moves with the item at frame-perfect accuracy and stretches with overscroll for free.

```kotlin
LazyColumn {
    itemsIndexed(entries, key = { _, entry -> entry.id }) { index, entry ->
        TranscriptRow(
            entry = entry,
            modifier = Modifier.drawWithCache {
                val path = Path()
                val strokePx = 2.dp.toPx()
                onDrawBehind {
                    path.rewind()
                    // Connect from this item's anchor to the next, using
                    // index to decide whether to draw the top/bottom segment.
                    drawPath(path, Color.Black, style = Stroke(strokePx))
                }
            }
        )
    }
}
```

The general principle: when decoration must stay synchronized with scrolling content, make it part of the content rather than something tracking it. Anything that *observes* scroll position to position itself will lag by at least a frame.

Always pass a stable `key` to `items` / `itemsIndexed`. Without it, `LazyColumn` reuses slots by index, which scrambles per-item animation state when the list reorders.

## Performance checklist

Run through this before calling a creative component done. Each item corresponds to a real way these effects turn janky.

**Allocation**
- No `Path`, `Paint`, list, or `data class` constructed inside `onDrawBehind`, `Canvas`'s lambda, or a frame loop. Allocate in `drawWithCache` / `remember`.
- `path.rewind()` instead of a new `Path`.
- Compose geometry types (`Offset`, `Size`, `IntOffset`) in hot paths, not custom data classes — they are inline value classes and don't allocate.
- No `map`/`filter`/`sortedBy` inside per-frame code; they build intermediate collections.

**Animation layer**
- Scale, alpha, rotation, and translation go through `graphicsLayer`, not through `size`, `padding`, or `offset` modifiers. The former redraws; the latter re-measures the whole subtree every frame.
- Animated values read as late as possible — inside a lambda like `graphicsLayer { }` or a draw scope — so recomposition is confined rather than propagating up the tree.

**Lists**
- Stable `key` on every lazy item.
- `LaunchedEffect` keyed on item identity, not `Unit`.
- No `LazyListState` reads to position decoration (see above).

**Simulation**
- Frame delta clamped so a stall doesn't teleport everything.
- Particle pool with a hard ceiling; degrade to fewer particles rather than dropping frames.
- Continuous effects scoped to moments, or user-switchable. All-day tools shouldn't run a permanent simulation.

**Verify on a real device**
- Previews don't reveal scrolling, density changes, or frame timing. Build and run.
- Check: longest realistic content, largest accessibility font scale, dark mode, RTL if the app ships it, and a fast scroll through the list.
- If you cannot run the app in this environment, say so plainly instead of reporting the work as verified.

## Where effort actually goes

Expect the breakdown of a polished custom component to be roughly: a quarter on the shape, a quarter on layout and integration, and half on the long tail — long text, tiny screens, huge fonts, dark mode, the seam where two painted layers meet. That tail is why the whimsy budget in `SKILL.md` matters. One component taken all the way through it looks professional; five stopped at the halfway point look unfinished, and cost more in total.
