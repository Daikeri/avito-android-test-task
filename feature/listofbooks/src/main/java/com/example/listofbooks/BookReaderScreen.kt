package com.example.listofbooks

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.TextFields
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BookReaderScreen(
    localPath: String,
    onBack: () -> Unit,
    vm: BookReaderViewModel = hiltViewModel()
) {
    val ui by vm.uiState.collectAsState()
    val list = rememberLazyListState()
    var restored by remember { mutableStateOf(false) }

    // состояние шторки
    val sheetState = rememberModalBottomSheetState()
    var showSettings by remember { mutableStateOf(false) }

    // восстановление позиции
    LaunchedEffect(ui.paragraphs) {
        if (!restored && ui.paragraphs.isNotEmpty()) {
            restored = true
            list.scrollToItem(ui.initialIndex, ui.initialOffset)
        }
    }

    // сохранение позиции
    LaunchedEffect(list.firstVisibleItemIndex, list.firstVisibleItemScrollOffset) {
        if (!ui.isLoading)
            vm.onScrollChanged(list.firstVisibleItemIndex, list.firstVisibleItemScrollOffset)
    }

    // прогресс чтения
    val progress = remember(ui.paragraphs, list.firstVisibleItemIndex) {
        if (ui.paragraphs.isEmpty()) 0f
        else list.firstVisibleItemIndex.toFloat() / ui.paragraphs.lastIndex
    }

    Scaffold(
        topBar = {
            TopAppBar(
                title = { Text("Чтение") },
                navigationIcon = {
                    IconButton(onClick = onBack) {
                        Icon(Icons.Default.ArrowBack, null)
                    }
                },
                actions = {
                    IconButton(onClick = { showSettings = true }) {
                        Icon(Icons.Default.TextFields, null)
                    }
                }
            )
        },
        bottomBar = {
            if (!ui.isLoading && ui.errorMessage == null) {
                Column(Modifier.fillMaxWidth().padding(8.dp)) {
                    LinearProgressIndicator(
                        progress = progress,
                        modifier = Modifier.fillMaxWidth()
                    )
                    Text(
                        text = "${(progress * 100).toInt()}% прочитано",
                        modifier = Modifier.align(Alignment.End),
                        fontSize = 12.sp
                    )
                }
            }
        }
    ) { pad ->

        Box(
            Modifier
                .padding(pad)
                .fillMaxSize()
                .background(Color.White)
        ) {
            when {
                ui.isLoading -> CircularProgressIndicator(Modifier.align(Alignment.Center))

                ui.errorMessage != null -> Text(
                    ui.errorMessage ?: "Ошибка",
                    modifier = Modifier.align(Alignment.Center)
                )

                else -> LazyColumn(
                    state = list,
                    contentPadding = PaddingValues(16.dp)
                ) {
                    itemsIndexed(ui.paragraphs) { _, p ->
                        Text(
                            p,
                            fontSize = ui.fontSizeSp.sp,
                            lineHeight = (ui.fontSizeSp * ui.lineHeightMultiplier).sp,
                            color = Color.Black
                        )
                        Spacer(Modifier.height(12.dp))
                    }
                }
            }
        }

        if (showSettings) {
            ModalBottomSheet(
                onDismissRequest = { showSettings = false },
                sheetState = sheetState
            ) {
                Column(Modifier.fillMaxWidth().padding(16.dp)) {

                    Text("Размер шрифта", style = MaterialTheme.typography.titleMedium)
                    Spacer(Modifier.height(8.dp))

                    Row(
                        horizontalArrangement = Arrangement.SpaceBetween,
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        listOf(16f, 18f, 20f, 22f, 24f).forEach { size ->
                            FilterChip(
                                selected = ui.fontSizeSp == size,
                                onClick = { vm.onFontSizeChanged(size) },
                                label = { Text(size.toInt().toString()) }
                            )
                        }
                    }

                    Spacer(Modifier.height(24.dp))

                    Text("Интервал между строками", style = MaterialTheme.typography.titleMedium)
                    Slider(
                        value = ui.lineHeightMultiplier,
                        onValueChange = { vm.onLineHeightChanged(it) },
                        valueRange = 1.0f..2.0f,
                        steps = 3
                    )

                    Spacer(Modifier.height(24.dp))
                }
            }
        }
    }
}