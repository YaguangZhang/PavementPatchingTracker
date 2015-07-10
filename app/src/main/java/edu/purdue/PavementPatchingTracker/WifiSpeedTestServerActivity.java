package edu.purdue.PavementPatchingTracker;

import android.content.Context;
import android.net.wifi.WifiConfiguration;
import android.net.wifi.WifiManager;
import android.os.Bundle;
import android.util.Log;

import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

/**
 * Created by Zyglabs on 7/9/15.
 *
 * Will create a Wifi spot and
 * TODO: control the Wifi speed test.
 */
public class WifiSpeedTestServerActivity extends BasicGpsLoggingActivity
{

    private String SSID = "OpenAgSpeedTestServer";
    private String PASSWORD = "ecemsee288";

    public String getSSID() {
        return SSID;
    }

    public String getPASSWORD() {
        return PASSWORD;
    }

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        setLogStateFlag(true);
        setLOG_CELL_FLAG(true);
        setLOG_WIFI_FLAG(false);
    }

    @Override
    protected void onResume(){
        super.onResume();

        // Create the Wifi hotspot.
        createWifiAccessPoint();
    }

    private void createWifiAccessPoint() {
        WifiManager wifiManager = (WifiManager)getBaseContext().getSystemService(Context.WIFI_SERVICE);
        if(wifiManager.isWifiEnabled())
        {
            wifiManager.setWifiEnabled(false);
        }
        Method[] wmMethods = wifiManager.getClass().getDeclaredMethods();
        boolean methodFound=false;
        for(Method method: wmMethods){
            if(method.getName().equals("setWifiApEnabled")){
                methodFound=true;
                WifiConfiguration netConfig = new WifiConfiguration();

                netConfig.SSID = "\""+ SSID + "\"";
                netConfig.hiddenSSID = false;
                netConfig.preSharedKey  = PASSWORD;
                netConfig.wepKeys[0] = "\"" + PASSWORD + "\"";
                netConfig.wepTxKeyIndex = 0;
                netConfig.status = WifiConfiguration.Status.ENABLED;

                netConfig.allowedKeyManagement.set(WifiConfiguration.KeyMgmt.WPA_PSK);
                netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.OPEN);
                netConfig.allowedAuthAlgorithms.set(WifiConfiguration.AuthAlgorithm.SHARED);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.RSN);
                netConfig.allowedProtocols.set(WifiConfiguration.Protocol.WPA);
                netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.CCMP);
                netConfig.allowedPairwiseCiphers.set(WifiConfiguration.PairwiseCipher.TKIP);
                netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.CCMP);
                netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.TKIP);
                netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP40);
                netConfig.allowedGroupCiphers.set(WifiConfiguration.GroupCipher.WEP104);

                wifiManager.saveConfiguration();

                try {
                    boolean apstatus=(Boolean) method.invoke(wifiManager, netConfig, true);
                    Log.i("WifiSpeedTestServer", "Creating a Wi-Fi Network \"" + netConfig.SSID + "\"");
                    for (Method isWifiApEnabledmethod: wmMethods)
                    {
                        if(isWifiApEnabledmethod.getName().equals("isWifiApEnabled")){
                            while(!(Boolean)isWifiApEnabledmethod.invoke(wifiManager));
                            for(Method method1: wmMethods){
                                if(method1.getName().equals("getWifiApState")){
                                    int apstate;
                                    apstate=(Integer)method1.invoke(wifiManager);
                                    netConfig=(WifiConfiguration)method1.invoke(wifiManager);
                                    Log.i("WifiSpeedTestServer", "\nSSID:"+netConfig.SSID+"\nPassword:"+netConfig.preSharedKey+"\n");
                                }
                            }
                        }
                    }

                    if(apstatus)
                    {
                        System.out.println("SUCCESSdddd");
                        //statusView.append("\nAccess Point Created!");
                        //finish();
                        //Intent searchSensorsIntent = new Intent(this,SearchSensors.class);
                        //startActivity(searchSensorsIntent);
                    }else
                    {
                        System.out.println("FAILED");
                        //statusView.append("\nAccess Point Creation failed!");
                    }
                } catch (IllegalArgumentException e) {
                    e.printStackTrace();
                } catch (IllegalAccessException e) {
                    e.printStackTrace();
                } catch (InvocationTargetException e) {
                    e.printStackTrace();
                }
            }
        }
        if(!methodFound){
             Log.i("WifiSpeedTestServer", "Your phone's API does not contain setWifiApEnabled method to configure an access point");
        }


    }

}
