package com.example.students.data.database

import android.content.Context
import androidx.room.Database
import androidx.room.Room
import androidx.room.RoomDatabase
import androidx.sqlite.db.SupportSQLiteDatabase
import com.example.students.data.dao.CourseDao
import com.example.students.data.dao.InvoiceDao
import com.example.students.data.dao.NotificationDao
import com.example.students.data.dao.ScheduleDao
import com.example.students.data.dao.StudentDao
import com.example.students.data.model.Attendance
import com.example.students.data.model.Course
import com.example.students.data.model.Invoice
import com.example.students.data.model.Notification
import com.example.students.data.model.Schedule
import com.example.students.data.model.Student
import com.example.students.data.model.StudentCourse
import com.example.students.data.model.StudentParent
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch

@Database(
    entities = [
        Student::class, StudentParent::class, Course::class, StudentCourse::class,
        Schedule::class, Attendance::class, Notification::class, Invoice::class
    ],
    version = 2,
    exportSchema = false
)
abstract class AppDatabase : RoomDatabase() {
    abstract fun studentDao(): StudentDao
    abstract fun scheduleDao(): ScheduleDao
    abstract fun courseDao(): CourseDao
    abstract fun notificationDao(): NotificationDao
    abstract fun invoiceDao(): InvoiceDao

