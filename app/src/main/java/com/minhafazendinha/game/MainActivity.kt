package com.minhafazendinha.game

import android.os.Bundle
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var soundPlayer: AnimalSoundPlayer
    private lateinit var stage: AnimalStageView
    private lateinit var status: TextView
    private var selectedAnimal = "vaca"
    private var selectedEmoji = "🐮"
    private var selectedAccessory = ""

    private val animals = listOf(
        Triple("🐮", "Vaca", "vaca"), Triple("🐔", "Galinha", "galinha"),
        Triple("🐶", "Cachorro", "cachorro"), Triple("🐴", "Cavalo", "cavalo"),
        Triple("🐷", "Porco", "porco"), Triple("🐑", "Ovelha", "ovelha"),
        Triple("🐐", "Cabra", "cabra"), Triple("🫏", "Burro", "burro")
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
            text = "Minha Fazendinha — Teste 2.5D"
            textSize = 27f
            gravity = Gravity.CENTER
        })
        content.addView(TextView(this).apply {
            text = "🔓 Animais e roupas liberados • arraste o bichinho para girar"
            textSize = 15f
            gravity = Gravity.CENTER
            setPadding(0, 8, 0, 16)
        })

        stage = AnimalStageView(this).apply { setAnimal(selectedEmoji) }
        content.addView(stage, LinearLayout.LayoutParams(
            LinearLayout.LayoutParams.MATCH_PARENT, 330
        ).apply { setMargins(0, 4, 0, 12) })

        status = TextView(this).apply {
            text = "Vaca selecionada"
            textSize = 17f
            gravity = Gravity.CENTER
            setPadding(0, 0, 0, 12)
        }
        content.addView(status)
        content.addView(section("ANIMAIS — selecionar + testar som"))

        animals.forEach { (emoji, name, key) ->
            content.addView(Button(this).apply {
                text = "$emoji $name  🔊"
                textSize = 18f
                isAllCaps = false
                setOnClickListener {
                    selectedAnimal = key
                    selectedEmoji = emoji
                    selectedAccessory = ""
                    stage.setAnimal(emoji)
                    stage.celebrate()
                    status.text = "$name selecionado — som acionado"
                    soundPlayer.play(key)
                }
            }, fullWidthParams())
        }

        content.addView(section("ROUPAS — todas liberadas" ).apply { setPadding(0, 24, 0, 4) })
        listOf(
            "Sem roupa" to "", "👒 Chapéu" to " 👒", "🎀 Laço" to " 🎀",
            "🧢 Boné" to " 🧢", "👑 Coroa" to " 👑", "🕶️ Óculos" to " 🕶️",
            "🧣 Cachecol" to " 🧣", "🎩 Cartola" to " 🎩"
        ).forEach { (name, accessory) ->
            content.addView(Button(this).apply {
                text = "🔓 $name"
                isAllCaps = false
                setOnClickListener {
                    selectedAccessory = accessory
                    stage.setAnimal(selectedEmoji, selectedAccessory)
                    stage.celebrate()
                    status.text = "$name equipado em ${animalName(selectedAnimal)}"
                }
            }, fullWidthParams())
        }

        content.addView(Button(this).apply {
            text = "🔊 TESTAR SOM NOVAMENTE"
            textSize = 18f
            isAllCaps = false
            setOnClickListener {
                soundPlayer.play(selectedAnimal)
                stage.celebrate()
            }
        }, fullWidthParams().apply { setMargins(0, 24, 0, 8) })

        setContentView(ScrollView(this).apply { addView(content) })
    }

    private fun section(value: String) = TextView(this).apply {
        text = value; textSize = 15f; gravity = Gravity.CENTER
    }

    private fun fullWidthParams() = LinearLayout.LayoutParams(
        LinearLayout.LayoutParams.MATCH_PARENT, LinearLayout.LayoutParams.WRAP_CONTENT
    ).apply { setMargins(0, 6, 0, 6) }

    private fun animalName(key: String) = animals.firstOrNull { it.third == key }?.second ?: key

    override fun onDestroy() {
        soundPlayer.release()
        super.onDestroy()
    }
}
