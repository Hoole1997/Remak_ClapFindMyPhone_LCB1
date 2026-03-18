package com.mobile.clap.dev.ui.activity

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mobile.clap.dev.R
import com.mobile.clap.dev.ui.dialog.RateDialog

class DebugActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_debug)

        findViewById<TextView>(R.id.btnShowRateDialog).setOnClickListener {
            RateDialog.newInstance()
                .setOnSubmitListener { rating ->
                    android.widget.Toast.makeText(this, "Rating: $rating", android.widget.Toast.LENGTH_SHORT).show()
                }
                .setOnLaterListener {
                    // dismissed
                }
                .show(supportFragmentManager, RateDialog.TAG)
        }
    }
}
