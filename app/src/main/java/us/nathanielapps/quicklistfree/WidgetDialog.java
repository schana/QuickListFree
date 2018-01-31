package us.nathanielapps.quicklistfree;

import android.app.Activity;
import android.app.DialogFragment;
import android.os.Bundle;

public class WidgetDialog extends Activity {
    public static final String TYPE = "DialogType";

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        showNewDialog(this.getIntent().getIntExtra(TYPE, -1));
    }

    private void showNewDialog(int type) {
        // Create and show the dialog.
        if(getFragmentManager().findFragmentByTag("dialog") != null
                && getFragmentManager().findFragmentByTag("dialog").isVisible())
            finish();
        DialogFragment newFragment = WidgetDialogFragment.newInstance(type);
        newFragment.show(getFragmentManager(), "dialog");
    }

}
