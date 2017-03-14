package co.realityshifters.weatheroo;

import android.content.Intent;
import android.content.SharedPreferences;
import android.net.Uri;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.preference.PreferenceManager;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.util.Log;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;

import java.net.URL;

import co.realityshifters.weatheroo.data.SunshinePreferences;
import co.realityshifters.weatheroo.utilities.NetworkUtils;
import co.realityshifters.weatheroo.utilities.OpenWeatherJsonUtils;

public class MainActivity extends AppCompatActivity implements
        ForecastAdapter.ForecastClickListener, LoaderManager.LoaderCallbacks<String[]>,
        SharedPreferences.OnSharedPreferenceChangeListener {

    private static final String TAG = MainActivity.class.getSimpleName();

    private TextView mErrorMessage;
    private ProgressBar mProgressBar;

    private RecyclerView mRecyclerView;
    private ForecastAdapter mForecastAdapter;

    private static final int FORECAST_LOADER_ID = 22;

    private static boolean PREFERENCE_UPDATED = false;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_forecast);

        mRecyclerView = (RecyclerView) findViewById(R.id.rv_forecast);
        RecyclerView.LayoutManager layoutManager = new LinearLayoutManager(this,
                LinearLayoutManager.VERTICAL, false);
        mRecyclerView.setLayoutManager(layoutManager);
        mRecyclerView.setHasFixedSize(true);

        mForecastAdapter = new ForecastAdapter(null, this);
        mRecyclerView.setAdapter(mForecastAdapter);

        mErrorMessage = (TextView) findViewById(R.id.tv_error_message);

        mProgressBar = (ProgressBar) findViewById(R.id.pg_loading_progress);

        getSupportLoaderManager().initLoader(FORECAST_LOADER_ID, null, this);

        PreferenceManager
                .getDefaultSharedPreferences(this)
                .registerOnSharedPreferenceChangeListener(this);
    }

    @Override
    protected void onStart() {
        super.onStart();
        if (PREFERENCE_UPDATED) {
            getSupportLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
            PREFERENCE_UPDATED = false;
        }
    }

    @Override
    protected void onDestroy() {
        super.onDestroy();
        PreferenceManager
                .getDefaultSharedPreferences(this)
                .unregisterOnSharedPreferenceChangeListener(this);
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
            case R.id.action_refresh:
                invalidateData();
                getSupportLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
                return true;
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
        String addressString = SunshinePreferences.getPreferredWeatherLocation(this);
        Uri geoLocation = Uri.parse("geo:0,0?q=" + addressString);

        Intent intent = new Intent(Intent.ACTION_VIEW);
        intent.setData(geoLocation);

        if (intent.resolveActivity(getPackageManager()) != null) {
            startActivity(intent);
        } else {
            Log.d(TAG, "Couldn't open " + geoLocation.toString() + ".");
        }
    }

    @Override
    public void onForecastClick(String weather) {
        Intent detailIntent = new Intent(this, DetailActivity.class);
        detailIntent.putExtra(Intent.EXTRA_TEXT, weather);
        startActivity(detailIntent);

    }

    private void showWeatherDataView() {
        mErrorMessage.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    @Override
    public Loader<String[]> onCreateLoader(int id, final Bundle args) {
        return new AsyncTaskLoader<String[]>(this) {
            private String[] mWeatherData = null;

            @Override
            protected void onStartLoading() {
                if (mWeatherData == null) {
                    mProgressBar.setVisibility(View.VISIBLE);
                    forceLoad();
                } else {
                    deliverResult(mWeatherData);
                }
            }

            @Override
            public String[] loadInBackground() {
                URL weatherRequest = NetworkUtils.getUrl(MainActivity.this);

                try {
                    String response = NetworkUtils.getResponseFromHttpUrl(weatherRequest);
                    String[] jsonData = OpenWeatherJsonUtils.getSimpleWeatherStringsFromJson(
                            MainActivity.this, response);
                    return jsonData;
                } catch (Exception e) {
                    e.printStackTrace();
                    return null;
                }
            }

            @Override
            public void deliverResult(String[] data) {
                mWeatherData = data;
                super.deliverResult(data);
            }
        };
    }

    @Override
    public void onLoadFinished(Loader<String[]> loader, String[] data) {
        mProgressBar.setVisibility(View.INVISIBLE);
        mForecastAdapter.setWeatherData(data);
        if (data != null) {
            showWeatherDataView();
        } else {
            showErrorMessage();
        }
    }

    @Override
    public void onLoaderReset(Loader<String[]> loader) {

    }

    private void invalidateData() {
        mForecastAdapter.setWeatherData(null);
    }

    @Override
    public void onSharedPreferenceChanged(SharedPreferences sharedPreferences, String key) {
        PREFERENCE_UPDATED = true;
    }
}
