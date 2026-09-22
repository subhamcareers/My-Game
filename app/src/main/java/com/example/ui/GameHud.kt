package com.example.ui

import androidx.compose.animation.*
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.horizontalScroll
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.*
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Season

@Composable
fun GameHud(
  uiState: GameUiState,
  onSpeedChange: (Float) -> Unit,
  onOpenBuildMenu: () -> Unit,
  onToggleRoadMode: () -> Unit,
  onToggleBulldozeMode: () -> Unit,
  onCancelMode: () -> Unit,
  onOpenCouncil: () -> Unit,
  onOpenMilitary: () -> Unit,
  onOpenScenarios: () -> Unit,
  modifier: Modifier = Modifier
) {
  Column(
    modifier = modifier
      .fillMaxSize()
      .statusBarsPadding()
      .navigationBarsPadding(),
    verticalArrangement = Arrangement.SpaceBetween
  ) {
    // TOP BAR: Season, Day, Resources, Speed
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 8.dp, vertical = 4.dp)
    ) {
      Surface(
        color = Color(0xF02B1D14),
        shape = RoundedCornerShape(12.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37)),
        shadowElevation = 6.dp,
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(horizontal = 10.dp, vertical = 6.dp)) {
          // Row 1: Season & Time + Morale + Speed Controls
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            // Season badge
            val seasonIcon = when (uiState.season) {
              Season.SPRING -> "🌱"
              Season.SUMMER -> "☀️"
              Season.AUTUMN -> "🍂"
              Season.WINTER -> "❄️"
            }
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "$seasonIcon ${uiState.season.displayName}",
                fontWeight = FontWeight.Bold,
                fontSize = 14.sp,
                color = Color(0xFFFFE082)
              )
              Spacer(modifier = Modifier.width(6.dp))
              Text(
                text = "Day ${uiState.seasonDay} (Yr ${uiState.yearCount})",
                fontSize = 11.sp,
                color = Color(0xFFE0E0E0)
              )
            }

            // Population & Approval
            Row(verticalAlignment = Alignment.CenterVertically) {
              Text(
                text = "👥 ${uiState.population}/${uiState.maxPopulation}",
                fontSize = 12.sp,
                fontWeight = FontWeight.SemiBold,
                color = Color.White
              )
              Spacer(modifier = Modifier.width(8.dp))
              Text(
                text = "👑 ${uiState.approvalRating.toInt()}%",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = if (uiState.approvalRating >= 60f) Color(0xFF81C784) else Color(0xFFE57373)
              )
            }

            // Speed Controls
            Row(verticalAlignment = Alignment.CenterVertically) {
              SpeedButton(label = "⏸", isActive = uiState.gameSpeed == 0f) { onSpeedChange(0f) }
              SpeedButton(label = "1x", isActive = uiState.gameSpeed == 1f) { onSpeedChange(1f) }
              SpeedButton(label = "2x", isActive = uiState.gameSpeed == 2f) { onSpeedChange(2f) }
              SpeedButton(label = "3x", isActive = uiState.gameSpeed == 3f) { onSpeedChange(3f) }
            }
          }

          Divider(modifier = Modifier.padding(vertical = 4.dp), color = Color(0x33D4AF37))

          // Row 2: Live Resource Badges
          Row(
            modifier = Modifier
              .fillMaxWidth()
              .horizontalScroll(rememberScrollState()),
            horizontalArrangement = Arrangement.spacedBy(10.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            ResourceBadge("🪙", "${uiState.resources.gold}", "Gold")
            ResourceBadge("🍞", "${uiState.resources.food}", "Food")
            ResourceBadge("🪵", "${uiState.resources.wood}", "Wood")
            ResourceBadge("🪵", "${uiState.resources.planks}", "Planks")
            ResourceBadge("🪨", "${uiState.resources.stone}", "Stone")
            ResourceBadge("⛏️", "${uiState.resources.iron}", "Iron")
            ResourceBadge("🌾", "${uiState.resources.wheat}", "Wheat")
            ResourceBadge("⚔️", "${uiState.resources.weapons}", "Weapons")
            ResourceBadge(
              "🔥",
              "${uiState.resources.firewood}",
              "Firewood",
              isAlert = (uiState.season == Season.WINTER && uiState.resources.firewood < 15)
            )
          }
        }
      }

      // Notification / Alert Banner
      AnimatedVisibility(
        visible = uiState.bannerMessage != null,
        enter = fadeIn() + expandVertically(),
        exit = fadeOut() + shrinkVertically()
      ) {
        Surface(
          color = Color(0xF04A1513),
          shape = RoundedCornerShape(8.dp),
          border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFFF7043)),
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
          ) {
            Text(text = "📢 ", fontSize = 14.sp)
            Text(
              text = uiState.bannerMessage ?: "",
              fontSize = 12.sp,
              fontWeight = FontWeight.Medium,
              color = Color(0xFFFFEBEE)
            )
          }
        }
      }

      // Raid Warning Banner
      AnimatedVisibility(
        visible = uiState.isRaidActive || uiState.isRaidWarning,
        enter = fadeIn(),
        exit = fadeOut()
      ) {
        Surface(
          color = Color(0xEEB71C1C),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 6.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            val raidText = if (uiState.isRaidActive) {
              "⚔️ RAID IN PROGRESS! Defend the kingdom!"
            } else {
              "📯 RAIDERS SPOTTED! Incoming in ${uiState.raidCountdown}s"
            }
            Text(text = raidText, color = Color.White, fontWeight = FontWeight.Bold, fontSize = 12.sp)
            Button(
              onClick = onOpenMilitary,
              colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFFFD54F), contentColor = Color.Black),
              contentPadding = PaddingValues(horizontal = 8.dp, vertical = 2.dp),
              modifier = Modifier.height(28.dp)
            ) {
              Text("Muster Army", fontSize = 11.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }

      // Active Mode Banner (Placing Building, Road, or Bulldoze)
      if (uiState.buildingToPlace != null || uiState.isRoadMode || uiState.isBulldozeMode) {
        Surface(
          color = Color(0xEE1E3A5F),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .padding(top = 4.dp)
        ) {
          Row(
            modifier = Modifier.padding(horizontal = 12.dp, vertical = 4.dp),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
          ) {
            val modeTitle = when {
              uiState.buildingToPlace != null -> "Placing: ${uiState.buildingToPlace.displayName}"
              uiState.isRoadMode -> "Paving Cobblestone Roads"
              else -> "Demolish Mode (Tap building to salvage)"
            }
            Text(text = modeTitle, color = Color.White, fontSize = 12.sp, fontWeight = FontWeight.SemiBold)
            TextButton(
              onClick = onCancelMode,
              contentPadding = PaddingValues(horizontal = 6.dp, vertical = 2.dp)
            ) {
              Text("Cancel", color = Color(0xFFFF8A80), fontSize = 12.sp, fontWeight = FontWeight.Bold)
            }
          }
        }
      }
    }

    // BOTTOM ACTION BAR
    Surface(
      color = Color(0xF02B1D14),
      shape = RoundedCornerShape(topStart = 16.dp, topEnd = 16.dp),
      border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37)),
      shadowElevation = 8.dp,
      modifier = Modifier.fillMaxWidth()
    ) {
      Row(
        modifier = Modifier
          .fillMaxWidth()
          .padding(horizontal = 8.dp, vertical = 6.dp),
        horizontalArrangement = Arrangement.SpaceAround,
        verticalAlignment = Alignment.CenterVertically
      ) {
        HudActionButton(
          icon = "🏛️",
          label = "Build",
          badgeColor = Color(0xFFD4AF37),
          onClick = onOpenBuildMenu
        )
        HudActionButton(
          icon = "🛤️",
          label = "Roads",
          isActive = uiState.isRoadMode,
          onClick = onToggleRoadMode
        )
        HudActionButton(
          icon = "👑",
          label = "Council",
          badgeColor = Color(0xFFFFB300),
          onClick = onOpenCouncil
        )
        HudActionButton(
          icon = "⚔️",
          label = "Military",
          badgeColor = if (uiState.isRaidActive) Color.Red else Color(0xFF42A5F5),
          onClick = onOpenMilitary
        )
        HudActionButton(
          icon = "📜",
          label = "Kingdom",
          onClick = onOpenScenarios
        )
        HudActionButton(
          icon = "🔨",
          label = "Bulldoze",
          isActive = uiState.isBulldozeMode,
          badgeColor = Color(0xFFEF5350),
          onClick = onToggleBulldozeMode
        )
      }
    }
  }
}

