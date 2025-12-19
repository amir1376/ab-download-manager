package com.abdownloadmanager.android.di

import AndroidDirectLinkUpdateApplier
import android.app.Application
import android.content.Context
import com.abdownloadmanager.github.GithubApi
import com.abdownloadmanager.UpdateDownloadLocationProvider
import com.abdownloadmanager.UpdateManager
import com.abdownloadmanager.android.ABDMApp
import com.abdownloadmanager.android.pages.home.HomePageStateToPersist
import com.abdownloadmanager.android.pages.onboarding.permissions.ABDMPermissions
import com.abdownloadmanager.android.pages.onboarding.permissions.PermissionManager
import com.abdownloadmanager.android.receiver.StartOnBootBroadcastReceiver
import com.abdownloadmanager.android.storage.AndroidExtraDownloadItemSettings
import com.abdownloadmanager.android.storage.AndroidExtraQueueSettings
import com.abdownloadmanager.android.storage.AndroidOnBoardingStorage
import com.abdownloadmanager.android.storage.AppSettingsStorage
import com.abdownloadmanager.android.storage.HomePageStorage
import com.abdownloadmanager.android.storage.OnBoardingData
import com.abdownloadmanager.android.util.ABDMAppManager
import com.abdownloadmanager.android.util.ABDMServiceNotificationManager
import com.abdownloadmanager.android.util.AndroidDefinedPaths
import com.abdownloadmanager.android.util.AndroidDownloadItemOpener
import com.abdownloadmanager.android.util.AppInfo
import com.abdownloadmanager.shared.util.SharedConstants
import com.abdownloadmanager.shared.ui.theme.ThemeManager
import ir.amirab.downloader.queue.QueueManager
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.theme.ISystemThemeDetector
import ir.amirab.downloader.DownloadManagerMinimalControl
import ir.amirab.downloader.DownloadSettings
import ir.amirab.downloader.connection.HttpDownloaderClient
import ir.amirab.downloader.connection.OkHttpHttpDownloaderClient
import ir.amirab.downloader.db.*
import ir.amirab.downloader.monitor.DownloadMonitor
import ir.amirab.downloader.utils.IDiskStat
import com.abdownloadmanager.resources.ABDMLanguageResources
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.downloaderinui.hls.HLSDownloaderInUi
import com.abdownloadmanager.shared.downloaderinui.http.HttpDownloaderInUi
import com.abdownloadmanager.shared.repository.BaseAppRepository
import com.abdownloadmanager.shared.storage.BaseAppSettingsStorage
import com.abdownloadmanager.shared.storage.ExtraDownloadSettingsStorage
import com.abdownloadmanager.shared.storage.ExtraQueueSettingsStorage
import com.abdownloadmanager.shared.storage.IExtraDownloadSettingsStorage
import com.abdownloadmanager.shared.storage.IExtraQueueSettingsStorage
import com.abdownloadmanager.shared.storage.ILastSavedLocationsStorage
import com.abdownloadmanager.shared.storage.PerHostSettingsDatastoreStorage
import com.abdownloadmanager.shared.storage.ProxyDatastoreStorage
import com.abdownloadmanager.shared.storage.impl.LastSavedLocationStorage
import com.abdownloadmanager.shared.ui.theme.ThemeSettingsStorage
import com.abdownloadmanager.shared.ui.widget.NotificationManager
import com.abdownloadmanager.shared.updater.UpdateDownloaderViaDownloadSystem
import com.abdownloadmanager.shared.util.AndroidDiskStat
import com.abdownloadmanager.shared.util.AndroidSystemThemeDetector
import com.abdownloadmanager.shared.util.AppVersion
import com.abdownloadmanager.shared.util.DefinedPaths
import com.abdownloadmanager.shared.util.SizeAndSpeedUnitProvider
import com.abdownloadmanager.shared.util.UserAgentProviderFromSettings
import com.abdownloadmanager.shared.util.*
import com.abdownloadmanager.updateapplier.UpdateApplier
import ir.amirab.downloader.DownloadManager
import ir.amirab.util.config.datastore.createMapConfigDatastore
import kotlinx.coroutines.*
import kotlinx.serialization.json.Json
import okhttp3.Dispatcher
import okhttp3.OkHttpClient
import org.koin.core.component.KoinComponent
import org.koin.core.context.startKoin
import org.koin.dsl.bind
import org.koin.dsl.module
import com.abdownloadmanager.updatechecker.GithubUpdateChecker
import com.abdownloadmanager.updatechecker.UpdateChecker
import ir.amirab.util.AppVersionTracker
import com.abdownloadmanager.shared.util.appinfo.PreviousVersion
import com.abdownloadmanager.shared.util.autoremove.RemovedDownloadsFromDiskTracker
import com.abdownloadmanager.shared.util.category.*
import com.abdownloadmanager.shared.util.ondownloadcompletion.NoOpOnDownloadCompletionActionProvider
import com.abdownloadmanager.shared.util.ondownloadcompletion.OnDownloadCompletionActionProvider
import com.abdownloadmanager.shared.util.ondownloadcompletion.OnDownloadCompletionActionRunner
import com.abdownloadmanager.shared.util.onqueuecompletion.NoopOnQueueCompletionActionProvider
import com.abdownloadmanager.shared.util.onqueuecompletion.OnQueueEventActionRunner
import com.abdownloadmanager.shared.util.onqueuecompletion.OnQueueCompletionActionProvider
import com.abdownloadmanager.shared.util.perhostsettings.IPerHostSettingsStorage
import com.abdownloadmanager.shared.util.perhostsettings.PerHostSettingsItem
import com.abdownloadmanager.shared.util.perhostsettings.PerHostSettingsManager
import com.abdownloadmanager.shared.util.ui.IMyIcons
import com.abdownloadmanager.shared.util.proxy.IProxyStorage
import com.abdownloadmanager.shared.util.proxy.ProxyData
import com.abdownloadmanager.shared.util.proxy.ProxyManager
import ir.amirab.downloader.DownloaderRegistry
import ir.amirab.downloader.connection.UserAgentProvider
import ir.amirab.downloader.connection.proxy.AutoConfigurableProxyProvider
import ir.amirab.downloader.connection.proxy.NoopSystemProxySelectorProvider
import ir.amirab.downloader.connection.proxy.ProxyStrategyProvider
import ir.amirab.downloader.connection.proxy.SystemProxySelectorProvider
import ir.amirab.downloader.downloaditem.DownloadJob
import ir.amirab.downloader.downloaditem.IDownloadCredentials
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.downloaditem.hls.HLSDownloader
import ir.amirab.downloader.downloaditem.http.HttpDownloadCredentials
import ir.amirab.downloader.downloaditem.http.HttpDownloadItem
import ir.amirab.downloader.downloaditem.http.HttpDownloader
import ir.amirab.downloader.monitor.DownloadItemStateFactory
import ir.amirab.downloader.monitor.IDownloadMonitor
import ir.amirab.downloader.utils.EmptyFileCreator
import ir.amirab.util.compose.IIconResolver
import ir.amirab.util.compose.localizationmanager.LanguageManager
import ir.amirab.util.compose.localizationmanager.LanguageSourceProvider
import ir.amirab.util.compose.localizationmanager.LanguageStorage
import ir.amirab.util.config.datastore.kotlinxSerializationDataStore
import ir.amirab.util.startup.AbstractStartupManager
import ir.amirab.util.startup.Startup
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import okhttp3.Protocol
import okhttp3.internal.tls.OkHostnameVerifier

