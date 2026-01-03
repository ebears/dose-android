package com.waseefakhtar.doseapp.feature.home.usecase

import com.waseefakhtar.doseapp.domain.model.Medication
import com.waseefakhtar.doseapp.domain.repository.MedicationRepository
import javax.inject.Inject

class DeleteMedicationUseCase @Inject constructor(
    private val repository: MedicationRepository
) {

    suspend fun deleteMedication(medication: Medication) {
        val isPastDose = medication.medicationTime.time < System.currentTimeMillis()
        
        if (isPastDose) {
            // Delete only this single past dose
            repository.deleteMedication(medication)
        } else {
            // Delete all future doses of this medication
            repository.deleteFutureMedicationDoses(medication)
        }
        Unit
    }

    suspend fun deleteFutureMedicationDoses(medication: Medication): Int {
        return repository.deleteFutureMedicationDoses(medication)
    }
}
