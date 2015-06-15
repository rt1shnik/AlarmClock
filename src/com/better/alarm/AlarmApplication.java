/*
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
package com.better.alarm;

import java.lang.reflect.Field;
import java.util.ArrayList;
import java.util.List;
import java.util.Locale;

import org.acra.ACRA;
import org.acra.ReportField;
import org.acra.ReportingInteractionMode;
import org.acra.annotation.ReportsCrashes;
import org.acra.sender.HttpSender.Method;
import org.acra.sender.HttpSender.Type;

import android.app.Application;
import android.content.Context;
import android.content.res.Configuration;
import android.graphics.Typeface;
import android.preference.PreferenceManager;
import android.view.View;
import android.view.ViewConfiguration;
import android.view.ViewGroup;
import android.widget.TextView;

import com.better.alarm.model.AlarmCore;
import com.better.alarm.model.Alarms;
import com.better.alarm.model.AlarmsManager;
import com.better.alarm.model.AlarmsScheduler;
import com.better.alarm.model.AlarmsService;
import com.better.alarm.model.persistance.AlarmDatabaseHelper;
import com.better.alarm.model.persistance.AlarmProvider;
import com.better.alarm.presenter.AlarmsListFragment;
import com.better.alarm.presenter.DynamicThemeHandler;
import com.better.alarm.presenter.alert.AlarmAlertFullScreen;
import com.better.alarm.presenter.background.KlaxonService;
import com.better.alarm.presenter.background.VibrationService;
import com.github.androidutils.logger.FileLogWriter;
import com.github.androidutils.logger.LogcatLogWriterWithLines;
import com.github.androidutils.logger.Logger;
import com.github.androidutils.logger.Logger.LogLevel;
import com.github.androidutils.logger.LoggingExceptionHandler;
import com.github.androidutils.logger.StartupLogWriter;
import com.github.androidutils.statemachine.StateMachine;
import com.github.androidutils.wakelock.WakeLockManager;

// @formatter:on
@ReportsCrashes(formUri = "https://rt1shnik.cloudant.com/acra-sidonie/_design/acra-storage/_update/report", reportType = Type.JSON, httpMethod = Method.POST, formUriBasicAuthLogin = "andredenesseeileyetedsta", formUriBasicAuthPassword = "rsg4ALpnyvE3T7bXqHjIj7NG", customReportContent = {
        ReportField.APP_VERSION_CODE, ReportField.APP_VERSION_NAME, ReportField.ANDROID_VERSION,
        ReportField.PACKAGE_NAME, ReportField.REPORT_ID, ReportField.BUILD, ReportField.STACK_TRACE }, mode = ReportingInteractionMode.SILENT)
public class AlarmApplication extends Application {

    @Override
    public void onCreate() {
        DynamicThemeHandler.init(this);
        // setTheme(DynamicThemeHandler.getInstance().getIdForName(DynamicThemeHandler.DEFAULT));
        setTheme(DynamicThemeHandler.getInstance().getIdForName("dark"));
        try {
            ViewConfiguration config = ViewConfiguration.get(this);
            Field menuKeyField = ViewConfiguration.class.getDeclaredField("sHasPermanentMenuKey");
            if (menuKeyField != null) {
                menuKeyField.setAccessible(true);
                menuKeyField.setBoolean(config, false);
            }
        } catch (Exception ex) {
            // Ignore
        }

        Logger logger = Logger.getDefaultLogger();
        logger.addLogWriter(LogcatLogWriterWithLines.getInstance());
        logger.addLogWriter(FileLogWriter.getInstance(this, false));
        logger.addLogWriter(StartupLogWriter.getInstance());
        LoggingExceptionHandler.addLoggingExceptionHandlerToAllThreads(logger);

        logger.setLogLevel(WakeLockManager.class, LogLevel.ERR);
        logger.setLogLevel(AlarmDatabaseHelper.class, LogLevel.ERR);
        logger.setLogLevel(AlarmProvider.class, LogLevel.ERR);
        logger.setLogLevel(AlarmsScheduler.class, LogLevel.DBG);
        logger.setLogLevel(AlarmCore.class, LogLevel.DBG);
        logger.setLogLevel(Alarms.class, LogLevel.DBG);
        logger.setLogLevel(StateMachine.class, LogLevel.DBG);
        logger.setLogLevel(AlarmsService.class, LogLevel.DBG);
        logger.setLogLevel(KlaxonService.class, LogLevel.DBG);
        logger.setLogLevel(VibrationService.class, LogLevel.DBG);
        logger.setLogLevel(AlarmsListFragment.class, LogLevel.DBG);
        logger.setLogLevel(AlarmAlertFullScreen.class, LogLevel.DBG);

        WakeLockManager.init(getApplicationContext(), logger, true);
        AlarmsManager.init(getApplicationContext(), logger);

        PreferenceManager.setDefaultValues(this, R.xml.preferences, false);

        logger.d("onCreate");
        super.onCreate();

        ACRA.init(this);
    }

    public static void updateFrLanguage(Context context) {
        Locale locale = Locale.FRANCE;
        Locale.setDefault(locale);
        Configuration config = new Configuration();
        config.locale = locale;
        context.getResources().updateConfiguration(config, null);
    }

    public static List<TextView> udateTypeFace(Context context, ViewGroup containerView) {
        List<TextView> buttons = new ArrayList<TextView>();
        int childCount = containerView.getChildCount();
        for (int i = 0; i < childCount; i++) {
            View view = containerView.getChildAt(i);
            if (view instanceof ViewGroup) {
                buttons.addAll(udateTypeFace(context, (ViewGroup) view));
            }
            if (view instanceof TextView) {
                TextView button = (TextView) view;
                buttons.add(button);
                button.setTypeface(getFontLatoBold(context));
            }
        }
        return buttons;
    }

    public static Typeface getFontLatoBold(Context context) {
        return Typeface.createFromAsset(context.getAssets(), "fonts/Lato-Bold.ttf");
    }

}
