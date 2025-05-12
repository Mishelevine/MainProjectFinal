package org.hse.android;

import android.os.Bundle;
import android.view.View;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import androidx.lifecycle.ViewModelProvider;

public class TeacherActivity extends BaseActivity {
    private MainViewModel viewModel;
    private final List<SpinnerItem> spinnerItems = new ArrayList<>();
    private View scheduleDayButton;
    private View scheduleWeekButton;

    @Override
    protected void onCreate(Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        viewModel.getTeachers().observe(this, teachers -> {
            spinnerItems.clear();
            spinnerItems.add(new SpinnerItem(-1, getString(R.string.teacher_spinner_select)));
            for (TeacherEntity teacher : teachers) {
                spinnerItems.add(new SpinnerItem(teacher.id, teacher.fio));
            }
            setupSpinner();
        });

        scheduleDayButton = findViewById(R.id.day_schedule);
        scheduleWeekButton = findViewById(R.id.week_schedule);

        scheduleDayButton.setOnClickListener(v -> showSchedule(ScheduleType.DAY));
        scheduleWeekButton.setOnClickListener(v -> showSchedule(ScheduleType.WEEK));

        scheduleDayButton.setEnabled(false);
        scheduleWeekButton.setEnabled(false);
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
        return spinnerItems;
    }

    @Override
    protected void onSpinnerItemChanged(SpinnerItem selected) {
        boolean validSelection = selected.getId() != -1;

        scheduleDayButton.setEnabled(validSelection);
        scheduleWeekButton.setEnabled(validSelection);

        if (!validSelection) {
            status.setText(getString(R.string.lesson_is_not_going_message));
            subject.setText(getString(R.string.subject));
            cabinet.setText(getString(R.string.classroom));
            corp.setText(getString(R.string.building));
            teacher.setText(getString(R.string.teacher));
            return;
        }

        if (currentTime != null) {
            viewModel.getTimeTableWithTeacherByTeacherAndDate(selected.getId(), currentTime)
                    .observe(this, list -> {
                        TimeTableWithTeacherEntity current = null;
                        for (TimeTableWithTeacherEntity item : list) {
                            Date start = item.timeTableEntity.timeStart;
                            Date end = item.timeTableEntity.timeEnd;
                            if (start.before(currentTime) && end.after(currentTime)) {
                                current = item;
                                break;
                            }
                        }
                        initDataFromTimeTable(current);
                    });
        }
    }

    private void initDataFromTimeTable(TimeTableWithTeacherEntity timeTableTeacherEntity) {
        if (timeTableTeacherEntity == null) {
            status.setText(getString(R.string.lesson_is_not_going_message));
            subject.setText(getString(R.string.subject));
            cabinet.setText(getString(R.string.classroom));
            corp.setText(getString(R.string.building));
            teacher.setText(getString(R.string.teacher));
            return;
        }

        TimeTableEntity timeTable = timeTableTeacherEntity.timeTableEntity;

        status.setText(getString(R.string.lesson_is_going_message));
        subject.setText(timeTable.subjName);
        cabinet.setText(timeTable.cabinet);
        corp.setText(timeTable.corp);
        teacher.setText(timeTableTeacherEntity.teacherEntity != null
                ? timeTableTeacherEntity.teacherEntity.fio
                : "-");
    }

    private void showSchedule(ScheduleType type) {
        super.showSchedule(ScheduleMode.TEACHER, type);
    }
}
