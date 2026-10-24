package com.example.ui.tasks

import android.app.TimePickerDialog
import androidx.compose.animation.core.animateFloatAsState
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.automirrored.filled.KeyboardArrowRight
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.example.data.Priority
import com.example.data.Task
import com.example.ui.theme.*
import java.text.SimpleDateFormat
import java.util.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskScreen(
    viewModel: TaskViewModel,
    onBack: () -> Unit
) {
    val tasks by viewModel.tasks.collectAsStateWithLifecycle()
    val progress by viewModel.completionPercentage.collectAsStateWithLifecycle()
    var showAddTaskDialog by remember { mutableStateOf(false) }

    Scaffold(
        containerColor = MaterialTheme.colorScheme.background,
        topBar = {
            TaskPulseHeader(onBack = onBack)
        },
        floatingActionButton = {
            FloatingActionButton(
                onClick = { showAddTaskDialog = true },
                containerColor = BluePriority,
                contentColor = Color.White,
                shape = CircleShape,
                modifier = Modifier.testTag("add_task_fab").padding(bottom = 16.dp, end = 16.dp)
            ) {
                Icon(Icons.Default.Add, contentDescription = "Add Task")
            }
        }
    ) { innerPadding ->
        LazyColumn(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding),
            contentPadding = PaddingValues(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            item {
                MomentumCard(progress = progress, totalTasks = tasks.size, completedTasks = tasks.count { it.isCompleted })
            }
            
            item {
                ProfileStatusCard(completedCount = tasks.count { it.isCompleted })
            }

            item {
                WorkCompletionFlowPlaceholder()
            }

            item { TaskSectionHeader("Important Work", "High priority & critical focus", RedPriority, Icons.Default.Error) }
            items(tasks.filter { it.priority == Priority.HIGH }) { task ->
                TaskItem(task, onToggle = { viewModel.toggleTaskCompletion(task) }, onDelete = { viewModel.deleteTask(task) })
            }
            if (tasks.none { it.priority == Priority.HIGH }) {
                item { EmptyTaskPlaceholder("Important Work") { showAddTaskDialog = true } }
            }

            item { TaskSectionHeader("Less Important Work", "Medium priority & secondary tasks", AmberPriority, Icons.Default.Schedule) }
            items(tasks.filter { it.priority == Priority.MEDIUM }) { task ->
                TaskItem(task, onToggle = { viewModel.toggleTaskCompletion(task) }, onDelete = { viewModel.deleteTask(task) })
            }
            if (tasks.none { it.priority == Priority.MEDIUM }) {
                item { EmptyTaskPlaceholder("Less Important Work") { showAddTaskDialog = true } }
            }

            item { TaskSectionHeader("Daily Work", "Routine & daily habits", GreenPriority, Icons.Default.Event) }
            items(tasks.filter { it.priority == Priority.LOW }) { task ->
                TaskItem(task, onToggle = { viewModel.toggleTaskCompletion(task) }, onDelete = { viewModel.deleteTask(task) })
            }
            if (tasks.none { it.priority == Priority.LOW }) {
                item { EmptyTaskPlaceholder("Daily Work") { showAddTaskDialog = true } }
            }
            
            item { Spacer(modifier = Modifier.height(80.dp)) }
        }
    }

    if (showAddTaskDialog) {
        AddTaskDialog(
            onDismiss = { showAddTaskDialog = false },
            onConfirm = { title, desc, priority, dueTime, isReminder, duration, sound ->
                viewModel.addTask(title, desc, priority, dueTime, isReminder, duration, sound)
                showAddTaskDialog = false
            }
        )
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun TaskPulseHeader(onBack: () -> Unit) {
    Row(
        modifier = Modifier
            .fillMaxWidth()
            .statusBarsPadding()
            .padding(16.dp),
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.SpaceBetween
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            IconButton(onClick = onBack) {
                Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
            }
            Spacer(modifier = Modifier.width(8.dp))
            Surface(
                modifier = Modifier.size(40.dp),
                shape = RoundedCornerShape(8.dp),
                color = BluePriority
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White)
                }
            }
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text("TaskPulse", style = MaterialTheme.typography.titleLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text("Daily Flow & Momentum", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
            }
        }
        
        Row(verticalAlignment = Alignment.CenterVertically) {
            Icon(Icons.Default.Person, contentDescription = null, tint = BluePriority, modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Surface(
                color = Color.White.copy(alpha = 0.1f),
                shape = RoundedCornerShape(8.dp),
                modifier = Modifier.clickable { /* Reset */ }
            ) {
                Row(modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp), verticalAlignment = Alignment.CenterVertically) {
                    Icon(Icons.Default.Refresh, contentDescription = null, tint = Color.White, modifier = Modifier.size(14.dp))
                    Spacer(modifier = Modifier.width(4.dp))
                    Text("Reset", color = Color.White, style = MaterialTheme.typography.labelSmall)
                }
            }
            Spacer(modifier = Modifier.width(8.dp))
            Icon(Icons.Default.WbSunny, contentDescription = null, tint = AmberPriority)
        }
    }
}

