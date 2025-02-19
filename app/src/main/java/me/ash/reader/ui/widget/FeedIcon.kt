package me.ash.reader.ui.widget

import android.graphics.Bitmap
import android.util.Log
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.core.graphics.drawable.toBitmapOrNull
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.Image
import androidx.glance.ImageProvider
import androidx.glance.LocalContext
import androidx.glance.appwidget.CircularProgressIndicator
import androidx.glance.background
import androidx.glance.layout.Alignment
import androidx.glance.layout.Box
import androidx.glance.layout.size
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextStyle
import coil.ImageLoader
import coil.decode.SvgDecoder
import coil.request.ErrorResult
import coil.request.ImageRequest
import coil.request.SuccessResult
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext

@Composable
fun FeedIcon(
    feedName: String? = "",
    iconUrl: String?,
    size: Dp = 20.dp,
    modifier: GlanceModifier = GlanceModifier
) {
    if (iconUrl.isNullOrEmpty()) {
        FontIcon(feedName ?: "", size, modifier)
    } else if ("^image/.*;base64,.*".toRegex().matches(iconUrl)) {
        Base64Image(
            modifier = modifier.size(size),
            base64Uri = iconUrl,
            onEmpty = { FontIcon(feedName ?: "", size, modifier) }
        )
    } else {
        AsyncImage(
            imageUrl = iconUrl,
            contentDescription = feedName,
            modifier = modifier.size(size)
        )
    }

}

@Composable
private fun FontIcon(
    feedName: String,
    size: Dp,
    modifier: GlanceModifier = GlanceModifier
) {
    Box(
        modifier = modifier
            .size(size)
            .background(GlanceTheme.colors.secondary),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = feedName.ifEmpty { " " }.first().toString(),
            style = TextStyle(
                color = GlanceTheme.colors.onPrimary,
                fontSize = 10.sp,
                fontWeight = FontWeight.Bold
            )
        )

    }
}

@Composable
private fun AsyncImage(
    imageUrl: String,
    contentDescription: String?,
    modifier: GlanceModifier = GlanceModifier
) {
    var context = LocalContext.current
    var loadedBitmap by remember(imageUrl) { mutableStateOf<Bitmap?>(null) }
    LaunchedEffect(imageUrl) {
        withContext(Dispatchers.IO) {
            val request = ImageRequest.Builder(context)
                .data(imageUrl)
                .build()
            val loader = ImageLoader.Builder(context)
                .components {
                    add(SvgDecoder.Factory())
                }
                .build()
            val result = loader.execute(request)
            if (result is ErrorResult) {
                Log.e("AsyncImage", "Error: ${result.throwable}")
            }
            loadedBitmap = when (result) {
                is ErrorResult -> null
                is SuccessResult -> result.drawable.toBitmapOrNull()
            }
        }
    }

    loadedBitmap.let { bitmap ->
        if (bitmap != null) {
            Image(
                provider = ImageProvider(bitmap),
                contentDescription = contentDescription,
                modifier = modifier
            )
        } else {
            CircularProgressIndicator(modifier = modifier)
        }
    }
}

