package com.minhafazendinha.game

import android.content.Context
import android.graphics.Canvas
import android.graphics.Color
import android.graphics.Paint
import android.graphics.Path
import android.util.AttributeSet
import android.view.MotionEvent
import android.view.View
import kotlin.math.cos
import kotlin.math.sin

class Cow3DView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint=Paint(Paint.ANTI_ALIAS_FLAG)
    private var angleY=-.35f; private var lastX=0f; private var accessory=""
    private val vertices=arrayOf(floatArrayOf(-1.25f,-.65f,-.55f),floatArrayOf(1.25f,-.65f,-.55f),floatArrayOf(1.25f,.65f,-.55f),floatArrayOf(-1.25f,.65f,-.55f),floatArrayOf(-1.25f,-.65f,.55f),floatArrayOf(1.25f,-.65f,.55f),floatArrayOf(1.25f,.65f,.55f),floatArrayOf(-1.25f,.65f,.55f))
    private val faces=arrayOf(intArrayOf(0,1,2,3),intArrayOf(4,7,6,5),intArrayOf(0,4,5,1),intArrayOf(3,2,6,7),intArrayOf(1,5,6,2),intArrayOf(0,3,7,4))

    fun setAccessory(value:String){ accessory=value.trim(); invalidate() }

    override fun onDraw(canvas:Canvas){
        super.onDraw(canvas); canvas.drawColor(Color.rgb(196,235,255))
        val cx=width/2f; val cy=height*.55f; val s=width.coerceAtMost(height)*.23f
        paint.color=0x22000000; canvas.drawOval(cx-s*1.5f,cy+s*.9f,cx+s*1.5f,cy+s*1.25f,paint)
        val pts=vertices.map{v-> val x=v[0]*cos(angleY)-v[2]*sin(angleY); val z=v[0]*sin(angleY)+v[2]*cos(angleY); val p=4.8f/(4.8f+z); floatArrayOf(cx+x*s*p,cy-v[1]*s*p,z)}
        faces.sortedBy{f->f.map{pts[it][2]}.average()}.forEachIndexed{i,f->
            val p=Path(); f.forEachIndexed{j,id->val q=pts[id];if(j==0)p.moveTo(q[0],q[1])else p.lineTo(q[0],q[1])};p.close()
            paint.color=if(i%3==0)Color.WHITE else if(i%3==1)0xFFE7E7E7.toInt() else 0xFFF7F7F7.toInt();canvas.drawPath(p,paint)
        }
        paint.color=Color.WHITE;canvas.drawCircle(cx+s*.92f,cy-s*.18f,s*.55f,paint)
        paint.color=0xFFFFB7C8.toInt();canvas.drawOval(cx+s*.75f,cy,cx+s*1.35f,cy+s*.35f,paint)
        paint.color=Color.BLACK;canvas.drawCircle(cx+s*.77f,cy-s*.27f,s*.07f,paint);canvas.drawCircle(cx+s*1.08f,cy-s*.27f,s*.07f,paint)
        canvas.drawCircle(cx-s*.35f,cy-s*.15f,s*.22f,paint);canvas.drawCircle(cx+s*.25f,cy+s*.22f,s*.18f,paint)
        paint.strokeWidth=s*.18f;paint.strokeCap=Paint.Cap.ROUND
        for(dx in floatArrayOf(-.72f,-.28f,.48f,.82f))canvas.drawLine(cx+s*dx,cy+s*.5f,cx+s*dx,cy+s*1.05f,paint)
        drawAccessory(canvas,cx+s*.92f,cy-s*.65f,s)
    }

    private fun drawAccessory(c:Canvas,x:Float,y:Float,s:Float){
        when(accessory){
            "👒","🧢","🎩"->{ paint.color=when(accessory){"🧢"->0xFF4285F4.toInt();"🎩"->0xFF292929.toInt();else->0xFFFFD45C.toInt()}; c.drawOval(x-s*.55f,y-s*.12f,x+s*.55f,y+s*.08f,paint); c.drawRoundRect(x-s*.32f,y-s*.48f,x+s*.32f,y, s*.12f,s*.12f,paint) }
            "👑"->{paint.color=0xFFFFC928.toInt();val p=Path();p.moveTo(x-s*.42f,y);p.lineTo(x-s*.38f,y-s*.45f);p.lineTo(x-s*.12f,y-s*.2f);p.lineTo(x,y-s*.55f);p.lineTo(x+s*.14f,y-s*.2f);p.lineTo(x+s*.4f,y-s*.45f);p.lineTo(x+s*.42f,y);p.close();c.drawPath(p,paint)}
            "🎀"->{paint.color=0xFFFF5B91.toInt();c.drawCircle(x-s*.18f,y,s*.22f,paint);c.drawCircle(x+s*.18f,y,s*.22f,paint);paint.color=0xFFFF8FB4.toInt();c.drawCircle(x,y,s*.12f,paint)}
            "🕶️"->{paint.style=Paint.Style.STROKE;paint.strokeWidth=s*.1f;paint.color=Color.BLACK;c.drawCircle(x-s*.16f,y+s*.42f,s*.18f,paint);c.drawCircle(x+s*.16f,y+s*.42f,s*.18f,paint);c.drawLine(x-s*.02f,y+s*.42f,x+s*.02f,y+s*.42f,paint);paint.style=Paint.Style.FILL}
            "🧣"->{paint.color=0xFFE84D4D.toInt();c.drawRoundRect(x-s*.42f,y+s*.68f,x+s*.42f,y+s*.9f,s*.1f,s*.1f,paint);c.drawRect(x+s*.2f,y+s*.82f,x+s*.38f,y+s*1.3f,paint)}
        }
    }

    override fun onTouchEvent(e:MotionEvent):Boolean{when(e.actionMasked){MotionEvent.ACTION_DOWN->lastX=e.x;MotionEvent.ACTION_MOVE->{angleY+=(e.x-lastX)/260f;lastX=e.x;invalidate()};MotionEvent.ACTION_UP->performClick()};return true}
    override fun performClick():Boolean{super.performClick();return true}
}
