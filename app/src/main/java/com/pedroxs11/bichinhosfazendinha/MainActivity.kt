package com.pedroxs11.bichinhosfazendinha

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import com.pedroxs11.bichinhosfazendinha.ui.ProgressionWardrobeApp
import com.pedroxs11.bichinhosfazendinha.ui.initializeAnimalAudio
import com.pedroxs11.bichinhosfazendinha.ui.stopAnimalAudio

class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        initializeAnimalAudio(applicationContext)
        setContent { ProgressionWardrobeApp() }
    }

    override fun onDestroy() {
        stopAnimalAudio()
        super.onDestroy()
    }
}