@Composable
fun MomentumCard(progress: Float, totalTasks: Int, completedTasks: Int) {
    val animatedProgress by animateFloatAsState(targetValue = progress, label = "progress")
    
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(32.dp),
                        shape = CircleShape,
                        color = Color(0xFFFF6D00)
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Bolt, contentDescription = null, tint = Color.White, modifier = Modifier.size(16.dp))
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Text("Daily Momentum", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                }
                Surface(
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(12.dp)
                ) {
                    Text(
                        "$completedTasks of $totalTasks tasks",
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            // Progress Bar
            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .height(16.dp)
                    .clip(CircleShape)
                    .background(Color.Black.copy(alpha = 0.3f))
            ) {
                Box(
                    modifier = Modifier
                        .fillMaxWidth(animatedProgress)
                        .fillMaxHeight()
                        .background(Brush.horizontalGradient(RainbowGradient))
                )
            }
            
            Spacer(modifier = Modifier.height(16.dp))
            
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.Bottom
            ) {
                Text(
                    "Add tasks below to start your pulse",
                    style = MaterialTheme.typography.labelSmall,
                    color = Color.White.copy(alpha = 0.4f)
                )
                Text(
                    "${(progress * 100).toInt()}%",
                    style = MaterialTheme.typography.headlineMedium,
                    color = BluePriority,
                    fontWeight = FontWeight.Bold
                )
            }
        }
    }
}

@Composable
fun ProfileStatusCard(completedCount: Int) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Surface(
                        modifier = Modifier.size(40.dp),
                        shape = CircleShape,
                        color = BluePriority
                    ) {
                        Box(contentAlignment = Alignment.Center) {
                            Icon(Icons.Default.Person, contentDescription = null, tint = Color.White)
                        }
                    }
                    Spacer(modifier = Modifier.width(12.dp))
                    Column {
                        Row(verticalAlignment = Alignment.CenterVertically) {
                            Text("Profile & Status", style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.width(4.dp))
                            Icon(Icons.Default.CheckCircle, contentDescription = null, tint = GreenPriority, modifier = Modifier.size(16.dp))
                        }
                        Text("Daily tracker & quest overview", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    }
                }
                Icon(Icons.AutoMirrored.Filled.KeyboardArrowRight, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
            }
            
            Spacer(modifier = Modifier.height(20.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                StatusItem(Modifier.weight(1f), "0 days", "Active", "Since install", Icons.Default.CalendarToday, BluePriority)
                StatusItem(Modifier.weight(1f), "0", "Quests Given", "Total in app", Icons.Default.List, AmberPriority)
                StatusItem(Modifier.weight(1f), completedCount.toString(), "Completed", "Finished", Icons.Default.CheckBox, GreenPriority)
            }
        }
    }
}

