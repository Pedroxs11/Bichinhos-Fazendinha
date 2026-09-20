package com.minhafazendinha.game

/** Reusable definition layer for virtual-pet games. */
data class CarePetDefinition(
    val id: String,
    val name: String,
    val emoji: String,
    val level: Int = 1,
    val soundKey: String,
    val initialStats: CareStats = CareStats(),
    val actions: List<CareActionDefinition>,
    val visual: CareVisualDefinition = CareVisualDefinition(),
    val assetPack: CareGameAssetPack? = null
)

data class CareStats(val hunger:Int=80,val hygiene:Int=70,val happiness:Int=75,val energy:Int=90,val coins:Int=120)
data class CareDelta(val hunger:Int=0,val hygiene:Int=0,val happiness:Int=0,val energy:Int=0,val coins:Int=0)
data class CareActionDefinition(val id:String,val icon:String,val label:String,val message:String,val buttonColor:Int,val delta:CareDelta)

/** Art is addressed by stable keys so generated/3D assets can be swapped without touching game logic. */
data class CareVisualDefinition(
    val sceneKey:String="farm_day",
    val idleAssetKey:String="idle",
    val reactionAssetKeys:Map<String,String> = emptyMap(),
    val skyTop:Int=0xFF87D8FF.toInt(),
    val skyBottom:Int=0xFFE8F8D8.toInt(),
    val panelColor:Int=0xEEFFFFFF.toInt(),
    val inkColor:Int=0xFF49372D.toInt()
){ fun assetFor(actionId:String)=reactionAssetKeys[actionId] ?: idleAssetKey }

data class CareFactoryReadiness(
    val games: Map<String, CareGameAssetPackReadiness>
) {
    val ready: Boolean get() = games.values.all { it.ready }
    val missing: Set<String> get() = games.values.flatMap { it.missing }.toSet()
    val progress: Int get() = if (games.isEmpty()) 100 else games.values.map { it.progress }.average().toInt()
}

object CareGameFactory {
    val mimosa = CarePetDefinition(
        id="mimosa", name="Mimosa", emoji="🐮", soundKey="vaca",
        actions=listOf(
            CareActionDefinition("feed","🍎","Alimentar","Muuu! Que delícia!",0xFFFF625C.toInt(),CareDelta(hunger=10,coins=2)),
            CareActionDefinition("bathe","🚿","Banho","Muuu! Estou limpinha!",0xFF55B8FF.toInt(),CareDelta(hygiene=15,coins=2)),
            CareActionDefinition("brush","🧹","Escovar","Que carinho gostoso!",0xFFFFC83D.toInt(),CareDelta(happiness=8,coins=1)),
            CareActionDefinition("play","🏐","Brincar","Muuu! Vamos brincar!",0xFFFF65B7.toInt(),CareDelta(happiness=12,energy=-8,coins=2))
        ),
        visual=CareVisualDefinition(
            sceneKey="mimosa_paddock",
            idleAssetKey="mimosa_idle",
            reactionAssetKeys=mapOf("feed" to "mimosa_feed","bathe" to "mimosa_bathe","brush" to "mimosa_brush","play" to "mimosa_play")
        ),
        assetPack=CareGameAssetPack.MIMOSA
    )

    /** Registry is the single entry point for future animals/games. */
    private val pets = linkedMapOf(mimosa.id to mimosa)

    fun pet(id: String): CarePetDefinition? = pets[id.lowercase()]
    fun requirePet(id: String): CarePetDefinition = pet(id) ?: error("Unknown care pet: $id")
    fun allPets(): List<CarePetDefinition> = pets.values.toList()

    fun register(pet: CarePetDefinition) {
        require(pet.id.isNotBlank()) { "Care pet id cannot be blank" }
        pets[pet.id.lowercase()] = pet
    }

    fun assetPack(id: String): CareGameAssetPack? = pet(id)?.assetPack

    fun drawableManifest(id: String): Map<String,String> {
        val pack = requirePet(id).assetPack ?: return emptyMap()
        return linkedMapOf<String,String>().apply {
            put("scene_background", pack.background)
            pack.foreground?.let { put("scene_foreground", it) }
            putAll(pack.contract.drawableManifest())
            pack.actionCharacters.forEach { (action, drawable) -> put("character_$action", drawable) }
        }
    }

    /** One deterministic manifest for art export/import tooling across every registered game. */
    fun drawableManifestAll(): Map<String,String> = linkedMapOf<String,String>().apply {
        allPets().forEach { pet ->
            drawableManifest(pet.id).forEach { (slot, drawable) -> put("${pet.id}.$slot", drawable) }
        }
    }

    fun readiness(id: String, availableDrawables: Set<String>): CareGameAssetPackReadiness? =
        assetPack(id)?.readiness(availableDrawables)

    /** CI/art tooling can validate every game at once before packaging visual assets. */
    fun validateAll(availableDrawables: Set<String>): CareFactoryReadiness {
        val games = allPets().mapNotNull { pet ->
            readiness(pet.id, availableDrawables)?.let { pet.id to it }
        }.toMap()
        return CareFactoryReadiness(games)
    }

    fun stateFor(pet: CarePetDefinition) = MimosaCareState(
        pet.initialStats.hunger,
        pet.initialStats.hygiene,
        pet.initialStats.happiness,
        pet.initialStats.energy,
        pet.initialStats.coins
    )
}
