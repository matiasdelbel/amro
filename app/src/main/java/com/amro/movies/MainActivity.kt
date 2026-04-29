package com.amro.movies

import android.content.Intent
import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.activity.enableEdgeToEdge
import com.amro.designsystem.theme.AmroTheme
import dagger.hilt.android.AndroidEntryPoint
import androidx.core.net.toUri

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        enableEdgeToEdge()
        setContent { AmroTheme { AmroNavHost(onOpenExternalUrl = ::openExternalUrl) } }
    }

    private fun openExternalUrl(url: String) {
        val intent = Intent(Intent.ACTION_VIEW, url.toUri())
        startActivity(intent)
    }
}