@Composable
fun StatusItem(modifier: Modifier, value: String, label: String, sublabel: String, icon: ImageVector, color: Color) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = Color.White.copy(alpha = 0.05f)
    ) {
        Column(modifier = Modifier.padding(12.dp)) {
            Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
            Spacer(modifier = Modifier.height(12.dp))
            Text(value, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
            Text(label, style = MaterialTheme.typography.labelSmall, color = color, fontWeight = FontWeight.Bold)
            Text(sublabel, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
        }
    }
}

@Composable
fun WorkCompletionFlowPlaceholder() {
    Card(
        modifier = Modifier.fillMaxWidth().height(200.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
            Column(horizontalAlignment = Alignment.CenterHorizontally) {
                Icon(Icons.Default.BarChart, contentDescription = null, tint = BluePriority, modifier = Modifier.size(48.dp))
                Spacer(modifier = Modifier.height(8.dp))
                Text("Work Completion Flow", color = Color.White, fontWeight = FontWeight.Bold)
                Text("Dual-bar comparison with trend lines", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
fun TaskSectionHeader(title: String, subtitle: String, color: Color, icon: ImageVector) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(topStart = 24.dp, topEnd = 24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier.padding(16.dp).fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceBetween
        ) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = color.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                        Spacer(modifier = Modifier.width(8.dp))
                        Text("0/0", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                    }
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                }
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
                Icon(Icons.Default.Add, contentDescription = null, tint = color, modifier = Modifier.size(20.dp))
                Spacer(modifier = Modifier.width(8.dp))
                Icon(Icons.Default.KeyboardArrowDown, contentDescription = null, tint = Color.White.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
fun EmptyTaskPlaceholder(section: String, onAdd: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().height(100.dp),
        color = SurfaceDark,
        shape = RoundedCornerShape(bottomStart = 24.dp, bottomEnd = 24.dp)
    ) {
        Column(
            modifier = Modifier.fillMaxSize(),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text("No tasks in $section yet.", style = MaterialTheme.typography.labelMedium, color = Color.White.copy(alpha = 0.4f))
            Spacer(modifier = Modifier.height(12.dp))
            OutlinedButton(
                onClick = onAdd,
                shape = RoundedCornerShape(8.dp),
                colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White),
                border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f))
            ) {
                Icon(Icons.Default.Add, contentDescription = null, modifier = Modifier.size(16.dp))
                Spacer(modifier = Modifier.width(4.dp))
                Text("Add Task", style = MaterialTheme.typography.labelSmall)
            }
        }
    }
}

@Composable
fun TaskItem(task: Task, onToggle: () -> Unit, onDelete: () -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(0.dp), // Items are part of the section card vibe
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Row(
            modifier = Modifier
                .padding(horizontal = 16.dp, vertical = 12.dp)
                .fillMaxWidth(),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Checkbox(
                checked = task.isCompleted,
                onCheckedChange = { onToggle() },
                colors = CheckboxDefaults.colors(
                    checkedColor = GreenPriority,
                    uncheckedColor = Color.White.copy(alpha = 0.3f)
                )
            )
            Spacer(modifier = Modifier.width(8.dp))
            Column(modifier = Modifier.weight(1f)) {
                Text(
                    text = task.title,
                    style = MaterialTheme.typography.bodyLarge,
                    fontWeight = FontWeight.SemiBold,
                    color = if (task.isCompleted) Color.White.copy(alpha = 0.4f) else Color.White
                )
                if (task.description.isNotEmpty()) {
                    Text(
                        text = task.description,
                        style = MaterialTheme.typography.labelSmall,
                        color = Color.White.copy(alpha = 0.3f)
                    )
                }
                if (task.isReminderEnabled) {
                    Spacer(modifier = Modifier.height(4.dp))
                    Row(verticalAlignment = Alignment.CenterVertically) {
                        Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = BluePriority, modifier = Modifier.size(12.dp))
                        Spacer(modifier = Modifier.width(4.dp))
                        Text(
                            text = "Reminder set",
                            style = MaterialTheme.typography.labelSmall,
                            color = BluePriority,
                            fontWeight = FontWeight.Bold
                        )
                    }
                }
            }
            IconButton(onClick = onDelete) {
                Icon(Icons.Default.Delete, contentDescription = "Delete", tint = Color.White.copy(alpha = 0.2f), modifier = Modifier.size(20.dp))
            }
        }
        Divider(modifier = Modifier.padding(horizontal = 16.dp), color = Color.White.copy(alpha = 0.05f))
    }
}

