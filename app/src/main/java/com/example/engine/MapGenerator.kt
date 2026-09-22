package com.example.engine

import com.example.model.*
import kotlin.random.Random

object MapGenerator {
  const val MAP_SIZE = 26

  data class GeneratedMap(
    val tiles: Array<Array<MapTile>>,
    val initialBuildings: List<BuildingInstance>,
    val initialTownies: List<TownieUnit>,
    val initialMilitary: List<MilitaryUnit>,
    val initialSheep: List<PastureSheep>,
    val startingResources: KingdomResources
  )

  fun generate(biome: Biome, randomSeed: Long = System.currentTimeMillis()): GeneratedMap {
    val rng = Random(randomSeed)
    val tiles = Array(MAP_SIZE) { x ->
      Array(MAP_SIZE) { y ->
        val defaultType = when (biome) {
          Biome.DESERT_OASIS -> TileType.SAND
          else -> TileType.GRASS
        }
        MapTile(
          pos = GridPos(x, y),
          tileType = defaultType,
          elevation = 0,
          hasTree = false,
          hasRock = false
        )
      }
    }

    // 1. Generate Biome Geography (Rivers, Mountains, Forests)
    when (biome) {
      Biome.RIVERLAND -> {
        // Meandering river from (x=0, y=10) across to (x=25, y=14)
        for (x in 0 until MAP_SIZE) {
          val riverY = 11 + ((kotlin.math.sin(x * 0.35) * 2.5)).toInt()
          if (riverY in 0 until MAP_SIZE) {
            tiles[x][riverY].tileType = TileType.WATER
            if (riverY + 1 < MAP_SIZE && (x % 3 != 0)) {
              tiles[x][riverY + 1].tileType = TileType.WATER
            }
          }
        }
        // Build bridges across the river
        val bridgeX1 = 8
        val bridgeY1 = 11 + ((kotlin.math.sin(bridgeX1 * 0.35) * 2.5)).toInt()
        tiles[bridgeX1][bridgeY1].tileType = TileType.RIVER_BRIDGE
        if (bridgeY1 + 1 < MAP_SIZE && tiles[bridgeX1][bridgeY1 + 1].tileType == TileType.WATER) {
          tiles[bridgeX1][bridgeY1 + 1].tileType = TileType.RIVER_BRIDGE
        }

        val bridgeX2 = 18
        val bridgeY2 = 11 + ((kotlin.math.sin(bridgeX2 * 0.35) * 2.5)).toInt()
        tiles[bridgeX2][bridgeY2].tileType = TileType.RIVER_BRIDGE
        if (bridgeY2 + 1 < MAP_SIZE && tiles[bridgeX2][bridgeY2 + 1].tileType == TileType.WATER) {
          tiles[bridgeX2][bridgeY2 + 1].tileType = TileType.RIVER_BRIDGE
        }

        // Add forests and tree groves
        for (x in 0 until MAP_SIZE) {
          for (y in 0 until MAP_SIZE) {
            if (tiles[x][y].tileType == TileType.GRASS) {
              if (rng.nextFloat() < 0.18f && !(x in 10..16 && y in 4..9)) {
                tiles[x][y].hasTree = true
              } else if (rng.nextFloat() < 0.04f) {
                tiles[x][y].hasRock = true
              }
            }
          }
        }
      }

      Biome.HIGHLAND -> {
        // Rocky mountain ridge in North-West and East
        for (x in 0 until MAP_SIZE) {
          for (y in 0 until MAP_SIZE) {
            val distToEdge = minOf(x, y, MAP_SIZE - 1 - x, MAP_SIZE - 1 - y)
            if (distToEdge < 3 || (x + y < 9 && rng.nextFloat() < 0.7f)) {
              tiles[x][y].tileType = TileType.MOUNTAIN
              tiles[x][y].elevation = 2
            } else if (rng.nextFloat() < 0.16f) {
              tiles[x][y].hasRock = true
            } else if (rng.nextFloat() < 0.10f) {
              tiles[x][y].hasTree = true
            }
          }
        }
      }

      Biome.DESERT_OASIS -> {
        // Central oasis pond
        for (x in 11..14) {
          for (y in 11..14) {
            if ((x == 11 || x == 14) && (y == 11 || y == 14)) continue
            tiles[x][y].tileType = TileType.WATER
          }
        }
        // Lush green belt around oasis
        for (x in 9..16) {
          for (y in 9..16) {
            if (tiles[x][y].tileType != TileType.WATER) {
              tiles[x][y].tileType = TileType.GRASS
              if (rng.nextFloat() < 0.25f) tiles[x][y].hasTree = true
            }
          }
        }
        // Sparse rocks in the dunes
        for (x in 0 until MAP_SIZE) {
          for (y in 0 until MAP_SIZE) {
            if (tiles[x][y].tileType == TileType.SAND && rng.nextFloat() < 0.05f) {
              tiles[x][y].hasRock = true
            }
          }
        }
      }

      Biome.BOREAL_FJORD -> {
        // Fjord water inlet on south side
        for (x in 0 until MAP_SIZE) {
          for (y in 19 until MAP_SIZE) {
            tiles[x][y].tileType = TileType.WATER
          }
        }
        // Dense pine forests & snow patches
        for (x in 0 until MAP_SIZE) {
          for (y in 0 until 19) {
            if (rng.nextFloat() < 0.32f) {
              tiles[x][y].hasTree = true
            } else if (rng.nextFloat() < 0.08f) {
              tiles[x][y].hasRock = true
            } else if (rng.nextFloat() < 0.12f) {
              tiles[x][y].tileType = TileType.SNOW
            }
          }
        }
      }
    }

    // 2. Prepare Castle & Starter Settlement Area
    val castleOrigin = when (biome) {
      Biome.RIVERLAND -> GridPos(12, 6)
      Biome.HIGHLAND -> GridPos(13, 12)
      Biome.DESERT_OASIS -> GridPos(7, 7)
      Biome.BOREAL_FJORD -> GridPos(12, 8)
    }

    // Clear castle footprint
    for (cx in 0..2) {
      for (cy in 0..2) {
        val gx = castleOrigin.x + cx
        val gy = castleOrigin.y + cy
        if (gx in 0 until MAP_SIZE && gy in 0 until MAP_SIZE) {
          tiles[gx][gy].tileType = TileType.GRASS
          tiles[gx][gy].hasTree = false
          tiles[gx][gy].hasRock = false
        }
      }
    }

    // Lay starter cobblestone roads
    val roadPoints = listOf(
      GridPos(castleOrigin.x + 1, castleOrigin.y + 2),
      GridPos(castleOrigin.x + 1, castleOrigin.y + 3),
      GridPos(castleOrigin.x + 1, castleOrigin.y + 4),
      GridPos(castleOrigin.x, castleOrigin.y + 3),
      GridPos(castleOrigin.x + 2, castleOrigin.y + 3),
      GridPos(castleOrigin.x + 3, castleOrigin.y + 3),
      GridPos(castleOrigin.x - 1, castleOrigin.y + 3)
    )
    for (rp in roadPoints) {
      if (rp.x in 0 until MAP_SIZE && rp.y in 0 until MAP_SIZE) {
        tiles[rp.x][rp.y].tileType = TileType.ROAD
        tiles[rp.x][rp.y].hasTree = false
        tiles[rp.x][rp.y].hasRock = false
      }
    }

    val initialBuildings = mutableListOf<BuildingInstance>()

    // Castle
    val castle = BuildingInstance(
      type = BuildingType.CASTLE,
      origin = castleOrigin,
      sizeX = 2,
      sizeY = 2,
      assignedWorkers = 1
    )
    initialBuildings.add(castle)
    for (pos in castle.allTiles()) {
      tiles[pos.x][pos.y].buildingId = castle.id
    }

    // Starter Cottage
    val cottagePos = GridPos(castleOrigin.x - 2, castleOrigin.y + 2)
    tiles[cottagePos.x][cottagePos.y].tileType = TileType.GRASS
    tiles[cottagePos.x][cottagePos.y].hasTree = false
    tiles[cottagePos.x][cottagePos.y].hasRock = false
    val cottage = BuildingInstance(
      type = BuildingType.HOUSE,
      origin = cottagePos,
      sizeX = 1,
      sizeY = 1
    )
    initialBuildings.add(cottage)
    tiles[cottagePos.x][cottagePos.y].buildingId = cottage.id

    // Starter Lumberjack Lodge near trees
    val lumberjackPos = GridPos(castleOrigin.x + 4, castleOrigin.y + 2)
    tiles[lumberjackPos.x][lumberjackPos.y].tileType = TileType.GRASS
    tiles[lumberjackPos.x][lumberjackPos.y].hasTree = false
    tiles[lumberjackPos.x][lumberjackPos.y].hasRock = false
    val lumberjack = BuildingInstance(
      type = BuildingType.LUMBERJACK,
      origin = lumberjackPos,
      sizeX = 1,
      sizeY = 1,
      assignedWorkers = 1
    )
    initialBuildings.add(lumberjack)
    tiles[lumberjackPos.x][lumberjackPos.y].buildingId = lumberjack.id

    // Starter Wheat Farm
    val farmPos = GridPos(castleOrigin.x - 2, castleOrigin.y + 4)
    tiles[farmPos.x][farmPos.y].tileType = TileType.GRASS
    tiles[farmPos.x][farmPos.y].hasTree = false
    tiles[farmPos.x][farmPos.y].hasRock = false
    val farm = BuildingInstance(
      type = BuildingType.WHEAT_FARM,
      origin = farmPos,
      sizeX = 1,
      sizeY = 1,
      assignedWorkers = 1
    )
    initialBuildings.add(farm)
    tiles[farmPos.x][farmPos.y].buildingId = farm.id

    // Starter Townies
    val townieNames = listOf("Willem", "Adela", "Geoffrey", "Elsbeth", "Roland", "Beatrice")
    val initialTownies = listOf(
      TownieUnit(
        name = townieNames[0],
        currentPos = GridPos(castleOrigin.x + 1, castleOrigin.y + 3),
        homeBuildingId = cottage.id,
        workBuildingId = lumberjack.id,
        job = TownieJob.WOODCUTTER,
        happiness = 85f
      ),
      TownieUnit(
        name = townieNames[1],
        currentPos = GridPos(castleOrigin.x, castleOrigin.y + 3),
        homeBuildingId = cottage.id,
        workBuildingId = farm.id,
        job = TownieJob.FARMER,
        happiness = 82f
      ),
      TownieUnit(
        name = townieNames[2],
        currentPos = GridPos(castleOrigin.x + 2, castleOrigin.y + 3),
        homeBuildingId = cottage.id,
        workBuildingId = castle.id,
        job = TownieJob.GUARD,
        happiness = 88f
      ),
      TownieUnit(
        name = townieNames[3],
        currentPos = GridPos(castleOrigin.x + 1, castleOrigin.y + 2),
        homeBuildingId = cottage.id,
        workBuildingId = null,
        job = TownieJob.UNASSIGNED,
        happiness = 80f
      )
    )

    // Starter defense spearmen
    val initialMilitary = listOf(
      MilitaryUnit(
        name = "Sir Cedric",
        type = UnitType.SPEARMAN,
        currentPos = GridPos(castleOrigin.x + 1, castleOrigin.y + 4),
        hp = 80,
        maxHp = 80,
        isHostile = false
      )
    )

    val resources = KingdomResources(
      gold = 160,
      food = 80,
      wood = biome.baseWood,
      planks = 25,
      stone = biome.baseStone,
      iron = biome.baseIron,
      wheat = 30,
      weapons = 3,
      firewood = 60
    )

    return GeneratedMap(tiles, initialBuildings, initialTownies, initialMilitary, resources)
  }
}
