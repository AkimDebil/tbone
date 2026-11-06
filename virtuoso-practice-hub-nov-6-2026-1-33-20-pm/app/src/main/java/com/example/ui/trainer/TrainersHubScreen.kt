package com.example.ui.trainer

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Speed
import androidx.compose.material.icons.filled.Tune
import androidx.compose.material3.Icon
import androidx.compose.material3.Tab
import androidx.compose.material3.TabRow
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.testTag
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.ui.theme.LocalVirtuosoColors
import com.example.util.localization.LocalVirtuosoStrings

@Composable
fun TrainersHubScreen(
    viewModel: TrainersViewModel,
    modifier: Modifier = Modifier
) {
    val colors = LocalVirtuosoColors.current
    val strings = LocalVirtuosoStrings.current
    var selectedTab by rememberSaveable { mutableIntStateOf(0) } // 0: Ритм-тренер, 1: Тренировка тона

    Box(
        modifier = modifier
            .fillMaxSize()
            .background(colors.background)
    ) {
        Column(modifier = Modifier.fillMaxSize()) {
            Spacer(modifier = Modifier.height(8.dp))

            // Sub-tabs: Ритм-тренер vs Тренировка тона
            TabRow(
                selectedTabIndex = selectedTab,
                containerColor = colors.background,
                contentColor = colors.primary,
                modifier = Modifier
                    .padding(horizontal = 16.dp)
                    .testTag("trainers_sub_tabs")
            ) {
                Tab(
                    selected = selectedTab == 0,
                    onClick = { selectedTab = 0 },
                    text = {
                        Text(
                            text = strings.rhythmTrainerTitle,
                            fontWeight = if (selectedTab == 0) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    },
                    icon = {
                        Icon(Icons.Default.Speed, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    modifier = Modifier.testTag("tab_rhythm_trainer")
                )

                Tab(
                    selected = selectedTab == 1,
                    onClick = { selectedTab = 1 },
                    text = {
                        Text(
                            text = strings.toneTrainerTitle,
                            fontWeight = if (selectedTab == 1) FontWeight.Bold else FontWeight.Normal,
                            fontSize = 14.sp
                        )
                    },
                    icon = {
                        Icon(Icons.Default.Tune, contentDescription = null, modifier = Modifier.size(20.dp))
                    },
                    modifier = Modifier.testTag("tab_tone_trainer")
                )
            }

            Spacer(modifier = Modifier.height(10.dp))

            when (selectedTab) {
                0 -> {
                    RhythmTrainerView(
                        engine = viewModel.rhythmEngine,
                        modifier = Modifier.fillMaxSize()
                    )
                }

                1 -> {
                    ToneTrainerView(
                        droneSynthesizer = viewModel.droneSynthesizer,
                        pitchDetector = viewModel.pitchDetector,
                        earTrainer = viewModel.earTrainer,
                        modifier = Modifier.fillMaxSize()
                    )
                }
            }
        }
    }
}
