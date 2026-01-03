package com.waseefakhtar.doseapp.feature.medicationconfirm.viewmodel

import android.util.Log
import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.google.firebase.crashlytics.FirebaseCrashlytics
import com.waseefakhtar.doseapp.MedicationNotificationService
import com.waseefakhtar.doseapp.analytics.AnalyticsHelper
import com.waseefakhtar.doseapp.feature.medicationconfirm.usecase.AddMedicationUseCase
import dagger.hilt.android.lifecycle.HiltViewModel
import kotlinx.coroutines.flow.MutableSharedFlow
import kotlinx.coroutines.flow.asSharedFlow
import kotlinx.coroutines.launch
import javax.inject.Inject

@HiltViewModel
class MedicationConfirmViewModel @Inject constructor(
    private val addMedicationUseCase: AddMedicationUseCase,
    private val medicationNotificationService: MedicationNotificationService,
    private val analyticsHelper: AnalyticsHelper
) : ViewModel() {
    private val _isMedicationSaved = MutableSharedFlow<Unit>()
    val isMedicationSaved = _isMedicationSaved.asSharedFlow()

    companion object {
        private const val TAG = "MedicationConfirmVM"
        private const val MAX_NOTIFICATIONS_TO_SCHEDULE = 90 // Limit notifications to 90 days
    }

    fun addMedication(state: MedicationConfirmState) {
        viewModelScope.launch {
            val medications = state.medications
            addMedicationUseCase.addMedication(medications).collect { savedMedications ->
                try {
                    // Limit notification scheduling to prevent crashes with lifetime medications
                    val medicationsToSchedule = if (savedMedications.size > MAX_NOTIFICATIONS_TO_SCHEDULE) {
                        Log.w(
                            TAG,
                            "Limiting notification scheduling to $MAX_NOTIFICATIONS_TO_SCHEDULE " +
                                    "out of ${savedMedications.size} medications"
                        )
                        FirebaseCrashlytics.getInstance().log(
                            "Limited notification scheduling: $MAX_NOTIFICATIONS_TO_SCHEDULE " +
                                    "out of ${savedMedications.size} medications"
                        )
                        savedMedications.take(MAX_NOTIFICATIONS_TO_SCHEDULE)
                    } else {
                        savedMedications
                    }

                    // Schedule notifications for the limited set of medications
                    medicationsToSchedule.forEach { medication ->
                        try {
                            medicationNotificationService.scheduleNotification(
                                medication = medication,
                                analyticsHelper = analyticsHelper
                            )
                        } catch (e: Exception) {
                            Log.e(TAG, "Failed to schedule notification for medication: ${medication.name}", e)
                            FirebaseCrashlytics.getInstance().recordException(e)
                        }
                    }

                    _isMedicationSaved.emit(Unit)
                } catch (e: Exception) {
                    Log.e(TAG, "Error saving medications", e)
                    FirebaseCrashlytics.getInstance().recordException(e)
                    // Still emit save complete even if some notifications failed
                    _isMedicationSaved.emit(Unit)
                }
            }
        }
    }

    fun logEvent(eventName: String) {
        analyticsHelper.logEvent(eventName = eventName)
    }
}
