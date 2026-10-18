package com.myplanetservices.stillwords.util

import androidx.activity.ComponentActivity
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.IntentSenderRequest
import androidx.activity.result.contract.ActivityResultContracts
import com.google.android.play.core.appupdate.AppUpdateInfo
import com.google.android.play.core.appupdate.AppUpdateManager
import com.google.android.play.core.appupdate.AppUpdateManagerFactory
import com.google.android.play.core.appupdate.AppUpdateOptions
import com.google.android.play.core.install.InstallStateUpdatedListener
import com.google.android.play.core.install.model.AppUpdateType
import com.google.android.play.core.install.model.InstallStatus
import com.google.android.play.core.install.model.UpdateAvailability

class InAppUpdateHelper(
    activity: ComponentActivity,
    private val onUpdateDownloaded: () -> Unit
) {
    private val appUpdateManager: AppUpdateManager = AppUpdateManagerFactory.create(activity)

    private val installStateUpdatedListener = InstallStateUpdatedListener { state ->
        if (state.installStatus() == InstallStatus.DOWNLOADED) {
            onUpdateDownloaded()
        }
    }

    @android.annotation.SuppressLint("InvalidFragmentVersionForActivityResult")
    private val updateLauncher: ActivityResultLauncher<IntentSenderRequest> =
        activity.registerForActivityResult(ActivityResultContracts.StartIntentSenderForResult()) {
            // Flexible update user flow result
        }

    fun start() {
        try {
            appUpdateManager.registerListener(installStateUpdatedListener)
            checkForUpdate()
        } catch (e: Exception) {
            // Fail silently
        }
    }

    fun checkForUpdate() {
        try {
            appUpdateManager.appUpdateInfo.addOnSuccessListener { info ->
                handleUpdateInfo(info)
            }.addOnFailureListener {
                // Fail silently without Google Play or offline
            }
        } catch (e: Exception) {
            // Fail silently
        }
    }

    private fun handleUpdateInfo(info: AppUpdateInfo) {
        if (info.installStatus() == InstallStatus.DOWNLOADED) {
            onUpdateDownloaded()
        } else if (info.updateAvailability() == UpdateAvailability.UPDATE_AVAILABLE &&
            info.isUpdateTypeAllowed(AppUpdateType.FLEXIBLE)
        ) {
            try {
                appUpdateManager.startUpdateFlowForResult(
                    info,
                    updateLauncher,
                    AppUpdateOptions.newBuilder(AppUpdateType.FLEXIBLE).build()
                )
            } catch (e: Exception) {
                // Fail silently
            }
        }
    }

    fun completeUpdate() {
        try {
            appUpdateManager.completeUpdate()
        } catch (e: Exception) {
            // Fail silently
        }
    }

    fun unregister() {
        try {
            appUpdateManager.unregisterListener(installStateUpdatedListener)
        } catch (e: Exception) {
            // Fail silently
        }
    }
}

