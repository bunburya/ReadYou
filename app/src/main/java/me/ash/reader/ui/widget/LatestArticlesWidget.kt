package me.ash.reader.ui.widget

import android.content.ComponentName
import android.content.Context
import androidx.compose.runtime.Composable
import androidx.glance.GlanceId
import androidx.glance.GlanceModifier
import androidx.glance.action.actionParametersOf
import androidx.glance.action.actionStartActivity
import androidx.glance.action.clickable
import androidx.glance.appwidget.GlanceAppWidget
import androidx.glance.appwidget.components.Scaffold
import androidx.glance.appwidget.lazy.LazyColumn
import androidx.glance.appwidget.provideContent
import androidx.glance.layout.Row
import androidx.glance.layout.fillMaxSize
import androidx.glance.text.Text
import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.compose.collectAsLazyPagingItems
import me.ash.reader.domain.model.article.ArticleWithFeed
import me.ash.reader.infrastructure.android.ActionKeys
import me.ash.reader.infrastructure.db.AndroidDatabase
import me.ash.reader.ui.ext.currentAccountId
import me.ash.reader.ui.page.common.RouteName
import kotlin.math.min

class LatestArticlesWidget: GlanceAppWidget() {

    override suspend fun provideGlance(context: Context, id: GlanceId) {
        provideContent {
            Scaffold(titleBar = { Text("Latest Articles") }) {
                SimpleArticleList(context, 20)

            }
        }
    }

    @Composable
    private fun SimpleArticleItem(
        article: ArticleWithFeed,
        modifier: GlanceModifier
    ) {
        Row {
            Text(
                text = article.article.title,
                modifier = modifier
            )
        }
    }

    @Composable
    private fun SimpleArticleList(
        context: Context,
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