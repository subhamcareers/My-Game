package com.example.model

import java.util.UUID

data class BuildingInstance(
  val id: String = UUID.randomUUID().toString(),
  val type: BuildingType,
  val origin: GridPos,
  val sizeX: Int = if (type == BuildingType.CASTLE) 2 else 1,
  val sizeY: Int = if (type == BuildingType.CASTLE) 2 else 1,
  var assignedWorkers: Int = type.maxWorkers,
  var maxWorkers: Int = type.maxWorkers,
  var productionProgress: Float = 0f,
  var level: Int = 1,
  var hp: Int = 100,
  var maxHp: Int = 100,
  var isActive: Boolean = true,
  var isOnFire: Boolean = false,
  var totalProduced: Int = 0,
  var floatingBubbleText: String? = null,
  var floatingBubbleTimer: Float = 0f
) {
  fun occupies(pos: GridPos): Boolean {
    return pos.x in origin.x until (origin.x + sizeX) &&
           pos.y in origin.y until (origin.y + sizeY)
  }

  fun allTiles(): List<GridPos> {
    val list = mutableListOf<GridPos>()
    for (x in 0 until sizeX) {
      for (y in 0 until sizeY) {
        list.add(GridPos(origin.x + x, origin.y + y))
      }
    }
    return list
  }
}
