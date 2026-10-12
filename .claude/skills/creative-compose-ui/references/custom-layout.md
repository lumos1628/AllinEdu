# Custom Layout: measure and place

Covers: when to leave `Row`/`Column`/`Box` behind, the `Layout` contract, destructuring measurables, overlap arithmetic, and the pitfalls that produce crashes or wrong sizes.

## When a custom Layout is the right call

`Row`, `Column`, and `Box` cover most arrangements. Write a custom `Layout` when the positioning rule depends on *measured results* in a way those can't express:

- One child must overlap another by a specific amount.
- A child's available width depends on another child's measured width.
- An anchor point moves based on content length (a bubble tail that sits differently for one-line and five-line messages).
- Children need to be positioned on an irregular path, arc, or staggered grid.

`Box` with `offset` modifiers is the tempting shortcut, and it is fine for a fixed nudge. It breaks down when the offset must be computed from a sibling's size, because modifiers can't see siblings. That is the line where a custom `Layout` pays for itself — and it is usually around 15 lines, not a scary rewrite.

## The contract

```kotlin
import androidx.compose.ui.layout.Layout

@Composable
fun BubbleWithAvatar(
    modifier: Modifier = Modifier,
    avatar: @Composable () -> Unit,
    bubble: @Composable () -> Unit,
) {
    Layout(
        modifier = modifier,
        content = {
            avatar()
            bubble()
        }
    ) { measurables, constraints ->
        // 1. measure children
        // 2. declare own size
        // 3. place children
    }
}
```

Three obligations, in order: measure each child exactly once, report your own size via `layout(width, height)`, and place every child inside that `layout` block. Skipping a child means it never appears; measuring one twice throws.

## Destructuring measurables

Kotlin's destructuring works on `List` for up to five elements, which makes fixed-arity layouts read cleanly:

```kotlin
) { measurables, constraints ->
    val (avatarMeasurable, bubbleMeasurable) = measurables
```

This is far more legible than `measurables[0]` and `measurables[1]`, and it documents the expected content order. The tradeoff is that it is positional and unchecked — if a caller passes a different number of children, you get an exception at runtime rather than a compile error.

For layouts whose content is variable or caller-controlled, use layout ids instead:

```kotlin
content = {
    Box(Modifier.layoutId("avatar")) { avatar() }
    Box(Modifier.layoutId("bubble")) { bubble() }
}
// ...
val avatarMeasurable = measurables.first { it.layoutId == "avatar" }
```

Keep destructuring for private, single-purpose components where you control both sides. Use `layoutId` for anything public.

## Overlap arithmetic

The core pattern: measure the first child, then *subtract* the overlap from the constraints you hand the second child, so the pair's total width stays within the parent's limit.

```kotlin
) { measurables, constraints ->
    val (avatarMeasurable, bubbleMeasurable) = measurables

    val avatarPlaceable = avatarMeasurable.measure(Constraints())  // unbounded: let it be its natural size

    val overlapPx = 18.dp.roundToPx()   // MeasureScope is a Density
    val occupiedWidth = avatarPlaceable.width - overlapPx

    val bubblePlaceable = bubbleMeasurable.measure(
        constraints.copy(
            minWidth = 0,
            maxWidth = (constraints.maxWidth - occupiedWidth).coerceAtLeast(0)
        )
    )

    val width = occupiedWidth + bubblePlaceable.width
    val height = maxOf(avatarPlaceable.height, bubblePlaceable.height)

    layout(width, height) {
        avatarPlaceable.place(x = 0, y = 0)
        bubblePlaceable.place(x = occupiedWidth, y = 0)
    }
}
```

Note `minWidth = 0` in the copy. Incoming constraints are often *fixed* (equal min and max) when the parent fills width. Copying only `maxWidth` while leaving a larger `minWidth` produces a child forced wider than the space you just computed, and the layout overflows with no obvious cause. Resetting `minWidth` is the fix almost every time a custom layout mysteriously overflows.

`MeasureScope` implements `Density`, so `dp.roundToPx()` and `dp.toPx()` work directly inside the measure lambda — no `LocalDensity` lookup needed. Prefer `roundToPx()` for positions and sizes, since placement takes integers.

## Vertical anchoring that depends on content

A tail or avatar often needs to align to the *first line* of text for long messages but to the center for short ones. Because you have both measured heights, that rule is a few lines:

```kotlin
val avatarY = if (bubblePlaceable.height > singleLineThresholdPx) {
    0                                                        // anchor to top for tall bubbles
} else {
    (height - avatarPlaceable.height) / 2                    // center for short ones
}
```

This conditional positioning is precisely what `Row`'s fixed `verticalAlignment` cannot do, and it is the kind of detail that makes a custom component feel designed rather than assembled.

## Pitfalls

**Measuring a child twice.** Compose throws `IllegalStateException` on a second `measure` call for the same measurable. If you need a size before deciding constraints, use intrinsics (below) rather than a speculative measure.

**Ignoring incoming constraints.** Passing `Constraints()` (fully unbounded) to a text child lets it grow to a single enormous line. Only use unbounded constraints for children with intrinsic size, like a fixed-size avatar.

**Placing outside your reported size.** Children placed beyond the `width`/`height` you declared are drawn but not clipped and will not receive touch events reliably. Either grow your reported size or clip deliberately.

**Forgetting that `place` is integer pixels.** Fractional offsets need `placeRelative` with an `IntOffset`, or accept the rounding. For animation, animate a draw-layer offset instead of re-laying out every frame — relayout per frame is far more expensive than redraw.

**Hardcoding RTL-wrong positions.** `place` is absolute; `placeRelative` mirrors automatically in RTL layouts. Use `placeRelative` unless you specifically want absolute positioning.

## Intrinsics

When a parent needs a child's size *before* measuring — "how tall is this bubble if it's 200dp wide?" — implement intrinsic measurements by using `MeasurePolicy` instead of the lambda form and overriding `minIntrinsicHeight` / `maxIntrinsicWidth`.

Most creative layouts never need this. Reach for it only when a parent like `Row(Modifier.height(IntrinsicSize.Min))` is involved, and prefer restructuring the layout so the question never comes up — intrinsics add real complexity and a measurement pass.
