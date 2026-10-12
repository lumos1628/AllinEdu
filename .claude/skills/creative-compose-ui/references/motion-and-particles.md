# Motion and particles

Covers: choreographed entrances with `Animatable`, interpolating positions with `lerp` over `Offset`, the `withFrameMillis` frame loop, and object pooling for particle effects.

## Choose the smallest animation API

| Need | Use |
|---|---|
| One value eases to a new target when state changes | `animateFloatAsState` / `animateDpAsState` |
| Several values driven by one state change | `updateTransition` |
| A *sequence* with precise waits, snaps, and overshoots | `Animatable` inside `LaunchedEffect` |
| Continuous simulation with per-frame physics | `withFrameMillis` loop |

The declarative helpers cover most cases and are much less code. Escalate only when you need control they don't give you.

## Choreographed entrances with Animatable

`Animatable` is the imperative escape hatch. Inside a coroutine you can mix `snapTo`, `delay`, and `animateTo` to express timing that declarative specs can't, which is how UI gets a sense of weight and personality.

```kotlin
import androidx.compose.animation.core.Animatable
import androidx.compose.animation.core.tween

val foregroundScale = remember { Animatable(0f) }

LaunchedEffect(messageId) {
    foregroundScale.snapTo(0f)              // hidden
    delay(160)                              // beat of anticipation
    foregroundScale.snapTo(0.8f)            // pop in already partly grown
    foregroundScale.animateTo(1f, tween(150))   // settle
}
```

The `snapTo(0.8f)` before animating is the trick worth internalizing: appearing at 80% and growing to 100% feels like something arriving, whereas easing from 0% feels like something inflating. Small discontinuities read as energy.

Key the `LaunchedEffect` on the identity of the thing being animated (`messageId`), not `Unit`, when the composable is reused in a list. Keyed on `Unit` inside a `LazyColumn` item, the animation runs once and then never again for recycled slots — or runs on the wrong item.

### Expose state, hide the machinery

Have the component that owns the `Animatable` expose a read-only `State` so consumers can't accidentally drive the animation:

```kotlin
val scale by foregroundScale.asState()

Box(
    Modifier.graphicsLayer {
        scaleX = scale
        scaleY = scale
    }
)
```

Prefer `graphicsLayer` for scale, alpha, rotation, and translation. It animates on the draw layer, skipping layout and measurement entirely — far cheaper than animating a `size` or `padding` modifier, which forces a relayout every frame.

### Respect reduced-motion preferences

