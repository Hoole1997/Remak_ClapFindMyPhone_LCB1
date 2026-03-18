package com.mobile.clap.dev.ui.activity

import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.mobile.clap.dev.R
import com.mobile.clap.dev.ui.dialog.MicPermissionDialog
import com.mobile.clap.dev.ui.dialog.RateDialog
import com.mobile.clap.dev.ui.dialog.TestMicDialog

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

        findViewById<TextView>(R.id.btnShowMicPermissionDialog).setOnClickListener {
            MicPermissionDialog.newInstance()
                .setOnGotItListener {
                    android.widget.Toast.makeText(this, "Got it clicked", android.widget.Toast.LENGTH_SHORT).show()
                }
                .setOnCancelListener {
                    // dismissed
                }
                .show(supportFragmentManager, MicPermissionDialog.TAG)
        }

        findViewById<TextView>(R.id.btnShowTestMicDialog).setOnClickListener {
            TestMicDialog.newInstance()
                .setOnLaterListener {
                    android.widget.Toast.makeText(this, "Test later clicked", android.widget.Toast.LENGTH_SHORT).show()
                }
                .setOnCancelListener {
                    // dismissed
                }
                .show(supportFragmentManager, TestMicDialog.TAG)
        }
    }
}
