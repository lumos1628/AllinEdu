---
name: creative-compose-ui
description: Design and build expressive, non-generic Jetpack Compose UIs using custom shapes (GenericShape and the Shape interface), custom Layout measure-and-place, layered drawBehind/drawWithCache painting, clip masks, coroutine-driven animation sequences, and Canvas frame-loop/particle effects. Use this skill whenever the work touches how a Compose screen looks or feels: asked to make a screen prettier, more modern, less generic, "with more personality", to add animation, micro-interactions, custom backgrounds, speech bubbles, badges, connector lines, non-rectangular cards, or decorative and seasonal effects. Also use it when Row/Column/Box cannot express a layout (overlap, anchoring, irregular grids), when reviewing a Compose screen that looks like default Material boilerplate, and when a Figma design needs shapes that Material components do not provide. Reach for it even if the user never says the words "custom shape" or "canvas" — a request like "this looks boring" is this skill's job.
---

# Creative UIs with Compose

Based on techniques from Chris Horner's "Creative UIs with Compose" (Cash App, Design Systems).

## Why this skill exists

Mobile UI has converged. Open Spotify, then a podcast app, then a banking app: rounded rectangle, 16dp padding, elevated card, repeat. It is usable and it is forgettable. Earlier eras — HTC Sense widgets, Winamp skins — had personality, and games never gave it up: *Persona 5* and *Metaphor: ReFantazio* break the 2D/3D boundary and push manga and punk-rock styling into plain menu screens, because entertainment is the point.

Compose can do all of that. The drawing and layout primitives are low-level and composable, so an angled speech bubble that reflows with its text, or a connector line threading through a scrolling list, is a few dozen lines — not a custom View. Most Compose code never reaches for them, so screens default to the generic look.

Your job with this skill is to add character **without** breaking usability, accessibility, or frame rate. Those three are what make "creative" different from "broken".

## Start here: spend a whimsy budget, don't carpet-bomb

The common failure is treating every surface as a canvas. Every element gets a custom shape, three gradients, and a spring animation, and the result is noisy, slow, and hard to maintain. The director of *Metaphor: ReFantazio* put the cost plainly: making beautiful menus is genuinely annoying work. Treat that effort as a budget to allocate, not a style to apply everywhere.

Before writing code, decide:

1. **What is the one hero moment?** The screen's emotional center — the message bubble, the streak counter, the empty state, the success confirmation. Spend the budget there.
2. **What stays calm?** Everything else should remain quiet, conventional, and legible so the hero moment reads as intentional rather than as one more loud thing.
3. **What is the app's voice?** A teacher's attendance tool and a music app want different kinds of character. Playful is not automatically right; "crisp and confident" is also a voice. Look at the product's domain and existing palette before inventing one.

State this plan in a sentence or two before implementing, so the user can redirect cheaply. "I'll make the message bubble the hero — angled shape with an overlapping avatar — and leave the list chrome standard" is a much better opening than a wall of code.

Prefer deepening one idea over scattering five. A single well-made custom component with a considered entrance animation beats five half-finished flourishes.

## Choosing a technique

Match the problem to the smallest primitive that solves it. Reaching straight for `Canvas` when a `Shape` would do makes code harder to maintain and often worse for accessibility, because content drawn into a canvas is invisible to the semantics tree.

| The problem | Reach for | Notes |
|---|---|---|
| Non-rectangular background that must adapt to content size | `GenericShape`, or the `Shape` interface when you need `Dp` → px conversion | See `references/shapes-and-drawing.md` |
| Several stacked shapes: fill, border, shadow, connector | `drawWithCache` + `onDrawBehind`, layered in strict order | Draw order is the whole game; same reference |
| Content (image, avatar) masked to a shape | `Modifier.clip(shape)` as the **last** modifier before the content | Same reference |
| Overlap, anchoring, or measurement rules `Row`/`Column`/`Box` can't express | Custom `Layout` | See `references/custom-layout.md` |
| Choreographed entrance: pop, settle, stagger | `Animatable` driven imperatively inside `LaunchedEffect` | See `references/motion-and-particles.md` |
| Position interpolation in 2D | `lerp` over `Offset` | Same reference |
| Continuous simulation: falling petals, snow, confetti, ambient motion | `Canvas` + `withFrameMillis` loop + object pooling | Same reference |
| Decorative state that depends on screen density or measured pixels | A dedicated UI state class, **not** the ViewModel | See `references/state-and-performance.md` |

If the answer is "a standard Material component with a better color and spacing", do that instead. Restraint is a legitimate output of this skill.

