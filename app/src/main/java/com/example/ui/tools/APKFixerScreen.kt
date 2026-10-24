package com.example.ui.tools

import android.content.Context
import android.content.pm.PackageInfo
import android.content.pm.PackageManager
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Primary
import java.io.File
import java.io.FileOutputStream

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun APKFixerScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    var apkInfo by remember { mutableStateOf<PackageInfo?>(null) }
    var apkUri by remember { mutableStateOf<Uri?>(null) }
    var errorMessage by remember { mutableStateOf<String?>(null) }

    val launcher = rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri ->
        if (uri != null) {
            apkUri = uri
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

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("APK Fixer & Info") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .padding(16.dp),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            if (apkInfo == null) {
                Button(onClick = { launcher.launch("application/vnd.android.package-archive") }) {
                    Icon(Icons.Default.FileOpen, contentDescription = null)
                    Spacer(modifier = Modifier.width(8.dp))
                    Text("Select APK File")
                }
                if (errorMessage != null) {
                    Spacer(modifier = Modifier.height(16.dp))
                    Text(errorMessage!!, color = MaterialTheme.colorScheme.error)
                }
            } else {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Package: ${apkInfo?.packageName}", style = MaterialTheme.typography.bodyLarge)
                        Text("Version: ${apkInfo?.versionName} (${apkInfo?.versionCode})", style = MaterialTheme.typography.bodyMedium)
                        Spacer(modifier = Modifier.height(16.dp))
                        Button(
                            onClick = { /* Install logic requires complex FileProvider + Intent */ },
                            modifier = Modifier.fillMaxWidth(),
                            colors = ButtonDefaults.buttonColors(containerColor = Primary)
                        ) {
                            Text("Install APK")
                        }
                        Spacer(modifier = Modifier.height(8.dp))
                        TextButton(onClick = { apkInfo = null }, modifier = Modifier.fillMaxWidth()) {
                            Text("Clear")
                        }
                    }
                }
            }
        }
    }
}

private fun getApkInfo(context: Context, uri: Uri): PackageInfo? {
    return try {
        val file = File(context.cacheDir, "temp_apk.apk")
        context.contentResolver.openInputStream(uri)?.use { input ->
            FileOutputStream(file).use { output ->
                input.copyTo(output)
            }
        }
        context.packageManager.getPackageArchiveInfo(file.absolutePath, 0)
    } catch (e: Exception) {
        null
    }
}
