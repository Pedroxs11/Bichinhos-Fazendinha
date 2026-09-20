package com.minhafazendinha.game

import android.content.Context
import android.graphics.*
import android.view.View
import android.view.MotionEvent
import android.animation.ValueAnimator
import android.view.animation.LinearInterpolator
import kotlin.math.min
import kotlin.math.sin

class GameSceneView(context: Context): View(context){
    var sceneMode="garden"; var onFarmAnimalTap:((String)->Unit)?=null; private val p=Paint(Paint.ANTI_ALIAS_FLAG)
    private var lakePhase=0f
    private var farmPhase=0f
    private var splashX=.5f
    private var splashY=.72f
    private var splashAlpha=0f
    private var lakeTapCount=0
    private var wildlifeBoost=0f
    private var lakeHearts=0f
    private var lakeBubbles=0f
    private val ambienceAnimator=ValueAnimator.ofFloat(0f,1f).apply{duration=7000;repeatCount=ValueAnimator.INFINITE;interpolator=LinearInterpolator();addUpdateListener{farmPhase=it.animatedValue as Float;if(sceneMode!="lake")invalidate()}}
    private val lakeAnimator=ValueAnimator.ofFloat(0f,1f).apply{duration=6000;repeatCount=ValueAnimator.INFINITE;interpolator=LinearInterpolator();addUpdateListener{lakePhase=it.animatedValue as Float;if(sceneMode=="lake")invalidate()}}
    init{ambienceAnimator.start()}
    fun setMode(v:String){sceneMode=v;if(v=="lake"){if(!lakeAnimator.isStarted)lakeAnimator.start()}else lakeAnimator.cancel();if(!ambienceAnimator.isStarted)ambienceAnimator.start();invalidate()}
    private fun oval(c:Canvas,col:Int,l:Float,t:Float,r:Float,b:Float){p.color=col;c.drawOval(l,t,r,b,p)}
    private fun rr(c:Canvas,col:Int,l:Float,t:Float,r:Float,b:Float,rad:Float=18f){p.color=col;c.drawRoundRect(l,t,r,b,rad,rad,p)}
    private fun drawFish(c:Canvas,x:Float,y:Float,scale:Float){val s=scale*.06f;p.color=Color.rgb(255,154,42);c.drawOval(x-s,y-s*.45f,x+s,y+s*.45f,p);val tail=Path();tail.moveTo(x-s,y);tail.lineTo(x-s*1.65f,y-s*.65f);tail.lineTo(x-s*1.65f,y+s*.65f);tail.close();c.drawPath(tail,p);p.color=Color.WHITE;c.drawCircle(x+s*.45f,y-s*.10f,s*.14f,p);p.color=Color.rgb(45,61,70);c.drawCircle(x+s*.49f,y-s*.10f,s*.07f,p)}
    private fun drawDuck(c:Canvas,x:Float,y:Float,scale:Float){val s=scale*.055f;p.color=Color.rgb(255,242,201);c.drawOval(x-s*1.25f,y-s*.45f,x+s*1.25f,y+s*.65f,p);c.drawCircle(x+s*.70f,y-s*.65f,s*.58f,p);p.color=Color.rgb(255,166,37);val beak=Path();beak.moveTo(x+s*1.20f,y-s*.68f);beak.lineTo(x+s*1.75f,y-s*.48f);beak.lineTo(x+s*1.18f,y-s*.32f);beak.close();c.drawPath(beak,p);p.color=Color.rgb(45,61,70);c.drawCircle(x+s*.86f,y-s*.78f,s*.09f,p)}
    private fun drawLily(c:Canvas,x:Float,y:Float,scale:Float){val s=scale*.045f;p.color=Color.rgb(72,167,75);c.drawOval(x-s,y-s*.35f,x+s,y+s*.35f,p);p.color=Color.rgb(244,119,171);c.drawCircle(x,y-s*.20f,s*.23f,p)}
    private fun drawBarn(c:Canvas,w:Float,h:Float){rr(c,Color.rgb(205,72,55),w*.66f,h*.34f,w*.91f,h*.68f,10f);p.color=Color.rgb(135,54,43);val roof=Path();roof.moveTo(w*.62f,h*.37f);roof.lineTo(w*.785f,h*.22f);roof.lineTo(w*.95f,h*.37f);roof.close();c.drawPath(roof,p);rr(c,Color.rgb(104,62,43),w*.745f,h*.49f,w*.835f,h*.68f,5f);p.color=Color.WHITE;p.strokeWidth=5f;p.style=Paint.Style.STROKE;c.drawLine(w*.75f,h*.50f,w*.83f,h*.67f,p);c.drawLine(w*.83f,h*.50f,w*.75f,h*.67f,p);p.style=Paint.Style.FILL}
    private fun drawTree(c:Canvas,w:Float,h:Float){rr(c,Color.rgb(116,73,43),w*.10f,h*.45f,w*.14f,h*.70f,8f);p.color=Color.rgb(55,151,70);c.drawCircle(w*.12f,h*.40f,w*.075f,p);c.drawCircle(w*.075f,h*.45f,w*.055f,p);c.drawCircle(w*.17f,h*.45f,w*.06f,p)}
    private fun drawFlowers(c:Canvas,w:Float,h:Float){val sway=sin(farmPhase*Math.PI*2).toFloat()*w*.004f;for(i in 0..4){val x=w*(.27f+i*.07f);val y=h*(.80f+(i%2)*.025f);p.color=Color.rgb(55,145,62);p.strokeWidth=3f;c.drawLine(x,y+h*.035f,x+sway,y,p);p.color=if(i%2==0)Color.rgb(255,115,166) else Color.rgb(255,222,70);c.drawCircle(x+sway,y,w*.012f,p);p.color=Color.WHITE;c.drawCircle(x+sway,y,w*.004f,p)}}
    private fun drawHayBales(c:Canvas,w:Float,h:Float){val y=h*.77f;for(i in 0..2){val x=w*(.53f+i*.065f);rr(c,Color.rgb(224,174,63),x,y-i*h*.015f,x+w*.075f,y+h*.075f-i*h*.015f,9f);p.color=Color.rgb(180,128,42);p.style=Paint.Style.STROKE;p.strokeWidth=3f;c.drawLine(x+w*.037f,y-i*h*.015f,x+w*.037f,y+h*.075f-i*h*.015f,p);p.style=Paint.Style.FILL}}
    private fun drawFarmSign(c:Canvas,w:Float,h:Float){rr(c,Color.rgb(133,83,47),w*.025f,h*.57f,w*.20f,h*.65f,9f);rr(c,Color.rgb(101,64,39),w*.065f,h*.64f,w*.08f,h*.82f,4f);p.color=Color.WHITE;p.textAlign=Paint.Align.CENTER;p.textSize=w*.030f;p.isFakeBoldText=true;c.drawText("FAZENDA",w*.112f,h*.622f,p);p.isFakeBoldText=false;p.textAlign=Paint.Align.LEFT}
    private fun drawClouds(c:Canvas,w:Float,h:Float){val drift=(farmPhase*w*.16f);val x=(w*.52f+drift)%(w*1.15f)-w*.08f;oval(c,Color.argb(235,255,255,255),x,h*.10f,x+w*.23f,h*.18f);oval(c,Color.argb(235,255,255,255),x+w*.09f,h*.07f,x+w*.33f,h*.18f)}
    private fun drawBirds(c:Canvas,w:Float,h:Float){p.style=Paint.Style.STROKE;p.strokeWidth=3f;p.color=Color.argb(130,45,61,70);val x=w*(.35f+.12f*farmPhase);val y=h*.20f;c.drawArc(x-w*.025f,y,x,y+h*.025f,205f,125f,false,p);c.drawArc(x,y,x+w*.025f,y+h*.025f,210f,125f,false,p);p.style=Paint.Style.FILL}
    private fun drawWindmill(c:Canvas,w:Float,h:Float){
        val x=w*.49f;val y=h*.43f
        p.color=Color.rgb(205,194,166);val tower=Path();tower.moveTo(x-w*.025f,h*.67f);tower.lineTo(x+w*.025f,h*.67f);tower.lineTo(x+w*.010f,y);tower.lineTo(x-w*.010f,y);tower.close();c.drawPath(tower,p)
        p.color=Color.rgb(118,83,58);c.drawCircle(x,y,w*.012f,p)
        p.style=Paint.Style.STROKE;p.strokeWidth=5f
        val a=farmPhase*6.28318f
        for(i in 0..3){val ang=a+i*1.5708f;val ex=x+kotlin.math.cos(ang)*w*.07f;val ey=y+kotlin.math.sin(ang)*w*.07f;c.drawLine(x,y,ex,ey,p)}
        p.style=Paint.Style.FILL
    }
    private fun drawButterflies(c:Canvas,w:Float,h:Float){
        for(i in 0..1){val phase=(farmPhase+i*.43f)%1f;val x=w*(.25f+.38f*phase);val y=h*(.46f+.025f*kotlin.math.sin(phase*12.56f+i));val s=w*.010f
            p.color=if(i==0)Color.rgb(255,188,64) else Color.rgb(237,111,177)
            c.drawOval(x-s*1.7f,y-s,x,y+s,p);c.drawOval(x,y-s,x+s*1.7f,y+s,p)
            p.color=Color.rgb(70,70,55);c.drawCircle(x,y,s*.30f,p)
        }
    }
    private fun drawPathToBarn(c:Canvas,w:Float,h:Float){
        p.color=Color.rgb(218,190,137);val path=Path();path.moveTo(w*.73f,h);path.lineTo(w*.77f,h*.66f);path.lineTo(w*.84f,h*.66f);path.lineTo(w*.94f,h);path.close();c.drawPath(path,p)
        p.color=Color.argb(70,130,92,55);for(i in 0..3){val y=h*(.72f+i*.065f);c.drawOval(w*(.79f+i*.018f),y,w*(.82f+i*.022f),y+h*.012f,p)}
    }
    private fun drawChicken(c:Canvas,x:Float,y:Float,w:Float){
        val s=w*.026f;p.color=Color.rgb(250,239,211);c.drawOval(x-s,y-s*.7f,x+s,y+s*.75f,p);c.drawCircle(x+s*.55f,y-s*.65f,s*.55f,p)
        p.color=Color.rgb(218,67,54);c.drawCircle(x+s*.42f,y-s*1.18f,s*.18f,p);c.drawCircle(x+s*.70f,y-s*1.15f,s*.16f,p)
        p.color=Color.rgb(244,170,48);val beak=Path();beak.moveTo(x+s*1.02f,y-s*.68f);beak.lineTo(x+s*1.45f,y-s*.52f);beak.lineTo(x+s*1.02f,y-s*.38f);beak.close();c.drawPath(beak,p)
        p.color=Color.rgb(55,61,60);c.drawCircle(x+s*.70f,y-s*.78f,s*.08f,p)
    }
    private fun drawFarmChickens(c:Canvas,w:Float,h:Float){
        val bob=kotlin.math.sin(farmPhase*12.56f)*h*.006f
        drawChicken(c,w*.37f,h*.77f+bob,w);drawChicken(c,w*.44f,h*.81f-bob,w*.82f)
    }
    private fun drawPig(c:Canvas,x:Float,y:Float,w:Float){
        val s=w*.034f;p.color=Color.rgb(246,166,177);c.drawOval(x-s*1.35f,y-s*.70f,x+s*1.20f,y+s*.70f,p);c.drawCircle(x+s*.92f,y-s*.30f,s*.62f,p)
        p.color=Color.rgb(238,139,154);c.drawOval(x+s*.80f,y-s*.18f,x+s*1.42f,y+s*.18f,p)
        p.color=Color.rgb(72,63,64);c.drawCircle(x+s*.78f,y-s*.47f,s*.08f,p);c.drawCircle(x+s*1.12f,y-s*.47f,s*.08f,p)
        p.color=Color.rgb(231,126,143);val ear=Path();ear.moveTo(x+s*.62f,y-s*.75f);ear.lineTo(x+s*.72f,y-s*1.25f);ear.lineTo(x+s*1.02f,y-s*.78f);ear.close();c.drawPath(ear,p)
        p.strokeWidth=4f;p.style=Paint.Style.STROKE;c.drawArc(x-s*1.65f,y-s*.35f,x-s*1.15f,y+s*.20f,80f,250f,false,p);p.style=Paint.Style.FILL
    }
    private fun drawFarmPig(c:Canvas,w:Float,h:Float){
        val bob=kotlin.math.sin(farmPhase*12.56f+1.7f)*h*.004f;drawPig(c,w*.57f,h*.82f+bob,w)
    }
    private fun drawLakeRipples(c:Canvas,x:Float,y:Float,scale:Float){p.style=Paint.Style.STROKE;p.strokeWidth=3f;p.color=Color.argb(125,255,255,255);val s=scale*.075f;c.drawOval(x-s,y-s*.20f,x+s,y+s*.20f,p);p.style=Paint.Style.FILL}
    private fun drawLakeBubbles(c:Canvas,x:Float,y:Float,scale:Float,alpha:Float){p.style=Paint.Style.STROKE;p.strokeWidth=3f;p.color=Color.argb((190*alpha).toInt(),255,255,255);val rise=scale*.07f*(1f-alpha);val s=scale*.012f;c.drawCircle(x-s*2.2f,y-rise,s*.55f,p);c.drawCircle(x+s*1.4f,y-rise-s*1.6f,s*.75f,p);c.drawCircle(x+s*3.1f,y-rise+s*.4f,s*.42f,p);p.style=Paint.Style.FILL;p.alpha=255}
    private fun drawLakeHeart(c:Canvas,x:Float,y:Float,scale:Float,alpha:Float){val s=scale*.018f;p.alpha=(255*alpha).toInt();p.color=Color.rgb(255,92,145);c.drawCircle(x-s*.55f,y,s*.62f,p);c.drawCircle(x+s*.55f,y,s*.62f,p);val heart=Path();heart.moveTo(x-s*1.15f,y+s*.15f);heart.lineTo(x,y+s*1.35f);heart.lineTo(x+s*1.15f,y+s*.15f);heart.close();c.drawPath(heart,p);p.alpha=255}
    override fun onTouchEvent(event:MotionEvent):Boolean{if(event.action==MotionEvent.ACTION_DOWN && sceneMode!="lake"){val nx=event.x/width.coerceAtLeast(1);val ny=event.y/height.coerceAtLeast(1);val animal=when{sceneMode!="coop" && nx in .50f..66f && ny in .72f..90f->"porco";nx in .30f..50f && ny in .68f..89f->"galinha";else->null};if(animal!=null){onFarmAnimalTap?.invoke(animal);animate().scaleX(1.018f).scaleY(1.018f).setDuration(85).withEndAction{animate().scaleX(1f).scaleY(1f).setDuration(120).start()}.start();performClick();return true};return true};if(sceneMode!="lake") return true;if(event.action==MotionEvent.ACTION_DOWN){splashX=(event.x/width.coerceAtLeast(1)).coerceIn(.08f,.92f);splashY=(event.y/height.coerceAtLeast(1)).coerceIn(.52f,.90f);lakeTapCount++;wildlifeBoost=(wildlifeBoost+.11f)%1f;lakeHearts=1f;lakeBubbles=1f;ValueAnimator.ofFloat(1f,0f).apply{duration=900;addUpdateListener{lakeHearts=it.animatedValue as Float;invalidate()};start()};ValueAnimator.ofFloat(1f,0f).apply{duration=1100;addUpdateListener{lakeBubbles=it.animatedValue as Float;invalidate()};start()};ValueAnimator.ofFloat(1f,0f).apply{duration=650;addUpdateListener{splashAlpha=it.animatedValue as Float;invalidate()};start()};animate().scaleX(1.008f).scaleY(1.008f).setDuration(70).withEndAction{animate().scaleX(1f).scaleY(1f).setDuration(110).start()}.start();performClick();return true};return true}
    override fun performClick():Boolean{super.performClick();return true}
    override fun onDetachedFromWindow(){lakeAnimator.cancel();ambienceAnimator.cancel();super.onDetachedFromWindow()}
    override fun onDraw(c:Canvas){super.onDraw(c);val w=width.toFloat();val h=height.toFloat();if(w<=0)return;p.shader=LinearGradient(0f,0f,0f,h,Color.rgb(92,198,240),Color.rgb(219,244,175),Shader.TileMode.CLAMP);c.drawRect(0f,0f,w,h,p);p.shader=null;p.color=Color.rgb(255,220,62);c.drawCircle(w*.12f,h*.15f,min(w,h)*.065f,p);drawClouds(c,w,h);drawBirds(c,w,h);p.color=Color.rgb(91,176,74);val hill=Path();hill.moveTo(0f,h*.48f);hill.quadTo(w*.28f,h*.34f,w*.56f,h*.49f);hill.quadTo(w*.8f,h*.35f,w,h*.47f);hill.lineTo(w,h);hill.lineTo(0f,h);hill.close();c.drawPath(hill,p);drawTree(c,w,h);if(sceneMode!="coop"){drawBarn(c,w,h);drawPathToBarn(c,w,h)};drawWindmill(c,w,h);drawFlowers(c,w,h);drawButterflies(c,w,h);if(sceneMode!="lake"){drawFarmChickens(c,w,h);drawFarmPig(c,w,h)};drawFarmSign(c,w,h);if(sceneMode!="lake")drawHayBales(c,w,h);when(sceneMode){"coop"->{rr(c,Color.rgb(230,173,76),w*.55f,h*.31f,w*.86f,h*.68f);p.color=Color.rgb(181,62,48);val roof=Path();roof.moveTo(w*.50f,h*.35f);roof.lineTo(w*.70f,h*.20f);roof.lineTo(w*.91f,h*.35f);roof.close();c.drawPath(roof,p);for(i in 0..2){oval(c,Color.WHITE,w*(.17f+i*.18f),h*.70f,w*(.24f+i*.18f),h*.77f);oval(c,Color.WHITE,w*(.18f+i*.18f),h*.53f,w*(.31f+i*.18f),h*.69f);p.color=Color.rgb(206,65,48);c.drawCircle(w*(.22f+i*.18f),h*.52f,10f,p)}};"lake"->{oval(c,Color.rgb(78,181,226),w*.08f,h*.52f,w*.92f,h*.90f);oval(c,Color.rgb(105,213,237),w*.16f,h*.60f,w*.83f,h*.82f);val motion=(lakePhase+wildlifeBoost)%1f;drawFish(c,w*(.40f+.16f*motion),h*.72f,w);drawFish(c,w*(.74f-.14f*motion),h*.78f,w*.72f);drawDuck(c,w*(.24f+.14f*motion),h*.64f,w);drawLily(c,w*.76f,h*.68f,w);drawLakeRipples(c,w*.30f,h*.70f,w);drawLakeRipples(c,w*.53f,h*.77f,w);if(splashAlpha>0f){p.alpha=(255*splashAlpha).toInt();drawLakeRipples(c,w*splashX,h*splashY,w*(1.15f-splashAlpha*.25f));p.alpha=255};if(lakeHearts>0f)drawLakeHeart(c,w*splashX,h*(splashY-.05f*(1f-lakeHearts)),w,lakeHearts);if(lakeBubbles>0f)drawLakeBubbles(c,w*splashX,h*splashY,w,lakeBubbles)};else->{rr(c,Color.rgb(121,76,40),w*.05f,h*.66f,w*.95f,h*.90f,12f);for(i in 0..5){val x=w*(.12f+i*.15f);p.color=Color.rgb(60,150,62);c.drawRect(x-4,h*.54f,x+4,h*.75f,p);c.drawCircle(x-15,h*.57f,17f,p);c.drawCircle(x+13,h*.60f,16f,p);p.color=Color.rgb(239,174,47);c.drawCircle(x,h*.54f,9f,p)}}};p.color=Color.rgb(220,176,102);for(i in 0..5){val x=i*w/5f;rr(c,p.color,x-5,h*.86f,x+10,h,5f)};rr(c,Color.rgb(191,139,72),0f,h*.91f,w,h*.95f,4f)}
}
