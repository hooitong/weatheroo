package co.realityshifters.weatheroo.data;

import android.content.Context;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;

import co.realityshifters.weatheroo.data.WeatherContract.WeatherEntry;

public class WeatherDBHelper extends SQLiteOpenHelper {

    private final static String DB_NAME = "weather.db";
    private final static int DB_VERSION = 4;

    public WeatherDBHelper(Context context) {
        super(context, DB_NAME, null, DB_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        final String SQL_CREATE_WEATHER_TABLE = "CREATE TABLE " + WeatherEntry.TABLE_NAME + " (" +
                WeatherEntry._ID + " INTEGER PRIMARY KEY AUTOINCREMENT," +
                WeatherEntry.COLUMN_WEATHER_ID + " INTEGER NOT NULL," +
                WeatherEntry.COLUMN_DATE + "INTEGER NOT NULL," +

                WeatherEntry.COLUMN_MAX_TEMP + " REAL NOT NULL," +
                WeatherEntry.COLUMN_MIN_TEMP + " REAL NOT NULL," +

                WeatherEntry.COLUMN_PRESSURE + " REAL NOT NULL," +
                WeatherEntry.COLUMN_HUMIDITY + " REAL NOT NULL," +
                WeatherEntry.COLUMN_WIND_SPEED + " REAL NOT NULL," +
                WeatherEntry.COLUMN_DEGREES + " REAL NOT NULL" +
                " UNIQUE (" + WeatherEntry.COLUMN_DATE + ") ON CONFLICT REPLACE);";

        db.execSQL(SQL_CREATE_WEATHER_TABLE);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        final String SQL_DROP_WEATHER_TABLE = "DROP TABLE IF EXISTS " + WeatherEntry.TABLE_NAME;
        db.execSQL(SQL_DROP_WEATHER_TABLE);
        onCreate(db);
    }
}
