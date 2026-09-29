package com.mastermind.ui.screens

import android.annotation.SuppressLint
import android.content.res.Configuration
import androidx.activity.compose.BackHandler
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Save
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalConfiguration
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import androidx.compose.foundation.BorderStroke
import com.mastermind.R
import com.mastermind.models.Attempt
import com.mastermind.models.ColorPeg
import com.mastermind.models.GameColors
import com.mastermind.ui.theme.AccentCyan
import com.mastermind.ui.theme.AccentTeal
import com.mastermind.ui.theme.BrandBlue
import com.mastermind.ui.theme.BrandBlueLight
import com.mastermind.viewModel.GameViewModel
import com.mastermind.ui.components.MastermindBackground
import kotlinx.coroutines.delay


private val GameBackgroundTop = Color(0xFF07111F)
private val GameBackgroundBottom = Color(0xFF0E1830)
private val GameSurface = Color(0xE6121D31)
private val GameSurfaceStrong = Color(0xF21A2740)
private val GameOutline = Color(0xFF2B4165)
private val GameTextPrimary = Color(0xFFF8FAFC)
private val GameTextSecondary = Color(0xFF9FB0C7)

@Composable
fun GameScreen(
    viewModel: GameViewModel,
    onBackPressed: () -> Unit,
    onSaveGame: () -> Unit
) {
    val gameState by viewModel.gameState.collectAsState()
    val currentAttempt by viewModel.currentAttempt.collectAsState()
    val selectedColorIndex by viewModel.selectedColorIndex.collectAsState()
    val gameTime by viewModel.gameTime.collectAsState()
    val isFromHistory by viewModel.isFromHistory.collectAsState()
    val compatibleCombinations by viewModel.compatibleCombinations.collectAsState()

    val configuration = LocalConfiguration.current
    val isLandscape =
        configuration.orientation == Configuration.ORIENTATION_LANDSCAPE

    var showSaveDialog by remember { mutableStateOf(false) }
    var showBackConfirmDialog by remember { mutableStateOf(false) }

    val handleBack: () -> Unit = {
        if (!gameState.isGameOver) {
            showBackConfirmDialog = true
        } else {
            onBackPressed()
        }
    }

    BackHandler(onBack = handleBack)

    val availableColors = remember(gameState.settings.numColors) {
        GameColors.getColorPegs(gameState.settings.numColors)
    }

    LaunchedEffect(gameState.isGameOver) {
        if (!gameState.isGameOver) {
            while (true) {
                delay(1000)
                viewModel.incrementTime()
            }
        }
    }

    MastermindBackground {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = if (isLandscape) 20.dp else 16.dp,
                    vertical = 12.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            TopActionsSection(
                gameTime = gameTime,
                onBackPressed = handleBack,
                onSaveGame = { showSaveDialog = true }
            )

            Spacer(modifier = Modifier.height(if (isLandscape) 8.dp else 14.dp))

            if (!isLandscape) {
                Column(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    TitleSection()

                    Spacer(modifier = Modifier.height(12.dp))

                    SecretCodeSection(
                        secretCode = gameState.secretCode,
                        isRevealed = gameState.isGameOver
                    )

                    Spacer(modifier = Modifier.height(12.dp))

                    GameStatsRow(
                        usedAttempts = gameState.getUsedAttempts(),
                        maxAttempts = gameState.settings.maxAttempts,
                        compatibleCombinations = compatibleCombinations,
                        score = if (gameState.isGameOver && gameState.isWon) {
                            gameState.getScore(gameTime)
                        } else {
                            null
                        },
                        isGameOver = gameState.isGameOver
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    AttemptsSection(
                        attempts = gameState.attempts,
                        modifier = Modifier.weight(1f)
                    )

                    Spacer(modifier = Modifier.height(14.dp))

                    if (!gameState.isGameOver) {
                        CurrentAttemptSection(
                            currentAttempt = currentAttempt,
                            availableColors = availableColors,
                            selectedColorIndex = selectedColorIndex,
                            onColorSelected = viewModel::setSelectedColor,
                            onPegClick = viewModel::setPegEmpty,
                            onReset = viewModel::resetAttempt,
                            onConfirm = viewModel::confirmAttempt,
                            canConfirm = currentAttempt.none {
                                it == ColorPeg.EMPTY
                            }
                        )
                    } else {
                        GameOverSection(
                            isWon = gameState.isWon,
                            score = gameState.getScore(gameTime),
                            onNewGame = { viewModel.newGame() },
                            isFromHistory = isFromHistory
                        )
                    }
                }
            } else {
                Row(
                    modifier = Modifier
                        .fillMaxWidth()
                        .weight(1f),
                    horizontalArrangement = Arrangement.spacedBy(16.dp)
                ) {
                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        TitleSection(compact = true)

                        Spacer(modifier = Modifier.height(8.dp))

                        SecretCodeSection(
                            secretCode = gameState.secretCode,
                            isRevealed = gameState.isGameOver
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        GameStatsRow(
                            usedAttempts = gameState.getUsedAttempts(),
                            maxAttempts = gameState.settings.maxAttempts,
                            compatibleCombinations = compatibleCombinations,
                            score = if (
                                gameState.isGameOver &&
                                gameState.isWon
                            ) {
                                gameState.getScore(gameTime)
                            } else {
                                null
                            },
                            isGameOver = gameState.isGameOver
                        )
                    }

                    Column(
                        modifier = Modifier
                            .weight(1f)
                            .fillMaxHeight(),
                        horizontalAlignment = Alignment.CenterHorizontally
                    ) {
                        AttemptsSection(
                            attempts = gameState.attempts,
                            modifier = Modifier.weight(1f)
                        )

                        Spacer(modifier = Modifier.height(8.dp))

                        if (!gameState.isGameOver) {
                            CurrentAttemptSection(
                                currentAttempt = currentAttempt,
                                availableColors = availableColors,
                                selectedColorIndex = selectedColorIndex,
                                onColorSelected = viewModel::setSelectedColor,
                                onPegClick = viewModel::setPegEmpty,
                                onReset = viewModel::resetAttempt,
                                onConfirm = viewModel::confirmAttempt,
                                canConfirm = currentAttempt.none {
                                    it == ColorPeg.EMPTY
                                },
                                compact = true
                            )
                        } else {
                            GameOverSection(
                                isWon = gameState.isWon,
                                score = gameState.getScore(gameTime),
                                onNewGame = { viewModel.newGame() },
                                isFromHistory = isFromHistory
                            )
                        }
                    }
                }
            }
        }

        if (showSaveDialog) {
            Dialog(onDismissRequest = { showSaveDialog = false }) {
                SaveGameDialog(
                    onSave = {
                        showSaveDialog = false
                        onSaveGame()
                    },
                    onDismiss = {
                        showSaveDialog = false
                    }
                )
            }
        }

        if (showBackConfirmDialog) {
            Dialog(onDismissRequest = { showBackConfirmDialog = false }) {
                BackConfirmationDialog(
                    onSaveAndBack = {
                        showBackConfirmDialog = false
                        viewModel.saveCurrentGame {
                            onBackPressed()
                        }
                    },
                    onBackWithoutSave = {
                        showBackConfirmDialog = false
                        onBackPressed()
                    },
                    onDismiss = {
                        showBackConfirmDialog = false
                    }
                )
            }
        }
    }
}

