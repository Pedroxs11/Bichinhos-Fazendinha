package com.minhafazendinha.game

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var soundPlayer: AnimalSoundPlayer

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundPlayer = AnimalSoundPlayer(this)

        val root = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER
            setPadding(32, 48, 32, 48)
            setBackgroundColor(0xFFEAF7D5.toInt())
        }
        root.addView(TextView(this).apply {
            text = "Minha Fazendinha"
            textSize = 30f
            gravity = Gravity.CENTER
        })
        root.addView(TextView(this).apply {
            text = "Toque em um bichinho para ouvir o som"
            textSize = 17f
            gravity = Gravity.CENTER
            setPadding(0, 16, 0, 28)
        })

        listOf(
            "🐮 Vaca" to "vaca", "🐔 Galinha" to "galinha",
            "🐶 Cachorro" to "cachorro", "🐴 Cavalo" to "cavalo",
            "🐷 Porco" to "porco", "🐑 Ovelha" to "ovelha",
            "🐐 Cabra" to "cabra", "🫏 Burro" to "burro"
        ).forEach { (label, animal) ->
            root.addView(Button(this).apply {
                text = label
                textSize = 20f
                isAllCaps = false
                setOnClickListener {
                    soundPlayer.play(animal)
                    bounce(it)
                }
            }, LinearLayout.LayoutParams(
                LinearLayout.LayoutParams.MATCH_PARENT,
                LinearLayout.LayoutParams.WRAP_CONTENT
            ).apply { setMargins(0, 7, 0, 7) })
        }
        setContentView(root)
    }

    private fun bounce(view: View) {
        ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 1.08f, 1f).apply {
            duration = 220
            start()
        }
        ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.04f, 1f).apply {
            duration = 220
            start()
        }
    }

    override fun onDestroy() {
        soundPlayer.release()
        super.onDestroy()
    }
}
