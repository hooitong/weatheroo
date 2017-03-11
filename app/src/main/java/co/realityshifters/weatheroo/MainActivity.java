package co.realityshifters.weatheroo;

import android.content.Intent;
import android.os.Bundle;
import android.support.v4.app.LoaderManager;
import android.support.v4.content.AsyncTaskLoader;
import android.support.v4.content.Loader;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
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
        ForecastAdapter.ForecastClickListener, LoaderManager.LoaderCallbacks<String[]> {

    private static final int FORECAST_LOADER_ID = 22;
    private static final String LOCATION_QUERY_EXTRA = "query";

    private TextView mErrorMessage;
    private ProgressBar mProgressBar;

    private RecyclerView mRecyclerView;
    private ForecastAdapter mForecastAdapter;

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
                mForecastAdapter.setWeatherData(null);
                getSupportLoaderManager().restartLoader(FORECAST_LOADER_ID, null, this);
                return true;
            default:
                return super.onOptionsItemSelected(item);
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
                String locationQuery = SunshinePreferences.getPreferredWeatherLocation(
                        MainActivity.this);
                URL weatherRequest = NetworkUtils.buildUrl(locationQuery);

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
}
