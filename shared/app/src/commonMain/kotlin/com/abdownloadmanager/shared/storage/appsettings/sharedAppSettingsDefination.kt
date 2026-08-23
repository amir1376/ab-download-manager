package com.abdownloadmanager.shared.storage.appsettings

import com.abdownloadmanager.shared.storage.SupportedSizeUnits
import com.abdownloadmanager.shared.util.MaximumDownloadRetriesLimitation
import com.abdownloadmanager.shared.util.schemakt.enum
import io.github.amir1376.schemakt.S
import io.github.amir1376.schemakt.schema.composite.TypeSafeObjectSchemaBuilder
import io.github.amir1376.schemakt.schema.modifier.catch
import io.github.amir1376.schemakt.schema.modifier.min
import io.github.amir1376.schemakt.schema.modifier.nullable
import io.github.amir1376.schemakt.schema.modifier.range
import io.github.amir1376.schemakt.schema.primitive.boolean
import io.github.amir1376.schemakt.schema.primitive.float
import io.github.amir1376.schemakt.schema.primitive.int
import io.github.amir1376.schemakt.schema.primitive.long
import io.github.amir1376.schemakt.schema.primitive.string

object BaseAppSettingsDefinition {
    context(builder: TypeSafeObjectSchemaBuilder<PlatformAppSettingsModel>)
    fun sharedAppSettingsTypeSafeDefinition() = builder.run {
        prop(IAppSettingsModel::theme) bind S.string().catch(PlatformDefaultSettings::theme)
        prop(IAppSettingsModel::defaultDarkTheme) bind S.string().catch(PlatformDefaultSettings::defaultDarkTheme)
        prop(IAppSettingsModel::defaultLightTheme) bind S.string().catch(PlatformDefaultSettings::defaultLightTheme)
        prop(IAppSettingsModel::language) bind S.string().nullable().catch(PlatformDefaultSettings::language)
        prop(IAppSettingsModel::font) bind S.string().nullable().catch(PlatformDefaultSettings::font)
        prop(IAppSettingsModel::uiScale) bind S.float().nullable().catch(PlatformDefaultSettings::uiScale)
        prop(IAppSettingsModel::showIconLabels) bind S.boolean().catch(PlatformDefaultSettings::showIconLabels)
        prop(IAppSettingsModel::useRelativeDateTime) bind S.boolean()
            .catch(PlatformDefaultSettings::useRelativeDateTime)
        prop(IAppSettingsModel::threadCount) bind S.int().catch(PlatformDefaultSettings::threadCount)
        prop(IAppSettingsModel::maxConcurrentDownloads) bind S.int()
            .range(0, MaximumDownloadRetriesLimitation.MAX_ALLOWED_RETRIES)
            .catch(PlatformDefaultSettings::maxConcurrentDownloads)
        prop(IAppSettingsModel::maxDownloadRetryCount) bind S.int()
            .range(0, MaximumDownloadRetriesLimitation.MAX_ALLOWED_RETRIES).catch(
                PlatformDefaultSettings::maxDownloadRetryCount
            )
        prop(IAppSettingsModel::dynamicPartCreation) bind S.boolean()
            .catch(PlatformDefaultSettings::dynamicPartCreation)
        prop(IAppSettingsModel::useServerLastModifiedTime) bind S.boolean()
            .catch(PlatformDefaultSettings::useServerLastModifiedTime)
        prop(IAppSettingsModel::appendExtensionToIncompleteDownloads) bind S.boolean()
            .catch(PlatformDefaultSettings::appendExtensionToIncompleteDownloads)
        prop(IAppSettingsModel::useSparseFileAllocation) bind S.boolean()
            .catch(PlatformDefaultSettings::useSparseFileAllocation)
        prop(IAppSettingsModel::useAverageSpeed) bind S.boolean().catch(PlatformDefaultSettings::useAverageSpeed)
        prop(IAppSettingsModel::showDownloadProgressDialog) bind S.boolean()
            .catch(PlatformDefaultSettings::showDownloadProgressDialog)
        prop(IAppSettingsModel::showDownloadCompletionDialog) bind S.boolean()
            .catch(PlatformDefaultSettings::showDownloadCompletionDialog)
        prop(IAppSettingsModel::speedLimit) bind S.long().min(0L).catch(PlatformDefaultSettings::speedLimit)
        prop(IAppSettingsModel::autoStartOnBoot) bind S.boolean().catch(PlatformDefaultSettings::autoStartOnBoot)
        prop(IAppSettingsModel::notificationSound) bind S.boolean().catch(PlatformDefaultSettings::notificationSound)
        prop(IAppSettingsModel::generalNotificationSound) bind S.string()
            .catch(PlatformDefaultSettings::generalNotificationSound)
        prop(IAppSettingsModel::errorNotificationSound) bind S.string()
            .catch(PlatformDefaultSettings::errorNotificationSound)
        prop(IAppSettingsModel::successNotificationSound) bind S.string()
            .catch(PlatformDefaultSettings::successNotificationSound)
        prop(IAppSettingsModel::defaultDownloadFolder) bind S.string()
            .catch(PlatformDefaultSettings::defaultDownloadFolder)
        prop(IAppSettingsModel::apiEnabled) bind S.boolean().catch(PlatformDefaultSettings::apiEnabled)
        prop(IAppSettingsModel::apiPort) bind S.int().range(0, 65000).catch(PlatformDefaultSettings::apiPort)
        prop(IAppSettingsModel::apiAuthKey) bind S.string().catch(PlatformDefaultSettings::apiAuthKey)
        prop(IAppSettingsModel::apiAuthEnabled) bind S.boolean().catch(PlatformDefaultSettings::apiAuthEnabled)
        prop(IAppSettingsModel::trackDeletedFilesOnDisk) bind S.boolean()
            .catch(PlatformDefaultSettings::trackDeletedFilesOnDisk)
        prop(IAppSettingsModel::deletePartialFileOnDownloadCancellation) bind S.boolean()
            .catch(PlatformDefaultSettings::deletePartialFileOnDownloadCancellation)
        prop(IAppSettingsModel::sizeUnit) bind S.enum<SupportedSizeUnits>().catch(PlatformDefaultSettings::sizeUnit)
        prop(IAppSettingsModel::speedUnit) bind S.enum<SupportedSizeUnits>().catch(PlatformDefaultSettings::speedUnit)
        prop(IAppSettingsModel::ignoreSSLCertificates) bind S.boolean()
            .catch(PlatformDefaultSettings::ignoreSSLCertificates)
        prop(IAppSettingsModel::useCategoryByDefault) bind S.boolean()
            .catch(PlatformDefaultSettings::useCategoryByDefault)
        prop(IAppSettingsModel::userAgent) bind S.string().catch(PlatformDefaultSettings::userAgent)
    }
}

