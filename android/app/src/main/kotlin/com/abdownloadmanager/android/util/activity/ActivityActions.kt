package com.abdownloadmanager.android.util.activity

import android.content.Intent
import com.abdownloadmanager.shared.util.mvi.ContainsEffects

interface ActivityActions {
    fun startActivityAction(intent: Intent)
    fun finishActivityAction()
}
