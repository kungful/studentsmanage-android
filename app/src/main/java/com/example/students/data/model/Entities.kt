package com.example.students.data.model

import androidx.room.Entity
import androidx.room.ForeignKey
import androidx.room.Index
import androidx.room.PrimaryKey

@Entity(tableName = "students")
data class Student(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val gender: String = "male",
    val phone: String = "",
    val birthday: String = "",
    val school: String = "",
    val avatar: String = "",
    val course: String = "",
    val status: String = "normal",
    val hours: Int = 0,
    val createdAt: Long = System.currentTimeMillis()
)

@Entity(
    tableName = "student_parents",
    foreignKeys = [ForeignKey(
        entity = Student::class,
        parentColumns = ["id"],
        childColumns = ["studentId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class StudentParent(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val name: String,
    val relation: String,
    val phone: String,
    val address: String
)

@Entity(tableName = "courses")
data class Course(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val name: String,
    val type: String = "individual",
    val price: Double = 0.0,
    val duration: Int = 60,
    val description: String = "",
    val color: String = "#6750A4"
)

@Entity(
    tableName = "student_courses",
    foreignKeys = [
        ForeignKey(entity = Student::class, parentColumns = ["id"], childColumns = ["studentId"], onDelete = ForeignKey.CASCADE),
        ForeignKey(entity = Course::class, parentColumns = ["id"], childColumns = ["courseId"], onDelete = ForeignKey.CASCADE)
    ],
    indices = [Index("studentId"), Index("courseId")]
)
data class StudentCourse(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long,
    val courseId: Long,
    val totalHours: Int = 20,
    val remainingHours: Int = 20,
    val price: Double = 0.0
)

@Entity(tableName = "schedules")
data class Schedule(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val title: String,
    val studentId: Long? = null,
    val courseId: Long? = null,
    val date: String,
    val startTime: String,
    val endTime: String,
    val room: String = "",
    val type: String = "individual",
    val status: String = "pending",
    val students: Int = 1,
    val location: String = ""
)

@Entity(
    tableName = "attendance",
    foreignKeys = [ForeignKey(
        entity = Schedule::class,
        parentColumns = ["id"],
        childColumns = ["scheduleId"],
        onDelete = ForeignKey.CASCADE
    )]
)
data class Attendance(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val scheduleId: Long,
    val studentId: Long,
    val status: String = "present",
    val rating: Int = 0,
    val note: String = "",
    val attachments: String = ""
)

@Entity(tableName = "notifications")
data class Notification(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val type: String = "academic",
    val title: String,
    val content: String,
    val time: String,
    val isRead: Boolean = false,
    val priority: String = "medium",
    val detail: String = ""
)

@Entity(tableName = "invoices")
data class Invoice(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val studentId: Long? = null,
    val name: String,
    val amount: String,
    val status: String,
    val date: String,
    val method: String = ""
)
