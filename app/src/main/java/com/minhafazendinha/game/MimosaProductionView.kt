package com.minhafazendinha.game

import android.content.Context
import android.graphics.Color
import android.graphics.drawable.Drawable
import android.view.Gravity
import android.widget.FrameLayout
import android.widget.ImageView

/** Cena visual da Mimosa: preview aprovado enquanto os layers reais nao chegaram. */
class MimosaProductionView(context: Context) : FrameLayout(context) {
    private val reference=layer(ImageView.ScaleType.CENTER_CROP);private val background=layer(ImageView.ScaleType.CENTER_CROP);private val character=layer(ImageView.ScaleType.CENTER_INSIDE);private val foreground=layer(ImageView.ScaleType.CENTER_CROP)
    private var visualState=CareVisualState.IDLE
    init{setBackgroundColor(Color.TRANSPARENT);addView(reference,LayoutParams(-1,-1));addView(background,LayoutParams(-1,-1));addView(character,LayoutParams(-1,-1).apply{gravity=Gravity.CENTER});addView(foreground,LayoutParams(-1,-1));render()}
    fun setCareState(state:Int){visualState=when(state.coerceIn(0,3)){0->CareVisualState.FEED;1->CareVisualState.BATHE;2->CareVisualState.BRUSH;else->CareVisualState.IDLE};render()}
    fun setCareAction(state:CareVisualState){visualState=state;render();character.animate().cancel();character.scaleX=.96f;character.scaleY=.96f;character.alpha=.72f;character.animate().scaleX(1f).scaleY(1f).alpha(1f).setDuration(180).start()}
    fun showIdle(){visualState=CareVisualState.IDLE;render()}
    fun hasVisualPreview()=drawable(ProductionVisuals.mimosa.referenceAsset)!=null
    fun hasLayeredProduction():Boolean{val s=ProductionVisuals.mimosa;return drawable(s.backgroundAsset)!=null&&drawable(s.characterAsset)!=null}
    fun hasProductionAssets()=hasVisualPreview()||hasLayeredProduction()
    private fun render(){val s=ProductionVisuals.mimosa;val layered=hasLayeredProduction();reference.visibility=if(layered)GONE else VISIBLE;reference.setImageDrawable(if(layered)null else drawable(s.referenceAsset));background.setImageDrawable(if(layered)drawable(s.backgroundAsset)else null);foreground.setImageDrawable(if(layered)drawable(s.foregroundAsset)else null);val key=when(visualState){CareVisualState.FEED->"feed";CareVisualState.BATHE->"bath";CareVisualState.BRUSH->"brush";CareVisualState.PLAY->"play";CareVisualState.IDLE->null};val asset=key?.let{s.interactionAssets[it]};character.setImageDrawable(if(layered)drawable(asset?:s.characterAsset)?:drawable(s.characterAsset)else null)}
    private fun layer(scale:ImageView.ScaleType)=ImageView(context).apply{scaleType=scale;adjustViewBounds=false}
    private fun drawable(name:String):Drawable?{val id=resources.getIdentifier(name,"drawable",context.packageName);return if(id==0)null else runCatching{context.getDrawable(id)}.getOrNull()}
}
