package edu.purdue.PavementPatchingTracker;

/*
 * The activity for grain kart.
 * 
 * @author: Yaguang Zhang
 * 
 */

import android.app.Activity;
import android.app.AlertDialog;
import android.content.DialogInterface;
import android.os.Bundle;
import android.support.v4.app.Fragment;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.Button;
import android.widget.TextView;
import android.widget.Toast;

import java.io.IOException;

public class KartActivity extends BasicGpsLoggingActivity {

    private boolean kartIsUnloading = false;
    private boolean kartDoneUnloading = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setContentView(R.layout.activity_kart);
        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }

    }

    @Override
    protected void onResume() {
        super.onResume();

        LogFileWrite(isLOG_STATE_FLAG(), getmLogFileState(),
                "% Kart state: not unloading (default)\n",
                "KartOnStartWrite");
    }

    @Override
    public String getLoginType() {
        return getString(R.string.vehicle_kart);
    }

    @Override
    public String getPartialLogFilePath() {
        return getString(R.string.gps_log_file_path_kart);
    }

    @Override
    public void setBackgroundColor() {
        findViewById(R.id.textViewVehicleTypeLabel).getRootView()
                .setBackgroundColor(
                        getResources().getColor(
                                MainLoginActivity.COLOR_ACTIVITY_KART));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.kart, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        // Handle action bar item clicks here. The action bar will
        // automatically handle clicks on the Home/Up button, so long
        // as you specify a parent activity in AndroidManifest.xml.
        int id = item.getItemId();
        if (id == R.id.action_settings) {
            return true;
        }
        return super.onOptionsItemSelected(item);
    }

    /**
     * A placeholder fragment containing a simple view.
     */
    public static class PlaceholderFragment extends Fragment {

        public PlaceholderFragment() {
        }

        @Override
        public View onCreateView(LayoutInflater inflater, ViewGroup container,
                                 Bundle savedInstanceState) {
            View rootView = inflater.inflate(R.layout.fragment_kart, container,
                    false);
            return rootView;
        }
    }

    public void changeKartUnloadingState(View view) {
        Button changeStateButton = (Button) view;

        // Change the text and color (which will be effective) of the button and
        // record the change of
        // state into log file.
        if (kartIsUnloading) {
            // From "unloading" to "not unloading".
            changeStateButton.setText(getString(R.string.kart_not_unloading));
            changeStateButton.setBackgroundColor(getResources().getColor(
                    R.color.kart_not_unloading));

            buildAlertMessageDoneUnloading(this);

            long date = System.currentTimeMillis();
            String string;

            if (kartDoneUnloading) {
                string = super.getFormatterClock().format(date)
                        + " ("
                        + date
                        + ") Kart state changes to: not unloading";
            } else {
                string = super.getFormatterClock().format(date)
                        + " ("
                        + date
                        + ") Kart state changes to: not unloading";
            }

            LogFileWrite(isLOG_STATE_FLAG(), getmLogFileState(),
                    string, "KartChangeStateWrite");

        } else {
            // From "not unloading" to "unloading".
            changeStateButton.setText(getString(R.string.kart_unloading));
            changeStateButton.setBackgroundColor(getResources().getColor(
                    R.color.kart_unloading));

            long date = System.currentTimeMillis();
            LogFileWrite(isLOG_STATE_FLAG(), getmLogFileState(),
                    super.getFormatterClock().format(date) + " (" + date
                            + ") Kart state changes to: unloading\n",
                    "KartChangeStateWrite");
        }

        changeStateButton.invalidate();
        // Change the state flag.
        kartIsUnloading = !kartIsUnloading;
    }

    private void buildAlertMessageDoneUnloading(final Activity activity) {

        final AlertDialog.Builder builder = new AlertDialog.Builder(this);

        builder.setMessage(getString(R.string.kart_done_unloading))
                .setCancelable(false)
                .setPositiveButton(getString(R.string.button_yes),
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                dialog.cancel();

                                LogFileWrite(isLOG_STATE_FLAG(), getmLogFileState(),
                                        " (all unloaded)\n",
                                        "KartChangeStateWrite");
                            }
                        })
                .setNegativeButton(getString(R.string.button_no),
                        new DialogInterface.OnClickListener() {
                            public void onClick(final DialogInterface dialog,
                                                final int id) {
                                dialog.cancel();

                                LogFileWrite(isLOG_STATE_FLAG(), getmLogFileState(),
                                        " (not all unloaded)\n",
                                        "KartChangeStateWrite");
                            }
                        });
        final AlertDialog alert = builder.create();
        alert.show();

        TextView textView = (TextView) alert.findViewById(android.R.id.message);
        textView.setTextSize(30);
        Button buttonNeg = alert.getButton(DialogInterface.BUTTON_NEGATIVE);
        buttonNeg.setTextSize(25);
        Button buttonPos = alert.getButton(DialogInterface.BUTTON_POSITIVE);
        buttonPos.setTextSize(25);
    }
}
