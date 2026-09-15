package com.minhafazendinha.game

import android.content.Context
import android.graphics.*
import android.view.View
import kotlin.math.min

class FarmSceneView(context: Context): View(context) {
    private val p = Paint(Paint.ANTI_ALIAS_FLAG)
    private val cloud = Paint(Paint.ANTI_ALIAS_FLAG).apply { color = Color.WHITE }
    private val brown = Color.rgb(125,76,38)
    private fun oval(c:Canvas,color:Int,l:Float,t:Float,r:Float,b:Float){p.color=color;c.drawOval(l,t,r,b,p)}
    private fun rect(c:Canvas,color:Int,l:Float,t:Float,r:Float,b:Float,rad:Float=0f){p.color=color;c.drawRoundRect(l,t,r,b,rad,rad,p)}
    override fun onDraw(c: Canvas) {
        super.onDraw(c); val w=width.toFloat(); val h=height.toFloat(); if(w<=0||h<=0)return
        p.shader=LinearGradient(0f,0f,0f,h,Color.rgb(88,194,239),Color.rgb(211,241,165),Shader.TileMode.CLAMP);c.drawRect(0f,0f,w,h,p);p.shader=null
        p.color=Color.rgb(255,221,72);c.drawCircle(w*.14f,h*.17f,min(w,h)*.075f,p)
        fun clouds(x:Float,y:Float){oval(c,Color.WHITE,x,y,x+w*.16f,y+h*.07f);oval(c,Color.WHITE,x+w*.06f,y-h*.035f,x+w*.18f,y+h*.06f);oval(c,Color.WHITE,x+w*.12f,y,x+w*.27f,y+h*.07f)}
        clouds(w*.52f,h*.12f)
        p.color=Color.rgb(102,181,82);val hill=Path();hill.moveTo(0f,h*.56f);hill.quadTo(w*.25f,h*.38f,w*.52f,h*.55f);hill.quadTo(w*.78f,h*.37f,w,h*.52f);hill.lineTo(w,h);hill.lineTo(0f,h);hill.close();c.drawPath(hill,p)
        rect(c,Color.rgb(244,201,102),w*.59f,h*.34f,w*.86f,h*.62f,8f);p.color=Color.rgb(188,69,50);val roof=Path();roof.moveTo(w*.55f,h*.38f);roof.lineTo(w*.73f,h*.24f);roof.lineTo(w*.91f,h*.38f);roof.close();c.drawPath(roof,p);rect(c,brown,w*.69f,h*.46f,w*.78f,h*.62f,5f);rect(c,Color.rgb(246,234,174),w*.63f,h*.41f,w*.68f,h*.47f,3f)
        rect(c,Color.rgb(145,91,43),0f,h*.70f,w,h*.73f);for(i in 0..7){val x=i*w/7f;rect(c,Color.rgb(222,184,110),x-5,h*.62f,x+7,h*.82f,4f)}
        p.color=Color.rgb(57,133,57);for(i in 0..8){val x=w*(.05f+i*.115f);c.drawCircle(x,h*.80f,18f,p);c.drawCircle(x+13,h*.78f,16f,p)}
        // cute cow foreground
        oval(c,Color.WHITE,w*.25f,h*.48f,w*.55f,h*.84f);oval(c,Color.WHITE,w*.30f,h*.38f,w*.52f,h*.60f);oval(c,Color.rgb(70,55,45),w*.31f,h*.44f,w*.37f,h*.51f);oval(c,Color.rgb(70,55,45),w*.46f,h*.51f,w*.52f,h*.59f);oval(c,Color.rgb(255,190,190),w*.35f,h*.52f,w*.48f,h*.61f);p.color=Color.BLACK;c.drawCircle(w*.355f,h*.475f,7f,p);c.drawCircle(w*.465f,h*.475f,7f,p);p.color=Color.WHITE;c.drawCircle(w*.357f,h*.472f,2.5f,p);c.drawCircle(w*.467f,h*.472f,2.5f,p)
        p.color=Color.rgb(242,174,50);for(i in 0..5){val x=w*(.08f+i*.17f);c.drawCircle(x,h*.91f,10f,p);p.color=Color.rgb(101,170,63);c.drawRect(x-2,h*.91f,x+2,h*.98f,p);p.color=Color.rgb(242,174,50)}
    }
}
