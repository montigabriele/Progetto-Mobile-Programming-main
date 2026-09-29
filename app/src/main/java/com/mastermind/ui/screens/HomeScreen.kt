package com.mastermind.ui.screens

import android.app.Activity
import android.content.Context
import android.content.res.Configuration
import androidx.compose.foundation.BorderStroke
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.foundation.verticalScroll
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.automirrored.filled.ArrowBack
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.compose.ui.window.Dialog
import com.mastermind.R
import com.mastermind.data.GameRepository
import com.mastermind.ui.theme.AccentCyan
import com.mastermind.ui.theme.AccentTeal
import com.mastermind.ui.theme.BrandBlue
import com.mastermind.ui.theme.BrandBlueLight
import com.mastermind.ui.theme.MastermindTheme
import com.mastermind.utils.persistLanguage
import com.mastermind.viewModel.HomeViewModel
import com.mastermind.ui.theme.MastermindAqua
import com.mastermind.ui.theme.MastermindAquaLight
import com.mastermind.ui.theme.MastermindAquaSoft
import com.mastermind.ui.components.MastermindBackground
import java.util.Locale

private val HomeBackgroundTop = Color(0xFF08101D)
private val HomeBackgroundBottom = Color(0xFF101A2C)
private val HomeSurface = Color(0xE6121D31)
private val HomeSurfaceStrong = Color(0xF21A2740)
private val HomeOutline = Color(0xFF2B4165)
private val HomeTextPrimary = Color(0xFFF8FAFC)
private val HomeTextSecondary = Color(0xFF9FB0C7)

@Composable
fun HomeScreen(
    viewModel: HomeViewModel,
    onPlayClicked: (numColors: Int, codeLength: Int, allowDuplicates: Boolean) -> Unit,
    onSavedGamesClicked: () -> Unit,
    hasCurrentGame: Boolean = false,
    onContinueGameClicked: () -> Unit = {}
) {
    var showSettings by remember { mutableStateOf(false) }
    var showRulesDialog by remember { mutableStateOf(false) }
    var showLanguageDialog by remember { mutableStateOf(false) }

    val numColors by viewModel.numColors.collectAsState()
    val codeLength by viewModel.codeLength.collectAsState()
    val allowDuplicates by viewModel.allowDuplicates.collectAsState()

    val context = LocalContext.current
    val activity = context as? Activity

    val colorOptions = listOf(6, 8, 10)
    val lengthOptions = listOf(4, 5)

    MastermindBackground {

        if (showLanguageDialog) {
            Dialog(onDismissRequest = { showLanguageDialog = false }) {
                LanguageDialog(
                    onItalian = {
                        activity?.let { setAppLocale(it, "it") }
                        showLanguageDialog = false
                    },
                    onEnglish = {
                        activity?.let { setAppLocale(it, "en") }
                        showLanguageDialog = false
                    },
                    onDismiss = { showLanguageDialog = false }
                )
            }
        }

        if (showRulesDialog) {
            Dialog(onDismissRequest = { showRulesDialog = false }) {
                RulesDialog(onDismiss = { showRulesDialog = false })
            }
        }

        Column(
            modifier = Modifier
                .fillMaxSize()
                .statusBarsPadding()
                .navigationBarsPadding()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 24.dp, vertical = 24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Spacer(modifier = Modifier.height(18.dp))

            BrandHeader()

            Spacer(modifier = Modifier.height(42.dp))

            if (!showSettings) {
                HomeMenu(
                    hasCurrentGame = hasCurrentGame,
                    onContinueGameClicked = onContinueGameClicked,
                    onNewGameClicked = { showSettings = true },
                    onRulesClicked = { showRulesDialog = true },
                    onSavedGamesClicked = onSavedGamesClicked,
                    onLanguageClicked = { showLanguageDialog = true }
                )
            } else {
                SettingsPanel(
                    numColors = numColors,
                    codeLength = codeLength,
                    allowDuplicates = allowDuplicates,
                    colorOptions = colorOptions,
                    lengthOptions = lengthOptions,
                    onBack = { showSettings = false },
                    onNumColorsSelected = viewModel::setNumColors,
                    onCodeLengthSelected = viewModel::setCodeLength,
                    onDuplicatesSelected = viewModel::setAllowDuplicates,
                    onPlayClicked = {
                        onPlayClicked(
                            numColors,
                            codeLength,
                            allowDuplicates
                        )
                    }
                )
            }

            Spacer(modifier = Modifier.height(24.dp))
        }
    }
}

