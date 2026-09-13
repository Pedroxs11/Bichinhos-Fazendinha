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

/** Protótipo 3D leve da vaquinha, renderizado sem engine externa.
 * Usa projeção perspectiva de sólidos para validar rotação/interação antes do modelo GLB final.
 */
class Cow3DView @JvmOverloads constructor(context: Context, attrs: AttributeSet? = null) : View(context, attrs) {
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private var angleY = -0.35f
    private var lastX = 0f
    private val vertices = arrayOf(
        floatArrayOf(-1.25f,-.65f,-.55f), floatArrayOf(1.25f,-.65f,-.55f),
        floatArrayOf(1.25f,.65f,-.55f), floatArrayOf(-1.25f,.65f,-.55f),
        floatArrayOf(-1.25f,-.65f,.55f), floatArrayOf(1.25f,-.65f,.55f),
        floatArrayOf(1.25f,.65f,.55f), floatArrayOf(-1.25f,.65f,.55f)
    )
    private val faces = arrayOf(
        intArrayOf(0,1,2,3), intArrayOf(4,7,6,5), intArrayOf(0,4,5,1),
        intArrayOf(3,2,6,7), intArrayOf(1,5,6,2), intArrayOf(0,3,7,4)
    )

    override fun onDraw(canvas: Canvas) {
        super.onDraw(canvas)
        canvas.drawColor(Color.rgb(196,235,255))
        val cx = width/2f; val cy = height*.55f; val scale = width.coerceAtMost(height)*.23f
        // ground shadow
        paint.color = 0x22000000
        canvas.drawOval(cx-scale*1.5f, cy+scale*.9f, cx+scale*1.5f, cy+scale*1.25f, paint)
        val projected = vertices.map { v ->
            val x = v[0]*cos(angleY) - v[2]*sin(angleY)
            val z = v[0]*sin(angleY) + v[2]*cos(angleY)
            val perspective = 4.8f/(4.8f+z)
            floatArrayOf(cx+x*scale*perspective, cy-v[1]*scale*perspective, z)
        }
        faces.sortedBy { f -> f.map { projected[it][2] }.average() }.forEachIndexed { index, face ->
            val p = Path(); face.forEachIndexed { i, id ->
                val q=projected[id]; if(i==0)p.moveTo(q[0],q[1]) else p.lineTo(q[0],q[1])
            }; p.close()
            paint.color = if(index%3==0) Color.WHITE else if(index%3==1) 0xFFE7E7E7.toInt() else 0xFFF7F7F7.toInt()
            canvas.drawPath(p,paint)
        }
        // cartoon cow head + spots layered over projected body
        paint.color=Color.WHITE; canvas.drawCircle(cx+scale*.92f,cy-scale*.18f,scale*.55f,paint)
        paint.color=0xFFFFB7C8.toInt(); canvas.drawOval(cx+scale*.75f,cy, cx+scale*1.35f,cy+scale*.35f,paint)
        paint.color=Color.BLACK
        canvas.drawCircle(cx+scale*.77f,cy-scale*.27f,scale*.07f,paint)
        canvas.drawCircle(cx+scale*1.08f,cy-scale*.27f,scale*.07f,paint)
        canvas.drawCircle(cx-scale*.35f,cy-scale*.15f,scale*.22f,paint)
        canvas.drawCircle(cx+scale*.25f,cy+scale*.22f,scale*.18f,paint)
        // legs
        paint.strokeWidth=scale*.18f; paint.strokeCap=Paint.Cap.ROUND
        for(dx in floatArrayOf(-.72f,-.28f,.48f,.82f)) canvas.drawLine(cx+scale*dx,cy+scale*.5f,cx+scale*dx,cy+scale*1.05f,paint)
    }

    override fun onTouchEvent(event: MotionEvent): Boolean {
        when(event.actionMasked){
            MotionEvent.ACTION_DOWN -> lastX=event.x
            MotionEvent.ACTION_MOVE -> { angleY += (event.x-lastX)/260f; lastX=event.x; invalidate() }
            MotionEvent.ACTION_UP -> performClick()
        }
        return true
    }
    override fun performClick(): Boolean { super.performClick(); return true }
}