Some users disable animations system-wide. Read `Settings.Global.ANIMATOR_DURATION_SCALE` (or check the platform's accessibility manager) and skip to the final state when animations are off. A delightful entrance that a user has explicitly asked not to see is just a delay.

## Interpolating positions with lerp

For 2D movement, `lerp` over `Offset` handles both axes in one call:

```kotlin
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.lerp

val position = lerp(startOffset, endOffset, progress)
```

`Offset` is a Kotlin inline value class: two 32-bit floats packed into a single 64-bit `Long` at runtime. Passing offsets through an animation loop therefore allocates nothing on the heap, which is exactly what you want at 60 or 120 frames per second. Reach for Compose's geometry types (`Offset`, `Size`, `IntOffset`) over your own `data class Point(val x: Float, val y: Float)` in any per-frame code path — the data class allocates on every frame and hands the garbage collector work that shows up as stutter.

`lerp` overloads exist for `Color`, `Dp`, `Size`, `Rect`, and `IntOffset` too.

## The frame loop

For continuous simulation — falling petals, snow, drifting confetti — drive updates from the frame clock rather than a timer. `withFrameMillis` suspends until the next frame, so your simulation stays in lockstep with the display and never computes frames nobody sees. It also pauses automatically when the composition leaves the screen.

The structure is a game loop: **input → update → render**.

```kotlin
import androidx.compose.runtime.withFrameMillis

@Composable
fun PetalField(modifier: Modifier = Modifier) {
    val particles = remember { ParticleSystem(maxParticles = 60) }
    var frameTick by remember { mutableIntStateOf(0) }

    LaunchedEffect(Unit) {
        var lastFrame = withFrameMillis { it }
        while (true) {
            withFrameMillis { now ->
                val deltaMs = (now - lastFrame).coerceAtMost(64L)   // clamp after a stall
                lastFrame = now
                particles.update(deltaMs)
                frameTick++          // state read by Canvas → triggers redraw
            }
        }
    }

    Canvas(modifier) {
        frameTick    // read the tick so this draw scope re-runs each frame
        particles.forEachActive { p ->
            drawCircle(p.color, radius = p.radius, center = p.position)
        }
    }
}
```

Two details carry real weight:

**Clamp the delta.** After a GC pause or a backgrounded app, `now - lastFrame` can be hundreds of milliseconds. Feeding that into physics teleports every particle across the screen. Clamping to roughly four frames keeps the simulation stable.

**Invalidate deliberately.** The particle list is intentionally *not* snapshot state — making 60 particles individually observable would cost more than the drawing. Instead a single `frameTick` is read inside the `Canvas` lambda, which is enough for Compose to know it must redraw. Driving motion with an advancing counter rather than per-object state is the standard pattern here.

## Object pooling

Particles are created and destroyed constantly. Allocating a fresh object per spawn means steady garbage collection pressure, so reuse instead: keep two lists, one of active particles and one of available ones.

```kotlin
class ParticleSystem(maxParticles: Int) {
    private val available = ArrayDeque<Particle>(maxParticles)
    private val active = ArrayList<Particle>(maxParticles)

    init {
        repeat(maxParticles) { available.addLast(Particle()) }
    }

    fun spawn(x: Float) {
        val particle = available.removeLastOrNull() ?: return   // at capacity: skip, don't allocate
        particle.reset(x)
        active.add(particle)
    }

    fun update(deltaMs: Long) {
        val iterator = active.iterator()
        while (iterator.hasNext()) {
            val particle = iterator.next()
            particle.advance(deltaMs)
            if (particle.isDead) {
                iterator.remove()
                available.addLast(particle)      // back to the pool, not to the GC
            }
        }
    }

    inline fun forEachActive(action: (Particle) -> Unit) {
        for (i in active.indices) action(active[i])
    }
}
```

`Particle` holds mutable `var` fields and is reset rather than reconstructed. This is one of the few places where mutable state and index loops are the right call over idiomatic immutable Kotlin — the hot loop runs 60 times a second and `map`/`filter` would allocate an intermediate list every frame.

Returning early when the pool is empty gives a natural ceiling on cost: the effect degrades to "fewer petals" under pressure instead of dropping frames.

## Making motion feel organic

Linear falling looks mechanical. Layer cheap variation:

- **Sinusoidal drift.** Offset x by `amplitude * sin(elapsed * frequency + phase)`, with a per-particle `phase` so they don't sway in unison.
- **Per-particle variation.** Randomize size, speed, rotation, and start time at spawn. Uniform particles read as a pattern.
- **Depth cues.** Smaller, slower, more transparent particles behind larger, faster, opaque ones creates parallax without a second render pass.
- **Rotation tied to drift.** Rotating slightly in the direction of horizontal motion sells the idea of air resistance.

## Keep decorative motion decorative

Ambient effects must never block or obscure interaction. Put the `Canvas` behind content, not above it, and give it `Modifier.fillMaxSize()` inside the same `Box` as the real UI rather than wrapping it. If an effect sits above content, it needs no pointer input and no semantics — otherwise it intercepts touches and confuses screen readers.

Consider battery and thermals too. A permanent full-screen particle system on a tool people use all day is a different proposition from a two-second celebration after a completed task. Scope continuous effects to moments or make them a user preference.
