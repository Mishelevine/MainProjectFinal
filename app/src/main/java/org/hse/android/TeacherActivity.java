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
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class TeacherActivity extends AppCompatActivity {
    private static final String TAG = "TeacherActivity";
    private static List<String> teachers = new ArrayList<>();
    private TextView time, status, subject, room, building, teacher;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_teacher);

        final Spinner spinner = findViewById(R.id.spinner_teacher);

        initTeachersList();

        ArrayAdapter<String> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, teachers);
        adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item);

        spinner.setAdapter(adapter);

        spinner.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            public void onItemSelected(AdapterView<?> parent, View itemSelected, int selectedItemPosition, long selectedId) {
                Object item = adapter.getItem(selectedItemPosition);
                Log.d(TAG, "selectedItem: " + item);
            }

            @Override
            public void onNothingSelected(AdapterView<?> parent) {
                //
            }
        });

        time = findViewById(R.id.time);
        initTime();

        status = findViewById(R.id.status);
        subject = findViewById(R.id.subject);
        room = findViewById(R.id.room);
        building = findViewById(R.id.building);
        teacher = findViewById(R.id.teacher);

        initData();
    }

    private void initTeachersList() {
        teachers.add("Преподаватель 1");
        teachers.add("Преподаватель 2");
        teachers.add("Преподаватель 3");
    }

    private void initTime() {
        Date currentDate = new Date();

        SimpleDateFormat timeFormat = new SimpleDateFormat("HH:mm", new Locale("ru"));
        SimpleDateFormat dayFormat = new SimpleDateFormat("EEEE", new Locale("ru"));

        String formattedTime = timeFormat.format(currentDate);
        String formattedDay = dayFormat.format(currentDate);

        time.setText(String.format("%s, %s", formattedTime, formattedDay));
    }

    private void initData() {
        status.setText(getString(R.string.class_status));

        subject.setText(getString(R.string.subject));
        room.setText(getString(R.string.classroom));
        building.setText(getString(R.string.building));
        teacher.setText(getString(R.string.teacher));
    }
}