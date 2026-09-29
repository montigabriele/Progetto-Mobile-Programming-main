package com.mastermind.ui.screens

import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.itemsIndexed
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.mastermind.R
import com.mastermind.models.GameState
import com.mastermind.ui.theme.AccentCyan
import com.mastermind.ui.theme.AccentTeal
import com.mastermind.ui.theme.BrandBlue
import com.mastermind.ui.theme.BrandBlueLight
import com.mastermind.viewModel.GameViewModel
import com.mastermind.ui.components.MastermindBackground
import java.text.SimpleDateFormat
import java.util.Locale

private val HistoryBackgroundTop = Color(0xFF07111F)
private val HistoryBackgroundBottom = Color(0xFF0E1830)
private val HistorySurface = Color(0xE6121D31)
private val HistorySurfaceStrong = Color(0xF21A2740)
private val HistoryOutline = Color(0xFF2B4165)
private val HistoryTextPrimary = Color(0xFFF8FAFC)
private val HistoryTextSecondary = Color(0xFF9FB0C7)

@Composable
fun SavedGamesScreen(
    viewModel: GameViewModel,
    onGameSelected: (GameState) -> Unit,
    onBack: () -> Unit
) {
    val savedGames by viewModel.savedGames.collectAsState()
    var showDeleteConfirmation by remember {
        mutableStateOf<GameState?>(null)
    }

    LaunchedEffect(Unit) {
        viewModel.loadSavedGames()
    }

    MastermindBackground {

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .padding(
                    horizontal = 18.dp,
                    vertical = 14.dp
                )
        ) {
            HistoryTopBar(onBackPressed = onBack)

            Spacer(modifier = Modifier.height(22.dp))

            HistoryTitle()

            Spacer(modifier = Modifier.height(20.dp))

            Box(
                modifier = Modifier
                    .fillMaxWidth()
                    .weight(1f)
            ) {
                if (savedGames.isEmpty()) {
                    EmptyStateContent()
                } else {
                    SavedGamesContent(
                        savedGames = savedGames,
                        onGameSelected = onGameSelected,
                        onDeleteGame = { game ->
                            showDeleteConfirmation = game
                        }
                    )
                }
            }
        }

        showDeleteConfirmation?.let { gameToDelete ->
            DeleteConfirmationDialog(
                onConfirm = {
                    viewModel.deleteSavedGame(gameToDelete.id)
                    showDeleteConfirmation = null
                },
                onDismiss = {
                    showDeleteConfirmation = null
                }
            )
        }
    }
}

@Composable
private fun HistoryTopBar(
    onBackPressed: () -> Unit
) {
    Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.Start,
        verticalAlignment = Alignment.CenterVertically
    ) {
        Box(
            modifier = Modifier
                .size(48.dp)
                .clip(RoundedCornerShape(16.dp))
                .background(HistorySurfaceStrong)
                .clickable(onClick = onBackPressed),
            contentAlignment = Alignment.Center
        ) {
            Icon(
                imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                contentDescription = stringResource(R.string.back),
                tint = HistoryTextPrimary,
                modifier = Modifier.size(22.dp)
            )
        }
    }
}

@Composable
private fun HistoryTitle() {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.menu_history).uppercase(),
            style = MaterialTheme.typography.headlineLarge.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 34.sp,
                letterSpacing = 1.3.sp
            ),
            color = HistoryTextPrimary,
            textAlign = TextAlign.Center
        )

        Spacer(modifier = Modifier.height(7.dp))

        Box(
            modifier = Modifier
                .width(92.dp)
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

        Spacer(modifier = Modifier.height(10.dp))

        Text(
            text = stringResource(R.string.cron),
            style = MaterialTheme.typography.bodyMedium,
            color = HistoryTextSecondary,
            textAlign = TextAlign.Center
        )
    }
}

