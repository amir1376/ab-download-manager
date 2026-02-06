package com.abdownloadmanager.android.storage

import androidx.compose.runtime.Immutable
import androidx.datastore.core.DataStore
import com.abdownloadmanager.shared.util.ConfigBaseSettingsByJson
import kotlinx.serialization.Serializable

@Serializable
@Immutable
data class BrowserBookmark(
    val url: String,
    val title: String,
)

class BrowserBookmarksStorage(
    dataStore: DataStore<List<BrowserBookmark>>,
) : ConfigBaseSettingsByJson<List<BrowserBookmark>>(dataStore) {
    val bookmarksFlow = data
}
