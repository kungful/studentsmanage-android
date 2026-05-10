package com.example.students.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.students.data.model.Student
import com.example.students.data.model.StudentParent
import kotlinx.coroutines.flow.Flow

@Dao
interface StudentDao {
    @Query("SELECT * FROM students ORDER BY name ASC")
    fun getAll(): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE id = :id")
    suspend fun getById(id: Long): Student?

    @Query("SELECT * FROM students WHERE name LIKE '%' || :query || '%' OR course LIKE '%' || :query || '%' ORDER BY name ASC")
    fun search(query: String): Flow<List<Student>>

    @Query("SELECT * FROM students WHERE status = :status ORDER BY name ASC")
    fun getByStatus(status: String): Flow<List<Student>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(student: Student): Long

    @Update
    suspend fun update(student: Student)

    @Query("SELECT * FROM students WHERE id IN (SELECT studentId FROM student_courses WHERE courseId = :courseId)")
    suspend fun getByCourseId(courseId: Long): List<Student>

    @Delete
    suspend fun delete(student: Student)

    @Query("SELECT * FROM students WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Student?

    @Query("SELECT * FROM students WHERE hours <= :threshold ORDER BY hours ASC")
    fun getLowLessonStudents(threshold: Int = 3): Flow<List<Student>>

    @Query("SELECT COUNT(*) FROM students")
    fun getStudentCount(): Flow<Int>

    @Query("SELECT COUNT(*) FROM students WHERE status = :status")
    fun getCountByStatus(status: String): Flow<Int>

    @Query("SELECT * FROM student_parents WHERE studentId = :studentId")
    suspend fun getParentsByStudent(studentId: Long): List<StudentParent>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertParent(parent: StudentParent): Long

    @Update
    suspend fun updateParent(parent: StudentParent)

    @Delete
    suspend fun deleteParent(parent: StudentParent)
}