@Composable
private fun EmptyStateContent() {
    Box(
        modifier = Modifier.fillMaxSize(),
        contentAlignment = Alignment.Center
    ) {
        Card(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 10.dp),
            colors = CardDefaults.cardColors(
                containerColor = HistorySurface
            ),
            border = BorderStroke(
                1.dp,
                HistoryOutline
            ),
            shape = RoundedCornerShape(24.dp)
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth()
                    .padding(
                        horizontal = 28.dp,
                        vertical = 34.dp
                    ),
                horizontalAlignment = Alignment.CenterHorizontally
            ) {
                EmptyPegDecoration()

                Spacer(modifier = Modifier.height(22.dp))

                Text(
                    text = stringResource(R.string.no_saved_games),
                    style = MaterialTheme.typography.titleLarge.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = HistoryTextPrimary,
                    textAlign = TextAlign.Center
                )

                Spacer(modifier = Modifier.height(8.dp))

                Text(
                    text = stringResource(
                        R.string.no_saved_games_description
                    ),
                    style = MaterialTheme.typography.bodyMedium,
                    color = HistoryTextSecondary,
                    textAlign = TextAlign.Center
                )
            }
        }
    }
}

@Composable
private fun EmptyPegDecoration() {
    Row(
        horizontalArrangement = Arrangement.spacedBy(10.dp)
    ) {
        repeat(4) { index ->
            Box(
                modifier = Modifier
                    .size(34.dp)
                    .clip(CircleShape)
                    .background(
                        if (index == 1) {
                            BrandBlue.copy(alpha = 0.30f)
                        } else {
                            Color.White.copy(alpha = 0.055f)
                        }
                    )
                    .then(
                        Modifier.background(Color.Transparent)
                    )
            )
        }
    }
}

@Composable
private fun SavedGamesContent(
    savedGames: List<GameState>,
    onGameSelected: (GameState) -> Unit,
    onDeleteGame: (GameState) -> Unit
) {
    LazyColumn(
        modifier = Modifier.fillMaxSize(),
        verticalArrangement = Arrangement.spacedBy(14.dp),
        contentPadding = PaddingValues(bottom = 22.dp)
    ) {
        itemsIndexed(
            items = savedGames.sortedByDescending { it.date },
            key = { _, game -> game.id }
        ) { index, game ->
            SavedGameCard(
                game = game,
                index = index + 1,
                onSelected = {
                    onGameSelected(game)
                },
                onDelete = {
                    onDeleteGame(game)
                }
            )
        }
    }
}

@Composable
private fun SavedGameCard(
    game: GameState,
    index: Int,
    onSelected: () -> Unit,
    onDelete: () -> Unit
) {
    Card(
        onClick = onSelected,
        colors = CardDefaults.cardColors(
            containerColor = HistorySurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = if (game.isGameOver && game.isWon) {
                AccentTeal.copy(alpha = 0.30f)
            } else {
                BrandBlue.copy(alpha = 0.28f)
            }
        ),
        shape = RoundedCornerShape(22.dp),
        modifier = Modifier.fillMaxWidth()
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(18.dp),
            verticalArrangement = Arrangement.spacedBy(15.dp)
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Surface(
                    shape = RoundedCornerShape(12.dp),
                    color = BrandBlue.copy(alpha = 0.18f),
                    border = BorderStroke(
                        1.dp,
                        BrandBlueLight.copy(alpha = 0.28f)
                    )
                ) {
                    Text(
                        text = "#$index",
                        style = MaterialTheme.typography.titleMedium.copy(
                            fontWeight = FontWeight.SemiBold
                        ),
                        color = AccentCyan,
                        modifier = Modifier.padding(
                            horizontal = 12.dp,
                            vertical = 7.dp
                        )
                    )
                }

                Spacer(modifier = Modifier.width(10.dp))

                GameStatusChip(
                    isCompleted = game.isGameOver,
                    isWon = game.isWon
                )

                Spacer(modifier = Modifier.weight(1f))

                IconButton(
                    onClick = onDelete
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error.copy(
                            alpha = 0.90f
                        )
                    )
                }
            }

            HorizontalDivider(
                color = HistoryOutline.copy(alpha = 0.75f)
            )

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(12.dp)
            ) {
                GameStatItem(
                    value = "${game.attempts.size}/${game.settings.maxAttempts}",
                    label = stringResource(R.string.game_attempts),
                    accent = BrandBlueLight,
                    modifier = Modifier.weight(1f)
                )

                if (game.isWon) {
                    GameStatItem(
                        value = game
                            .getScore(game.gameTime)
                            .toString(),
                        label = stringResource(R.string.game_score),
                        accent = AccentTeal,
                        modifier = Modifier.weight(1f)
                    )
                } else {
                    GameStatItem(
                        value = game.settings.numColors.toString(),
                        label = stringResource(R.string.label_num_colors),
                        accent = AccentCyan,
                        modifier = Modifier.weight(1f)
                    )
                }
            }

            Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(8.dp)
            ) {
                ConfigChip(
                    text = "${game.settings.numColors}",
                    modifier = Modifier.weight(1f)
                )

                ConfigChip(
                    text = "${game.settings.codeLength}",
                    modifier = Modifier.weight(1f)
                )

                ConfigChip(
                    text = if (game.settings.allowDuplicates) {
                        stringResource(R.string.yes)
                    } else {
                        stringResource(R.string.no)
                    },
                    modifier = Modifier.weight(1f)
                )
            }

            Text(
                text = SimpleDateFormat(
                    stringResource(R.string.date),
                    Locale.getDefault()
                ).format(game.date),
                style = MaterialTheme.typography.bodySmall,
                color = HistoryTextSecondary,
                modifier = Modifier.fillMaxWidth()
            )
        }
    }
}

