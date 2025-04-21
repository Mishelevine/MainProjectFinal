package org.hse.android;

import android.os.Bundle;
import android.util.Log;
import android.view.View;
import android.widget.TextView;
import android.widget.Toast;

import androidx.annotation.Nullable;
import androidx.appcompat.app.AppCompatActivity;
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
    public static final int DEFAULT_ID = 0;

    private ScheduleType type;
    private ScheduleMode mode;
    private int id;
    private String selectedName;

    private RecyclerView recyclerView;
    private ItemAdapter adapter;
    private TextView titleTextView;
    private TextView dateTextView;

    @Override
    protected void onCreate(@Nullable Bundle savedInstanceState) {
        super.onCreate(savedInstanceState);
        setContentView(R.layout.activity_schedule);

        type = (ScheduleType) getIntent().getSerializableExtra(ARG_TYPE);
        mode = (ScheduleMode) getIntent().getSerializableExtra(ARG_MODE);
        id = getIntent().getIntExtra(ARG_ID, DEFAULT_ID);
        selectedName = getIntent().getStringExtra(ARG_NAME);

        titleTextView = findViewById(R.id.title);
        recyclerView = findViewById(R.id.listView);
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
        initData();
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
        Date now = new Date();
        SimpleDateFormat monthFormat = new SimpleDateFormat("LLLL", new Locale("ru"));
        String formattedMonth = monthFormat.format(now);

        if (!formattedMonth.isEmpty()) {
            formattedMonth = formattedMonth.substring(0, 1).toUpperCase() + formattedMonth.substring(1);
        }
        dateTextView.setText(formattedMonth);
    }

    private void initData() {
        Log.d(TAG, "Initializing data for Mode: " + mode + ", Type: " + type + ", ID: " + id);
        List<ScheduleItem> list = new ArrayList<>();
        Locale localeRu = new Locale("ru");
        Date now = new Date();

        if (type == ScheduleType.DAY) {
            SimpleDateFormat dayHeaderFormat = new SimpleDateFormat("EEEE, dd MMMM", localeRu);
            String currentDayTitle = dayHeaderFormat.format(now);
            if (!currentDayTitle.isEmpty()) {
                currentDayTitle = currentDayTitle.substring(0, 1).toUpperCase() + currentDayTitle.substring(1);
            }


            ScheduleItemHeader header = new ScheduleItemHeader();
            header.setTitle(currentDayTitle);
            list.add(header);

            ScheduleItem item1 = new ScheduleItem();
            item1.setStart("10:00"); item1.setEnd("11:30"); item1.setType("ПРАКТИКА");
            item1.setName("Анализ данных (анг)"); item1.setPlace("Ауд. 503"); item1.setTeacher("Гущин Михаил Иванович");
            list.add(item1);

            ScheduleItem item2 = new ScheduleItem();
            item2.setStart("12:00"); item2.setEnd("13:30"); item2.setType("ЛЕКЦИЯ");
            item2.setName("Мобильная разработка"); item2.setPlace("Ауд. 301"); item2.setTeacher("Иванов Иван Иванович");
            list.add(item2);

        } else if (type == ScheduleType.WEEK) {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(now);

            calendar.set(Calendar.DAY_OF_WEEK, Calendar.MONDAY);

            SimpleDateFormat weekHeaderFormat = new SimpleDateFormat("EEEE, dd MMMM", localeRu);

            for (int i = 0; i < 6; i++) {
                Date dayOfWeek = calendar.getTime();
                String dayTitle = weekHeaderFormat.format(dayOfWeek);
                if (!dayTitle.isEmpty()) {
                    dayTitle = dayTitle.substring(0, 1).toUpperCase() + dayTitle.substring(1);
                }

                ScheduleItemHeader header = new ScheduleItemHeader();
                header.setTitle(dayTitle);
                list.add(header);

                ScheduleItem itemWeek1 = new ScheduleItem();
                itemWeek1.setStart(String.format(Locale.US,"%02d:00", 9 + i % 3));
                itemWeek1.setEnd(String.format(Locale.US,"%02d:30", 10 + i % 3));
                itemWeek1.setType(i % 2 == 0 ? "ЛЕКЦИЯ" : "СЕМИНАР");
                itemWeek1.setName("Предмет " + (i + 1));
                itemWeek1.setPlace("Ауд. " + (100 + i * 10));
                itemWeek1.setTeacher("Преп. " + (char)('А' + i));
                list.add(itemWeek1);

                if (i % 2 != 0) {
                    ScheduleItem itemWeek2 = new ScheduleItem();
                    itemWeek2.setStart(String.format(Locale.US,"%02d:00", 14 + i % 2));
                    itemWeek2.setEnd(String.format(Locale.US,"%02d:30", 15 + i % 2));
                    itemWeek2.setType("ПРАКТИКА");
                    itemWeek2.setName("Доп. Предмет " + (i + 1));
                    itemWeek2.setPlace("Ауд. " + (200 + i * 5));
                    itemWeek2.setTeacher("Ассист. " + (char)('А' + i));
                    list.add(itemWeek2);
                }


                calendar.add(Calendar.DAY_OF_MONTH, 1);
            }
        }

        adapter.setDataList(list);
    }

    private void onScheduleItemClick(ScheduleItem item) {
        if (!(item instanceof ScheduleItemHeader)) {
            Log.d(TAG, "Clicked item: " + item.getName());
            Toast.makeText(this, "Chosen: " + item.getName(), Toast.LENGTH_SHORT).show();
        }
    }
}
