package com.example.ui

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.AutoAwesome
import androidx.compose.material.icons.filled.FitnessCenter
import androidx.compose.material.icons.filled.LibraryMusic
import androidx.compose.material.icons.filled.Settings
import androidx.compose.material.icons.filled.Timer
import androidx.compose.material.icons.outlined.AutoAwesome
import androidx.compose.material.icons.outlined.FitnessCenter
import androidx.compose.material.icons.outlined.LibraryMusic
import androidx.compose.material.icons.outlined.Settings
import androidx.compose.material.icons.outlined.Timer
import androidx.compose.material3.Icon
import androidx.compose.material3.NavigationBar
import androidx.compose.material3.NavigationBarItem
import androidx.compose.material3.NavigationBarItemDefaults
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.vector.ImageVector
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import androidx.lifecycle.viewmodel.compose.viewModel
import com.example.ui.guide.RepertoireGuideScreen
import com.example.ui.guide.RepertoireGuideViewModel
import com.example.ui.library.RepertoireAndLogScreen
import com.example.ui.library.RepertoireAndLogViewModel
import com.example.ui.settings.SettingsScreen
import com.example.ui.settings.SettingsViewModel
import com.example.ui.studio.PracticeStudioScreen
import com.example.ui.studio.PracticeStudioViewModel
import com.example.ui.theme.LocalVirtuosoColors
import com.example.ui.trainer.TrainersHubScreen
import com.example.ui.trainer.TrainersViewModel
import com.example.util.localization.LocalVirtuosoStrings
import com.example.util.localization.VirtuosoStrings

enum class VirtuosoTab(
    val getTitle: (VirtuosoStrings) -> String,
    val selectedIcon: ImageVector,
    val unselectedIcon: ImageVector,
    val testTag: String
) {
    STUDIO(
        getTitle = { it.tabStudio },
        selectedIcon = Icons.Filled.Timer,
        unselectedIcon = Icons.Outlined.Timer,
        testTag = "nav_practice_studio"
    ),
    TRAINERS(
        getTitle = { it.tabTrainers },
        selectedIcon = Icons.Filled.FitnessCenter,
        unselectedIcon = Icons.Outlined.FitnessCenter,
        testTag = "nav_trainers_hub"
    ),
    AI_GUIDE(
        getTitle = { it.tabGuide },
        selectedIcon = Icons.Filled.AutoAwesome,
        unselectedIcon = Icons.Outlined.AutoAwesome,
        testTag = "nav_ai_guide"
    ),
    LIBRARY(
        getTitle = { it.tabLibrary },
        selectedIcon = Icons.Filled.LibraryMusic,
        unselectedIcon = Icons.Outlined.LibraryMusic,
        testTag = "nav_repertoire_library"
    ),
    SETTINGS(
        getTitle = { it.tabSettings },
        selectedIcon = Icons.Filled.Settings,
        unselectedIcon = Icons.Outlined.Settings,
        testTag = "nav_settings"
    )
}

@Composable
fun VirtuosoApp(
    studioViewModel: PracticeStudioViewModel = viewModel(),
    trainersViewModel: TrainersViewModel = viewModel(),
    guideViewModel: RepertoireGuideViewModel = viewModel(),
    libraryViewModel: RepertoireAndLogViewModel = viewModel(),
    settingsViewModel: SettingsViewModel = viewModel()
) {
    val colors = LocalVirtuosoColors.current
    val strings = LocalVirtuosoStrings.current
    var currentTabIndex by rememberSaveable { mutableIntStateOf(0) }

    // Custom back navigation handling
    BackHandler(enabled = currentTabIndex != 0) {
        currentTabIndex = 0
    }

    Scaffold(
        modifier = Modifier.fillMaxSize(),
        containerColor = colors.background,
        bottomBar = {
            NavigationBar(
                containerColor = colors.surface,
                tonalElevation = 8.dp,
                modifier = Modifier
                    .navigationBarsPadding()
                    .testTag("virtuoso_bottom_nav")
            ) {
                VirtuosoTab.entries.forEachIndexed { index, tab ->
                    val isSelected = currentTabIndex == index
                    val tabTitle = tab.getTitle(strings)
                    NavigationBarItem(
                        selected = isSelected,
                        onClick = { currentTabIndex = index },
                        icon = {
                            Icon(
                                imageVector = if (isSelected) tab.selectedIcon else tab.unselectedIcon,
                                contentDescription = tabTitle
                            )
                        },
                        label = {
                            Text(
                                text = tabTitle,
                                fontWeight = if (isSelected) FontWeight.Bold else FontWeight.Medium,
                                fontSize = 10.sp,
                                maxLines = 1
                            )
                        },
                        colors = NavigationBarItemDefaults.colors(
                            selectedIconColor = colors.background,
                            selectedTextColor = colors.primary,
                            indicatorColor = colors.primary,
                            unselectedIconColor = colors.textMuted,
                            unselectedTextColor = colors.textSecondary
                        ),
                        modifier = Modifier.testTag(tab.testTag)
                    )
                }
            }
        }
    ) { innerPadding ->
        when (currentTabIndex) {
            0 -> PracticeStudioScreen(
                viewModel = studioViewModel,
                modifier = Modifier.padding(innerPadding)
            )
            1 -> TrainersHubScreen(
                viewModel = trainersViewModel,
                modifier = Modifier.padding(innerPadding)
            )
            2 -> RepertoireGuideScreen(
                viewModel = guideViewModel,
                modifier = Modifier.padding(innerPadding)
            )
            3 -> RepertoireAndLogScreen(
                viewModel = libraryViewModel,
                onNavigateToGuideAnalysis = { analysis ->
                    guideViewModel.displayAnalysis(analysis)
                    currentTabIndex = 2 // Switch to AI Guide tab
                },
                modifier = Modifier.padding(innerPadding)
            )
            4 -> SettingsScreen(
                viewModel = settingsViewModel,
                modifier = Modifier.padding(innerPadding)
            )
        }
    }
}
