package com.waseefakhtar.doseapp

import android.app.NotificationManager
import android.app.PendingIntent
import android.content.BroadcastReceiver
import android.content.Context
import android.content.Intent
import android.net.Uri
import android.util.Log
import androidx.core.app.NotificationCompat
import com.waseefakhtar.doseapp.analytics.AnalyticsHelper
import com.waseefakhtar.doseapp.domain.model.Medication
import com.waseefakhtar.doseapp.domain.repository.MedicationRepository
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.SupervisorJob
import kotlinx.coroutines.launch
import java.util.Calendar
import javax.inject.Inject

const val MEDICATION_INTENT = "medication_intent"
const val MEDICATION_NOTIFICATION = "medication_notification"

@AndroidEntryPoint
class MedicationNotificationReceiver : BroadcastReceiver() {

    @Inject
    lateinit var analyticsHelper: AnalyticsHelper

    @Inject
    lateinit var repository: MedicationRepository

    @Inject
    lateinit var notificationService: MedicationNotificationService

    private val scope = CoroutineScope(SupervisorJob() + Dispatchers.IO)

    companion object {
        private const val TAG = "MedicationNotifReceiver"
        private const val LIFETIME_RESCHEDULE_DAYS = 90
    }

    override fun onReceive(context: Context?, intent: Intent?) {
        context?.let {
            intent?.getParcelableExtra<Medication>(MEDICATION_INTENT)?.let { medication ->
                showNotification(it, medication)
                rescheduleLifetimeMedication(medication)
            }
        }
    }

    private fun showNotification(context: Context, medication: Medication) {
        // Create deep link intent for notification click
        val activityIntent = Intent(context, MainActivity::class.java).apply {
            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            data = Uri.parse("doseapp://medication/${medication.id}")
        }

        val activityPendingIntent = PendingIntent.getActivity(
            context,
            medication.id.toInt(),
            activityIntent,
            PendingIntent.FLAG_UPDATE_CURRENT or PendingIntent.FLAG_IMMUTABLE
        )

        val notification = NotificationCompat.Builder(
            context,
            MedicationNotificationService.MEDICATION_CHANNEL_ID
        )
            .setSmallIcon(R.drawable.ic_dose)
            .setContentTitle(context.getString(R.string.medication_reminder))
            .setContentText(context.getString(R.string.medication_reminder_time, medication.name))
            .setAutoCancel(true)
            .setContentIntent(activityPendingIntent)
            .build()

        val notificationManager = context.getSystemService(Context.NOTIFICATION_SERVICE) as NotificationManager
        notificationManager.notify(medication.id.toInt(), notification)

        analyticsHelper.trackNotificationShown(medication)
    }

    private fun rescheduleLifetimeMedication(medication: Medication) {
        // Only reschedule if this is a lifetime medication
        if (medication.endDate == null) {
            scope.launch {
                try {
                    // Create a new medication entry 90 days in the future
                    val calendar = Calendar.getInstance()
                    calendar.time = medication.medicationTime
                    calendar.add(Calendar.DAY_OF_YEAR, LIFETIME_RESCHEDULE_DAYS)

                    val nextMedication = Medication(
                        id = 0,
                        name = medication.name,
                        dosage = medication.dosage,
                        frequency = medication.frequency,
                        startDate = medication.startDate,
                        endDate = null, // Keep as lifetime
                        medicationTaken = false,
                        medicationTime = calendar.time,
                        type = medication.type
                    )

                    // Insert the new medication and get the saved version with ID
                    repository.insertMedications(listOf(nextMedication)).collect { savedMedications ->
                        savedMedications.firstOrNull()?.let { medWithId ->
                            // Schedule notification for the new medication
                            notificationService.scheduleNotification(
                                medication = medWithId,
                                analyticsHelper = analyticsHelper
                            )
                            Log.d(
                                TAG,
                                "Rescheduled lifetime medication '${medication.name}' " +
                                        "90 days ahead to ${calendar.time}"
                            )
                        }
                    }
                } catch (e: Exception) {
                    Log.e(TAG, "Failed to reschedule lifetime medication: ${medication.name}", e)
                }
            }
        }
    }
}
