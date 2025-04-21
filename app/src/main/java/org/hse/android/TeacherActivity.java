package org.hse.android;

import android.os.Bundle;
import android.view.View;
import java.util.ArrayList;
import java.util.List;

public class TeacherActivity extends BaseActivity {

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        View scheduleDayButton = findViewById(R.id.day_schedule);
        View scheduleWeekButton = findViewById(R.id.week_schedule);

        scheduleDayButton.setOnClickListener(v -> showSchedule(ScheduleType.DAY));
        scheduleWeekButton.setOnClickListener(v -> showSchedule(ScheduleType.WEEK));
    }

    @Override
    protected int getLayoutResId() {
        return R.layout.activity_teacher;
    }

    @Override
    protected int getSpinnerId() {
        return R.id.spinner_teacher;
    }

    @Override
    protected List<SpinnerItem> getSpinnerItems() {
        List<SpinnerItem> spinnerItems = new ArrayList<>();
        spinnerItems.add(new SpinnerItem(1, "Преподаватель 1"));
        spinnerItems.add(new SpinnerItem(2, "Преподаватель 2"));
        spinnerItems.add(new SpinnerItem(3, "Преподаватель 3"));
        return spinnerItems;
    }

    private void showSchedule(ScheduleType type) {
        super.showSchedule(ScheduleMode.TEACHER, type);
    }
}
