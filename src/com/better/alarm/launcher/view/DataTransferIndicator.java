package com.better.alarm.launcher.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.AttributeSet;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.better.alarm.R;

public class DataTransferIndicator extends FrameLayout {
    public static final int THEME_BLACK = 0;
    public static final int THEME_WHITE = 1;

    private static DataTransferIndicator mInstance;

    private ImageView mIndicator;

    private WifiReceiver mWifiStateReceiver;
    private boolean wifiOf;
    private boolean mobileOn;

    public static DataTransferIndicator getInstanceCanReturnNull() {
        return mInstance;
    }

    public DataTransferIndicator(Context context) {
        super(context);
        init();
    }

    public DataTransferIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mInstance = this;

        mIndicator = new ImageView(getContext());
        addView(mIndicator);

        checkWifiStatus();

        registerListenner();

        setTheme(THEME_WHITE);
    }

    public void setTheme(int theme) {
        switch (theme) {
        case THEME_BLACK:
            setBlackTheme();
            break;
        case THEME_WHITE:
            setWhiteTheme();
            break;
        default:
            setWhiteTheme();
            break;
        }
    }

    private void setWhiteTheme() {
        updateView(R.drawable.n_connection_wt);
    }

    private void setBlackTheme() {
        updateView(R.drawable.n_connection_bk);
    }

    private void updateView(int resId) {
        mIndicator.setImageResource(resId);
    }

    private void registerListenner() {
        mWifiStateReceiver = new WifiReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(WifiManager.NETWORK_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_CONNECTION_CHANGE_ACTION);
        filter.addAction(WifiManager.WIFI_STATE_CHANGED_ACTION);
        filter.addAction(WifiManager.SUPPLICANT_STATE_CHANGED_ACTION);
        filter.addAction(ConnectivityManager.CONNECTIVITY_ACTION);

        getContext().registerReceiver(mWifiStateReceiver, filter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterListenner();
    }

    private void unregisterListenner() {
        getContext().unregisterReceiver(mWifiStateReceiver);
    }

    public class WifiReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            checkWifiStatus();
        }
    }

    public void checkWifiStatus() {
        ConnectivityManager conMgr = (ConnectivityManager) getContext().getApplicationContext().getSystemService(
                Context.CONNECTIVITY_SERVICE);
        NetworkInfo[] netInf = conMgr.getAllNetworkInfo();
        for (NetworkInfo inf : netInf) {
            if (inf.getType() == ConnectivityManager.TYPE_WIFI) {
                if (inf.isConnected() == false) {
                    wifiOf = true;
                } else {
                    wifiOf = false;
                }
            }
            if (inf.getType() == ConnectivityManager.TYPE_MOBILE) {
                if (inf.isConnected()) {
                    mobileOn = true;
                } else {
                    mobileOn = false;
                }
            }
        }

        if (isOnline() && wifiOf && mobileOn) {
            showIndicator();
        } else {
            hideIndicator();
        }
    }

    public boolean isOnline() {
        ConnectivityManager cm = (ConnectivityManager) getContext().getSystemService(Context.CONNECTIVITY_SERVICE);
        NetworkInfo netInfo = cm.getActiveNetworkInfo();
        return netInfo != null && netInfo.isConnectedOrConnecting();
    }

    private void hideIndicator() {
        mIndicator.setVisibility(GONE);
    }

    private void showIndicator() {
        mIndicator.setVisibility(VISIBLE);
    }
}
