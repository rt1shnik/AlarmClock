package com.better.alarm.launcher.view;

import java.lang.reflect.Field;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;

import com.better.alarm.R;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.telephony.SignalStrength;
import android.telephony.TelephonyManager;
import android.util.AttributeSet;
import android.util.TypedValue;
import android.widget.ImageView;
import android.widget.LinearLayout;

public class NetworkLevelTriangleStyle extends LinearLayout {
    public static final int THEME_BLACK = 0;
    public static final int THEME_WHITE = 1;

    private static int BACKGROUND_FOR_0 = R.drawable.n_nonetwork;
    private static int BACKGROUND_FOR_25 = R.drawable.n_25_wh;
    private static int BACKGROUND_FOR_50 = R.drawable.n_50_wh;
    private static int BACKGROUND_FOR_75 = R.drawable.n_75_wh;
    private static int BACKGROUND_FOR_100 = R.drawable.n_100_wh;
    private static int BACKGROUND_FOR_CURR_STATE = BACKGROUND_FOR_0;

    private static int BACKGROUND_FOR_NETWORK_TYPE_2G = R.drawable.n_2g_wh;
    private static int BACKGROUND_FOR_NETWORK_TYPE_3G = R.drawable.n_3g_wh;
    private static int BACKGROUND_FOR_NETWORK_TYPE_4G = R.drawable.n_4g_wh;
    private static int BACKGROUND_FOR_CURR_NETWORK_TYPE = R.drawable.n_2g_wh;

    protected TelephonyManager mTelephonyManager;
    private PhoneStateListener mPhoneStateListener;

    private ImageView networkStrangeView;
    private int strange;
    protected ImageView networkTypeView;

    public NetworkLevelTriangleStyle(Context context) {
        super(context);
        init();
    }

    public NetworkLevelTriangleStyle(Context context, AttributeSet attrs) {
        super(context, attrs);
        init();
    }

    private void init() {
        networkStrangeView = new ImageView(getContext());
        addView(networkStrangeView);

        networkTypeView = new ImageView(getContext());

        // Init layout params(margin)
        LayoutParams params = new LayoutParams(LayoutParams.MATCH_PARENT, LayoutParams.WRAP_CONTENT);
        int lMargin = (int) TypedValue.applyDimension(TypedValue.COMPLEX_UNIT_DIP, 5, getResources()
                .getDisplayMetrics());
        params.setMargins(lMargin, 0, 0, 0);

        addView(networkTypeView, params);

        mPhoneStateListener = createPhoneStateListener();
        mTelephonyManager = (TelephonyManager) getContext().getSystemService(Context.TELEPHONY_SERVICE);
        if (mTelephonyManager == null) {
            BACKGROUND_FOR_CURR_STATE = BACKGROUND_FOR_0;
        } else {
            if (mPhoneStateListener != null) {
                listen(mPhoneStateListener, PhoneStateListener.LISTEN_SIGNAL_STRENGTHS
                        | PhoneStateListener.LISTEN_DATA_CONNECTION_STATE);
            }
        }
    }

    public void listen(PhoneStateListener listener, int events) {
        if (TelephonyInfo.getInstance(getContext()).isDualSIM()) {
            listen(listener, events, getSimId());
        } else {
            mTelephonyManager.listen(listener, events);
        }
    }

    protected int getSimId() {
        return 0;
    }

    public void listen(PhoneStateListener listener, int events, int simId) {
        try {
            Class<?> telephonyClass = Class.forName(TelephonyManager.class.getName());
            Class<?>[] parameter = new Class[3];
            parameter[0] = android.telephony.PhoneStateListener.class;
            parameter[1] = int.class;
            parameter[2] = int.class;

            Method getSimID = null;

            try {
                getSimID = telephonyClass.getMethod("listenGemini", parameter);
            } catch (NoSuchMethodException e) {
                getSimID = telephonyClass.getMethod("listen", parameter);
            }

            Object[] obParameter = new Object[3];
            obParameter[0] = listener;
            obParameter[1] = events;
            obParameter[2] = simId;
            getSimID.invoke(mTelephonyManager, obParameter);

        } catch (ClassNotFoundException | NoSuchMethodException | IllegalAccessException | IllegalArgumentException
                | InvocationTargetException e) {
            e.printStackTrace();
        }
    }

    protected PhoneStateListener createPhoneStateListener() {
        PhoneStateListener phoneStateListener = new MyPhoneStateListenerMultiSim(0);
        return phoneStateListener;
    }

