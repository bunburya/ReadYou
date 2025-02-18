package me.ash.reader.ui.widget

import android.content.ComponentName
import android.content.Context
import android.util.Log
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.TextUnit
import androidx.compose.ui.unit.dp
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.GlanceTheme
import androidx.glance.LocalContext
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Alignment
import androidx.glance.layout.Column
import androidx.glance.layout.Row
import androidx.glance.layout.Spacer
import androidx.glance.layout.fillMaxSize
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontFamily
import androidx.glance.text.FontStyle
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextDecoration
import androidx.glance.text.TextStyle
import androidx.glance.unit.ColorProvider
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import me.ash.reader.R
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.infrastructure.android.ActionKeys
import me.ash.reader.infrastructure.db.AndroidDatabase
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.page.common.RouteName
import kotlin.math.absoluteValue
import kotlin.math.min

/**
 * Simple `ColorProvider` implementation that just wraps a `Color` (`glance` has a class that does
 * the same thing but it seems to be restricted).
 */
private class RYColorProvider(private val color: Color): ColorProvider {
    override fun getColor(context: Context) = color
}

private fun androidx.compose.ui.text.font.FontWeight.toGlanceFontWeight(): FontWeight =
    if (this.weight < 450) {
        FontWeight.Normal
    } else if (this.weight < 650) {
        FontWeight.Medium
    } else {
        FontWeight.Bold
    }

private fun androidx.compose.ui.text.font.FontStyle.toGlanceFontStyle(): FontStyle =
    when(this) {
        androidx.compose.ui.text.font.FontStyle.Italic -> FontStyle.Italic
        androidx.compose.ui.text.font.FontStyle.Normal -> FontStyle.Normal
        else -> FontStyle.Normal
    }


private fun androidx.compose.ui.text.style.TextAlign.toGlaceTextAlign(): TextAlign =
    when(this) {
        androidx.compose.ui.text.style.TextAlign.Center -> TextAlign.Center
        androidx.compose.ui.text.style.TextAlign.Start -> TextAlign.Start
        androidx.compose.ui.text.style.TextAlign.End -> TextAlign.End
        androidx.compose.ui.text.style.TextAlign.Left -> TextAlign.Left
        androidx.compose.ui.text.style.TextAlign.Right -> TextAlign.Right
        else -> TextAlign.Start
    }

private fun androidx.compose.ui.text.style.TextDecoration.toGlanceTextDecoration(): TextDecoration =
    when(this) {
        androidx.compose.ui.text.style.TextDecoration.Underline -> TextDecoration.Underline
        androidx.compose.ui.text.style.TextDecoration.LineThrough -> TextDecoration.LineThrough
        androidx.compose.ui.text.style.TextDecoration.None -> TextDecoration.None
        else -> TextDecoration.None
    }

private fun androidx.compose.ui.text.font.FontFamily.toGlanceFontFamily(): FontFamily {
    return when(this) {
        androidx.compose.ui.text.font.FontFamily.Serif -> FontFamily.Serif
        androidx.compose.ui.text.font.FontFamily.SansSerif -> FontFamily.SansSerif
        androidx.compose.ui.text.font.FontFamily.Cursive -> FontFamily.Cursive
        androidx.compose.ui.text.font.FontFamily.Monospace -> FontFamily.Monospace
        else -> FontFamily.SansSerif
    }
}

private fun androidx.compose.ui.text.TextStyle.toGlanceTextStyle(
    color: ColorProvider? = null,
    fontSize: TextUnit? = null,
    fontWeight: FontWeight? = null,
    fontStyle: FontStyle? = null,
    textAlign: TextAlign? = null,
    textDecoration: TextDecoration? = null,
    fontFamily: FontFamily? = null
): TextStyle {
    return TextStyle(
        color = color ?: RYColorProvider(this.color),
        fontSize = fontSize ?: this.fontSize,
        fontWeight = fontWeight ?: this.fontWeight?.toGlanceFontWeight(),
        fontStyle = fontStyle ?: this.fontStyle?.toGlanceFontStyle(),
        textAlign = textAlign ?: this.textAlign.toGlaceTextAlign(),
        textDecoration = textDecoration ?: this.textDecoration?.toGlanceTextDecoration(),
        fontFamily = fontFamily ?: this.fontFamily?.toGlanceFontFamily()
    )
}

