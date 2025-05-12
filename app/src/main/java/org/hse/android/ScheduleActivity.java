package org.hse.android;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
import androidx.lifecycle.ViewModelProvider;
import androidx.recyclerview.widget.DividerItemDecoration;
import androidx.recyclerview.widget.LinearLayoutManager;
import androidx.recyclerview.widget.RecyclerView;


import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Calendar;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class ScheduleActivity extends AppCompatActivity {

    private final static String TAG = "ScheduleActivity";

    public static final String ARG_ID = "id";
    public static final String ARG_TYPE = "type";
    public static final String ARG_MODE = "mode";
    public static final String ARG_NAME = "name";
    public static final String ARG_TIME = "time";
    public static final int DEFAULT_ID = 0;

    private ScheduleType type;
    private ScheduleMode mode;
    private int id;
    private String selectedName;
    private Date now;

    private ItemAdapter adapter;
    private TextView titleTextView;
    private TextView dateTextView;

    private MainViewModel viewModel;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        viewModel = new ViewModelProvider(this).get(MainViewModel.class);

        type = (ScheduleType) getIntent().getSerializableExtra(ARG_TYPE);
        mode = (ScheduleMode) getIntent().getSerializableExtra(ARG_MODE);
        id = getIntent().getIntExtra(ARG_ID, DEFAULT_ID);
        selectedName = getIntent().getStringExtra(ARG_NAME);

        long timeMillis = getIntent().getLongExtra(ARG_TIME, -1);
        now = timeMillis != -1 ? new Date(timeMillis) : new Date();
        Calendar calendar = Calendar.getInstance();
        calendar.setTime(now);
        calendar.set(Calendar.HOUR_OF_DAY, 0);
        calendar.set(Calendar.MINUTE, 0);
        calendar.set(Calendar.SECOND, 0);
        calendar.set(Calendar.MILLISECOND, 0);
        now = calendar.getTime();


        titleTextView = findViewById(R.id.title);
        RecyclerView recyclerView = findViewById(R.id.listView);
        dateTextView = findViewById(R.id.current_date_text);

        recyclerView.setLayoutManager(new LinearLayoutManager(this));
        recyclerView.addItemDecoration(new DividerItemDecoration(this, LinearLayoutManager.VERTICAL));
        adapter = new ItemAdapter(this::onScheduleItemClick);
        recyclerView.setAdapter(adapter);

        initTitle();
        if (type == ScheduleType.DAY) {
            dateTextView.setVisibility(View.GONE);
        } else {
            dateTextView.setVisibility(View.VISIBLE);
            initDateDisplayForWeek();
        }

        loadDataFromViewModel();
    }

    private void initTitle() {
        String titlePrefix = "";
        if (selectedName != null && !selectedName.isEmpty()) {
            titlePrefix = selectedName;
        } else {
            if (mode == ScheduleMode.STUDENT) {
                titlePrefix = "Group ID: " + id;
            } else if (mode == ScheduleMode.TEACHER) {
                titlePrefix = "Teacher ID: " + id;
            }
        }


        titleTextView.setText(titlePrefix);
    }

    private void initDateDisplayForWeek() {
        SimpleDateFormat monthFormat = new SimpleDateFormat("LLLL", new Locale("ru"));
        String formattedMonth = monthFormat.format(now);

        if (!formattedMonth.isEmpty()) {
            formattedMonth = formattedMonth.substring(0, 1).toUpperCase() + formattedMonth.substring(1);
        }
        dateTextView.setText(formattedMonth);
    }

    private void loadDataFromViewModel() {
        if (mode == ScheduleMode.STUDENT) {
            viewModel.getTimeTableWithTeacherByGroupAndDate(id, now).observe(this, this::displayDataWithTeacher);
        } else if (mode == ScheduleMode.TEACHER) {
            viewModel.getTimeTableWithTeacherByTeacherAndDate(id, now).observe(this, this::displayDataWithTeacher);
        }
    }

    private void displayDataWithTeacher(List<TimeTableWithTeacherEntity> rawList) {
        List<ScheduleItem> list = new ArrayList<>();

        if (type == ScheduleType.DAY) {
            SimpleDateFormat headerFormat = new SimpleDateFormat("EEEE, dd MMMM", new Locale("ru"));
            String title = headerFormat.format(now);
            title = Character.toUpperCase(title.charAt(0)) + title.substring(1);

            ScheduleItemHeader header = new ScheduleItemHeader();
            header.setTitle(title);
            list.add(header);

            for (TimeTableWithTeacherEntity entity : rawList) {
                if (isSameDay(entity.timeTableEntity.timeStart, now)) {
                    list.add(mapEntityWithTeacherToItem(entity));
                }
            }

        } else if (type == ScheduleType.WEEK) {
            Calendar today = Calendar.getInstance();
            today.setTime(now);

            Calendar calendar = (Calendar) today.clone();

            for (int i = today.get(Calendar.DAY_OF_WEEK); i <= Calendar.SATURDAY; i++) {
                String title = new SimpleDateFormat("EEEE, dd MMMM", new Locale("ru"))
                        .format(calendar.getTime());
                title = Character.toUpperCase(title.charAt(0)) + title.substring(1);

                ScheduleItemHeader header = new ScheduleItemHeader();
                header.setTitle(title);
                list.add(header);

                for (TimeTableWithTeacherEntity entity : rawList) {
                    Calendar itemDate = Calendar.getInstance();
                    itemDate.setTime(entity.timeTableEntity.timeStart);

                    if (isSameDay(itemDate.getTime(), calendar.getTime())) {
                        list.add(mapEntityWithTeacherToItem(entity));
                    }
                }

                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        adapter.setDataList(list);
    }


    private boolean isSameDay(Date d1, Date d2) {
        Calendar c1 = Calendar.getInstance();
        Calendar c2 = Calendar.getInstance();
        c1.setTime(d1);
        c2.setTime(d2);
        return c1.get(Calendar.YEAR) == c2.get(Calendar.YEAR) &&
                c1.get(Calendar.DAY_OF_YEAR) == c2.get(Calendar.DAY_OF_YEAR);
    }

    private ScheduleItem mapEntityWithTeacherToItem(TimeTableWithTeacherEntity entity) {
        ScheduleItem item = new ScheduleItem();
        SimpleDateFormat sdf = new SimpleDateFormat("HH:mm", new Locale("ru"));

        item.setStart(sdf.format(entity.timeTableEntity.timeStart));
        item.setEnd(sdf.format(entity.timeTableEntity.timeEnd));
        item.setType(mapType(entity.timeTableEntity.type));
        item.setName(entity.timeTableEntity.subjName);
        item.setPlace(entity.timeTableEntity.cabinet);
        item.setTeacher(entity.teacherEntity != null ? entity.teacherEntity.fio : "—");

        return item;
    }

    private String mapType(int typeCode) {
        switch (typeCode) {
            case 0: return getString(R.string.lesson_type_1);
            case 1: return getString(R.string.lesson_type_3);
            case 2: return getString(R.string.lesson_type_2);
            default: return getString(R.string.lesson_type_default);
        }
    }

    private void onScheduleItemClick(ScheduleItem item) {
        if (!(item instanceof ScheduleItemHeader)) {
            Log.d(TAG, "Clicked item: " + item.getName());
            Toast.makeText(this, "Chosen: " + item.getName(), Toast.LENGTH_SHORT).show();
        }
    }
}
