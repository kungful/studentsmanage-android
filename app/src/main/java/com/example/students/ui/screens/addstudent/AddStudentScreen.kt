package com.example.students.ui.screens.addstudent

import androidx.compose.animation.AnimatedContent
import androidx.compose.animation.core.RepeatMode
import androidx.compose.animation.core.animateFloat
import androidx.compose.animation.core.infiniteRepeatable
import androidx.compose.animation.core.rememberInfiniteTransition
import androidx.compose.animation.core.tween
import androidx.compose.animation.fadeIn
import androidx.compose.animation.fadeOut
import androidx.compose.animation.slideInHorizontally
import androidx.compose.animation.slideOutHorizontally
import androidx.compose.animation.togetherWith
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.width
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.ArrowDropDown
import androidx.compose.material.icons.filled.CheckCircle
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.DropdownMenu
import androidx.compose.material3.DropdownMenuItem
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.FilterChip
import androidx.compose.material3.FilterChipDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedButton
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TopAppBar
import androidx.compose.material3.TopAppBarDefaults
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.scale
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.input.KeyboardType
import androidx.compose.ui.unit.dp
import com.example.students.data.model.Course
import com.example.students.ui.viewmodel.AppViewModel

data class CourseItem(
    val id: Int,
    val name: String = "",
    val hours: String = "",
    val price: String = ""
)

data class FormData(
    val name: String = "",
    val gender: String = "男",
    val phone: String = "",
    val birthday: String = "",
    val school: String = "",
    val selectedCourses: List<CourseItem> = emptyList()
)

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun AddStudentScreen(viewModel: AppViewModel, onBack: () -> Unit, onSuccess: () -> Unit) {
    var currentStep by remember { mutableIntStateOf(0) }
    var formData by remember { mutableStateOf(FormData()) }
    var courses by remember { mutableStateOf(listOf<CourseItem>()) }
    var parentName by remember { mutableStateOf("") }
    var parentPhone by remember { mutableStateOf("") }
    var parentRelation by remember { mutableStateOf("父亲") }

    val steps = listOf("基本信息", "课程选择", "确认信息")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text(if (currentStep == 3) "" else "添加学生") },
                navigationIcon = {
                    if (currentStep < 3) {
                        IconButton(onClick = {
                            when {
                                currentStep > 0 -> currentStep--
                                else -> onBack()
                            }
                        }) {
                            Icon(Icons.Default.ArrowBack, contentDescription = "返回")
                        }
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(
                    containerColor = MaterialTheme.colorScheme.surface
                )
            )
        }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .padding(horizontal = 16.dp)
        ) {
            if (currentStep < 3) {
                StepIndicator(
                    steps = steps,
                    currentStep = currentStep,
                    modifier = Modifier
                        .fillMaxWidth()
                        .padding(vertical = 16.dp)
                )
            }

            AnimatedContent(
                targetState = currentStep,
                transitionSpec = {
                    val direction = if (targetState > initialState) 1 else -1
                    (slideInHorizontally { width -> direction * width } + fadeIn())
                        .togetherWith(slideOutHorizontally { width -> -direction * width } + fadeOut())
                },
                label = "step_transition"
            ) { step ->
                when (step) {
                    0 -> BasicInfoStep(
                        formData = formData,
                        onFormDataChange = { formData = it },
                        parentName = parentName,
                        onParentNameChange = { parentName = it },
                        parentPhone = parentPhone,
                        onParentPhoneChange = { parentPhone = it },
                        parentRelation = parentRelation,
                        onParentRelationChange = { parentRelation = it },
                        onNext = { currentStep++ }
                    )
                    1 -> CoursesStep(
                        courses = courses,
                        onCoursesChange = { courses = it },
                        viewModel = viewModel,
                        onNext = { currentStep++ }
                    )
                    2 -> SummaryStep(
                        formData = formData,
                        courses = courses,
                        parentName = parentName,
                        parentPhone = parentPhone,
                        parentRelation = parentRelation,
                        viewModel = viewModel,
                        onSubmit = { currentStep = 3 }
                    )
                    3 -> SuccessStep(
                        onGoHome = onSuccess,
                        onBack = onBack
                    )
                }
            }
        }
    }
}

