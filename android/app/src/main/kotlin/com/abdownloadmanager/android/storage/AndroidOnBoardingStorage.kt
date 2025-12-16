package com.abdownloadmanager.android.storage

import androidx.datastore.core.DataStore
import arrow.optics.optics
import com.abdownloadmanager.shared.util.ConfigBaseSettingsByJson
import kotlinx.serialization.Serializable

@optics
@Serializable
data class OnBoardingData(
    val initialSetupPassed: Boolean = false,
    val permissionsPassedAtLeastOnce: Boolean = false,
) {
    companion object
}

class AndroidOnBoardingStorage(
    dataStore: DataStore<OnBoardingData>,
) : ConfigBaseSettingsByJson<OnBoardingData>(dataStore) {
    val onBoardingFlow = data

    val initialSetupPassed = from(OnBoardingData.initialSetupPassed)
    val permissionsPassedAtLeastOnce = from(OnBoardingData.permissionsPassedAtLeastOnce)

}
