package com.example.model

import java.util.UUID

enum class TownieJob(val title: String, val iconEmoji: String) {
  UNASSIGNED("Villager", "🧑"),
  FARMER("Farmer", "🌾"),
  WOODCUTTER("Lumberjack", "🪓"),
  BAKER("Miller & Baker", "🍞"),
  MINER("Quarryman", "⛏️"),
  SMITH("Blacksmith", "🔨"),
  FISHER("Fisherman", "🎣"),
  TAVERN_KEEPER("Innkeeper", "🍺"),
  GUARD("Town Guard", "🛡️"),
  SHEPHERD("Shepherd", "🐑")
}

enum class TownieState {
  IDLE,
  WALKING_TO_WORK,
  WORKING,
  HAULING_CARGO,
  RESTING_AT_TAVERN,
  FETCHING_WATER,
  SLEEPING_AT_HOME,
  FLEEING_ALARM
}

data class TownieUnit(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  var currentPos: GridPos,
  var worldX: Float = currentPos.x.toFloat(),
  var worldY: Float = currentPos.y.toFloat(),
  var targetPos: GridPos? = null,
  var homeBuildingId: String? = null,
  var workBuildingId: String? = null,
  var job: TownieJob = TownieJob.UNASSIGNED,
  var state: TownieState = TownieState.IDLE,
  var happiness: Float = 85f,
  var hunger: Float = 0f,
  var warmth: Float = 100f,
  var carriedResource: ResourceType? = null,
  var carriedAmount: Int = 0,
  var speechText: String? = "Good morrow, Sire!",
  var speechTimer: Float = 4f,
  var animTick: Float = 0f,
  var facingLeft: Boolean = false,
  val tunicColorHex: Long = 0xFF5D4037,
  val isFemale: Boolean = false
)

data class PastureSheep(
  val id: String = UUID.randomUUID().toString(),
  var worldX: Float,
  var worldY: Float,
  var targetX: Float = worldX,
  var targetY: Float = worldY,
  var chewAnim: Float = 0f,
  var woolGrown: Float = 1f
)

data class MilitaryUnit(
  val id: String = UUID.randomUUID().toString(),
  val name: String,
  val type: UnitType,
  var currentPos: GridPos,
  var worldX: Float = currentPos.x.toFloat(),
  var worldY: Float = currentPos.y.toFloat(),
  var targetPos: GridPos? = null,
  var hp: Int = type.maxHp,
  var maxHp: Int = type.maxHp,
  var attackDamage: Int = type.attackDamage,
  var isHostile: Boolean = type.isHostile,
  var attackCooldown: Float = 0f,
  var isFighting: Boolean = false,
  var slashEffectAnim: Float = 0f,
  var facingLeft: Boolean = false
)

data class ArrowProjectile(
  val id: String = UUID.randomUUID().toString(),
  val startPos: GridPos,
  val targetPos: GridPos,
  var progress: Float = 0f // 0f to 1f
)

