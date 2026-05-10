package com.example.students.ui.viewmodel

import android.app.Application
import androidx.lifecycle.AndroidViewModel
import androidx.lifecycle.viewModelScope
import com.example.students.data.database.AppDatabase
import com.example.students.data.model.Attendance
import com.example.students.data.model.Course
import com.example.students.data.model.Invoice
import com.example.students.data.model.Notification
import com.example.students.data.model.Schedule
import com.example.students.data.model.Student
import com.example.students.data.model.StudentCourse
import com.example.students.data.model.StudentParent
import com.example.students.data.repository.AppRepository
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.distinctUntilChanged
import kotlinx.coroutines.flow.map
import kotlinx.coroutines.launch
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Date
import java.util.Locale

class AppViewModel(application: Application) : AndroidViewModel(application) {
    private val database = AppDatabase.getInstance(application)
    val repository = AppRepository(
        database.studentDao(),
        database.scheduleDao(),
        database.courseDao(),
        database.notificationDao(),
        database.invoiceDao()
    )

    val allStudents = repository.allStudents
    val allSchedules = repository.allSchedules
    val allCourses = repository.allCourses
    val allNotifications = repository.allNotifications
    val allInvoices = repository.allInvoices
    val lowLessonStudents = repository.getLowLessonStudents(3)
    val unreadNotificationCount = repository.unreadNotificationCount

    private val _selectedDate = MutableStateFlow("")
    val selectedDate: StateFlow<String> = _selectedDate.asStateFlow()

    private val _todayDate = MutableStateFlow(
        SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
    )
    val todayDate: StateFlow<String> = _todayDate.asStateFlow()

    private val _currentYearMonth = MutableStateFlow(
        SimpleDateFormat("yyyy-MM", Locale.getDefault()).format(Date())
    )
    val currentYearMonth: StateFlow<String> = _currentYearMonth.asStateFlow()

    fun setSelectedDate(date: String) {
        _selectedDate.value = date
    }

    fun searchStudents(query: String) = repository.searchStudents(query)
    fun getSchedulesByDate(date: String) = repository.getSchedulesByDate(date)
    fun getSchedulesByMonth(yearMonth: String) = repository.getSchedulesByMonth(yearMonth)
    fun getSchedulesByStudentId(studentId: Long) = repository.getSchedulesByStudentId(studentId)
    fun getSchedulesByStudentAndDate(studentId: Long, date: String) = repository.getSchedulesByStudentAndDate(studentId, date)
    fun getSchedulesByStudentAndMonth(studentId: Long, yearMonth: String) = repository.getSchedulesByStudentAndMonth(studentId, yearMonth)
    fun getSchedulesByStudentCoursesAndMonth(studentId: Long, yearMonth: String) = repository.getSchedulesByStudentCoursesAndMonth(studentId, yearMonth)
    fun getSchedulesByStudentCoursesAndDate(studentId: Long, date: String) = repository.getSchedulesByStudentCoursesAndDate(studentId, date)
    fun getSchedulesByStudentCourses(studentId: Long) = repository.getSchedulesByStudentCourses(studentId)
    fun getSchedulesByStatus(status: String) = repository.getSchedulesByStatus(status)
    fun getNotificationsByType(type: String) = repository.getNotificationsByType(type)
    fun getInvoicesByStatus(status: String) = repository.getInvoicesByStatus(status)
    fun getInvoicesByStudentId(studentId: Long) = repository.getInvoicesByStudentId(studentId)
    fun getAttendanceCountByStatus(status: String) = repository.getAttendanceCountByStatus(status)

    suspend fun getStudentsByCourseId(courseId: Long) = repository.getStudentsByCourseId(courseId)

    suspend fun getStudentById(id: Long) = repository.getStudentById(id)
    suspend fun getStudentByName(name: String) = repository.getStudentByName(name)
    suspend fun getParentsByStudent(studentId: Long) = repository.getParentsByStudent(studentId)
    suspend fun getScheduleById(id: Long) = repository.getScheduleById(id)
    suspend fun getCourseById(id: Long) = repository.getCourseById(id)
    suspend fun getCourseByName(name: String) = repository.getCourseByName(name)
    suspend fun getNotificationById(id: Long) = repository.getNotificationById(id)
    suspend fun getInvoiceById(id: Long) = repository.getInvoiceById(id)
    suspend fun getAttendanceBySchedule(scheduleId: Long) = repository.getAttendanceBySchedule(scheduleId)
    suspend fun getAttendanceByStudent(studentId: Long) = repository.getAttendanceByStudent(studentId)
    suspend fun getStudentCourses(studentId: Long) = repository.getStudentCourses(studentId)
    suspend fun getStudentCoursesWithName(studentId: Long) = repository.getStudentCoursesWithName(studentId)

    suspend fun insertStudent(student: Student) = repository.insertStudent(student)
    suspend fun updateStudent(student: Student) = repository.updateStudent(student)
    suspend fun deleteStudent(student: Student) = repository.deleteStudent(student)