@Composable
private fun BackConfirmationDialog(
    onSaveAndBack: () -> Unit,
    onBackWithoutSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GameSurfaceStrong
        ),
        border = BorderStroke(
            width = 1.dp,
            color = GameOutline
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.save_before_exit),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = GameTextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.save_before_exit_prompt),
                style = MaterialTheme.typography.bodyLarge,
                color = GameTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

            Column(
                verticalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                Button(
                    onClick = onSaveAndBack,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.save_and_exit),
                        fontWeight = FontWeight.SemiBold
                    )
                }

                OutlinedButton(
                    onClick = onBackWithoutSave,
                    modifier = Modifier
                        .fillMaxWidth()
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GameTextPrimary
                    ),
                    border = BorderStroke(
                        1.dp,
                        GameOutline
                    )
                ) {
                    Text(stringResource(R.string.exit_without_saving))
                }

                TextButton(
                    onClick = onDismiss,
                    modifier = Modifier.fillMaxWidth(),
                    colors = ButtonDefaults.textButtonColors(
                        contentColor = GameTextSecondary
                    )
                ) {
                    Text(stringResource(R.string.cancel))
                }
            }
        }
    }
}

@SuppressLint("DefaultLocale")
@Composable
private fun TopActionsSection(
    gameTime: Int,
    onBackPressed: () -> Unit,
    onSaveGame: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
    ) {
        ActionButton(
            onClick = onBackPressed
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = GameTextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }

        Surface(
            shape = RoundedCornerShape(18.dp),
            color = BrandBlue.copy(alpha = 0.20f),
            border = BorderStroke(
                width = 1.dp,
                color = BrandBlueLight.copy(alpha = 0.45f)
            )
        ) {
            Row(
                modifier = Modifier.padding(
                    horizontal = 16.dp,
                    vertical = 10.dp
                ),
                verticalAlignment = Alignment.CenterVertically,
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                Icon(
                    imageVector = Icons.Default.Timer,
                    contentDescription = stringResource(R.string.timer),
                    tint = AccentCyan,
                    modifier = Modifier.size(18.dp)
                )

                Text(
                    text = "${gameTime / 60}:${String.format("%02d", gameTime % 60)}",
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = GameTextPrimary
                )
            }
        }

        ActionButton(
            onClick = onSaveGame
        ) {
            Icon(
                imageVector = Icons.Default.Save,
                contentDescription = stringResource(R.string.save_game),
                tint = GameTextPrimary,
                modifier = Modifier.size(21.dp)
            )
        }
    }
}

