package org.hse.android;

import android.os.Bundle;
import android.view.View;

import java.util.ArrayList;
import java.util.List;

public class StudentActivity extends BaseActivity {
    private static final List<String> PROGRAMS = List.of("РИС", "МБ");
    private static final List<String> YEARS = List.of("22", "23", "24");
    private static final int GROUPS_NUM = 4;

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
        return R.layout.activity_student;
    }

    @Override
    protected int getSpinnerId() {
        return R.id.spinner_group;
    }

    @Override
    protected List<SpinnerItem> getSpinnerItems() {
        List<SpinnerItem> spinnerItems = new ArrayList<>();
        var id = 1;
        for (String program : PROGRAMS) {
            for (String year : YEARS) {
                for (int k = 1; k <= GROUPS_NUM; k++) {
                    spinnerItems.add(new SpinnerItem(id++, program + "-" + year + "-" + k));
                }
            }
        }
        return spinnerItems;
    }

    private void showSchedule(ScheduleType type) {
        super.showSchedule(ScheduleMode.STUDENT, type);
    }
}
