package com.example.ui

import androidx.compose.foundation.Canvas
import androidx.compose.foundation.gestures.detectTapGestures
import androidx.compose.foundation.gestures.detectTransformGestures
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.runtime.*
import androidx.compose.ui.Modifier
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.Path
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Fill
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.input.pointer.pointerInput
import com.example.engine.IsoProjection
import com.example.engine.MapGenerator
import com.example.model.*
import kotlin.math.*

@Composable
fun IsoGameCanvas(
  tiles: Array<Array<MapTile>>,
  buildings: List<BuildingInstance>,
  townies: List<TownieUnit>,
  militaryUnits: List<MilitaryUnit>,
  projectiles: List<ArrowProjectile>,
  season: Season,
  biome: Biome,
  selectedTilePos: GridPos?,
  buildingToPlace: BuildingType?,
  onTileTapped: (GridPos) -> Unit,
  modifier: Modifier = Modifier
) {
  var panX by remember { mutableFloatStateOf(0f) }
  var panY by remember { mutableFloatStateOf(0f) }
  var zoom by remember { mutableFloatStateOf(1.0f) }

  // Animated weather & water wave phase
  var animPhase by remember { mutableFloatStateOf(0f) }
  LaunchedEffect(Unit) {
    while (true) {
      withFrameNanos { time ->
        animPhase = (time / 1_000_000_000f) % 1000f
      }
    }
  }

  Canvas(
    modifier = modifier
      .fillMaxSize()
      .pointerInput(Unit) {
        detectTransformGestures { _, pan, gestureZoom, _ ->
          panX += pan.x
          panY += pan.y
          zoom = (zoom * gestureZoom).coerceIn(0.6f, 2.2f)
        }
      }
      .pointerInput(zoom, panX, panY) {
        detectTapGestures { tapOffset ->
          val gridPos = IsoProjection.screenToGrid(
            tapOffset.x,
            tapOffset.y,
            zoom,
            panX,
            panY,
            size.width.toFloat(),
            size.height.toFloat()
          )
          if (gridPos.x in 0 until MapGenerator.MAP_SIZE && gridPos.y in 0 until MapGenerator.MAP_SIZE) {
            onTileTapped(gridPos)
          }
        }
      }
  ) {
    val canvasW = size.width
    val canvasH = size.height

    // Background color based on season
    val bgSkyColor = when (season) {
      Season.WINTER -> Color(0xFFC7D7E0)
      Season.AUTUMN -> Color(0xFFD6C8AC)
      Season.SPRING -> Color(0xFF88B37A)
      Season.SUMMER -> Color(0xFF7FA85C)
    }
    drawRect(color = bgSkyColor)

    val tw = IsoProjection.BASE_TILE_WIDTH * zoom
    val th = IsoProjection.BASE_TILE_HEIGHT * zoom

    // Sort isometric drawing order from top-left (0,0) to bottom-right (MAP_SIZE, MAP_SIZE)
    for (sum in 0 until (MapGenerator.MAP_SIZE * 2)) {
      for (x in 0 until MapGenerator.MAP_SIZE) {
        val y = sum - x
        if (y !in 0 until MapGenerator.MAP_SIZE) continue

        val tile = tiles[x][y]
        val screenPos = IsoProjection.gridToScreen(
          x.toFloat(),
          y.toFloat(),
          zoom,
          panX,
          panY,
          canvasW,
          canvasH,
          tile.elevation.toFloat()
        )

        // Culling optimization
        if (screenPos.x < -tw || screenPos.x > canvasW + tw ||
            screenPos.y < -th * 2 || screenPos.y > canvasH + th * 2) {
          continue
        }

        // Draw diamond terrain tile
        drawIsoTile(tile, screenPos, tw, th, season, biome, animPhase)

        // Draw road if applicable
        if (tile.tileType == TileType.ROAD) {
          drawRoadTile(screenPos, tw, th, zoom)
        }

        // Draw trees or rocks
        if (tile.hasTree) {
          drawTree(screenPos, zoom, season, biome)
        } else if (tile.hasRock) {
          drawRock(screenPos, zoom)
        }

        // Draw selection cursor / highlight
        if (selectedTilePos?.x == x && selectedTilePos.y == y) {
          drawSelectionCursor(screenPos, tw, th, Color(0xFFE9C46A), zoom)
        }
      }
    }

    // Draw Buildings (sorted by isometric depth)
    val sortedBuildings = buildings.sortedBy { it.origin.x + it.origin.y }
    for (b in sortedBuildings) {
      val centerGx = b.origin.x + (b.sizeX - 1) * 0.5f
      val centerGy = b.origin.y + (b.sizeY - 1) * 0.5f
      val screenPos = IsoProjection.gridToScreen(
        centerGx,
        centerGy,
        zoom,
        panX,
        panY,
        canvasW,
        canvasH
      )
      drawBuilding(b, screenPos, zoom, season, animPhase)
    }

    // Draw Townies
    for (t in townies) {
      val screenPos = IsoProjection.gridToScreen(
        t.currentPos.x.toFloat(),
        t.currentPos.y.toFloat(),
        zoom,
        panX,
        panY,
        canvasW,
        canvasH
      )
      drawTownie(t, screenPos, zoom)
    }

    // Draw Military & Raiders
    for (unit in militaryUnits) {
      val screenPos = IsoProjection.gridToScreen(
        unit.currentPos.x.toFloat(),
        unit.currentPos.y.toFloat(),
        zoom,
        panX,
        panY,
        canvasW,
        canvasH
      )
      drawCombatUnit(unit, screenPos, zoom)
    }

    // Draw flying arrows
    for (p in projectiles) {
      val startScreen = IsoProjection.gridToScreen(p.startPos.x.toFloat(), p.startPos.y.toFloat(), zoom, panX, panY, canvasW, canvasH)
      val targetScreen = IsoProjection.gridToScreen(p.targetPos.x.toFloat(), p.targetPos.y.toFloat(), zoom, panX, panY, canvasW, canvasH)
      val curX = startScreen.x + (targetScreen.x - startScreen.x) * p.progress
      val arcHeight = sin(p.progress * Math.PI.toFloat()) * 30f * zoom
      val curY = startScreen.y + (targetScreen.y - startScreen.y) * p.progress - arcHeight

      drawCircle(Color(0xFF222222), radius = 3.5f * zoom, center = Offset(curX, curY))
      drawLine(
        Color(0xFFFFD700),
        start = Offset(curX - 4f * zoom, curY - 2f * zoom),
        end = Offset(curX + 4f * zoom, curY + 2f * zoom),
        strokeWidth = 2f * zoom
      )
    }

    // Seasonal Weather Particles (Snow in Winter, Rain in Autumn)
    drawWeatherOverlay(season, canvasW, canvasH, animPhase)
  }
}

