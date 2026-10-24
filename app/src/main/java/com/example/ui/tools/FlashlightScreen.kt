package com.example.ui.tools

import android.content.Context
import android.hardware.camera2.CameraManager
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.FlashlightOff
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun FlashlightScreen(onBack: () -> Unit) {
    val context = LocalContext.current
    val cameraManager = context.getSystemService(Context.CAMERA_SERVICE) as CameraManager
    val cameraId = cameraManager.cameraIdList.firstOrNull()
    
    var isOn by remember { mutableStateOf(false) }

    DisposableEffect(Unit) {
        onDispose {
            if (isOn && cameraId != null) {
                cameraManager.setTorchMode(cameraId, false)
            }
        }
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Flashlight") },
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
                .padding(innerPadding),
            verticalArrangement = Arrangement.Center,
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Surface(
                onClick = {
                    if (cameraId != null) {
                        isOn = !isOn
                        cameraManager.setTorchMode(cameraId, isOn)
                    }
                },
                modifier = Modifier
                    .size(200.dp)
                    .clip(CircleShape),
                color = if (isOn) Primary else MaterialTheme.colorScheme.surfaceVariant,
                shape = CircleShape,
                tonalElevation = 8.dp
            ) {
                Box(contentAlignment = Alignment.Center) {
                    Icon(
                        imageVector = if (isOn) Icons.Default.FlashlightOn else Icons.Default.FlashlightOff,
                        contentDescription = null,
                        modifier = Modifier.size(80.dp),
                        tint = if (isOn) Color.White else MaterialTheme.colorScheme.onSurface
                    )
                }
            }
            Spacer(modifier = Modifier.height(32.dp))
            Text(
                text = if (isOn) "Light is ON" else "Light is OFF",
                style = MaterialTheme.typography.headlineSmall
            )
        }
    }
}