@Composable
private fun StepIndicator(
    steps: List<String>,
    currentStep: Int,
    modifier: Modifier = Modifier
) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.SpaceEvenly,
        verticalAlignment = Alignment.CenterVertically
    ) {
        steps.forEachIndexed { index, label ->
            val isActive = index <= currentStep
            val isCurrent = index == currentStep

            Column(
                horizontalAlignment = Alignment.CenterHorizontally,
                modifier = Modifier.weight(1f)
            ) {
                Surface(
                    shape = CircleShape,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.surfaceVariant,
                    modifier = Modifier.size(32.dp)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        if (index < currentStep) {
                            Icon(
                                Icons.Default.CheckCircle,
                                contentDescription = null,
                                tint = MaterialTheme.colorScheme.onPrimary,
                                modifier = Modifier.size(18.dp)
                            )
                        } else {
                            Text(
                                text = "${index + 1}",
                                color = if (isActive) MaterialTheme.colorScheme.onPrimary
                                else MaterialTheme.colorScheme.onSurfaceVariant,
                                style = MaterialTheme.typography.bodySmall,
                                fontWeight = FontWeight.Bold
                            )
                        }
                    }
                }
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                    text = label,
                    style = MaterialTheme.typography.labelSmall,
                    color = if (isActive) MaterialTheme.colorScheme.primary
                    else MaterialTheme.colorScheme.onSurfaceVariant,
                    fontWeight = if (isCurrent) FontWeight.Bold else FontWeight.Normal
                )
            }

            if (index < steps.lastIndex) {
                Box(
                    modifier = Modifier
                        .weight(0.5f)
                        .padding(bottom = 24.dp)
                ) {
                    Surface(
                        color = if (index < currentStep) MaterialTheme.colorScheme.primary
                        else MaterialTheme.colorScheme.surfaceVariant,
                        modifier = Modifier
                            .fillMaxWidth()
                            .height(2.dp)
                    ) {}
                }
            }
        }
    }
}

