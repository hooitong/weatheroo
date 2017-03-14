package co.realityshifters.weatheroo;

import android.content.Intent;
import android.database.Cursor;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.CursorLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;

import co.realityshifters.weatheroo.data.SunshinePreferences;
import co.realityshifters.weatheroo.data.WeatherContract;
import co.realityshifters.weatheroo.utilities.FakeDataUtils;

public class MainActivity extends AppCompatActivity implements
        ForecastAdapter.ForecastClickListener, LoaderManager.LoaderCallbacks<Cursor> {

    private static final String TAG = MainActivity.class.getSimpleName();

    private ProgressBar mProgressBar;

    private RecyclerView mRecyclerView;
    private ForecastAdapter mForecastAdapter;
    private int mPosition = RecyclerView.NO_POSITION;

    private static final int ID_FORECAST_LOADER = 22;

    public static final String[] MAIN_FORECAST_PROJECTION = {
            WeatherContract.WeatherEntry.COLUMN_DATE,
            WeatherContract.WeatherEntry.COLUMN_MAX_TEMP,
            WeatherContract.WeatherEntry.COLUMN_MIN_TEMP,
            WeatherContract.WeatherEntry.COLUMN_WEATHER_ID,
    };

    public static final int INDEX_WEATHER_DATE = 0;
    public static final int INDEX_WEATHER_MAX_TEMP = 1;
    public static final int INDEX_WEATHER_MIN_TEMP = 2;
    public static final int INDEX_WEATHER_CONDITION_ID = 3;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        FakeDataUtils.insertFakeData(this);

        mProgressBar = (ProgressBar) findViewById(R.id.pg_loading_progress);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_forecast);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mForecastAdapter = new ForecastAdapter(this, this);
        mRecyclerView.setAdapter(mForecastAdapter);

        showLoading();

        getSupportLoaderManager().initLoader(ID_FORECAST_LOADER, null, this);
    }

    @Override
    public boolean onCreateOptionsMenu(Menu menu) {
        MenuInflater inflater = getMenuInflater();
        inflater.inflate(R.menu.forecast, menu);
        return true;
    }

    @Override
    public boolean onOptionsItemSelected(MenuItem item) {
        switch (item.getItemId()) {
            case R.id.action_settings:
                Intent startSettingsActivity = new Intent(this, SettingsActivity.class);
                startActivity(startSettingsActivity);
                return true;
            case R.id.action_map:
                openLocationInMap();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void openLocationInMap() {
        double[] coords = SunshinePreferences.getLocationCoordinates(this);
        String posLat = Double.toString(coords[0]);
        String posLong = Double.toString(coords[1]);
        Uri geoLocation = Uri.parse("geo:" + posLat + "," + posLong);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't open " + geoLocation.toString() + ".");
        }
    }

    @Override
    public void onForecastClick(long date) {
        Intent detailIntent = new Intent(this, DetailActivity.class);
        Uri uriForDateClicked = WeatherContract.WeatherEntry.buildWeatherUriWithDate(date);
        detailIntent.setData(uriForDateClicked);
        startActivity(detailIntent);

    }

    private void showWeatherDataView() {
        mRecyclerView.setVisibility(View.VISIBLE);
        mProgressBar.setVisibility(View.INVISIBLE);
    }

    private void showLoading() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mProgressBar.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<Cursor> onCreateLoader(int id, final Bundle args) {
        switch (id) {
            case ID_FORECAST_LOADER:
                Uri forecastQueryUri = WeatherContract.WeatherEntry.CONTENT_URI;
                String sortOrder = WeatherContract.WeatherEntry.COLUMN_DATE + " ASC";
                String selection = WeatherContract.WeatherEntry.getSqlSelectForTodayOnwards();

                return new CursorLoader(this,
                        forecastQueryUri,
                        MAIN_FORECAST_PROJECTION,
                        selection,
                        null,
                        sortOrder
                );
            default:
                throw new RuntimeException("Loader not implemented: " + id);
        }
    }

    @Override
    public void onLoadFinished(Loader<Cursor> loader, Cursor data) {
        mForecastAdapter.swapCursor(data);
        if (mPosition == RecyclerView.NO_POSITION) mPosition = 0;
        mRecyclerView.smoothScrollToPosition(mPosition);
        if (data.getCount() != 0) showWeatherDataView();
    }

    @Override
    public void onLoaderReset(Loader<Cursor> loader) {
        mForecastAdapter.swapCursor(null);
    }
}