private fun DrawScope.drawIsoTile(
  tile: MapTile,
  pos: Offset,
  tw: Float,
  th: Float,
  season: Season,
  biome: Biome,
  animPhase: Float
) {
  val path = Path().apply {
    moveTo(pos.x, pos.y - th * 0.5f)
    lineTo(pos.x + tw * 0.5f, pos.y)
    lineTo(pos.x, pos.y + th * 0.5f)
    lineTo(pos.x - tw * 0.5f, pos.y)
    close()
  }

  val baseColor = when (tile.tileType) {
    TileType.GRASS -> when (season) {
      Season.SPRING -> Color(0xFF6B9E3A)
      Season.SUMMER -> Color(0xFF5E9130)
      Season.AUTUMN -> Color(0xFF948A3C)
      Season.WINTER -> Color(0xFFE2EBE8)
    }
    TileType.WATER -> {
      val wave = sin((pos.x * 0.05f + animPhase * 3f)) * 0.05f
      when (season) {
        Season.WINTER -> Color(0xFF8BBAC4) // Icy frozen water
        else -> Color(0xFF3880C8 + (wave * 20).toInt())
      }
    }
    TileType.RIVER_BRIDGE -> Color(0xFF8B5A2B)
    TileType.MOUNTAIN -> Color(0xFF706D67)
    TileType.FOREST -> when (season) {
      Season.AUTUMN -> Color(0xFF8B5A2B)
      Season.WINTER -> Color(0xFFD6E2DD)
      else -> Color(0xFF48732A)
    }
    TileType.SAND -> Color(0xFFDEB887)
    TileType.SNOW -> Color(0xFFE8F1F5)
    TileType.DIRT -> Color(0xFF8B6B4F)
    TileType.WHEAT_FIELD -> Color(0xFFE6C242)
    TileType.ROAD -> Color(0xFF9E9E9E)
  }

  drawPath(path, color = baseColor, style = Fill)
  // Subtle tile grid edge
  drawPath(path, color = Color(0x18000000), style = Stroke(width = 1f))

  // If river bridge, draw wooden planks
  if (tile.tileType == TileType.RIVER_BRIDGE) {
    drawLine(Color(0xFF5C3A1E), Offset(pos.x - tw * 0.25f, pos.y - th * 0.15f), Offset(pos.x + tw * 0.25f, pos.y + th * 0.15f), strokeWidth = 2.5f)
    drawLine(Color(0xFF5C3A1E), Offset(pos.x - tw * 0.15f, pos.y + th * 0.15f), Offset(pos.x + tw * 0.15f, pos.y - th * 0.15f), strokeWidth = 2.5f)
  }
}

