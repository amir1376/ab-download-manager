package com.abdownloadmanager.shared.util

import com.xeton.util.datasize.ConvertSizeConfig
import kotlinx.coroutines.flow.StateFlow

interface SizeAndSpeedUnitProvider {
    val sizeUnit: StateFlow<ConvertSizeConfig>
    val speedUnit: StateFlow<ConvertSizeConfig>
}
