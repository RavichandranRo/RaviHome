package com.example.ravihome

import android.app.Activity
import android.content.Intent
import android.os.Bundle
import android.speech.RecognizerIntent
import androidx.activity.result.contract.ActivityResultContracts
import androidx.appcompat.app.AppCompatActivity
import androidx.biometric.BiometricManager
import androidx.biometric.BiometricPrompt
import androidx.core.content.ContextCompat
import androidx.core.view.WindowCompat
import androidx.navigation.fragment.NavHostFragment
import com.example.ravihome.ui.util.PopupUtils
import com.google.android.material.appbar.MaterialToolbar
import dagger.hilt.android.AndroidEntryPoint
import java.util.Locale
import java.time.LocalDate

@AndroidEntryPoint
class MainActivity : AppCompatActivity() {
    private val voiceLauncher = registerForActivityResult(
        ActivityResultContracts.StartActivityForResult()
    ) { result ->
        if (result.resultCode == Activity.RESULT_OK) {
            val spokenText = result.data
                ?.getStringArrayListExtra(RecognizerIntent.EXTRA_RESULTS)
                ?.firstOrNull()
                .orEmpty()
            if (spokenText.isNotBlank()) {
                PopupUtils.showAutoDismiss(
                    this,
                    "Voice captured",
                    "Detected: $spokenText"
                )
            }
        }
    }
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        WindowCompat.setDecorFitsSystemWindows(window, false)

        // 🔑 REQUIRED — attach window
        setContentView(R.layout.activity_main)

        val executor = ContextCompat.getMainExecutor(this)
        val biometricManager = BiometricManager.from(this)

        when (biometricManager.canAuthenticate(
            BiometricManager.Authenticators.BIOMETRIC_STRONG
                    or BiometricManager.Authenticators.DEVICE_CREDENTIAL
        )) {
            BiometricManager.BIOMETRIC_SUCCESS -> Unit

            else -> {
                // No biometric → open app normally
                setupNavigation()
                PopupUtils.showAutoDismiss(this, "Welcome!", "Your home dashboard is ready.")
                return
            }
        }

        val biometricPrompt = BiometricPrompt(
            this,
            executor,
            object : BiometricPrompt.AuthenticationCallback() {

                override fun onAuthenticationSucceeded(result: BiometricPrompt.AuthenticationResult) {
                    setupNavigation()
                    PopupUtils.showAutoDismiss(
                        this@MainActivity,
                        "Welcome back!",
                        "Your home dashboard is ready."
                    )
                }

                override fun onAuthenticationFailed() = finish()

                override fun onAuthenticationError(errorCode: Int, errString: CharSequence) = finish()
            }
        )

        val promptInfo = BiometricPrompt.PromptInfo.Builder()
            .setTitle("App Locked")
            .setSubtitle("Authenticate to continue")
            .setAllowedAuthenticators(
                BiometricManager.Authenticators.BIOMETRIC_STRONG
                        or BiometricManager.Authenticators.DEVICE_CREDENTIAL
            )
            .build()

        biometricPrompt.authenticate(promptInfo)
    }

    private fun setupNavigation() {
        val navHost = supportFragmentManager
            .findFragmentById(R.id.nav_host) as NavHostFragment

        val navController = navHost.navController

        val toolbar = findViewById<MaterialToolbar>(R.id.topAppBar)

        navController.addOnDestinationChangedListener { _, destination, args ->
            val inWelcome = destination.id == R.id.welcomeFragment
            toolbar.title = destination.label ?: "Ravi Home"
            toolbar.navigationIcon = if (inWelcome) null else ContextCompat.getDrawable(this, androidx.appcompat.R.drawable.abc_ic_ab_back_material)
        }

        toolbar.setNavigationOnClickListener {
            if (!navController.popBackStack()) finish()
        }
        toolbar.setOnMenuItemClickListener { item ->
            when (item.itemId) {
                R.id.action_search_info -> {
                    val destination = navController.currentDestination
                    val args = navController.currentBackStackEntry?.arguments
                    val argString = args?.keySet()?.joinToString { key -> "$key=${args.get(key)}" }
                        ?.ifBlank { "No parameters" } ?: "No parameters"
                    PopupUtils.showAutoDismiss(
                        this,
                        "Search parameters",
                        "View: ${destination?.displayName ?: "unknown"}\n$argString\nDate: ${LocalDate.now()}"
                    )
                    true
                }

                R.id.action_voice -> {
                    val intent = Intent(RecognizerIntent.ACTION_RECOGNIZE_SPEECH).apply {
                        putExtra(
                            RecognizerIntent.EXTRA_LANGUAGE_MODEL,
                            RecognizerIntent.LANGUAGE_MODEL_FREE_FORM
                        )
                        putExtra(RecognizerIntent.EXTRA_LANGUAGE, Locale.getDefault())
                        putExtra(RecognizerIntent.EXTRA_PROMPT, "Speak your input")
                    }
                    voiceLauncher.launch(intent)
                    true
                }

                else -> false
            }
        }
    }
}