    fun deleteStudents(ids: Set<Long>, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                for (id in ids) {
                    val student = repository.getStudentById(id)
                    if (student != null) {
                        repository.deleteStudent(student)
                    }
                }
                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    suspend fun insertParent(parent: StudentParent) = repository.insertParent(parent)
    suspend fun updateParent(parent: StudentParent) = repository.updateParent(parent)

    suspend fun insertSchedule(schedule: Schedule) = repository.insertSchedule(schedule)
    suspend fun updateSchedule(schedule: Schedule) = repository.updateSchedule(schedule)
    suspend fun deleteSchedule(schedule: Schedule) = repository.deleteSchedule(schedule)

    fun updateScheduleInfo(
        id: Long, title: String, date: String, startTime: String, endTime: String,
        room: String, status: String, type: String, courseId: Long?, studentId: Long?
    ) {
        viewModelScope.launch {
            try {
                val schedule = repository.getScheduleById(id) ?: return@launch
                repository.updateSchedule(schedule.copy(
                    title = title, date = date, startTime = startTime, endTime = endTime,
                    room = room, status = status, type = type, courseId = courseId,
                    studentId = studentId, location = room
                ))
                repository.insertNotification(Notification(
                    type = "academic", title = "排课已更新",
                    content = "课程 $title 已更新：$date $startTime-$endTime",
                    time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                    priority = "medium", isRead = false
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun deleteScheduleById(id: Long, onComplete: () -> Unit) {
        viewModelScope.launch {
            try {
                val schedule = repository.getScheduleById(id)
                if (schedule != null) {
                    repository.deleteSchedule(schedule)
                }
                onComplete()
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }
    suspend fun updateScheduleStatus(id: Long, status: String) = repository.updateScheduleStatus(id, status)

    suspend fun insertCourse(course: Course) = repository.insertCourse(course)
    suspend fun insertStudentCourse(sc: StudentCourse) = repository.insertStudentCourse(sc)
    suspend fun updateStudentCourse(sc: StudentCourse) = repository.updateStudentCourse(sc)
    suspend fun deleteStudentCourse(sc: StudentCourse) = repository.deleteStudentCourse(sc)
    suspend fun deductStudentCourseHours(id: Long, hours: Int) = repository.deductStudentCourseHours(id, hours)

    suspend fun insertAttendance(attendance: Attendance) = repository.insertAttendance(attendance)
    suspend fun updateAttendanceStatus(scheduleId: Long, studentId: Long, status: String) =
        repository.updateAttendanceStatus(scheduleId, studentId, status)

    suspend fun insertNotification(notification: Notification) = repository.insertNotification(notification)
    suspend fun markNotificationRead(id: Long) = repository.markNotificationRead(id)
    suspend fun markAllNotificationsRead() = repository.markAllNotificationsRead()

    suspend fun insertInvoice(invoice: Invoice) = repository.insertInvoice(invoice)
    suspend fun updateInvoice(invoice: Invoice) = repository.updateInvoice(invoice)
    suspend fun updateInvoiceStatus(id: Long, status: String) = repository.updateInvoiceStatus(id, status)

    suspend fun checkDuplicateName(name: String): Boolean {
        return repository.getStudentByName(name) != null
    }

    fun addNewStudent(
        name: String, gender: String, phone: String, birthday: String, school: String,
        courses: List<Triple<String, Int, Double>>,
        parentName: String = "", parentPhone: String = "", parentRelation: String = "父亲"
    ) {
        viewModelScope.launch {
            try {
                if (repository.getStudentByName(name) != null) {
                    repository.insertNotification(Notification(
                        type = "system", title = "添加失败：重复学生",
                        content = "学生 $name 已存在，无法重复添加。",
                        time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                        priority = "high", isRead = false
                    ))
                    return@launch
                }

                val courseName = courses.firstOrNull()?.first ?: ""
                val totalHours = courses.sumOf { it.second }
                val student = Student(
                    name = name, gender = gender, phone = phone, birthday = birthday,
                    school = school, course = courseName, hours = totalHours,
                    status = "new", createdAt = System.currentTimeMillis()
                )
                val studentId = repository.insertStudent(student)

                if (parentName.isNotBlank()) {
                    repository.insertParent(
                        StudentParent(studentId = studentId, name = parentName,
                            relation = parentRelation, phone = parentPhone, address = "")
                    )
                }

                for ((cName, hours, price) in courses) {
                    val existingCourses = repository.getCourseByName(cName)
                    val courseId = if (existingCourses != null) {
                        existingCourses.id
                    } else {
                        repository.insertCourse(Course(name = cName, type = "individual", price = price, duration = 60))
                    }
                    repository.insertStudentCourse(
                        StudentCourse(studentId = studentId, courseId = courseId,
                            totalHours = hours, remainingHours = hours, price = price)
                    )
                }

                repository.insertNotification(Notification(
                    type = "academic", title = "新学生注册",
                    content = "$name 已成功注册，课程：$courseName，总课时：$totalHours 节",
                    time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                    priority = "medium", isRead = false
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addNewSchedule(
        title: String, studentId: Long?, courseId: Long?, date: String,
        startTime: String, endTime: String, room: String, type: String, students: Int = 1,
        courseName: String? = null
    ) {
        viewModelScope.launch {
            try {
                val finalCourseId = when {
                    courseId != null && courseId > 0 -> courseId
                    courseName != null && courseName.isNotBlank() -> {
                        val existing = repository.getCourseByName(courseName)
                        existing?.id ?: repository.insertCourse(
                            Course(name = courseName, type = type, duration = 60,
                                description = "从排课时自动创建")
                        )
                    }
                    else -> null
                }

                val schedule = Schedule(
                    title = title, studentId = studentId, courseId = finalCourseId,
                    date = date, startTime = startTime, endTime = endTime,
                    room = room, type = type, status = "pending", students = students,
                    location = room
                )
                val scheduleId = repository.insertSchedule(schedule)

                if (studentId != null) {
                    val student = repository.getStudentById(studentId)
                    if (student != null && student.hours > 0) {
                        val updated = student.copy(hours = (student.hours - 1).coerceAtLeast(0))
                        repository.updateStudent(updated)

                        if (updated.hours <= 2) {
                            repository.insertNotification(Notification(
                                type = "academic", title = "课时即将不足",
                                content = "${student.name}的课时即将用完，剩余仅${updated.hours}节，请及时续费。",
                                time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                                priority = "high", isRead = false
                            ))
                        }
                    }
                }

                if (finalCourseId != null && courseName != null) {
                    repository.insertNotification(Notification(
                        type = "academic", title = "新班级已创建",
                        content = "已创建班级「$courseName」并排课：$title ($date $startTime-$endTime, $room)",
                        time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                        priority = "medium", isRead = false
                    ))
                } else {
                    repository.insertNotification(Notification(
                        type = "academic", title = "新增排课",
                        content = "已添加课程：$title ($date $startTime-$endTime, $room)",
                        time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                        priority = "medium", isRead = false
                    ))
                }
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun submitAttendance(scheduleId: Long, records: List<Triple<Long, String, String>>) {
        viewModelScope.launch {
            try {
                for ((studentId, status, note) in records) {
                    repository.insertAttendance(Attendance(
                        scheduleId = scheduleId, studentId = studentId,
                        status = status, note = note, rating = 0
                    ))

                    if (status == "leave") {
                        val schedule = repository.getScheduleById(scheduleId)
                        if (schedule != null) {
                            try {
                                val sdf = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault())
                                val originalDate = sdf.parse(schedule.date) ?: Date()
                                val cal = Calendar.getInstance()
                                cal.time = originalDate
                                cal.add(Calendar.DAY_OF_MONTH, 7)
                                val newDate = sdf.format(cal.time)

                                val existing = repository.getStudentById(studentId)
                                addNewSchedule(
                                    title = "${schedule.title}（补课·${existing?.name ?: ""}）",
                                    studentId = studentId,
                                    courseId = schedule.courseId,
                                    date = newDate,
                                    startTime = schedule.startTime,
                                    endTime = schedule.endTime,
                                    room = schedule.room,
                                    type = schedule.type,
                                    students = 1
                                )

                                repository.insertNotification(Notification(
                                    type = "academic", title = "已自动补课",
                                    content = "${existing?.name ?: "学生"}请假，已自动在 $newDate 安排补课",
                                    time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                                    priority = "high", isRead = false
                                ))
                            } catch (_: Exception) {}
                        }
                    }
                }
                repository.updateScheduleStatus(scheduleId, "completed")

                repository.insertNotification(Notification(
                    type = "academic", title = "考勤已提交",
                    content = "课程考勤已提交，共${records.size}名学生",
                    time = SimpleDateFormat("yyyy-MM-dd HH:mm", Locale.getDefault()).format(Date()),
                    priority = "medium", isRead = false
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addInvoice(studentId: Long, name: String, amount: String, status: String, method: String) {
        viewModelScope.launch {
            try {
                repository.insertInvoice(Invoice(
                    studentId = studentId, name = name, amount = amount,
                    status = status, method = method,
                    date = SimpleDateFormat("yyyy-MM-dd", Locale.getDefault()).format(Date())
                ))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun updateStudentInfo(id: Long, name: String, course: String) {
        viewModelScope.launch {
            try {
                val student = repository.getStudentById(id) ?: return@launch
                repository.updateStudent(student.copy(name = name, course = course))
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun addStudentCourse(studentId: Long, courseName: String, hours: Int, price: Double) {
        viewModelScope.launch {
            try {
                var course = repository.getCourseByName(courseName)
                val courseId = if (course != null) {
                    course.id
                } else {
                    repository.insertCourse(Course(name = courseName, type = "individual", price = price, duration = 60))
                }
                repository.insertStudentCourse(
                    StudentCourse(studentId = studentId, courseId = courseId,
                        totalHours = hours, remainingHours = hours, price = price)
                )
            } catch (e: Exception) {
                e.printStackTrace()
            }
        }
    }

    fun getTodayDate(): String {
        return _todayDate.value
    }

    fun getCurrentYearMonth(): String {
        return _currentYearMonth.value
    }

    companion object {
        const val TAG = "AppViewModel"
    }
}