@Composable
fun AddTaskDialog(onDismiss: () -> Unit, onConfirm: (String, String, Priority, Long?, Boolean, Int, String) -> Unit) {
    var title by remember { mutableStateOf("") }
    var desc by remember { mutableStateOf("") }
    var priority by remember { mutableStateOf(Priority.MEDIUM) }
    
    var isReminderEnabled by remember { mutableStateOf(false) }
    val calendar = remember { Calendar.getInstance() }
    var reminderTime by remember { mutableStateOf(calendar.timeInMillis) }
    var reminderDuration by remember { mutableStateOf(30) } // seconds
    var reminderSound by remember { mutableStateOf("System Default") }

    val context = LocalContext.current
    val timeFormatter = remember { SimpleDateFormat("h:mm a", Locale.getDefault()) }

    AlertDialog(
        onDismissRequest = onDismiss,
        title = {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Text("Create New Task", color = Color.White, fontWeight = FontWeight.Bold)
                IconButton(onClick = onDismiss) {
                    Icon(Icons.Default.Close, contentDescription = "Close", tint = Color.White.copy(alpha = 0.6f))
                }
            }
        },
        text = {
            Column(modifier = Modifier.verticalScroll(rememberScrollState())) {
                OutlinedTextField(
                    value = title,
                    onValueChange = { title = it },
                    label = { Text("Task Title *") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePriority,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(12.dp))
                OutlinedTextField(
                    value = desc,
                    onValueChange = { desc = it },
                    label = { Text("Optional Notes") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = OutlinedTextFieldDefaults.colors(
                        focusedBorderColor = BluePriority,
                        unfocusedBorderColor = Color.White.copy(alpha = 0.1f),
                        focusedTextColor = Color.White,
                        unfocusedTextColor = Color.White
                    )
                )
                Spacer(modifier = Modifier.height(24.dp))
                Text("Priority Section", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.6f))
                Spacer(modifier = Modifier.height(12.dp))
                PriorityOption("Important Work", "High priority & critical focus", RedPriority, priority == Priority.HIGH) { priority = Priority.HIGH }
                Spacer(modifier = Modifier.height(8.dp))
                PriorityOption("Less Important Work", "Medium priority & secondary tasks", AmberPriority, priority == Priority.MEDIUM) { priority = Priority.MEDIUM }
                Spacer(modifier = Modifier.height(8.dp))
                PriorityOption("Daily Work", "Routine & daily habits", GreenPriority, priority == Priority.LOW) { priority = Priority.LOW }
                
                Spacer(modifier = Modifier.height(24.dp))
                
                // Reminder Section
                Surface(
                    modifier = Modifier.fillMaxWidth(),
                    color = Color.White.copy(alpha = 0.05f),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Row(
                            modifier = Modifier.fillMaxWidth(),
                            horizontalArrangement = Arrangement.SpaceBetween,
                            verticalAlignment = Alignment.CenterVertically
                        ) {
                            Row(verticalAlignment = Alignment.CenterVertically) {
                                Icon(Icons.Default.NotificationsActive, contentDescription = null, tint = BluePriority, modifier = Modifier.size(20.dp))
                                Spacer(modifier = Modifier.width(12.dp))
                                Column {
                                    Text("Alarm Reminder", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                                    Text("Rings system default alarm or launches Spotify", style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                                }
                            }
                            Switch(
                                checked = isReminderEnabled,
                                onCheckedChange = { isReminderEnabled = it },
                                colors = SwitchDefaults.colors(checkedThumbColor = Color.White, checkedTrackColor = BluePriority)
                            )
                        }

                        if (isReminderEnabled) {
                            Spacer(modifier = Modifier.height(16.dp))
                            Row(
                                modifier = Modifier.fillMaxWidth(),
                                horizontalArrangement = Arrangement.SpaceBetween,
                                verticalAlignment = Alignment.CenterVertically
                            ) {
                                Text(
                                    text = "Alarm Time: ${timeFormatter.format(Date(reminderTime))}",
                                    style = MaterialTheme.typography.bodyMedium,
                                    color = BluePriority
                                )
                                Button(
                                    onClick = {
                                        val timePicker = TimePickerDialog(
                                            context,
                                            { _, hour, minute ->
                                                val cal = Calendar.getInstance().apply {
                                                    timeInMillis = reminderTime
                                                    set(Calendar.HOUR_OF_DAY, hour)
                                                    set(Calendar.MINUTE, minute)
                                                }
                                                reminderTime = cal.timeInMillis
                                            },
                                            calendar.get(Calendar.HOUR_OF_DAY),
                                            calendar.get(Calendar.MINUTE),
                                            false
                                        )
                                        timePicker.show()
                                    },
                                    shape = RoundedCornerShape(8.dp),
                                    colors = ButtonDefaults.buttonColors(containerColor = BluePriority)
                                ) {
                                    Text("Change Time", style = MaterialTheme.typography.labelSmall)
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Alarm Ring Duration", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                                listOf(15, 30, 60, 120).forEach { sec ->
                                    val label = if (sec >= 60) "${sec / 60} min" else "$sec sec"
                                    Surface(
                                        modifier = Modifier.weight(1f).clickable { reminderDuration = sec },
                                        shape = RoundedCornerShape(8.dp),
                                        color = if (reminderDuration == sec) BluePriority else Color.Black.copy(alpha = 0.3f)
                                    ) {
                                        Text(
                                            text = label,
                                            modifier = Modifier.padding(vertical = 8.dp),
                                            textAlign = TextAlign.Center,
                                            style = MaterialTheme.typography.labelSmall,
                                            color = Color.White
                                        )
                                    }
                                }
                            }

                            Spacer(modifier = Modifier.height(16.dp))
                            Text("Alarm Sound Selection", style = MaterialTheme.typography.labelSmall, color = Color.White, fontWeight = FontWeight.Bold)
                            Spacer(modifier = Modifier.height(8.dp))
                            SoundOption("System Default Alarm Tone", "Plays mobile's native loud alarm ringtone", Icons.Default.Notifications, reminderSound == "System Default") { reminderSound = "System Default" }
                            Spacer(modifier = Modifier.height(8.dp))
                            SoundOption("Special: Spotify Music Alarm", "Opens Spotify focus playlist or your chosen music", Icons.Default.MusicNote, reminderSound == "Spotify") { reminderSound = "Spotify" }
                        }
                    }
                }
            }
        },
        confirmButton = {
            Button(
                onClick = { if (title.isNotEmpty()) onConfirm(title, desc, priority, if (isReminderEnabled) reminderTime else null, isReminderEnabled, reminderDuration, reminderSound) },
                modifier = Modifier.fillMaxWidth().padding(horizontal = 24.dp, vertical = 12.dp),
                shape = RoundedCornerShape(12.dp),
                colors = ButtonDefaults.buttonColors(containerColor = BluePriority)
            ) {
                Text("Add Task")
            }
        },
        containerColor = BackgroundDark
    )
}

@Composable
fun SoundOption(title: String, subtitle: String, icon: ImageVector, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier.fillMaxWidth().clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) BluePriority.copy(alpha = 0.2f) else Color.Black.copy(alpha = 0.3f),
        border = BorderStroke(1.dp, if (isSelected) BluePriority else Color.Transparent)
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Icon(icon, contentDescription = null, tint = if (isSelected) BluePriority else Color.White.copy(alpha = 0.6f), modifier = Modifier.size(20.dp))
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelMedium, color = Color.White, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
            }
        }
    }
}

@Composable
fun PriorityOption(title: String, subtitle: String, color: Color, isSelected: Boolean, onClick: () -> Unit) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .clickable { onClick() },
        shape = RoundedCornerShape(12.dp),
        color = if (isSelected) color.copy(alpha = 0.1f) else Color.Transparent,
        border = BorderStroke(1.dp, if (isSelected) color else Color.White.copy(alpha = 0.1f))
    ) {
        Row(
            modifier = Modifier.padding(12.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(12.dp)
                    .clip(CircleShape)
                    .background(color)
            )
            Spacer(modifier = Modifier.width(12.dp))
            Column {
                Text(title, style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
                Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
            }
        }
    }
}
