package com.example.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.items
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
import com.example.model.DecreeType
import com.example.model.TaxRate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun CouncilDialog(
  uiState: GameUiState,
  onTaxRateChange: (TaxRate) -> Unit,
  onEnactDecree: (DecreeType) -> Unit,
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
            text = "👑 Sovereign's Royal Council",
            fontSize = 18.sp,
            fontWeight = FontWeight.Bold,
            color = Color(0xFFFFD54F)
          )
          Text(
            text = "Townie Approval & Popular Election",
            fontSize = 12.sp,
            color = Color(0xFFD7CCC8)
          )
        }
        TextButton(onClick = onDismiss) {
          Text("Close", color = Color.White)
        }
      }

      Spacer(modifier = Modifier.height(10.dp))

      LazyColumn(
        verticalArrangement = Arrangement.spacedBy(12.dp),
        modifier = Modifier
          .fillMaxWidth()
          .heightIn(max = 420.dp)
      ) {
        // Section 1: Election & Popular Mandate
        item {
          Surface(
            color = Color(0x443E2723),
            shape = RoundedCornerShape(10.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, Color(0xFFD4AF37)),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.SpaceBetween,
                verticalAlignment = Alignment.CenterVertically
              ) {
                Text(
                  text = "Popular Approval Rating",
                  fontWeight = FontWeight.SemiBold,
                  color = Color.White,
                  fontSize = 14.sp
                )
                Text(
                  text = "${uiState.approvalRating.toInt()}%",
                  fontWeight = FontWeight.Bold,
                  fontSize = 16.sp,
                  color = if (uiState.approvalRating >= 60f) Color(0xFF81C784) else Color(0xFFE57373)
                )
              }
              Spacer(modifier = Modifier.height(6.dp))
              LinearProgressIndicator(
                progress = { (uiState.approvalRating / 100f).coerceIn(0f, 1f) },
                color = if (uiState.approvalRating >= 60f) Color(0xFF81C784) else Color(0xFFE57373),
                trackColor = Color(0x33FFFFFF),
                modifier = Modifier.fillMaxWidth().height(6.dp).clip(RoundedCornerShape(3.dp))
              )
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Next Democratic Council Election: in ${uiState.electionDaysRemaining} days. Citizens re-elect you if approval is >= 50% (+120 Gold bounty).",
                fontSize = 11.sp,
                color = Color(0xFFD7CCC8)
              )
            }
          }
        }

        // Section 2: Royal Tax Policy
        item {
          Surface(
            color = Color(0x443E2723),
            shape = RoundedCornerShape(10.dp),
            modifier = Modifier.fillMaxWidth()
          ) {
            Column(modifier = Modifier.padding(12.dp)) {
              Text(
                text = "💰 Imperial Tax Rate",
                fontWeight = FontWeight.SemiBold,
                color = Color.White,
                fontSize = 14.sp
              )
              Spacer(modifier = Modifier.height(8.dp))
              Row(
                modifier = Modifier.fillMaxWidth(),
                horizontalArrangement = Arrangement.spacedBy(4.dp)
              ) {
                TaxRate.values().forEach { rate ->
                  val isSelected = uiState.taxRate == rate
                  Box(
                    modifier = Modifier
                      .weight(1f)
                      .clip(RoundedCornerShape(6.dp))
                      .background(if (isSelected) Color(0xFFD4AF37) else Color(0x33FFFFFF))
                      .clickable { onTaxRateChange(rate) }
                      .padding(vertical = 6.dp),
                    contentAlignment = Alignment.Center
                  ) {
                    Text(
                      text = rate.label.split(" ").first(),
                      fontSize = 10.sp,
                      fontWeight = FontWeight.Bold,
                      color = if (isSelected) Color.Black else Color.White
                    )
                  }
                }
              }
              Spacer(modifier = Modifier.height(6.dp))
              Text(
                text = "Current: ${uiState.taxRate.label} (+${uiState.taxRate.taxPerCitizen * uiState.population} Gold every 5 days, Happiness: ${uiState.taxRate.happinessImpact}%).",
                fontSize = 11.sp,
                color = Color(0xFFFFD54F)
              )
            }
          }
        }

        // Section 3: Royal Decrees
        item {
          Text(
            text = "📜 Royal Decrees & Edicts",
            fontWeight = FontWeight.Bold,
            fontSize = 14.sp,
            color = Color(0xFFFFD54F)
          )
        }

        items(DecreeType.values()) { decree ->
          val isActive = uiState.activeDecrees.any { it.decree == decree }
          val canAfford = uiState.resources.gold >= decree.goldCost

          Surface(
            color = Color(0x334E342E),
            shape = RoundedCornerShape(8.dp),
            border = androidx.compose.foundation.BorderStroke(1.dp, if (isActive) Color(0xFF81C784) else Color(0x22FFFFFF)),
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
                  text = "${decree.iconEmoji} ${decree.title}",
                  fontWeight = FontWeight.Bold,
                  fontSize = 13.sp,
                  color = Color.White
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = decree.description,
                  fontSize = 11.sp,
                  color = Color(0xFFD7CCC8)
                )
                Spacer(modifier = Modifier.height(2.dp))
                Text(
                  text = "Cost: 🪙 ${decree.goldCost} Gold",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.SemiBold,
                  color = Color(0xFFFFD54F)
                )
              }
              Spacer(modifier = Modifier.width(8.dp))
              Button(
                onClick = { onEnactDecree(decree) },
                enabled = !isActive && canAfford,
                colors = ButtonDefaults.buttonColors(
                  containerColor = Color(0xFFD4AF37),
                  contentColor = Color.Black
                ),
                shape = RoundedCornerShape(6.dp)
              ) {
                Text(
                  text = if (isActive) "Active" else "Enact",
                  fontSize = 11.sp,
                  fontWeight = FontWeight.Bold
                )
              }
            }
          }
        }
      }

      Spacer(modifier = Modifier.height(16.dp))
    }
  }
}
