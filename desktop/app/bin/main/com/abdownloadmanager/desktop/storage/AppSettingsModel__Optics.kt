package com.abdownloadmanager.desktop.storage



public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`theme`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.String>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`theme` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.String ->
  appSettingsModel.copy(`theme` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`defaultDarkTheme`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.String>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`defaultDarkTheme` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.String ->
  appSettingsModel.copy(`defaultDarkTheme` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`defaultLightTheme`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.String>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`defaultLightTheme` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.String ->
  appSettingsModel.copy(`defaultLightTheme` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`language`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.String?>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`language` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.String? ->
  appSettingsModel.copy(`language` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`font`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.String?>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`font` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.String? ->
  appSettingsModel.copy(`font` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`uiScale`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Float?>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`uiScale` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Float? ->
  appSettingsModel.copy(`uiScale` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`mergeTopBarWithTitleBar`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`mergeTopBarWithTitleBar` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`mergeTopBarWithTitleBar` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`useNativeMenuBar`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`useNativeMenuBar` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`useNativeMenuBar` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`showIconLabels`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`showIconLabels` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`showIconLabels` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`useRelativeDateTime`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`useRelativeDateTime` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`useRelativeDateTime` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`useSystemTray`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`useSystemTray` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`useSystemTray` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`threadCount`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Int>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`threadCount` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Int ->
  appSettingsModel.copy(`threadCount` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`maxConcurrentDownloads`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Int>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`maxConcurrentDownloads` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Int ->
  appSettingsModel.copy(`maxConcurrentDownloads` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`maxDownloadRetryCount`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Int>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`maxDownloadRetryCount` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Int ->
  appSettingsModel.copy(`maxDownloadRetryCount` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`dynamicPartCreation`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`dynamicPartCreation` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`dynamicPartCreation` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`useServerLastModifiedTime`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`useServerLastModifiedTime` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`useServerLastModifiedTime` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`appendExtensionToIncompleteDownloads`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`appendExtensionToIncompleteDownloads` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`appendExtensionToIncompleteDownloads` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`useSparseFileAllocation`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`useSparseFileAllocation` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`useSparseFileAllocation` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`useAverageSpeed`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`useAverageSpeed` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`useAverageSpeed` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`showDownloadProgressDialog`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`showDownloadProgressDialog` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`showDownloadProgressDialog` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`showDownloadCompletionDialog`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`showDownloadCompletionDialog` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`showDownloadCompletionDialog` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`speedLimit`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Long>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`speedLimit` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Long ->
  appSettingsModel.copy(`speedLimit` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`autoStartOnBoot`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`autoStartOnBoot` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`autoStartOnBoot` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`notificationSound`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`notificationSound` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`notificationSound` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`generalNotificationSound`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.String>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`generalNotificationSound` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.String ->
  appSettingsModel.copy(`generalNotificationSound` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`successNotificationSound`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.String>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`successNotificationSound` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.String ->
  appSettingsModel.copy(`successNotificationSound` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`errorNotificationSound`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.String>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`errorNotificationSound` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.String ->
  appSettingsModel.copy(`errorNotificationSound` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`defaultDownloadFolder`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.String>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`defaultDownloadFolder` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.String ->
  appSettingsModel.copy(`defaultDownloadFolder` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`browserIntegrationEnabled`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`browserIntegrationEnabled` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`browserIntegrationEnabled` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`browserIntegrationPort`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Int>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`browserIntegrationPort` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Int ->
  appSettingsModel.copy(`browserIntegrationPort` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`trackDeletedFilesOnDisk`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`trackDeletedFilesOnDisk` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`trackDeletedFilesOnDisk` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`deletePartialFileOnDownloadCancellation`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`deletePartialFileOnDownloadCancellation` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`deletePartialFileOnDownloadCancellation` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`sizeUnit`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, com.abdownloadmanager.shared.storage.SupportedSizeUnits>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`sizeUnit` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: com.abdownloadmanager.shared.storage.SupportedSizeUnits ->
  appSettingsModel.copy(`sizeUnit` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`speedUnit`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, com.abdownloadmanager.shared.storage.SupportedSizeUnits>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`speedUnit` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: com.abdownloadmanager.shared.storage.SupportedSizeUnits ->
  appSettingsModel.copy(`speedUnit` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`ignoreSSLCertificates`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`ignoreSSLCertificates` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`ignoreSSLCertificates` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`useCategoryByDefault`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.Boolean>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`useCategoryByDefault` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.Boolean ->
  appSettingsModel.copy(`useCategoryByDefault` = value)
}
)

public  val com.abdownloadmanager.desktop.storage.AppSettingsModel.Companion.`userAgent`: arrow.optics.Lens<com.abdownloadmanager.desktop.storage.AppSettingsModel, kotlin.String>  get() = arrow.optics.Lens(
  get = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel -> appSettingsModel.`userAgent` },
  set = { appSettingsModel: com.abdownloadmanager.desktop.storage.AppSettingsModel, value: kotlin.String ->
  appSettingsModel.copy(`userAgent` = value)
}
)
