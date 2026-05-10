package com.example.students.data.dao

import androidx.room.Dao
import androidx.room.Delete
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import androidx.room.Update
import com.example.students.data.model.Course
import com.example.students.data.model.StudentCourse
import kotlinx.coroutines.flow.Flow

@Dao
interface CourseDao {
    @Query("SELECT * FROM courses ORDER BY name ASC")
    fun getAll(): Flow<List<Course>>

    @Query("SELECT * FROM courses WHERE id = :id")
    suspend fun getById(id: Long): Course?

    @Query("SELECT * FROM courses WHERE name = :name LIMIT 1")
    suspend fun getByName(name: String): Course?

    @Query("SELECT * FROM courses WHERE type = :type ORDER BY name ASC")
    fun getByType(type: String): Flow<List<Course>>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insert(course: Course): Long

    @Update
    suspend fun update(course: Course)

    @Delete
    suspend fun delete(course: Course)

    @Query("SELECT * FROM student_courses WHERE studentId = :studentId")
    suspend fun getStudentCourses(studentId: Long): List<StudentCourse>

    @Query("SELECT sc.*, c.name as courseName FROM student_courses sc INNER JOIN courses c ON sc.courseId = c.id WHERE sc.studentId = :studentId")
    suspend fun getStudentCoursesWithName(studentId: Long): List<StudentCourseWithName>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun insertStudentCourse(sc: StudentCourse): Long

    @Update
    suspend fun updateStudentCourse(sc: StudentCourse)

    @Delete
    suspend fun deleteStudentCourse(sc: StudentCourse)

    @Query("UPDATE student_courses SET remainingHours = remainingHours - :hours WHERE id = :id")
    suspend fun deductHours(id: Long, hours: Int)
}

data class StudentCourseWithName(
    val id: Long = 0,
    val studentId: Long,
    val courseId: Long,
    val totalHours: Int = 20,
    val remainingHours: Int = 20,
    val price: Double = 0.0,
    val courseName: String = ""
)
