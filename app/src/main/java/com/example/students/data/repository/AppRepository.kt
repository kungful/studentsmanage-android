package com.example.students.data.repository

import com.example.students.data.dao.CourseDao
import com.example.students.data.dao.InvoiceDao
import com.example.students.data.dao.NotificationDao
import com.example.students.data.dao.ScheduleDao
import com.example.students.data.dao.StudentCourseWithName
import com.example.students.data.dao.StudentDao
import com.example.students.data.model.Attendance
import com.example.students.data.model.Course
import com.example.students.data.model.Invoice
import com.example.students.data.model.Notification
import com.example.students.data.model.Schedule
import com.example.students.data.model.Student
import com.example.students.data.model.StudentCourse
import com.example.students.data.model.StudentParent
import kotlinx.coroutines.flow.Flow

class AppRepository(
    private val studentDao: StudentDao,
    private val scheduleDao: ScheduleDao,
    private val courseDao: CourseDao,
    private val notificationDao: NotificationDao,
    private val invoiceDao: InvoiceDao
) {
    val allStudents: Flow<List<Student>> = studentDao.getAll()
    val allSchedules: Flow<List<Schedule>> = scheduleDao.getAll()
    val allCourses: Flow<List<Course>> = courseDao.getAll()
    val allNotifications: Flow<List<Notification>> = notificationDao.getAll()
    val allInvoices: Flow<List<Invoice>> = invoiceDao.getAll()
    val unreadNotificationCount: Flow<Int> = notificationDao.getUnreadCount()

    fun getLowLessonStudents(threshold: Int = 3) = studentDao.getLowLessonStudents(threshold)
    fun searchStudents(query: String) = studentDao.search(query)
    fun getStudentsByStatus(status: String) = studentDao.getByStatus(status)
    fun getStudentCount() = studentDao.getStudentCount()
    fun getSchedulesByDate(date: String) = scheduleDao.getByDate(date)
    fun getSchedulesByMonth(yearMonth: String) = scheduleDao.getByMonth(yearMonth)
    fun getSchedulesByDateRange(startDate: String, endDate: String) = scheduleDao.getByDateRange(startDate, endDate)
    fun getSchedulesByStudentId(studentId: Long) = scheduleDao.getByStudentId(studentId)
    fun getSchedulesByStudentAndDate(studentId: Long, date: String) = scheduleDao.getByStudentAndDate(studentId, date)
    fun getSchedulesByStudentAndMonth(studentId: Long, yearMonth: String) = scheduleDao.getByStudentAndMonth(studentId, yearMonth)
    fun getSchedulesByStudentCoursesAndMonth(studentId: Long, yearMonth: String) = scheduleDao.getByStudentCoursesAndMonth(studentId, yearMonth)
    fun getSchedulesByStudentCoursesAndDate(studentId: Long, date: String) = scheduleDao.getByStudentCoursesAndDate(studentId, date)
    fun getSchedulesByStudentCourses(studentId: Long) = scheduleDao.getByStudentCourses(studentId)
    fun getSchedulesByStatus(status: String) = scheduleDao.getByStatus(status)
    fun getNotificationsByType(type: String) = notificationDao.getByType(type)
    fun getInvoicesByStatus(status: String) = invoiceDao.getByStatus(status)
    fun getInvoicesByStudentId(studentId: Long) = invoiceDao.getByStudentId(studentId)
    fun getAttendanceCountByStatus(status: String) = scheduleDao.getAttendanceCountByStatus(status)

    suspend fun getStudentsByCourseId(courseId: Long) = studentDao.getByCourseId(courseId)

    suspend fun getStudentById(id: Long) = studentDao.getById(id)
    suspend fun getStudentByName(name: String) = studentDao.getByName(name)
    suspend fun getParentsByStudent(studentId: Long) = studentDao.getParentsByStudent(studentId)
    suspend fun getScheduleById(id: Long) = scheduleDao.getById(id)
    suspend fun getCourseById(id: Long) = courseDao.getById(id)
    suspend fun getCourseByName(name: String) = courseDao.getByName(name)
    suspend fun getNotificationById(id: Long) = notificationDao.getById(id)
    suspend fun getInvoiceById(id: Long) = invoiceDao.getById(id)
    suspend fun getAttendanceBySchedule(scheduleId: Long) = scheduleDao.getAttendanceBySchedule(scheduleId)
    suspend fun getAttendanceByStudent(studentId: Long) = scheduleDao.getAttendanceByStudent(studentId)
    suspend fun getStudentCourses(studentId: Long) = courseDao.getStudentCourses(studentId)
    suspend fun getStudentCoursesWithName(studentId: Long) = courseDao.getStudentCoursesWithName(studentId)

    suspend fun insertStudent(student: Student) = studentDao.insert(student)
    suspend fun updateStudent(student: Student) = studentDao.update(student)
    suspend fun deleteStudent(student: Student) = studentDao.delete(student)
    suspend fun insertParent(parent: StudentParent) = studentDao.insertParent(parent)
    suspend fun updateParent(parent: StudentParent) = studentDao.updateParent(parent)

    suspend fun insertSchedule(schedule: Schedule) = scheduleDao.insert(schedule)
    suspend fun updateSchedule(schedule: Schedule) = scheduleDao.update(schedule)
    suspend fun deleteSchedule(schedule: Schedule) = scheduleDao.delete(schedule)
    suspend fun updateScheduleStatus(id: Long, status: String) = scheduleDao.updateStatus(id, status)

    suspend fun insertCourse(course: Course) = courseDao.insert(course)
    suspend fun updateCourse(course: Course) = courseDao.update(course)
    suspend fun insertStudentCourse(sc: StudentCourse) = courseDao.insertStudentCourse(sc)
    suspend fun updateStudentCourse(sc: StudentCourse) = courseDao.updateStudentCourse(sc)
    suspend fun deleteStudentCourse(sc: StudentCourse) = courseDao.deleteStudentCourse(sc)
    suspend fun deductStudentCourseHours(id: Long, hours: Int) = courseDao.deductHours(id, hours)

    suspend fun insertAttendance(attendance: Attendance) = scheduleDao.insertAttendance(attendance)
    suspend fun updateAttendanceStatus(scheduleId: Long, studentId: Long, status: String) =
        scheduleDao.updateAttendanceStatus(scheduleId, studentId, status)

    suspend fun insertNotification(notification: Notification) = notificationDao.insert(notification)
    suspend fun markNotificationRead(id: Long) = notificationDao.markAsRead(id)
    suspend fun markAllNotificationsRead() = notificationDao.markAllAsRead()

    suspend fun insertInvoice(invoice: Invoice) = invoiceDao.insert(invoice)
    suspend fun updateInvoice(invoice: Invoice) = invoiceDao.update(invoice)
    suspend fun updateInvoiceStatus(id: Long, status: String) = invoiceDao.updateStatus(id, status)
}
