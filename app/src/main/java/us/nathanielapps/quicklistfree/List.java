package us.nathanielapps.quicklistfree;

import android.app.AlertDialog;
import android.app.DialogFragment;
import android.app.Fragment;
import android.app.FragmentManager;
import android.app.FragmentTransaction;
import android.app.ListActivity;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.widget.AdapterView;
import android.widget.AdapterView.OnItemLongClickListener;
import android.widget.ListView;
import android.widget.Toast;
import us.nathanielapps.quicklistfree.R;

public class List extends ListActivity {

    public static final String PREFS            = "QuickListPrefs";
    public static final String PREFS_LIST_COUNT = "LISTCOUNT";
    public static final String PREFS_LIST_CURR  = "LISTCURR";
    public static final String PREFS_LIST_ORDER = "LISTORDER";
    private Data               data;
    private SharedPreferences  sp;
    private QuickListAdapter   adapter;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_list);
        sp = getSharedPreferences(PREFS, 0);
        data = new Data(this);
        getListView().setOnItemLongClickListener(new OnItemLongClickListener() {
            @Override
            public boolean onItemLongClick(AdapterView<?> parent, View view, int position, long id) {
                Cursor c = (Cursor) adapter.getItem(position);
                if (c.moveToPosition(position)) {
                    int item_id = c.getInt(c.getColumnIndexOrThrow(Data.ID));
                    String name = c.getString(c.getColumnIndexOrThrow(Data.NAME));
                    DialogFragment newFragment = WidgetDialogFragment.newInstance(WidgetDialogFragment.EDIT_ITEM,
                            item_id, name);
                    newFragment.show(getFragmentManager(), "dialog");
                }
                return true;
            }
        });
    }

    @Override
    protected void onResume() {
        super.onResume();
        // remove dialogfragment if one exists
        FragmentManager fm = getFragmentManager();
        Fragment frag = fm.findFragmentByTag("dialog");
        if (frag != null) {
            FragmentTransaction transaction = fm.beginTransaction();
            transaction.remove(frag);
            transaction.commit();
        }
        fillData();
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.activity_list, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        int id = item.getItemId();
        if (id == R.id.menu_add_item) {
            addItem();
        } else if (id == R.id.menu_delete_list) {
            deleteList();
        } else if (id == R.id.menu_order_list) {
            orderList();
        } else if (id == R.id.menu_new_list) {
            createList();
        } else if (id == R.id.menu_open_list) {
            openList();
        } else if (id == R.id.menu_check_all) {
            checkAll();
        } else if (id == R.id.menu_uncheck_all) {
            uncheckAll();
        }
        return super.onOptionsItemSelected(item);
    }

    public void fillData() {
        int listCount = sp.getInt(PREFS_LIST_COUNT, 0);
        int curList = sp.getInt(PREFS_LIST_CURR, -1);
        if (listCount == 0 || curList == -1) {
            this.setTitle(getString(R.string.app_name));
            setListAdapter(null);
            if (listCount == 0)
                createList();
            return;
        }

        int order = sp.getInt(PREFS_LIST_ORDER, Data.ORDER_TIME_ADDED);
        Cursor c = data.getList(curList, order);
        if (getListAdapter() == null) {
            adapter = new QuickListAdapter(this, c, 0);
            setListAdapter(adapter);
        } else {
            adapter.swapCursor(c);
        }

        this.setTitle(/* this.getString(R.string.app_name) + " - " + */data.getListName(curList));
    }

    @Override
    public void onListItemClick(ListView l, View v, int position, long id) {
        Cursor c = (Cursor) this.getListAdapter().getItem(position);
        if (c.moveToPosition(position)) {
            if (c.getInt(c.getColumnIndexOrThrow(Data.VALUE)) == 1) { // need to uncheck
                data.click(c.getInt(c.getColumnIndexOrThrow(Data.ID)), 0);
            } else { // need to check
                data.click(c.getInt(c.getColumnIndexOrThrow(Data.ID)), 1);
            }
            fillData();
            // notify appwidget that it needs to update
            Intent intent = new Intent(this, QuickAppWidgetProvider.class);
            intent.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);
            int widgetIds[] = AppWidgetManager.getInstance(this).getAppWidgetIds(
                    new ComponentName(this, QuickAppWidgetProvider.class));
            intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
            sendBroadcast(intent);
        }
    }

    private void checkAll() {
        batchCheck(1);
    }

    private void uncheckAll() {
        batchCheck(0);
    }

    private void batchCheck(int value) {
        Cursor c;
        for (int i = 0; i < this.getListAdapter().getCount(); i++) {
            c = (Cursor) this.getListAdapter().getItem(i);
            data.click(c.getInt(c.getColumnIndexOrThrow(Data.ID)), value);
        }
        fillData();
        // notify appwidget that it needs to update
        Intent intent = new Intent(this, QuickAppWidgetProvider.class);
        intent.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);
        int widgetIds[] = AppWidgetManager.getInstance(this).getAppWidgetIds(
                new ComponentName(this, QuickAppWidgetProvider.class));
        intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
        sendBroadcast(intent);
    }

    private void createList() {
        if (getFragmentManager().findFragmentByTag("dialog") != null
                && getFragmentManager().findFragmentByTag("dialog").isVisible())
            return;
        DialogFragment newFragment = WidgetDialogFragment.newInstance(WidgetDialogFragment.NEW_LIST);
        newFragment.show(getFragmentManager(), "dialog");
    }

    private void addItem() {
        if (getFragmentManager().findFragmentByTag("dialog") != null
                && getFragmentManager().findFragmentByTag("dialog").isVisible())
            return;
        DialogFragment newFragment = WidgetDialogFragment.newInstance(WidgetDialogFragment.NEW_ITEM);
        newFragment.show(getFragmentManager(), "dialog");
    }

    private void openList() {
        if (getFragmentManager().findFragmentByTag("dialog") != null
                && getFragmentManager().findFragmentByTag("dialog").isVisible())
            return;
        DialogFragment newFragment = WidgetDialogFragment.newInstance(WidgetDialogFragment.OPEN_LIST);
        newFragment.show(getFragmentManager(), "dialog");
    }

    private void orderList() {
        if (getFragmentManager().findFragmentByTag("dialog") != null
                && getFragmentManager().findFragmentByTag("dialog").isVisible())
            return;
        DialogFragment newFragment = WidgetDialogFragment.newInstance(WidgetDialogFragment.SORT_LIST);
        newFragment.show(getFragmentManager(), "dialog");
    }

    private void deleteList() {
        final int id = sp.getInt(PREFS_LIST_CURR, -1);
        if (id == -1) {
            Toast.makeText(getApplicationContext(), "No list selected", Toast.LENGTH_SHORT).show();
            return;
        }
        AlertDialog.Builder builder = new AlertDialog.Builder(this);
        builder.setMessage("OK to delete list?");
        builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
            @Override
            public void onClick(DialogInterface arg0, int arg1) {
                data.deleteList(id);
                SharedPreferences.Editor edit = sp.edit();
                edit.putInt(PREFS_LIST_CURR, -1);
                edit.putInt(PREFS_LIST_COUNT, Math.max(0, sp.getInt(PREFS_LIST_COUNT, 0) - 1));
                edit.commit();
                fillData();

            }
        });
        builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
            @Override
            public void onClick(DialogInterface dialog, int arg1) {
                dialog.cancel();
            }
        });
        builder.create().show();
    }
}
