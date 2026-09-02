package com.snapmechanic.app.ui

import android.content.Intent
import android.net.Uri
import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import com.snapmechanic.app.R
import com.snapmechanic.app.databinding.ActivityHelpSupportBinding

class HelpSupportActivity : AppCompatActivity() {
    private lateinit var binding: ActivityHelpSupportBinding

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        binding = ActivityHelpSupportBinding.inflate(layoutInflater)
        setContentView(binding.root)

        binding.btnBack.setOnClickListener { finish() }
        binding.btnContactEmail.setOnClickListener {
            val email = getString(R.string.developer_email)
            val intent = Intent(Intent.ACTION_SENDTO, Uri.parse("mailto:$email")).apply {
                putExtra(Intent.EXTRA_SUBJECT, "SnapMechanic Support Request")
            }
            startActivity(Intent.createChooser(intent, "Send email via..."))
        }
    }
}
