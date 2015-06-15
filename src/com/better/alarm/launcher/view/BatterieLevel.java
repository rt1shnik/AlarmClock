package com.better.alarm.launcher.view;

import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.graphics.Color;
import android.os.BatteryManager;
import android.util.AttributeSet;
import android.view.LayoutInflater;
import android.view.View;
import android.widget.FrameLayout;
import android.widget.ImageView;
import android.widget.LinearLayout;
import android.widget.TextView;

import com.better.alarm.R;

public class BatterieLevel extends FrameLayout {
    public static final int THEME_WHITE = 0;
    public static final int THEME_BLACK = 1;

    private static BatterieLevel mInstance;

    private int mCurrentTheme = THEME_WHITE;
    private boolean isCharged = false;

    private static int BACKGROUND_FOR_FULL_BATTERIE = R.drawable.b_battery_wt;
    private static int BACKGROUND_FOR_EMPTY_BATTERIE = R.drawable.b_battery_wt;

    private static final int WIDTH = 28;
    private static final int HIGHT = 11;

    private View mViewWithBackground;
    private View mViewFilledBatterieSpace;
    private View mViewEmptyBatterieSpace;
    private BatterieBroadcastReceiver mBatterieReceiver;
    private TextView mBatterieValue;
    private int mLastBatterieLevel;

    public static BatterieLevel getInstanceCanReturnNull() {
        return mInstance;
    }

    public BatterieLevel(Context context) {
        super(context);
        init();
    }

    public BatterieLevel(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        mInstance = this;

        LayoutInflater layoutInflater = LayoutInflater.from(getContext());
        layoutInflater.inflate(R.layout.battarie_header_layout, this);

        mViewWithBackground = findViewById(R.id.batterie_view_with_bg);
        mViewFilledBatterieSpace = findViewById(R.id.fill);
        mViewEmptyBatterieSpace = findViewById(R.id.empty);

        mBatterieValue = (TextView) findViewById(R.id.batterieValue);

        registerBatterieListenner();
    }

    public void updateView(int batterieLevel) {
        if (mViewWithBackground != null) {
            if (batterieLevel == 100) {
                mViewWithBackground.setBackgroundResource(BACKGROUND_FOR_FULL_BATTERIE);
            } else {
                mViewWithBackground.setBackgroundResource(BACKGROUND_FOR_EMPTY_BATTERIE);
            }

            LinearLayout.LayoutParams layoutParamsFilled = (android.widget.LinearLayout.LayoutParams) mViewFilledBatterieSpace
                    .getLayoutParams();
            layoutParamsFilled.weight = batterieLevel;
            mViewFilledBatterieSpace.setLayoutParams(layoutParamsFilled);

            LinearLayout.LayoutParams layoutParamsEmpty = (android.widget.LinearLayout.LayoutParams) mViewEmptyBatterieSpace
                    .getLayoutParams();
            layoutParamsEmpty.weight = 100 - batterieLevel;
            mViewEmptyBatterieSpace.setLayoutParams(layoutParamsEmpty);

            mBatterieValue.setText(String.valueOf(batterieLevel));
        }
    }

    public void setTheme(int theme) {
        mCurrentTheme = theme;
        if (isCharged == false) {
            switch (theme) {
            case THEME_BLACK:
                setBlackTheme();
                break;
            case THEME_WHITE:
                setWhiteTheme();
                break;
            default:
                break;
            }
        }
    }

    private void setBlackTheme() {
        BACKGROUND_FOR_FULL_BATTERIE = R.drawable.b_battery_bk;
        BACKGROUND_FOR_EMPTY_BATTERIE = R.drawable.b_battery_bk;

        ImageView percent = (ImageView) findViewById(R.id.percent);
        percent.setImageResource(R.drawable.header_battarie_percent_black);
        mViewFilledBatterieSpace.setBackgroundResource(R.drawable.b_battery_filled_space_bk);

        mBatterieValue.setTextColor(Color.parseColor("#d4000000"));

        updateView(mLastBatterieLevel);
    }

    private void setWhiteTheme() {
        BACKGROUND_FOR_FULL_BATTERIE = R.drawable.b_battery_wt;
        BACKGROUND_FOR_EMPTY_BATTERIE = R.drawable.b_battery_wt;

        ImageView percent = (ImageView) findViewById(R.id.percent);
        percent.setImageResource(R.drawable.percent);
        mViewFilledBatterieSpace.setBackgroundResource(R.drawable.b_battery_filled_space_wt);

        mBatterieValue.setTextColor(Color.parseColor("#d4ffffff"));

        updateView(mLastBatterieLevel);
    }

    private void setChargeState() {
        isCharged = true;

        BACKGROUND_FOR_FULL_BATTERIE = R.drawable.b_battery_charge;
        BACKGROUND_FOR_EMPTY_BATTERIE = R.drawable.b_battery_charge;

        ImageView percent = (ImageView) findViewById(R.id.percent);
        percent.setImageResource(R.drawable.header_battarie_percent_green);
        mViewFilledBatterieSpace.setBackgroundColor(Color.parseColor("#0000BB00"));

        mBatterieValue.setTextColor(Color.parseColor("#00BB00"));

        updateView(mLastBatterieLevel);
    }

    private void registerBatterieListenner() {
        mBatterieReceiver = new BatterieBroadcastReceiver();
        IntentFilter filter = new IntentFilter();
        filter.addAction(Intent.ACTION_BATTERY_CHANGED);

        getContext().registerReceiver(mBatterieReceiver, filter);
    }

    @Override
    protected void onDetachedFromWindow() {
        super.onDetachedFromWindow();
        getContext().unregisterReceiver(mBatterieReceiver);
    }

    class BatterieBroadcastReceiver extends BroadcastReceiver {

        @Override
        public void onReceive(Context context, Intent intent) {
            int rawlevel = intent.getIntExtra(BatteryManager.EXTRA_LEVEL, -1);
            int scale = intent.getIntExtra(BatteryManager.EXTRA_SCALE, -1);
            int batterie = -1;
            if (rawlevel >= 0 && scale > 0) {
                batterie = (rawlevel * 100) / scale;
            }

            if (mLastBatterieLevel != batterie) {
                updateView(batterie);
            }

            mLastBatterieLevel = batterie;

            int status = intent.getIntExtra(BatteryManager.EXTRA_STATUS, -1);
            boolean isCharging = status == BatteryManager.BATTERY_STATUS_CHARGING
                    || status == BatteryManager.BATTERY_STATUS_FULL;

            if (isCharging) {
                setChargeState();
            } else {
                isCharged = false;
                setTheme(mCurrentTheme);
            }
        }
    }
}
