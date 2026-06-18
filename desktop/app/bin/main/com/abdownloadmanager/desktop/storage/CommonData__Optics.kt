package com.abdownloadmanager.desktop.storage



public  val com.abdownloadmanager.desktop.storage.CommonData.Companion.`lastSavedLocations`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.CommonData, kotlin.collections.List<kotlin.String>>  get() = arrow.optics.Lens(
  get = { commonData: com.abdownloadmanager.desktop.storage.CommonData -> commonData.`lastSavedLocations` },
  set = { commonData: com.abdownloadmanager.desktop.storage.CommonData, value: kotlin.collections.List<kotlin.String> ->
  commonData.copy(`lastSavedLocations` = value)
}
)

public  val <__S> arrow.optics.Lens<__S, com.abdownloadmanager.desktop.storage.CommonData>.`lastSavedLocations`: arrow.optics.Lens<__S, kotlin.collections.List<kotlin.String>>  get() = this + com.abdownloadmanager.desktop.storage.CommonData.`lastSavedLocations`
public  val <__S> arrow.optics.Optional<__S, com.abdownloadmanager.desktop.storage.CommonData>.`lastSavedLocations`: arrow.optics.Optional<__S, kotlin.collections.List<kotlin.String>>  get() = this + com.abdownloadmanager.desktop.storage.CommonData.`lastSavedLocations`
public  val <__S> arrow.optics.Traversal<__S, com.abdownloadmanager.desktop.storage.CommonData>.`lastSavedLocations`: arrow.optics.Traversal<__S, kotlin.collections.List<kotlin.String>>  get() = this + com.abdownloadmanager.desktop.storage.CommonData.`lastSavedLocations`
