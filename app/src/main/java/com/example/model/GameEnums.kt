package com.example.model

enum class Season(val displayName: String, val description: String) {
  SPRING("Spring", "Mild weather. Crops grow +50% faster, rivers thaw."),
  SUMMER("Summer", "Warm sun. Plentiful harvest, high morale & trade."),
  AUTUMN("Autumn", "Chilly winds. Final harvest, stockpiling firewood."),
  WINTER("Winter", "Freezing cold! Crops wither, firewood needed for warmth.")
}

enum class Biome(
  val id: String,
  val displayName: String,
  val subtitle: String,
  val description: String,
  val baseWood: Int,
  val baseStone: Int,
  val baseIron: Int
) {
  RIVERLAND(
    "riverland",
    "Riverland Valley",
    "Fertile Heartland",
    "Lush green meadows and a winding river with abundant fish and fast crop growth.",
    baseWood = 60,
    baseStone = 40,
    baseIron = 20
  ),
  HIGHLAND(
    "highland",
    "Highland Duchy",
    "Mountain Fortress",
    "Rugged crags rich in stone & iron ore, with narrow passes and harsh winter blizzards.",
    baseWood = 45,
    baseStone = 70,
    baseIron = 40
  ),
  DESERT_OASIS(
    "desert_oasis",
    "Sultan's Oasis",
    "Golden Dunes",
    "Arid sands centered around a flourishing palm oasis with rich gold trade caravans.",
    baseWood = 35,
    baseStone = 50,
    baseIron = 30
  ),
  BOREAL_FJORD(
    "boreal_fjord",
    "Boreal Fjord",
    "Northern Wilds",
    "Dense pine wilderness and icy fjords with Viking raider incursions and deep timber reserves.",
    baseWood = 80,
    baseStone = 40,
    baseIron = 25
  )
}

enum class ResourceType(val displayName: String, val iconSymbol: String) {
  GOLD("Gold", "🪙"),
  FOOD("Food", "🍞"),
  WOOD("Wood", "🪵"),
  PLANKS("Planks", "🪵"),
  STONE("Stone", "🪨"),
  IRON("Iron", "⛏️"),
  WHEAT("Wheat", "🌾"),
  WEAPONS("Weapons", "⚔️"),
  FIREWOOD("Firewood", "🔥")
}

enum class BuildingCategory(val displayName: String) {
  CIVIC("Civic & Housing"),
  FOOD("Food & Farming"),
  INDUSTRY("Gathering & Industry"),
  MILITARY("Defense & War")
}

