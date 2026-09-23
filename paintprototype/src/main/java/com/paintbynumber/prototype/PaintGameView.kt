package com.paintbynumber.prototype

import android.content.Context
import android.graphics.*
import android.view.MotionEvent
import android.view.View
import kotlin.math.min

class PaintGameView(context: Context) : View(context) {

    data class Area(val number: Int, val path: Path, var painted: Boolean = false)
    private val colors = listOf(
        Color.rgb(244,67,54), Color.rgb(255,193,7), Color.rgb(76,175,80), Color.rgb(33,150,243),
        Color.rgb(156,39,176), Color.rgb(255,152,0), Color.rgb(0,188,212), Color.rgb(233,30,99)
    )
    private var selected = 1
    private var drawingIndex = 0
    private var areas = mutableListOf<Area>()
    private val paint = Paint(Paint.ANTI_ALIAS_FLAG)
    private val textPaint = Paint(Paint.ANTI_ALIAS_FLAG).apply {
        color = Color.DKGRAY
        textAlign = Paint.Align.CENTER
        typeface = Typeface.DEFAULT_BOLD
    }

    init { setBackgroundColor(Color.WHITE) }

    override fun onSizeChanged(w: Int, h: Int, oldw: Int, oldh: Int) = rebuild()

    private fun rebuild() {
        areas = if (drawingIndex == 0) butterfly() else fish()
        invalidate()
    }

    private fun p(vararg pts: Float): Path {
        val path = Path()
        path.moveTo(pts[0], pts[1])
        var i = 2
        while (i < pts.size) { path.lineTo(pts[i], pts[i+1]); i += 2 }
        path.close()
        return path
    }

    private fun butterfly(): MutableList<Area> {
        val w = width.toFloat(); val top = height * .14f; val bottom = height * .72f
        fun sx(x: Float) = x * w
        fun sy(y: Float) = top + y * (bottom - top)
        return mutableListOf(
            Area(1,p(sx(.48f),sy(.10f),sx(.52f),sy(.10f),sx(.53f),sy(.80f),sx(.47f),sy(.80f))),
            Area(2,p(sx(.47f),sy(.18f),sx(.25f),sy(.05f),sx(.12f),sy(.22f),sx(.28f),sy(.42f))),
            Area(3,p(sx(.28f),sy(.42f),sx(.12f),sy(.22f),sx(.10f),sy(.52f),sx(.31f),sy(.58f))),
            Area(4,p(sx(.31f),sy(.58f),sx(.10f),sy(.52f),sx(.20f),sy(.82f),sx(.43f),sy(.72f))),
            Area(5,p(sx(.53f),sy(.18f),sx(.75f),sy(.05f),sx(.88f),sy(.22f),sx(.72f),sy(.42f))),
            Area(6,p(sx(.72f),sy(.42f),sx(.88f),sy(.22f),sx(.90f),sy(.52f),sx(.69f),sy(.58f))),
            Area(7,p(sx(.69f),sy(.58f),sx(.90f),sy(.52f),sx(.80f),sy(.82f),sx(.57f),sy(.72f))),
            Area(8,p(sx(.43f),sy(.72f),sx(.47f),sy(.80f),sx(.50f),sy(.95f),sx(.53f),sy(.80f),sx(.57f),sy(.72f)))
        )
    }

    private fun fish(): MutableList<Area> {
        val w = width.toFloat(); val top = height*.18f; val bottom = height*.70f
        fun sx(x: Float) = x * w
        fun sy(y: Float) = top + y * (bottom - top)
        return mutableListOf(
            Area(1,p(sx(.18f),sy(.50f),sx(.34f),sy(.22f),sx(.45f),sy(.18f),sx(.42f),sy(.82f),sx(.30f),sy(.77f))),
            Area(2,p(sx(.42f),sy(.18f),sx(.52f),sy(.15f),sx(.52f),sy(.85f),sx(.42f),sy(.82f))),
            Area(3,p(sx(.52f),sy(.15f),sx(.62f),sy(.18f),sx(.63f),sy(.82f),sx(.52f),sy(.85f))),
            Area(4,p(sx(.62f),sy(.18f),sx(.74f),sy(.28f),sx(.78f),sy(.68f),sx(.63f),sy(.82f))),
            Area(5,p(sx(.78f),sy(.68f),sx(.92f),sy(.88f),sx(.88f),sy(.52f),sx(.92f),sy(.16f),sx(.74f),sy(.28f))),
            Area(6,p(sx(.36f),sy(.24f),sx(.48f),sy(.02f),sx(.58f),sy(.18f))),
            Area(7,p(sx(.38f),sy(.76f),sx(.49f),sy(.98f),sx(.60f),sy(.82f))),
            Area(8,p(sx(.22f),sy(.50f),sx(.30f),sy(.42f),sx(.30f),sy(.58f)))
        )
    }