private fun DrawScope.drawRoadTile(pos: Offset, tw: Float, th: Float, zoom: Float) {
  val roadColor = Color(0xFFB5A99B)
  val path = Path().apply {
    moveTo(pos.x, pos.y - th * 0.35f)
    lineTo(pos.x + tw * 0.35f, pos.y)
    lineTo(pos.x, pos.y + th * 0.35f)
    lineTo(pos.x - tw * 0.35f, pos.y)
    close()
  }
  drawPath(path, color = roadColor, style = Fill)
  drawCircle(Color(0xFF8D7B68), radius = 2.5f * zoom, center = Offset(pos.x, pos.y))
}

private fun DrawScope.drawTree(pos: Offset, zoom: Float, season: Season, biome: Biome) {
  val trunkColor = Color(0xFF5C4033)
  val foliageColor = when {
    season == Season.WINTER -> Color(0xFFE1EAE6)
    season == Season.AUTUMN -> Color(0xFFC86428)
    biome == Biome.BOREAL_FJORD -> Color(0xFF264D3B) // Pine
    else -> Color(0xFF386B28)
  }

  // Trunk
  drawRect(
    color = trunkColor,
    topLeft = Offset(pos.x - 2f * zoom, pos.y - 12f * zoom),
    size = Size(4f * zoom, 12f * zoom)
  )

  // Foliage Crown
  if (biome == Biome.BOREAL_FJORD) {
    // Pine triangle
    val pinePath = Path().apply {
      moveTo(pos.x, pos.y - 28f * zoom)
      lineTo(pos.x + 8f * zoom, pos.y - 10f * zoom)
      lineTo(pos.x - 8f * zoom, pos.y - 10f * zoom)
      close()
    }
    drawPath(pinePath, color = foliageColor, style = Fill)
  } else {
    // Rounded canopy
    drawCircle(color = foliageColor, radius = 9f * zoom, center = Offset(pos.x, pos.y - 18f * zoom))
    drawCircle(color = foliageColor.copy(alpha = 0.8f), radius = 6f * zoom, center = Offset(pos.x - 3f * zoom, pos.y - 20f * zoom))
  }
}

private fun DrawScope.drawRock(pos: Offset, zoom: Float) {
  drawCircle(Color(0xFF6E6862), radius = 6f * zoom, center = Offset(pos.x, pos.y - 4f * zoom))
  drawCircle(Color(0xFF8C867F), radius = 3.5f * zoom, center = Offset(pos.x - 2f * zoom, pos.y - 6f * zoom))
}

