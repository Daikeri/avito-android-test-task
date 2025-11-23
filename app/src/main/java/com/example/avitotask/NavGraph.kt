package com.example.avitotask

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.MenuBook
import androidx.compose.material.icons.filled.Person
import androidx.compose.material.icons.filled.Upload
import androidx.compose.material3.CircularProgressIndicator
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.toRoute
import com.example.auth.LoginScreen
import com.example.auth.RegisterScreen
import com.example.avitotask.ui.AvitoTaskTheme
import com.example.listofbooks.MyBooksScreen
import com.example.listofbooks.BookReaderScreen
import com.example.uploadbooks.UploadBookScreen
import kotlinx.serialization.Serializable

@Serializable
sealed class MainNavGraphDest {
    @Serializable
    data object Login : MainNavGraphDest()

    @Serializable
    data object Tabs : MainNavGraphDest()

    @Serializable
    data class ReadBook(val localPath: String) : MainNavGraphDest()

    @Serializable
    data object Register : MainNavGraphDest()
}


@Serializable
sealed class TabsNavGraphDest {
    @Serializable
    data object UploadBooks : TabsNavGraphDest()

    @Serializable
    data object ListOfBooks : TabsNavGraphDest()

    @Serializable
    data object Profile : TabsNavGraphDest()
}


@Composable
fun MainNavHost(
    viewModel: MainViewModel = hiltViewModel()
) {
    val uiState by viewModel.uiState.collectAsState()
    val navController = rememberNavController()

    AvitoTaskTheme {
        when (val state = uiState) {

            MainUiState.Loading -> {
                Surface(modifier = Modifier.fillMaxSize()) {
                    Box(contentAlignment = Alignment.Center) {
                        CircularProgressIndicator()
                    }
                }
            }

            is MainUiState.Success -> {
                NavHost(
                    navController = navController,
                    startDestination = state.startDestination
                ) {

                    composable<MainNavGraphDest.Login> {
                        LoginScreen(
                            onLoginSuccess = {
                                navController.navigate(MainNavGraphDest.Tabs) {
                                    popUpTo(MainNavGraphDest.Login) { inclusive = true }
                                }
                            },
                            onNavigateToRegister = { navController.navigate(MainNavGraphDest.Register) }

                        )
                    }

                    composable<MainNavGraphDest.Tabs> {
                        TabsScreen(
                            onNavigateToReader = { path ->
                                navController.navigate(MainNavGraphDest.ReadBook(path))
                            }
                        )
                    }

                    composable<MainNavGraphDest.ReadBook> { entry ->
                        val route = entry.toRoute<MainNavGraphDest.ReadBook>()
                        BookReaderScreen(
                            localPath = route.localPath,
                            onBack = { navController.popBackStack() }
                        )
                    }

                    composable<MainNavGraphDest.Register> {
                        RegisterScreen(
                            onSuccess = {
                                navController.navigate(MainNavGraphDest.Tabs) {
                                    popUpTo(MainNavGraphDest.Register) { inclusive = true }
                                }
                            },
                            onBack = { navController.popBackStack() }
                        )
                    }

                }
            }
        }
    }
}


@Composable
fun TabsScreen(
    onNavigateToReader: (String) -> Unit
) {
    val navController = rememberNavController()

    val tabs = listOf(
        TabsNavGraphDest.ListOfBooks,
        TabsNavGraphDest.UploadBooks,
        TabsNavGraphDest.Profile,
    )

    Box(modifier = Modifier.fillMaxSize()) {

        NavHost(
            navController = navController,
            startDestination = TabsNavGraphDest.ListOfBooks,
            modifier = Modifier.fillMaxSize()
        ) {

            composable<TabsNavGraphDest.ListOfBooks> {
                MyBooksScreen(
                    onBookClick = { path -> onNavigateToReader(path) }
                )
            }

            composable<TabsNavGraphDest.UploadBooks> {
                UploadBookScreen({})
            }

            composable<TabsNavGraphDest.Profile> {
            }
        }

        NavigationBar(
            modifier = Modifier.align(Alignment.BottomCenter)
        ) {
            val current = navController.currentBackStackEntryFlow
                .collectAsState(null).value
            val currentRoute = current?.destination?.route

            tabs.forEach { dest ->
                NavigationBarItem(
                    selected = currentRoute == dest::class.qualifiedName,
                    onClick = { navController.navigate(dest) },
                    label = {
                        Text(
                            when (dest) {
                                TabsNavGraphDest.ListOfBooks -> "Мои книги"
                                TabsNavGraphDest.UploadBooks -> "Загрузка"
                                TabsNavGraphDest.Profile -> "Профиль"
                            }
                        )
                    },
                    icon = {
                        val icon = when (dest) {
                            TabsNavGraphDest.ListOfBooks -> Icons.Default.MenuBook
                            TabsNavGraphDest.UploadBooks -> Icons.Default.Upload
                            TabsNavGraphDest.Profile -> Icons.Default.Person
                        }
                        Icon(imageVector = icon, contentDescription = null)
                    }
                )
            }
        }
    }
}