enum class BuildingType(
  val displayName: String,
  val category: BuildingCategory,
  val description: String,
  val goldCost: Int,
  val woodCost: Int,
  val stoneCost: Int,
  val maxWorkers: Int,
  val upkeepGold: Int = 1
) {
  CASTLE(
    "King's Castle",
    BuildingCategory.CIVIC,
    "Seat of the sovereign. Collects taxes and houses the royal council.",
    goldCost = 0,
    woodCost = 0,
    stoneCost = 0,
    maxWorkers = 2
  ),
  HOUSE(
    "Townie Cottage",
    BuildingCategory.CIVIC,
    "Houses up to 5 citizens. Generates tax revenue every season.",
    goldCost = 30,
    woodCost = 25,
    stoneCost = 10,
    maxWorkers = 0
  ),
  STATUE(
    "Royal Monument",
    BuildingCategory.CIVIC,
    "Inspires townie pride, granting +15% approval in royal elections.",
    goldCost = 120,
    woodCost = 20,
    stoneCost = 60,
    maxWorkers = 0
  ),
  SHEEP_FARM(
    "Sheep Pasture",
    BuildingCategory.FOOD,
    "Fenced meadow with grazing sheep. Generates wool and food for the settlement.",
    goldCost = 45,
    woodCost = 35,
    stoneCost = 5,
    maxWorkers = 1
  ),
  MARKET_STALL(
    "Market Square",
    BuildingCategory.CIVIC,
    "Striped vendor awnings where townies buy provisions, boosting happiness & gold.",
    goldCost = 60,
    woodCost = 40,
    stoneCost = 15,
    maxWorkers = 1
  ),
  TOWN_WELL(
    "Village Well",
    BuildingCategory.CIVIC,
    "Fresh communal water well. Townies gather here to chat and drink.",
    goldCost = 25,
    woodCost = 10,
    stoneCost = 20,
    maxWorkers = 0
  ),
  WHEAT_FARM(
    "Wheat Farm",
    BuildingCategory.FOOD,
    "Cultivates wheat fields across Spring, Summer, and Autumn.",
    goldCost = 35,
    woodCost = 30,
    stoneCost = 5,
    maxWorkers = 2
  ),
  WINDMILL(
    "Windmill & Bakery",
    BuildingCategory.FOOD,
    "Mills harvested wheat into flour and bakes hearty loaves of bread.",
    goldCost = 50,
    woodCost = 40,
    stoneCost = 20,
    maxWorkers = 2
  ),
  FISHERMAN(
    "Fisherman's Hut",
    BuildingCategory.FOOD,
    "Catches fresh fish from nearby rivers, providing food year-round.",
    goldCost = 40,
    woodCost = 35,
    stoneCost = 5,
    maxWorkers = 2
  ),
  TAVERN(
    "Tavern & Brewery",
    BuildingCategory.FOOD,
    "Serves frothy ale to citizens. Dramatically boosts happiness & morale.",
    goldCost = 70,
    woodCost = 50,
    stoneCost = 25,
    maxWorkers = 2
  ),
  LUMBERJACK(
    "Lumberjack Lodge",
    BuildingCategory.INDUSTRY,
    "Chops timber from surrounding forests for construction and fuel.",
    goldCost = 25,
    woodCost = 10,
    stoneCost = 0,
    maxWorkers = 2
  ),
  SAWMILL(
    "Sawmill & Woodchopper",
    BuildingCategory.INDUSTRY,
    "Processes raw timber into refined planks and vital winter firewood.",
    goldCost = 55,
    woodCost = 40,
    stoneCost = 15,
    maxWorkers = 2
  ),
  QUARRY(
    "Stone Quarry",
    BuildingCategory.INDUSTRY,
    "Mines stone blocks from nearby mountain rock deposits.",
    goldCost = 45,
    woodCost = 30,
    stoneCost = 10,
    maxWorkers = 2
  ),
  IRON_MINE(
    "Iron Mine & Forge",
    BuildingCategory.INDUSTRY,
    "Extracts iron ore and smelts it into ingots for weapons.",
    goldCost = 80,
    woodCost = 45,
    stoneCost = 35,
    maxWorkers = 2
  ),
  WATCHTOWER(
    "Archer Watchtower",
    BuildingCategory.MILITARY,
    "Automated archers shoot incoming raiders and bandits on sight.",
    goldCost = 65,
    woodCost = 35,
    stoneCost = 30,
    maxWorkers = 1
  ),
  BARRACKS(
    "Royal Barracks",
    BuildingCategory.MILITARY,
    "Trains and equips swordsmen, archers, and heavy knights.",
    goldCost = 90,
    woodCost = 60,
    stoneCost = 45,
    maxWorkers = 3
  ),
  ROAD(
    "Cobblestone Road",
    BuildingCategory.CIVIC,
    "Paved pathway. Townies walk 60% faster along roads.",
    goldCost = 5,
    woodCost = 2,
    stoneCost = 4,
    maxWorkers = 0
  )
}

enum class UnitType(
  val displayName: String,
  val maxHp: Int,
  val attackDamage: Int,
  val isHostile: Boolean,
  val goldCost: Int = 0,
  val weaponCost: Int = 0
) {
  TOWNIE("Townie", maxHp = 40, attackDamage = 5, isHostile = false),
  SPEARMAN("Militia Spearman", maxHp = 80, attackDamage = 18, isHostile = false, goldCost = 25, weaponCost = 1),
  ARCHER("Longbow Archer", maxHp = 60, attackDamage = 22, isHostile = false, goldCost = 35, weaponCost = 1),
  KNIGHT("Royal Knight", maxHp = 140, attackDamage = 35, isHostile = false, goldCost = 60, weaponCost = 2),
  RAIDER("Bandit Raider", maxHp = 70, attackDamage = 16, isHostile = true),
  RAIDER_CHIEFTAIN("Bandit Warlord", maxHp = 180, attackDamage = 32, isHostile = true)
}

enum class GameMode {
  SANDBOX,
  SCENARIO
}

enum class ScenarioId(
  val title: String,
  val subtitle: String,
  val goalDescription: String,
  val targetSeasonGoal: Season? = null
) {
  LONG_WINTER(
    "The Great Frost",
    "Survival Challenge",
    "Survive through the freezing Winter with at least 150 Firewood, 100 Bread, and no lost cottages."
  ),
  BANDIT_SIEGE(
    "Siege of the Bandit Lord",
    "Military Challenge",
    "Build at least 2 Watchtowers, recruit 6 Soldiers, and defeat 3 incoming waves of Bandit Raiders."
  ),
  ROYAL_ELECTION(
    "The Sovereign's Mandate",
    "Political Challenge",
    "Achieve and maintain at least 85% popular happiness to win the year-end democratic Council Election."
  ),
  METROPOLIS(
    "Grand Medieval Empire",
    "Economic Challenge",
    "Reach a population of 40 Townies, construct all production buildings, and amass 800 Gold Thalers."
  )
}
