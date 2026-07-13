package com.teleport.messenger

import android.graphics.Color
import android.os.Bundle
import androidx.activity.SystemBarStyle
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.WindowCompat
import androidx.fragment.app.FragmentActivity
import com.teleport.messenger.ui.TeleportApp

class MainActivity : FragmentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        installSplashScreen()
        super.onCreate(savedInstanceState)
        val darkBg = Color.parseColor("#050B18")
        enableEdgeToEdge(
            statusBarStyle = SystemBarStyle.dark(darkBg),
            navigationBarStyle = SystemBarStyle.dark(darkBg),
        )
        WindowCompat.setDecorFitsSystemWindows(window, false)
        setContent { TeleportApp() }
    }
}
