package com.minhafazendinha.game

import android.animation.ObjectAnimator
import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var soundPlayer: AnimalSoundPlayer
    private var selectedAnimal = "vaca"
    private var selectedEmoji = "🐮"
    private lateinit var preview: TextView
    private lateinit var status: TextView

    private val animals = listOf(
        Triple("🐮", "Vaca", "vaca"),
        Triple("🐔", "Galinha", "galinha"),
        Triple("🐶", "Cachorro", "cachorro"),
        Triple("🐴", "Cavalo", "cavalo"),
        Triple("🐷", "Porco", "porco"),
        Triple("🐑", "Ovelha", "ovelha"),
        Triple("🐐", "Cabra", "cabra"),
        Triple("🫏", "Burro", "burro")
    )

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        soundPlayer = AnimalSoundPlayer(this)

        val content = LinearLayout(this).apply {
            orientation = LinearLayout.VERTICAL
            gravity = Gravity.CENTER_HORIZONTAL
            setPadding(32, 42, 32, 42)
            setBackgroundColor(0xFFEAF7D5.toInt())
        }

        content.addView(TextView(this).apply {
            text = "Minha Fazendinha — Modo Teste"
            textSize = 27f
            gravity = Gravity.CENTER
        })
        content.addView(TextView(this).apply {
            text = "🔓 Todos os animais e roupas liberados"
            textSize = 16f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 18)
        })

        preview = TextView(this).apply {
            text = "🐮"
            textSize = 74f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 8)
        }
        content.addView(preview)

        status = TextView(this).apply {
            text = "Vaca selecionada"
            textSize = 17f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 12)
        }
        content.addView(status)

        content.addView(TextView(this).apply {
            text = "ANIMAIS — toque para selecionar e ouvir"
            textSize = 15f
            gravity = Gravity.CENTER
        })

        animals.forEach { (emoji, name, key) ->
            content.addView(Button(this).apply {
                text = "$emoji $name  🔊"
                textSize = 19f
                isAllCaps = false
                setOnClickListener {
                    selectedAnimal = key
                    selectedEmoji = emoji
                    preview.text = emoji
                    status.text = "$name selecionado — som acionado"
                    soundPlayer.play(key)
                    bounce(it)
                }
            }, fullWidthParams())
        }

        content.addView(TextView(this).apply {
            text = "ROUPAS — todas liberadas para teste"
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, 24, 0, 4)
        })

        listOf(
            "Sem roupa" to "",
            "👒 Chapéu" to " 👒",
            "🎀 Laço" to " 🎀",
            "🧢 Boné" to " 🧢",
            "👑 Coroa" to " 👑",
            "🕶️ Óculos" to " 🕶️",
            "🧣 Cachecol" to " 🧣",
            "🎩 Cartola" to " 🎩"
        ).forEach { (name, accessory) ->
            content.addView(Button(this).apply {
                text = "🔓 $name"
                isAllCaps = false
                setOnClickListener {
                    preview.text = selectedEmoji + accessory
                    status.text = "$name equipado em ${animalName(selectedAnimal)}"
                    bounce(preview)
                }
            }, fullWidthParams())
        }

        content.addView(Button(this).apply {
            text = "🔊 TESTAR SOM NOVAMENTE"
            textSize = 18f
            isAllCaps = false
            setOnClickListener {
                soundPlayer.play(selectedAnimal)
                bounce(preview)
            }
        }, fullWidthParams().apply { setMargins(0, 24, 0, 8) })

        setContentView(ScrollView(this).apply { addView(content) })
    }

    private fun fullWidthParams() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT,
        LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { setMargins(0, 6, 0, 6) }

    private fun animalName(key: String) = animals.firstOrNull { it.third == key }?.second ?: key

    private fun bounce(view: View) {
        ObjectAnimator.ofFloat(view, View.SCALE_Y, 1f, 1.08f, 1f).apply { duration = 220; start() }
        ObjectAnimator.ofFloat(view, View.SCALE_X, 1f, 1.04f, 1f).apply { duration = 220; start() }
    }

    override fun onDestroy() {
        soundPlayer.release()
        super.onDestroy()
    }
}
