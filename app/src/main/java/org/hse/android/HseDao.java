package org.hse.android;

import androidx.lifecycle.LiveData;
import androidx.room.Dao;
import androidx.room.Delete;
import androidx.room.Insert;
import androidx.room.Query;
import androidx.room.Transaction;

import java.util.Date;
import java.util.List;

@Dao
public interface HseDao {

    @Query("SELECT * FROM `group`")
    LiveData<List<GroupEntity>> getAllGroups();

    @Query("SELECT * FROM `teacher`")
    LiveData<List<TeacherEntity>> getAllTeachers();

    @Transaction
    @Query("SELECT * FROM time_table WHERE group_id = :groupId AND date(time_start) = date(:date)")
    LiveData<List<TimeTableWithTeacherEntity>> getTimeTableWithTeacherByGroupAndDate(int groupId, Date date);


    @Transaction
    @Query("SELECT * FROM time_table WHERE teacher_id = :teacherId AND date(time_start) = date(:date)")
    LiveData<List<TimeTableWithTeacherEntity>> getTimeTableWithTeacherByTeacherAndDate(int teacherId, Date date);


    @Query("SELECT * FROM time_table")
    LiveData<List<TimeTableEntity>> getAllTimetable();

    @Insert
    void insertGroups(List<GroupEntity> groups);

    @Insert
    void insertTeachers(List<TeacherEntity> teachers);

    @Insert
    void insertTimeTables(List<TimeTableEntity> timeTables);

    @Delete
    void deleteGroup(GroupEntity group);

    @Delete
    void deleteTeacher(TeacherEntity teacher);
}