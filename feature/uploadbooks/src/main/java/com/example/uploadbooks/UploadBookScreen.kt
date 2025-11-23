package com.example.uploadbooks

import android.content.Context
import android.net.Uri
import android.provider.OpenableColumns
import android.util.Log
import android.widget.Toast
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.UploadFile
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.focus.FocusRequester
import androidx.compose.ui.focus.focusRequester
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel



@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun UploadBookScreen(
    viewModel: UploadBookViewModel = hiltViewModel()
) {
    val state by viewModel.uiState.collectAsState()
    val context = LocalContext.current
    val focusManager = LocalFocusManager.current

    var selectedFileName by remember { mutableStateOf<String?>(null) }
    var selectedFileUri by remember { mutableStateOf<Uri?>(null) }
    var title by remember { mutableStateOf("") }
    var author by remember { mutableStateOf("") }

    val focusRequesterTitle = remember { FocusRequester() }
    var shouldFocusTitle by remember { mutableStateOf(false) }


    val filePickerLauncher = rememberLauncherForActivityResult(
        contract = ActivityResultContracts.OpenDocument(),
        onResult = { uri ->
            uri?.let {
                selectedFileUri = it
                selectedFileName = getRealFileName(context, it)
                shouldFocusTitle = true
            }
        }
    )

    LaunchedEffect(shouldFocusTitle) {
        if (shouldFocusTitle) {
            focusRequesterTitle.requestFocus()
            shouldFocusTitle = false
        }
    }


    LaunchedEffect(state.isSuccess) {
        if (state.isSuccess) {
            Toast.makeText(context, "Книга загружена!", Toast.LENGTH_SHORT).show()
            title = ""
            author = ""
            selectedFileUri = null
            selectedFileName = null
            viewModel.resetState()
        }
    }


    LaunchedEffect(state.error) {
        state.error?.let { error ->
            val errorMessage = viewModel.getErrorMessage(error)
            Log.e("UPLOAD_ERROR", errorMessage)
            Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
            viewModel.clearError()
        }
    }

    Scaffold(
        topBar = { TopAppBar(title = { Text("Загрузка книги") }) }
    ) { padding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
                .imePadding()
                .padding(16.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {


            OutlinedButton(
                onClick = {
                    filePickerLauncher.launch(
                        arrayOf(
                            "application/pdf",
                            "application/epub+zip",
                            "text/plain"
                        )
                    )
                },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading
            ) {
                Icon(Icons.Default.UploadFile, contentDescription = null)
                Spacer(Modifier.width(8.dp))
                Text(selectedFileName ?: "Выбрать файл")
            }

            OutlinedTextField(
                value = title,
                onValueChange = { title = it },
                label = { Text("Название") },
                modifier = Modifier
                    .fillMaxWidth()
                    .focusRequester(focusRequesterTitle),
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Next
                ),
                keyboardActions = KeyboardActions(
                    onNext = { focusManager.moveFocus(FocusDirection.Down) }
                )
            )


            OutlinedTextField(
                value = author,
                onValueChange = { author = it },
                label = { Text("Автор") },
                modifier = Modifier.fillMaxWidth(),
                enabled = !state.isLoading,
                keyboardOptions = KeyboardOptions.Default.copy(
                    imeAction = ImeAction.Done
                ),
                keyboardActions = KeyboardActions(
                    onDone = { focusManager.clearFocus() }
                )
            )

            if (state.isLoading) {
                LinearProgressIndicator(
                    progress = { state.progress },
                    modifier = Modifier.fillMaxWidth()
                )
            } else {
                Button(
                    onClick = {
                        viewModel.uploadBook(
                            title = title,
                            author = author,
                            fileUri = selectedFileUri,
                            fileName = selectedFileName ?: ""
                        )
                    },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = title.isNotBlank() &&
                            author.isNotBlank() &&
                            selectedFileUri != null
                ) {
                    Text("Загрузить")
                }
            }
        }
    }
}

private fun getRealFileName(context: Context, uri: Uri): String {
    val cursor = context.contentResolver.query(
        uri,
        arrayOf(OpenableColumns.DISPLAY_NAME),
        null, null, null
    )
    cursor?.use {
        if (it.moveToFirst()) {
            val index = it.getColumnIndex(OpenableColumns.DISPLAY_NAME)
            if (index != -1) return it.getString(index)
        }
    }
    return "Файл"
}


