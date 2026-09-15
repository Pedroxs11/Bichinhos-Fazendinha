package com.minhafazendinha.game

import android.content.Context
import android.media.AudioAttributes
import android.media.SoundPool
import android.os.Handler
import android.os.Looper

/** Reprodutor dos sons reais dos bichinhos. Os arquivos originais permanecem intactos. */
class AnimalSoundPlayer(context: Context) {
    private val pool = SoundPool.Builder().setMaxStreams(1).setAudioAttributes(
        AudioAttributes.Builder().setUsage(AudioAttributes.USAGE_GAME).setContentType(AudioAttributes.CONTENT_TYPE_SONIFICATION).build()
    ).build()
    private val sounds=mutableMapOf<String,Int>();private val loaded=mutableSetOf<Int>();private var pending:Int?=null;private var activeStream=0
    private val handler=Handler(Looper.getMainLooper());private val scheduled=mutableListOf<Runnable>();private var released=false
    init{
        pool.setOnLoadCompleteListener{_,id,status->if(status==0&&!released){loaded+=id;if(pending==id){pending=null;playSample(id)}}}
        listOf("sound_cow","sound_chicken","sound_dog","sound_donkey","sound_goat","sound_horse","sound_pig","sound_sheep").forEach{name->val id=context.resources.getIdentifier(name,"raw",context.packageName);if(id!=0)sounds[name]=pool.load(context,id,1)}
    }
    private fun key(animal:String)=when(animal.lowercase()){ "vaca"->"sound_cow";"galinha","pintinho"->"sound_chicken";"cachorro"->"sound_dog";"burro"->"sound_donkey";"cabra"->"sound_goat";"cavalo"->"sound_horse";"porco"->"sound_pig";"ovelha"->"sound_sheep";else->null }
    fun play(animal:String){cancelSequence();playKey(key(animal)?:return)}
    /** Repete o mesmo som aprovado com intervalo folgado, sem sobrepor uma reprodução na outra. */
    fun playExtended(animal:String){cancelSequence();val k=key(animal)?:return;playKey(k);listOf(1800L,3600L).forEach{delay->val r=Runnable{if(!released){stopActive();playKey(k)}};scheduled+=r;handler.postDelayed(r,delay)}}
    fun stop(){cancelSequence()}
    private fun playKey(k:String){val sample=sounds[k]?:return;if(sample in loaded)playSample(sample)else pending=sample}
    private fun playSample(sample:Int){if(!released){stopActive();activeStream=pool.play(sample,1f,1f,1,0,1f)}}
    private fun stopActive(){if(activeStream!=0){pool.stop(activeStream);activeStream=0}}
    private fun cancelSequence(){scheduled.forEach{handler.removeCallbacks(it)};scheduled.clear();pending=null;stopActive()}
    fun release(){cancelSequence();released=true;pool.release()}
}
