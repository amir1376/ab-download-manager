package com.abdownloadmanager.shared.pagemanager

import com.abdownloadmanager.shared.pages.adddownload.AddDownloadCredentialsInUiProps
import com.abdownloadmanager.shared.pages.adddownload.ImportOptions

interface AddDownloadDialogManager {
    fun closeAddDownloadDialog()
    fun openAddDownloadDialog(
        links: List<AddDownloadCredentialsInUiProps>,
        importOptions: ImportOptions = ImportOptions(),
    )
}
