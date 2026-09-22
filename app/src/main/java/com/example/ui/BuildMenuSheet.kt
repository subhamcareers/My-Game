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
import com.example.model.BuildingCategory
import com.example.model.BuildingType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildMenuSheet(
  uiState: GameUiState,
  onSelectBuilding: (BuildingType) -> Unit,
  onDismiss: () -> Unit
) {
  var selectedCategory by remember { mutableStateOf(BuildingCategory.CIVIC) }

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
        Text(
          text = "🏗️ Royal Construction Guild",
          fontSize = 18.sp,
          fontWeight = FontWeight.Bold,
          color = Color(0xFFFFD54F)
        )
        TextButton(onClick = onDismiss) {
          Text("Close", color = Color.LightGray)
        }
      }

      Spacer(modifier = Modifier.height(8.dp))

      // Category Tabs
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.spacedBy(6.dp)
      ) {
        BuildingCategory.values().forEach { category ->
          val isSelected = selectedCategory == category
          Box(
            modifier = Modifier
              .weight(1f)
              .clip(RoundedCornerShape(8.dp))
              .background(if (isSelected) Color(0xFFD4AF37) else Color(0x334E342E))
              .clickable { selectedCategory = category }
              .padding(vertical = 8.dp),
            contentAlignment = Alignment.Center
          ) {
            Text(
              text = category.displayName.split(" ").first(),
              fontSize = 11.sp,
              fontWeight = FontWeight.Bold,
              color = if (isSelected) Color.Black else Color.White
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      // Buildings List
      val buildingsInCategory = BuildingType.values().filter {
        it.category == selectedCategory && it != BuildingType.CASTLE && it != BuildingType.ROAD
      }

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 380.dp)
      ) {
        items(buildingsInCategory) { bType ->
          val canAfford = uiState.resources.gold >= bType.goldCost &&
                          uiState.resources.wood >= bType.woodCost &&
                          uiState.resources.stone >= bType.stoneCost

          Surface(
            color = Color(0x443E2723),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (canAfford) Color(0x44D4AF37) else Color(0x22FFFFFF)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Row(
              modifier = Modifier
                .fillMaxWidth()
                .padding(10.dp),
              horizontalArrangement = Arrangement.SpaceBetween,
              verticalAlignment = Alignment.CenterVertically
            ) {
              Column(modifier = Modifier.weight(1f)) {
                Text(
                  text = bType.displayName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 14.sp,
                  color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = bType.description,
                  fontSize = 11.sp,
                  color = Color(0xFFD7CCC8)
                )
                Spacer(modifier = Modifier.height(4.dp))
                // Cost badges
                Row(horizontalArrangement = Arrangement.spacedBy(8.dp)) {
                  CostTag("🪙", bType.goldCost, uiState.resources.gold >= bType.goldCost)
                  CostTag("🪵", bType.woodCost, uiState.resources.wood >= bType.woodCost)
                  CostTag("🪨", bType.stoneCost, uiState.resources.stone >= bType.stoneCost)
                }
              }

              Spacer(modifier = Modifier.width(8.dp))

              Button(
                onClick = {
                  onSelectBuilding(bType)
                  onDismiss()
                },
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFFD4AF37),
                  contentColor = Color.Black,
                  disabledContainerColor = Color(0x33FFFFFF),
                  disabledContentColor = Color.Gray
                ),
                shape = RoundedCornerShape(8.dp)
              ) {
                Text("Build", fontWeight = FontWeight.Bold, fontSize = 12.sp)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}

@Composable
private fun CostTag(icon: String, amount: Int, hasEnough: Boolean) {
  if (amount <= 0) return
  Row(verticalAlignment = Alignment.CenterVertically) {
    Text(text = icon, fontSize = 11.sp)
    Spacer(modifier = Modifier.width(2.dp))
    Text(
      text = "$amount",
      fontSize = 11.sp,
      fontWeight = FontWeight.Bold,
      color = if (hasEnough) Color.White else Color(0xFFEF9A9A)
    )
  }
}
