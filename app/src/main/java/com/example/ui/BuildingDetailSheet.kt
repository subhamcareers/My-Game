package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.example.model.BuildingInstance
import com.example.model.BuildingType

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun BuildingDetailSheet(
  building: BuildingInstance?,
  onDemolish: () -> Unit,
  onDismiss: () -> Unit
) {
  if (building == null) return

  ModalBottomSheet(
    onDismissRequest = onDismiss,
    containerColor = Color(0xFF231812),
    scrimColor = Color(0x99000000)
  ) {
    Column(
      modifier = Modifier
        .fillMaxWidth()
        .padding(horizontal = 20.dp, vertical = 8.dp)
    ) {
      Row(
        modifier = Modifier.fillMaxWidth(),
        horizontalArrangement = Arrangement.SpaceBetween,
        verticalAlignment = Alignment.CenterVertically
      ) {
        Column {
          Text(
            text = building.type.displayName,
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD54F)
          )
          Text(
            text = building.type.category.displayName,
            fontSize = 12.sp,
            color = Color(0xFFBCAAA4)
          )
        }
        TextButton(onClick = onDismiss) {
          Text("Done", color = Color.White)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      // Status card
      Surface(
        color = Color(0x334E342E),
        shape = RoundedCornerShape(8.dp),
        modifier = Modifier.fillMaxWidth()
      ) {
        Column(modifier = Modifier.padding(12.dp)) {
          Text(
            text = building.type.description,
            fontSize = 12.sp,
            color = Color(0xFFEFEBE9)
          )
          Spacer(modifier = Modifier.height(8.dp))
          Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween
          ) {
            Text(text = "Condition:", fontSize = 12.sp, color = Color.Gray)
            Text(
              text = "${building.hp} / ${building.maxHp} HP",
              fontSize = 12.sp,
              fontWeight = FontWeight.Bold,
              color = if (building.hp < 50) Color.Red else Color.Green
            )
          }
          if (building.maxWorkers > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "Assigned Townies:", fontSize = 12.sp, color = Color.Gray)
              Text(
                text = "${building.assignedWorkers} / ${building.maxWorkers}",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color.White
              )
            }
          }
          if (building.totalProduced > 0) {
            Spacer(modifier = Modifier.height(4.dp))
            Row(
              modifier = Modifier.fillMaxWidth(),
              horizontalArrangement = Arrangement.SpaceBetween
            ) {
              Text(text = "Lifetime Yield:", fontSize = 12.sp, color = Color.Gray)
              Text(
                text = "${building.totalProduced} units",
                fontSize = 12.sp,
                fontWeight = FontWeight.Bold,
                color = Color(0xFFFFD54F)
              )
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))

      // Action buttons
      if (building.type != BuildingType.CASTLE) {
        Button(
          onClick = {
            onDemolish()
            onDismiss()
          },
          colors = ButtonDefaults.buttonColors(containerColor = Color(0xFFB71C1C)),
          shape = RoundedCornerShape(8.dp),
          modifier = Modifier.fillMaxWidth()
        ) {
          Text("Demolish Structure (Salvage Materials)", fontWeight = FontWeight.Bold)
        }
      }

      Spacer(modifier = Modifier.height(20.dp))
    }
  }
}
