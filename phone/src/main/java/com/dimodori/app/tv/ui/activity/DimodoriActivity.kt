package com.dimodori.app.tv.ui.activity

import android.content.Context
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Surface
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalView
import androidx.core.view.WindowCompat
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.hilt.lifecycle.viewmodel.compose.hiltViewModel
import androidx.lifecycle.lifecycleScope
import androidx.media3.common.util.UnstableApi
import com.dimodori.app.locale.AppLanguageManager
import com.jellycine.shared.ui.theme.DimodoriTheme
import com.dimodori.app.tv.ui.navigation.AppNavigation
import com.dimodori.app.tv.ui.splash.SplashScreen
import com.dimodori.app.tv.ui.splash.SplashViewModel
import com.jellycine.auth.AuthStateManager
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.launch
import java.util.concurrent.atomic.AtomicBoolean

@UnstableApi
@AndroidEntryPoint
class DimodoriTvActivity : ComponentActivity() {

    override fun attachBaseContext(newBase: Context) {
        super.attachBaseContext(AppLanguageManager.wrapContext(newBase))
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        super.onCreate(savedInstanceState)
        AppLanguageManager.applySavedLanguage(this)

        val authStateManager = AuthStateManager.getInstance(this)
        val authCheckCompleted = AtomicBoolean(false)
        val firstComposeCommitted = AtomicBoolean(false)
        splashScreen.setKeepOnScreenCondition {
            !authCheckCompleted.get() ||
                !firstComposeCommitted.get()
        }

        lifecycleScope.launch {
            authStateManager.checkAuthenticationState()
            authCheckCompleted.set(true)
        }

        // Use modern edge-to-edge approach
        enableEdgeToEdge()

        setContent {
            DimodoriTheme {
                LaunchedEffect(Unit) {
                    withFrameNanos { }
                    firstComposeCommitted.set(true)
                }

                // Handle system bar colors for edge-to-edge
                val view = LocalView.current
                SideEffect {
                    val window = (view.context as ComponentActivity).window
                    WindowCompat.getInsetsController(window, view).isAppearanceLightStatusBars = false
                    WindowCompat.getInsetsController(window, view).isAppearanceLightNavigationBars = false
                }

                Surface(
                    modifier = Modifier.fillMaxSize(),
                    color = MaterialTheme.colorScheme.background
                ) {
                    val splashViewModel: SplashViewModel = hiltViewModel()
                    val shouldShowSplash by splashViewModel.shouldShowSplash.collectAsState()

                    Box(modifier = Modifier.fillMaxSize()) {
                        AppNavigation()
                        if (shouldShowSplash) {
                            SplashScreen(
                                onSplashComplete = {
                                    splashViewModel.onSplashComplete()
                                }
                            )
                        } 
                    }
                }
            }
        }
    }

}
