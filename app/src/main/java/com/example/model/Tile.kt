package com.example.model

data class GridPos(val x: Int, val y: Int) {
  fun distanceTo(other: GridPos): Float {
    val dx = (x - other.x).toFloat()
    val dy = (y - other.y).toFloat()
    return kotlin.math.sqrt(dx * dx + dy * dy)
  }

  fun isAdjacent(other: GridPos): Boolean {
    val dx = kotlin.math.abs(x - other.x)
    val dy = kotlin.math.abs(y - other.y)
    return (dx <= 1 && dy <= 1) && !(dx == 0 && dy == 0)
  }
}

enum class TileType(val displayName: String, val isWalkable: Boolean, val isBuildable: Boolean) {
  GRASS("Lush Grass", isWalkable = true, isBuildable = true),
  DIRT("Tilled Soil", isWalkable = true, isBuildable = true),
  WATER("River Waters", isWalkable = false, isBuildable = false),
  RIVER_BRIDGE("Wooden River Bridge", isWalkable = true, isBuildable = false),
  MOUNTAIN("Rocky Mountain", isWalkable = false, isBuildable = false),
  FOREST("Dense Forest", isWalkable = true, isBuildable = false),
  WHEAT_FIELD("Farmed Wheatfield", isWalkable = true, isBuildable = false),
  ROAD("Cobblestone Road", isWalkable = true, isBuildable = false),
  SAND("Desert Sands", isWalkable = true, isBuildable = true),
  SNOW("Frozen Ground", isWalkable = true, isBuildable = true)
}

data class MapTile(
  val pos: GridPos,
  var tileType: TileType,
  var elevation: Int = 0,
  var hasTree: Boolean = false,
  var hasRock: Boolean = false,
  var cropGrowth: Float = 0f, // 0.0 to 1.0 for farming
  var buildingId: String? = null
)
