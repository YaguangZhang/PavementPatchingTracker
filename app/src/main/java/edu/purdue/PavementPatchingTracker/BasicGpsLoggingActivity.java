package edu.purdue.PavementPatchingTracker;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.SharedPreferences;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.net.Uri;
import android.net.wifi.ScanResult;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.os.Environment;
import android.support.v4.app.Fragment;
import android.support.v7.app.ActionBarActivity;
import android.telephony.CellInfo;
import android.telephony.CellInfoCdma;
import android.telephony.CellInfoGsm;
import android.telephony.CellInfoLte;
import android.telephony.CellSignalStrengthCdma;
import android.telephony.CellSignalStrengthGsm;
import android.telephony.CellSignalStrengthLte;
import android.telephony.TelephonyManager;
import android.util.Log;
import android.view.LayoutInflater;
import android.view.Menu;
import android.view.MenuItem;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;
import android.widget.Toast;

import java.io.File;
import java.io.FileWriter;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

/*
 * The activity for GPS data recording only.
 * 
 * @author: Yaguang Zhang
 * Reference: Stephan Williams' LogAccelGpsActivity project.
 * Available at https://github.com/OATS-Group/android-logger
 */

public class BasicGpsLoggingActivity extends ActionBarActivity implements
        LocationListener {

    private final boolean LOG_GPS_FLAG = true;

    private boolean LOG_STATE_FLAG = true;
    private boolean LOG_CELL_FLAG = true;
    private boolean LOG_WIFI_FLAG = true;
    private boolean IS_SPEED_TEST_SERVER_FLAG = true;

    private TelephonyManager mTelephonyManager;
    private WifiManager mWifiManager;
    private WifiScanReceiver mWifiScanReceiver;
    private String wifis[];

    private String loginId, logFilesPath;
    private LogFile mLogFileGps = new LogFile();
    private LogFile mLogFileState = new LogFile();
    private LogFile mLogFileCell = new LogFile();
    private LogFile mLogFileWifiDisc = new LogFile();
    private LogFile mLogFileConn = new LogFile();
    private LocationManager mLocationManager;

    private TextView textViewTime;
    private SimpleDateFormat formatterUnderline = new SimpleDateFormat(
            "yyyy_MM_dd_HH_mm_ss",
            java.util.Locale.getDefault());
    private SimpleDateFormat formatterClock;

    // Preference file used to store the info.
    private SharedPreferences sharedPref;

    public boolean isLOG_STATE_FLAG() {
        return LOG_STATE_FLAG;
    }

    public String getLoginId() {
        return loginId;
    }

    public void setLoginId(String loginId) {
        this.loginId = loginId;
    }

    public String getLogFilesPath() {
        return logFilesPath;
    }

    public void setLogFilesPath(String logFilesPath) {
        this.logFilesPath = logFilesPath;
    }

    public void setLogStateFlag(boolean flag) {
        LOG_STATE_FLAG = flag;
    }

    public LogFile getmLogFileState() {
        return mLogFileState;
    }

    public FileWriter getMLogState() {
        return getmLogFileState().getWriter();
    }

    public void setmLogFileState(LogFile mLogFileState) {
        this.mLogFileState = mLogFileState;
    }

    public SimpleDateFormat getFormatterClock() {
        return formatterClock;
    }

    public SharedPreferences getSharedPref() {
        return sharedPref;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        actionBarActivityOnCreate(savedInstanceState);

        setContentView(R.layout.activity_basic_gps_logging);

        if (savedInstanceState == null) {
            getSupportFragmentManager().beginTransaction()
                    .add(R.id.container, new PlaceholderFragment()).commit();
        }
    }

    public void actionBarActivityOnCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
    }

    @Override
    protected void onStart() {
        super.onStart();

        /**
         * Initialization.
         */

        // Set the sharedPref if we haven't done so.
        if (sharedPref == null) {
            sharedPref = this.getSharedPreferences(
                    getString(R.string.shared_preference_file_key),
                    Context.MODE_PRIVATE);
        }

        // Load the history ID record if it's initializing.
        if (loginId == null) {
            loginId = sharedPref.getString(getString(R.string.saved_last_id),
                    null);
        }

        // Create directories if necessary.
        if (logFilesPath == null) {
            logFilesPath = Environment.getExternalStorageDirectory()
                    + getPartialLogFilePath() + loginId;

            if (Environment.MEDIA_MOUNTED.equals(Environment
                    .getExternalStorageState())) {
                File logFileDirFile = new File(logFilesPath);

                logFileDirFile.mkdirs();

                if (!logFileDirFile.isDirectory()) {
                    MainLoginActivity.toastStringTextAtCenterWithLargerSize(
                            this,
                            getString(R.string.gps_log_file_dir_create_error));
                }
            } else {
                MainLoginActivity
                        .toastStringTextAtCenterWithLargerSize(
                                this,
                                getString(R.string.gps_log_file_external_storage_error));
            }
        }

        mLocationManager = (LocationManager) getSystemService(Context.LOCATION_SERVICE);
        /** Todo: May want to improve the GPS performance using fused location provider.
         * https://developer.android.com/training/location/receive-location-updates.html
         */
        mLocationManager.requestLocationUpdates(
                LocationManager.NETWORK_PROVIDER, 0, 0, this);

        mLocationManager.requestLocationUpdates(
                LocationManager.GPS_PROVIDER, 0, 0, this);
    }

    public String getLoginType() {
        return getString(R.string.vehicle_kart);
    }

    public String getPartialLogFilePath() {
        return getString(R.string.gps_log_file_path_kart);
    }

    public String getLogFilePath() {
        return logFilesPath;
    }

    public SimpleDateFormat getFormatterUnderline() {
        return formatterUnderline;
    }

    @Override
    protected void onResume() {
        super.onResume();

        if (LOG_CELL_FLAG) {
            mTelephonyManager = (TelephonyManager) this.getSystemService(Context.TELEPHONY_SERVICE);
        }

        if (LOG_WIFI_FLAG) {
            mWifiManager = (WifiManager) this.getSystemService(Context.WIFI_SERVICE);
            mWifiScanReceiver = new WifiScanReceiver();
            registerReceiver(mWifiScanReceiver,
                    new IntentFilter(WifiManager.SCAN_RESULTS_AVAILABLE_ACTION));
            super.onResume();
        }

        createLogFiles();

        // Start the timer textView.
        textViewTime = ((TextView) findViewById(R.id.textViewTime));
        formatterClock = new SimpleDateFormat("yyyy/MM/dd HH:mm:ss",
                java.util.Locale.getDefault());

        Thread threadTimer = new Thread() {
            @Override
            public void run() {
                try {
                    while (!isInterrupted()) {
                        Thread.sleep(100);
                        runOnUiThread(new Runnable() {
                            @Override
                            public void run() {
                                Date date = new Date();
                                textViewTime.setText("Time: "
                                        + formatterClock.format(date));
                            }
                        });
                    }
                } catch (Exception e) {
                    Log.e("BasicGpsLogTimer", e.toString());
                }
            }
        };
        threadTimer.start();

        setBackgroundColor();

    }

    public void setBackgroundColor() {
        findViewById(R.id.textViewVehicleTypeLabel).getRootView()
                .setBackgroundColor(
                        getResources().getColor(
                                MainLoginActivity.COLOR_BASIC_GPS_LOGGING));
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {

        // Inflate the menu; this adds items to the action bar if it is present.
        getMenuInflater().inflate(R.menu.main_activity_login, menu);
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
            View rootView = inflater.inflate(
                    R.layout.fragment_basic_gps_logging, container, false);
            return rootView;
        }
    }

    @Override
    protected void onPause() {
        super.onPause();

        if (LOG_WIFI_FLAG) {
            unregisterReceiver(mWifiScanReceiver);
        }

        closeLogFiles();

        TextView ckt = ((TextView) findViewById(R.id.textViewCktState));
        ckt.setText(getString(R.string.ckt_state_loading));

        mLocationManager.removeUpdates(this);
    }

    @Override
    public void onLocationChanged(Location location) {
        TextView ckt = ((TextView) findViewById(R.id.textViewCktState));

        TextView latGps = ((TextView) findViewById(R.id.textViewLatGps));
        TextView lonGps = ((TextView) findViewById(R.id.textViewLonGps));
        TextView altitudeGps = ((TextView) findViewById(R.id.textViewAltitudeGps));
        TextView speedGps = ((TextView) findViewById(R.id.textViewSpeedGps));
        TextView bearingGps = ((TextView) findViewById(R.id.textViewBearingGps));
        TextView accuracyGps = ((TextView) findViewById(R.id.textViewAccuracyGps));

        ckt.setText(getString(R.string.ckt_state_recording));

        latGps.setText("Lat: " + location.getLatitude());
        lonGps.setText("Lon: " + location.getLongitude());
        altitudeGps.setText("Altitude: " + location.getAltitude());
        speedGps.setText("Speed: " + location.getSpeed());
        bearingGps.setText("Bearing: " + location.getBearing());
        accuracyGps.setText("Accuracy: " + location.getAccuracy());

        long cur_time = System.currentTimeMillis();

        LogFileWrite(LOG_GPS_FLAG, mLogFileGps, formatterClock.format(cur_time) + ", " + cur_time
                        + ", " + location.getLatitude() + ", "
                        + location.getLongitude() + ", " + location.getAltitude()
                        + ", " + location.getSpeed() + ", " + location.getBearing()
                        + ", " + location.getAccuracy() + "\n",
                "BasicActGpsWrite");

        if (LOG_CELL_FLAG) {
            ArrayList<Integer> gsmDbms = new ArrayList<>();
            ArrayList<Integer> cdmaDbms = new ArrayList<>();
            ArrayList<Integer> lteDbms = new ArrayList<>();

            ArrayList<String> gsmIds = new ArrayList<>();
            ArrayList<String> cdmaIds = new ArrayList<>();
            ArrayList<String> lteIds = new ArrayList<>();

            ArrayList<Long> gsmTimeStamps = new ArrayList<>();
            ArrayList<Long> cdmaTimeStamps = new ArrayList<>();
            ArrayList<Long> lteTimeStamps = new ArrayList<>();

            // Also log the cell signal strength.
            try {

                for (final CellInfo info : mTelephonyManager.getAllCellInfo()) {
                    if (info instanceof CellInfoGsm) {
                        final CellSignalStrengthGsm gsm = ((CellInfoGsm) info).getCellSignalStrength();
                        // do what you need
                        gsmDbms.add(gsm.getDbm());
                        gsmIds.add(((CellInfoGsm) info).getCellIdentity().toString());
                        gsmTimeStamps.add(info.getTimeStamp());

                    } else if (info instanceof CellInfoCdma) {
                        final CellSignalStrengthCdma cdma = ((CellInfoCdma) info).getCellSignalStrength();
                        // do what you need
                        cdmaDbms.add(cdma.getDbm());
                        cdmaIds.add(((CellInfoCdma) info).getCellIdentity().toString());
                        cdmaTimeStamps.add(info.getTimeStamp());
                    } else if (info instanceof CellInfoLte) {
                        final CellSignalStrengthLte lte = ((CellInfoLte) info).getCellSignalStrength();
                        // do what you need
                        lteDbms.add(lte.getDbm());
                        lteIds.add(((CellInfoLte) info).getCellIdentity().toString());
                        lteTimeStamps.add(info.getTimeStamp());
                    } else {
                        throw new Exception("Unknown type of cell signal!");
                    }
                }
            } catch (Exception e) {
                Log.e("CellStrengthLogger", "Unable to obtain cell signal information", e);
            }

            LogFileWrite(LOG_CELL_FLAG, mLogFileCell,
                    formatterClock.format(cur_time) + ", " + cur_time
                            + ", " + location.getLatitude() + ", "
                            + location.getLongitude() + ", " + location.getSpeed() + ", "
                            + location.getBearing() + ", "
                            + gsmIds + ", " + gsmDbms + ", " + gsmTimeStamps + ", "
                            + cdmaIds + ", " + cdmaDbms + ", " + cdmaTimeStamps + ", "
                            + lteIds + ", " + lteDbms + ", " + lteTimeStamps + "\n",
                    "BasicActCellWrite");

        }
    }

    @Override
    public void onProviderDisabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onProviderEnabled(String provider) {
        // TODO Auto-generated method stub

    }

    @Override
    public void onStatusChanged(String provider, int status, Bundle extras) {
        // TODO Auto-generated method stub

    }

    public void createLogFiles() {

        mLogFileGps = createLogFile(LOG_GPS_FLAG, mLogFileGps,
                "gps", getString(R.string.gps_log_file_head), "BasicGpsLogCreate");

        mLogFileState = createLogFile(LOG_STATE_FLAG, mLogFileState,
                "state", getString(R.string.gps_log_file_head), "BasicStateLogCreate");

        mLogFileCell = createLogFile(LOG_CELL_FLAG, mLogFileCell,
                "cell", getString(R.string.cell_log_file_head), "BasicCellLogCreate");

        mLogFileWifiDisc = createLogFile(LOG_WIFI_FLAG, mLogFileWifiDisc,
                "wifi_disc", getString(R.string.wifi_disc_log_file_head), "BasicWifiDiscLogCreate");

    }

    public LogFile createLogFile(boolean logFlag, LogFile logFile,
                                 String fileType, String fileTitle, String errorTag) {
        if (logFlag) {
            if (logFile.getName() == null) {
                Date date = new Date();
                logFile.setName(fileType + "_" + formatterUnderline.format(date) + ".txt");
            }

            try {
                logFile.setFile(new File(getLogFilesPath(), logFile.getName()));

                logFile.setWriter(new FileWriter(logFile.getFile()));
                logFile.getWriter().write("% " + getLoginType() + " " + getLoginId() + ": "
                        + logFile.getName() + "\n"
                        + fileTitle);

            } catch (IOException e) {
                MainLoginActivity.toastStringTextAtCenterWithLargerSize(this,
                        logFile.getName() + "\n" +
                                getString(R.string.log_file_create_error)
                );
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                Log.e(errorTag, e.toString());
            }

            return logFile;
        } else {
            return new LogFile();
        }
    }

    public void closeLogFiles() {

        // Will test corresponding flags when close the file writers.
        mLogFileGps = closeLogFile(LOG_GPS_FLAG, mLogFileGps, "closeGpsLog");
        mLogFileState = closeLogFile(LOG_STATE_FLAG, mLogFileState, "closeStateLog");
        mLogFileCell = closeLogFile(LOG_CELL_FLAG, mLogFileCell, "closeCellLog");
        mLogFileWifiDisc = closeLogFile(LOG_WIFI_FLAG, mLogFileWifiDisc, "closeWifiDiscLog");
    }

    public void LogFileWrite(boolean logFlag, LogFile logFile, String string, String errorTag) {
        if (logFlag) {
            try {
                logFile.getWriter().write(string);

                // Make sure that the data is recorded immediately so that the auto sync (e.g. for Goolge
                // Drive) works.
                logFile.getWriter().flush();
                logFile.getFile().setLastModified(System.currentTimeMillis());
            } catch (IOException e) {
                MainLoginActivity.toastStringTextAtCenterWithLargerSize(this,
                        logFile.getName() + "\n" +
                                getString(R.string.log_file_write_error));
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                Log.e(errorTag, e.toString());
            }
        }
    }

    public LogFile closeLogFile(boolean logFlag, LogFile logFile, String errorTag) {
        if (logFlag) {

            Date date = new Date();

            LogFileWrite(true, logFile,
                    "% Stopped at " + formatterClock.format(date) + "\n",
                    errorTag);

            try {

                logFile.getWriter().close();

                // Make the new file available for other apps.
                Intent mediaScanIntent = new Intent(
                        Intent.ACTION_MEDIA_SCANNER_SCAN_FILE);
                Uri contentUri = Uri.fromFile(logFile.getFile());
                mediaScanIntent.setData(contentUri);
                this.sendBroadcast(mediaScanIntent);

            } catch (IOException e) {
                MainLoginActivity.toastStringTextAtCenterWithLargerSize(this,
                        logFile.getName() + "\n" +
                                getString(R.string.log_file_close_error)
                );
                Toast.makeText(this, e.toString(), Toast.LENGTH_LONG).show();
                Log.e(errorTag, e.toString());
            }

            return new LogFile();
        } else {
            return logFile;
        }
    }

    private class WifiScanReceiver extends BroadcastReceiver {
        public void onReceive(Context c, Intent intent) {

            long cur_time = System.currentTimeMillis();
            long elapsed_time = android.os.SystemClock.elapsedRealtime();

            List<ScanResult> wifiScanList = mWifiManager.getScanResults();
            wifis = new String[wifiScanList.size()];

            for (int i = 0; i < wifiScanList.size(); i++) {
                wifis[i] = i + ", " + (wifiScanList.get(i)).SSID + ", "
                        + (wifiScanList.get(i)).BSSID + ", "
                        + (wifiScanList.get(i)).level + ", "
                        + (wifiScanList.get(i)).frequency + ", "
                        + (wifiScanList.get(i)).timestamp;

//                Log.i("ZygLabs", wifis[i]);
            }


            String string = "------\n"
                    + formatterClock.format(cur_time) + ", " + cur_time + ", " + elapsed_time
                    + "\n------\n";
            for (int i = 0; i < wifis.length; i++) {
                string = string + wifis[i] + "\n";
            }

            LogFileWrite(LOG_WIFI_FLAG, mLogFileWifiDisc,
                    string, "BasicActWifiDiscWrite");

            mWifiManager.startScan();
        }
    }

}