@Composable
private fun ActionButton(
    onClick: () -> Unit,
    content: @Composable BoxScope.() -> Unit
) {
    Box(
        modifier = Modifier
            .size(48.dp)
            .clip(RoundedCornerShape(16.dp))
            .background(GameSurfaceStrong)
            .border(
                width = 1.dp,
                color = GameOutline,
                shape = RoundedCornerShape(16.dp)
            )
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center,
        content = content
    )
}

@Composable
private fun TitleSection(
    compact: Boolean = false
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.game_title).uppercase(),
            style = if (compact) {
                MaterialTheme.typography.headlineMedium.copy(
                    fontWeight = FontWeight.Bold,
                    letterSpacing = 1.4.sp
                )
            } else {
                MaterialTheme.typography.headlineLarge.copy(
                    fontWeight = FontWeight.Bold,
                    fontSize = 34.sp,
                    letterSpacing = 1.6.sp
                )
            },
            color = GameTextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false,
            modifier = Modifier.fillMaxWidth()
        )

        Spacer(modifier = Modifier.height(7.dp))

        Box(
            modifier = Modifier
                .width(if (compact) 72.dp else 92.dp)
                .height(3.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            AccentCyan,
                            BrandBlue,
                            AccentTeal
                        )
                    )
                )
        )
    }
}

@Composable
private fun SecretCodeSection(
    secretCode: List<ColorPeg>,
    isRevealed: Boolean
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = GameSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = BrandBlue.copy(alpha = 0.40f)
        ),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 16.dp,
                    vertical = 14.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isRevealed) {
                    stringResource(R.string.game_secret_code)
                } else {
                    stringResource(R.string.game_hidden_code)
                },
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = AccentCyan,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                verticalAlignment = Alignment.CenterVertically
            ) {
                secretCode.forEach { peg ->
                    if (isRevealed) {
                        ColorPegDisplay(
                            peg = peg,
                            size = 38f
                        )
                    } else {
                        HiddenPegDisplay(size = 38f)
                    }
                }
            }
        }
    }
}

@Composable
private fun GameStatsRow(
    usedAttempts: Int,
    maxAttempts: Int,
    compatibleCombinations: Int,
    score: Int?,
    isGameOver: Boolean
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        StatCard(
            title = stringResource(R.string.game_attempts),
            value = "$usedAttempts/$maxAttempts",
            modifier = Modifier.weight(1f)
        )

        if (score != null) {
            StatCard(
                title = stringResource(R.string.game_score),
                value = score.toString(),
                modifier = Modifier.weight(1f),
                accent = AccentTeal
            )
        } else if (!isGameOver) {
            StatCard(
                title = if (usedAttempts == 0) {
                    stringResource(R.string.totalCombinations)
                } else {
                    stringResource(R.string.possibleCombinations)
                },
                value = compatibleCombinations.toString(),
                modifier = Modifier.weight(1f),
                accent = AccentCyan
            )
        }
    }
}

