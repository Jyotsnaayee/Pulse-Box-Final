package com.example.ui.tools

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.grid.GridCells
import androidx.compose.foundation.lazy.grid.LazyVerticalGrid
import androidx.compose.foundation.lazy.grid.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.History
import androidx.compose.material.icons.filled.Science
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.Primary
import com.example.ui.theme.Secondary
import kotlin.math.*

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CalculatorScreen(onBack: () -> Unit) {
    var display by remember { mutableStateOf("0") }
    var result by remember { mutableStateOf("") }
    var isScientific by remember { mutableStateOf(false) }

    val buttons = if (isScientific) scientificButtons else basicButtons

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Calculator") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.AutoMirrored.Filled.ArrowBack, contentDescription = "Back")
                    }
                },
                actions = {
                    IconButton(onClick = { isScientific = !isScientific }) {
                        Icon(Icons.Default.Science, contentDescription = "Toggle Scientific")
                    }
                }
            )
        }
    ) { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .background(MaterialTheme.colorScheme.background)
        ) {
            // Display Area
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
                    .padding(24.dp),
                verticalArrangement = Arrangement.Bottom,
                horizontalAlignment = Alignment.End
            ) {
                Text(
                    text = display,
                    style = MaterialTheme.typography.displayMedium,
                    maxLines = 2,
                    textAlign = TextAlign.End,
                    color = MaterialTheme.colorScheme.onBackground
                )
                if (result.isNotEmpty()) {
                    Text(
                        text = result,
                        style = MaterialTheme.typography.headlineMedium,
                        color = Primary,
                        textAlign = TextAlign.End
                    )
                }
            }

            // Keypad
            LazyVerticalGrid(
                columns = GridCells.Fixed(4),
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(16.dp),
                contentPadding = PaddingValues(8.dp),
                horizontalArrangement = Arrangement.spacedBy(8.dp),
                verticalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                items(buttons) { btn ->
                    CalcButton(
                        text = btn.text,
                        color = btn.color,
                        onClick = {
                            display = handleInput(display, btn.text)
                            if (btn.text == "=") {
                                result = evaluate(display)
                            }
                        }
                    )
                }
            }
        }
    }
}

@Composable
fun CalcButton(text: String, color: Color, onClick: () -> Unit) {
    Surface(
        onClick = onClick,
        modifier = Modifier
            .aspectRatio(1f)
            .clip(CircleShape)
            .testTag("calc_btn_$text"),
        color = color,
        shape = CircleShape
    ) {
        Box(contentAlignment = Alignment.Center) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleLarge,
                fontWeight = FontWeight.Bold,
                color = if (color == Primary) Color.White else MaterialTheme.colorScheme.onSurface
            )
        }
    }
}

data class ButtonInfo(val text: String, val color: Color)

val basicButtons = listOf(
    ButtonInfo("AC", Color.LightGray), ButtonInfo("DEL", Color.LightGray), ButtonInfo("%", Color.LightGray), ButtonInfo("/", Secondary),
    ButtonInfo("7", Color.Transparent), ButtonInfo("8", Color.Transparent), ButtonInfo("9", Color.Transparent), ButtonInfo("*", Secondary),
    ButtonInfo("4", Color.Transparent), ButtonInfo("5", Color.Transparent), ButtonInfo("6", Color.Transparent), ButtonInfo("-", Secondary),
    ButtonInfo("1", Color.Transparent), ButtonInfo("2", Color.Transparent), ButtonInfo("3", Color.Transparent), ButtonInfo("+", Secondary),
    ButtonInfo("0", Color.Transparent), ButtonInfo(".", Color.Transparent), ButtonInfo("=", Primary)
)

val scientificButtons = basicButtons // Simplified for now

fun handleInput(current: String, input: String): String {
    return when (input) {
        "AC" -> "0"
        "DEL" -> if (current.length <= 1) "0" else current.dropLast(1)
        "=" -> current
        else -> {
            if (current == "0" && input !in listOf(".", "/", "*", "-", "+", "%")) input
            else current + input
        }
    }
}

fun evaluate(expr: String): String {
    return try {
        val sanitized = expr.replace("x", "*").replace("÷", "/")
        val result = object : Any() {
            var pos = -1
            var ch = 0

            fun nextChar() {
                ch = if (++pos < sanitized.length) sanitized[pos].toInt() else -1
            }

            fun eat(charToEat: Int): Boolean {
                while (ch == ' '.toInt()) nextChar()
                if (ch == charToEat) {
                    nextChar()
                    return true
                }
                return false
            }

            fun parse(): Double {
                nextChar()
                val x = parseExpression()
                if (pos < sanitized.length) throw RuntimeException("Unexpected: " + ch.toChar())
                return x
            }

            fun parseExpression(): Double {
                var x = parseTerm()
                while (true) {
                    if (eat('+'.toInt())) x += parseTerm()
                    else if (eat('-'.toInt())) x -= parseTerm()
                    else return x
                }
            }

            fun parseTerm(): Double {
                var x = parseFactor()
                while (true) {
                    if (eat('*'.toInt())) x *= parseFactor()
                    else if (eat('/'.toInt())) x /= parseFactor()
                    else if (eat('%'.toInt())) x %= parseFactor()
                    else return x
                }
            }

            fun parseFactor(): Double {
                if (eat('+'.toInt())) return parseFactor()
                if (eat('-'.toInt())) return -parseFactor()
                var x: Double
                val startPos = pos
                if (eat('('.toInt())) {
                    x = parseExpression()
                    eat(')'.toInt())
                } else if (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) {
                    while (ch >= '0'.toInt() && ch <= '9'.toInt() || ch == '.'.toInt()) nextChar()
                    x = sanitized.substring(startPos, pos).toDouble()
                } else {
                    throw RuntimeException("Unexpected: " + ch.toChar())
                }
                return x
            }
        }.parse()
        
        if (result == result.toLong().toDouble()) {
            result.toLong().toString()
        } else {
            String.format("%.4f", result).trimEnd('0').trimEnd('.')
        }
    } catch (e: Exception) {
        "Error"
    }
}
