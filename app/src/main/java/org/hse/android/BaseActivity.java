package org.hse.android;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public abstract class BaseActivity extends AppCompatActivity {
    protected static final String TAG = "BaseActivity";
    protected TextView time, status, subject, room, building, teacher;
    protected Spinner spinner;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        spinner = findViewById(getSpinnerId());

        time = findViewById(R.id.time);
        status = findViewById(R.id.status);
        subject = findViewById(R.id.subject);
        room = findViewById(R.id.room);
        building = findViewById(R.id.building);
        teacher = findViewById(R.id.teacher);

        initTime();
        initData();
        setupSpinner();
    }

    protected void initTime() {
        Date currentDate = new Date();
        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("ru"));
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("ru"));

        String formattedTime = timeFormat.format(currentDate);
        String formattedDay = dayFormat.format(currentDate);

        formattedDay = formattedDay.substring(0, 1).toUpperCase() + formattedDay.substring(1);

        time.setText(String.format("%s, %s", formattedTime, formattedDay));
    }

    protected void initData() {
        status.setText(getString(R.string.class_status));
        subject.setText(getString(R.string.subject));
        room.setText(getString(R.string.classroom));
        building.setText(getString(R.string.building));
        teacher.setText(getString(R.string.teacher));
    }

    protected void setupSpinner() {
        List<SpinnerItem> items = getSpinnerItems();
        ArrayAdapter<SpinnerItem> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, items);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);
        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                SpinnerItem item = adapter.getItem(selectedItemPosition);
                Log.d(TAG, "Selected item: " + item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });
    }

    protected abstract int getLayoutResId();
    protected abstract int getSpinnerId();
    protected abstract List<SpinnerItem> getSpinnerItems();
}

