package org.hse.android;

import android.app.Application;

import androidx.lifecycle.LiveData;

import java.util.Date;
import java.util.List;

public class HseRepository {

    private final HseDao hseDao;

    public HseRepository(Application application) {
        hseDao = DatabaseManager.getInstance(application).getHseDao();
    }

    public LiveData<List<GroupEntity>> getGroups() {
        return hseDao.getAllGroups();
    }

    public LiveData<List<TeacherEntity>> getTeachers() {
        return hseDao.getAllTeachers();
    }

    public LiveData<List<TimeTableWithTeacherEntity>> getTimeTableWithTeacherByGroupAndDate(int groupId, Date date) {
        return hseDao.getTimeTableWithTeacherByGroupAndDate(groupId, date);
    }

    public LiveData<List<TimeTableWithTeacherEntity>> getTimeTableWithTeacherByTeacherAndDate(int teacherId, Date date) {
        return hseDao.getTimeTableWithTeacherByTeacherAndDate(teacherId, date);
    }

    public void insertGroups(List<GroupEntity> groups) {
        new Thread(() -> hseDao.insertGroups(groups)).start();
    }

    public void insertTeachers(List<TeacherEntity> teachers) {
        new Thread(() -> hseDao.insertTeachers(teachers)).start();
    }

    public void insertTimeTables(List<TimeTableEntity> tables) {
        new Thread(() -> hseDao.insertTimeTables(tables)).start();
    }
}