@Composable
private fun BrandHeader() {
    Column(
        horizontalAlignment = Alignment.CenterHorizontally
    ) {
        Text(
            text = stringResource(R.string.app_name).uppercase(),
            style = MaterialTheme.typography.displayMedium.copy(
                fontWeight = FontWeight.Bold,
                fontSize = 35.sp,
                letterSpacing = 2.sp
            ),
            color = HomeTextPrimary,
            textAlign = TextAlign.Center,
            maxLines = 1,
            softWrap = false
        )

        Spacer(modifier = Modifier.height(10.dp))

        Box(
            modifier = Modifier
                .width(120.dp)
                .height(4.dp)
                .clip(CircleShape)
                .background(
                    brush = Brush.horizontalGradient(
                        listOf(
                            MastermindAquaLight,
                            MastermindAqua,
                            BrandBlue
                        )
                    )
                )
        )

        Spacer(modifier = Modifier.height(14.dp))

        Row(
            horizontalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            listOf(
                Color(0xFFDC2626),
                Color(0xFF16A34A),
                Color(0xFF2563EB),
                Color(0xFFEAB308),
                Color(0xFFEA580C),
                Color(0xFF9333EA)
            ).forEach { color ->
                Box(
                    modifier = Modifier
                        .size(12.dp)
                        .clip(CircleShape)
                        .background(color)
                        .border(
                            width = 1.dp,
                            color = Color.White.copy(alpha = 0.25f),
                            shape = CircleShape
                        )
                )
            }
        }
    }
}

@Composable
private fun HomeMenu(
    hasCurrentGame: Boolean,
    onContinueGameClicked: () -> Unit,
    onNewGameClicked: () -> Unit,
    onRulesClicked: () -> Unit,
    onSavedGamesClicked: () -> Unit,
    onLanguageClicked: () -> Unit
) {
    Column(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 430.dp),
        verticalArrangement = Arrangement.spacedBy(13.dp)
    ) {
        if (hasCurrentGame) {
            Button(
                onClick = onContinueGameClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(60.dp),
                shape = RoundedCornerShape(20.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = MastermindAqua,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.menu_continue_game),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }

        Button(
            onClick = onNewGameClicked,
            modifier = Modifier
                .fillMaxWidth()
                .height(60.dp),
            shape = RoundedCornerShape(18.dp),
            colors = ButtonDefaults.buttonColors(
                containerColor = BrandBlue,
                contentColor = Color.White
            )
        ) {
            Text(
                text = stringResource(R.string.menu_new_game),
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                )
            )
        }

        Spacer(modifier = Modifier.height(4.dp))

        HomeSecondaryAction(
            text = stringResource(R.string.menu_history),
            onClick = onSavedGamesClicked
        )

        HomeSecondaryAction(
            text = stringResource(R.string.rules),
            onClick = onRulesClicked
        )

        HomeSecondaryAction(
            text = stringResource(R.string.menu_language),
            onClick = onLanguageClicked
        )
    }
}

@Composable
private fun HomeSecondaryAction(
    text: String,
    onClick: () -> Unit
) {
    Surface(
        modifier = Modifier
            .fillMaxWidth()
            .height(56.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(18.dp),
        color = HomeSurfaceStrong,
        border = BorderStroke(
            width = 1.dp,
            color = HomeOutline
        )
    ) {
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(horizontal = 20.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(
                modifier = Modifier
                    .size(8.dp)
                    .clip(CircleShape)
                    .background(MastermindAqua)
            )

            Spacer(modifier = Modifier.width(14.dp))

            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.Medium
                ),
                color = HomeTextPrimary
            )

            Spacer(modifier = Modifier.weight(1f))

            Text(
                text = "›",
                color = BrandBlueLight,
                fontSize = 28.sp,
                fontWeight = FontWeight.Light
            )
        }
    }
}

