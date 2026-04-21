package com.messark.hawkerrush.ui.components

import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.material3.Button
import androidx.compose.material3.ButtonDefaults
import androidx.compose.material3.Text
import androidx.compose.ui.text.SpanStyle
import androidx.compose.ui.text.buildAnnotatedString
import androidx.compose.ui.text.withStyle
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import androidx.compose.ui.unit.sp
import com.messark.hawkerrush.model.Stall
import com.messark.hawkerrush.model.StallType

@Composable
fun StallConsole(
    stall: Stall,
    baseStall: Stall,
    onSell: () -> Unit,
    onUpgrade: () -> Unit,
    onCycleTarget: () -> Unit,
    canAffordUpgrade: Boolean,
    upgradeCost: Int,
    modifier: Modifier = Modifier
) {
    Column(
        modifier = modifier
            .fillMaxSize()
            .padding(4.dp),
        horizontalAlignment = Alignment.CenterHorizontally,
        verticalArrangement = Arrangement.SpaceBetween
    ) {
        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.SpaceBetween,
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(modifier = Modifier.weight(1f)) {
                Text(text = stall.name.uppercase(), color = Color.White, fontSize = 14.sp)
                if (stall.upgrades.isEmpty()) {
                    Text(text = "No upgrades", color = Color.Gray, fontSize = 10.sp)
                } else {
                    val upgradeText = stall.upgrades.entries.joinToString(", ") { (key, value) ->
                        val benefit = stall.getUpgradeBenefit(key, value, baseStall)
                        if (benefit.isNotEmpty()) "$key: $value ($benefit)" else "$key: $value"
                    }
                    Text(text = upgradeText, color = Color.Gray, fontSize = 10.sp)
                }
            }
            Column(horizontalAlignment = Alignment.End) {
                Text(text = "Target: ${stall.targetMode.name}", color = Color.Cyan, fontSize = 12.sp, modifier = Modifier.clickable { onCycleTarget() })
                Text(text = "Targets: ${stall.uniqueTargetIds.size} | Kills: ${stall.kills}", color = Color.Green, fontSize = 10.sp)
            }
        }

        Row(
            modifier = Modifier.fillMaxWidth(),
            horizontalArrangement = Arrangement.spacedBy(4.dp)
        ) {
            Button(
                onClick = onSell,
                modifier = Modifier.weight(1f).height(36.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = "SELL $${(stall.totalInvestment * 0.5f).toInt()}", fontSize = 10.sp)
            }
            Button(
                onClick = onUpgrade,
                modifier = Modifier.weight(1f).height(36.dp),
                enabled = canAffordUpgrade,
                colors = ButtonDefaults.buttonColors(
                    disabledContainerColor = Color.DarkGray,
                    disabledContentColor = Color.Gray
                ),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(
                    text = buildAnnotatedString {
                        append("UPGRADE ")
                        withStyle(style = SpanStyle(color = if (canAffordUpgrade) Color.Yellow else Color.Red)) {
                            append("$$upgradeCost")
                        }
                    },
                    fontSize = 10.sp
                )
            }
            Button(
                onClick = onCycleTarget,
                modifier = Modifier.weight(1f).height(36.dp),
                contentPadding = PaddingValues(0.dp)
            ) {
                Text(text = "TARGET", fontSize = 10.sp)
            }
        }
    }
}