class LatestArticlesWidget: GlanceAppWidget() {
    private lateinit var context: Context

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        this.context = context

        provideContent {
            GlanceTheme(
                //colors = ColorProviders(
                //    light = dynamicLightColorScheme(),
                //    dark = dynamicDarkColorScheme()
                //)
                colors = ColorProviders(
                    light = lightColorScheme(),
                    dark = darkColorScheme()
                )
            ) {
                Log.d("LatestArticlesWidget", "colors: ${GlanceTheme.colors}")
                Scaffold(
                    titleBar = {
                        val titleStyle = MaterialTheme.typography.titleLarge.toGlanceTextStyle(
                            textAlign = TextAlign.Center
                        )
                        Log.d("Scaffold", "titleStyle: $titleStyle")
                        Text(
                            text = stringResource(R.string.latest),
                            style = titleStyle
                        )
                    }
                ) {
                    SimpleArticleList(20)

                }
            }
        }
    }

    @Composable
    private fun SimpleArticleItem(
        articleWithFeed: ArticleWithFeed,
        modifier: GlanceModifier
    ) {
        Row(
            modifier = modifier
                .fillMaxWidth()
                .padding(top = 4.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            // Feed icon
            articleWithFeed.feed.icon?.let {
                FeedIcon(feedName = articleWithFeed.feed.name, iconUrl = it)
                Spacer(modifier = GlanceModifier.width(10.dp))
            }

            // Article
            Column(
                modifier = GlanceModifier.defaultWeight()
            ) {
                // Title
                Text(
                    text = articleWithFeed.article.title,
                    style = MaterialTheme.typography.titleMedium.toGlanceTextStyle(
                        color = RYColorProvider(MaterialTheme.colorScheme.onSurface),
                    ),
                    maxLines = 2
                )

                // Description
                if (articleWithFeed.article.shortDescription.isNotBlank()) {
                    Text(
                        modifier = GlanceModifier.padding(top = 4.dp),
                        text = articleWithFeed.article.shortDescription,
                        style = MaterialTheme.typography.bodySmall.toGlanceTextStyle(
                            color = RYColorProvider(MaterialTheme.colorScheme.onSurfaceVariant)
                        ),
                        maxLines=2
                    )
                }
            }
        }
    }

    @Composable
    private fun SimpleArticleList(
        count: Int
    ) {
        val pager = Pager(PagingConfig(pageSize = count)) {
            AndroidDatabase.getInstance(context).articleDao()
                .queryArticleWithFeedWhenIsAll(context.currentAccountId)
        }
        val pagingItems = pager.flow.collectAsLazyPagingItems()

        LazyColumn(
            modifier = GlanceModifier.fillMaxSize()
        ) {
            items(min(pagingItems.itemCount, count)) { i ->
                val item = pagingItems[i]
                if (item is ArticleWithFeed) {
                    SimpleArticleItem(
                        item,
                        GlanceModifier.clickable(
                            actionStartActivity(
                                ComponentName(
                                    "me.ash.reader",
                                    "me.ash.reader.infrastructure.android.MainActivity"
                                ),
                                parameters = actionParametersOf(
                                    ActionKeys.NAV_ROUTE to "${RouteName.READING}/${item.article.id}"
                                )
                            )
                        )
                    )

                }
            }
        }
    }
}

@Composable
private fun stringResource(id: Int): String = LocalContext.current.getString(id)
@Composable
private fun stringResource(id: Int, vararg formatArgs: Any) =
    LocalContext.current.getString(id, *formatArgs)