@Composable
private fun SpeedButton(label: String, isActive: Boolean, onClick: () -> Unit) {
  Box(
    modifier = Modifier
      .padding(horizontal = 2.dp)
      .clip(RoundedCornerShape(4.dp))
      .background(if (isActive) Color(0xFFD4AF37) else Color(0x33FFFFFF))
      .clickable { onClick() }
      .padding(horizontal = 6.dp, vertical = 2.dp),
    contentAlignment = Alignment.Center
  ) {
    Text(
      text = label,
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = if (isActive) Color.Black else Color.White
    )
  }
}

@Composable
private fun ResourceBadge(icon: String, value: String, label: String, isAlert: Boolean = false) {
  Row(
    verticalAlignment = Alignment.CenterVertically,
    modifier = Modifier
      .clip(RoundedCornerShape(6.dp))
      .background(if (isAlert) Color(0x88D32F2F) else Color(0x22FFFFFF))
      .padding(horizontal = 6.dp, vertical = 2.dp)
  ) {
    Text(text = icon, fontSize = 12.sp)
    Spacer(modifier = Modifier.width(3.dp))
    Text(
      text = value,
      fontSize = 12.sp,
      fontWeight = FontWeight.Bold,
      color = if (isAlert) Color(0xFFFFCDD2) else Color.White
    )
  }
}

@Composable
private fun HudActionButton(
  icon: String,
  label: String,
  isActive: Boolean = false,
  badgeColor: Color = Color.White,
  onClick: () -> Unit
) {
  Column(
    horizontalAlignment = Alignment.CenterHorizontally,
    modifier = Modifier
      .clip(RoundedCornerShape(8.dp))
      .background(if (isActive) Color(0x44D4AF37) else Color.Transparent)
      .clickable { onClick() }
      .padding(horizontal = 8.dp, vertical = 4.dp)
  ) {
    Box(
      modifier = Modifier
        .size(34.dp)
        .clip(CircleShape)
        .background(if (isActive) Color(0xFFD4AF37) else Color(0x334E342E)),
      contentAlignment = Alignment.Center
    ) {
      Text(text = icon, fontSize = 18.sp)
    }
    Spacer(modifier = Modifier.height(2.dp))
    Text(
      text = label,
      fontSize = 10.sp,
      fontWeight = FontWeight.Medium,
      color = if (isActive) Color(0xFFFFD54F) else Color(0xFFE0E0E0)
    )
  }
}
