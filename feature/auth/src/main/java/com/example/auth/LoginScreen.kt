package com.example.auth


import androidx.compose.foundation.layout.*
import androidx.compose.foundation.text.KeyboardActions
import androidx.compose.foundation.text.KeyboardOptions
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.focus.FocusDirection
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.text.input.ImeAction
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
    val focusManager = LocalFocusManager.current

    LaunchedEffect(uiState.loginStatus) {
        when (val status = uiState.loginStatus) {
            LoginStatus.Success -> onLoginSuccess()
            is LoginStatus.Error -> {
                val errorMessage = viewModel.getErrorMessage(status.error)

                snackbarHostState.showSnackbar(
                    message = errorMessage,
                    actionLabel = if (status.error is LoginDomainError.Network) "Повторить" else null,
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

        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues)
                .padding(16.dp)
                .imePadding()  // ⬅ Клавиатура НЕ перекрывает
        ) {
            Spacer(Modifier.weight(1f))

            LoginFormContent(
                uiState = uiState,
                onEmailChange = viewModel::onEmailChange,
                onPasswordChange = viewModel::onPasswordChange,
                onLoginClick = {
                    focusManager.clearFocus()  // скрыть клаву
                    viewModel.login()
                }
            )

            Spacer(Modifier.weight(1f))
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
    val focusManager = LocalFocusManager.current
    val isFormEnabled = uiState.loginStatus !is LoginStatus.Loading
    val errorTextHeight = 20.dp

    Column(horizontalAlignment = Alignment.CenterHorizontally) {

        OutlinedTextField(
            value = uiState.emailInput,
            onValueChange = onEmailChange,
            label = { Text("Email") },
            supportingText = { Text(uiState.emailError ?: "", Modifier.height(errorTextHeight)) },
            isError = uiState.emailError != null,
            enabled = isFormEnabled,
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Next
            ),
            keyboardActions = KeyboardActions(
                onNext = { focusManager.moveFocus(FocusDirection.Down) }
            )
        )

        OutlinedTextField(
            value = uiState.passwordInput,
            onValueChange = onPasswordChange,
            label = { Text("Пароль") },
            supportingText = { Text(uiState.passwordError ?: "", Modifier.height(errorTextHeight)) },
            isError = uiState.passwordError != null,
            enabled = isFormEnabled,
            visualTransformation = PasswordVisualTransformation(),
            modifier = Modifier.fillMaxWidth(),
            singleLine = true,
            maxLines = 1,
            keyboardOptions = KeyboardOptions(
                imeAction = ImeAction.Done
            ),
            keyboardActions = KeyboardActions(
                onDone = {
                    focusManager.clearFocus() // скрывает клавиатуру
                    onLoginClick()
                }
            )
        )

        Spacer(Modifier.height(32.dp))

        Button(
            onClick = onLoginClick,
            enabled = isFormEnabled,
            modifier = Modifier
                .fillMaxWidth()
                .height(56.dp)
        ) {
            if (uiState.loginStatus is LoginStatus.Loading) {
                CircularProgressIndicator(
                    modifier = Modifier.size(24.dp),
                    color = MaterialTheme.colorScheme.onPrimary
                )
            } else {
                Text("Войти")
            }
        }
    }
}
