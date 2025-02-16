package me.ash.reader.ui.widget

import androidx.glance.appwidget.GlanceAppWidgetReceiver

class LatestArticlesWidgetReceiver: GlanceAppWidgetReceiver() {
    override val glanceAppWidget = LatestArticlesWidget()
}