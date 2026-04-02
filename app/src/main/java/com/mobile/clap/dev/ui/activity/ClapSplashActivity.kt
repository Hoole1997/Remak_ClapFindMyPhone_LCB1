package com.mobile.clap.dev.ui.activity

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import android.widget.TextView
import androidx.activity.OnBackPressedCallback
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.browser.customtabs.CustomTabsIntent
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.mobile.clap.dev.ClapApp
import com.mobile.clap.dev.R
import com.remax.base.ext.KvBoolDelegate

class ClapSplashActivity : AppCompatActivity() {

    private var splashShown by KvBoolDelegate("splash_shown", false)

    private companion object {
        const val AGREEMENT_URL = "https://daisytalestudios.com/privacy.html"
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        if (splashShown) {
            startActivity(Intent(this, MainActivity::class.java))
            finish()
            return
        }

        enableEdgeToEdge()
        setContentView(R.layout.activity_clap_splash)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.rootLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        initViews()
    }

    private fun initViews() {
        val btnGetStarted = findViewById<TextView>(R.id.btnGetStarted)
        val tvPrivacyPolicy = findViewById<TextView>(R.id.tvPrivacyPolicy)
        val tvTermsOfService = findViewById<TextView>(R.id.tvTermsOfService)

        btnGetStarted.setOnClickListener {
            splashShown = true
            startActivity(Intent(this, MainActivity::class.java))
            finish()
        }

        tvPrivacyPolicy.setOnClickListener {
            openAgreementPage()
        }

        tvTermsOfService.setOnClickListener {
            openAgreementPage()
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                ClapApp.clapApp?.ultracorebattery()
            }
        })
    }

    private fun openAgreementPage() {
        val agreementUri = Uri.parse(AGREEMENT_URL)
        val customTabsIntent = CustomTabsIntent.Builder().build()

        runCatching {
            customTabsIntent.launchUrl(this, agreementUri)
        }.onFailure {
            startActivity(Intent(Intent.ACTION_VIEW, agreementUri))
        }
    }
}