    companion object {
        @Volatile private var INSTANCE: AppDatabase? = null

        fun getInstance(context: Context): AppDatabase {
            return INSTANCE ?: synchronized(this) {
                INSTANCE ?: buildDatabase(context).also { db ->
                    INSTANCE = db
                    CoroutineScope(Dispatchers.IO).launch {
                        populateMockData(db)
                    }
                }
            }
        }

        private fun buildDatabase(context: Context): AppDatabase {
            return Room.databaseBuilder(context, AppDatabase::class.java, "academic_mastery.db")
                .fallbackToDestructiveMigration()
                .build()
        }

        private suspend fun populateMockData(db: AppDatabase) {
            val studentDao = db.studentDao()
            val courseDao = db.courseDao()
            val scheduleDao = db.scheduleDao()
            val notificationDao = db.notificationDao()
            val invoiceDao = db.invoiceDao()

            val now = java.util.Calendar.getInstance()
            val todayStr = "${now.get(java.util.Calendar.YEAR)}-${(now.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')}-${now.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}"
            // future dates: +1 day, +2 days, +3 days, +5 days
            val cal = java.util.Calendar.getInstance()
            fun addDays(days: Int): String {
                cal.time = now.time
                cal.add(java.util.Calendar.DAY_OF_MONTH, days)
                return "${cal.get(java.util.Calendar.YEAR)}-${(cal.get(java.util.Calendar.MONTH) + 1).toString().padStart(2, '0')}-${cal.get(java.util.Calendar.DAY_OF_MONTH).toString().padStart(2, '0')}"
            }
            val yesterday = addDays(-1)
            val tomorrow = addDays(1)
            val dayAfter = addDays(2)
            val day3 = addDays(3)
            val day5 = addDays(5)

            val students = listOf(
                Student(name = "张伟杰", gender = "male", course = "高级钢琴进阶课 / 乐理基础", hours = 2, status = "warning"),
                Student(name = "李慕青", gender = "female", course = "视唱练耳基础 / 声乐训练", hours = 15, status = "normal"),
                Student(name = "王晓东", gender = "male", course = "一对一私教", hours = 24, status = "new"),
                Student(name = "赵子涵", gender = "female", course = "高级钢琴进阶课", hours = 8, status = "normal"),
                Student(name = "陈大明", gender = "male", course = "高级钢琴进阶课", hours = 3, status = "warning"),
                Student(name = "林小梅", gender = "female", course = "视唱练耳基础", hours = 20, status = "normal"),
            )
            val studentIds = students.map { studentDao.insert(it) }

            studentIds.forEachIndexed { index, sid ->
                studentDao.insertParent(StudentParent(studentId = sid, name = "张建国", relation = "父亲", phone = "138 0000 ${10001 + index}", address = "北京市朝阳区阳光花园 8 号楼"))
                studentDao.insertParent(StudentParent(studentId = sid, name = "林秋梅", relation = "母亲", phone = "139 1111 ${20001 + index}", address = "北京市朝阳区阳光花园 8 号楼"))
            }

            val courses = listOf(
                Course(name = "高级钢琴进阶课", type = "group", price = 280.0, duration = 90, color = "#6750A4"),
                Course(name = "视唱练耳基础", type = "group", price = 150.0, duration = 60, color = "#625B71"),
                Course(name = "一对一私教", type = "individual", price = 500.0, duration = 60, color = "#7D5260"),
                Course(name = "乐理基础", type = "group", price = 120.0, duration = 45, color = "#006D40"),
                Course(name = "声乐训练", type = "individual", price = 350.0, duration = 60, color = "#BA1A1A"),
            )
            val courseIds = courses.map { courseDao.insert(it) }

            // StudentCourse junction: create multiple enrollments for multi-course students
            courseDao.insertStudentCourse(StudentCourse(studentId = studentIds[0], courseId = courseIds[0], totalHours = 10, remainingHours = 1, price = 280.0))
            courseDao.insertStudentCourse(StudentCourse(studentId = studentIds[0], courseId = courseIds[3], totalHours = 20, remainingHours = 1, price = 120.0))
            courseDao.insertStudentCourse(StudentCourse(studentId = studentIds[1], courseId = courseIds[1], totalHours = 20, remainingHours = 8, price = 150.0))
            courseDao.insertStudentCourse(StudentCourse(studentId = studentIds[1], courseId = courseIds[4], totalHours = 15, remainingHours = 7, price = 350.0))
            courseDao.insertStudentCourse(StudentCourse(studentId = studentIds[2], courseId = courseIds[2], totalHours = 30, remainingHours = 24, price = 500.0))
            courseDao.insertStudentCourse(StudentCourse(studentId = studentIds[3], courseId = courseIds[0], totalHours = 15, remainingHours = 8, price = 280.0))
            courseDao.insertStudentCourse(StudentCourse(studentId = studentIds[4], courseId = courseIds[0], totalHours = 10, remainingHours = 3, price = 280.0))
            courseDao.insertStudentCourse(StudentCourse(studentId = studentIds[5], courseId = courseIds[1], totalHours = 25, remainingHours = 20, price = 150.0))

            val schedules = listOf(
                Schedule(title = "高级钢琴进阶课", date = todayStr, startTime = "10:00", endTime = "11:30", room = "琴房 302", type = "group", students = 5, location = "琴房 302", status = "active"),
                Schedule(title = "视唱练耳基础", date = todayStr, startTime = "14:00", endTime = "15:00", room = "多媒体教室 A", type = "group", students = 12, location = "多媒体教室 A", status = "pending"),
                Schedule(title = "一对一私教 - 王晓东", date = todayStr, startTime = "16:30", endTime = "18:00", room = "VIP 琴房 1", type = "individual", students = 1, location = "VIP 琴房 1", studentId = studentIds[2], status = "pending"),
                Schedule(title = "高级钢琴进阶课", date = yesterday, startTime = "09:00", endTime = "10:30", room = "琴房 302", type = "group", students = 5, location = "琴房 302", status = "completed"),
                Schedule(title = "视唱练耳基础", date = yesterday, startTime = "14:00", endTime = "15:30", room = "多媒体教室 A", type = "group", students = 8, location = "多媒体教室 A", status = "completed"),
                Schedule(title = "高级钢琴进阶课", date = tomorrow, startTime = "10:00", endTime = "11:30", room = "琴房 302", type = "group", students = 5, location = "琴房 302", status = "pending"),
                Schedule(title = "教研组周会", date = todayStr, startTime = "08:30", endTime = "09:30", room = "会议室 1", type = "group", students = 0, location = "会议室 1", status = "completed"),
                Schedule(title = "一对一私教 - 张伟杰", date = dayAfter, startTime = "09:00", endTime = "10:00", room = "VIP 琴房 2", type = "individual", students = 1, location = "VIP 琴房 2", studentId = studentIds[0], status = "pending"),
                Schedule(title = "声乐训练", date = dayAfter, startTime = "15:00", endTime = "16:30", room = "声乐教室 201", type = "individual", students = 1, location = "声乐教室 201", studentId = studentIds[1], status = "pending"),
                Schedule(title = "乐理基础", date = day3, startTime = "10:00", endTime = "11:00", room = "理论教室 B", type = "group", students = 8, location = "理论教室 B", status = "pending"),
                Schedule(title = "高级钢琴进阶课", date = day5, startTime = "10:00", endTime = "11:30", room = "琴房 302", type = "group", students = 5, location = "琴房 302", status = "pending"),
            )
            schedules.forEach { scheduleDao.insert(it) }

            val notifications = listOf(
                Notification(type = "academic", title = "课程冲突提醒", content = "陈大明同学的钢琴课与王小红的萨克斯课时间重叠", time = "2分钟前", priority = "high", isRead = false, detail = "冲突详情：\n陈大明 - 周二 14:00-15:30 - 琴房302\n王小红 - 周二 14:00-15:30 - 琴房302\n\n建议方案：\n1. 将陈大明调至周三同一时间\n2. 将王小红调至周五同一时间\n3. 调整其中一个课程为不同时段"),
                Notification(type = "admin", title = "系统维护通知", content = "系统将于今晚 02:00-04:00 进行例行维护", time = "1小时前", priority = "medium", isRead = false),
                Notification(type = "system", title = "新版本更新", content = "课时管理系统 v2.4.0 已发布，新增批量签到功能", time = "昨天", priority = "low", isRead = true),
                Notification(type = "academic", title = "续费提醒", content = "张伟杰同学课时即将到期，请及时跟进续费事宜", time = "3天前", priority = "medium", isRead = false),
            )
            notifications.forEach { notificationDao.insert(it) }

            val invoices = listOf(
                Invoice(studentId = studentIds[0], name = "张伟杰", amount = "¥3,600", status = "paid", date = todayStr, method = "微信支付"),
                Invoice(studentId = studentIds[1], name = "李慕青", amount = "¥2,400", status = "pending", date = todayStr, method = "银行转账"),
                Invoice(studentId = studentIds[2], name = "王晓东", amount = "¥1,800", status = "pending", date = todayStr, method = "现金"),
                Invoice(studentId = studentIds[3], name = "赵子涵", amount = "¥4,200", status = "paid", date = yesterday, method = "微信支付"),
                Invoice(studentId = studentIds[4], name = "陈大明", amount = "¥2,800", status = "overdue", date = addDays(-30), method = "银行转账"),
            )
            invoices.forEach { invoiceDao.insert(it) }
        }
    }
}
