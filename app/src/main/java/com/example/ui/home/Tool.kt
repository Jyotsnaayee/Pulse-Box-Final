package com.example.ui.home

import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Calculate
import androidx.compose.material.icons.filled.Camera
import androidx.compose.material.icons.filled.Compress
import androidx.compose.material.icons.filled.FileOpen
import androidx.compose.material.icons.filled.FlashlightOn
import androidx.compose.material.icons.filled.Image
import androidx.compose.material.icons.filled.Newspaper
import androidx.compose.material.icons.filled.PictureAsPdf
import androidx.compose.material.icons.filled.QrCodeScanner
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material.icons.filled.TaskAlt
import androidx.compose.ui.graphics.vector.ImageVector

data class Tool(
    val id: String,
    val name: String,
    val description: String,
    val icon: ImageVector,
    val route: String
)

val allTools = listOf(
    Tool("tasks", "TaskPulse", "Daily tasks & priorities", Icons.Default.TaskAlt, "tasks"),
    Tool("calculator", "Calculator", "Math & Scientific", Icons.Default.Calculate, "calculator"),
    Tool("unit_converter", "Converter", "Units & Scales", Icons.Default.SwapHoriz, "unit_converter"),
    Tool("flashlight", "Flashlight", "Bright Utility", Icons.Default.FlashlightOn, "flashlight"),
    Tool("qr_scanner", "QR Scanner", "Scan & Open", Icons.Default.QrCodeScanner, "qr_scanner"),
    Tool("pdf_reader", "PDF Studio", "Pro PDF Viewer", Icons.Default.PictureAsPdf, "pdf_reader"),
    Tool("file_reader", "Universal Reader", "Open Any File", Icons.Default.FileOpen, "file_reader"),
    Tool("apk_fixer", "APK Fixer", "Build & Fix APKs", Icons.Default.Camera, "apk_fixer"), // Using Camera as placeholder icon for "System/Build"
    Tool("compressor", "Compressor", "Shrink Images", Icons.Default.Compress, "compressor"),
    Tool("wallpaper", "Wallpapers", "HD Backgrounds", Icons.Default.Image, "wallpaper"),
    Tool("news", "News Hub", "India & Global", Icons.Default.Newspaper, "news")
)