    private void setStrange(int signalStrength) {
        final int maxStrangth = 20;
        final int cellCount = 4;
        strange = signalStrength;
        if (strange == 99 || strange == 0) {
            BACKGROUND_FOR_CURR_STATE = BACKGROUND_FOR_0;
        } else {

            int stangPerCell = maxStrangth / cellCount;
            int fullCellCount = strange / stangPerCell + 1;
            if (fullCellCount > cellCount) {
                fullCellCount = cellCount;
            }
            switch (fullCellCount) {
            case 1:
                BACKGROUND_FOR_CURR_STATE = BACKGROUND_FOR_25;
                break;
            case 2:
                BACKGROUND_FOR_CURR_STATE = BACKGROUND_FOR_50;
                break;
            case 3:
                BACKGROUND_FOR_CURR_STATE = BACKGROUND_FOR_75;
                break;
            case 4:
                BACKGROUND_FOR_CURR_STATE = BACKGROUND_FOR_100;
                break;

            default:
                break;
            }
        }

        updateNetworkClass(getNetworkType());

        updateView();
    }

    private int getNetworkType() {
        TelephonyManager mTelephonyManager = (TelephonyManager) getContext()
                .getSystemService(Context.TELEPHONY_SERVICE);

        int networkType;
        if (TelephonyInfo.getInstance(getContext()).isDualSIM()) {
            int simId = ((MyPhoneStateListenerMultiSim) mPhoneStateListener).getSimId();
            networkType = getNetworkType(simId);
        } else {
            networkType = mTelephonyManager.getNetworkType();
        }

        return networkType;
    }

    private int getNetworkType(int simId) {
        TelephonyManager mTelephonyManager = (TelephonyManager) getContext()
                .getSystemService(Context.TELEPHONY_SERVICE);
        int networkType = 0;
        try {
            Class<?> telephonyClass = Class.forName(TelephonyManager.class.getName());

            Class<?>[] parameter = new Class[1];
            parameter[0] = int.class;
            Method getSimStateGemini = null;
            try {
                getSimStateGemini = telephonyClass.getMethod("getNetworkTypeGemini", parameter);
            } catch (NoSuchMethodException e) {
                getSimStateGemini = telephonyClass.getMethod("getNetworkType", parameter);
            }
            Object[] obParameter = new Object[1];
            obParameter[0] = simId;
            Object ob_phone = getSimStateGemini.invoke(mTelephonyManager, obParameter);
            if (ob_phone != null) {
                networkType = (int) ob_phone;
            }
        } catch (IllegalAccessException e1) {
            e1.printStackTrace();
        } catch (IllegalArgumentException e1) {
            e1.printStackTrace();
        } catch (InvocationTargetException e1) {
            e1.printStackTrace();
        } catch (NoSuchMethodException e1) {
            e1.printStackTrace();
        } catch (ClassNotFoundException e1) {
            e1.printStackTrace();
        }
        return networkType;
    }

    private void updateNetworkClass(int networkType) {
        switch (networkType) {
        case TelephonyManager.NETWORK_TYPE_GPRS:
        case TelephonyManager.NETWORK_TYPE_EDGE:
        case TelephonyManager.NETWORK_TYPE_CDMA:
        case TelephonyManager.NETWORK_TYPE_1xRTT:
        case TelephonyManager.NETWORK_TYPE_IDEN:
            BACKGROUND_FOR_CURR_NETWORK_TYPE = BACKGROUND_FOR_NETWORK_TYPE_2G;
            break;
        case TelephonyManager.NETWORK_TYPE_UMTS:
        case TelephonyManager.NETWORK_TYPE_EVDO_0:
        case TelephonyManager.NETWORK_TYPE_EVDO_A:
        case TelephonyManager.NETWORK_TYPE_HSDPA:
        case TelephonyManager.NETWORK_TYPE_HSUPA:
        case TelephonyManager.NETWORK_TYPE_HSPA:
        case TelephonyManager.NETWORK_TYPE_EVDO_B:
        case TelephonyManager.NETWORK_TYPE_EHRPD:
        case TelephonyManager.NETWORK_TYPE_HSPAP:
            BACKGROUND_FOR_CURR_NETWORK_TYPE = BACKGROUND_FOR_NETWORK_TYPE_3G;
            break;
        case TelephonyManager.NETWORK_TYPE_LTE:
            BACKGROUND_FOR_CURR_NETWORK_TYPE = BACKGROUND_FOR_NETWORK_TYPE_4G;
            break;
        default:
            BACKGROUND_FOR_CURR_STATE = BACKGROUND_FOR_0;
            networkTypeView.setVisibility(GONE);
        }
    }