private fun DrawScope.drawBuilding(
  b: BuildingInstance,
  pos: Offset,
  zoom: Float,
  season: Season,
  animPhase: Float
) {
  val isWinter = (season == Season.WINTER)
  val roofSnowColor = Color(0xFFE8F0F2)

  when (b.type) {
    BuildingType.CASTLE -> {
      // 2x2 Grand Fortress Keep
      val wallColor = Color(0xFF9E9A93)
      val roofColor = if (isWinter) roofSnowColor else Color(0xFFB33927)
      val goldTrim = Color(0xFFE8B923)

      // Base stone keep
      drawRect(
        color = wallColor,
        topLeft = Offset(pos.x - 28f * zoom, pos.y - 44f * zoom),
        size = Size(56f * zoom, 44f * zoom)
      )
      // Left tower
      drawRect(
        color = wallColor.copy(red = 0.55f),
        topLeft = Offset(pos.x - 34f * zoom, pos.y - 56f * zoom),
        size = Size(16f * zoom, 56f * zoom)
      )
      // Right tower
      drawRect(
        color = wallColor.copy(red = 0.55f),
        topLeft = Offset(pos.x + 18f * zoom, pos.y - 56f * zoom),
        size = Size(16f * zoom, 56f * zoom)
      )
      // Roof conical caps
      val leftRoof = Path().apply {
        moveTo(pos.x - 26f * zoom, pos.y - 70f * zoom)
        lineTo(pos.x - 16f * zoom, pos.y - 56f * zoom)
        lineTo(pos.x - 36f * zoom, pos.y - 56f * zoom)
        close()
      }
      val rightRoof = Path().apply {
        moveTo(pos.x + 26f * zoom, pos.y - 70f * zoom)
        lineTo(pos.x + 36f * zoom, pos.y - 56f * zoom)
        lineTo(pos.x + 16f * zoom, pos.y - 56f * zoom)
        close()
      }
      drawPath(leftRoof, color = roofColor)
      drawPath(rightRoof, color = roofColor)

      // Royal Gate Arch
      drawRect(
        color = Color(0xFF2B1D14),
        topLeft = Offset(pos.x - 8f * zoom, pos.y - 20f * zoom),
        size = Size(16f * zoom, 20f * zoom)
      )
      // Royal Flag
      drawLine(Color(0xFF222222), Offset(pos.x, pos.y - 58f * zoom), Offset(pos.x, pos.y - 74f * zoom), strokeWidth = 2f * zoom)
      val flagPath = Path().apply {
        moveTo(pos.x, pos.y - 74f * zoom)
        lineTo(pos.x + 12f * zoom, pos.y - 68f * zoom)
        lineTo(pos.x, pos.y - 62f * zoom)
        close()
      }
      drawPath(flagPath, color = goldTrim)
    }

    BuildingType.HOUSE -> {
      // Half-timbered cottage
      val wallColor = Color(0xFFEDE4D5)
      val roofColor = if (isWinter) roofSnowColor else Color(0xFFB5492F)
      drawRect(color = wallColor, topLeft = Offset(pos.x - 14f * zoom, pos.y - 18f * zoom), size = Size(28f * zoom, 18f * zoom))
      // Pitched roof
      val roof = Path().apply {
        moveTo(pos.x, pos.y - 30f * zoom)
        lineTo(pos.x + 17f * zoom, pos.y - 18f * zoom)
        lineTo(pos.x - 17f * zoom, pos.y - 18f * zoom)
        close()
      }
      drawPath(roof, color = roofColor)
      // Door & Window
      drawRect(Color(0xFF5A3825), topLeft = Offset(pos.x - 4f * zoom, pos.y - 10f * zoom), size = Size(8f * zoom, 10f * zoom))
      drawCircle(Color(0xFFF0E68C), radius = 2.5f * zoom, center = Offset(pos.x + 8f * zoom, pos.y - 12f * zoom))
    }

    BuildingType.WINDMILL -> {
      val baseColor = Color(0xFFDFD8C8)
      drawRect(color = baseColor, topLeft = Offset(pos.x - 10f * zoom, pos.y - 26f * zoom), size = Size(20f * zoom, 26f * zoom))
      // Rotating windmill sails
      val angle = animPhase * 2.5f
      val sailLen = 18f * zoom
      val cx = pos.x
      val cy = pos.y - 20f * zoom
      for (i in 0 until 4) {
        val sa = angle + (i * Math.PI.toFloat() * 0.5f)
        val ex = cx + cos(sa) * sailLen
        val ey = cy + sin(sa) * sailLen
        drawLine(Color(0xFF5A3825), Offset(cx, cy), Offset(ex, ey), strokeWidth = 2.5f * zoom)
      }
    }

    BuildingType.WATCHTOWER -> {
      val stoneColor = Color(0xFF88847C)
      drawRect(color = stoneColor, topLeft = Offset(pos.x - 8f * zoom, pos.y - 34f * zoom), size = Size(16f * zoom, 34f * zoom))
      // Lookout platform
      drawRect(Color(0xFF5C3A21), topLeft = Offset(pos.x - 12f * zoom, pos.y - 38f * zoom), size = Size(24f * zoom, 6f * zoom))
      // Archer silhouette
      drawCircle(Color(0xFF333333), radius = 2.5f * zoom, center = Offset(pos.x, pos.y - 42f * zoom))
    }

    BuildingType.LUMBERJACK -> {
      drawRect(Color(0xFF784E2D), topLeft = Offset(pos.x - 12f * zoom, pos.y - 16f * zoom), size = Size(24f * zoom, 16f * zoom))
      // Stack of logs outside
      drawCircle(Color(0xFF5C3A21), radius = 3.5f * zoom, center = Offset(pos.x + 8f * zoom, pos.y - 4f * zoom))
      drawCircle(Color(0xFF5C3A21), radius = 3.5f * zoom, center = Offset(pos.x + 14f * zoom, pos.y - 4f * zoom))
    }

    BuildingType.TAVERN -> {
      drawRect(Color(0xFF6B4226), topLeft = Offset(pos.x - 16f * zoom, pos.y - 22f * zoom), size = Size(32f * zoom, 22f * zoom))
      // Ale sign
      drawCircle(Color(0xFFFFD700), radius = 4f * zoom, center = Offset(pos.x - 12f * zoom, pos.y - 24f * zoom))
    }

    BuildingType.WHEAT_FARM -> {
      drawRect(Color(0xFFBFA054), topLeft = Offset(pos.x - 14f * zoom, pos.y - 16f * zoom), size = Size(28f * zoom, 16f * zoom))
      // Wheat sheaves
      drawLine(Color(0xFFE8C838), Offset(pos.x - 8f * zoom, pos.y - 4f * zoom), Offset(pos.x - 8f * zoom, pos.y - 12f * zoom), strokeWidth = 2f)
      drawLine(Color(0xFFE8C838), Offset(pos.x - 4f * zoom, pos.y - 4f * zoom), Offset(pos.x - 4f * zoom, pos.y - 12f * zoom), strokeWidth = 2f)
    }

    BuildingType.BARRACKS -> {
      drawRect(Color(0xFF6A6560), topLeft = Offset(pos.x - 18f * zoom, pos.y - 24f * zoom), size = Size(36f * zoom, 24f * zoom))
      // Crossed swords shield emblem
      drawCircle(Color(0xFFB22222), radius = 5f * zoom, center = Offset(pos.x, pos.y - 16f * zoom))
    }

    BuildingType.STATUE -> {
      // Golden King statue on stone plinth
      drawRect(Color(0xFF88847C), topLeft = Offset(pos.x - 8f * zoom, pos.y - 10f * zoom), size = Size(16f * zoom, 10f * zoom))
      drawCircle(Color(0xFFFFD700), radius = 6f * zoom, center = Offset(pos.x, pos.y - 22f * zoom))
      drawLine(Color(0xFFFFD700), Offset(pos.x, pos.y - 16f * zoom), Offset(pos.x, pos.y - 26f * zoom), strokeWidth = 3f * zoom)
    }

    else -> {
      // General medieval workshop / mill
      drawRect(Color(0xFF8A7A6D), topLeft = Offset(pos.x - 12f * zoom, pos.y - 16f * zoom), size = Size(24f * zoom, 16f * zoom))
    }
  }

  // Fire smoke if damaged / burning
  if (b.isOnFire || b.hp < b.maxHp * 0.7f) {
    val smokeAlpha = (sin(animPhase * 5f) * 0.2f + 0.6f).coerceIn(0.2f, 0.8f)
    drawCircle(Color.Red.copy(alpha = smokeAlpha), radius = 5f * zoom, center = Offset(pos.x, pos.y - 26f * zoom))
    drawCircle(Color.DarkGray.copy(alpha = smokeAlpha * 0.7f), radius = 8f * zoom, center = Offset(pos.x - 4f * zoom, pos.y - 36f * zoom))
  }
}

