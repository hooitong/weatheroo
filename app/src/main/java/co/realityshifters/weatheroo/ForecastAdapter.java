package co.realityshifters.weatheroo;

import android.content.Context;
import android.support.v7.widget.RecyclerView;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.widget.TextView;

public class ForecastAdapter extends RecyclerView.Adapter<ForecastAdapter.ForecastViewHolder> {

    final private ForecastClickListener mClickListener;

    private String[] mWeatherData;

    public interface ForecastClickListener {
        void onForecastClick(String weather);
    }

    public ForecastAdapter(String[] weatherData, ForecastClickListener listener) {
        mWeatherData = weatherData;
        mClickListener = listener;
    }

    @Override
    public ForecastViewHolder onCreateViewHolder(ViewGroup parent, int viewType) {
        Context context = parent.getContext();
        LayoutInflater inflater = LayoutInflater.from(context);
        View view = inflater.inflate(R.layout.forecast_list_item, parent, false);
        ForecastViewHolder viewHolder = new ForecastViewHolder(view);
        return viewHolder;
    }

    @Override
    public void onBindViewHolder(ForecastViewHolder holder, int position) {
        holder.bind(position);
    }

    @Override
    public int getItemCount() {
        return mWeatherData == null ? 0 : mWeatherData.length;
    }

    public void setWeatherData(String[] weatherData) {
        mWeatherData = weatherData;
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
            mWeatherTextView.setText(mWeatherData[position]);
        }

        @Override
        public void onClick(View v) {
            int adapterPosition = getAdapterPosition();
            String weatherForDay = mWeatherData[adapterPosition];
            mClickListener.onForecastClick(weatherForDay);
        }
    }
}
