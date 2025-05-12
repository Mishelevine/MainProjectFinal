package org.hse.android;

import android.app.Application;

import androidx.annotation.NonNull;
import androidx.lifecycle.AndroidViewModel;
import androidx.lifecycle.LiveData;

import java.util.Date;
import java.util.List;

public class MainViewModel extends AndroidViewModel {

    private final HseRepository repository;

    public MainViewModel(@NonNull Application application) {
        super(application);
        repository = new HseRepository(application);
    }

    public LiveData<List<GroupEntity>> getGroups() {
        return repository.getGroups();
    }

    public LiveData<List<TeacherEntity>> getTeachers() {
        return repository.getTeachers();
    }

    public LiveData<List<TimeTableWithTeacherEntity>> getTimeTableWithTeacherByGroupAndDate(int groupId, Date date) {
        return repository.getTimeTableWithTeacherByGroupAndDate(groupId, date);
    }

    public LiveData<List<TimeTableWithTeacherEntity>> getTimeTableWithTeacherByTeacherAndDate(int teacherId, Date date) {
        return repository.getTimeTableWithTeacherByTeacherAndDate(teacherId, date);
    }
}
