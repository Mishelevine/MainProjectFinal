package org.hse.android;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.Spinner;
import android.widget.AdapterView;
import android.widget.ArrayAdapter;
import android.widget.TextView;

import androidx.appcompat.app.AppCompatActivity;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class StudentActivity extends AppCompatActivity {
    private static final String TAG = "StudentActivity";
    private static final List<String> programs = List.of("РИС", "МБ");
    private static final List<String> years = List.of("22", "23", "24");
    private static final Integer numGroups = 4;
    private TextView time, status, subject, room, building, teacher;
    @Override
    protected void onCreate(Bundle savedInstanceState) {

        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_student);

        final Spinner spinner = findViewById(R.id.spinner_group);

        List<Group> groups = new ArrayList<>();
        initGroupList(groups);

        ArrayAdapter<Group> adapter = new ArrayAdapter<>(this, android.R.layout.simple_spinner_item, groups);
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

    private void initGroupList(List<Group> groups) {
        var id = 1;
        for (int i = 0; i < programs.size(); i++){
            for (int j = 0; j < years.size(); j++){
                for (int k = 1; k <= numGroups; k++){
                    groups.add(new Group(id++, programs.get(i) + "-" + years.get(j) + "-" + k));
                }
            }
        }
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
