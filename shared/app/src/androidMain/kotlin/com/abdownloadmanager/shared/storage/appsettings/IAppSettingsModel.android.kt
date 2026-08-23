package com.abdownloadmanager.shared.storage.appsettings

import arrow.optics.optics
import com.abdownloadmanager.shared.storage.SupportedSizeUnits
import io.github.amir1376.schemakt.S
import io.github.amir1376.schemakt.schema.composite.TypeSafeObjectSchema
import io.github.amir1376.schemakt.schema.composite.typeSafeObject
import io.github.amir1376.schemakt.schema.modifier.catch
import io.github.amir1376.schemakt.schema.primitive.boolean
import ir.amirab.util.config.datastore.asSettingsSchema
import kotlinx.serialization.Serializable


@optics([arrow.optics.OpticsTarget.LENS])
@Serializable
data class AppSettingsModel(
    override val theme: String,
    override val defaultDarkTheme: String,
    override val defaultLightTheme: String,
    override val language: String?,
    override val font: String?,
    override val uiScale: Float?,
    override val showIconLabels: Boolean,
    override val useRelativeDateTime: Boolean,
    override val threadCount: Int,
    override val maxConcurrentDownloads: Int,
    override val maxDownloadRetryCount: Int,
    override val dynamicPartCreation: Boolean,
    override val useServerLastModifiedTime: Boolean,
    override val appendExtensionToIncompleteDownloads: Boolean,
    override val useSparseFileAllocation: Boolean,
    override val useAverageSpeed: Boolean,
    override val showDownloadProgressDialog: Boolean,
    override val showDownloadCompletionDialog: Boolean,
    override val speedLimit: Long,
    override val autoStartOnBoot: Boolean,
    override val notificationSound: Boolean,
    override val generalNotificationSound: String,
    override val errorNotificationSound: String,
    override val successNotificationSound: String,
    override val defaultDownloadFolder: String,
    override val apiEnabled: Boolean,
    override val apiPort: Int,
    override val apiAuthKey: String,
    override val apiAuthEnabled: Boolean,
    override val trackDeletedFilesOnDisk: Boolean,
    override val deletePartialFileOnDownloadCancellation: Boolean,
    override val sizeUnit: SupportedSizeUnits,
    override val speedUnit: SupportedSizeUnits,
    override val ignoreSSLCertificates: Boolean,
    override val useCategoryByDefault: Boolean,
    override val userAgent: String,
    val browserIconInLauncher: Boolean,
) : IAppSettingsModel {
    companion object {
    }
}

private val AndroidSettingsSchema = S.typeSafeObject(
    definition = {
        BaseAppSettingsDefinition.sharedAppSettingsTypeSafeDefinition()

        prop(AppSettingsModel::browserIconInLauncher) bind S.boolean()
            .catch(PlatformDefaultSettings::browserIconInLauncher)
    },
    factory = {
        PlatformAppSettingsModel(
            theme = it[AppSettingsModel::theme],
            defaultDarkTheme = it[AppSettingsModel::defaultDarkTheme],
            defaultLightTheme = it[AppSettingsModel::defaultLightTheme],
            language = it[AppSettingsModel::language],
            font = it[AppSettingsModel::font],
            uiScale = it[AppSettingsModel::uiScale],
            showIconLabels = it[AppSettingsModel::showIconLabels],
            useRelativeDateTime = it[AppSettingsModel::useRelativeDateTime],
            threadCount = it[AppSettingsModel::threadCount],
            maxConcurrentDownloads = it[AppSettingsModel::maxConcurrentDownloads],
            maxDownloadRetryCount = it[AppSettingsModel::maxDownloadRetryCount],
            dynamicPartCreation = it[AppSettingsModel::dynamicPartCreation],
            useServerLastModifiedTime = it[AppSettingsModel::useServerLastModifiedTime],
            appendExtensionToIncompleteDownloads = it[AppSettingsModel::appendExtensionToIncompleteDownloads],
            useSparseFileAllocation = it[AppSettingsModel::useSparseFileAllocation],
            useAverageSpeed = it[AppSettingsModel::useAverageSpeed],
            showDownloadProgressDialog = it[AppSettingsModel::showDownloadProgressDialog],
            showDownloadCompletionDialog = it[AppSettingsModel::showDownloadCompletionDialog],
            speedLimit = it[AppSettingsModel::speedLimit],
            autoStartOnBoot = it[AppSettingsModel::autoStartOnBoot],
            notificationSound = it[AppSettingsModel::notificationSound],
            generalNotificationSound = it[AppSettingsModel::generalNotificationSound],
            errorNotificationSound = it[AppSettingsModel::errorNotificationSound],
            successNotificationSound = it[AppSettingsModel::successNotificationSound],
            defaultDownloadFolder = it[AppSettingsModel::defaultDownloadFolder],
            apiEnabled = it[AppSettingsModel::apiEnabled],
            apiPort = it[AppSettingsModel::apiPort],
            apiAuthEnabled = it[AppSettingsModel::apiAuthEnabled],
            apiAuthKey = it[AppSettingsModel::apiAuthKey],
            trackDeletedFilesOnDisk = it[AppSettingsModel::trackDeletedFilesOnDisk],
            deletePartialFileOnDownloadCancellation = it[AppSettingsModel::deletePartialFileOnDownloadCancellation],
            sizeUnit = it[AppSettingsModel::sizeUnit],
            speedUnit = it[AppSettingsModel::speedUnit],
            ignoreSSLCertificates = it[AppSettingsModel::ignoreSSLCertificates],
            useCategoryByDefault = it[AppSettingsModel::useCategoryByDefault],
            userAgent = it[AppSettingsModel::userAgent],

            browserIconInLauncher = it[AppSettingsModel::browserIconInLauncher],
        )
    }
).asSettingsSchema()

actual typealias PlatformAppSettingsModel = AppSettingsModel

actual val PlatformAppSettingsSchema = AndroidSettingsSchema