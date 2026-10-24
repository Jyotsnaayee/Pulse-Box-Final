package com.example.ui.tools

import android.content.Context
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.example.ui.theme.BackgroundDark
import com.example.ui.theme.BluePriority
import com.example.ui.theme.SurfaceDark
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import java.io.File
import java.io.FileOutputStream
import java.util.zip.ZipInputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageStudioScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val scope = rememberCoroutineScope()
    
    var selectedUri by remember { mutableStateOf<Uri?>(null) }
    var fileName by remember { mutableStateOf("") }
    var isProcessing by remember { mutableStateOf(false) }
    var processType by remember { mutableStateOf("") }
    var resultMessage by remember { mutableStateOf<String?>(null) }
    var safetyStatus by remember { mutableStateOf<String?>(null) }

    val filePicker = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.GetContent()
    ) { uri ->
        selectedUri = uri
        fileName = uri?.let { getFileName(context, it) } ?: ""
        resultMessage = null
        safetyStatus = null
    }

    Scaffold(
        containerColor = BackgroundDark,
        topBar = {
            TopAppBar(
                title = { Text("Package Studio", color = Color.White) },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back", tint = Color.White)
                    }
                },
                colors = TopAppBarDefaults.topAppBarColors(containerColor = BackgroundDark)
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            ToolCard(
                title = "File Selection",
                subtitle = "Select APK, XAPK, or ZIP for processing",
                icon = Icons.Default.FileOpen
            ) {
                Button(
                    onClick = { filePicker.launch("*/*") },
                    modifier = Modifier.fillMaxWidth(),
                    shape = RoundedCornerShape(12.dp),
                    colors = ButtonDefaults.buttonColors(containerColor = BluePriority)
                ) {
                    Text(if (selectedUri == null) "Choose File" else "Change File")
                }
                
                if (selectedUri != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Surface(
                        color = Color.White.copy(alpha = 0.05f),
                        shape = RoundedCornerShape(12.dp),
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Row(modifier = Modifier.padding(12.dp), verticalAlignment = Alignment.CenterVertically) {
                            Icon(Icons.Default.InsertDriveFile, contentDescription = null, tint = BluePriority)
                            Spacer(modifier = Modifier.width(12.dp))
                            Text(fileName, color = Color.White, style = MaterialTheme.typography.bodyMedium)
                        }
                    }
                }
            }

            Spacer(modifier = Modifier.height(16.dp))

    if (selectedUri != null) {
        ToolCard(
            title = "Actions",
            subtitle = "Convert or Extract your package",
            icon = Icons.Default.Build
        ) {
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                ActionButton(
                    modifier = Modifier.weight(1f),
                    text = "Convert to APK",
                    icon = Icons.Default.Transform,
                    onClick = {
                        scope.launch {
                            isProcessing = true
                            processType = "Converting"
                            val result = convertXapkToApk(context, selectedUri!!)
                            resultMessage = if (result != null) "Converted: ${result.name}" else "Conversion Failed"
                            if (result != null) {
                                safetyStatus = checkApkSafety(context, result)
                            }
                            isProcessing = false
                        }
                    }
                )
                ActionButton(
                    modifier = Modifier.weight(1f),
                    text = "Extract ZIP",
                    icon = Icons.Default.Unarchive,
                    onClick = {
                        scope.launch {
                            isProcessing = true
                            processType = "Extracting"
                            val success = extractZip(context, selectedUri!!)
                            resultMessage = if (success) "Extracted to Downloads/extracted" else "Extraction Failed"
                            isProcessing = false
                        }
                    }
                )
            }
        }
    }

    if (isProcessing) {
        Spacer(modifier = Modifier.height(24.dp))
        Column(horizontalAlignment = Alignment.CenterHorizontally) {
            CircularProgressIndicator(color = BluePriority)
            Spacer(modifier = Modifier.height(8.dp))
            Text("$processType...", color = Color.White)
        }
    }

    if (resultMessage != null) {
        Spacer(modifier = Modifier.height(24.dp))
        Card(
            modifier = Modifier.fillMaxWidth(),
            shape = RoundedCornerShape(16.dp),
            colors = CardDefaults.cardColors(containerColor = if (resultMessage!!.contains("Failed")) MaterialTheme.colorScheme.errorContainer else SurfaceDark)
        ) {
            Column(modifier = Modifier.padding(16.dp)) {
                Row(verticalAlignment = Alignment.CenterVertically) {
                    Icon(
                        if (resultMessage!!.contains("Failed")) Icons.Default.Error else Icons.Default.CheckCircle,
                        contentDescription = null,
                        tint = if (resultMessage!!.contains("Failed")) MaterialTheme.colorScheme.error else Color.Green
                    )
                    Spacer(modifier = Modifier.width(12.dp))
                    Text(resultMessage!!, color = Color.White, fontWeight = FontWeight.Bold)
                }
                
                if (safetyStatus != null) {
                    Spacer(modifier = Modifier.height(8.dp))
                    Text(
                        safetyStatus!!,
                        color = if (safetyStatus!!.contains("COMPATIBLE")) Color.Green else MaterialTheme.colorScheme.error,
                        style = MaterialTheme.typography.labelMedium
                    )
                }
            }
        }
    }
}

