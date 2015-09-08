/*
 * Copyright (C) 2007 The Android Open Source Project
 * Copyright (C) 2012 Yuriy Kulikov yuriy.kulikov.87@gmail.com
 *
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 *
 *      http://www.apache.org/licenses/LICENSE-2.0
 *
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */

package com.better.alarm.presenter;

import android.app.Activity;
import android.content.BroadcastReceiver;
import android.content.Context;
import android.content.Intent;
import android.content.IntentFilter;
import android.content.pm.ActivityInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.preference.PreferenceManager;
import android.view.Menu;
import android.view.View;
import android.view.View.OnClickListener;
import android.view.ViewGroup;
import android.view.Window;
import android.view.WindowManager;

import com.better.alarm.AlarmApplication;
import com.better.alarm.R;
import com.better.alarm.launcher.view.BatterieLevel;
import com.better.alarm.launcher.view.BluetoothIndicator;
import com.better.alarm.launcher.view.DataTransferIndicator;
import com.better.alarm.launcher.view.DigitalClock;
import com.better.alarm.launcher.view.NetworkLevelTriangleStyle;
import com.better.alarm.launcher.view.NetworkLevelTriangleStyleSecondSim;
import com.better.alarm.launcher.view.WifiIndicator;
import com.better.alarm.model.interfaces.Alarm;
import com.better.alarm.model.interfaces.Intents;
import com.better.alarm.presenter.AlarmsListFragment.ShowDetailsStrategy;
import com.better.alarm.presenter.TimePickerDialogFragment.AlarmTimePickerDialogHandler;

/**
 * This activity displays a list of alarms and optionally a details fragment.
 */
public class AlarmsListActivity extends Activity implements AlarmTimePickerDialogHandler {
    private static int mPadding;
    private static AlarmsListActivity mInstance;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        requestWindowFeature(Window.FEATURE_NO_TITLE);
        getWindow().setFlags(WindowManager.LayoutParams.FLAG_FULLSCREEN, WindowManager.LayoutParams.FLAG_FULLSCREEN);

        AlarmApplication.updateFrLanguage(this);
        setTheme(DynamicThemeHandler.getInstance().getIdForName(AlarmsListActivity.class.getName()));
        // getActionBar().hide();
        super.onCreate(savedInstanceState);

        mInstance = this;

        boolean isTablet = !getResources().getBoolean(R.bool.isTablet);
        if (isTablet) {
            setRequestedOrientation(ActivityInfo.SCREEN_ORIENTATION_PORTRAIT);
        }

        ViewGroup rootView = (ViewGroup) getLayoutInflater().inflate(R.layout.list_activity, null);
        setContentView(rootView);

        setNotificationBarToBlackTheme();
        AlarmApplication.udateTypeFace(this, rootView);

        alarmsListFragment = (AlarmsListFragment) getFragmentManager().findFragmentById(
                R.id.list_activity_list_fragment);

        if (isTablet) {
            // TODO
            // alarmsListFragment.setShowDetailsStrategy(showDetailsInFragmentStrategy);
            alarmsListFragment.setShowDetailsStrategy(showDetailsInActivityFragment);
        } else {
            alarmsListFragment.setShowDetailsStrategy(showDetailsInActivityFragment);
        }

