package us.nathanielapps.quicklistfree;

import android.app.Activity;
import android.app.AlertDialog;
import android.app.Dialog;
import android.app.DialogFragment;
import android.appwidget.AppWidgetManager;
import android.content.ComponentName;
import android.content.DialogInterface;
import android.content.DialogInterface.OnClickListener;
import android.content.Intent;
import android.content.SharedPreferences;
import android.database.Cursor;
import android.os.Bundle;
import android.view.View;
import android.view.WindowManager;
import android.widget.EditText;
import android.widget.Toast;
import us.nathanielapps.quicklistfree.R;

public class WidgetDialogFragment extends DialogFragment {
    public static final int NEW_ITEM  = 0;
    public static final int SORT_LIST = 1;
    public static final int OPEN_LIST = 2;
    public static final int EDIT_ITEM = 3;
    public static final int NEW_LIST  = 4;
    int                     mNum;

    /**
     * Create a new instance of MyDialogFragment, providing "num" as an argument.
     */
    static WidgetDialogFragment newInstance(int num) {
        WidgetDialogFragment f = new WidgetDialogFragment();
        f.getId();
        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        f.setArguments(args);

        return f;
    }

    static WidgetDialogFragment newInstance(int num, int item_id, String name) {
        WidgetDialogFragment f = new WidgetDialogFragment();

        // Supply num input as an argument.
        Bundle args = new Bundle();
        args.putInt("num", num);
        args.putInt(Data.ID, item_id);
        args.putString(Data.NAME, name);
        f.setArguments(args);

        return f;
    }

    @Override
    public void onDismiss(DialogInterface di) {
        super.onDismiss(di);
        if (getActivity() instanceof WidgetDialog)
            getActivity().finish();
    }

    @Override
    public Dialog onCreateDialog(Bundle savedInstanceState) {
        final Data data = new Data(getActivity());
        final SharedPreferences sp = getActivity().getSharedPreferences(List.PREFS, 0);
        mNum = getArguments().getInt("num");

        AlertDialog.Builder builder;
        Dialog dialog;

        switch (mNum) {
        case NEW_ITEM:
            builder = new AlertDialog.Builder(getActivity());

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            final View dialog_view = getActivity().getLayoutInflater().inflate(R.layout.dialog_input, null);

            builder.setView(dialog_view);
            final EditText edit = (EditText) dialog_view.findViewById(R.id.dialog_text);

            builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    int listid = sp.getInt(List.PREFS_LIST_CURR, -1);
                    if (listid == -1) {
                        // error
                        Toast.makeText(getActivity(), "No list selected", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        return;
                    }
                    String newItem = edit.getText().toString();
                    if (newItem.length() < 1) {
                        // error
                        Toast.makeText(getActivity(), "Length must be at least 1", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        return;
                    }
                    data.addItem(listid, newItem);

                    // update lists ui
                    Activity a = getActivity();
                    // update currently running activity
                    if (a instanceof List)
                        ((List) a).fillData();

                    // update appwidgets
                    Intent intent = new Intent(getActivity(), QuickAppWidgetProvider.class);
                    intent.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    int widgetIds[] = AppWidgetManager.getInstance(getActivity()).getAppWidgetIds(
                            new ComponentName(getActivity(), QuickAppWidgetProvider.class));
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
                    getActivity().sendBroadcast(intent);
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            builder.setTitle("Add item");

            dialog = builder.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            return dialog;
        case SORT_LIST:
            builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Sort method");
            builder.setItems(R.array.orders, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    SharedPreferences.Editor edit = sp.edit();
                    edit.putInt(List.PREFS_LIST_ORDER, which);
                    edit.commit();
                    // TODO: consolidate duplicate code
                    // update lists ui
                    Activity a = getActivity();
                    // update currently running activity
                    if (a instanceof List)
                        ((List) a).fillData();

                    // update appwidgets
                    Intent intent = new Intent(getActivity(), QuickAppWidgetProvider.class);
                    intent.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    int widgetIds[] = AppWidgetManager.getInstance(getActivity()).getAppWidgetIds(
                            new ComponentName(getActivity(), QuickAppWidgetProvider.class));
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
                    getActivity().sendBroadcast(intent);
                }
            });
            dialog = builder.create();
            return dialog;
        case OPEN_LIST:
            builder = new AlertDialog.Builder(getActivity());
            builder.setTitle("Pick a list");
            final Cursor c = data.getLists();
            builder.setCursor(c, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    c.moveToPosition(which);
                    int list_id = c.getInt(c.getColumnIndexOrThrow(Data.ID));
                    SharedPreferences.Editor edit = sp.edit();
                    edit.putInt(List.PREFS_LIST_CURR, list_id);
                    edit.commit();

                    // TODO: consolidate duplicate code
                    // update lists ui
                    Activity a = getActivity();
                    // update currently running activity
                    if (a instanceof List)
                        ((List) a).fillData();

                    // update appwidgets
                    Intent intent = new Intent(getActivity(), QuickAppWidgetProvider.class);
                    intent.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    int widgetIds[] = AppWidgetManager.getInstance(getActivity()).getAppWidgetIds(
                            new ComponentName(getActivity(), QuickAppWidgetProvider.class));
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
                    getActivity().sendBroadcast(intent);
                }
            }, Data.NAME);
            dialog = builder.create();
            return builder.create();
        case EDIT_ITEM:
            final int item_id = getArguments().getInt(Data.ID);
            String name = getArguments().getString(Data.NAME);