@Composable
private fun StatCard(
    title: String,
    value: String,
    modifier: Modifier = Modifier,
    accent: Color = BrandBlueLight
) {
    Card(
        modifier = modifier,
        colors = CardDefaults.cardColors(
            containerColor = GameSurfaceStrong
        ),
        border = BorderStroke(
            width = 1.dp,
            color = accent.copy(alpha = 0.30f)
        ),
        shape = RoundedCornerShape(20.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 12.dp,
                    vertical = 13.dp
                ),
            horizontalAlignment = Alignment.Start
        ) {
            Text(
                text = title,
                style = MaterialTheme.typography.labelMedium,
                color = GameTextSecondary,
                maxLines = 2
            )

            Spacer(modifier = Modifier.height(3.dp))

            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = accent,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun AttemptsSection(
    attempts: List<Attempt>,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier.fillMaxWidth()
    ) {
        Text(
            text = stringResource(R.string.game_hystory),
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = GameTextPrimary,
            modifier = Modifier.padding(
                start = 4.dp,
                bottom = 8.dp
            )
        )

        Card(
            modifier = Modifier
                .fillMaxWidth()
                .weight(1f),
            colors = CardDefaults.cardColors(
                containerColor = GameSurface
            ),
            border = BorderStroke(
                1.dp,
                GameOutline.copy(alpha = 0.85f)
            ),
            shape = RoundedCornerShape(20.dp)
        ) {
            LazyColumn(
                modifier = Modifier
                    .fillMaxSize()
                    .padding(10.dp),
                verticalArrangement = Arrangement.spacedBy(6.dp),
                reverseLayout = true
            ) {
                items(attempts.reversed()) { attempt ->
                    AttemptRow(attempt = attempt)
                }

                if (attempts.isEmpty()) {
                    item {
                        Text(
                            text = stringResource(R.string.game_no_attempts),
                            style = MaterialTheme.typography.bodyMedium,
                            color = GameTextSecondary,
                            textAlign = TextAlign.Center,
                            modifier = Modifier
                                .fillMaxWidth()
                                .padding(20.dp)
                        )
                    }
                }
            }
        }
    }
}

@Composable
private fun CurrentAttemptSection(
    currentAttempt: List<ColorPeg>,
    availableColors: List<ColorPeg>,
    selectedColorIndex: Int,
    onColorSelected: (Int) -> Unit,
    onPegClick: (Int) -> Unit,
    onReset: () -> Unit,
    onConfirm: () -> Unit,
    canConfirm: Boolean,
    compact: Boolean = false
) {
    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = GameSurfaceStrong
        ),
        border = BorderStroke(
            width = 1.dp,
            color = BrandBlue.copy(alpha = 0.35f)
        ),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = if (compact) 12.dp else 16.dp,
                    vertical = if (compact) 10.dp else 14.dp
                ),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.game_attempt),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = GameTextPrimary,
                textAlign = TextAlign.Center
            )

            Spacer(modifier = Modifier.height(if (compact) 8.dp else 12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(
                    if (compact) 8.dp else 12.dp
                ),
                verticalAlignment = Alignment.CenterVertically
            ) {
                currentAttempt.forEachIndexed { index, peg ->
                    ColorPegDisplay(
                        peg = peg,
                        size = if (compact) 38f else 44f,
                        modifier = Modifier.clickable {
                            onPegClick(index)
                        }
                    )
                }
            }

            Spacer(modifier = Modifier.height(if (compact) 8.dp else 12.dp))

            ColorPalette(
                availableColors = availableColors,
                selectedColorIndex = selectedColorIndex,
                onColorSelected = onColorSelected,
                compact = compact
            )

            Spacer(modifier = Modifier.height(if (compact) 8.dp else 12.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onReset,
                    modifier = Modifier
                        .weight(1f)
                        .height(if (compact) 44.dp else 48.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = BrandBlueLight
                    ),
                    border = BorderStroke(
                        width = 1.dp,
                        color = BrandBlueLight.copy(alpha = 0.65f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.game_reset),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = onConfirm,
                    enabled = canConfirm,
                    modifier = Modifier
                        .weight(1f)
                        .height(if (compact) 44.dp else 48.dp),
                    shape = RoundedCornerShape(15.dp),
                    colors = ButtonDefaults.buttonColors(
                        containerColor = BrandBlue,
                        contentColor = Color.White,
                        disabledContainerColor = BrandBlue.copy(alpha = 0.28f),
                        disabledContentColor = Color.White.copy(alpha = 0.55f)
                    )
                ) {
                    Text(
                        text = stringResource(R.string.game_submit),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}

@Composable
private fun ColorPalette(
    availableColors: List<ColorPeg>,
    selectedColorIndex: Int,
    onColorSelected: (Int) -> Unit,
    compact: Boolean
) {
    if (availableColors.size > 6) {
        Column(
            horizontalAlignment = Alignment.CenterHorizontally,
            verticalArrangement = Arrangement.spacedBy(7.dp)
        ) {
            availableColors.chunked(5).forEach { colorRow ->
                Row(
                    horizontalArrangement = Arrangement.spacedBy(7.dp)
                ) {
                    colorRow.forEach { color ->
                        val index = availableColors.indexOf(color)

                        SelectableColorPeg(
                            color = color,
                            selected = selectedColorIndex == index,
                            onClick = {
                                onColorSelected(index)
                            },
                            compact = compact
                        )
                    }
                }
            }
        }
    } else {
        Row(
            horizontalArrangement = Arrangement.spacedBy(
                if (compact) 6.dp else 8.dp
            )
        ) {
            availableColors.forEachIndexed { index, color ->
                SelectableColorPeg(
                    color = color,
                    selected = selectedColorIndex == index,
                    onClick = {
                        onColorSelected(index)
                    },
                    compact = compact
                )
            }
        }
    }
}

@Composable
private fun SelectableColorPeg(
    color: ColorPeg,
    selected: Boolean,
    onClick: () -> Unit,
    compact: Boolean
) {
    Box(
        modifier = Modifier
            .padding(2.dp)
            .border(
                width = if (selected) 3.dp else 1.dp,
                color = if (selected) {
                    AccentCyan
                } else {
                    GameOutline
                },
                shape = CircleShape
            )
            .padding(3.dp)
    ) {
        ColorPegDisplay(
            peg = color,
            size = if (compact) 31f else 35f,
            modifier = Modifier.clickable(onClick = onClick)
        )
    }
}

@Composable
private fun GameOverSection(
    isWon: Boolean,
    score: Int,
    onNewGame: () -> Unit,
    isFromHistory: Boolean
) {
    val accent = if (isWon) {
        AccentTeal
    } else {
        MaterialTheme.colorScheme.error
    }

    Card(
        modifier = Modifier.fillMaxWidth(),
        colors = CardDefaults.cardColors(
            containerColor = GameSurfaceStrong
        ),
        border = BorderStroke(
            width = 1.dp,
            color = accent.copy(alpha = 0.55f)
        ),
        shape = RoundedCornerShape(22.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(20.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = if (isWon) {
                    stringResource(R.string.game_won)
                } else {
                    stringResource(R.string.game_lose)
                },
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = GameTextPrimary,
                textAlign = TextAlign.Center
            )

            if (isWon) {
                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(
                        R.string.game_final_score,
                        score
                    ),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = AccentTeal,
                    textAlign = TextAlign.Center
                )
            }

            if (!isFromHistory) {
                Spacer(modifier = Modifier.height(14.dp))

                Button(
                    onClick = onNewGame,
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.new_game),
                        fontWeight = FontWeight.SemiBold
                    )
                }
            }
        }
    }
}

@Composable
private fun AttemptRow(
    attempt: Attempt,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier.fillMaxWidth(),
        color = Color.White.copy(alpha = 0.035f),
        shape = RoundedCornerShape(14.dp)
    ) {
        Row(
            modifier = Modifier
                .fillMaxWidth()
                .padding(
                    horizontal = 10.dp,
                    vertical = 9.dp
                ),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Row(
                horizontalArrangement = Arrangement.spacedBy(6.dp)
            ) {
                attempt.guess.forEach { peg ->
                    ColorPegDisplay(
                        peg = peg,
                        size = 27f
                    )
                }
            }

            FeedbackDisplay(
                blackPegs = attempt.correctPosition,
                whitePegs = attempt.correctColor,
                totalPegs = attempt.guess.size,
                size = 13f
            )
        }
    }
}

