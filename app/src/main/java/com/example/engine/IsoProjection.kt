package com.example.engine

import androidx.compose.ui.geometry.Offset
import com.example.model.GridPos
import kotlin.math.floor

object IsoProjection {
  const val BASE_TILE_WIDTH = 80f
  const val BASE_TILE_HEIGHT = 40f

  fun gridToScreen(
    gx: Float,
    gy: Float,
    zoom: Float,
    panX: Float,
    panY: Float,
    canvasWidth: Float,
    canvasHeight: Float,
    elevation: Float = 0f
  ): Offset {
    val tw = BASE_TILE_WIDTH * zoom
    val th = BASE_TILE_HEIGHT * zoom

    val sx = (gx - gy) * (tw * 0.5f) + panX + (canvasWidth * 0.5f)
    val sy = (gx + gy) * (th * 0.5f) + panY + (canvasHeight * 0.25f) - (elevation * 10f * zoom)
    return Offset(sx, sy)
  }

  fun screenToGrid(
    sx: Float,
    sy: Float,
    zoom: Float,
    panX: Float,
    panY: Float,
    canvasWidth: Float,
    canvasHeight: Float
  ): GridPos {
    val tw = BASE_TILE_WIDTH * zoom
    val th = BASE_TILE_HEIGHT * zoom

    val relX = (sx - panX - (canvasWidth * 0.5f)) / (tw * 0.5f)
    val relY = (sy - panY - (canvasHeight * 0.25f)) / (th * 0.5f)

    val gx = (relX + relY) * 0.5f
    val gy = (relY - relX) * 0.5f

    return GridPos(floor(gx).toInt(), floor(gy).toInt())
  }
}