@Composable
private fun BasicInfoStep(
    formData: FormData,
    onFormDataChange: (FormData) -> Unit,
    parentName: String,
    onParentNameChange: (String) -> Unit,
    parentPhone: String,
    onParentPhoneChange: (String) -> Unit,
    parentRelation: String,
    onParentRelationChange: (String) -> Unit,
    onNext: () -> Unit
) {
    val relationOptions = listOf("父亲", "母亲", "爷爷", "奶奶", "外公", "外婆", "其他")
    var relationExpanded by remember { mutableStateOf(false) }

    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            OutlinedTextField(
                value = formData.name,
                onValueChange = { onFormDataChange(formData.copy(name = it)) },
                label = { Text("姓名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            Text(
                "性别",
                style = MaterialTheme.typography.bodyMedium,
                color = MaterialTheme.colorScheme.onSurface
            )
            Spacer(modifier = Modifier.height(8.dp))
            Row(horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                FilterChip(
                    selected = formData.gender == "男",
                    onClick = { onFormDataChange(formData.copy(gender = "男")) },
                    label = { Text("男") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
                FilterChip(
                    selected = formData.gender == "女",
                    onClick = { onFormDataChange(formData.copy(gender = "女")) },
                    label = { Text("女") },
                    colors = FilterChipDefaults.filterChipColors(
                        selectedContainerColor = MaterialTheme.colorScheme.primaryContainer
                    )
                )
            }
        }

        item {
            OutlinedTextField(
                value = formData.birthday,
                onValueChange = { onFormDataChange(formData.copy(birthday = it)) },
                label = { Text("出生日期") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                placeholder = { Text("例如：2010-01-01") }
            )
        }

        item {
            OutlinedTextField(
                value = formData.phone,
                onValueChange = { onFormDataChange(formData.copy(phone = it)) },
                label = { Text("联系电话") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }

        item {
            OutlinedTextField(
                value = formData.school,
                onValueChange = { onFormDataChange(formData.copy(school = it)) },
                label = { Text("学校") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            Text(
                "家长信息",
                style = MaterialTheme.typography.titleSmall,
                fontWeight = FontWeight.Bold,
                color = MaterialTheme.colorScheme.primary
            )
        }

        item {
            OutlinedTextField(
                value = parentName,
                onValueChange = onParentNameChange,
                label = { Text("家长姓名") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true
            )
        }

        item {
            OutlinedTextField(
                value = parentPhone,
                onValueChange = onParentPhoneChange,
                label = { Text("家长电话") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Phone)
            )
        }

        item {
            Box(modifier = Modifier.fillMaxWidth()) {
                OutlinedTextField(
                    value = parentRelation,
                    onValueChange = {},
                    label = { Text("家长关系") },
                    modifier = Modifier.fillMaxWidth(),
                    singleLine = true,
                    readOnly = true,
                    trailingIcon = {
                        IconButton(onClick = { relationExpanded = true }) {
                            Icon(Icons.Default.ArrowDropDown, contentDescription = "选择关系")
                        }
                    },
                    enabled = false,
                    colors = androidx.compose.material3.OutlinedTextFieldDefaults.colors(
                        disabledTextColor = MaterialTheme.colorScheme.onSurface,
                        disabledBorderColor = MaterialTheme.colorScheme.outline,
                        disabledLabelColor = MaterialTheme.colorScheme.onSurfaceVariant
                    )
                )
                DropdownMenu(
                    expanded = relationExpanded,
                    onDismissRequest = { relationExpanded = false }
                ) {
                    relationOptions.forEach { option ->
                        DropdownMenuItem(
                            text = { Text(option) },
                            onClick = {
                                onParentRelationChange(option)
                                relationExpanded = false
                            }
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = onNext,
                modifier = Modifier.fillMaxWidth(),
                enabled = formData.name.isNotBlank()
            ) {
                Text("下一步：配置课程")
            }
        }
    }
}

@Composable
private fun CoursesStep(
    courses: List<CourseItem>,
    onCoursesChange: (List<CourseItem>) -> Unit,
    viewModel: AppViewModel,
    onNext: () -> Unit
) {
    val existingCourses by viewModel.allCourses.collectAsState(initial = emptyList())

    Column(modifier = Modifier.fillMaxSize()) {
        LazyColumn(
            modifier = Modifier.weight(1f),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            if (existingCourses.isNotEmpty()) {
                item {
                    Text(
                        "从现有课程中选择",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
                items(existingCourses) { course ->
                    val alreadySelected = courses.any { it.name == course.name }
                    Card(
                        modifier = Modifier
                            .fillMaxWidth()
                            .clickable(enabled = !alreadySelected) {
                                val newId = (courses.maxOfOrNull { it.id } ?: 0) + 1
                                onCoursesChange(
                                    courses + CourseItem(
                                        id = newId,
                                        name = course.name,
                                        hours = "",
                                        price = if (course.price > 0) course.price.toInt().toString() else ""
                                    )
                                )
                            },
                        colors = CardDefaults.cardColors(
                            containerColor = if (alreadySelected)
                                MaterialTheme.colorScheme.primaryContainer
                            else MaterialTheme.colorScheme.surfaceVariant
                        )
                    ) {
                        Row(
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(12.dp),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Column {
                                Text(
                                    course.name,
                                    style = MaterialTheme.typography.bodyMedium,
                                    fontWeight = FontWeight.Bold
                                )
                                Text(
                                    if (course.price > 0) "¥${course.price} / ${course.duration}分钟"
                                    else "${course.duration}分钟",
                                    style = MaterialTheme.typography.bodySmall,
                                    color = MaterialTheme.colorScheme.onSurfaceVariant
                                )
                            }
                            if (alreadySelected) {
                                Text(
                                    "已添加",
                                    style = MaterialTheme.typography.labelSmall,
                                    color = MaterialTheme.colorScheme.primary
                                )
                            }
                        }
                    }
                }
                item {
                    Spacer(modifier = Modifier.height(12.dp))
                    Text(
                        "或自定义新课程",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                }
            }

            item {
                Button(
                    onClick = {
                        val newId = (courses.maxOfOrNull { it.id } ?: 0) + 1
                        onCoursesChange(courses + CourseItem(id = newId))
                    },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Icon(Icons.Default.Add, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("添加课程")
                }
            }

            itemsIndexed(courses) { index, course ->
                Card(
                    modifier = Modifier.fillMaxWidth(),
                    colors = CardDefaults.cardColors(
                        containerColor = MaterialTheme.colorScheme.surfaceVariant
                    )
                ) {
                    Column(modifier = Modifier.padding(12.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Text(
                                "课程 ${index + 1}",
                                style = MaterialTheme.typography.titleSmall,
                                fontWeight = FontWeight.Bold
                            )
                            IconButton(onClick = {
                                onCoursesChange(courses.toMutableList().apply { removeAt(index) })
                            }) {
                                Icon(
                                    Icons.Default.Delete,
                                    contentDescription = "删除",
                                    tint = MaterialTheme.colorScheme.error
                                )
                            }
                        }

                        OutlinedTextField(
                            value = course.name,
                            onValueChange = { v ->
                                onCoursesChange(courses.toMutableList().apply {
                                    this[index] = this[index].copy(name = v)
                                })
                            },
                            label = { Text("课程名称") },
                            modifier = Modifier.fillMaxWidth(),
                            singleLine = true
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                            OutlinedTextField(
                                value = course.hours,
                                onValueChange = { v ->
                                    onCoursesChange(courses.toMutableList().apply {
                                        this[index] = this[index].copy(hours = v)
                                    })
                                },
                                label = { Text("课时") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Number)
                            )
                            OutlinedTextField(
                                value = course.price,
                                onValueChange = { v ->
                                    onCoursesChange(courses.toMutableList().apply {
                                        this[index] = this[index].copy(price = v)
                                    })
                                },
                                label = { Text("费用") },
                                modifier = Modifier.weight(1f),
                                singleLine = true,
                                keyboardOptions = KeyboardOptions(keyboardType = KeyboardType.Decimal)
                            )
                        }
                    }
                }
            }
        }

        Spacer(modifier = Modifier.height(16.dp))
        Button(
            onClick = onNext,
            modifier = Modifier.fillMaxWidth(),
            enabled = courses.isNotEmpty()
        ) {
            Text("下一步：审核确认")
        }
    }
}

@Composable
private fun SummaryStep(
    formData: FormData,
    courses: List<CourseItem>,
    parentName: String,
    parentPhone: String,
    parentRelation: String,
    viewModel: AppViewModel,
    onSubmit: () -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(12.dp)
    ) {
        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "基本信息",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    InfoRow("姓名", formData.name)
                    InfoRow("性别", formData.gender)
                    InfoRow("出生日期", formData.birthday)
                    InfoRow("联系电话", formData.phone)
                    InfoRow("学校", formData.school)
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        "家长信息",
                        style = MaterialTheme.typography.titleSmall,
                        fontWeight = FontWeight.Bold,
                        color = MaterialTheme.colorScheme.primary
                    )
                    Spacer(modifier = Modifier.height(4.dp))
                    InfoRow("家长姓名", parentName)
                    InfoRow("家长电话", parentPhone)
                    InfoRow("家长关系", parentRelation)
                }
            }
        }

        item {
            Card(
                modifier = Modifier.fillMaxWidth(),
                colors = CardDefaults.cardColors(
                    containerColor = MaterialTheme.colorScheme.surfaceVariant
                )
            ) {
                Column(modifier = Modifier.padding(16.dp)) {
                    Text(
                        "课程信息",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold
                    )
                    Spacer(modifier = Modifier.height(8.dp))
                    if (courses.isEmpty()) {
                        Text(
                            "未选择课程",
                            color = MaterialTheme.colorScheme.onSurfaceVariant
                        )
                    } else {
                        courses.forEachIndexed { index, course ->
                            Text(
                                "${index + 1}. ${course.name} - ${course.hours}课时 - ¥${course.price}",
                                style = MaterialTheme.typography.bodyMedium
                            )
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        val totalHours = courses.sumOf { it.hours.toIntOrNull() ?: 0 }
                        val totalPrice = courses.sumOf { it.price.toDoubleOrNull() ?: 0.0 }
                        Text(
                            "课程数量：${courses.size}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "总课时：$totalHours",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                        Text(
                            "总费用：¥${"%.2f".format(totalPrice)}",
                            style = MaterialTheme.typography.bodyMedium,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
        }

        item {
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = {
                    viewModel.addNewStudent(
                        name = formData.name,
                        gender = formData.gender,
                        phone = formData.phone,
                        birthday = formData.birthday,
                        school = formData.school,
                        courses = courses.map { Triple(it.name, it.hours.toIntOrNull() ?: 0, it.price.toDoubleOrNull() ?: 0.0) },
                        parentName = parentName,
                        parentPhone = parentPhone,
                        parentRelation = parentRelation
                    )
                    onSubmit()
                },
                modifier = Modifier.fillMaxWidth()
            ) {
                Text("确认并提交注册")
            }
        }
    }
}

@Composable
private fun InfoRow(label: String, value: String) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = 2.dp)
    ) {
        Text(
            "$label：",
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant,
            modifier = Modifier.width(80.dp)
        )
        Text(
            value.ifBlank { "未填写" },
            style = MaterialTheme.typography.bodyMedium,
            color = MaterialTheme.colorScheme.onSurface
        )
    }
}

@Composable
private fun SuccessStep(
    onGoHome: () -> Unit,
    onBack: () -> Unit
) {
    val infiniteTransition = rememberInfiniteTransition(label = "success_pulse")
    val scale by infiniteTransition.animateFloat(
        initialValue = 0.8f,
        targetValue = 1.2f,
        animationSpec = infiniteRepeatable(
            animation = tween(600),
            repeatMode = RepeatMode.Reverse
        ),
        label = "pulse_scale"
    )

    Column(
        modifier = Modifier.fillMaxSize(),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        Icon(
            Icons.Default.CheckCircle,
            contentDescription = null,
            tint = MaterialTheme.colorScheme.primary,
            modifier = Modifier
                .size(96.dp)
                .scale(scale)
        )

        Spacer(modifier = Modifier.height(24.dp))

        Text(
            "注册成功！",
            style = MaterialTheme.typography.headlineMedium,
            fontWeight = FontWeight.Bold,
            color = MaterialTheme.colorScheme.primary
        )

        Spacer(modifier = Modifier.height(8.dp))

        Text(
            "学生信息已成功提交",
            style = MaterialTheme.typography.bodyLarge,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )

        Spacer(modifier = Modifier.height(48.dp))

        Button(
            onClick = onGoHome,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("回到首页")
        }

        Spacer(modifier = Modifier.height(12.dp))

        OutlinedButton(
            onClick = onBack,
            modifier = Modifier.fillMaxWidth()
        ) {
            Text("返回学生列表")
        }
    }
}
