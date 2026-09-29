package com.mastermind

import android.content.Context
import android.content.res.Configuration
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.runtime.*
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewmodel.compose.viewModel
import androidx.navigation.NavType
import androidx.navigation.compose.NavHost
import androidx.navigation.compose.composable
import androidx.navigation.compose.rememberNavController
import androidx.navigation.navArgument
import com.mastermind.data.GameRepository
import com.mastermind.models.GameSettings
import com.mastermind.ui.screens.GameScreen
import com.mastermind.ui.screens.HomeScreen
import com.mastermind.ui.screens.SavedGamesScreen
import com.mastermind.ui.theme.MastermindTheme
import com.mastermind.utils.getSavedLanguage
import com.mastermind.viewModel.GameViewModel
import com.mastermind.viewModel.HomeViewModel
import java.util.Locale

class MainActivity : ComponentActivity() {

    private lateinit var gameRepository: GameRepository

    override fun attachBaseContext(newBase: Context) {
        val lang = getSavedLanguage(newBase)

        val context = if (lang.isNotEmpty()) {
            val locale = Locale(lang)
            val config = Configuration(newBase.resources.configuration)

            config.setLocale(locale)

            newBase.createConfigurationContext(config)
        } else {
            newBase
        }

        super.attachBaseContext(context)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        gameRepository = GameRepository(applicationContext)

        setContent {
            MastermindTheme {

                val navController = rememberNavController()

                var selectedGameId by rememberSaveable {
                    mutableStateOf<Int?>(null)
                }

                var selectedGameFromHistory by rememberSaveable {
                    mutableStateOf(false)
                }

                NavHost(
                    navController = navController,
                    startDestination = "home"
                ) {

                    composable("home") {

                        val homeViewModel: HomeViewModel = viewModel(
                            factory = HomeViewModelFactory(gameRepository)
                        )

                        val hasCurrentGame by
                            homeViewModel.hasCurrentGame.collectAsState()

                        LaunchedEffect(Unit) {
                            homeViewModel.refreshCurrentGame()
                        }

                        HomeScreen(
                            viewModel = homeViewModel,

                            onPlayClicked = {
                                    numColors,
                                    codeLength,
                                    allowDuplicates ->

                                navController.navigate(
                                    "game/$numColors/$codeLength/$allowDuplicates"
                                )
                            },

                            onSavedGamesClicked = {
                                navController.navigate("savedGames")
                            },

                            hasCurrentGame = hasCurrentGame,

                            onContinueGameClicked = {

                                val currentGame =
                                    homeViewModel.getCurrentGame()

                                if (currentGame != null) {

                                    selectedGameId =
                                        currentGame.id

                                    selectedGameFromHistory =
                                        false

                                    navController.navigate(
                                        "resumeGame"
                                    )
                                }
                            }
                        )
                    }

                    composable("savedGames") {

                        val gameViewModel: GameViewModel = viewModel(
                            factory = GameViewModelFactory(
                                gameRepository
                            )
                        )

                        SavedGamesScreen(
                            viewModel = gameViewModel,

                            onGameSelected = { gameState ->

                                selectedGameId =
                                    gameState.id

                                selectedGameFromHistory =
                                    gameState.isGameOver

                                navController.navigate(
                                    "resumeGame"
                                )
                            },

                            onBack = {
                                navController.popBackStack()
                            }
                        )
                    }

                    composable("resumeGame") {

                        val gameViewModel: GameViewModel = viewModel(
                            factory = GameViewModelFactory(
                                gameRepository
                            )
                        )

                        val isGameReady by
                            gameViewModel.isGameReady.collectAsState()

                        LaunchedEffect(
                            selectedGameId,
                            selectedGameFromHistory
                        ) {

                            selectedGameId?.let { gameId ->

                                gameViewModel.loadGameById(
                                    gameId = gameId,
                                    fromHistory =
                                        selectedGameFromHistory
                                )
                            }
                        }

                        if (isGameReady) {

                            GameScreen(
                                viewModel = gameViewModel,

                                onBackPressed = {
                                    navController.popBackStack(
                                        "home",
                                        false
                                    )
                                },

                                onSaveGame = {
                                    gameViewModel.saveGame()
                                }
                            )
                        }
                    }

                    composable(
                        route =
                            "game/{numColors}/{codeLength}/{allowDuplicates}",

                        arguments = listOf(

                            navArgument("numColors") {
                                type = NavType.IntType
                            },

                            navArgument("codeLength") {
                                type = NavType.IntType
                            },

                            navArgument("allowDuplicates") {
                                type = NavType.BoolType
                            }
                        )
                    ) { backStackEntry ->

                        val numColors =
                            backStackEntry.arguments
                                ?.getInt("numColors")
                                ?: 6

                        val codeLength =
                            backStackEntry.arguments
                                ?.getInt("codeLength")
                                ?: 4

                        val allowDuplicates =
                            backStackEntry.arguments
                                ?.getBoolean(
                                    "allowDuplicates"
                                )
                                ?: true

                        val gameViewModel: GameViewModel = viewModel(
                            factory = GameViewModelFactory(
                                gameRepository
                            )
                        )

                        val isGameReady by
                            gameViewModel.isGameReady.collectAsState()

                        LaunchedEffect(
                            numColors,
                            codeLength,
                            allowDuplicates
                        ) {

                            gameViewModel
                                .initializeNewGameIfNeeded(
                                    GameSettings(
                                        numColors = numColors,
                                        codeLength = codeLength,
                                        allowDuplicates =
                                            allowDuplicates,
                                        maxAttempts = 10
                                    )
                                )
                        }

                        if (isGameReady) {

                            GameScreen(
                                viewModel = gameViewModel,

                                onBackPressed = {
                                    navController.popBackStack()
                                },

                                onSaveGame = {
                                    gameViewModel.saveGame()
                                }
                            )
                        }
                    }
                }
            }
        }
    }
}


class GameViewModelFactory(
    private val repository: GameRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                GameViewModel::class.java
            )
        ) {

            @Suppress("UNCHECKED_CAST")
            return GameViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}

class HomeViewModelFactory(
    private val repository: GameRepository
) : ViewModelProvider.Factory {

    override fun <T : ViewModel> create(
        modelClass: Class<T>
    ): T {

        if (
            modelClass.isAssignableFrom(
                HomeViewModel::class.java
            )
        ) {

            @Suppress("UNCHECKED_CAST")
            return HomeViewModel(repository) as T
        }

        throw IllegalArgumentException(
            "Unknown ViewModel class"
        )
    }
}