    override fun onDraw(c: Canvas) {
        super.onDraw(c)
        val w = width.toFloat(); val h = height.toFloat()
        paint.style = Paint.Style.FILL; paint.color = Color.rgb(248,248,248)
        c.drawRect(0f,0f,w,h*.11f,paint)
        textPaint.textSize = min(w,h)*.045f
        textPaint.color = Color.DKGRAY
        c.drawText(if(drawingIndex==0) "Borboleta" else "Peixinho", w/2,h*.07f,textPaint)

        areas.forEach { a ->
            paint.style = Paint.Style.FILL
            paint.color = if(a.painted) colors[a.number-1] else Color.WHITE
            c.drawPath(a.path,paint)
            paint.style = Paint.Style.STROKE; paint.strokeWidth = 3f; paint.color = Color.rgb(120,120,120)
            c.drawPath(a.path,paint)
            if(!a.painted){
                val b=RectF(); a.path.computeBounds(b,true)
                textPaint.textSize=min(w,h)*.034f; textPaint.color=Color.rgb(110,110,110)
                c.drawText(a.number.toString(),b.centerX(),b.centerY()+textPaint.textSize*.35f,textPaint)
            }
        }

        val y=h*.84f
        colors.forEachIndexed { i,col ->
            val x=w*(.075f+i*.122f)
            paint.style=Paint.Style.FILL; paint.color=col
            c.drawCircle(x,y,w*.045f,paint)
            paint.style=Paint.Style.STROKE; paint.strokeWidth=if(selected==i+1) 8f else 2f
            paint.color=if(selected==i+1) Color.BLACK else Color.LTGRAY
            c.drawCircle(x,y,w*.052f,paint)
            textPaint.textSize=w*.035f; textPaint.color=Color.WHITE
            c.drawText((i+1).toString(),x,y+textPaint.textSize*.35f,textPaint)
        }

        paint.style=Paint.Style.FILL; paint.color=Color.rgb(245,245,245)
        c.drawRoundRect(w*.15f,h*.91f,w*.45f,h*.97f,20f,20f,paint)
        c.drawRoundRect(w*.55f,h*.91f,w*.85f,h*.97f,20f,20f,paint)
        textPaint.textSize=w*.035f; textPaint.color=Color.DKGRAY
        c.drawText("Borboleta",w*.30f,h*.95f,textPaint)
        c.drawText("Peixinho",w*.70f,h*.95f,textPaint)
    }

    override fun onTouchEvent(e: MotionEvent): Boolean {
        if(e.action!=MotionEvent.ACTION_UP) return true
        val w=width.toFloat(); val h=height.toFloat()
        if(e.y in h*.79f..h*.89f){
            val idx=((e.x/w-.014f)/.122f).toInt().coerceIn(0,7)
            selected=idx+1; invalidate(); return true
        }
        if(e.y>h*.90f){
            drawingIndex=if(e.x<w/2) 0 else 1
            rebuild(); return true
        }
        // Prefer the currently selected numbered region. Some drawings have
        // intentionally overlapping paths (for example, the fish eye sits
        // inside the body), so checking the first path alone can block it.
        areas.firstOrNull {
            it.number == selected && !it.painted && contains(it.path, e.x, e.y)
        }?.let {
            it.painted = true
            invalidate()
        }
        return true
    }

    private fun contains(path: Path,x:Float,y:Float):Boolean{
        val b=RectF(); path.computeBounds(b,true)
        val clip=Region(b.left.toInt(),b.top.toInt(),b.right.toInt(),b.bottom.toInt())
        return Region().apply{ setPath(path,clip) }.contains(x.toInt(),y.toInt())
    }
}