@Composable
private fun ColorPegDisplay(
    peg: ColorPeg,
    size: Float = 32f,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(
                if (peg == ColorPeg.EMPTY) {
                    Color.White.copy(alpha = 0.07f)
                } else {
                    peg.color
                }
            )
            .border(
                width = if (peg == ColorPeg.EMPTY) 1.5.dp else 1.dp,
                color = if (peg == ColorPeg.EMPTY) {
                    GameOutline
                } else {
                    Color.White.copy(alpha = 0.28f)
                },
                shape = CircleShape
            )
    ) {
        if (peg == ColorPeg.EMPTY) {
            Box(
                modifier = Modifier
                    .size((size * 0.32f).dp)
                    .align(Alignment.Center)
                    .clip(CircleShape)
                    .border(
                        width = 1.dp,
                        color = GameTextSecondary.copy(alpha = 0.65f),
                        shape = CircleShape
                    )
            )
        }
    }
}

@Composable
private fun HiddenPegDisplay(
    size: Float = 32f,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    Box(
        modifier = modifier
            .size(size.dp)
            .clip(CircleShape)
            .background(Color.White.copy(alpha = 0.08f))
            .border(
                width = 1.5.dp,
                color = AccentCyan.copy(alpha = 0.55f),
                shape = CircleShape
            ),
        contentAlignment = Alignment.Center
    ) {
        Text(
            text = "?",
            style = MaterialTheme.typography.titleLarge.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = GameTextPrimary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun FeedbackDisplay(
    blackPegs: Int,
    whitePegs: Int,
    totalPegs: Int,
    size: Float = 12f,
    @SuppressLint("ModifierParameter") modifier: Modifier = Modifier
) {
    val emptyPegs =
        totalPegs - (blackPegs + whitePegs)

    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(3.dp)
    ) {
        repeat(blackPegs) {
            Box(
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .background(Color(0xFF020617))
                    .border(
                        0.5.dp,
                        Color.White.copy(alpha = 0.30f),
                        CircleShape
                    )
            )
        }

        repeat(whitePegs) {
            Box(
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .background(Color.White)
                    .border(
                        0.5.dp,
                        GameOutline,
                        CircleShape
                    )
            )
        }

        repeat(emptyPegs) {
            Box(
                modifier = Modifier
                    .size(size.dp)
                    .clip(CircleShape)
                    .background(Color.Transparent)
                    .border(
                        0.75.dp,
                        GameTextSecondary.copy(alpha = 0.55f),
                        CircleShape
                    )
            )
        }
    }
}

@Composable
private fun SaveGameDialog(
    onSave: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        modifier = Modifier.padding(16.dp),
        colors = CardDefaults.cardColors(
            containerColor = GameSurfaceStrong
        ),
        border = BorderStroke(
            width = 1.dp,
            color = GameOutline
        ),
        shape = RoundedCornerShape(24.dp)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.save_game),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = GameTextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(10.dp))

            Text(
                text = stringResource(R.string.save_game_prompt),
                style = MaterialTheme.typography.bodyLarge,
                color = GameTextSecondary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(22.dp))

            Row(
                horizontalArrangement = Arrangement.spacedBy(10.dp),
                modifier = Modifier.fillMaxWidth()
            ) {
                OutlinedButton(
                    onClick = onDismiss,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp),
                    colors = ButtonDefaults.outlinedButtonColors(
                        contentColor = GameTextPrimary
                    ),
                    border = BorderStroke(
                        1.dp,
                        GameOutline
                    )
                ) {
                    Text(
                        text = stringResource(R.string.cancel),
                        textAlign = TextAlign.Center
                    )
                }

                Button(
                    onClick = onSave,
                    modifier = Modifier
                        .weight(1f)
                        .height(50.dp),
                    shape = RoundedCornerShape(16.dp)
                ) {
                    Text(
                        text = stringResource(R.string.save),
                        fontWeight = FontWeight.SemiBold,
                        textAlign = TextAlign.Center
                    )
                }
            }
        }
    }
}
