package com.better.alarm.launcher.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.net.ConnectivityManager;
import android.net.NetworkInfo;
import android.net.wifi.WifiManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.better.alarm.R;

public class WifiIndicator extends FrameLayout {
    public static final int THEME_BLACK = 0;
    public static final int THEME_WHITE = 1;

    private static WifiIndicator mInstance;

    private ImageView mIndicator;

    private int currentTheme = THEME_WHITE;
    private boolean dontHaveConnection = false;
    private WifiReceiver mWifiStateReceiver;

    public static WifiIndicator getInstanceCanReturnNull() {
        return mInstance;
    }

    public WifiIndicator(Context context) {
        super(context);
        init();
    }

    public WifiIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mInstance = this;

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        ViewGroup rootView = (ViewGroup) layoutInflater.inflate(R.layout.wifi_indicator, this);
        mIndicator = (ImageView) rootView.getChildAt(0);

        checkWifiStatus();

        registerListenner();
    }

    public void setTheme(int theme) {
        currentTheme = theme;
        if (dontHaveConnection == false) {
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
    }

    private void setWhiteTheme() {
        updateView(R.drawable.ic_notification_bar_network);
    }

    private void setBlackTheme() {
        updateView(R.drawable.header_network_icon_black);
    }

    private void setRedTheme() {
        dontHaveConnection = true;
        updateView(R.drawable.header_network_icon_red);
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
        WifiManager wifiMgr = (WifiManager) getContext().getSystemService(Context.WIFI_SERVICE);
        if (wifiMgr == null) {
            mIndicator.setVisibility(GONE);
        } else {
            if (wifiMgr.isWifiEnabled()) {
                mIndicator.setVisibility(VISIBLE);

                ConnectivityManager conMgr = (ConnectivityManager) getContext().getSystemService(
                        Context.CONNECTIVITY_SERVICE);
                NetworkInfo[] netInf = conMgr.getAllNetworkInfo();
                for (NetworkInfo inf : netInf) {
                    if (inf.getType() == ConnectivityManager.TYPE_WIFI) {
                        if (inf.isConnectedOrConnecting()) {
                            dontHaveConnection = false;
                            setTheme(currentTheme);
                        } else {
                            setRedTheme();
                        }
                    }
                }

            } else {
                mIndicator.setVisibility(GONE);
            }
        }
    }
}