            builder = new AlertDialog.Builder(getActivity());

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            final View edit_view = getActivity().getLayoutInflater().inflate(R.layout.dialog_input, null);

            builder.setView(edit_view);
            final EditText edit_item = (EditText) edit_view.findViewById(R.id.dialog_text);
            edit_item.setText(name);

            builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    String newItem = edit_item.getText().toString();
                    if (newItem.length() < 1) {
                        // error
                        Toast.makeText(getActivity(), "Length must be at least 1", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                    }
                    data.editItem(item_id, newItem);

                    // update lists ui
                    Activity a = getActivity();
                    // update currently running activity
                    if (a instanceof List)
                        ((List) a).fillData();

                    // update appwidgets
                    Intent intent = new Intent(getActivity(), QuickAppWidgetProvider.class);
                    intent.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    int widgetIds[] = AppWidgetManager.getInstance(getActivity()).getAppWidgetIds(
                            new ComponentName(getActivity(), QuickAppWidgetProvider.class));
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
                    getActivity().sendBroadcast(intent);
                }
            });
            builder.setNeutralButton(android.R.string.cancel, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            builder.setNegativeButton(R.string.delete, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int which) {
                    data.deleteItem(item_id);

                    // update lists ui
                    Activity a = getActivity();
                    // update currently running activity
                    if (a instanceof List)
                        ((List) a).fillData();

                    // update appwidgets
                    Intent intent = new Intent(getActivity(), QuickAppWidgetProvider.class);
                    intent.setAction(android.appwidget.AppWidgetManager.ACTION_APPWIDGET_UPDATE);
                    int widgetIds[] = AppWidgetManager.getInstance(getActivity()).getAppWidgetIds(
                            new ComponentName(getActivity(), QuickAppWidgetProvider.class));
                    intent.putExtra(AppWidgetManager.EXTRA_APPWIDGET_IDS, widgetIds);
                    getActivity().sendBroadcast(intent);
                }
            });
            builder.setTitle("Edit item");

            dialog = builder.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            return dialog;
        case NEW_LIST:
            builder = new AlertDialog.Builder(getActivity());

            // Inflate and set the layout for the dialog
            // Pass null as the parent view because its going in the dialog layout
            final View new_list_view = getActivity().getLayoutInflater().inflate(R.layout.dialog_input, null);

            builder.setView(new_list_view);
            final EditText list_edit = (EditText) new_list_view.findViewById(R.id.dialog_text);

            builder.setPositiveButton(android.R.string.ok, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    String newList = list_edit.getText().toString();
                    if (newList.length() < 1) {
                        // error
                        Toast.makeText(getActivity(), "Length must be at least 1", Toast.LENGTH_SHORT).show();
                        dialog.cancel();
                        return;
                    }
                    int list_id = data.createList(newList);
                    SharedPreferences.Editor edit = sp.edit();
                    edit.putInt(List.PREFS_LIST_CURR, list_id);
                    edit.putInt(List.PREFS_LIST_COUNT, sp.getInt(List.PREFS_LIST_COUNT, 0) + 1);
                    edit.commit();
                    ((List) getActivity()).fillData();
                }
            });
            builder.setNegativeButton(android.R.string.cancel, new OnClickListener() {
                @Override
                public void onClick(DialogInterface dialog, int id) {
                    dialog.cancel();
                }
            });
            builder.setTitle("New list name");

            dialog = builder.create();
            dialog.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_VISIBLE);
            return dialog;
        }

        return null;
    }
}
