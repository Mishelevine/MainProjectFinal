package org.hse.android;

import android.content.Intent;
import android.os.Bundle;
import android.widget.Button;

import androidx.appcompat.app.AppCompatActivity;

public class MainActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        Button studentsTimetableButton = findViewById(R.id.studentsTimetable);
        Button teachersTimetableButton = findViewById(R.id.teachersTimetable);
        Button settingsButton = findViewById(R.id.settings);

        studentsTimetableButton.setOnClickListener(v -> getStudentsTimetable());
        teachersTimetableButton.setOnClickListener(v -> getTeachersTimetable());
        settingsButton.setOnClickListener(v -> getSettings());
    }

    private void getStudentsTimetable(){
        Intent intent = new Intent(this, StudentActivity.class);
        startActivity(intent);
    }

    private void getTeachersTimetable(){
        Intent intent = new Intent(this, TeacherActivity.class);
        startActivity(intent);
    }

    private void getSettings(){
        Intent intent = new Intent(this, SettingsActivity.class);
        startActivity(intent);
    }
}