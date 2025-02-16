package me.ash.reader.infrastructure.widget

import android.content.Context
import androidx.glance.appwidget.updateAll
import dagger.hilt.android.qualifiers.ApplicationContext
import me.ash.reader.ui.widget.LatestArticlesWidget
import javax.inject.Inject


class WidgetUpdateManager @Inject constructor(
    @ApplicationContext
    private val context: Context,
) {

    suspend fun update() {
        LatestArticlesWidget().updateAll(context)
    }
}