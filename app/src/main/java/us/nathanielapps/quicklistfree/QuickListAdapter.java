package us.nathanielapps.quicklistfree;

import android.content.Context;
import android.database.Cursor;
import android.graphics.Color;
import android.graphics.Paint;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.CursorAdapter;
import android.widget.TextView;
import us.nathanielapps.quicklistfree.R;

public class QuickListAdapter extends CursorAdapter {
    private LayoutInflater inflater;

    public QuickListAdapter(Context context, Cursor c, int flags) {
        super(context, c, flags);
        inflater = LayoutInflater.from(context);
    }

    @Override
    public void bindView(View view, Context context, Cursor cursor) {
        String name = cursor.getString(cursor.getColumnIndexOrThrow(Data.NAME));
        int value = cursor.getInt(cursor.getColumnIndexOrThrow(Data.VALUE));
        TextView text = (TextView) view.findViewById(R.id.text);
        text.setText(name);
        if (value == 1) { // checked
            text.setPaintFlags(text.getPaintFlags() | Paint.STRIKE_THRU_TEXT_FLAG);
            text.setTextColor(Color.DKGRAY);
        }else{
            text.setPaintFlags(0);
            text.setTextColor(Color.WHITE);
        }
    }

    @Override
    public View newView(Context context, Cursor cursor, ViewGroup parent) {
        View v = inflater.inflate(R.layout.list_item, parent, false);
        return v;
    }

}
