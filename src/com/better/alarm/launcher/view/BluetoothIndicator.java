package com.better.alarm.launcher.view;

import android.bluetooth.BluetoothAdapter;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.ViewGroup;
import android.widget.FrameLayout;
import android.widget.ImageView;

import com.better.alarm.R;

public class BluetoothIndicator extends FrameLayout {
    public BluetoothIndicator(Context context) {
        super(context);
        init();
    }

    public BluetoothIndicator(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    public static final int THEME_BLACK = 0;
    public static final int THEME_WHITE = 1;

    private static BluetoothIndicator mInstance;

    public ImageView mIndicator;

    private int currentTheme = THEME_WHITE;
    private final boolean dontHaveConnection = false;

    private BluetoothReceiver mBluetoothStateReceiver;

    public static BluetoothIndicator getInstanceCanReturnNull() {
        return mInstance;
    }

    private void init() {
        mInstance = this;

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        ViewGroup rootView = (ViewGroup) layoutInflater.inflate(R.layout.bluetooth_indicator, this);
        mIndicator = (ImageView) rootView.getChildAt(0);

        checkBluetooth();

        registerListenner();
    }

    public void setTheme(int theme) {
        currentTheme = theme;

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
        updateView(R.drawable.notification_bluetooth);
    }

    private void setBlackTheme() {
        updateView(R.drawable.notification_blutooth_dark);
    }

    private void updateView(int resId) {
        mIndicator.setImageResource(resId);
    }

    private void registerListenner() {
        mBluetoothStateReceiver = new BluetoothReceiver();
        IntentFilter filter = new IntentFilter(BluetoothAdapter.ACTION_STATE_CHANGED);

        getContext().registerReceiver(mBluetoothStateReceiver, filter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        unregisterListenner();
    }

    private void unregisterListenner() {
        getContext().unregisterReceiver(mBluetoothStateReceiver);
    }

    public class BluetoothReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            final String action = intent.getAction();

            if (action.equals(BluetoothAdapter.ACTION_STATE_CHANGED)) {
                final int state = intent.getIntExtra(BluetoothAdapter.EXTRA_STATE, BluetoothAdapter.ERROR);
                switch (state) {
                case BluetoothAdapter.STATE_OFF:
                    mIndicator.setVisibility(GONE);
                    break;

                case BluetoothAdapter.STATE_ON:
                    mIndicator.setVisibility(VISIBLE);
                    break;

                }
            }
        }
    }

    private void checkBluetooth() {
        BluetoothAdapter bluetooth = BluetoothAdapter.getDefaultAdapter();
        if (bluetooth != null && bluetooth.isEnabled()) {
            mIndicator.setVisibility(VISIBLE);
        } else {
            mIndicator.setVisibility(GONE);
        }
    }

}
