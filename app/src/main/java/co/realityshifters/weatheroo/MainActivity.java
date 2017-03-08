package co.realityshifters.weatheroo;

import android.os.AsyncTask;
import android.os.Bundle;
import android.support.v7.app.AppCompatActivity;
import android.support.v7.widget.LinearLayoutManager;
import android.support.v7.widget.RecyclerView;
import android.view.Menu;
import android.view.MenuInflater;
import android.view.MenuItem;
import android.view.View;
import android.widget.ProgressBar;
import android.widget.TextView;
import android.widget.Toast;

import java.net.URL;

import co.realityshifters.weatheroo.data.SunshinePreferences;
import co.realityshifters.weatheroo.utilities.NetworkUtils;
import co.realityshifters.weatheroo.utilities.OpenWeatherJsonUtils;

public class MainActivity extends AppCompatActivity implements
        ForecastAdapter.ForecastClickListener {

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
        loadWeatherData();
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
                loadWeatherData();
                return true;
            default:
                return super.onOptionsItemSelected(item);
        }
    }

    private void loadWeatherData() {
        String location = SunshinePreferences.getPreferredWeatherLocation(this);
        new FetchWeatherTask().execute(location);
    }

    @Override
    public void onForecastClick(String weather) {
        Toast.makeText(this, weather, Toast.LENGTH_SHORT).show();
    }

    private void showWeatherDataView() {
        mErrorMessage.setVisibility(View.INVISIBLE);
        mRecyclerView.setVisibility(View.VISIBLE);
    }

    private void showErrorMessage() {
        mRecyclerView.setVisibility(View.INVISIBLE);
        mErrorMessage.setVisibility(View.VISIBLE);
    }

    public class FetchWeatherTask extends AsyncTask<String, Void, String[]> {
        @Override
        protected void onPreExecute() {
            super.onPreExecute();
            mProgressBar.setVisibility(View.VISIBLE);
        }

        @Override
        protected String[] doInBackground(String... params) {
            if (params.length == 0) return null;

            String location = params[0];
            URL weatherRequest = NetworkUtils.buildUrl(location);

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
        protected void onPostExecute(String[] weatherData) {
            super.onPostExecute(weatherData);
            mProgressBar.setVisibility(View.INVISIBLE);
            if (weatherData != null) {
                showWeatherDataView();
                mForecastAdapter.setWeatherData(weatherData);
            } else {
                showErrorMessage();
            }
        }
    }
}
