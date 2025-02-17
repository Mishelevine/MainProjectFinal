package org.hse.android;

import android.os.Bundle;
import android.widget.Button;
import android.widget.Toast;

import androidx.appcompat.app.AppCompatActivity;

public class BaseActivity extends AppCompatActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_base);

        Button studentsTimetableButton = findViewById(R.id.studentsTimetable);
        Button teachersTimetableButton = findViewById(R.id.teachersTimetable);

        studentsTimetableButton.setOnClickListener(v -> getStudentsTimetable());
        teachersTimetableButton.setOnClickListener(v -> getTeachersTimetable());

    }

    private void getStudentsTimetable(){
        Toast.makeText(BaseActivity.this, R.string.getStudentsTimetable, Toast.LENGTH_SHORT).show();
    }

    private void getTeachersTimetable(){
        Toast.makeText(BaseActivity.this, R.string.getTeachersTimetable, Toast.LENGTH_SHORT).show();
    }
}