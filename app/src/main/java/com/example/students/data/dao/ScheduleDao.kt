package com.example.students.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.students.data.model.Attendance
import com.example.students.data.model.Schedule
import kotlinx.coroutines.flow.Flow

@Dao
interface ScheduleDao {
    @Query("SELECT * FROM schedules ORDER BY date ASC, startTime ASC")
    fun getAll(): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE date = :date ORDER BY startTime ASC")
    fun getByDate(date: String): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE id = :id")
    suspend fun getById(id: Long): Schedule?

    @Query("SELECT * FROM schedules WHERE studentId = :studentId ORDER BY date ASC, startTime ASC")
    fun getByStudentId(studentId: Long): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE studentId = :studentId AND date = :date ORDER BY startTime ASC")
    fun getByStudentAndDate(studentId: Long, date: String): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE studentId = :studentId AND strftime('%Y-%m', date) = :yearMonth ORDER BY date ASC")
    fun getByStudentAndMonth(studentId: Long, yearMonth: String): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE (studentId = :studentId OR courseId IN (SELECT courseId FROM student_courses WHERE studentId = :studentId)) AND strftime('%Y-%m', date) = :yearMonth ORDER BY date ASC")
    fun getByStudentCoursesAndMonth(studentId: Long, yearMonth: String): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE (studentId = :studentId OR courseId IN (SELECT courseId FROM student_courses WHERE studentId = :studentId)) AND date = :date ORDER BY startTime ASC")
    fun getByStudentCoursesAndDate(studentId: Long, date: String): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE studentId = :studentId OR courseId IN (SELECT courseId FROM student_courses WHERE studentId = :studentId) ORDER BY date ASC, startTime ASC")
    fun getByStudentCourses(studentId: Long): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE strftime('%Y-%m', date) = :yearMonth ORDER BY date ASC")
    fun getByMonth(yearMonth: String): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE date BETWEEN :startDate AND :endDate ORDER BY date ASC")
    fun getByDateRange(startDate: String, endDate: String): Flow<List<Schedule>>

    @Query("SELECT * FROM schedules WHERE status = :status ORDER BY date DESC")
    fun getByStatus(status: String): Flow<List<Schedule>>

    @Query("SELECT COUNT(*) FROM schedules WHERE status = :status")
    fun getCountByStatus(status: String): Flow<Int>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(schedule: Schedule): Long

    @Update
    suspend fun update(schedule: Schedule)

    @Delete
    suspend fun delete(schedule: Schedule)

    @Query("UPDATE schedules SET status = :status WHERE id = :id")
    suspend fun updateStatus(id: Long, status: String)

    @Query("SELECT * FROM attendance WHERE scheduleId = :scheduleId")
    suspend fun getAttendanceBySchedule(scheduleId: Long): List<Attendance>

    @Query("SELECT * FROM attendance WHERE studentId = :studentId ORDER BY scheduleId DESC")
    suspend fun getAttendanceByStudent(studentId: Long): List<Attendance>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertAttendance(attendance: Attendance): Long

    @Query("UPDATE attendance SET status = :status WHERE scheduleId = :scheduleId AND studentId = :studentId")
    suspend fun updateAttendanceStatus(scheduleId: Long, studentId: Long, status: String)

    @Query("SELECT COUNT(*) FROM attendance WHERE status = :status")
    fun getAttendanceCountByStatus(status: String): Flow<Int>
}
