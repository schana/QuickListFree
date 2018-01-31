package us.nathanielapps.quicklistfree;

import android.annotation.TargetApi;
import android.app.PendingIntent;
import android.appwidget.AppWidgetManager;
import android.appwidget.AppWidgetProvider;
import android.content.Context;
import android.content.Intent;
import android.net.Uri;
import android.os.Build;
import android.os.Bundle;
import android.util.DisplayMetrics;
import android.view.View;
import android.view.WindowManager;
import android.widget.RemoteViews;
import us.nathanielapps.quicklistfree.R;

public class QuickAppWidgetProvider extends AppWidgetProvider {
    // public static final String CLICK = "us.nathanielapps.quicklistfree.CLICK";
    public static final String CLICK_ACTION = "us.nathanielapps.quicklistfree.CLICK_ACTION";

    @Override
    public void onReceive(Context context, Intent intent) {
        // Log.i("widget","onReceive");
        AppWidgetManager mgr = AppWidgetManager.getInstance(context);
        if (intent.getAction().equals(CLICK_ACTION)) {
            int appWidgetId = intent.getIntExtra(AppWidgetManager.EXTRA_APPWIDGET_ID,
                    AppWidgetManager.INVALID_APPWIDGET_ID);
            // int viewIndex = intent.getIntExtra(CLICK, 0);
            int id = intent.getIntExtra(Data.ID, -1);
            int value = intent.getIntExtra(Data.VALUE, 0);
            if (value == 0)
                value = 1;
            else
                value = 0;
            new Data(context).click(id, value);
            mgr.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list);
        }

        super.onReceive(context, intent);
    }

    @Override
    public void onUpdate(Context context, AppWidgetManager appWidgetManager, int[] appWidgetIds) {
        // Log.i("widget", "onUpdate()");
        final int N = appWidgetIds.length;
        for (int i = 0; i < N; i++) {
            int appWidgetId = appWidgetIds[i];
            RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);

            // launcher button
            Intent launch_intent = new Intent(context, List.class);
            PendingIntent launch_pendingIntent = PendingIntent.getActivity(context, 0, launch_intent, 0);
            views.setOnClickPendingIntent(R.id.app_launch_button, launch_pendingIntent);

            // new item
            Intent newItemIntent = new Intent(context, WidgetDialog.class);
            newItemIntent.putExtra(WidgetDialog.TYPE, WidgetDialogFragment.NEW_ITEM);
            newItemIntent.setData(Uri.parse(newItemIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent newItemPendingIntent = PendingIntent.getActivity(context, 0, newItemIntent, 0);
            views.setOnClickPendingIntent(R.id.new_item_widget, newItemPendingIntent);

            // sort
            Intent sortListIntent = new Intent(context, WidgetDialog.class);
            sortListIntent.putExtra(WidgetDialog.TYPE, WidgetDialogFragment.SORT_LIST);
            sortListIntent.setData(Uri.parse(sortListIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent sortListPendingIntent = PendingIntent.getActivity(context, 0, sortListIntent, 0);
            views.setOnClickPendingIntent(R.id.sort_widget, sortListPendingIntent);

            // open list
            Intent openListIntent = new Intent(context, WidgetDialog.class);
            openListIntent.putExtra(WidgetDialog.TYPE, WidgetDialogFragment.OPEN_LIST);
            openListIntent.setData(Uri.parse(openListIntent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent openListPendingIntent = PendingIntent.getActivity(context, 0, openListIntent, 0);
            views.setOnClickPendingIntent(R.id.open_widget, openListPendingIntent);

            // set list name
            views.setTextViewText(
                    R.id.widget_title,
                    new Data(context).getListName(context.getSharedPreferences(List.PREFS, 0).getInt(
                            List.PREFS_LIST_CURR, -1)));

            // fill list view with data from remote adapter
            Intent data_intent = new Intent(context, DataService.class);
            data_intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            // When intents are compared, the extras are ignored, so we need to embed the extras
            // into the data so that the extras will not be ignored.
            data_intent.setData(Uri.parse(data_intent.toUri(Intent.URI_INTENT_SCHEME)));

            views.setRemoteAdapter(R.id.widget_list, data_intent);

            views.setEmptyView(R.id.widget_list, R.id.empty_view);

            // set up pending intent template for individual items
            Intent click_intent = new Intent(context, QuickAppWidgetProvider.class);
            click_intent.setAction(CLICK_ACTION);
            click_intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_ID, appWidgetId);
            click_intent.setData(Uri.parse(click_intent.toUri(Intent.URI_INTENT_SCHEME)));
            PendingIntent clickPendingIntent = PendingIntent.getBroadcast(context, 0, click_intent,
                    PendingIntent.FLAG_UPDATE_CURRENT);
            views.setPendingIntentTemplate(R.id.widget_list, clickPendingIntent);

            appWidgetManager.notifyAppWidgetViewDataChanged(appWidgetId, R.id.widget_list);
            appWidgetManager.updateAppWidget(appWidgetId, views);

        }
        super.onUpdate(context, appWidgetManager, appWidgetIds);
    }

    @TargetApi(Build.VERSION_CODES.JELLY_BEAN)
    @Override
    public void onAppWidgetOptionsChanged(Context context, AppWidgetManager appWidgetManager, int appWidgetId,
            Bundle newOptions) {
        // get new size and update accordingly - only for 4.1 & higher
        WindowManager wm = (WindowManager) context.getSystemService(Context.WINDOW_SERVICE);
        DisplayMetrics metrics = new DisplayMetrics();
        wm.getDefaultDisplay().getMetrics(metrics);
        float display_width = metrics.widthPixels / context.getResources().getDisplayMetrics().density;
        float display_height = metrics.heightPixels / context.getResources().getDisplayMetrics().density;
        float width;
        if (display_width > display_height) {
            width = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MAX_WIDTH)
                    / context.getResources().getDisplayMetrics().density;
        } else {
            width = newOptions.getInt(AppWidgetManager.OPTION_APPWIDGET_MIN_WIDTH)
                    / context.getResources().getDisplayMetrics().density;
        }

        // Log.i("widget", Integer.toString((int) width) + " " + Integer.toString((int) display_width));

        RemoteViews views = new RemoteViews(context.getPackageName(), R.layout.appwidget);
        views.setViewVisibility(R.id.sort_widget, View.VISIBLE);
        views.setViewVisibility(R.id.open_widget, View.VISIBLE);
        views.setViewVisibility(R.id.app_launch_button, View.VISIBLE);
        if (width < (display_width * 4 / 7)) {
            // views.setViewVisibility(R.id.sort_widget, View.GONE);
            // views.setViewVisibility(R.id.open_widget, View.GONE);
        }
        if (width < (display_width * 1 / 4)) {
            // views.setViewVisibility(R.id.app_launch_button, View.GONE);
        }

        appWidgetManager.updateAppWidget(appWidgetId, views);
    }
}
