package com.example.ui.tools

import android.content.Context
import android.content.Intent
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.BorderStroke
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
import com.example.ui.theme.*
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Primary
import java.io.File
import java.io.FileOutputStream
import java.io.InputStream
import java.util.zip.ZipEntry
import java.util.zip.ZipInputStream
import androidx.documentfile.provider.DocumentFile

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun PackageStudioScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var selectedTab by remember { mutableStateOf(0) }
    val tabs = listOf("APK Info", "Split Converter", "ZIP Extractor")

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Package Studio") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(modifier = Modifier.padding(innerPadding)) {
            TabRow(selectedTabIndex = selectedTab) {
                tabs.forEachIndexed { index, title ->
                    Tab(
                        selected = selectedTab == index,
                        onClick = { selectedTab = index },
                        text = { Text(title) }
                    )
                }
            }

            when (selectedTab) {
                0 -> ApkInfoTab()
                1 -> SplitConverterTab()
                2 -> ZipExtractorTab()
            }
        }
    }
}

@Composable
fun ApkInfoTab() {
    val context = LocalContext.current
    var apkInfo by remember { mutableStateOf<PackageInfo?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            val info = getApkInfo(context, uri)
            if (info != null) {
                apkInfo = info
                errorMessage = null
            } else {
                apkInfo = null
                errorMessage = "Could not parse APK info"
            }
        }
    }

    Column(
        modifier = Modifier
            .fillMaxSize()
            .padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (apkInfo == null) {
            ToolButton(
                text = "Select APK File",
                icon = Icons.Default.FileOpen,
                onClick = { launcher.launch("application/vnd.android.package-archive") }
            )
            if (errorMessage != null) {
                Spacer(modifier = Modifier.height(16.dp))
                Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
            }
        } else {
            ApkDetailCard(apkInfo!!) { apkInfo = null }
        }
    }
}

@Composable
fun SplitConverterTab() {
    val context = LocalContext.current
    var splitUri by remember { mutableStateOf<Uri?>(null) }
    var splits by remember { mutableStateOf<List<String>>(emptyList()) }
    var isProcessing by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            splitUri = uri
            splits = listZipContents(context, uri)
        }
    }

    val saveLauncher = rememberLauncherForActivityResult(ActivityResultContracts.CreateDocument("application/vnd.android.package-archive")) { uri ->
        if (uri != null && splitUri != null) {
            isProcessing = true
            extractFileFromZip(context, splitUri!!, "base.apk", uri) { success ->
                isProcessing = false
                if (success) {
                    Toast.makeText(context, "Converted & Saved successfully!", Toast.LENGTH_LONG).show()
                } else {
                    Toast.makeText(context, "Failed to convert. Ensure it's a valid APKS/XAPKS", Toast.LENGTH_LONG).show()
                }
            }
        }
    }

    Column(modifier = Modifier.fillMaxSize().padding(16.dp)) {
        if (splitUri == null) {
            Box(modifier = Modifier.fillMaxSize(), contentAlignment = Alignment.Center) {
                ToolButton(
                    text = "Select APKS / XAPKS",
                    icon = Icons.Default.Inventory,
                    onClick = { pickerLauncher.launch("*/*") }
                )
            }
        } else {
            Text("Container Contents:", style = MaterialTheme.typography.titleMedium)
            Spacer(modifier = Modifier.height(8.dp))
            LazyColumn(modifier = Modifier.weight(1f)) {
                items(splits) { name ->
                    Card(
                        modifier = Modifier.fillMaxWidth().padding(vertical = 4.dp),
                        colors = CardDefaults.cardColors(containerColor = MaterialTheme.colorScheme.surfaceVariant)
                    ) {
                        Text(name, modifier = Modifier.padding(12.dp), style = MaterialTheme.typography.bodySmall)
                    }
                }
            }
            Spacer(modifier = Modifier.height(16.dp))
            Button(
                onClick = { saveLauncher.launch("extracted_base.apk") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isProcessing && splits.any { it.contains("base.apk", ignoreCase = true) }
            ) {
                if (isProcessing) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                else Text("Convert to Standard APK (Extract Base)")
            }
            TextButton(onClick = { splitUri = null; splits = emptyList() }, modifier = Modifier.fillMaxWidth()) {
                Text("Select Another")
            }
        }
    }
}

