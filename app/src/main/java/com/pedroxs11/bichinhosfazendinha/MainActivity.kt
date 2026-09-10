package com.pedroxs11.bichinhosfazendinha

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pedroxs11.bichinhosfazendinha.ui.ProgressionWardrobeApp

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent { ProgressionWardrobeApp() }
    }
}
