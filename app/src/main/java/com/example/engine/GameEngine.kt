package com.example.engine

import com.example.model.*
import kotlin.math.max
import kotlin.math.min
import kotlin.random.Random

class GameEngine(
  var biome: Biome = Biome.RIVERLAND,
  var gameMode: GameMode = GameMode.SANDBOX,
  var currentScenario: ScenarioId? = null
) {
  var tiles: Array<Array<MapTile>>
  val buildings = mutableListOf<BuildingInstance>()
  val townies = mutableListOf<TownieUnit>()
  val militaryUnits = mutableListOf<MilitaryUnit>()
  val projectiles = mutableListOf<ArrowProjectile>()
  val resources: KingdomResources

  var currentSeason: Season = Season.SPRING
  var seasonDay: Int = 1
  var yearCount: Int = 1
  var totalDays: Int = 1
  var dayTickProgress: Float = 0f // 0f to 1f

  var taxRate: TaxRate = TaxRate.NORMAL
  var activeDecrees = mutableListOf<ActiveDecree>()
  var popularApprovalRating: Float = 85f
  var electionDaysRemaining: Int = 100 // Occurs yearly (4 * 25 days)
  var lastElectionResult: String? = null

  // Raids
  var raidCountdownSeconds: Int = 75
  var isRaidWarningActive: Boolean = false
  var isRaidActive: Boolean = false
  var raidWaveCount: Int = 0
  var raiderCountAlive: Int = 0

  // Alert notification
  var bannerMessage: String? = "Welcome to your medieval realm, Sire!"
  var bannerTimerSeconds: Float = 5f

  // Scenario tracker
  val scenarioProgress = ScenarioProgress(currentScenario ?: ScenarioId.METROPOLIS)

  init {
    val gen = MapGenerator.generate(biome)
    tiles = gen.tiles
    buildings.addAll(gen.initialBuildings)
    townies.addAll(gen.initialTownies)
    militaryUnits.addAll(gen.initialMilitary)
    resources = gen.startingResources
  }

  fun update(deltaTime: Float, gameSpeed: Float) {
    if (gameSpeed <= 0f) return
    val scaledDelta = deltaTime * gameSpeed

    // 1. Alert banner countdown
    if (bannerTimerSeconds > 0) {
      bannerTimerSeconds -= scaledDelta
      if (bannerTimerSeconds <= 0) bannerMessage = null
    }

    // 2. Day & Season Clock
    dayTickProgress += scaledDelta * 0.35f // ~3 seconds per in-game day
    if (dayTickProgress >= 1f) {
      dayTickProgress -= 1f
      onNewDay()
    }

    // 3. Raid Timer
    updateRaidTimer(scaledDelta)

    // 4. Production & Buildings Update
    updateBuildings(scaledDelta)

    // 5. Townies Movement & Needs
    updateTownies(scaledDelta)

    // 6. Military & Combat
    updateCombat(scaledDelta)

    // 7. Projectiles (Arrows)
    updateProjectiles(scaledDelta)

    // 8. Scenario check
    checkScenarioGoals()
  }

  private fun onNewDay() {
    totalDays++
    seasonDay++
    electionDaysRemaining--

    if (seasonDay > 25) {
      seasonDay = 1
      advanceSeason()
    }

    // Decrees countdown
    val it = activeDecrees.iterator()
    while (it.hasNext()) {
      val d = it.next()
      d.daysRemaining--
      if (d.daysRemaining <= 0) {
        it.remove()
        setBanner("Decree '${d.decree.title}' has expired.")
      }
    }

    // Daily consumption
    val population = townies.size
    val foodConsumed = max(1, (population * 0.4f).toInt())
    if (resources.food >= foodConsumed) {
      resources.food -= foodConsumed
      for (t in townies) t.hunger = max(0f, t.hunger - 10f)
    } else {
      resources.food = 0
      for (t in townies) {
        t.hunger = min(100f, t.hunger + 25f)
        t.happiness = max(5f, t.happiness - 15f)
      }
      setBanner("Warning: Citizens are starving! Food supplies depleted.")
    }

    // Winter firewood warmth consumption
    if (currentSeason == Season.WINTER) {
      val hasFuelRelief = activeDecrees.any { it.decree == DecreeType.WINTER_WARMTH_RELIEF }
      val woodBurned = max(1, (population * (if (hasFuelRelief) 0.25f else 0.5f)).toInt())
      if (resources.firewood >= woodBurned) {
        resources.firewood -= woodBurned
        for (t in townies) t.warmth = min(100f, t.warmth + 15f)
      } else {
        resources.firewood = 0
        for (t in townies) {
          t.warmth = max(0f, t.warmth - 30f)
          t.happiness = max(10f, t.happiness - 20f)
        }
        setBanner("The bitter Winter freeze bites! Stockpile Firewood in sawmills.")
      }
    } else {
      for (t in townies) t.warmth = 100f
    }

    // Taxes collected every 5 days
    if (totalDays % 5 == 0) {
      val hasTradeTreaty = activeDecrees.any { it.decree == DecreeType.TRADE_CARAVAN_EMBARGO }
      var taxGold = population * taxRate.taxPerCitizen
      if (hasTradeTreaty) taxGold = (taxGold * 1.4f).toInt()
      resources.gold += taxGold
    }

    // Immigration: New citizens join if housing available & happiness >= 60%
    val maxPop = 4 + buildings.count { it.type == BuildingType.HOUSE && it.hp > 0 } * 5
    val avgHappiness = getAverageHappiness()
    if (population < maxPop && avgHappiness >= 60f && Random.nextFloat() < 0.25f) {
      spawnNewCitizen()
    }

    // Calculate election approval
    calculateApproval()

    // Royal Election check
    if (electionDaysRemaining <= 0) {
      conductElection()
      electionDaysRemaining = 100
    }
  }

  private fun advanceSeason() {
    currentSeason = when (currentSeason) {
      Season.SPRING -> Season.SUMMER
      Season.SUMMER -> Season.AUTUMN
      Season.AUTUMN -> Season.WINTER
      Season.WINTER -> {
        yearCount++
        Season.SPRING
      }
    }
    setBanner("A new season begins: ${currentSeason.displayName}! ${currentSeason.description}")
  }

  private fun calculateApproval() {
    val avgHappiness = getAverageHappiness()
    var approval = avgHappiness * 0.75f + taxRate.happinessImpact
    val hasStatue = buildings.any { it.type == BuildingType.STATUE && it.hp > 0 }
    if (hasStatue) approval += 15f
    if (activeDecrees.any { it.decree == DecreeType.FEAST_FESTIVAL }) approval += 15f
    popularApprovalRating = approval.coerceIn(10f, 100f)
  }

  private fun conductElection() {
    val passed = popularApprovalRating >= 50f
    if (passed) {
      resources.gold += 120
      lastElectionResult = "Re-elected! The populace rejoices with ${popularApprovalRating.toInt()}% approval."
      setBanner("The Royal Council has re-elected you as Sovereign! (+120 Gold)")
    } else {
      lastElectionResult = "Council Censured! Approval fell to ${popularApprovalRating.toInt()}%. Strikes in progress."
      setBanner("Civil Unrest: Low approval rating led to popular protests! Lower taxes or hold a Feast.")
    }
  }

  private fun updateRaidTimer(deltaTime: Float) {
    if (!isRaidActive) {
      raidCountdownSeconds = max(0, raidCountdownSeconds - 1)
      if (raidCountdownSeconds in 1..20 && !isRaidWarningActive) {
        isRaidWarningActive = true
        setBanner("Horn alert! Barbarian raiders spotted scouting your borders ($raidCountdownSeconds s)!")
      } else if (raidCountdownSeconds <= 0) {
        startRaidWave()
      }
    }
  }

  private fun startRaidWave() {
    isRaidActive = true
    isRaidWarningActive = false
    raidWaveCount++
    val raidersToSpawn = min(12, 3 + raidWaveCount * 2)
    raiderCountAlive = raidersToSpawn

    // Spawn at random border
    val spawnBorder = Random.nextInt(4)
    for (i in 0 until raidersToSpawn) {
      val spawnPos = when (spawnBorder) {
        0 -> GridPos(Random.nextInt(MapGenerator.MAP_SIZE), 0)
        1 -> GridPos(Random.nextInt(MapGenerator.MAP_SIZE), MapGenerator.MAP_SIZE - 1)
        2 -> GridPos(0, Random.nextInt(MapGenerator.MAP_SIZE))
        else -> GridPos(MapGenerator.MAP_SIZE - 1, Random.nextInt(MapGenerator.MAP_SIZE))
      }
      val isChieftain = (i == 0 && raidWaveCount >= 2)
      militaryUnits.add(
        MilitaryUnit(
          name = if (isChieftain) "Warlord Rogar" else "Raider",
          type = if (isChieftain) UnitType.RAIDER_CHIEFTAIN else UnitType.RAIDER,
          currentPos = spawnPos,
          hp = if (isChieftain) UnitType.RAIDER_CHIEFTAIN.maxHp else UnitType.RAIDER.maxHp,
          maxHp = if (isChieftain) UnitType.RAIDER_CHIEFTAIN.maxHp else UnitType.RAIDER.maxHp,
          isHostile = true
        )
      )
    }
    setBanner("WAR DECLARED! A raiding party of $raidersToSpawn barbarians has breached the realm!")
  }

  private fun updateBuildings(deltaTime: Float) {
    for (b in buildings) {
      if (!b.isActive || b.hp <= 0) continue

      val workerMultiplier = if (b.maxWorkers > 0) (b.assignedWorkers.toFloat() / b.maxWorkers) else 1f
      if (workerMultiplier <= 0f) continue

      when (b.type) {
        BuildingType.LUMBERJACK -> {
          b.productionProgress += deltaTime * 0.12f * workerMultiplier
          if (b.productionProgress >= 1f) {
            b.productionProgress = 0f
            resources.wood += 2
            b.totalProduced += 2
          }
        }

        BuildingType.SAWMILL -> {
          if (resources.wood >= 2) {
            b.productionProgress += deltaTime * 0.10f * workerMultiplier
            if (b.productionProgress >= 1f) {
              b.productionProgress = 0f
              resources.wood -= 2
              resources.planks += 1
              resources.firewood += 3
              b.totalProduced += 1
            }
          }
        }

        BuildingType.WHEAT_FARM -> {
          // Cannot grow wheat in Winter
          if (currentSeason != Season.WINTER) {
            val seasonBonus = if (currentSeason == Season.SPRING) 1.5f else 1.0f
            b.productionProgress += deltaTime * 0.11f * seasonBonus * workerMultiplier
            if (b.productionProgress >= 1f) {
              b.productionProgress = 0f
              resources.wheat += 3
              b.totalProduced += 3
            }
          }
        }

        BuildingType.WINDMILL -> {
          if (resources.wheat >= 2) {
            b.productionProgress += deltaTime * 0.12f * workerMultiplier
            if (b.productionProgress >= 1f) {
              b.productionProgress = 0f
              resources.wheat -= 2
              resources.food += 5
              b.totalProduced += 5
            }
          }
        }

        BuildingType.FISHERMAN -> {
          b.productionProgress += deltaTime * 0.09f * workerMultiplier
          if (b.productionProgress >= 1f) {
            b.productionProgress = 0f
            resources.food += 3
            b.totalProduced += 3
          }
        }

        BuildingType.QUARRY -> {
          b.productionProgress += deltaTime * 0.08f * workerMultiplier
          if (b.productionProgress >= 1f) {
            b.productionProgress = 0f
            resources.stone += 2
            b.totalProduced += 2
          }
        }

        BuildingType.IRON_MINE -> {
          b.productionProgress += deltaTime * 0.07f * workerMultiplier
          if (b.productionProgress >= 1f) {
            b.productionProgress = 0f
            resources.iron += 1
            b.totalProduced += 1
          }
        }

        BuildingType.TAVERN -> {
          if (resources.food >= 1) {
            b.productionProgress += deltaTime * 0.08f
            if (b.productionProgress >= 1f) {
              b.productionProgress = 0f
              resources.food -= 1
              // Boost nearby townie happiness
              for (t in townies) {
                if (t.currentPos.distanceTo(b.origin) < 8f) {
                  t.happiness = min(100f, t.happiness + 8f)
                }
              }
            }
          }
        }

        BuildingType.WATCHTOWER -> {
          // Automated archer tower fires at nearest hostile raider in 5 tile radius
          b.productionProgress += deltaTime * 0.6f
          if (b.productionProgress >= 1f) {
            val nearestRaider = militaryUnits.firstOrNull { it.isHostile && it.hp > 0 && it.currentPos.distanceTo(b.origin) <= 5.5f }
            if (nearestRaider != null) {
              b.productionProgress = 0f
              projectiles.add(ArrowProjectile(startPos = b.origin, targetPos = nearestRaider.currentPos))
              nearestRaider.hp -= 25
              nearestRaider.slashEffectAnim = 1f
              if (nearestRaider.hp <= 0) {
                onRaiderSlain(nearestRaider)
              }
            }
          }
        }

        else -> {}
      }
    }
  }

  private fun updateTownies(deltaTime: Float) {
    for (t in townies) {
      t.animTick += deltaTime * 2f

      // Townies wander along roads or near workplaces
      if (t.targetPos == null || t.currentPos == t.targetPos) {
        if (Random.nextFloat() < 0.05f) {
          val rx = (t.currentPos.x + Random.nextInt(-2, 3)).coerceIn(1, MapGenerator.MAP_SIZE - 2)
          val ry = (t.currentPos.y + Random.nextInt(-2, 3)).coerceIn(1, MapGenerator.MAP_SIZE - 2)
          if (tiles[rx][ry].tileType.isWalkable) {
            t.targetPos = GridPos(rx, ry)
          }
        }
      } else {
        // Step towards targetPos
        val target = t.targetPos!!
        val dx = (target.x - t.currentPos.x).coerceIn(-1, 1)
        val dy = (target.y - t.currentPos.y).coerceIn(-1, 1)
        t.currentPos = GridPos(t.currentPos.x + dx, t.currentPos.y + dy)
      }
    }
  }

  private fun updateCombat(deltaTime: Float) {
    val defenders = militaryUnits.filter { !it.isHostile && it.hp > 0 }
    val raiders = militaryUnits.filter { it.isHostile && it.hp > 0 }

    // If all raiders defeated
    if (isRaidActive && raiders.isEmpty()) {
      isRaidActive = false
      raidCountdownSeconds = 90
      scenarioProgress.defeatedRaids++
      val lootGold = 50 + raidWaveCount * 30
      resources.gold += lootGold
      resources.weapons += 2
      setBanner("VICTORY! All raiders repelled! (+${lootGold} Gold, +2 Weapons seized).")
    }

    // Raiders move toward Castle or nearest building
    val castle = buildings.firstOrNull { it.type == BuildingType.CASTLE }
    val targetPos = castle?.origin ?: GridPos(12, 12)

    for (raider in raiders) {
      raider.slashEffectAnim = max(0f, raider.slashEffectAnim - deltaTime * 3f)

      // Look for nearby soldier to fight
      val nearbyDefender = defenders.firstOrNull { it.currentPos.distanceTo(raider.currentPos) <= 1.5f }
      if (nearbyDefender != null) {
        raider.isFighting = true
        raider.attackCooldown -= deltaTime
        if (raider.attackCooldown <= 0f) {
          raider.attackCooldown = 1.2f
          nearbyDefender.hp -= raider.attackDamage
          nearbyDefender.slashEffectAnim = 1f
          if (nearbyDefender.hp <= 0) {
            setBanner("A brave soldier (${nearbyDefender.name}) has fallen in defense of the realm!")
          }
        }
      } else {
        raider.isFighting = false
        // Move towards target
        val dx = (targetPos.x - raider.currentPos.x).coerceIn(-1, 1)
        val dy = (targetPos.y - raider.currentPos.y).coerceIn(-1, 1)
        raider.currentPos = GridPos(raider.currentPos.x + dx, raider.currentPos.y + dy)

        // Pillaging buildings if standing on them
        val standingBuilding = buildings.firstOrNull { it.occupies(raider.currentPos) && it.hp > 0 }
        if (standingBuilding != null) {
          standingBuilding.hp = max(0, standingBuilding.hp - 2)
          standingBuilding.isOnFire = true
        }
      }
    }

    // Soldiers engage raiders
    for (def in defenders) {
      def.slashEffectAnim = max(0f, def.slashEffectAnim - deltaTime * 3f)
      val nearestRaider = raiders.minByOrNull { it.currentPos.distanceTo(def.currentPos) }
      if (nearestRaider != null) {
        val dist = def.currentPos.distanceTo(nearestRaider.currentPos)
        if (dist <= 1.5f) {
          def.isFighting = true
          def.attackCooldown -= deltaTime
          if (def.attackCooldown <= 0f) {
            def.attackCooldown = 1.0f
            nearestRaider.hp -= def.attackDamage
            nearestRaider.slashEffectAnim = 1f
            if (nearestRaider.hp <= 0) {
              onRaiderSlain(nearestRaider)
            }
          }
        } else {
          def.isFighting = false
          // March toward enemy
          val dx = (nearestRaider.currentPos.x - def.currentPos.x).coerceIn(-1, 1)
          val dy = (nearestRaider.currentPos.y - def.currentPos.y).coerceIn(-1, 1)
          def.currentPos = GridPos(def.currentPos.x + dx, def.currentPos.y + dy)
        }
      } else {
        def.isFighting = false
      }
    }

    // Clean up fallen units
    militaryUnits.removeAll { it.hp <= 0 }
  }

  private fun onRaiderSlain(raider: MilitaryUnit) {
    raiderCountAlive = max(0, raiderCountAlive - 1)
  }

  private fun updateProjectiles(deltaTime: Float) {
    val it = projectiles.iterator()
    while (it.hasNext()) {
      val p = it.next()
      p.progress += deltaTime * 2.5f
      if (p.progress >= 1f) {
        it.remove()
      }
    }
  }

  private fun checkScenarioGoals() {
    val sc = currentScenario ?: return
    when (sc) {
      ScenarioId.LONG_WINTER -> {
        if (currentSeason == Season.WINTER) {
          scenarioProgress.survivedWinterDays = seasonDay
          if (seasonDay >= 25 && resources.firewood >= 100 && resources.food >= 60) {
            scenarioProgress.isComplete = true
            setBanner("CHALLENGE COMPLETE! You survived the Great Frost!")
          }
        }
      }
      ScenarioId.BANDIT_SIEGE -> {
        val towers = buildings.count { it.type == BuildingType.WATCHTOWER && it.hp > 0 }
        val soldiers = militaryUnits.count { !it.isHostile && it.hp > 0 }
        if (towers >= 2 && soldiers >= 5 && scenarioProgress.defeatedRaids >= 3) {
          scenarioProgress.isComplete = true
          setBanner("CHALLENGE COMPLETE! The Bandit Lord was utterly crushed!")
        }
      }
      ScenarioId.ROYAL_ELECTION -> {
        if (popularApprovalRating >= 85f) {
          scenarioProgress.isComplete = true
          setBanner("CHALLENGE COMPLETE! Popular Mandate secured with ${popularApprovalRating.toInt()}% approval!")
        }
      }
      ScenarioId.METROPOLIS -> {
        if (townies.size >= 30 && resources.gold >= 700) {
          scenarioProgress.isComplete = true
          setBanner("CHALLENGE COMPLETE! Grand Metropolis achieved!")
        }
      }
    }
  }

  fun canAfford(b: BuildingType): Boolean {
    return resources.gold >= b.goldCost &&
           resources.wood >= b.woodCost &&
           resources.stone >= b.stoneCost
  }

  fun build(type: BuildingType, origin: GridPos): Boolean {
    if (!canAfford(type)) {
      setBanner("Not enough resources to construct ${type.displayName}!")
      return false
    }

    // Check bounds & availability
    val sizeX = if (type == BuildingType.CASTLE) 2 else 1
    val sizeY = if (type == BuildingType.CASTLE) 2 else 1

    for (dx in 0 until sizeX) {
      for (dy in 0 until sizeY) {
        val gx = origin.x + dx
        val gy = origin.y + dy
        if (gx !in 0 until MapGenerator.MAP_SIZE || gy !in 0 until MapGenerator.MAP_SIZE) return false
        val tile = tiles[gx][gy]
        if (type == BuildingType.ROAD) {
          if (!tile.tileType.isWalkable) return false
        } else {
          if (!tile.tileType.isBuildable || tile.buildingId != null || tile.hasTree || tile.hasRock) {
            setBanner("Cannot build here! Terrain blocked.")
            return false
          }
        }
      }
    }

    // Deduct cost
    resources.gold -= type.goldCost
    resources.wood -= type.woodCost
    resources.stone -= type.stoneCost

    if (type == BuildingType.ROAD) {
      tiles[origin.x][origin.y].tileType = TileType.ROAD
      setBanner("Paved Cobblestone Road.")
      return true
    }

    val building = BuildingInstance(
      type = type,
      origin = origin,
      sizeX = sizeX,
      sizeY = sizeY,
      assignedWorkers = type.maxWorkers
    )
    buildings.add(building)
    for (pos in building.allTiles()) {
      tiles[pos.x][pos.y].buildingId = building.id
    }
    setBanner("Constructed ${type.displayName}!")
    return true
  }

  fun demolish(building: BuildingInstance) {
    if (building.type == BuildingType.CASTLE) {
      setBanner("You cannot demolish the Sovereign Castle!")
      return
    }
    buildings.remove(building)
    for (pos in building.allTiles()) {
      tiles[pos.x][pos.y].buildingId = null
    }
    // Salvage 50% wood and stone
    resources.wood += building.type.woodCost / 2
    resources.stone += building.type.stoneCost / 2
    setBanner("Demolished ${building.type.displayName}. Salvaged materials.")
  }

  fun recruitUnit(type: UnitType): Boolean {
    if (resources.gold < type.goldCost || resources.weapons < type.weaponCost) {
      setBanner("Not enough Gold (${type.goldCost}) or Weapons (${type.weaponCost}) to muster ${type.displayName}!")
      return false
    }
    val barracks = buildings.firstOrNull { it.type == BuildingType.BARRACKS && it.hp > 0 }
    val spawnPos = barracks?.origin ?: GridPos(12, 12)

    resources.gold -= type.goldCost
    resources.weapons -= type.weaponCost

    militaryUnits.add(
      MilitaryUnit(
        name = "Sir " + listOf("Galahad", "Lancelot", "Bors", "Tristan", "Percival").random(),
        type = type,
        currentPos = spawnPos,
        hp = type.maxHp,
        maxHp = type.maxHp,
        isHostile = false
      )
    )
    setBanner("Recruited ${type.displayName} into the Royal Garrison!")
    return true
  }

  fun enactDecree(decree: DecreeType): Boolean {
    if (resources.gold < decree.goldCost) {
      setBanner("Treasury lacks ${decree.goldCost} Gold for this decree!")
      return false
    }
    if (activeDecrees.any { it.decree == decree }) {
      setBanner("Decree is already active!")
      return false
    }
    resources.gold -= decree.goldCost

    if (decree == DecreeType.MILITIA_CALL) {
      val castle = buildings.firstOrNull { it.type == BuildingType.CASTLE }
      val origin = castle?.origin ?: GridPos(12, 12)
      militaryUnits.add(MilitaryUnit(name = "Militia Spearman", type = UnitType.SPEARMAN, currentPos = origin, isHostile = false))
      militaryUnits.add(MilitaryUnit(name = "Militia Spearman", type = UnitType.SPEARMAN, currentPos = origin, isHostile = false))
      setBanner("Decree enacted! 2 Militia Spearmen mobilized to defend!")
    } else {
      activeDecrees.add(ActiveDecree(decree, decree.durationDays))
      setBanner("Royal Decree enacted: ${decree.title}!")
    }
    calculateApproval()
    return true
  }

  private fun spawnNewCitizen() {
    val names = listOf("Aldous", "Gwen", "Barnaby", "Margery", "Gilbert", "Rowena", "Thurstan")
    val castle = buildings.firstOrNull { it.type == BuildingType.CASTLE }
    val origin = castle?.origin ?: GridPos(12, 12)
    val newTownie = TownieUnit(
      name = names.random(),
      currentPos = origin,
      happiness = 85f
    )
    townies.add(newTownie)
    setBanner("A new Townie (${newTownie.name}) has immigrated to your prosperous kingdom!")
  }

  fun getAverageHappiness(): Float {
    if (townies.isEmpty()) return 50f
    return townies.map { it.happiness }.average().toFloat()
  }

  fun setBanner(msg: String) {
    bannerMessage = msg
    bannerTimerSeconds = 4.5f
  }
}
