package com.example

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.Surface
import androidx.compose.runtime.CompositionLocalProvider
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import com.example.data.settings.UserSettingsManager
import com.example.ui.VirtuosoApp
import com.example.ui.theme.LocalVirtuosoColors
import com.example.ui.theme.MyApplicationTheme
import com.example.util.configureAppCheck
import com.example.util.localization.LocalVirtuosoStrings
import com.example.util.localization.getStrings

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        // Mandatory Firebase App Check initialization before setContent or ViewModels
        configureAppCheck(this, intent)

        enableEdgeToEdge()

        val settingsManager = UserSettingsManager.getInstance(this)

        setContent {
            val userSettings by settingsManager.settings.collectAsState()
            val strings = getStrings(userSettings.language)

            MyApplicationTheme(appTheme = userSettings.theme) {
                CompositionLocalProvider(LocalVirtuosoStrings provides strings) {
                    val colors = LocalVirtuosoColors.current
                    Surface(
                        modifier = Modifier.fillMaxSize(),
                        color = colors.background
                    ) {
                        VirtuosoApp()
                    }
                }
            }
        }
    }
}
