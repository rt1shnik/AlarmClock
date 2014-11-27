package com.better.alarm.presenter;

import android.app.ActionBar;
import android.app.Activity;
import android.content.Context;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;

import com.better.alarm.R;
import com.google.common.base.Preconditions;

/**
 * This class handles options menu and action bar
 *
 * @author Kate
 *
 */
public class ActionBarHandler {

    private static final int JELLY_BEAN_MR1 = 17;
    private final Context mContext;

    /**
     * @param cmContext
     */
    public ActionBarHandler(Context context) {
        this.mContext = Preconditions.checkNotNull(context);
    }

    /**
     * Delegate {@link Activity#onCreateOptionsMenu(Menu)}
     *
     * @param menu
     * @param inflater
     * @param actionBar
     * @return
     */
    public boolean onCreateOptionsMenu(Menu menu, MenuInflater inflater, ActionBar actionBar) {
        inflater.inflate(R.menu.settings_menu, menu);
        return true;
    }

    /**
     * Delegate {@link Activity#onOptionsItemSelected(MenuItem)}
     *
     * @param item
     * @return
     */
    public boolean onOptionsItemSelected(MenuItem item) {
        // switch (item.getItemId()) {

        // case R.id.menu_item_settings:
        // // TODO show details
        // mContext.startActivity(new Intent(mContext, SettingsActivity.class));
        // return true;
        // }
        return false;
    }

}
