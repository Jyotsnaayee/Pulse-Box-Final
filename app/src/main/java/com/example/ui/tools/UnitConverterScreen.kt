package com.example.ui.tools

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.SwapHoriz
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.example.ui.theme.Primary

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitConverterScreen(onBack: () -> Unit) {
    var input by remember { mutableStateOf("") }
    var result by remember { mutableStateOf("") }
    var selectedCategory by remember { mutableStateOf(categories[0]) }
    var fromUnit by remember { mutableStateOf(selectedCategory.units[0]) }
    var toUnit by remember { mutableStateOf(selectedCategory.units[1]) }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Unit Converter") },
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
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            // Category Selector
            ScrollableTabRow(selectedTabIndex = categories.indexOf(selectedCategory)) {
                categories.forEach { category ->
                    Tab(
                        selected = selectedCategory == category,
                        onClick = {
                            selectedCategory = category
                            fromUnit = category.units[0]
                            toUnit = category.units[1]
                        },
                        text = { Text(category.name) }
                    )
                }
            }

            // Input Fields
            OutlinedTextField(
                value = input,
                onValueChange = { input = it },
                label = { Text("Value") },
                modifier = Modifier.fillMaxWidth()
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                UnitDropdown(
                    label = "From",
                    selected = fromUnit,
                    units = selectedCategory.units,
                    onSelect = { fromUnit = it },
                    modifier = Modifier.weight(1f)
                )
                Icon(Icons.Default.SwapHoriz, contentDescription = "Swap")
                UnitDropdown(
                    label = "To",
                    selected = toUnit,
                    units = selectedCategory.units,
                    onSelect = { toUnit = it },
                    modifier = Modifier.weight(1f)
                )
            }

            Button(
                onClick = {
                    val value = input.toDoubleOrNull()
                    if (value != null) {
                        result = performConversion(value, selectedCategory.name, fromUnit, toUnit)
                    } else {
                        result = "Invalid"
                    }
                },
                modifier = Modifier.fillMaxWidth(),
                colors = ButtonDefaults.buttonColors(containerColor = Primary)
            ) {
                Text("Convert")
            }

            if (input.isNotEmpty() && result.isNotEmpty()) {
                ElevatedCard(modifier = Modifier.fillMaxWidth()) {
                    Column(modifier = Modifier.padding(16.dp)) {
                        Text("Result", style = MaterialTheme.typography.labelSmall)
                        Text(
                            text = "$input $fromUnit = $result $toUnit",
                            style = MaterialTheme.typography.headlineMedium
                        )
                    }
                }
            }
        }
    }
}

fun performConversion(value: Double, category: String, from: String, to: String): String {
    return try {
        val converted = when (category) {
            "Length" -> {
                val toMeters = mapOf(
                    "Meters" to 1.0,
                    "Feet" to 0.3048,
                    "Kilometers" to 1000.0,
                    "Miles" to 1609.34
                )
                val meters = value * (toMeters[from] ?: 1.0)
                meters / (toMeters[to] ?: 1.0)
            }
            "Weight" -> {
                val toGrams = mapOf(
                    "Grams" to 1.0,
                    "Kilograms" to 1000.0,
                    "Pounds" to 453.592,
                    "Ounces" to 28.3495
                )
                val grams = value * (toGrams[from] ?: 1.0)
                grams / (toGrams[to] ?: 1.0)
            }
            "Temp" -> {
                val celsius = when (from) {
                    "Celsius" -> value
                    "Fahrenheit" -> (value - 32) * 5 / 9
                    "Kelvin" -> value - 273.15
                    else -> value
                }
                when (to) {
                    "Celsius" -> celsius
                    "Fahrenheit" -> celsius * 9 / 5 + 32
                    "Kelvin" -> celsius + 273.15
                    else -> celsius
                }
            }
            else -> value
        }
        if (converted == converted.toLong().toDouble()) {
            converted.toLong().toString()
        } else {
            String.format("%.4f", converted).trimEnd('0').trimEnd('.')
        }
    } catch (e: Exception) {
        "Error"
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UnitDropdown(label: String, selected: String, units: List<String>, onSelect: (String) -> Unit, modifier: Modifier) {
    var expanded by remember { mutableStateOf(false) }
    ExposedDropdownMenuBox(
        expanded = expanded,
        onExpandedChange = { expanded = !expanded },
        modifier = modifier
    ) {
        OutlinedTextField(
            value = selected,
            onValueChange = {},
            readOnly = true,
            label = { Text(label) },
            trailingIcon = { ExposedDropdownMenuDefaults.TrailingIcon(expanded = expanded) },
            modifier = Modifier.menuAnchor()
        )
        ExposedDropdownMenu(
            expanded = expanded,
            onDismissRequest = { expanded = false }
        ) {
            units.forEach { unit ->
                DropdownMenuItem(
                    text = { Text(unit) },
                    onClick = {
                        onSelect(unit)
                        expanded = false
                    }
                )
            }
        }
    }
}

data class Category(val name: String, val units: List<String>)

val categories = listOf(
    Category("Length", listOf("Meters", "Feet", "Kilometers", "Miles")),
    Category("Weight", listOf("Kilograms", "Pounds", "Grams", "Ounces")),
    Category("Temp", listOf("Celsius", "Fahrenheit", "Kelvin"))
)
