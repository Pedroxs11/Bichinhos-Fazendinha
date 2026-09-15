package com.minhafazendinha.game

import android.animation.ObjectAnimator
import android.content.Context
import android.graphics.Color
import android.graphics.drawable.GradientDrawable
import android.util.AttributeSet
import android.view.Gravity
import android.view.MotionEvent
import android.widget.FrameLayout
import android.widget.TextView

class AnimalStageView @JvmOverloads constructor(context:Context,attrs:AttributeSet?=null):FrameLayout(context,attrs){
    private val shadow=TextView(context);private val animal=TextView(context);private val hint=TextView(context)
    private var baseRotationY=-8f;private var idle:ObjectAnimator?=null
    init{
        clipChildren=false;clipToPadding=false;minimumHeight=320;setPadding(24,20,24,20)
        background=GradientDrawable(GradientDrawable.Orientation.TOP_BOTTOM,intArrayOf(Color.rgb(184,232,255),Color.rgb(224,246,190))).apply{cornerRadius=36f}
        shadow.apply{text="●";textSize=74f;gravity=Gravity.CENTER;alpha=.13f;scaleX=1.7f;scaleY=.38f;translationY=78f}
        animal.apply{text="🐮";textSize=100f;gravity=Gravity.CENTER;elevation=18f;rotationY=baseRotationY;cameraDistance=resources.displayMetrics.density*8000f}
        hint.apply{text="👆 Toque no bichinho";textSize=14f;gravity=Gravity.BOTTOM or Gravity.CENTER_HORIZONTAL;setPadding(0,0,0,12);alpha=.7f}
        addView(shadow,LayoutParams(-1,-1));addView(animal,LayoutParams(-1,-1));addView(hint,LayoutParams(-1,-1));startIdle()
    }
    private fun startIdle(){idle?.cancel();idle=ObjectAnimator.ofFloat(animal,"translationY",-5f,8f,-5f).apply{duration=1800;repeatCount=ObjectAnimator.INFINITE;start()}}
    fun setAnimal(emoji:String,accessory:String=""){animal.text=emoji+accessory;ObjectAnimator.ofFloat(animal,"rotationY",animal.rotationY,14f,baseRotationY).apply{duration=430;start()}}
    fun celebrate(){ObjectAnimator.ofFloat(animal,"scaleX",1f,1.18f,.96f,1f).apply{duration=380;start()};ObjectAnimator.ofFloat(animal,"scaleY",1f,1.18f,.96f,1f).apply{duration=380;start()};ObjectAnimator.ofFloat(animal,"rotationY",baseRotationY,25f,-20f,baseRotationY).apply{duration=560;start()}}
    override fun onTouchEvent(e:MotionEvent):Boolean{when(e.actionMasked){MotionEvent.ACTION_DOWN->{animal.scaleX=.96f;animal.scaleY=.96f};MotionEvent.ACTION_MOVE->{val center=width/2f;if(center>0)animal.rotationY=((e.x-center)/center*24f).coerceIn(-24f,24f)};MotionEvent.ACTION_UP,MotionEvent.ACTION_CANCEL->{animal.scaleX=1f;animal.scaleY=1f;ObjectAnimator.ofFloat(animal,"rotationY",animal.rotationY,baseRotationY).apply{duration=220;start()};performClick()}};return true}
    override fun performClick():Boolean{super.performClick();celebrate();return true}
    override fun onDetachedFromWindow(){idle?.cancel();idle=null;super.onDetachedFromWindow()}
    override fun onAttachedToWindow(){super.onAttachedToWindow();if(idle==null)startIdle()}
}