@Composable
private fun SettingsPanel(
    numColors: Int,
    codeLength: Int,
    allowDuplicates: Boolean,
    colorOptions: List<Int>,
    lengthOptions: List<Int>,
    onBack: () -> Unit,
    onNumColorsSelected: (Int) -> Unit,
    onCodeLengthSelected: (Int) -> Unit,
    onDuplicatesSelected: (Boolean) -> Unit,
    onPlayClicked: () -> Unit
) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .widthIn(max = 460.dp),
        shape = RoundedCornerShape(26.dp),
        colors = CardDefaults.cardColors(
            containerColor = HomeSurface
        ),
        border = BorderStroke(
            width = 1.dp,
            color = MastermindAquaSoft
        )
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(22.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Row(
                modifier = Modifier.fillMaxWidth(),
                verticalAlignment = Alignment.CenterVertically
            ) {
                Box(
                    modifier = Modifier
                        .size(44.dp)
                        .clip(RoundedCornerShape(14.dp))
                        .background(HomeSurfaceStrong)
                        .border(
                            1.dp,
                            HomeOutline,
                            RoundedCornerShape(14.dp)
                        )
                        .clickable(onClick = onBack),
                    contentAlignment = Alignment.Center
                ) {
                    Icon(
                        imageVector = Icons.AutoMirrored.Filled.ArrowBack,
                        contentDescription = stringResource(R.string.back),
                        tint = HomeTextPrimary,
                        modifier = Modifier.size(21.dp)
                    )
                }

                Spacer(modifier = Modifier.width(14.dp))

                Text(
                    text = stringResource(R.string.menu_new_game),
                    style = MaterialTheme.typography.headlineSmall.copy(
                        fontWeight = FontWeight.SemiBold
                    ),
                    color = HomeTextPrimary
                )
            }

            Spacer(modifier = Modifier.height(28.dp))

            SettingSelector(
                label = stringResource(R.string.label_num_colors),
                options = colorOptions,
                selected = numColors,
                onOptionSelected = onNumColorsSelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            SettingSelector(
                label = stringResource(R.string.label_code_length),
                options = lengthOptions,
                selected = codeLength,
                onOptionSelected = onCodeLengthSelected
            )

            Spacer(modifier = Modifier.height(24.dp))

            BooleanSettingSelector(
                label = stringResource(R.string.label_duplicates),
                selected = allowDuplicates,
                onOptionSelected = onDuplicatesSelected,
                trueText = stringResource(R.string.yes),
                falseText = stringResource(R.string.no)
            )

            Spacer(modifier = Modifier.height(30.dp))

            Button(
                onClick = onPlayClicked,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(56.dp),
                shape = RoundedCornerShape(17.dp),
                colors = ButtonDefaults.buttonColors(
                    containerColor = BrandBlue,
                    contentColor = Color.White
                )
            ) {
                Text(
                    text = stringResource(R.string.menu_play),
                    style = MaterialTheme.typography.titleMedium.copy(
                        fontWeight = FontWeight.SemiBold
                    )
                )
            }
        }
    }
}

@Composable
private fun LanguageDialog(
    onItalian: () -> Unit,
    onEnglish: () -> Unit,
    onDismiss: () -> Unit
) {
    Card(
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = HomeSurfaceStrong
        ),
        border = BorderStroke(1.dp, HomeOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp),
            horizontalAlignment = Alignment.CenterHorizontally
        ) {
            Text(
                text = stringResource(R.string.menu_language),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = HomeTextPrimary
            )

            Spacer(modifier = Modifier.height(20.dp))

            Button(
                onClick = onItalian,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(stringResource(R.string.language_it))
            }

            Spacer(modifier = Modifier.height(10.dp))

            OutlinedButton(
                onClick = onEnglish,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp),
                colors = ButtonDefaults.outlinedButtonColors(
                    contentColor = HomeTextPrimary
                ),
                border = BorderStroke(1.dp, HomeOutline)
            ) {
                Text(stringResource(R.string.language_en))
            }

            Spacer(modifier = Modifier.height(8.dp))

            TextButton(
                onClick = onDismiss,
                colors = ButtonDefaults.textButtonColors(
                    contentColor = HomeTextSecondary
                )
            ) {
                Text(stringResource(R.string.cancel))
            }
        }
    }
}

@Composable
fun RulesDialog(onDismiss: () -> Unit) {
    Card(
        modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 620.dp),
        shape = RoundedCornerShape(24.dp),
        colors = CardDefaults.cardColors(
            containerColor = HomeSurfaceStrong
        ),
        border = BorderStroke(1.dp, HomeOutline)
    ) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(24.dp)
        ) {
            Text(
                text = stringResource(R.string.menu_rules),
                style = MaterialTheme.typography.headlineSmall.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = HomeTextPrimary,
                textAlign = TextAlign.Center,
                modifier = Modifier.fillMaxWidth()
            )

            Spacer(modifier = Modifier.height(16.dp))

            Column(
                modifier = Modifier
                    .weight(1f, fill = false)
                    .verticalScroll(rememberScrollState())
            ) {
                val rules = listOf(
                    stringResource(R.string.rule_1),
                    stringResource(R.string.rule_2),
                    stringResource(R.string.rule_3),
                    stringResource(R.string.rule_4),
                    stringResource(R.string.rule_5),
                    stringResource(R.string.rule_6)
                )

                rules.forEachIndexed { index, rule ->
                    Row(
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(vertical = 8.dp),
                        verticalAlignment = Alignment.Top
                    ) {
                        Surface(
                            shape = CircleShape,
                            color = BrandBlue.copy(alpha = 0.22f),
                            border = BorderStroke(
                                1.dp,
                                BrandBlueLight.copy(alpha = 0.35f)
                            )
                        ) {
                            Box(
                                modifier = Modifier.size(28.dp),
                                contentAlignment = Alignment.Center
                            ) {
                                Text(
                                    text = "${index + 1}",
                                    style = MaterialTheme.typography.labelMedium,
                                    color = AccentCyan,
                                    fontWeight = FontWeight.SemiBold
                                )
                            }
                        }

                        Spacer(modifier = Modifier.width(12.dp))

                        Text(
                            text = rule,
                            style = MaterialTheme.typography.bodyMedium,
                            color = HomeTextSecondary,
                            modifier = Modifier.weight(1f)
                        )
                    }
                }
            }

            Spacer(modifier = Modifier.height(18.dp))

            Button(
                onClick = onDismiss,
                modifier = Modifier
                    .fillMaxWidth()
                    .height(50.dp),
                shape = RoundedCornerShape(16.dp)
            ) {
                Text(
                    text = stringResource(R.string.close_rules),
                    fontWeight = FontWeight.SemiBold
                )
            }
        }
    }
}