    class MyPhoneStateListenerMultiSim extends PhoneStateListener {

        private final long mSimId;

        @Override
        public void onSignalStrengthsChanged(SignalStrength signalStrength) {
            super.onSignalStrengthsChanged(signalStrength);
            System.out.println(signalStrength.getGsmSignalStrength());

            setStrange(signalStrength.getGsmSignalStrength());
        }

        @Override
        public void onDataConnectionStateChanged(int state) {
            super.onDataConnectionStateChanged(state);
            if (state == -1) return;

            if (state == TelephonyManager.DATA_CONNECTED || state == TelephonyManager.DATA_CONNECTING) {
                updateNetworkClass(getNetworkType());
                networkTypeView.setVisibility(VISIBLE);
            } else {
                networkTypeView.setVisibility(GONE);
            }
        }

        public MyPhoneStateListenerMultiSim(long simId) {
            super();
            mSimId = simId;
            if (simId > 0) {
                updateSimNum(simId);
            }
        }

        public int getSimId() {
            return (int) mSimId;
        }

        private boolean updateSimNum(long simId) {

            try {
                Class<?> c = Class.forName(TelephonyManager.class.getName());
                Field nameField = c.getField("mSubId");
                nameField.set(this, simId);
            } catch (IllegalArgumentException e) {
                e.printStackTrace();
                return false;
            } catch (ClassNotFoundException e) {
                e.printStackTrace();
            } catch (IllegalAccessException e) {
                e.printStackTrace();
            } catch (NoSuchFieldException e) {
                e.printStackTrace();
            }

            return true;
        }
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

        setStrange(strange);
        updateView();
    }

    // public boolean activeSim() {
    // TelephonyInfo info = TelephonyInfo.getInstance(getContext());
    // MyPhoneStateListenerMultiSim myPhoneStateListenerMultiSim =
    // (MyPhoneStateListenerMultiSim) mPhoneStateListener;
    // int simId = myPhoneStateListenerMultiSim.getSimId();
    // boolean sim1IsActive = info.isDualSIM() == true && info.isSIM1Ready() ==
    // true && simId == 0;
    // boolean sim2IsActive = info.isDualSIM() == true && info.isSIM2Ready() ==
    // true && simId == 1;
    // boolean activeSim = sim1IsActive | sim2IsActive;
    // return activeSim;
    // }

    private void setWhiteTheme() {
        BACKGROUND_FOR_100 = R.drawable.n_100_wh;
        BACKGROUND_FOR_75 = R.drawable.n_75_wh;
        BACKGROUND_FOR_50 = R.drawable.n_50_wh;
        BACKGROUND_FOR_25 = R.drawable.n_25_wh;
        BACKGROUND_FOR_0 = R.drawable.n_nonetwork;

        BACKGROUND_FOR_NETWORK_TYPE_2G = R.drawable.n_2g_wh;
        BACKGROUND_FOR_NETWORK_TYPE_3G = R.drawable.n_3g_wh;
        BACKGROUND_FOR_NETWORK_TYPE_4G = R.drawable.n_4g_wh;

        updateView();
    }

    private void setBlackTheme() {
        BACKGROUND_FOR_100 = R.drawable.n_100_;
        BACKGROUND_FOR_75 = R.drawable.n_75_;
        BACKGROUND_FOR_50 = R.drawable.n_50_;
        BACKGROUND_FOR_25 = R.drawable.n_25_;
        BACKGROUND_FOR_0 = R.drawable.n_nonetwork;

        BACKGROUND_FOR_NETWORK_TYPE_2G = R.drawable.n_2g_bk;
        BACKGROUND_FOR_NETWORK_TYPE_3G = R.drawable.n_3g_bk;
        BACKGROUND_FOR_NETWORK_TYPE_4G = R.drawable.n_4g_bk;

        updateView();
    }

    protected void updateView() {
        if (TelephonyInfo.getInstance(getContext()).isDualSIM() == false) {
            if (getSimId() != 0) {
                networkStrangeView.setVisibility(GONE);
                networkTypeView.setVisibility(GONE);
                return;
            }
        }
        networkStrangeView.setImageResource(BACKGROUND_FOR_CURR_STATE);
        networkTypeView.setImageResource(BACKGROUND_FOR_CURR_NETWORK_TYPE);
    }

}
