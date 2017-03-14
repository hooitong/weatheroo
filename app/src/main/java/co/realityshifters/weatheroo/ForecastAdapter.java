package co.realityshifters.weatheroo;

import android.content.Context;
import android.database.Cursor;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

import co.realityshifters.weatheroo.utilities.SunshineDateUtils;
import co.realityshifters.weatheroo.utilities.SunshineWeatherUtils;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    final private ForecastClickListener mClickListener;

    private final Context mContext;

    private Cursor mCursor;

    public interface ForecastClickListener {
        void onForecastClick(long date);
    }

    public ForecastAdapter(Context context, ForecastClickListener listener) {
        mContext = context;
        mClickListener = listener;
    }

    @Override
    public ForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        LayoutInflater inflater = LayoutInflater.from(mContext);
        View view = inflater.inflate(R.layout.forecast_list_item, parent, false);
        view.setFocusable(true);
        ForecastViewHolder viewHolder = new ForecastViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ForecastViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mCursor == null ? 0 : mCursor.getCount();
    }

    public void swapCursor(Cursor cursor) {
        mCursor = cursor;
        notifyDataSetChanged();
    }

    class ForecastViewHolder extends RecyclerView.ViewHolder implements View.OnClickListener {
        public final TextView mWeatherTextView;

        public ForecastViewHolder(View itemView) {
            super(itemView);
            mWeatherTextView = (TextView) itemView.findViewById(R.id.tv_weather_data);
            itemView.setOnClickListener(this);
        }

        void bind(int position) {
            if (mCursor.moveToPosition(position)) {
                long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
                String dateString = SunshineDateUtils.getFriendlyDateString(mContext, dateInMillis,
                        false);
                int weatherId = mCursor.getInt(MainActivity.INDEX_WEATHER_CONDITION_ID);
                String description = SunshineWeatherUtils.getStringForWeatherCondition(mContext,
                        weatherId);
                double highInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MAX_TEMP);
                double lowInCelsius = mCursor.getDouble(MainActivity.INDEX_WEATHER_MIN_TEMP);
                String highAndLowTemperature =
                        SunshineWeatherUtils.formatHighLows(mContext, highInCelsius, lowInCelsius);
                String weatherSummary =
                        dateString + " - " + description + " - " + highAndLowTemperature;
                mWeatherTextView.setText(weatherSummary);
            }
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            mCursor.moveToPosition(adapterPosition);
            long dateInMillis = mCursor.getLong(MainActivity.INDEX_WEATHER_DATE);
            mClickListener.onForecastClick(dateInMillis);
        }
    }
}
