# Shapes and layered drawing

Covers: choosing between `GenericShape` and the `Shape` interface, getting `Density` into shape math, layering with `drawWithCache`, clip masks, draw order, and shadows.

## GenericShape: the quick path

`GenericShape` gives you a `Path` receiver plus the actual `size` the composable was measured to. Because the geometry is recomputed whenever the size changes, the shape adapts to its content for free.

```kotlin
import androidx.compose.ui.graphics.GenericShape

val AngledBubble = GenericShape { size, _ ->
    moveTo(0f, 0f)
    lineTo(size.width, 0f)
    lineTo(size.width - 24f, size.height)   // bottom edge pulled in: angled right side
    lineTo(0f, size.height)
    close()
}
```

Apply it like any other `Shape`:

```kotlin
Text(
    text = message,
    modifier = Modifier
        .background(Color(0xFFFFF4D6), shape = AngledBubble)
        .padding(horizontal = 16.dp, vertical = 10.dp)
)
```

**Why this beats a 9-patch.** A 9-patch stretches designated straight regions and leaves the corners alone. That works when only flat segments grow. Here the right edge is *angled*, so as the bubble widens the slope itself has to change — there is no straight region to stretch. Recomputing the path from `size` handles it; a stretched bitmap cannot.

### Padding and shape interact

The shape is applied to the composable's full measured size, including whatever padding sits inside the `.background(...)` call. Order matters: `.background(shape).padding(...)` paints the shape around the padded area, while `.padding(...).background(shape)` insets the shape itself. For angled shapes, add extra padding on the side that slopes inward so text does not collide with the diagonal.

## When GenericShape is not enough: implement Shape

`GenericShape`'s builder hands you `size` and `layoutDirection` — but **not** `Density`. The moment your geometry needs a `Dp` value (and it usually should, so the shape scales correctly across screens), implement `Shape` directly. `createOutline` receives `density`:

```kotlin
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Outline
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.Shape
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.LayoutDirection

class AngledBubbleShape(private val slant: Dp) : Shape {
    override fun createOutline(
        size: Size,
        layoutDirection: LayoutDirection,
        density: Density
    ): Outline {
        val slantPx = with(density) { slant.toPx() }
        val path = Path().apply {
            moveTo(0f, 0f)
            lineTo(size.width, 0f)
            lineTo(size.width - slantPx, size.height)
            lineTo(0f, size.height)
            close()
        }
        return Outline.Generic(path)
    }
}
```

Prefer this form for anything reusable. Hardcoded float pixel values look fine on your test device and wrong everywhere else.

Respect `layoutDirection` when the shape is asymmetric — mirror horizontal math for `LayoutDirection.Rtl`, or the bubble's tail will point the wrong way in Arabic and Hebrew.

`Outline` has three variants: `Outline.Rectangle`, `Outline.Rounded` (for a `RoundRect`), and `Outline.Generic` (arbitrary path). Return the simplest one that fits — rectangle and rounded outlines can be clipped and shadowed more cheaply than a generic path.

## Layered painting with drawWithCache

A single shape handles a fill. Real components stack several passes: fill, border, a connector, a shadow. `drawWithCache` is the right tool — its lambda re-runs only when size or density changes, so you allocate there and draw in `onDrawBehind`.

```kotlin
import androidx.compose.ui.draw.drawWithCache

Modifier.drawWithCache {
    // Runs on size/density change only — safe place to allocate.
    val path = Path()
    val slantPx = 24.dp.toPx()        // CacheDrawScope is a Density
    val strokePx = 2.dp.toPx()

    onDrawBehind {
        // Runs every frame — no allocation here.
        path.rewind()
        path.moveTo(0f, 0f)
        path.lineTo(size.width, 0f)
        path.lineTo(size.width - slantPx, size.height)
        path.lineTo(0f, size.height)
        path.close()

        drawPath(path, Color(0xFFFFF4D6))                       // fill
        drawPath(path, Color.Black, style = Stroke(strokePx))    // border
    }
}
```

`drawBehind` is the simpler sibling — use it when there is nothing to cache. `drawWithCache` is the default for anything with a `Path`, because `path.rewind()` keeps the underlying buffer and avoids per-frame allocation.

`DrawScope` also offers `drawOutline(outline, ...)` if you already have an `Outline` from a `Shape`, which lets one shape definition serve both clipping and painting.

## Clip masks

`Modifier.clip(shape)` masks everything drawn *after* it in the modifier chain. This is how you get an avatar cropped to a custom silhouette while the frame and border around it stay uncropped.

```kotlin
Image(
    painter = avatarPainter,
    contentDescription = "Avatar de $studentName",
    modifier = Modifier
        .size(48.dp)
        .drawBehind { /* outer glow / frame — NOT clipped */ }
        .clip(AngledBubbleShape(slant = 6.dp))   // last: clips only the Image
)
```

Put `clip` immediately before the content it should mask. Placing it earlier in the chain silently clips your decorative layers too, which reads as "my border disappeared".

Clipping is not free; a generic-path clip forces a save layer on some devices. For a plain rounded rectangle, prefer `Modifier.background(color, RoundedCornerShape(...))` over clipping.

## Draw order is the design

When several painted layers meet, order decides whether the seams are invisible. The talk's "stem and box" case: a connector stem joins a message bubble, both have a white fill and a black border. Painting each element's fill *and* border together leaves the stem's border cutting across the bubble's interior.

The fix is to paint by layer, not by element:

1. Connector fill (white)
2. Bubble fill (white)
3. Connector border (black)
4. Bubble border (black)

All fills first, then all borders. The later border strokes sit on top of both fills and the junction reads as one continuous outline. Whenever a custom component looks "almost right but there's a weird line", suspect draw order before suspecting geometry.

The same reasoning applies to entrance animations: if a bubble scales up while its stem is already drawn, interleaved fills and borders will flicker at the seam mid-animation.

## Shadows

`Modifier.shadow(elevation, shape)` works with any `Shape`, including generic outlines, and is the first thing to try.

For soft, offset, or colored shadows that `Modifier.shadow` cannot express, drop to the platform paint. On Android:

```kotlin
onDrawBehind {
    drawIntoCanvas { canvas ->
        val frameworkPaint = paint.asFrameworkPaint()
        frameworkPaint.setShadowLayer(blurPx, 0f, offsetYPx, shadowArgb)
        canvas.nativeCanvas.drawPath(path.asAndroidPath(), frameworkPaint)
    }
}
```

This reaches into the Android framework `Paint`, so it does **not** compile on Desktop or iOS targets, which go through Skia instead. In a Compose Multiplatform project, put this behind an `expect`/`actual` boundary and tell the user you did. Check the project's Gradle files for the multiplatform plugin before assuming Android-only.

Allocate `paint` in the `drawWithCache` block, not in `onDrawBehind` — `setShadowLayer` on a fresh `Paint` every frame is a notable allocation.

## Getting shapes out of Figma

The **Shape Composer** Figma plugin exports vectors as Compose path code. It is a real time-saver for complex silhouettes, but the output has absolute coordinates baked in from one artboard size. Convert those constants to fractions of `size.width` / `size.height`, or to `Dp` values via `Density`, before shipping. An unconverted export is the most common source of "the shape looks wrong on my phone".
