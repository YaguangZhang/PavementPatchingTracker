package edu.purdue.PavementPatchingTracker;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;

import edu.purdue.PavementPatchingTracker.utils.WifiAdmin;

/**
 * Created by Zyglabs on 7/9/15.
 * <p/>
 * Will create a Wifi spot and
 * TODO: control the Wifi speed test.
 */
public class WifiSpeedTestServerActivity extends BasicGpsLoggingActivity {

    private String SSID = "OpenAgSpeedTestServer";
    private String PASSWORD = "ecemsee288";

    public String getSSID() {
        return SSID;
    }

    public String getPASSWORD() {
        return PASSWORD;
    }

    private WifiAdmin mWifiAdmin = null;
    private WifiConfiguration mWifiCon;
    private int mPastWifiState;
    public final int WIFI_STATE_ENABLED = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setLogStateFlag(true);
        setLOG_CELL_FLAG(true);
        setLOG_WIFI_FLAG(false);

        // Get the Wifi access point's id.
        WifiManager tempWifiManager = (WifiManager) getSystemService(Context.WIFI_SERVICE);
        mPastWifiState = tempWifiManager.getWifiState();
    }

    @Override
    protected void onStart() {
        super.onStart();

        // Create Wifi manager if necessary.
        if (mWifiAdmin == null) {
            mWifiAdmin = new WifiAdmin(this);
        }

        mWifiCon = mWifiAdmin.createWifiAccessPoint(SSID, PASSWORD);

//        // Close the Wifi.
//        mWifiAdmin.closeWifi();
//        // Create the Wifi hotspot.
//        mWifiCon = mWifiAdmin.CreateWifiInfo(SSID, PASSWORD, 3);
//        mWifiAdmin.addNetwork(mWifiCon);
//        // Make sure Wifi is on.
//        mWifiAdmin.openWifi();
    }

    @Override
    protected void onStop() {
        super.onStop();

        // Close the Wifi.
        mWifiCon = mWifiAdmin.closeWifiAccessPoint(mWifiCon);

        // Reconnect to the previous Wifi access point.
        if(mPastWifiState == WIFI_STATE_ENABLED) {
            mWifiAdmin.getmWifiManager().setWifiEnabled(true);
        } else {
            mWifiAdmin.getmWifiManager().setWifiEnabled(false);
        }

    }

}
