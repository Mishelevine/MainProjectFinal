package org.hse.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;


public abstract class BaseActivity extends AppCompatActivity {
    protected static final String TAG = "BaseActivity";
    protected TextView time, status, subject, cabinet, corp, teacher;
    protected Spinner spinner;

    protected Date currentTime;

    protected SpinnerItem selectedSpinnerItem = null;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(getLayoutResId());

        spinner = findViewById(getSpinnerId());

        time = findViewById(R.id.time);
        status = findViewById(R.id.status);
        subject = findViewById(R.id.subject);
        cabinet = findViewById(R.id.cabinet);
        corp = findViewById(R.id.corp);
        teacher = findViewById(R.id.teacher);

        initTime();
        initDataFromTimeTable();
        setupSpinner();
    }

    protected void initTime() {
        TimeViewModel timeViewModel = new ViewModelProvider(this).get(TimeViewModel.class);
        timeViewModel.getTime().observe(this, this::showTime);
    }


    private void showTime(Date dateTime) {
        if (dateTime == null) {
            Log.w(TAG, "showTime called with null dateTime");
            time.setText(R.string.time_not_available);
            return;
        }
        currentTime = dateTime;
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("HH:mm, EEEE", new Locale("ru"));
        time.setText(simpleDateFormat.format(currentTime));

        if (selectedSpinnerItem != null) {
            onSpinnerItemChanged(selectedSpinnerItem);
        }
    }

    protected void initDataFromTimeTable() {
        if (selectedSpinnerItem != null && currentTime != null) {
            onSpinnerItemChanged(selectedSpinnerItem);
        } else {
            status.setText(getString(R.string.class_status));
            subject.setText(getString(R.string.subject));
            cabinet.setText(getString(R.string.classroom));
            corp.setText(getString(R.string.building));
            teacher.setText(getString(R.string.teacher));
        }
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
                onSpinnerItemChanged(item);
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
    protected abstract void onSpinnerItemChanged(SpinnerItem selected);

    protected void showSchedule(ScheduleMode mode, ScheduleType type) {
        Object selectedItem = spinner.getSelectedItem();
        if (!(selectedItem instanceof SpinnerItem)) {
            Log.w(TAG, "Selected item is not a SpinnerItem: " + selectedItem);
            return;
        }
        showScheduleImpl(mode, type, (SpinnerItem) selectedItem);
    }

    protected void showScheduleImpl(ScheduleMode mode, ScheduleType type, SpinnerItem item) {
        Intent intent = new Intent(this, ScheduleActivity.class);

        intent.putExtra(ScheduleActivity.ARG_ID, item.getId());
        intent.putExtra(ScheduleActivity.ARG_TYPE, type);
        intent.putExtra(ScheduleActivity.ARG_MODE, mode);
        intent.putExtra(ScheduleActivity.ARG_NAME, item.getName());
        intent.putExtra(ScheduleActivity.ARG_TIME, currentTime.getTime());

        startActivity(intent);
    }
}

