package com.minhafazendinha.game

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.view.Gravity
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ProgressBar
import android.widget.TextView

data class CareUiAction(val state:CareVisualState,val label:String,val color:Int,val apply:MimosaCareState.()->CareReaction)

/** Compact production HUD: keeps the artwork dominant and exposes child-sized actions. */
class MimosaCarePanel(context:Context,private val state:MimosaCareState=MimosaCareState(),private val onAction:(CareVisualState,CareReaction)->Unit):LinearLayout(context){
 private val status=TextView(context)
 private val reaction=TextView(context)
 private val progress=ProgressBar(context,null,android.R.attr.progressBarStyleHorizontal).apply{max=100}
 private val actions=listOf(
  CareUiAction(CareVisualState.FEED,"🥕  COMER",0xFF48B92D.toInt()){feed()},
  CareUiAction(CareVisualState.BATHE,"🧽  BANHO",0xFF25A9F2.toInt()){bathe()},
  CareUiAction(CareVisualState.BRUSH,"🪮  ESCOVAR",0xFFF4B51C.toInt()){brush()},
  CareUiAction(CareVisualState.PLAY,"🏐  BRINCAR",0xFFED4F96.toInt()){play()}
 )
 init{
  orientation=VERTICAL;gravity=Gravity.CENTER;setPadding(12,8,12,10)
  background=GradientDrawable().apply{setColor(0xEFFFF8E8.toInt());cornerRadii=floatArrayOf(28f,28f,28f,28f,0f,0f,0f,0f)}
  status.gravity=Gravity.CENTER;status.textSize=17f;status.setTextColor(0xFF5B351B.toInt());status.setTypeface(null,android.graphics.Typeface.BOLD)
  reaction.gravity=Gravity.CENTER;reaction.textSize=15f;reaction.setTextColor(0xFF704525.toInt());reaction.text="❤️ Muuu! Vamos brincar?"
  addView(status,lp());addView(progress,LayoutParams(-1,18).apply{setMargins(24,2,24,5)});addView(reaction,lp())
  val row=LinearLayout(context).apply{orientation=HORIZONTAL;gravity=Gravity.CENTER}
  actions.forEach{row.addView(action(it),weight())};addView(row,lp());refresh()
 }
 private fun action(spec:CareUiAction)=Button(context).apply{
  text=spec.label;textSize=12f;isAllCaps=false;setTextColor(Color.WHITE);minHeight=96;stateListAnimator=null
  background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(lighten(spec.color),spec.color)).apply{cornerRadius=26f;setStroke(3,0x33FFFFFF)}
  setPadding(5,0,5,0)
  setOnClickListener{
   val result=spec.apply(state);reaction.text=result.icon+" "+result.message+(if(result.coins>0)"   +"+result.coins+" 🪙" else "")
   refresh();animate().scaleX(.94f).scaleY(.94f).setDuration(80).withEndAction{animate().scaleX(1f).scaleY(1f).setDuration(110).start()}.start()
   onAction(spec.state,result)
  }
 }
 private fun refresh(){
  val wellbeing=(state.hunger+state.hygiene+state.happiness+state.energy)/4
  progress.progress=wellbeing
  status.text="🐮  MIMOSA   •   ⭐ Nível 1   •   🪙 "+state.coins+"   •   ❤️ "+wellbeing+"%"
 }
 private fun lighten(color:Int):Int{
  val r=(Color.red(color)+42).coerceAtMost(255);val g=(Color.green(color)+42).coerceAtMost(255);val b=(Color.blue(color)+42).coerceAtMost(255)
  return Color.rgb(r,g,b)
 }
 private fun lp()=LayoutParams(-1,-2).apply{setMargins(0,3,0,3)}
 private fun weight()=LayoutParams(0,96,1f).apply{setMargins(4,4,4,4)}
}
