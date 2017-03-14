package co.realityshifters.weatheroo.data;

import android.content.Context;
import android.content.SharedPreferences;
import android.support.v7.preference.PreferenceManager;

import co.realityshifters.weatheroo.R;

public class SunshinePreferences {

    /*
     * In order to uniquely pinpoint the location on the map when we launch the map intent, we
     * store the latitude and longitude. We will also use the latitude and longitude to create
     * queries for the weather.
     */
    public static final String PREF_COORD_LAT = "coord_lat";
    public static final String PREF_COORD_LONG = "coord_long";

    /**
     * Helper method to handle setting location details in Preferences (City Name, Latitude,
     * Longitude)
     *
     * @param c   Context used to get the SharedPreferences
     * @param lat The latitude of the city
     * @param lon The longitude of the city
     */
    static public void setLocationDetails(Context c, double lat, double lon) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = sp.edit();

        editor.putLong(PREF_COORD_LAT, Double.doubleToRawLongBits(lat));
        editor.putLong(PREF_COORD_LONG, Double.doubleToRawLongBits(lon));
        editor.apply();
    }

    /**
     * Resets the stored location coordinates.
     *
     * @param c Context used to get the SharedPreferences
     */
    static public void resetLocationCoordinates(Context c) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(c);
        SharedPreferences.Editor editor = sp.edit();

        editor.remove(PREF_COORD_LAT);
        editor.remove(PREF_COORD_LONG);
        editor.apply();
    }

    /**
     * Returns the location currently set in Preferences. The default location this method
     * will return is "94043,USA", which is Mountain View, California. Mountain View is the
     * home of the headquarters of the Googleplex!
     *
     * @param context Context used to get the SharedPreferences
     * @return Location The current user has set in SharedPreferences. Will default to
     * "94043,USA" if SharedPreferences have not been implemented yet.
     */
    public static String getPreferredWeatherLocation(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String locationKey = context.getString(R.string.pref_location_key);
        String locationDefault = context.getString(R.string.pref_location_default);
        return sp.getString(locationKey, locationDefault);
    }

    /**
     * Returns true if the user has selected metric temperature display.
     *
     * @param context Context used to get the SharedPreferences
     * @return true If metric display should be used
     */
    public static boolean isMetric(Context context) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        String unitsKey = context.getString(R.string.pref_units_key);
        String unitsDefault = context.getString(R.string.pref_units_value_celsius);
        String preferredUnits = sp.getString(unitsKey, unitsDefault);
        final String celciusValue = context.getString(R.string.pref_units_value_celsius);
        final String fahrenheitValue = context.getString(R.string.pref_units_value_fahrenheit);
        return preferredUnits.equals(celciusValue);
    }

    /**
     * Returns the location coordinates associated with the location.  Note that these coordinates
     * may not be set, which results in (0,0) being returned. (conveniently, 0,0 is in the middle
     * of the ocean off the west coast of Africa)
     *
     * @param context Used to get the SharedPreferences
     * @return An array containing the two coordinate values.
     */
    public static double[] getLocationCoordinates(Context context) {
        SharedPreferences sp = android.preference.PreferenceManager.getDefaultSharedPreferences(
                context);

        double[] preferredCoordinates = new double[2];

        /*
         * This is a hack we have to resort to since you can't store doubles in SharedPreferences.
         *
         * Double.doubleToLongBits returns an integer corresponding to the bits of the given
         * IEEE 754 double precision value.
         *
         * Double.longBitsToDouble does the opposite, converting a long (that represents a double)
         * into the double itself.
         */
        preferredCoordinates[0] = Double
                .longBitsToDouble(sp.getLong(PREF_COORD_LAT, Double.doubleToRawLongBits(0.0)));
        preferredCoordinates[1] = Double
                .longBitsToDouble(sp.getLong(PREF_COORD_LONG, Double.doubleToRawLongBits(0.0)));

        return preferredCoordinates;
    }

    /**
     * Returns true if the latitude and longitude values are available. The latitude and
     * longitude will not be available until the lesson where the PlacePicker API is taught.
     *
     * @param context used to get the SharedPreferences
     * @return true if lat/long are set
     */
    public static boolean isLocationLatLonAvailable(Context context) {
        SharedPreferences sp = android.preference.PreferenceManager.getDefaultSharedPreferences(
                context);

        boolean spContainLatitude = sp.contains(PREF_COORD_LAT);
        boolean spContainLongitude = sp.contains(PREF_COORD_LONG);

        boolean spContainBothLatitudeAndLongitude = false;
        if (spContainLatitude && spContainLongitude) {
            spContainBothLatitudeAndLongitude = true;
        }

        return spContainBothLatitudeAndLongitude;
    }

    /**
     * Returns the last time that a notification was shown (in UNIX time)
     *
     * @param context Used to access SharedPreferences
     * @return UNIX time of when the last notification was shown
     */
    public static long getLastNotificationTimeInMillis(Context context) {
        /* Key for accessing the time at which Sunshine last displayed a notification */
        String lastNotificationKey = context.getString(R.string.pref_last_notification);

        /* As usual, we use the default SharedPreferences to access the user's preferences */
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);

        /*
         * Here, we retrieve the time in milliseconds when the last notification was shown. If
         * SharedPreferences doesn't have a value for lastNotificationKey, we return 0. The reason
         * we return 0 is because we compare the value returned from this method to the current
         * system time. If the difference between the last notification time and the current time
         * is greater than one day, we will show a notification again. When we compare the two
         * values, we subtract the last notification time from the current system time. If the
         * time of the last notification was 0, the difference will always be greater than the
         * number of milliseconds in a day and we will show another notification.
         */
        long lastNotificationTime = sp.getLong(lastNotificationKey, 0);

        return lastNotificationTime;
    }

    /**
     * Returns the elapsed time in milliseconds since the last notification was shown. This is used
     * as part of our check to see if we should show another notification when the weather is
     * updated.
     *
     * @param context Used to access SharedPreferences as well as use other utility methods
     * @return Elapsed time in milliseconds since the last notification was shown
     */
    public static long getEllapsedTimeSinceLastNotification(Context context) {
        long lastNotificationTimeMillis =
                SunshinePreferences.getLastNotificationTimeInMillis(context);
        long timeSinceLastNotification = System.currentTimeMillis() - lastNotificationTimeMillis;
        return timeSinceLastNotification;
    }

    /**
     * Saves the time that a notification is shown. This will be used to get the ellapsed time
     * since a notification was shown.
     *
     * @param context            Used to access SharedPreferences
     * @param timeOfNotification Time of last notification to save (in UNIX time)
     */
    public static void saveLastNotificationTime(Context context, long timeOfNotification) {
        SharedPreferences sp = PreferenceManager.getDefaultSharedPreferences(context);
        SharedPreferences.Editor editor = sp.edit();
        String lastNotificationKey = context.getString(R.string.pref_last_notification);
        editor.putLong(lastNotificationKey, timeOfNotification);
        editor.apply();
    }
}