@Composable
fun ZipExtractorTab() {
    val context = LocalContext.current
    var zipUri by remember { mutableStateOf<Uri?>(null) }
    var isExtracting by remember { mutableStateOf(false) }

    val pickerLauncher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            zipUri = uri
        }
    }

    val folderLauncher = rememberLauncherForActivityResult(ActivityResultContracts.OpenDocumentTree()) { uri ->
        if (uri != null && zipUri != null) {
            isExtracting = true
            extractZipToFolder(context, zipUri!!, uri) { count ->
                isExtracting = false
                if (count > 0) Toast.makeText(context, "Extracted $count files successfully!", Toast.LENGTH_LONG).show()
                else Toast.makeText(context, "Extraction failed", Toast.LENGTH_SHORT).show()
            }
        }
    }

    Column(
        modifier = Modifier.fillMaxSize().padding(16.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.Center
    ) {
        if (zipUri == null) {
            ToolButton(
                text = "Select ZIP File",
                icon = Icons.Default.FolderZip,
                onClick = { pickerLauncher.launch("application/zip") }
            )
        } else {
            Icon(Icons.Default.FolderZip, contentDescription = null, modifier = Modifier.size(64.dp), tint = Primary)
            Spacer(modifier = Modifier.height(16.dp))
            Text("Selected: ${zipUri?.path?.split("/")?.lastOrNull()}")
            Spacer(modifier = Modifier.height(24.dp))
            Button(
                onClick = { folderLauncher.launch(null) },
                modifier = Modifier.fillMaxWidth(),
                enabled = !isExtracting
            ) {
                if (isExtracting) CircularProgressIndicator(modifier = Modifier.size(24.dp), color = Color.White)
                else Text("Select Destination & Extract")
            }
            TextButton(onClick = { zipUri = null }) {
                Text("Cancel")
            }
        }
    }
}

@Composable
fun ToolButton(text: String, icon: ImageVector, onClick: () -> Unit) {
    Button(
        onClick = onClick,
        modifier = Modifier.fillMaxWidth(0.8f).height(64.dp),
        shape = RoundedCornerShape(16.dp)
    ) {
        Icon(icon, contentDescription = null)
        Spacer(modifier = Modifier.width(12.dp))
        Text(text)
    }
}

