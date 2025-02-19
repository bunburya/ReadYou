package me.ash.reader.ui.widget

import android.content.ComponentName
import android.content.Context
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.darkColorScheme
import androidx.compose.material3.lightColorScheme
import androidx.compose.runtime.Composable
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
import androidx.glance.layout.fillMaxWidth
import androidx.glance.layout.height
import androidx.glance.layout.padding
import androidx.glance.layout.width
import androidx.glance.material3.ColorProviders
import androidx.glance.text.FontWeight
import androidx.glance.text.Text
import androidx.glance.text.TextAlign
import androidx.glance.text.TextStyle
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import me.ash.reader.R
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.infrastructure.android.ActionKeys
import me.ash.reader.infrastructure.db.AndroidDatabase
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.page.common.RouteName
import kotlin.math.min


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
                ),
            ) {
                Scaffold(
                    backgroundColor = GlanceTheme.colors.surface,
                    titleBar = {
                        Text(
                            modifier = GlanceModifier
                                .fillMaxWidth()
                                .padding(
                                    start = 40.dp,
                                    top = 4.dp,
                                    bottom = 4.dp
                                ),
                            text = stringResource(R.string.latest),
                            style = TextStyle(
                                color = GlanceTheme.colors.onSurface,
                                textAlign = TextAlign.Left,
                                fontSize = MaterialTheme.typography.titleMedium.fontSize
                            )
                        )
                    }
                ) {
                    SimpleArticleList(count = 20)

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
                .height(80.dp)  // Height must be fixed - variable height leads to "jumpy" scrolling
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
                    style = TextStyle(
                        color = GlanceTheme.colors.onSurface,
                        fontWeight = FontWeight.Bold
                    ),
                    maxLines = 2
                )

                // Description
                if (articleWithFeed.article.shortDescription.isNotBlank()) {
                    Text(
                        modifier = GlanceModifier.padding(top = 4.dp),
                        text = articleWithFeed.article.shortDescription,
                        style = TextStyle(
                            color = GlanceTheme.colors.onSurfaceVariant
                        ),
                        maxLines=2
                    )
                }
            }
        }
    }

    @Composable
    private fun SimpleArticleList(
        modifier: GlanceModifier = GlanceModifier,
        count: Int
    ) {
        val pager = Pager(PagingConfig(pageSize = count)) {
            AndroidDatabase.getInstance(context).articleDao()
                .queryArticleWithFeedWhenIsAll(context.currentAccountId)
        }
        val pagingItems = pager.flow.collectAsLazyPagingItems()

        LazyColumn(modifier) {
            items(min(pagingItems.itemCount, count)) { i ->
                val item = pagingItems[i]
                if (item is ArticleWithFeed) {
                    SimpleArticleItem(
                        item,
                        modifier.clickable(
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