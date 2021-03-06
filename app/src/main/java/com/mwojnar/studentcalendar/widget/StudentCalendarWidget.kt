package com.mwojnar.studentcalendar.widget

import android.app.PendingIntent
import android.appwidget.AppWidgetManager
import android.appwidget.AppWidgetProvider
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.widget.RemoteViews
import androidx.preference.PreferenceManager
import com.mwojnar.studentcalendar.MainActivity
import com.mwojnar.studentcalendar.R

class StudentCalendarWidget : AppWidgetProvider() {
    override fun onUpdate(context: Context, appWidgetManager: AppWidgetManager, appWidgetIds: IntArray) {
        for (appWidgetId in appWidgetIds) {
            updateAppWidget(context, appWidgetManager, appWidgetId)
        }
    }
}

internal fun updateAppWidget(context: Context, appWidgetManager: AppWidgetManager, appWidgetId: Int) {
    // Go to main activity on click
    val onClickIntent = Intent(context, MainActivity::class.java)
    val onClickPendingIntent = PendingIntent.getActivity(context, appWidgetId, onClickIntent, 0)

    // Setup adapter for widgets list view
    val adapterIntent = Intent(context, StudentCalendarWidgetService::class.java)
    adapterIntent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId)
    adapterIntent.data = Uri.parse(adapterIntent.toUri(Intent.URI_INTENT_SCHEME))

    // Set theme
    val preferences = PreferenceManager.getDefaultSharedPreferences(context)
    val darkTheme = preferences.getBoolean(context.getString(R.string.theme_preference), true)
    val layout = if (darkTheme) { R.layout.student_calendar_widget_night } else { R.layout.student_calendar_widget }

    val views = RemoteViews(context.packageName, layout)
    views.setOnClickPendingIntent(R.id.top_bar, onClickPendingIntent)
    views.setRemoteAdapter(R.id.widget_list_view, adapterIntent)

    // Refresh data on widget update
    appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list_view)
    appWidgetManager.updateAppWidget(appWidgetId, views)
}