@Composable
fun ApkDetailCard(info: PackageInfo, onClear: () -> Unit) {
    val context = LocalContext.current
    val analysis = remember(info) { analyzeApk(context, info) }

    ElevatedCard(
        modifier = Modifier.fillMaxWidth(),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(modifier = Modifier.padding(20.dp)) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
            ) {
                Column(modifier = Modifier.weight(1f)) {
                    Text(
                        text = info.packageName ?: "Unknown Package",
                        style = MaterialTheme.typography.titleMedium,
                        fontWeight = FontWeight.Bold,
                        color = Color.White
                    )
                    Text(
                        text = "Version: ${info.versionName} (${info.versionCode})",
                        style = MaterialTheme.typography.bodySmall,
                        color = Color.White.copy(alpha = 0.6f)
                    )
                }
                
                // Status Badge
                Surface(
                    color = analysis.statusColor.copy(alpha = 0.2f),
                    shape = RoundedCornerShape(8.dp),
                    border = BorderStroke(1.dp, analysis.statusColor)
                ) {
                    Text(
                        text = analysis.statusText,
                        modifier = Modifier.padding(horizontal = 8.dp, vertical = 4.dp),
                        style = MaterialTheme.typography.labelSmall,
                        color = analysis.statusColor,
                        fontWeight = FontWeight.Bold
                    )
                }
            }

            Spacer(modifier = Modifier.height(20.dp))
            Divider(color = Color.White.copy(alpha = 0.05f))
            Spacer(modifier = Modifier.height(20.dp))

            // Analysis Results
            Text("Analysis Results", style = MaterialTheme.typography.labelLarge, color = Color.White, fontWeight = FontWeight.Bold)
            Spacer(modifier = Modifier.height(12.dp))
            
            AnalysisItem(
                label = "Compatibility",
                value = if (analysis.isCompatible) "OK - Supported" else "Incompatible - Min SDK ${info.applicationInfo?.minSdkVersion ?: "Unknown"}",
                icon = if (analysis.isCompatible) Icons.Default.CheckCircle else Icons.Default.Error,
                color = if (analysis.isCompatible) GreenPriority else RedPriority
            )
            
            Spacer(modifier = Modifier.height(8.dp))
            
            AnalysisItem(
                label = "Security Check",
                value = analysis.securityMessage,
                icon = if (analysis.isSuspicious) Icons.Default.Warning else Icons.Default.Shield,
                color = if (analysis.isSuspicious) AmberPriority else GreenPriority
            )

            if (analysis.suspiciousPermissions.isNotEmpty()) {
                Spacer(modifier = Modifier.height(12.dp))
                Text(
                    "Sensitive Permissions Detected:",
                    style = MaterialTheme.typography.labelSmall,
                    color = AmberPriority,
                    fontWeight = FontWeight.Bold
                )
                Text(
                    analysis.suspiciousPermissions.joinToString(", "),
                    style = MaterialTheme.typography.bodySmall,
                    color = Color.White.copy(alpha = 0.6f)
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
            
            Row(modifier = Modifier.fillMaxWidth(), horizontalArrangement = Arrangement.spacedBy(12.dp)) {
                Button(
                    onClick = { /* Install logic */ },
                    modifier = Modifier.weight(1f),
                    colors = ButtonDefaults.buttonColors(containerColor = Primary),
                    enabled = analysis.isCompatible
                ) {
                    Icon(Icons.Default.Download, contentDescription = null, modifier = Modifier.size(18.dp))
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Install")
                }
                OutlinedButton(
                    onClick = onClear,
                    modifier = Modifier.weight(1f),
                    border = BorderStroke(1.dp, Color.White.copy(alpha = 0.1f)),
                    colors = ButtonDefaults.outlinedButtonColors(contentColor = Color.White)
                ) {
                    Text("Clear")
                }
            }
        }
    }
}

@Composable
fun AnalysisItem(label: String, value: String, icon: ImageVector, color: Color) {
    Row(verticalAlignment = Alignment.CenterVertically) {
        Icon(icon, contentDescription = null, tint = color, modifier = Modifier.size(16.dp))
        Spacer(modifier = Modifier.width(8.dp))
        Column {
            Text(label, style = MaterialTheme.typography.labelSmall, color = Color.White.copy(alpha = 0.4f))
            Text(value, style = MaterialTheme.typography.bodySmall, color = Color.White)
        }
    }
}

data class ApkAnalysis(
    val isCompatible: Boolean,
    val isSuspicious: Boolean,
    val securityMessage: String,
    val suspiciousPermissions: List<String>,
    val statusText: String,
    val statusColor: Color
)

