package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.Biome
import com.example.model.GameMode
import com.example.model.ScenarioId

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun ScenarioDialog(
  uiState: GameUiState,
  onStartGame: (Biome, GameMode, ScenarioId?) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedTab by remember { mutableStateOf(0) } // 0 = Challenges, 1 = Geographical Biomes / Sandbox

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = Color(0xFF231812),
    scrimColor = Color(0x99000000)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 16.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = "🗺️ Kingdoms & Scenarios",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD54F)
          )
          Text(
            text = "Geographical Challenges & Sandbox Mode",
            fontSize = 12.sp,
            color = Color(0xFFD7CCC8)
          )
        }
        TextButton(onClick = onDismiss) {
          Text("Close", color = Color.White)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Tab selector
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(8.dp)
      ) {
        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selectedTab == 0) Color(0xFFD4AF37) else Color(0x334E342E))
            .clickable { selectedTab = 0 }
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Challenges & Quests",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (selectedTab == 0) Color.Black else Color.White
          )
        }

        Box(
          modifier = Modifier
            .weight(1f)
            .clip(RoundedCornerShape(8.dp))
            .background(if (selectedTab == 1) Color(0xFFD4AF37) else Color(0x334E342E))
            .clickable { selectedTab = 1 }
            .padding(vertical = 8.dp),
          contentAlignment = Alignment.Center
        ) {
          Text(
            text = "Biomes (Sandbox)",
            fontSize = 12.sp,
            fontWeight = FontWeight.Bold,
            color = if (selectedTab == 1) Color.Black else Color.White
          )
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      if (selectedTab == 0) {
        // Challenges list
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 380.dp)
        ) {
          items(ScenarioId.values()) { scenario ->
            val isCurrent = uiState.currentScenario == scenario
            Surface(
              color = Color(0x334E342E),
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (isCurrent) Color(0xFFD4AF37) else Color(0x22FFFFFF)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Row(
                  modifier = Modifier.fillMaxWidth(),
                  horizontalArrangement = Arrangement.SpaceBetween,
                  verticalAlignment = Alignment.CenterVertically
                ) {
                  Text(
                    text = scenario.title,
                    fontWeight = FontWeight.Bold,
                    fontSize = 14.sp,
                    color = Color.White
                  )
                  if (isCurrent && uiState.scenarioComplete) {
                    Text(
                      text = "⭐ COMPLETED",
                      fontSize = 11.sp,
                      fontWeight = FontWeight.Bold,
                      color = Color(0xFFFFD54F)
                    )
                  }
                }
                Text(
                  text = scenario.subtitle,
                  fontSize = 11.sp,
                  color = Color(0xFFFFB74D)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = scenario.goalDescription,
                  fontSize = 11.sp,
                  color = Color(0xFFD7CCC8)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                  onClick = {
                    onStartGame(uiState.biome, GameMode.SCENARIO, scenario)
                    onDismiss()
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color.Black),
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.align(Alignment.End)
                ) {
                  Text("Play Scenario", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      } else {
        // Biomes (Countries / Geographical Challenges)
        LazyColumn(
          verticalArrangement = Arrangement.spacedBy(8.dp),
          modifier = Modifier
            .fillMaxWidth()
            .heightIn(max = 380.dp)
        ) {
          items(Biome.values()) { biome ->
            val isCurrent = uiState.biome == biome
            Surface(
              color = Color(0x334E342E),
              shape = RoundedCornerShape(8.dp),
              border = androidx.compose.foundation.BorderStroke(1.dp, if (isCurrent) Color(0xFFD4AF37) else Color(0x22FFFFFF)),
              modifier = Modifier.fillMaxWidth()
            ) {
              Column(modifier = Modifier.padding(10.dp)) {
                Text(
                  text = biome.displayName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = Color.White
                )
                Text(
                  text = biome.subtitle,
                  fontSize = 11.sp,
                  color = Color(0xFFFFB74D)
                )
                Spacer(modifier = Modifier.height(4.dp))
                Text(
                  text = biome.description,
                  fontSize = 11.sp,
                  color = Color(0xFFD7CCC8)
                )
                Spacer(modifier = Modifier.height(8.dp))
                Button(
                  onClick = {
                    onStartGame(biome, GameMode.SANDBOX, null)
                    onDismiss()
                  },
                  colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFD4AF37), contentColor = Color.Black),
                  shape = RoundedCornerShape(6.dp),
                  modifier = Modifier.align(Alignment.End)
                ) {
                  Text("Start Sandbox", fontSize = 11.sp, fontWeight = FontWeight.Bold)
                }
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
