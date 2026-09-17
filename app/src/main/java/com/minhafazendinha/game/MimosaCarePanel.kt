package com.minhafazendinha.game

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

data class CareUiAction(val state: CareVisualState,val label: String,val color: Int,val apply: MimosaCareState.() -> CareReaction)

class MimosaCarePanel(context: Context,private val state: MimosaCareState=MimosaCareState(),private val onAction:(CareVisualState,CareReaction)->Unit):LinearLayout(context){
 private val hunger=stat("🍎 Fome");private val hygiene=stat("💧 Higiene");private val happiness=stat("❤️ Felicidade");private val energy=stat("⚡ Energia");private val reaction=TextView(context);private val coins=TextView(context)
 private val actions=listOf(CareUiAction(CareVisualState.FEED,"🍎\nAlimentar",0xFFFF625C.toInt()){feed()},CareUiAction(CareVisualState.BATHE,"🚿\nBanho",0xFF55B8FF.toInt()){bathe()},CareUiAction(CareVisualState.BRUSH,"🧹\nEscovar",0xFFFFC83D.toInt()){brush()},CareUiAction(CareVisualState.PLAY,"🏐\nBrincar",0xFFFF65B7.toInt()){play()})
 init{orientation=VERTICAL;gravity=Gravity.CENTER;setPadding(12,8,12,8);reaction.gravity=Gravity.CENTER;reaction.textSize=18f;reaction.setTextColor(0xFF4A392D.toInt());reaction.text="❤️ Muuu! Estou feliz!";coins.gravity=Gravity.CENTER;coins.textSize=18f;coins.setTextColor(0xFF4A392D.toInt());addView(coins,lp());addView(hunger.root,lp());addView(hygiene.root,lp());addView(happiness.root,lp());addView(energy.root,lp());addView(reaction,lp());val row=LinearLayout(context).apply{orientation=HORIZONTAL;gravity=Gravity.CENTER};actions.forEach{row.addView(action(it),weight())};addView(row,lp());refresh()}
 private data class Stat(val root:LinearLayout,val bar:ProgressBar,val value:TextView)
 private fun stat(label:String):Stat{val row=LinearLayout(context).apply{orientation=HORIZONTAL;gravity=Gravity.CENTER_VERTICAL;setPadding(12,5,12,5)};val name=TextView(context).apply{text=label;textSize=16f;setTextColor(0xFF3F332B.toInt())};val bar=ProgressBar(context,null,android.R.attr.progressBarStyleHorizontal).apply{max=100};val value=TextView(context).apply{textSize=15f;gravity=Gravity.END;setTextColor(0xFF3F332B.toInt())};row.addView(name,LayoutParams(0,-2,1.25f));row.addView(bar,LayoutParams(0,24,1.5f).apply{setMargins(8,0,8,0)});row.addView(value,LayoutParams(0,-2,.55f));return Stat(row,bar,value)}
 private fun action(spec:CareUiAction)=Button(context).apply{text=spec.label;textSize=14f;isAllCaps=false;setTextColor(Color.WHITE);backgroundTintList=null;background=GradientDrawable().apply{setColor(spec.color);cornerRadius=24f};setOnClickListener{val result=spec.apply(state);reaction.text="${result.icon} ${result.message}${if(result.coins>0)"   +${result.coins} 🪙" else ""}";refresh();animate().scaleX(1.05f).scaleY(1.05f).setDuration(90).withEndAction{animate().scaleX(1f).scaleY(1f).duration=90};onAction(spec.state,result)}}
 private fun refresh(){hunger.bar.progress=state.hunger;hunger.value.text="${state.hunger}%";hygiene.bar.progress=state.hygiene;hygiene.value.text="${state.hygiene}%";happiness.bar.progress=state.happiness;happiness.value.text="${state.happiness}%";energy.bar.progress=state.energy;energy.value.text="${state.energy}%";coins.text="🐮 Mimosa • Nível 1     🪙 ${state.coins}"}
 private fun lp()=LayoutParams(-1,-2).apply{setMargins(0,4,0,4)};private fun weight()=LayoutParams(0,112,1f).apply{setMargins(4,4,4,4)}
}