private fun analyzeApk(context: Context, info: PackageInfo): ApkAnalysis {
    val currentSdk = android.os.Build.VERSION.SDK_INT
    val minSdk = info.applicationInfo?.minSdkVersion ?: 0
    val isCompatible = currentSdk >= minSdk
    
    val sensitivePermissions = listOf(
        "android.permission.SEND_SMS",
        "android.permission.RECEIVE_SMS",
        "android.permission.READ_CALL_LOG",
        "android.permission.PROCESS_OUTGOING_CALLS",
        "android.permission.READ_SMS",
        "android.permission.RECORD_AUDIO",
        "android.permission.ACCESS_FINE_LOCATION"
    )
    
    val detectedSensitive = info.requestedPermissions?.filter { it in sensitivePermissions } ?: emptyList()
    val isSuspicious = detectedSensitive.size >= 3 || (detectedSensitive.contains("android.permission.SEND_SMS") && detectedSensitive.size >= 2)
    
    val securityMessage = when {
        isSuspicious -> "Suspicious - Multiple sensitive permissions"
        detectedSensitive.isNotEmpty() -> "Warning - Uses sensitive permissions"
        else -> "OK - No critical risks detected"
    }
    
    val (statusText, statusColor) = when {
        !isCompatible -> "Incompatible" to RedPriority
        isSuspicious -> "Risk Detected" to AmberPriority
        else -> "OK" to GreenPriority
    }
    
    return ApkAnalysis(
        isCompatible = isCompatible,
        isSuspicious = isSuspicious,
        securityMessage = securityMessage,
        suspiciousPermissions = detectedSensitive.map { it.split(".").last() },
        statusText = statusText,
        statusColor = statusColor
    )
}

// Logic Helpers

private fun getApkInfo(context: Context, uri: Uri): PackageInfo? {
    return try {
        val file = File(context.cacheDir, "temp_apk.apk")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        context.packageManager.getPackageArchiveInfo(file.absolutePath, PackageManager.GET_PERMISSIONS)
    } catch (e: Exception) {
        null
    }
}

private fun listZipContents(context: Context, uri: Uri): List<String> {
    val list = mutableListOf<String>()
    try {
        context.contentResolver.openInputStream(uri)?.use { input ->
            val zis = ZipInputStream(input)
            var entry: ZipEntry? = zis.nextEntry
            while (entry != null) {
                list.add(entry.name)
                entry = zis.nextEntry
            }
        }
    } catch (e: Exception) {
        e.printStackTrace()
    }
    return list
}

private fun extractFileFromZip(context: Context, zipUri: Uri, fileNameToFind: String, destUri: Uri, onResult: (Boolean) -> Unit) {
    try {
        val input = context.contentResolver.openInputStream(zipUri)
        val output = context.contentResolver.openOutputStream(destUri)
        if (input == null || output == null) {
            onResult(false)
            return
        }

        val zis = ZipInputStream(input)
        var entry: ZipEntry? = zis.nextEntry
        var found = false
        while (entry != null) {
            if (entry.name.contains(fileNameToFind, ignoreCase = true)) {
                zis.copyTo(output)
                found = true
                break
            }
            entry = zis.nextEntry
        }
        zis.close()
        output.close()
        onResult(found)
    } catch (e: Exception) {
        e.printStackTrace()
        onResult(false)
    }
}

private fun extractZipToFolder(context: Context, zipUri: Uri, destFolderUri: Uri, onResult: (Int) -> Unit) {
    try {
        val input = context.contentResolver.openInputStream(zipUri) ?: return onResult(0)
        val zis = ZipInputStream(input)
        val rootDoc = DocumentFile.fromTreeUri(context, destFolderUri) ?: return onResult(0)

        var count = 0
        var entry: ZipEntry? = zis.nextEntry
        while (entry != null) {
            if (!entry.isDirectory) {
                val pathParts = entry.name.split("/")
                var currentDir = rootDoc
                for (i in 0 until pathParts.size - 1) {
                    val dirName = pathParts[i]
                    val existingDir = currentDir.findFile(dirName)
                    currentDir = existingDir ?: currentDir.createDirectory(dirName) ?: currentDir
                }
                
                val fileName = pathParts.last()
                val file = currentDir.createFile("*/*", fileName)
                if (file != null) {
                    context.contentResolver.openOutputStream(file.uri)?.use { out ->
                        zis.copyTo(out)
                    }
                    count++
                }
            }
            entry = zis.nextEntry
        }
        zis.close()
        onResult(count)
    } catch (e: Exception) {
        e.printStackTrace()
        onResult(0)
    }
}
