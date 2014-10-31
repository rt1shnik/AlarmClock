package com.github.androidutils.logger;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.CopyOnWriteArrayList;

import android.util.Log;

public class Logger {

    private static final String TAG = Logger.class.getSimpleName();

    private static final boolean DBG = false;

    public enum LogLevel {
        ERR, WRN, DBG, INF
    }

    /**
     * Log writing strategy
     * 
     * @author Yuriy
     * 
     */
    public interface LogWriter {

        public void write(LogLevel level, String tag, String message);

        public void write(LogLevel level, String tag, String message, Throwable e);
    }

    private final Map<String, LogLevel> mLogLevels;

    private static volatile Logger sInstance;

    public static synchronized Logger getDefaultLogger() {
        if (sInstance == null) {
            sInstance = new Logger();
        }
        return sInstance;
    }

    @Deprecated
    public static synchronized Logger init() {
        if (sInstance == null) {
            sInstance = new Logger();
        }
        return sInstance;
    }

    private final CopyOnWriteArrayList<LogWriter> writers;

    private Logger() {
        mLogLevels = new ConcurrentHashMap<String, Logger.LogLevel>();
        writers = new CopyOnWriteArrayList<LogWriter>();
    }

    public void addLogWriter(LogWriter logWriter) {
        writers.addIfAbsent(logWriter);
    }

    public void removeLogWriter(LogWriter logWriter) {
        writers.remove(logWriter);
    }

    /**
     * For a given logClass only messages with logLevel above will be logged.
     * 
     * @param logClass
     * @param logLevel
     */
    public void setLogLevel(Class<?> logClass, LogLevel logLevel) {
        final String simpleName = logClass.getSimpleName();
        mLogLevels.put(simpleName, logLevel);
        if (DBG) {
            final String string = "Adding " + simpleName + " with LogLevel " + logLevel.toString();
            Log.d(TAG, string);
        }
    }

    /**
     * For a given simple name only messages with logLevel above will be logged.
     * 
     * @param logClass
     * @param logLevel
     */
    public void setLogLevel(String simpleName, LogLevel logLevel) {
        mLogLevels.put(simpleName, logLevel);
        final String string = "Adding " + simpleName + " with LogLevel " + logLevel.toString();
        Log.d(TAG, string);
    }

    public LogLevel getLevel(Class<?> logClass) {
        return mLogLevels.get(logClass.getSimpleName());
    }

    /**
     * Logs the message if configured log level for the class is above requested
     * log level. If configured {@link LogLevel} is {@link LogLevel#WRN}, only
     * logs with {@link LogLevel#ERR} and {@link LogLevel#WRN} will be shown.
     * 
     * @param logLevel
     * @param message
     */
    public void log(LogLevel logLevel, String message) {
        logIfApplicable(logLevel, message, null);
    }

    private void logIfApplicable(LogLevel logLevel, String message, Throwable throwable) {
        final StackTraceElement caller = Thread.currentThread().getStackTrace()[4];
        final String fileName = caller.getFileName();
        final String logClass = fileName.substring(0, fileName.length() - 5);

        LogLevel configuredLogLevel = mLogLevels.get(logClass);

        if (configuredLogLevel == null) {
            configuredLogLevel = LogLevel.DBG;
            mLogLevels.put(logClass, configuredLogLevel);
            final String string = "no LogLevel was found for " + logClass;
            Log.w(TAG, string);
            final String string2 = "Adding " + logClass + " with LogLevel " + configuredLogLevel.toString();
            Log.d(TAG, string2);
        }
        final boolean shouldBeLogged = logLevel.ordinal() <= configuredLogLevel.ordinal();
        if (shouldBeLogged) {
            final String formatTag = formatTag();
            for (final LogWriter writer : writers) {
                writer.write(logLevel, formatTag, message, throwable);
            }
        }
    }

    public void d(String message) {
        sInstance.logIfApplicable(LogLevel.DBG, message, null);
    }

    public void w(String message) {
        sInstance.logIfApplicable(LogLevel.WRN, message, null);
    }

    public void e(String message) {
        sInstance.logIfApplicable(LogLevel.ERR, message, null);
    }

    public void e(String message, Throwable throwable) {
        sInstance.logIfApplicable(LogLevel.ERR, message, throwable);
    }

    private String formatTag() {
        final StackTraceElement caller = Thread.currentThread().getStackTrace()[5];
        final String fileName = caller.getFileName();
        final String logClass = fileName.substring(0, fileName.length() - 5);
        final String methodName = caller.getMethodName();
        final String tag = "[" + logClass + "." + methodName + "]";
        return tag;
    }
}
