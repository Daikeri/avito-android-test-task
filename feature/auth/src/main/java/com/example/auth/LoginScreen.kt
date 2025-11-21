package com.example.auth


import androidx.compose.foundation.layout.*
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.input.PasswordVisualTransformation
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun LoginScreen(
    viewModel: LoginViewModel = hiltViewModel(),
    onLoginSuccess: () -> Unit,
    onNavigateToRegister: () -> Unit
) {
    val uiState by viewModel.uiState.collectAsState()
    val snackbarHostState = remember { SnackbarHostState() }


    LaunchedEffect(uiState.loginStatus) {
        when (val status = uiState.loginStatus) {
            LoginStatus.Success -> onLoginSuccess()
            is LoginStatus.Error -> {

                val errorMessage = viewModel.getErrorMessage(status.error)


                val actionLabel = if (status.error is LoginDomainError.Network) "Повторить" else null

                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    actionLabel = actionLabel,
                    duration = SnackbarDuration.Short
                )
                viewModel.resetErrorStatus()
            }
            else -> Unit
        }
    }

    Scaffold(
        snackbarHost = { SnackbarHost(snackbarHostState) },
        topBar = {
            TopAppBar(
                title = { Text(text = "Вход в аккаунт") },
                actions = {
                    TextButton(
                        onClick = onNavigateToRegister,
                        enabled = uiState.loginStatus != LoginStatus.Loading
                    ) {
                        Text(text = "Регистрация")
                    }
                }
            )
        }
    ) { paddingValues ->
        Box(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(horizontal = 16.dp),
            contentAlignment = Alignment.Center
        ) {
            LoginFormContent(
                uiState = uiState,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onLoginClick = viewModel::login
            )
        }
    }
}

@Composable
private fun LoginFormContent(
    uiState: LoginUiState,
    onEmailChange: (String) -> Unit,
    onPasswordChange: (String) -> Unit,
    onLoginClick: () -> Unit
) {
    val isFormEnabled = uiState.loginStatus is LoginStatus.Idle || uiState.loginStatus is LoginStatus.Error
    val isLoading = uiState.loginStatus is LoginStatus.Loading

    val errorTextHeight = 20.dp

    Column(
        horizontalAlignment = Alignment.CenterHorizontally,
        modifier = Modifier.fillMaxWidth()
    ) {
        // --- Поле ввода Email ---
        OutlinedTextField(
            value = uiState.emailInput,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            isError = uiState.emailError != null,
            supportingText = {

                Text(
                    text = uiState.emailError ?: "",
                    modifier = Modifier.height(errorTextHeight)
                )
            },
            enabled = isFormEnabled,
            modifier = Modifier.fillMaxWidth()
        )

        OutlinedTextField(
            value = uiState.passwordInput,
            onValueChange = onPasswordChange,
            label = { Text("Пароль") },
            isError = uiState.passwordError != null,
            supportingText = {
                Text(
                    text = uiState.passwordError ?: "",
                    modifier = Modifier.height(errorTextHeight)
                )
            },
            visualTransformation = PasswordVisualTransformation(),
            enabled = isFormEnabled,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(32.dp))

        Button(
            onClick = onLoginClick,
            enabled = isFormEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (isLoading) {
                CircularProgressIndicator(
                    color = MaterialTheme.colorScheme.onPrimary,
                    modifier = Modifier.size(24.dp)
                )
            } else {
                Text("Войти")
            }
        }
    }
}