private fun DrawScope.drawTownie(t: TownieUnit, pos: Offset, zoom: Float) {
  // Animated walk bob
  val bob = sin(t.animTick * 4f) * 2f * zoom
  val headY = pos.y - 12f * zoom + bob

  // Shadow
  drawOval(Color(0x33000000), topLeft = Offset(pos.x - 4f * zoom, pos.y - 2f * zoom), size = Size(8f * zoom, 4f * zoom))

  // Body / tunic (color based on job)
  val tunicColor = when (t.job) {
    TownieJob.FARMER -> Color(0xFF9E7B4F)
    TownieJob.WOODCUTTER -> Color(0xFF4A7033)
    TownieJob.BAKER -> Color(0xFFE0D5B7)
    TownieJob.MINER -> Color(0xFF635F5C)
    TownieJob.GUARD -> Color(0xFF4682B4)
    else -> Color(0xFF8B4513)
  }
  drawRect(tunicColor, topLeft = Offset(pos.x - 3f * zoom, headY + 3f * zoom), size = Size(6f * zoom, 7f * zoom))

  // Head
  drawCircle(Color(0xFFFFDAB9), radius = 2.5f * zoom, center = Offset(pos.x, headY))
}

private fun DrawScope.drawCombatUnit(unit: MilitaryUnit, pos: Offset, zoom: Float) {
  val isHostile = unit.isHostile
  val unitColor = if (isHostile) Color(0xFFB71C1C) else Color(0xFF1976D2)

  // Shadow
  drawOval(Color(0x44000000), topLeft = Offset(pos.x - 5f * zoom, pos.y - 2f * zoom), size = Size(10f * zoom, 4f * zoom))

  // Armor body
  drawRect(unitColor, topLeft = Offset(pos.x - 4f * zoom, pos.y - 16f * zoom), size = Size(8f * zoom, 12f * zoom))

  // Helmet / Head
  val helmetColor = if (unit.type == UnitType.KNIGHT) Color(0xFFCCCCCC) else Color(0xFF8D6E63)
  drawCircle(helmetColor, radius = 3.5f * zoom, center = Offset(pos.x, pos.y - 18f * zoom))

  // Weapon in hand
  if (unit.type == UnitType.ARCHER) {
    drawLine(Color(0xFF5D4037), Offset(pos.x + 5f * zoom, pos.y - 20f * zoom), Offset(pos.x + 5f * zoom, pos.y - 8f * zoom), strokeWidth = 2f * zoom)
  } else {
    // Sword / Spear
    drawLine(Color(0xFFECEFF1), Offset(pos.x + 5f * zoom, pos.y - 22f * zoom), Offset(pos.x + 5f * zoom, pos.y - 8f * zoom), strokeWidth = 2.5f * zoom)
  }

  // Health bar above head
  val hpFraction = (unit.hp.toFloat() / unit.maxHp).coerceIn(0f, 1f)
  val barW = 16f * zoom
  val barH = 2.5f * zoom
  val barX = pos.x - barW * 0.5f
  val barY = pos.y - 26f * zoom

  drawRect(Color(0x88000000), topLeft = Offset(barX, barY), size = Size(barW, barH))
  drawRect(
    if (isHostile) Color.Red else Color.Green,
    topLeft = Offset(barX, barY),
    size = Size(barW * hpFraction, barH)
  )

  // Attack slash effect
  if (unit.slashEffectAnim > 0f) {
    drawArc(
      color = Color.White.copy(alpha = unit.slashEffectAnim),
      startAngle = -45f,
      sweepAngle = 90f,
      useCenter = false,
      topLeft = Offset(pos.x - 12f * zoom, pos.y - 28f * zoom),
      size = Size(24f * zoom, 24f * zoom),
      style = Stroke(width = 3f * zoom)
    )
  }
}

