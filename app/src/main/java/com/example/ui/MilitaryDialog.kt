package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.UnitType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun MilitaryDialog(
  uiState: GameUiState,
  onRecruitUnit: (UnitType) -> Unit,
  onDismiss: () -> Unit
) {
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
            text = "⚔️ Royal War Office & Garrison",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFF8A80)
          )
          Text(
            text = "Train Troops & Defend Against Barbarian Raids",
            fontSize = 12.sp,
            color = Color(0xFFD7CCC8)
          )
        }
        TextButton(onClick = onDismiss) {
          Text("Close", color = Color.White)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Status Banner
      Surface(
        color = if (uiState.isRaidActive) Color(0x66D32F2F) else Color(0x334E342E),
        shape = RoundedCornerShape(8.dp),
        border = androidx.compose.foundation.BorderStroke(1.dp, if (uiState.isRaidActive) Color.Red else Color(0x33FFFFFF)),
        modifier = Modifier.fillMaxWidth()
      ) {
        Row(
          modifier = Modifier.padding(12.dp),
          horizontalArrangement = Arrangement.SpaceBetween,
          verticalAlignment = Alignment.CenterVertically
        ) {
          Column {
            Text(
              text = if (uiState.isRaidActive) "🔥 WAR DECLARED! Realm Under Siege" else "🛡️ Realm at Peace",
              fontWeight = FontWeight.Bold,
              fontSize = 13.sp,
              color = Color.White
            )
            Spacer(modifier = Modifier.height(2.dp))
            Text(
              text = if (uiState.isRaidActive) "Barbarians pillaging! Watchtowers & Soldiers responding!" else "Next Raid in ~${uiState.raidCountdown}s (Wave #${uiState.raidWave + 1})",
              fontSize = 11.sp,
              color = Color(0xFFE0E0E0)
            )
          }
        }
      }

      Spacer(modifier = Modifier.height(12.dp))

      Text(
        text = "Muster Troops (Requires Barracks & Weapons)",
        fontWeight = FontWeight.Bold,
        fontSize = 13.sp,
        color = Color(0xFFFFD54F)
      )

      Spacer(modifier = Modifier.height(6.dp))

      val recruitUnits = listOf(UnitType.SPEARMAN, UnitType.ARCHER, UnitType.KNIGHT)

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        items(recruitUnits) { unit ->
          val canAfford = uiState.resources.gold >= unit.goldCost && uiState.resources.weapons >= unit.weaponCost

          Surface(
            color = Color(0x334E342E),
            shape = RoundedCornerShape(8.dp),
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
                  text = unit.displayName,
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color.White
                )
                Text(
                  text = "HP: ${unit.maxHp} | Damage: ${unit.attackDamage}",
                  fontSize = 11.sp,
                  color = Color(0xFFB0BEC5)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Row(horizontalArrangement = Arrangement.spacedBy(6.dp)) {
                  Text(
                    text = "🪙 ${unit.goldCost} Gold",
                    fontSize = 11.sp,
                    color = if (uiState.resources.gold >= unit.goldCost) Color(0xFFFFD54F) else Color(0xFFEF9A9A)
                  )
                  Text(
                    text = "⚔️ ${unit.weaponCost} Weapon",
                    fontSize = 11.sp,
                    color = if (uiState.resources.weapons >= unit.weaponCost) Color.White else Color(0xFFEF9A9A)
                  )
                }
              }

              Button(
                onClick = { onRecruitUnit(unit) },
                enabled = canAfford,
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFFD4AF37),
                  contentColor = Color.Black
                ),
                shape = RoundedCornerShape(6.dp)
              ) {
                Text("Recruit", fontSize = 11.sp, fontWeight = FontWeight.Bold)
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
