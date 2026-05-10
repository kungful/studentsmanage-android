# 教学管家 (Academic Mastery)
![日历月视图](https://github.com/kungful/studentsmanage-android/blob/4a27d4c7e5e0ab6823e88503ec3209c7381fb466/see.jpg)
一个基于 Kotlin + Jetpack Compose 的 Android 教学管理系统，专为培训机构、私教老师设计，覆盖学生管理、排课、考勤、课历、收费等全流程。

## 功能概览

### 首页总览
- 今日概况：日期、课时不足提醒（剩余 ≤3 节的学生数量）
- 待办提醒：确认下周课表、续费待跟进
- 今日课程卡片：按时间排列，显示学生姓名、教室、状态（进行中/待上课/已完成/已请假）
- 快速跳转：今日排课详情、完整周表

### 学生管理
- 学生列表支持搜索、按姓名/班级排序
- 批量选择模式：全选、反选、批量删除（含确认弹窗）
- 添加学生时自动检测重名，拦截重复创建
- 学生详情页：头像、已报课程列表（含剩余课时）、学习课历、学习记录



### 日历主界面
- 月视图：日期格子显示课程状态（进行中/已完成/待上课/已请假/无学生），颜色区分清晰
- 今日高亮：主色圆形标记 + 翻月后一键「今天」按钮返回
- 周视图：本周课程列表
- 课程列表：按日期分组，支持编辑/删除单节课




### 排课管理
- 添加排课四步向导：类型 → 选择 → 详情 → 确认
- 支持一对一私教和班级授课
- 班级授课可自定义创建新班级（自动入库）
- 日期/时间选择器（Material3 DatePicker + TimePicker）
- 课程编辑弹窗：修改名称、日期、时间、教室、状态等全部参数


### 课堂管理
- 考勤：到课/请假/缺课三种状态切换
- 课堂表现评分（1-5星）+ 课堂笔记
- 提交考勤后自动标记课程完成



### 智能补课
- 学生请假时自动在原日期 +7 天后创建同名补课
- 通知提醒教师已自动安排补课

### 今日课程
- 展示当天所有有学生的课程
- 支持从日历点击任意日期查看对应课程
- 过滤纯教研组周会等无学生排课



### 课时提醒
- 剩余 ≤3 节的学生列表
- 一键发送续费提醒通知给家长

### 通知中心
- 课时不足预警、续费提醒、课程冲突、系统维护等
- 已读/未读状态区分


## 技术栈

| 类别 | 技术 | 说明 |
|------|------|------|
| 语言 | Kotlin | 100% Kotlin |
| UI | Jetpack Compose + Material 3 | 声明式 UI，Material Design 3 |
| 数据库 | Room (SQLite) | 本地持久化存储 |
| 架构 | MVVM | ViewModel + Repository + DAO |
| 导航 | Compose Navigation | 单 Activity 多页面 |
| 异步 | Kotlin Coroutines + Flow | 响应式数据流 |

## 数据模型

```
Student ──< StudentParent (家长信息)
Student ──< StudentCourse ──> Course (选课关系)
Student ──< Schedule (一对一私教排课)
Course  ──< Schedule (班级排课)
Schedule ──< Attendance (考勤记录)
Student ──< Invoice (收费单)
```

- **StudentCourse** 是多对多关联表，记录 totalHours / remainingHours / price
- **Schedule** 通过 studentId 或 courseId 关联，课程列表查询自动展开 `student_courses` 子查询覆盖班级课
- 学生课历 = studentId 直接排课 + 所报班级的 courseId 排课

## 构建运行

```bash
./gradlew assembleDebug
```

最低 SDK 24，目标 SDK 36。离线应用，无需网络。

## 截图替换

将所有 `https://your-image-host.com/xxx.png` 替换为你的实际图片链接即可。

推荐图床：GitHub Issues / Imgur / 七牛云 / 阿里云 OSS
