package com.shedulerforevents;

import android.content.Context;
import android.content.SharedPreferences;
import android.os.Bundle;
import android.support.annotation.Nullable;
import android.support.v4.app.Fragment;
import android.view.LayoutInflater;
import android.view.View;
import android.view.ViewGroup;
import android.view.WindowManager;
import android.widget.Button;
import android.widget.EditText;
import android.widget.ImageView;
import android.widget.TextView;
import android.widget.Toast;

import com.google.gson.FieldNamingPolicy;
import com.google.gson.Gson;
import com.shedulerforevents.client.ApiService;
import com.shedulerforevents.model.Response;

import java.text.DateFormat;
import java.util.Calendar;
import java.util.Locale;

import io.gsonfire.GsonFireBuilder;
import retrofit2.Call;
import retrofit2.Callback;
import retrofit2.Retrofit;
import retrofit2.converter.gson.GsonConverterFactory;

/**
 * Created by Usuario on 30/10/2016.
 */

public class HomeFragment extends Fragment implements View.OnClickListener {
    private ImageView mIconView;
    private TextView mFriendlyDateView;
    private TextView mDateView;
    private TextView mDescriptionView;
    private TextView mHighTempView;
    private TextView mLowTempView;
    private TextView mHumidityView;
    private TextView mWindView;
    private TextView mPressureView;
    private EditText mSearchText;
    private Button btn_search;

    Calendar calendar;
    ApiService service;
    SharedPreferences preferences;


    @Override
    public void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

    }

    @Nullable
    @Override
    public View onCreateView(LayoutInflater inflater, @Nullable ViewGroup container, @Nullable Bundle savedInstanceState) {

        View view = inflater.inflate(R.layout.fragment_home, container, false);

        preferences = getActivity().getSharedPreferences("sharedPreferences", Context.MODE_PRIVATE);

        mSearchText = (EditText) view.findViewById(R.id.edt_city);
        mIconView = (ImageView) view.findViewById(R.id.detail_icon);
        mDateView = (TextView) view.findViewById(R.id.detail_date_textview);
        mFriendlyDateView = (TextView) view.findViewById(R.id.detail_day_textview);
        mDescriptionView = (TextView) view.findViewById(R.id.detail_forecast_textview);
        mHighTempView = (TextView) view.findViewById(R.id.detail_high_textview);
        mLowTempView = (TextView) view.findViewById(R.id.detail_low_textview);
        mHumidityView = (TextView) view.findViewById(R.id.detail_humidity_textview);
        mWindView = (TextView) view.findViewById(R.id.detail_wind_textview);
        mPressureView = (TextView) view.findViewById(R.id.detail_pressure_textview);
        btn_search = (Button) view.findViewById(R.id.btn_search);
        btn_search.setOnClickListener(this);

        return view;
    }

    @Override
    public void onViewCreated(View view, @Nullable Bundle savedInstanceState) {
        super.onViewCreated(view, savedInstanceState);
        getActivity().getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);
        Gson gson = new GsonFireBuilder().createGsonBuilder().setFieldNamingPolicy(FieldNamingPolicy.LOWER_CASE_WITH_UNDERSCORES).create();
        calendar = Calendar.getInstance();
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl(ApiService.URL)
                .addConverterFactory(GsonConverterFactory.create(gson))
                .build();

        service = retrofit.create(ApiService.class);

        String cityName = preferences.getString("city", "Ourinhos");
        searchData(cityName);
    }

    @Override
    public void onClick(View view) {
        if (mSearchText.getText() != null) {
            preferences.edit().putString("city", mSearchText.getText().toString()).apply();
            searchData(mSearchText.getText().toString());
        } else {
            Toast.makeText(getActivity(), "Digite alguma coisa", Toast.LENGTH_LONG).show();
        }
    }

    private void searchData(String cityName) {
        Call<Response> response = service.getWheater(cityName, "4d8e815ec1e5b235919595b325324eb7", "metric", "pt");
        final Locale locale = new Locale("pt", "Br");
        final DateFormat format = DateFormat.getDateInstance(DateFormat.SHORT, locale);
        response.enqueue(new Callback<Response>() {
            @Override
            public void onResponse(Call<Response> call, retrofit2.Response<Response> response) {

                if (response.isSuccessful()) {
                    mFriendlyDateView.setText("Previsão do tempo para " + response.body().getName());
                    mDateView.setText("no dia " + format.format(calendar.getTime()));
                    mIconView.setImageResource(Util.getArtResourceForWeatherCondition(response.body().getWheater().get(0).getId()));
                    mHighTempView.setText(String.valueOf(response.body().getMain().getTempMax()) + (char) 0x00B0);
                    mLowTempView.setText(String.valueOf(response.body().getMain().getTempMin()) + (char) 0x00B0);
                    mHumidityView.setText("Humidade do ar: " + String.valueOf(response.body().getMain().getHumidity()) + "%");
                    mPressureView.setText("Pressão atmosférica: " + String.valueOf(response.body().getMain().getPressure()));
                    mDescriptionView.setText(response.body().getWheater().get(0).getDescription());
                    mWindView.setText("Velocidade do vento: " + String.valueOf(response.body().getWind().getSpeed()) + "km/h S");

                } else {
                    Toast.makeText(getActivity(), "Erro ao carregar Previsão do tempo!", Toast.LENGTH_LONG).show();
                }
            }

            @Override
            public void onFailure(Call<Response> call, Throwable t) {
                Toast.makeText(getActivity(), "Erro ao carregar Previsão do tempo!", Toast.LENGTH_LONG).show();
            }
        });
    }


}