private fun checkApkSafety(context: Context, file: File): String {
    return try {
        val packageManager = context.packageManager
        val info = packageManager.getPackageArchiveInfo(file.absolutePath, 0)
        if (info == null) {
            "ERROR: Could not read APK info"
        } else {
            val minSdk = info.applicationInfo.minSdkVersion
            val currentSdk = android.os.Build.VERSION.SDK_INT
            if (minSdk > currentSdk) {
                "INCOMPATIBLE: Requires API $minSdk (Current: $currentSdk)"
            } else {
                "COMPATIBLE: Safe for your device"
            }
        }
    } catch (e: Exception) {
        "ERROR: Safety check failed"
    }
}

private suspend fun convertXapkToApk(context: Context, uri: Uri): File? = withContext(Dispatchers.IO) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext null
        val zipInputStream = ZipInputStream(inputStream)
        val outputDir = File(context.getExternalFilesDir(null), "converted")
        if (!outputDir.exists()) outputDir.mkdirs()

        var baseApk: File? = null
        var entry = zipInputStream.nextEntry
        while (entry != null) {
            if (entry.name.endsWith(".apk") && (entry.name.contains("base") || baseApk == null)) {
                val file = File(outputDir, entry.name)
                FileOutputStream(file).use { output ->
                    zipInputStream.copyTo(output)
                }
                baseApk = file
            }
            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()
        baseApk
    } catch (e: Exception) {
        null
    }
}

@Composable
fun ToolCard(title: String, subtitle: String, icon: ImageVector, content: @Composable ColumnScope.() -> Unit) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(containerColor = SurfaceDark)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(verticalAlignment = Alignment.CenterVertically) {
                Surface(
                    modifier = Modifier.size(32.dp),
                    shape = RoundedCornerShape(8.dp),
                    color = BluePriority.copy(alpha = 0.1f)
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        Icon(icon, contentDescription = null, tint = BluePriority, modifier = Modifier.size(16.dp))
                    }
                }
                Spacer(modifier = Modifier.width(12.dp))
                Column {
                    Text(title, style = MaterialTheme.typography.titleMedium, color = Color.White, fontWeight = FontWeight.Bold)
                    Text(subtitle, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
                }
            }
            Spacer(modifier = Modifier.height(20.dp))
            content()
        }
    }
}

@Composable
fun ActionButton(modifier: Modifier, text: String, icon: ImageVector, onClick: () -> Unit) {
    OutlinedButton(
        onClick = onClick,
        modifier = modifier.height(56.dp),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
        colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
    ) {
        Icon(icon, contentDescription = null, modifier = Modifier.size(18.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Text(text, style = MaterialTheme.typography.labelSmall)
    }
}

private fun getFileName(context: Context, uri: Uri): String {
    return uri.path?.split("/")?.last() ?: "Selected File"
}

private suspend fun extractZip(context: Context, uri: Uri): Boolean = withContext(Dispatchers.IO) {
    try {
        val inputStream = context.contentResolver.openInputStream(uri) ?: return@withContext false
        val zipInputStream = ZipInputStream(inputStream)
        val outputDir = File(context.getExternalFilesDir(null), "extracted")
        if (!outputDir.exists()) outputDir.mkdirs()

        var entry = zipInputStream.nextEntry
        while (entry != null) {
            val file = File(outputDir, entry.name)
            if (entry.isDirectory) {
                file.mkdirs()
            } else {
                file.parentFile?.mkdirs()
                FileOutputStream(file).use { output ->
                    zipInputStream.copyTo(output)
                }
            }
            entry = zipInputStream.nextEntry
        }
        zipInputStream.close()
        true
    } catch (e: Exception) {
        false
    }
}
