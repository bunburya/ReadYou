package me.ash.reader.ui.widget

import android.graphics.Bitmap
import android.graphics.BitmapFactory
import android.util.Base64
import androidx.compose.runtime.Composable
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Canvas
import androidx.compose.ui.graphics.ImageBitmap
import androidx.compose.ui.graphics.asAndroidBitmap
import androidx.compose.ui.graphics.drawscope.CanvasDrawScope
import androidx.compose.ui.graphics.painter.Painter
import androidx.compose.ui.unit.Density
import androidx.compose.ui.unit.LayoutDirection
import androidx.glance.GlanceModifier
import androidx.glance.Image
import androidx.glance.ImageProvider


@Composable
fun Base64Image(
    modifier: GlanceModifier = GlanceModifier,
    base64Uri: String,
    onEmpty: @Composable () -> Unit = {},
) {
    val isSvg = base64Uri.startsWith("image/svg")

    if (isSvg) {
        // not implemented yet
        return
    } else {
        val bytes = base64ToBytes(base64Uri)
        val bitmap = BitmapFactory.decodeByteArray(bytes, 0, bytes.size)

        if (bitmap == null) {
            onEmpty()
        } else {
            Image(
                provider = ImageProvider(bitmap),
                modifier = modifier,
                contentDescription = null
            )
        }
    }
}

private fun base64ToBytes(base64String: String): ByteArray {
    val base64Data = base64String.substringAfter("base64,")
    return Base64.decode(base64Data, Base64.DEFAULT)
}

private fun painterToBitmap(
    painter: Painter,
    density: Density,
    layoutDirection: LayoutDirection,
    size: Size = painter.intrinsicSize
): Bitmap {
    val bitmap = ImageBitmap(size.width.toInt(), size.height.toInt())
    val canvas = Canvas(bitmap)
    CanvasDrawScope().draw(density, layoutDirection, canvas, size) {
        with (painter) {
            draw(size)
        }
    }
    return bitmap.asAndroidBitmap()
}