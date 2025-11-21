package com.example.avitotask

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import com.example.auth.LoginScreen
import com.example.avitotask.ui.AvitoTaskTheme
import com.example.uploadbooks.UploadBookScreen
import kotlinx.serialization.Serializable

@Serializable
sealed class MainNavGraphDest {
    @Serializable
    data object Login : MainNavGraphDest()

    @Serializable
    data object WithBottomBar : MainNavGraphDest()
}


@Composable
fun MainNavHost(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()

    val navController = rememberNavController()

    AvitoTaskTheme  {
        when (val state = uiState) {
            MainUiState.Loading -> {
                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }
            is MainUiState.Success -> {
                NavHost(
                    navController = navController,
                    startDestination = state.startDestination,
                    modifier = Modifier.fillMaxSize()
                ) {

                    composable<MainNavGraphDest.Login> {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate(MainNavGraphDest.WithBottomBar) {
                                    popUpTo(MainNavGraphDest.Login) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = {}
                        )
                    }

                    composable<MainNavGraphDest.WithBottomBar> {
                        UploadBookScreen({})
                    }
                }
            }
        }
    }
}