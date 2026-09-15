package com.minhafazendinha.game

import android.content.Context
import android.graphics.*
import android.view.View
import kotlin.math.min

class GameSceneView(context: Context): View(context){
    var sceneMode="garden"; private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    fun setMode(v:String){sceneMode=v;invalidate()}
    private fun oval(c:Canvas,col:Int,l:Float,t:Float,r:Float,b:Float){p.color=col;c.drawOval(l,t,r,b,p)}
    private fun rr(c:Canvas,col:Int,l:Float,t:Float,r:Float,b:Float,rad:Float=18f){p.color=col;c.drawRoundRect(l,t,r,b,rad,rad,p)}
    override fun onDraw(c:Canvas){super.onDraw(c);val w=width.toFloat();val h=height.toFloat();if(w<=0)return
        p.shader=LinearGradient(0f,0f,0f,h,Color.rgb(92,198,240),Color.rgb(219,244,175),Shader.TileMode.CLAMP);c.drawRect(0f,0f,w,h,p);p.shader=null
        p.color=Color.rgb(255,220,62);c.drawCircle(w*.12f,h*.15f,min(w,h)*.065f,p)
        oval(c,Color.WHITE,w*.55f,h*.10f,w*.78f,h*.18f);oval(c,Color.WHITE,w*.64f,h*.07f,w*.88f,h*.18f)
        p.color=Color.rgb(91,176,74);val hill=Path();hill.moveTo(0f,h*.48f);hill.quadTo(w*.28f,h*.34f,w*.56f,h*.49f);hill.quadTo(w*.8f,h*.35f,w,h*.47f);hill.lineTo(w,h);hill.lineTo(0f,h);hill.close();c.drawPath(hill,p)
        when(sceneMode){
            "coop"->{rr(c,Color.rgb(230,173,76),w*.55f,h*.31f,w*.86f,h*.68f);p.color=Color.rgb(181,62,48);val roof=Path();roof.moveTo(w*.50f,h*.35f);roof.lineTo(w*.70f,h*.20f);roof.lineTo(w*.91f,h*.35f);roof.close();c.drawPath(roof,p);for(i in 0..2){oval(c,Color.WHITE,w*(.17f+i*.18f),h*.70f,w*(.24f+i*.18f),h*.77f);oval(c,Color.WHITE,w*(.18f+i*.18f),h*.53f,w*(.31f+i*.18f),h*.69f);p.color=Color.rgb(206,65,48);c.drawCircle(w*(.22f+i*.18f),h*.52f,10f,p)}}
            "lake"->{oval(c,Color.rgb(78,181,226),w*.08f,h*.52f,w*.92f,h*.90f);oval(c,Color.rgb(105,213,237),w*.16f,h*.60f,w*.83f,h*.82f);p.color=Color.rgb(255,203,56);val fish=Path();fish.moveTo(w*.46f,h*.70f);fish.lineTo(w*.40f,h*.66f);fish.lineTo(w*.40f,h*.74f);fish.close();c.drawPath(fish,p);oval(c,Color.rgb(255,156,55),w*.46f,h*.66f,w*.57f,h*.74f)}
            else->{rr(c,Color.rgb(121,76,40),w*.05f,h*.66f,w*.95f,h*.90f,12f);for(i in 0..5){val x=w*(.12f+i*.15f);p.color=Color.rgb(60,150,62);c.drawRect(x-4,h*.54f,x+4,h*.75f,p);c.drawCircle(x-15,h*.57f,17f,p);c.drawCircle(x+13,h*.60f,16f,p);p.color=Color.rgb(239,174,47);c.drawCircle(x,h*.54f,9f,p)}}
        }
        p.color=Color.rgb(220,176,102);for(i in 0..5){val x=i*w/5f;rr(c,p.color,x-5,h*.86f,x+10,h,5f)};rr(c,Color.rgb(191,139,72),0f,h*.91f,w,h*.95f,4f)
    }
}