@Composable
fun <T> SettingSelector(
    label: String,
    options: List<T>,
    selected: T,
    onOptionSelected: (T) -> Unit
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = HomeTextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            options.forEach { option ->
                val isSelected = option == selected

                Surface(
                    modifier = Modifier
                        .weight(1f)
                        .height(48.dp)
                        .clickable {
                            onOptionSelected(option)
                        },
                    shape = RoundedCornerShape(14.dp),
                    color = if (isSelected) {
                        BrandBlue
                    } else {
                        HomeSurfaceStrong
                    },
                    border = BorderStroke(
                        width = 1.dp,
                        color = if (isSelected) {
                            BrandBlueLight.copy(alpha = 0.65f)
                        } else {
                            HomeOutline
                        }
                    )
                ) {
                    Box(
                        contentAlignment = Alignment.Center
                    ) {
                        Text(
                            text = option.toString(),
                            style = MaterialTheme.typography.titleMedium.copy(
                                fontWeight = FontWeight.SemiBold
                            ),
                            color = if (isSelected) {
                                Color.White
                            } else {
                                HomeTextSecondary
                            }
                        )
                    }
                }
            }
        }
    }
}

@Composable
fun BooleanSettingSelector(
    label: String,
    selected: Boolean,
    onOptionSelected: (Boolean) -> Unit,
    trueText: String,
    falseText: String
) {
    Column(
        modifier = Modifier.fillMaxWidth(),
        horizontalAlignment = Alignment.Start
    ) {
        Text(
            text = label,
            style = MaterialTheme.typography.titleMedium.copy(
                fontWeight = FontWeight.Medium
            ),
            color = HomeTextPrimary
        )

        Spacer(modifier = Modifier.height(10.dp))

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(9.dp)
        ) {
            BooleanOption(
                text = trueText,
                selected = selected,
                onClick = { onOptionSelected(true) },
                modifier = Modifier.weight(1f)
            )

            BooleanOption(
                text = falseText,
                selected = !selected,
                onClick = { onOptionSelected(false) },
                modifier = Modifier.weight(1f)
            )
        }
    }
}

@Composable
private fun BooleanOption(
    text: String,
    selected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    Surface(
        modifier = modifier
            .height(48.dp)
            .clickable(onClick = onClick),
        shape = RoundedCornerShape(14.dp),
        color = if (selected) {
            BrandBlue
        } else {
            HomeSurfaceStrong
        },
        border = BorderStroke(
            width = 1.dp,
            color = if (selected) {
                BrandBlueLight.copy(alpha = 0.65f)
            } else {
                HomeOutline
            }
        )
    ) {
        Box(
            contentAlignment = Alignment.Center
        ) {
            Text(
                text = text,
                style = MaterialTheme.typography.titleMedium.copy(
                    fontWeight = FontWeight.SemiBold
                ),
                color = if (selected) {
                    Color.White
                } else {
                    HomeTextSecondary
                }
            )
        }
    }
}

fun setAppLocale(context: Context, lang: String) {
    persistLanguage(context, lang)

    val locale = Locale(lang)
    Locale.setDefault(locale)

    val resources = context.resources
    val config = Configuration(resources.configuration)
    config.setLocale(locale)
    context.createConfigurationContext(config)

    val activity = context as? Activity ?: return
    activity.finish()
    activity.startActivity(activity.intent)
}

@Preview(showBackground = true)
@Composable
fun HomeScreenPreview() {
    val context = LocalContext.current
    val repository = remember {
        GameRepository(context.applicationContext)
    }
    val fakeViewModel = remember {
        HomeViewModel(repository)
    }

    MastermindTheme(dynamicColor = false) {
        HomeScreen(
            viewModel = fakeViewModel,
            onPlayClicked = { _, _, _ -> },
            onSavedGamesClicked = {}
        )
    }
}
