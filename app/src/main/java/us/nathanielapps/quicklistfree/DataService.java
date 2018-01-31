package us.nathanielapps.quicklistfree;

import android.content.Context;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.RemoteViews;
import android.widget.RemoteViewsService;
import us.nathanielapps.quicklistfree.R;

public class DataService extends RemoteViewsService {

    @Override
    public RemoteViewsFactory onGetViewFactory(Intent intent) {
        return new ListRemoteViewsFactory(this.getApplicationContext(), intent);
    }
}

class ListRemoteViewsFactory implements RemoteViewsService.RemoteViewsFactory {
    private Context           context;
    private Data              data;
    private SharedPreferences sp;
    private Cursor            c;

    public ListRemoteViewsFactory(Context context, Intent intent) {
        //Log.i("RemoteViewsService", "constructor");
        this.context = context;
        sp = context.getSharedPreferences(List.PREFS, 0);
    }

    @Override
    public void onCreate() {
        //Log.i("RemoteViewsService", "onCreate");
        data = new Data(context);
    }

    @Override
    public void onDataSetChanged() {
        //Log.i("RemoteViewsService", "onDataSetChanged");
        int curList = sp.getInt(List.PREFS_LIST_CURR, -1);
        int order = sp.getInt(List.PREFS_LIST_ORDER, Data.ORDER_TIME_ADDED);
        if (curList > -1) {
            c = data.getList(curList, order);
        } else {
            c = null;
        }

    }

    @Override
    public int getCount() {
        if (c == null)
            return 0;
        else
            return c.getCount();
    }

    @Override
    public long getItemId(int position) {
        if (!c.moveToPosition(position))
            return 0;

        return c.getInt(c.getColumnIndexOrThrow(Data.ID));
    }

    @Override
    public RemoteViews getLoadingView() {
        return null;
    }

    @Override
    public RemoteViews getViewAt(int position) {
        //Log.i("getViewAt", Integer.toString(position));
        if (!c.moveToPosition(position))
            return null;
        RemoteViews rv = new RemoteViews(context.getPackageName(), R.layout.widget_item); // may be able to specify
                                                                                        // different layout for checked
        rv.setTextViewText(R.id.text, c.getString(c.getColumnIndexOrThrow(Data.NAME)));
        int value = c.getInt(c.getColumnIndexOrThrow(Data.VALUE));
        if (value == 1) // checked - not sure how to do crossed out text, so relying on color for now
            rv.setTextColor(R.id.text, Color.DKGRAY);
        else
            rv.setTextColor(R.id.text, Color.WHITE);

        // build click listener to check item
        Bundle extras = new Bundle();
        // extras.putInt(QuickAppWidgetProvider.CLICK, position);
        extras.putInt(Data.ID, c.getInt(c.getColumnIndexOrThrow(Data.ID)));
        extras.putInt(Data.VALUE, c.getInt(c.getColumnIndexOrThrow(Data.VALUE)));
        Intent fillInIntent = new Intent();
        fillInIntent.putExtras(extras);
        rv.setOnClickFillInIntent(R.id.text, fillInIntent);

        return rv;
    }

    @Override
    public int getViewTypeCount() {
        return 1;
    }

    @Override
    public boolean hasStableIds() {
        return true;
    }

    @Override
    public void onDestroy() {
        // TODO Auto-generated method stub

    }

}
