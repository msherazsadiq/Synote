package com.sherazsadiq.synote.presentation.activity

import android.content.Intent
import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.splashscreen.SplashScreen.Companion.installSplashScreen
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.lifecycle.lifecycleScope
import com.sherazsadiq.synote.R
import com.sherazsadiq.synote.databinding.ActivitySplashScreenBinding
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch


class SplashScreen : AppCompatActivity() {
    private var keepSplashOnScreen = true
    private lateinit var binding: ActivitySplashScreenBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        val splashScreen = installSplashScreen()

        // Keep the splash screen visible while the condition is true
        splashScreen.setKeepOnScreenCondition { keepSplashOnScreen }

        super.onCreate(savedInstanceState)
        enableEdgeToEdge()

        binding = ActivitySplashScreenBinding.inflate(layoutInflater)
        setContentView(binding.root)

        setContentView(R.layout.activity_splash_screen)
        ViewCompat.setOnApplyWindowInsetsListener(binding.main) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val sharedPref = getSharedPreferences("SynotePrefs", MODE_PRIVATE)
        val isLoggedIn = sharedPref.getBoolean("isLoggedIn", false)



        // Delay using coroutine instead of Handler (cleaner)
        lifecycleScope.launch {
            delay(2000) // Wait 2 seconds
            keepSplashOnScreen = false // This will now allow the splash to exit

            if (isLoggedIn) {
                startActivity(Intent(this@SplashScreen, HomeActivity::class.java))
                finish()
            }
            else {

                startActivity(Intent(this@SplashScreen, LoginActivity::class.java))
                finish()
            }
        }
    }
}