@Composable
private fun GameStatusChip(
    isCompleted: Boolean,
    isWon: Boolean
) {
    val text: String
    val containerColor: Color
    val contentColor: Color

    when {
        isCompleted && isWon -> {
            text = stringResource(R.string.win)
            containerColor = AccentTeal.copy(alpha = 0.18f)
            contentColor = AccentTeal
        }

        isCompleted && !isWon -> {
            text = stringResource(R.string.loss)
            containerColor =
                MaterialTheme.colorScheme.error.copy(alpha = 0.16f)
            contentColor = MaterialTheme.colorScheme.error
        }

        else -> {
            text = stringResource(R.string.game_in_progress)
            containerColor = BrandBlue.copy(alpha = 0.18f)
            contentColor = AccentCyan
        }
    }

    Surface(
        shape = RoundedCornerShape(12.dp),
        color = containerColor
    ) {
        Text(
            text = text,
            style = MaterialTheme.typography.labelMedium.copy(
                fontWeight = FontWeight.SemiBold
            ),
            color = contentColor,
            modifier = Modifier.padding(
                horizontal = 11.dp,
                vertical = 7.dp
            )
        )
    }
}

@Composable
private fun GameStatItem(
    value: String,
    label: String,
    accent: Color,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(16.dp),
        color = HistorySurfaceStrong,
        border = BorderStroke(
            1.dp,
            accent.copy(alpha = 0.20f)
        )
    ) {
        Column(
            modifier = Modifier.padding(
                horizontal = 12.dp,
                vertical = 12.dp
            )
        ) {
            Text(
                text = value,
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.Bold
                ),
                color = accent
            )

            Spacer(modifier = Modifier.height(2.dp))

            Text(
                text = label,
                style = MaterialTheme.typography.bodySmall,
                color = HistoryTextSecondary,
                maxLines = 2
            )
        }
    }
}

@Composable
private fun ConfigChip(
    text: String,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier,
        shape = RoundedCornerShape(13.dp),
        color = Color.White.copy(alpha = 0.045f),
        border = BorderStroke(
            1.dp,
            HistoryOutline.copy(alpha = 0.75f)
        )
    ) {
        Box(
            modifier = Modifier.padding(
                horizontal = 8.dp,
                vertical = 9.dp
            ),
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.labelMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = HistoryTextPrimary,
                textAlign = TextAlign.Center,
                maxLines = 1
            )
        }
    }
}

@Composable
private fun DeleteConfirmationDialog(
    onConfirm: () -> Unit,
    onDismiss: () -> Unit
) {
    AlertDialog(
        onDismissRequest = onDismiss,
        containerColor = HistorySurfaceStrong,
        titleContentColor = HistoryTextPrimary,
        textContentColor = HistoryTextSecondary,
        title = {
            Text(
                text = stringResource(R.string.game_delete),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth(),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        },
        text = {
            Text(
                text = stringResource(
                    R.string.game_delete_confirmation
                ),
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )
        },
        confirmButton = {
            TextButton(
                onClick = onConfirm,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = MaterialTheme.colorScheme.error
                )
            ) {
                Text(stringResource(R.string.ok))
            }
        },
        dismissButton = {
            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = HistoryTextSecondary
                )
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    )
}
