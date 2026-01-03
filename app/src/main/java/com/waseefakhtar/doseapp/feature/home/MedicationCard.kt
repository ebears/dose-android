package com.waseefakhtar.doseapp.feature.home

import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Delete
import androidx.compose.material3.Card
import androidx.compose.material3.CardDefaults
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.luminance
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.tooling.preview.Preview
import androidx.compose.ui.unit.dp
import com.waseefakhtar.doseapp.R
import com.waseefakhtar.doseapp.domain.model.Medication
import com.waseefakhtar.doseapp.util.MedicationType
import java.util.Date

@Composable
fun MedicationCard(
    medication: Medication,
    navigateToMedicationDetail: (Medication) -> Unit,
    onDeleteClick: ((Medication) -> Unit)?
) {
    val (cardColor, boxColor, textColor) = medication.type.getCardColor()
    
    // Choose green color based on card background luminance for optimal contrast
    val checkmarkColor = if (Color(cardColor).luminance() > 0.5f) {
        // Light background: use dark green
        Color(0xFF1B5E20)
    } else {
        // Dark background: use light green
        Color(0xFF4CAF50)
    }

    Card(
        modifier = Modifier
            .fillMaxWidth()
            .padding(horizontal = 16.dp, vertical = 8.dp),
        onClick = { navigateToMedicationDetail(medication) },
        shape = RoundedCornerShape(30.dp),
        colors = CardDefaults.cardColors(
            containerColor = Color(cardColor),
        )
    ) {
        Row(
            modifier = Modifier.padding(24.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Column(
                modifier = Modifier.weight(2f),
                horizontalAlignment = Alignment.Start
            ) {
                Text(
                    text = medication.name,
                    fontWeight = FontWeight.Bold,
                    style = MaterialTheme.typography.titleLarge,
                    color = Color(textColor)
                )

                val doseAndType = "${medication.dosage} ${
                stringResource(
                    when (medication.type) {
                        MedicationType.TABLET -> R.string.tablet
                        MedicationType.CAPSULE -> R.string.capsule
                        MedicationType.SYRUP -> R.string.type_syrup
                        MedicationType.DROPS -> R.string.drops
                        MedicationType.SPRAY -> R.string.spray
                        MedicationType.GEL -> R.string.gel
                    }
                ).lowercase()
                }"

                Text(
                    text = doseAndType,
                    color = Color(textColor)
                )
            }

            Box(
                modifier = Modifier
                    .height(64.dp)
                    .aspectRatio(1f)
                    .border(
                        width = 1.5.dp, 
                        color = if (medication.medicationTaken) checkmarkColor else Color(textColor), 
                        shape = RoundedCornerShape(16.dp)
                    ),
                contentAlignment = Alignment.Center
            ) {
                if (medication.medicationTaken) {
                    Icon(
                        painter = painterResource(R.drawable.ic_check),
                        contentDescription = stringResource(R.string.medication_taken),
                        modifier = Modifier.size(42.dp),
                        tint = checkmarkColor
                    )
                } else {
                    Icon(
                        painter = painterResource(
                            when (medication.type) {
                                MedicationType.TABLET -> R.drawable.ic_tablet
                                MedicationType.CAPSULE -> R.drawable.ic_capsule
                                MedicationType.SYRUP -> R.drawable.ic_syrup
                                MedicationType.DROPS -> R.drawable.ic_drops
                                MedicationType.SPRAY -> R.drawable.ic_spray
                                MedicationType.GEL -> R.drawable.ic_gel
                            }
                        ),
                        contentDescription = stringResource(
                            when (medication.type) {
                                MedicationType.TABLET -> R.string.tablet
                                MedicationType.CAPSULE -> R.string.capsule
                                MedicationType.SYRUP -> R.string.type_syrup
                                MedicationType.DROPS -> R.string.drops
                                MedicationType.SPRAY -> R.string.spray
                                MedicationType.GEL -> R.string.gel
                            }
                        ),
                        modifier = Modifier.size(42.dp),
                        tint = Color(textColor)
                    )
                }
            }

            if (onDeleteClick != null) {
                IconButton(
                    modifier = Modifier.padding(start = 8.dp),
                    onClick = { onDeleteClick(medication) }
                ) {
                    Icon(
                        imageVector = Icons.Default.Delete,
                        contentDescription = stringResource(R.string.delete),
                        tint = MaterialTheme.colorScheme.error
                    )
                }
            }
        }
    }
}

@Preview
@Composable
private fun MedicationCardTakeNowPreview() {
    MedicationCard(
        medication = Medication(
            id = 123L,
            name = "A big big name for a little medication I needs to take",
            dosage = 1,
            frequency = "2",
            startDate = Date(),
            endDate = Date(),
            medicationTime = Date(),
            medicationTaken = false
        ),
        navigateToMedicationDetail = {},
        onDeleteClick = null
    )
}

@Preview
@Composable
private fun MedicationCardTakenPreview() {
    MedicationCard(
        medication = Medication(
            id = 123L,
            name = "A big big name for a little medication I needs to take",
            dosage = 1,
            frequency = "2",
            startDate = Date(),
            endDate = Date(),
            medicationTime = Date(),
            medicationTaken = true,
            type = MedicationType.TABLET
        ),
        navigateToMedicationDetail = {},
        onDeleteClick = null
    )
}