        View button = findViewById(R.id.valider);
        button.setOnClickListener(new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent intent = new Intent(AlarmsListActivity.this, AlarmDetailsActivity.class);
                startActivity(intent);
            }
        });

        OnClickListener goBackOnClickListenner = new OnClickListener() {

            @Override
            public void onClick(View v) {
                Intent i = new Intent(Intent.ACTION_MAIN);
                i.addCategory(Intent.CATEGORY_HOME);
                startActivity(i);
            }
        };

        View back = findViewById(R.id.go_to_prev_page);
        back.setOnClickListener(goBackOnClickListenner);
        back = findViewById(R.id.back);
        back.setOnClickListener(goBackOnClickListenner);
    }

    @Override
    protected void onResume() {
        super.onResume();

        View nextAlarmFragment = findViewById(R.id.list_activity_info_fragment);
        if (PreferenceManager.getDefaultSharedPreferences(this).getBoolean("show_info_fragment", false)) {
            nextAlarmFragment.setVisibility(View.VISIBLE);
        } else {
            nextAlarmFragment.setVisibility(View.GONE);
        }

        reqisterRecieverToGetPadding();
        requestForGetPaddindForSosButton();
    }

    @Override
    protected void onPause() {
        super.onPause();

        AlarmsListActivity.requestToShowArrow();
    }

    @Override
    public void onPanelClosed(int featureId, Menu menu) {
        super.onPanelClosed(featureId, menu);
    }

    public static void requestForGetPaddindForSosButton() {
        Intent intent = new Intent("com.louka.launcher.sosbutton.padding");
        AlarmsListActivity.mInstance.sendBroadcast(intent);
    }

    private void reqisterRecieverToGetPadding() {
        final PaddingReciever paddingReciever = new PaddingReciever();
        IntentFilter intentFilter = new IntentFilter("launcher.send.padding.for.sos.button");
        getApplication().registerReceiver(paddingReciever, intentFilter);
        Handler handler = new Handler();
        Runnable runnable = new Runnable() {

            @Override
            public void run() {
                getApplication().unregisterReceiver(paddingReciever);
            }
        };
        handler.postDelayed(runnable, 2000);
    }

    private final ShowDetailsStrategy showDetailsInActivityFragment = new ShowDetailsStrategy() {
        @Override
        public void showDetails(Alarm alarm) {
            Intent intent = new Intent(AlarmsListActivity.this, AlarmDetailsActivity.class);
            if (alarm != null) {
                intent.putExtra(Intents.EXTRA_ID, alarm.getId());
            }
            startActivity(intent);
        }
    };
    private AlarmsListFragment alarmsListFragment;

    private Alarm timePickerAlarm;

    public void showTimePicker(Alarm alarm) {
        timePickerAlarm = alarm;
        TimePickerDialogFragment.showTimePicker(getFragmentManager());
    }

    @Override
    public void onDialogTimeSet(int hourOfDay, int minute) {
        timePickerAlarm.edit().setEnabled(true).setHour(hourOfDay).setMinutes(minute).commit();
        // this must be invoked synchronously on the Pickers's OK button onClick
        // otherwise fragment is closed too soon and the time is not updated
        alarmsListFragment.updateAlarmsList();
    }

    public static int getmPadding() {
        return mPadding;
    }

    // Send back broadcast with padding for SOS button state.
    public static class PaddingReciever extends BroadcastReceiver {
        private final String PADDING = "padding";

        @Override
        public void onReceive(Context context, Intent intent) {
            if (intent.hasExtra(PADDING)) {
                int padding = intent.getIntExtra(PADDING, 0);
                setPadding(padding);
            }
        }

        private void setPadding(int padding2) {
            if (AlarmsListActivity.mInstance != null) {
                AlarmsListActivity.mInstance.getWindow().getDecorView().findViewById(R.id.rootView)
                .setPadding(0, 0, 0, padding2);
                mPadding = padding2;
            }
        }
    }

    public static void requestToShowArrow() {
        Intent intent = new Intent("com.louka.launcher.sosbutton.show");
        AlarmsListActivity.mInstance.sendBroadcast(intent);
    }

    public void setNotificationBarToBlackTheme() {
        Activity context = this;
        View notificationBar = context.findViewById(R.id.notification_bar);
        notificationBar.setBackgroundResource(android.R.color.white);

        DigitalClock digitalClock = (DigitalClock) context.findViewById(R.id.clock);
        digitalClock.setTextColor(Color.parseColor("#e0000000"));

        NetworkLevelTriangleStyle networkLevel = (NetworkLevelTriangleStyle) context.findViewById(R.id.network_level);
        networkLevel.setTheme(NetworkLevelTriangleStyle.THEME_BLACK);

        NetworkLevelTriangleStyleSecondSim networkLevelSecondSim = (NetworkLevelTriangleStyleSecondSim) context
                .findViewById(R.id.network_level_sim2);
        networkLevelSecondSim.setTheme(NetworkLevelTriangleStyle.THEME_BLACK);

        BluetoothIndicator bluetooth = (BluetoothIndicator) context.findViewById(R.id.blutooth_indicator);
        bluetooth.setTheme(BluetoothIndicator.THEME_BLACK);

        WifiIndicator wifiIndicator = (WifiIndicator) context.findViewById(R.id.wifi_indicator);
        wifiIndicator.setTheme(WifiIndicator.THEME_BLACK);

        DataTransferIndicator dataTransferIndicator = (DataTransferIndicator) context
                .findViewById(R.id.network_data_transfer_indicator);
        dataTransferIndicator.setTheme(DataTransferIndicator.THEME_BLACK);

        BatterieLevel batterieLevel = (BatterieLevel) context.findViewById(R.id.batterie_level);
        batterieLevel.setTheme(BatterieLevel.THEME_BLACK);
    }
}
