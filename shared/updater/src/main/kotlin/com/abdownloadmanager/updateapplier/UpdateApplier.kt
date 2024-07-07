package com.abdownloadmanager.updateapplier

abstract class UpdateApplier{
    abstract suspend fun applyUpdate()
}