## Workflow

Build in this order. Each phase produces something you can look at, which keeps the work honest and makes problems cheap to find.

**Phase 1 — Static shape.** Get the shape right at one fixed size, in a `@Preview`. Hardcode dimensions. Confirm the geometry before anything moves or reflows.

**Phase 2 — Make it size-dependent.** Replace hardcoded numbers with the `size` the shape builder receives, and `Dp` values converted through `Density`. Preview it at short, medium, and very long content. This is where 9-patch thinking fails: if a shape's *angle* changes with its width, no amount of stretchable regions will save it — the geometry has to be recomputed.

**Phase 3 — Layout and composition.** If pieces must overlap or anchor to each other, write the custom `Layout` now. Doing it after animation means debugging two unknowns at once.

**Phase 4 — Integrate into the real container.** Drop it into the actual `LazyColumn` / `Scaffold` / navigation graph. Scrolling containers surface problems previews never show: reused items, items drawn before their neighbors exist, overscroll. Expect to adjust here.

**Phase 5 — Motion.** Add entrance animation, then ambient effects. Last, because motion on top of unresolved layout hides which one is wrong.

**Phase 6 — Verify for real.** Build and run it. Compose previews lie about scrolling, density changes, and frame timing. Check long text, the system's largest font scale, dark mode, and an actual scroll through the list. If you cannot run the app, say so explicitly rather than reporting the work as done.

Figma can give you a head start: the **Shape Composer** plugin exports vectors to Compose code. Treat that output as a starting point with hardcoded coordinates — Phase 2 still applies, since a static export has no idea how to reflow.

## Non-negotiables

These are the constraints that separate a creative UI from a janky one. They are not style preferences.

### Do not allocate inside draw lambdas

A draw lambda runs on every frame — 60 or 120 times a second. Allocating a `Path`, `Paint`, or list inside it hands the garbage collector steady work, and GC pauses read to the user as stutter in exactly the moments you were trying to make delightful.

Allocate once in `drawWithCache` (or `remember`), then reuse. For paths, call `path.rewind()` to clear the geometry while keeping the buffer:

```kotlin
Modifier.drawWithCache {
    val path = Path()          // allocated once per size change
    onDrawBehind {
        path.rewind()          // reuse, don't reallocate
        path.moveTo(0f, 0f)
        path.lineTo(size.width, 0f)
        // ...
        drawPath(path, color)
    }
}
```

Compose's geometry types help here: `Offset` is an inline value class that packs two 32-bit floats into a single 64-bit `Long` at runtime, so passing offsets around in an animation loop costs no heap allocation. Prefer those types over your own data classes in hot paths.

### Creative must stay accessible

Irregular shapes change *where* things are drawn, never *whether* they can be perceived or reached.

- Content drawn with `Canvas` or `drawBehind` is invisible to screen readers. Decorative effects are fine to leave unlabeled, but anything carrying meaning needs a text equivalent or a real composable with `contentDescription`.
- Respect the system font scale. A shape sized against text must grow with it — this is exactly what Phase 2 protects. Test at the largest accessibility font setting.
- Keep contrast on custom-painted text and borders. A hand-picked palette bypasses the Material color system's contrast guarantees, so check it yourself.
- Keep touch targets at least 48dp even when the visible shape is smaller or angled.

### Adapt, don't assume

Density changes at runtime — foldables, external displays. State holding pixel values must be recreated when density changes rather than carried across. Decorative layouts should degrade gracefully on tablets and in landscape instead of stretching into something no one designed.

### Watch the multiplatform line

Shadow and blur drawing is where platforms diverge: Android reaches into the framework `Paint` and `setShadowLayer`, while Desktop and iOS go through Skia. If the project is Compose Multiplatform, keep platform-specific drawing behind an expect/actual boundary and say so. Check the project's build files before assuming Android-only.

## References

Read the one that matches your current phase rather than all of them.

- `references/shapes-and-drawing.md` — `GenericShape` vs the `Shape` interface, getting `Density` into a shape, layering with `drawWithCache`, clip masks, strict draw order, shadows.
- `references/custom-layout.md` — measure-and-place, destructuring measurables, overlap and anchoring, constraint arithmetic, intrinsics.
- `references/motion-and-particles.md` — imperative `Animatable` sequences, `lerp` over `Offset`, the `withFrameMillis` frame loop, object pooling for particles.
- `references/state-and-performance.md` — separating decorative UI state from the ViewModel, density-dependent state, drawing backgrounds inside `LazyColumn` items, performance checklist.
