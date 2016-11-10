package com.shedulerforevents;

import android.Manifest;
import android.app.AlarmManager;
import android.app.DatePickerDialog;
import android.app.Dialog;
import android.app.PendingIntent;
import android.app.TimePickerDialog;

import android.content.Context;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.os.Build;
import android.support.v4.app.ActivityCompat;
import android.support.v4.app.DialogFragment;
import android.support.v4.content.ContextCompat;
import android.support.v7.app.AppCompatActivity;
import android.os.Bundle;
import android.support.v7.widget.Toolbar;
import android.text.TextUtils;
import android.text.format.DateFormat;
import android.view.View;
import android.view.WindowManager;
import android.widget.DatePicker;
import android.widget.EditText;
import android.widget.TextView;
import android.widget.TimePicker;
import android.widget.Toast;

import com.google.android.gms.common.GooglePlayServicesNotAvailableException;
import com.google.android.gms.common.GooglePlayServicesRepairableException;
import com.google.android.gms.location.places.Place;
import com.google.android.gms.location.places.ui.PlacePicker;
import com.shedulerforevents.model.Event;

import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Locale;

/**
 * Created by Usuario on 30/10/2016.
 */

public class NewEventActivity extends AppCompatActivity implements TimePickerDialog.OnTimeSetListener,
        DatePickerDialog.OnDateSetListener {

    private static final int PLACE_PICKER_REQUEST = 1;
    private DataBaseHelper helper;
    private EventDAO eventDao;
    private Event event;
    private TextView textViewDate;
    private TextView textViewHour;
    private TextView textViewAddress;
    private EditText editTextTitle;
    private EditText editTextDescription;
    private Date eventDate;
    private Calendar calendar;
    private Place place;


    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        this.getWindow().setSoftInputMode(WindowManager.LayoutParams.SOFT_INPUT_STATE_ALWAYS_HIDDEN);

        setContentView(R.layout.activity_new_event);
        Toolbar toolbar = (Toolbar)  findViewById(R.id.toolbar);
        setSupportActionBar(toolbar);
        getSupportActionBar().setDisplayHomeAsUpEnabled(true);

        event = new Event();

        helper = new DataBaseHelper(this);
        helper.getWritableDatabase();

        editTextTitle = (EditText) findViewById(R.id.event_title);
        editTextDescription = (EditText) findViewById(R.id.event_description);
        textViewDate = (TextView) findViewById(R.id.event_date);
        textViewHour = (TextView) findViewById(R.id.event_hour);
        textViewAddress = (TextView) findViewById(R.id.event_address);

        calendar = Calendar.getInstance();

        try {
            eventDao = new EventDAO(helper.getConnectionSource());

            clearFields();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }

    private void clearFields() {
        editTextTitle.getText().clear();
        editTextDescription.getText().clear();
        textViewDate.setText("Data");
        textViewHour.setText("Hora");
        textViewAddress.setText("");
    }

    public void selectDate(View view) {
        DialogFragment dateFragment = new DatePickerFragment();
        dateFragment.show(getSupportFragmentManager(), "DatePicker");
    }

    public void selectTime(View view) {
        DialogFragment timeFragment = new TimePickerFragment();
        timeFragment.show(getSupportFragmentManager(), "TimePicker");
    }

    public void selectPlace(View view) {
        if (ContextCompat.checkSelfPermission(this, android.Manifest.permission.ACCESS_FINE_LOCATION)
                != PackageManager.PERMISSION_GRANTED) {

            if (ActivityCompat.shouldShowRequestPermissionRationale(this,
                    android.Manifest.permission.ACCESS_FINE_LOCATION)) {

                Toast.makeText(this, "Esta funcionalidade é fundamental para o funcionamento!", Toast.LENGTH_SHORT).show();

                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        101);
            } else {
                ActivityCompat.requestPermissions(this,
                        new String[]{Manifest.permission.ACCESS_FINE_LOCATION},
                        101);
            }
        } else {
            requestPlacePick();
        }
    }

    @Override
    public void onRequestPermissionsResult(int requestCode, String permissions[], int[] grantResults) {
        switch (requestCode) {
            case 101: {
                // If request is cancelled, the result arrays are empty.
                if (grantResults.length > 0
                        && grantResults[0] == PackageManager.PERMISSION_GRANTED) {
                    requestPlacePick();
                } else {
                    Toast.makeText(this, "Esta funcionalidade é fundamental para o funcionamento!", Toast.LENGTH_SHORT).show();
                }
            }
        }
    }

    private void requestPlacePick() {
        PlacePicker.IntentBuilder builder = new PlacePicker.IntentBuilder();

        try {
            startActivityForResult(builder.build(this), PLACE_PICKER_REQUEST);
        } catch (GooglePlayServicesRepairableException e) {
            e.printStackTrace();
        } catch (GooglePlayServicesNotAvailableException e) {
            e.printStackTrace();
        }
    }

    @Override
    protected void onActivityResult(int requestCode, int resultCode, Intent data) {
        if (requestCode == PLACE_PICKER_REQUEST) {
            if (resultCode == RESULT_OK) {
                place = PlacePicker.getPlace(this, data);
                textViewAddress.setText(place.getName() + " " + "em" + " " + place.getAddress());
            }
        }
    }

    @Override
    public void onTimeSet(TimePicker timePicker, int hourOfDay, int minute) {
        calendar.set(Calendar.HOUR_OF_DAY, hourOfDay);
        calendar.set(Calendar.MINUTE, minute);
        eventDate = calendar.getTime();

        SimpleDateFormat localDateFormat = new SimpleDateFormat("HH:mm a");

        textViewHour.setText(localDateFormat.format(eventDate));
    }

    @Override
    public void onDateSet(DatePicker datePicker, int year, int month, int day) {
        calendar.set(Calendar.YEAR, year);
        calendar.set(Calendar.MONTH, month);
        calendar.set(Calendar.DAY_OF_MONTH, day);
        eventDate = calendar.getTime();
        Locale locale;
        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.N) {
            locale = datePicker.getResources().getConfiguration().getLocales().get(0);
        } else {
            locale = datePicker.getResources().getConfiguration().locale;
        }
        java.text.DateFormat format = java.text.DateFormat.getDateInstance(java.text.DateFormat.SHORT, locale);

        textViewDate.setText(format.format(eventDate));
    }

    public void saveEvent(View view) {

        String title = editTextTitle.getText().toString();

        if (TextUtils.isEmpty(title)){
            editTextTitle.setError("Título do Evento Obrigatório!");
            return;
        }
        if (eventDate == null){
            Toast.makeText(this, "A data do Evento é Obrigatória!", Toast.LENGTH_LONG).show();
            return;
        }
         if (place == null){
             Toast.makeText(this, "A Local do Evento é Obrigatório!", Toast.LENGTH_LONG).show();
            return;
         }
        event.setTitle(editTextTitle.getText().toString());
        event.setDescription(editTextDescription.getText().toString());
        event.setDate(eventDate);
        event.setLatitude(place.getLatLng().latitude);
        event.setLongitude(place.getLatLng().longitude);
        event.setAddress(place.getAddress().toString());

        try {
            eventDao.create(event);
            Toast.makeText(this, "Evento salvo com sucesso!", Toast.LENGTH_SHORT).show();
            clearFields();
            scheduleEvent(event);
        } catch (SQLException e) {
            e.printStackTrace();
            Toast.makeText(this, "Erro ao salvar evento no banco", Toast.LENGTH_SHORT).show();
        }

    }

    private void scheduleEvent(Event event) {

        Intent intent = new Intent(EventBroadcastReceiver.ACTION);
        intent.putExtra("event", event);

        PendingIntent p = PendingIntent.getBroadcast(this, 1, intent, PendingIntent.FLAG_UPDATE_CURRENT);
        AlarmManager alarme = (AlarmManager)getSystemService(Context.ALARM_SERVICE);
        alarme.set(AlarmManager.RTC_WAKEUP, eventDate.getTime(), p);
    }

    public static class TimePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final Calendar c = Calendar.getInstance();
            int hour = c.get(Calendar.HOUR_OF_DAY);
            int minute = c.get(Calendar.MINUTE);

            return new TimePickerDialog(getActivity(), (NewEventActivity) getActivity(), hour, minute,
                    DateFormat.is24HourFormat(getActivity()));
        }
    }

    public static class DatePickerFragment extends DialogFragment {

        @Override
        public Dialog onCreateDialog(Bundle savedInstanceState) {

            final Calendar c = Calendar.getInstance();
            int year = c.get(Calendar.YEAR);
            int month = c.get(Calendar.MONTH);
            int day = c.get(Calendar.DAY_OF_MONTH);

            return new DatePickerDialog(getActivity(), (NewEventActivity) getActivity(), year, month, day);
        }
    }
}
