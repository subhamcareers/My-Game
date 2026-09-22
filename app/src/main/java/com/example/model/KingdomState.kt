package com.example.model

enum class TaxRate(val label: String, val taxPerCitizen: Int, val happinessImpact: Int) {
  GENEROUS("Generous (0%)", 0, +25),
  LOW("Low (5%)", 1, +10),
  NORMAL("Normal (15%)", 3, 0),
  HIGH("Heavy (30%)", 6, -15),
  EXTORTIONATE("Extortionate (50%)", 10, -35)
}

enum class DecreeType(
  val title: String,
  val description: String,
  val goldCost: Int,
  val durationDays: Int,
  val iconEmoji: String
) {
  FEAST_FESTIVAL("Grand Harvest Feast", "+25% Townie happiness for 15 days.", goldCost = 60, durationDays = 15, "🍗"),
  MILITIA_CALL("Call-to-Arms", "Instantly conscripts 2 volunteer Spearmen to guard the realm.", goldCost = 40, durationDays = 0, "⚔️"),
  WINTER_WARMTH_RELIEF("Winter Fuel Subsidy", "Reduces winter firewood consumption by 50% for 20 days.", goldCost = 50, durationDays = 20, "🔥"),
  TRADE_CARAVAN_EMBARGO("Royal Trade Treaty", "Boosts all tax income by +40% for 15 days.", goldCost = 80, durationDays = 15, "📜")
}

data class ActiveDecree(
  val decree: DecreeType,
  var daysRemaining: Int
)

data class KingdomResources(
  var gold: Int = 150,
  var food: Int = 80,
  var wood: Int = 80,
  var planks: Int = 20,
  var stone: Int = 40,
  var iron: Int = 15,
  var wheat: Int = 30,
  var weapons: Int = 4,
  var firewood: Int = 50
)

data class RaidEvent(
  val isWarningActive: Boolean = false,
  val secondsUntilRaid: Int = 0,
  val isActive: Boolean = false,
  val waveNumber: Int = 1,
  val raiderCount: Int = 0
)

data class ScenarioProgress(
  val scenarioId: ScenarioId,
  var isComplete: Boolean = false,
  var survivedWinterDays: Int = 0,
  var defeatedRaids: Int = 0,
  var peakHappiness: Float = 0f,
  var peakGold: Int = 0
)
