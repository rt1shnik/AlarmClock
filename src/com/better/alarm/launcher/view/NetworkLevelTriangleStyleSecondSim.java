package com.better.alarm.launcher.view;

import android.content.Context;
import android.telephony.PhoneStateListener;
import android.util.AttributeSet;

public class NetworkLevelTriangleStyleSecondSim extends NetworkLevelTriangleStyle {

	public NetworkLevelTriangleStyleSecondSim(Context context) {
		super(context);
	}
	
	public NetworkLevelTriangleStyleSecondSim(Context context, AttributeSet attrs) {
		super(context, attrs);
	}
	
	@Override
	protected PhoneStateListener createPhoneStateListener() {
		PhoneStateListener phoneStateListener = new MyPhoneStateListenerMultiSim(1);
		return phoneStateListener;
	}
	
	protected int getSimId() {
		return 1;
	}
}
