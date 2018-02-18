package ru.kuchanov.rate;

import android.content.Context;
import android.content.SharedPreferences;

class TimeSettings {
    private static final long FIRST_SHOW_INTERVALE = 2 * 24 * 60 * 60 * 1000;
    private static final long SHOW_LATER_INTERVALE = 7 * 24 * 60 * 60 * 1000;
//    private static final long FIRST_SHOW_INTERVALE = 1000;
//    private static final long SHOW_LATER_INTERVALE = 1000;

    private final static String KEY_FIRST_START_TIME = "firstRunTime";
    private final static String KEY_LAST_SHOW_TIME = "last_show_time";

    private final static String KEY_SHOW_MODE = "show_mode";
    private final static int SHOW = 0;
    final static int SHOW_LATER = 1;
    final static int NOT_SHOW = 2;

    static void setFirstStartTime(final Context context) {
        if (getStartTime(context) == 0) {
            final SharedPreferences.Editor editor = getPrefs(context).edit();
            editor.putLong(KEY_FIRST_START_TIME, System.currentTimeMillis());
            editor.apply();
        }
    }

    static boolean needShowPreRateDialog(final Context context) {
        final int showMode = getShowMode(context);
        if (showMode == SHOW) {
            final long firstTimeStart = getStartTime(context);
            if (firstTimeStart != 0) {
                return (System.currentTimeMillis() - firstTimeStart) > FIRST_SHOW_INTERVALE;
            }
        } else if (showMode == SHOW_LATER) {
            final long lastShowTime = getLastShowTime(context);
            return (System.currentTimeMillis() - lastShowTime) > SHOW_LATER_INTERVALE;
        }
        return false;
    }

    private static long getStartTime(final Context context) {
        return getPrefs(context).getLong(KEY_FIRST_START_TIME, 0);
    }

    private static long getLastShowTime(final Context context) {
        return getPrefs(context).getLong(KEY_LAST_SHOW_TIME, 0);
    }

    static void saveLastShowTime(final Context context) {
        final SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putLong(KEY_LAST_SHOW_TIME, System.currentTimeMillis());
        editor.apply();
    }

    private static int getShowMode(final Context context) {
        return getPrefs(context).getInt(KEY_SHOW_MODE, SHOW);
    }

    static void setShowMode(final Context context, final int showMode) {
        final SharedPreferences.Editor editor = getPrefs(context).edit();
        editor.putInt(KEY_SHOW_MODE, showMode);
        editor.apply();
    }

    private static SharedPreferences getPrefs(final Context context) {
        return context.getSharedPreferences(context.getPackageName(), Context.MODE_PRIVATE);
    }
}