package com.minhafazendinha.game

import android.os.Bundle
import android.view.Gravity
import android.view.View
import android.widget.Button
import android.widget.FrameLayout
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    private lateinit var soundPlayer: AnimalSoundPlayer
    private lateinit var stage2d: AnimalStageView
    private lateinit var cow3d: Cow3DView
    private lateinit var status: TextView
    private var selectedAnimal="vaca"; private var selectedEmoji="🐮"; private var selectedAccessory=""
    private val animals=listOf(Triple("🐮","Vaca","vaca"),Triple("🐔","Galinha","galinha"),Triple("🐶","Cachorro","cachorro"),Triple("🐴","Cavalo","cavalo"),Triple("🐷","Porco","porco"),Triple("🐑","Ovelha","ovelha"),Triple("🐐","Cabra","cabra"),Triple("🫏","Burro","burro"))

    override fun onCreate(savedInstanceState:Bundle?){super.onCreate(savedInstanceState);soundPlayer=AnimalSoundPlayer(this)
        val content=LinearLayout(this).apply{orientation=LinearLayout.VERTICAL;gravity=Gravity.CENTER_HORIZONTAL;setPadding(32,42,32,42);setBackgroundColor(0xFFEAF7D5.toInt())}
        content.addView(TextView(this).apply{text="Minha Fazendinha — Laboratório 3D";textSize=27f;gravity=Gravity.CENTER})
        content.addView(TextView(this).apply{text="🐮 Vaca 3D + roupas • demais animais 2.5D • tudo liberado";textSize=15f;gravity=Gravity.CENTER;setPadding(0,8,0,16)})
        val host=FrameLayout(this);stage2d=AnimalStageView(this).apply{visibility=View.GONE};cow3d=Cow3DView(this)
        host.addView(stage2d,FrameLayout.LayoutParams(-1,-1));host.addView(cow3d,FrameLayout.LayoutParams(-1,-1));content.addView(host,LinearLayout.LayoutParams(-1,360).apply{setMargins(0,4,0,12)})
        status=TextView(this).apply{text="Vaca 3D — arraste para girar";textSize=17f;gravity=Gravity.CENTER;setPadding(0,0,0,12)};content.addView(status);content.addView(section("ANIMAIS — selecionar + testar som"))
        animals.forEach{(emoji,name,key)->content.addView(Button(this).apply{text="$emoji $name  🔊";textSize=18f;isAllCaps=false;setOnClickListener{selectedAnimal=key;selectedEmoji=emoji;selectedAccessory="";val cow=key=="vaca";cow3d.visibility=if(cow)View.VISIBLE else View.GONE;stage2d.visibility=if(cow)View.GONE else View.VISIBLE;cow3d.setAccessory("");if(!cow){stage2d.setAnimal(emoji);stage2d.celebrate()};status.text=if(cow)"Vaca 3D — arraste para girar • som acionado" else "$name 2.5D — som acionado";soundPlayer.play(key)}},fullWidthParams())}
        content.addView(section("ROUPAS — todas liberadas").apply{setPadding(0,24,0,4)})
        listOf("Sem roupa" to "","👒 Chapéu" to "👒","🎀 Laço" to "🎀","🧢 Boné" to "🧢","👑 Coroa" to "👑","🕶️ Óculos" to "🕶️","🧣 Cachecol" to "🧣","🎩 Cartola" to "🎩").forEach{(name,a)->content.addView(Button(this).apply{text="🔓 $name";isAllCaps=false;setOnClickListener{selectedAccessory=a;if(selectedAnimal=="vaca"){cow3d.setAccessory(a);status.text="$name equipado na Vaca 3D"}else{stage2d.setAnimal(selectedEmoji,if(a.isEmpty())"" else " $a");stage2d.celebrate();status.text="$name equipado em ${animalName(selectedAnimal)}"}}},fullWidthParams())}
        content.addView(Button(this).apply{text="🔊 TESTAR SOM NOVAMENTE";textSize=18f;isAllCaps=false;setOnClickListener{soundPlayer.play(selectedAnimal);if(selectedAnimal!="vaca")stage2d.celebrate()}},fullWidthParams().apply{setMargins(0,24,0,8)})
        setContentView(ScrollView(this).apply{addView(content)})
    }
    private fun section(v:String)=TextView(this).apply{text=v;textSize=15f;gravity=Gravity.CENTER}
    private fun fullWidthParams()=LinearLayout.LayoutParams(-1,-2).apply{setMargins(0,6,0,6)}
    private fun animalName(k:String)=animals.firstOrNull{it.third==k}?.second?:k
    override fun onDestroy(){soundPlayer.release();super.onDestroy()}
}
