package org.hse.android;

import android.content.Intent;
import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.Spinner;
import android.widget.TextView;

import androidx.annotation.NonNull;
import androidx.appcompat.app.AppCompatActivity;

import com.google.gson.Gson;

import java.io.IOException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;

import okhttp3.Call;
import okhttp3.Callback;
import okhttp3.OkHttpClient;
import okhttp3.Request;
import okhttp3.Response;
import okhttp3.ResponseBody;

public abstract class BaseActivity extends AppCompatActivity {
    protected static final String TAG = "BaseActivity";
    protected TextView time, status, subject, cabinet, corp, teacher;
    protected Spinner spinner;

    public static final String URL = "https://api.ipgeolocation.io/ipgeo?apiKey=b03018f75ed94023a005637878ec0977";
    protected Date currentTime;
    private final OkHttpClient client = new OkHttpClient();

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
        initData();
        setupSpinner();
    }

    protected void initTime() {
        getTime();
    }

    protected void getTime() {
        Request request = new Request.Builder().url(URL).build();
        client.newCall(request).enqueue(new Callback() {
            @Override
            public void onResponse(@NonNull Call call, @NonNull Response response) throws IOException {
                parseResponse(response);
            }

            @Override
            public void onFailure(@NonNull Call call, @NonNull IOException e) {
                Log.e(TAG, "getTime failed", e);
                runOnUiThread(() -> showTime(new Date()));
            }
        });
    }

    private void parseResponse(Response response) {
        Gson gson = new Gson();
        try (ResponseBody body = response.body()) {
            TimeResponse timeResponse = null;
            if (body == null) {
                Log.e(TAG, "Response body is null");
                runOnUiThread(() -> showTime(new Date()));
                return;
            }
            String string = body.string();
            Log.d(TAG, "API Response: " + string);

            timeResponse = gson.fromJson(string, TimeResponse.class);

            if (timeResponse == null || timeResponse.getTimeZone() == null || timeResponse.getTimeZone().getCurrentTime() == null) {
                Log.e(TAG, "Failed to parse time response or time is null");
                runOnUiThread(() -> showTime(new Date()));
                return;
            }

            String currentTimeVal = timeResponse.getTimeZone().getCurrentTime();

            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss.SSS", Locale.getDefault());
            Date dateTime = simpleDateFormat.parse(currentTimeVal);

            runOnUiThread(() -> showTime(dateTime));

        } catch (Exception e) {
            Log.e(TAG, "Error parsing response", e);
            runOnUiThread(() -> showTime(new Date()));
        }
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
    }

    protected void initData() {
        status.setText(getString(R.string.class_status));
        subject.setText(getString(R.string.subject));
        cabinet.setText(getString(R.string.classroom));
        corp.setText(getString(R.string.building));
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

        startActivity(intent);
    }
}