val downloaderModule = module {
    single<IDownloadQueueDatabase> {
        val definedPaths = get<DefinedPaths>()

        DownloadQueueFileStorageDatabase(
            queueFolder = get<DownloadFoldersRegistry>().registerAndGet(
                definedPaths.queuesDir
            ),
            fileSaver = get(),
        )
    }
    single<IDownloadListDb> {
        val definedPaths = get<DefinedPaths>()
        DownloadListFileStorage(
            downloadListFolder = get<DownloadFoldersRegistry>().registerAndGet(
                definedPaths.downloadListDir
            ),
            fileSaver = get(),
        )
    }
    single {
        TransactionalFileSaver(get())
    }
    single<IDownloadPartListDb> {
        val definedPaths = get<DefinedPaths>()
        PartListFileStorage(
            get<DownloadFoldersRegistry>().registerAndGet(
                definedPaths.partsDir
            ),
            get()
        )
    }
    single<IDiskStat> {
        AndroidDiskStat()
    }
    single<ISystemThemeDetector> {
        AndroidSystemThemeDetector(get())
    }
    single {
        QueueManager(get(), get())
    }
    single {
        DownloadFoldersRegistry()
    }
    single {
        DownloadSettings(
            8,
        )
    }
    single {
        ProxyManager(
            get()
        )
    }.bind<ProxyStrategyProvider>()
    single<SystemProxySelectorProvider> {
        NoopSystemProxySelectorProvider()
    }
    single<AutoConfigurableProxyProvider> {
        AutoConfigurableProxyProvider.NoOp()
    }
    single<UserAgentProvider> {
        UserAgentProviderFromSettings(get())
    }
    single<HttpDownloaderClient> {
        OkHttpHttpDownloaderClient(
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    single {
        val downloadSettings: DownloadSettings = get()
        EmptyFileCreator(
            diskStat = get(),
            useSparseFile = { downloadSettings.useSparseFileAllocation }
        )
    }
    single {
        HLSDownloader(inject())
    }
    single {
        HLSDownloaderInUi(get(), get())
    }
    single {
        HttpDownloader(inject())
    }
    single {
        HttpDownloaderInUi(get(), get())
    }
    single {
        DownloaderInUiRegistry().apply {
            add(get<HttpDownloaderInUi>())
            add(get<HLSDownloaderInUi>())
        }
    }.bind<DownloadItemStateFactory<IDownloadItem, DownloadJob>>()
    single {
        DownloaderRegistry().apply {
            add(get<HttpDownloader>())
            add(get<HLSDownloader>())
        }
    }
    single {
        val definedPaths = get<DefinedPaths>()
        DownloadManager(
            get(),
            get(),
            get(),
            get(),
            get(),
            get<DownloadFoldersRegistry>().registerAndGet(
                definedPaths.downloadDataDir
            )
        )
    }.bind(DownloadManagerMinimalControl::class)
    single<IDownloadMonitor> {
        DownloadMonitor(
            downloadManager = get(),
            downloadItemStateFactory = inject()
        )
    }
}
val downloadSystemModule = module {
    single {
        val definedPaths = get<DefinedPaths>()
        get<DownloadFoldersRegistry>().registerAndGet(definedPaths.categoriesDir)
        CategoryFileStorage(
            file = definedPaths.categoriesFile.toFile(),
            fileSaver = get()
        )
    }.bind<CategoryStorage>()
    single {
        FileIconProviderUsingCategoryIcons(
            get(),
            get(),
            get(),
            get(),
        )
    }.bind<FileIconProvider>()
    single {
        DefaultCategories(
            icons = get(),
            getDefaultDownloadFolder = {
                get<BaseAppSettingsStorage>().defaultDownloadFolder.value
            }
        )
    }
    single {
        DownloadManagerCategoryItemProvider(get())
    }.bind<ICategoryItemProvider>()
    single {
        CategoryManager(
            categoryStorage = get(),
            scope = get(),
            defaultCategoriesFactory = get(),
            categoryItemProvider = get(),
        )
    }

    single {
        DownloadSystem(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }
    single {
        val definedPaths = get<DefinedPaths>()
        val extraDownloadSettingsStorageFolder = get<DownloadFoldersRegistry>().registerAndGet(
            definedPaths.extraDownloadSettings
        )
        ExtraDownloadSettingsStorage(
            extraDownloadSettingsStorageFolder,
            get(),
            AndroidExtraDownloadItemSettings
        )
    }.bind<IExtraDownloadSettingsStorage<*>>()
    single {
        val definedPaths = get<DefinedPaths>()
        val extraQueueSettingsStorageFolder = get<DownloadFoldersRegistry>().registerAndGet(
            definedPaths.extraQueueSettings
        )
        ExtraQueueSettingsStorage(
            extraQueueSettingsStorageFolder,
            get(),
            AndroidExtraQueueSettings
        )
    }.apply {
        bind<IExtraQueueSettingsStorage<*>>()
    }
    single<OnDownloadCompletionActionProvider> {
        NoOpOnDownloadCompletionActionProvider()
    }
    single<OnQueueCompletionActionProvider> {
        NoopOnQueueCompletionActionProvider()
    }
    single {
        OnDownloadCompletionActionRunner(
            downloadManagerMinimalControl = get(),
            scope = get(),
            onDownloadCompletionActionProvider = get(),
        )
    }
    single {
        OnQueueEventActionRunner(
            queueManager = get(),
            scope = get(),
            onQueueCompletionActionProvider = get(),
        )
    }
    single {
        PermissionManager(
            ABDMPermissions.importantPermissions,
            get(),
        )
    }
}
val coroutineModule = module {
    single {
        CoroutineScope(SupervisorJob())
    }
}
val jsonModule = module {
    single {
        val downloaderRegistry: DownloaderRegistry by inject()
        Json {
            this.encodeDefaults = true
            this.prettyPrint = true
            this.ignoreUnknownKeys = true
            this.serializersModule = SerializersModule {
                polymorphic(IDownloadItem::class) {
                    downloaderRegistry.getAll().forEach {
                        subclass(it.downloadItemClass, it.downloadItemSerializer)
                    }
                    defaultDeserializer {
                        HttpDownloadItem.serializer()
                    }
                }
                polymorphic(IDownloadCredentials::class) {
                    downloaderRegistry.getAll().forEach {
                        subclass(it.downloadCredentialsClass, it.downloadCredentialsSerializer)
                    }
                    defaultDeserializer {
                        HttpDownloadCredentials.serializer()
                    }
                }
            }
        }
    }
}
val updaterModule = module {
    single {
        val definedPaths = get<DefinedPaths>()
        UpdateDownloadLocationProvider {
            definedPaths.updateDownloadLocation.toFile()
        }
    }
    single<UpdateApplier> {
        val definedPaths = get<DefinedPaths>()
        definedPaths.updateDownloadLocation
        AndroidDirectLinkUpdateApplier(
            updateDownloader = UpdateDownloaderViaDownloadSystem(
                get(),
                get(),
            ),
        )
    }
    single<UpdateChecker> {
        GithubUpdateChecker(
            AppVersion.get(),
            githubApi = GithubApi(
                owner = SharedConstants.projectGithubOwner,
                repo = SharedConstants.projectGithubRepo,
                client = OkHttpClient
                    .Builder()
                    .build()
            )
        )
    }
    single {
        UpdateManager(
            updateChecker = get(),
            updateApplier = get(),
            appVersionTracker = get(),
        )
    }
}
val startUpModule = module {
    single {
        Startup.getStartUpManager(get(), StartOnBootBroadcastReceiver::class.java)
    }.apply {
        bind<AbstractStartupManager>()
    }
}

fun getAppModule(context: ABDMApp) = module {
    includes(downloaderModule)
    includes(downloadSystemModule)
    includes(coroutineModule)
    includes(jsonModule)
    includes(updaterModule)
    includes(startUpModule)
//    single {
//        NetworkChecker(get())
//    }
    single {
        AppInfo.definedPaths
    }.apply {
        bind<DefinedPaths>()
        bind<AndroidDefinedPaths>()
    }
    single {
        BaseAppRepository(
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
            get(),
        )
    }.apply {
        bind<BaseAppRepository>()
        bind<SizeAndSpeedUnitProvider>()
    }
    single {
        ThemeManager(get(), get(), get())
    }
//    single {
//        FontManager(get())
//    }
    single {
        LanguageManager(
            get(),
            LanguageSourceProvider(
                ABDMLanguageResources.defaultLanguageResource,
                ABDMLanguageResources.languages,
            )
        )
    }
    single {
        MyIcons
    }.apply {
        bind<IMyIcons>()
        bind<IIconResolver>()
    }
    single {
        val definedPaths = get<DefinedPaths>()
        ProxyDatastoreStorage(
            kotlinxSerializationDataStore(
                definedPaths.proxySettingsFile.toFile(),
                get(),
                ProxyData::default,
            )
        )
    }.bind<IProxyStorage>()
    single {
        val definedPaths = get<DefinedPaths>()
        AppSettingsStorage(
            createMapConfigDatastore(
                definedPaths.appSettingsFile.toFile(),
                get(),
            )
        )
    }.apply {
        bind<BaseAppSettingsStorage>()
        bind<LanguageStorage>()
        bind<ThemeSettingsStorage>()
    }
    single {
        RemovedDownloadsFromDiskTracker(
            get(), get(), get(),
        )
    }
    single {
        val definedPaths = get<DefinedPaths>()
        PreviousVersion(
            systemPath = definedPaths.systemDir.toFile(),
            currentVersion = AppVersion.get(),
        )
    }
    single {
        AppVersionTracker(
            previousVersion = {
                // it MUST be booted first
                get<PreviousVersion>().get()
            },
            currentVersion = AppVersion.get(),
        )
    }

    single {
        val appSettingsStorage: BaseAppSettingsStorage = get()
        AppSSLFactoryProvider(
            ignoreSSLCertificates = appSettingsStorage.ignoreSSLCertificates
        )
    }
    single {
        val appSettingsStorage: BaseAppSettingsStorage = get()
        AppHostNameVerifier(
            delegateHostnameVerifier = OkHostnameVerifier,
            ignoreHostNameVerification = appSettingsStorage.ignoreSSLCertificates
        )
    }
    single<OkHttpClient> {
        val appSSLFactoryProvider: AppSSLFactoryProvider = get()
        val appHostNameVerifier: AppHostNameVerifier = get()
        OkHttpClient
            .Builder()
            .protocols(listOf(Protocol.HTTP_1_1))
            .dispatcher(Dispatcher().apply {
                //bypass limit on concurrent connections!
                maxRequests = Int.MAX_VALUE
                maxRequestsPerHost = Int.MAX_VALUE
            })
            .sslSocketFactory(
                appSSLFactoryProvider.createSSLSocketFactory(),
                appSSLFactoryProvider.trustManager,
            )
            .hostnameVerifier(appHostNameVerifier)
            .build()
    }
    single<ILastSavedLocationsStorage> {
        val definedPaths = get<AndroidDefinedPaths>()
        LastSavedLocationStorage(
            kotlinxSerializationDataStore<List<String>>(
                definedPaths.lastSavedLocationFile.toFile(),
                get(),
                ::emptyList,
            )
        )
    }
    single<IPerHostSettingsStorage> {
        val definedPaths = get<DefinedPaths>()
        PerHostSettingsDatastoreStorage(
            kotlinxSerializationDataStore<List<PerHostSettingsItem>>(
                definedPaths.perHostSettingsFile.toFile(),
                get(),
                ::emptyList,
            )
        )
    }
    single {
        PerHostSettingsManager(get())
    }
    single { context }.apply {
        bind<ABDMApp>()
        bind<Application>()
        bind<Context>()
    }
    single {
        ABDMAppManager(get(), get(), get(), get(), get(), get(), get())
    }
    single {
        ABDMServiceNotificationManager(get(), get(), get(), get(), get())
    }
    single {
        AndroidDownloadItemOpener(get())
    }.apply {
        bind<DownloadItemOpener>()
    }
    single { NotificationManager() }
    single {
        val paths = get<AndroidDefinedPaths>()
        AndroidOnBoardingStorage(
            kotlinxSerializationDataStore(
                paths.onboardingFile.toFile(),
                get(),
                ::OnBoardingData,
            )
        )
    }
    single {
        val paths = get<AndroidDefinedPaths>()
        HomePageStorage(
            kotlinxSerializationDataStore(
                paths.homePageFile.toFile(),
                get(),
                ::HomePageStateToPersist,
            )
        )
    }
}


object Di : KoinComponent {
    fun boot(applicationContext: ABDMApp) {
        startKoin {
            modules(getAppModule(applicationContext))
        }
    }
}