private fun DrawScope.drawSelectionCursor(
  pos: Offset,
  tw: Float,
  th: Float,
  color: Color,
  zoom: Float
) {
  val path = Path().apply {
    moveTo(pos.x, pos.y - th * 0.5f)
    lineTo(pos.x + tw * 0.5f, pos.y)
    lineTo(pos.x, pos.y + th * 0.5f)
    lineTo(pos.x - tw * 0.5f, pos.y)
    close()
  }
  drawPath(path, color = color.copy(alpha = 0.35f), style = Fill)
  drawPath(path, color = color, style = Stroke(width = 2.5f * zoom))
}

private fun DrawScope.drawWeatherOverlay(
  season: Season,
  width: Float,
  height: Float,
  phase: Float
) {
  if (season == Season.WINTER) {
    // Drifting snowflakes
    for (i in 0 until 40) {
      val sx = (sin(i * 99f + phase * 0.4f) * 0.5f + 0.5f) * width
      val sy = ((i * 47f + phase * 60f) % height)
      drawCircle(Color.White.copy(alpha = 0.85f), radius = 2.5f, center = Offset(sx, sy))
    }
  } else if (season == Season.AUTUMN) {
    // Light rain streaks
    for (i in 0 until 30) {
      val rx = (cos(i * 37f + phase * 0.2f) * 0.5f + 0.5f) * width
      val ry = ((i * 53f + phase * 180f) % height)
      drawLine(
        Color(0x66FFFFFF),
        start = Offset(rx, ry),
        end = Offset(rx - 4f, ry + 12f),
        strokeWidth = 1.5f
      )
    }
  }
}
