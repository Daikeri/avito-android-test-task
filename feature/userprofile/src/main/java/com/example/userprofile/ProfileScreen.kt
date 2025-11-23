package com.example.userprofile
import android.net.Uri
import androidx.activity.compose.rememberLauncherForActivityResult
import androidx.activity.result.contract.ActivityResultContracts
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.ArrowBack
import androidx.compose.material.icons.filled.Person
import androidx.compose.material3.Button
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.OutlinedTextField
import androidx.compose.material3.Scaffold
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.material3.TopAppBar
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.layout.ContentScale
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import coil.compose.AsyncImage
import kotlinx.coroutines.flow.collectLatest


@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ProfileScreen(
    viewModel: ProfileViewModel = hiltViewModel(),
    onLogout: () -> Unit = {},
) {
    val uiState by viewModel.ui.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }

    LaunchedEffect(Unit) {
        viewModel.onEvent(ProfileEvent.Load)
    }

    LaunchedEffect(Unit) {
        viewModel.effect.collectLatest { effect ->
            when (effect) {
                ProfileEffect.SignedOut -> onLogout()
            }
        }
    }

    // ==== Обработка ошибок без контекста ====
    LaunchedEffect(uiState.error) {
        val error = uiState.error ?: return@LaunchedEffect
        snackbarHostState.showSnackbar(error.toMessage())
        viewModel.onEvent(ProfileEvent.ErrorConsumed)
    }

    val pickImageLauncher =
        rememberLauncherForActivityResult(ActivityResultContracts.GetContent()) { uri: Uri? ->
            if (uri != null) {
                viewModel.onEvent(ProfileEvent.UpdatePhoto(uri))
            }
        }

    Scaffold(
        topBar = {
            // ==== Убрали стрелку ====
            TopAppBar(
                title = { Text(text = "Профиль") }
            )
        },
        snackbarHost = { SnackbarHost(hostState = snackbarHostState) }
    ) { padding ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(padding)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(horizontal = 16.dp, vertical = 24.dp),
                horizontalAlignment = Alignment.CenterHorizontally,
                verticalArrangement = Arrangement.Top
            ) {

                // ==== АВАТАР ====
                Box(
                    modifier = Modifier
                        .size(120.dp)
                        .clip(CircleShape)
                        .background(MaterialTheme.colorScheme.primary.copy(alpha = 0.1f))
                        .clickable { pickImageLauncher.launch("image/*") },
                    contentAlignment = Alignment.Center
                ) {
                    if (uiState.photoUrl != null) {
                        AsyncImage(
                            model = uiState.photoUrl,
                            contentDescription = "Фото профиля",
                            modifier = Modifier.fillMaxSize(),
                            contentScale = ContentScale.Crop
                        )
                    } else {
                        Icon(
                            imageVector = Icons.Default.Person,
                            contentDescription = "Фото отсутствует",
                            modifier = Modifier.size(64.dp),
                            tint = MaterialTheme.colorScheme.primary
                        )
                    }
                }

                Spacer(modifier = Modifier.height(24.dp))

                // ==== Имя ====
                OutlinedTextField(
                    value = uiState.firstName,
                    onValueChange = { viewModel.onEvent(ProfileEvent.ChangeFirstName(it)) },
                    label = { Text("Имя") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.isEditing,
                    singleLine = true,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                // ==== Фамилия ====
                OutlinedTextField(
                    value = uiState.lastName,
                    onValueChange = { viewModel.onEvent(ProfileEvent.ChangeLastName(it)) },
                    label = { Text("Фамилия") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = uiState.isEditing,
                    singleLine = true,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.email,
                    onValueChange = {},
                    label = { Text("Email") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    singleLine = true,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(8.dp))

                OutlinedTextField(
                    value = uiState.phone,
                    onValueChange = {},
                    label = { Text("Телефон") },
                    modifier = Modifier.fillMaxWidth(),
                    enabled = false,
                    singleLine = true,
                    maxLines = 1
                )

                Spacer(modifier = Modifier.height(16.dp))

                if (uiState.isEditing) {
                    Button(
                        onClick = { viewModel.onEvent(ProfileEvent.SaveName) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Сохранить")
                    }
                } else {
                    TextButton(
                        onClick = { viewModel.onEvent(ProfileEvent.ToggleEdit) },
                        modifier = Modifier.fillMaxWidth()
                    ) {
                        Text(text = "Редактировать")
                    }
                }

                Spacer(modifier = Modifier.height(16.dp))

                Button(
                    onClick = { viewModel.onEvent(ProfileEvent.SignOut) },
                    modifier = Modifier.fillMaxWidth()
                ) {
                    Text(text = "Выйти из аккаунта")
                }
            }

            if (uiState.isLoading) {
                Box(
                    modifier = Modifier
                        .fillMaxSize()
                        .background(
                            MaterialTheme.colorScheme.background.copy(alpha = 0.5f)
                        ),
                    contentAlignment = Alignment.Center
                ) {
                    CircularProgressIndicator()
                }
            }
        }
    }
}
