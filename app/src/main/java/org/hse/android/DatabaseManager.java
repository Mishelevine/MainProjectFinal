package org.hse.android;

import android.content.Context;

import androidx.annotation.NonNull;
import androidx.room.Room;
import androidx.room.RoomDatabase;
import androidx.sqlite.db.SupportSQLiteDatabase;

import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.concurrent.Executors;

public class DatabaseManager {

    private static DatabaseManager instance;
    private final DatabaseHelper db;

    public static DatabaseManager getInstance(Context context) {
        if (instance == null) {
            instance = new DatabaseManager(context.getApplicationContext());
        }
        return instance;
    }

    private DatabaseManager(Context context) {
        db = Room.databaseBuilder(
                        context,
                        DatabaseHelper.class,
                        DatabaseHelper.DATABASE_NAME
                )
                .addCallback(new RoomDatabase.Callback() {
                    @Override
                    public void onCreate(@NonNull SupportSQLiteDatabase db) {
                        Executors.newSingleThreadExecutor().execute(() -> initData(context));
                    }
                })
                .build();
    }

    public HseDao getHseDao() {
        return db.hseDao();
    }

    private void initData(Context context) {
        List<GroupEntity> groups = new ArrayList<>();
        GroupEntity group = new GroupEntity();
        group.id = 1;
        group.name = "Группа-18-1";
        groups.add(group);

        group = new GroupEntity();
        group.id = 2;
        group.name = "Группа-18-2";
        groups.add(group);

        group = new GroupEntity();
        group.id = 3;
        group.name = "РИС-22-1";
        groups.add(group);

        db.hseDao().insertGroups(groups);

        List<TeacherEntity> teachers = new ArrayList<>();
        TeacherEntity teacher = new TeacherEntity();
        teacher.id = 1;
        teacher.fio = "Петров Петр Петрович";
        teachers.add(teacher);

        teacher = new TeacherEntity();
        teacher.id = 2;
        teacher.fio = "Иванова Мария Сергеевна";
        teachers.add(teacher);

        teacher = new TeacherEntity();
        teacher.id = 3;
        teacher.fio = "Кузнецов Алексей Владимирович";
        teachers.add(teacher);

        db.hseDao().insertTeachers(teachers);

        List<TimeTableEntity> timeTables = new ArrayList<>();

        TimeTableEntity timeTable = new TimeTableEntity();
        timeTable.id = 1;
        timeTable.cabinet = "Кабинет 1";
        timeTable.subGroup = "ПИ";
        timeTable.subjName = "Философия";
        timeTable.corp = "К1";
        timeTable.type = 0;
        timeTable.timeStart = dateFromString("2025-05-13 13:00");
        timeTable.timeEnd = dateFromString("2025-05-13 14:30");
        timeTable.groupId = 1;
        timeTable.teacherId = 3;
        timeTables.add(timeTable);


        timeTable = new TimeTableEntity();
        timeTable.id = 2;
        timeTable.cabinet = "Кабинет 2";
        timeTable.subGroup = "ПИ";
        timeTable.subjName = "Мобильная разработка";
        timeTable.corp = "К1";
        timeTable.type = 1;
        timeTable.timeStart = dateFromString("2025-05-13 15:00");
        timeTable.timeEnd = dateFromString("2025-05-13 16:30");
        timeTable.groupId = 1;
        timeTable.teacherId = 2;
        timeTables.add(timeTable);


        int id = 3;

        timeTables.add(newTimeTable(id++, "Алгебра", "Каб. 101", "К2", 0, "2025-05-13 08:10", "2025-05-13 9:30", 1));
        timeTables.add(newTimeTable(id++, "История", "Каб. 102", "К2", 2, "2025-05-13 9:40", "2025-05-13 11:00", 2));

        timeTables.add(newTimeTable(id++, "Программирование", "Каб. 201", "К3", 1, "2025-05-14 09:30", "2025-05-14 11:00", 3));
        timeTables.add(newTimeTable(id++, "Английский язык", "Каб. 202", "К3", 0, "2025-05-14 11:30", "2025-05-14 13:00", 1));

        timeTables.add(newTimeTable(id++, "Физика", "Каб. 301", "К1", 2, "2025-05-15 10:00", "2025-05-15 11:30", 2));
        timeTables.add(newTimeTable(id++, "Проектная работа", "Каб. 302", "К1", 1, "2025-05-15 12:00", "2025-05-15 13:30", 3));

        db.hseDao().insertTimeTables(timeTables);
    }

    private TimeTableEntity newTimeTable(int id, String subj, String cabinet, String corp, int type,
                                         String start, String end, int teacherId) {
        TimeTableEntity t = new TimeTableEntity();
        t.id = id;
        t.subjName = subj;
        t.cabinet = cabinet;
        t.corp = corp;
        t.type = type;
        t.timeStart = dateFromString(start);
        t.timeEnd = dateFromString(end);
        t.groupId = 3;
        t.teacherId = teacherId;
        t.subGroup = "1";
        return t;
    }

    private Date dateFromString(String val) {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault());
        try {
            return simpleDateFormat.parse(val);
        } catch (Exception e) {
            return null;
        }
    }
}