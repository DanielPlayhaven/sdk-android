package v2.com.playhaven.utils;

import android.content.Context;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.NetworkInfo.State;

import v2.com.playhaven.configuration.PHConfiguration.ConnectionType;

/**
 * Simple utility class for fetching the network state
 * @author andreiciortea
 */
public class PHConnectionUtils {
	
	/** Gets the connection type if this app has appropriate permissions */
    public static ConnectionType getConnectionType(Context context) {
        try {
            ConnectivityManager manager	= (ConnectivityManager) context.getSystemService(Context.CONNECTIVITY_SERVICE);
            
            if (manager == null) 
            	return ConnectionType.NO_NETWORK; // happens during tests

            NetworkInfo mobileInfo = manager.getNetworkInfo(ConnectivityManager.TYPE_MOBILE);
            NetworkInfo wifiInfo   = manager.getNetworkInfo(ConnectivityManager.TYPE_WIFI);
            
            if (mobileInfo == null || wifiInfo == null) 
            	return ConnectionType.NO_NETWORK; // happens during tests
            
            State mobile                = mobileInfo.getState();
            State wifi                  = wifiInfo.getState();
            
            if (mobile == NetworkInfo.State.CONNECTED || mobile == NetworkInfo.State.CONNECTING) {
                return ConnectionType.MOBILE;
                
            } else if (wifi == NetworkInfo.State.CONNECTED || wifi == NetworkInfo.State.CONNECTING) {
                return ConnectionType.WIFI;
            }
        } catch (SecurityException e) {
            // ACCESS_NETWORK_STATE permission not granted in the manifest
            return ConnectionType.NO_PERMISSION;
        }
        
        return ConnectionType.NO_NETWORK;
    }
}
