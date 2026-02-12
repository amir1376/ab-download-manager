package com.abdownloadmanager.desktop.di

import com.abdownloadmanager.github.GithubApi
import com.abdownloadmanager.UpdateDownloadLocationProvider
import com.abdownloadmanager.UpdateManager
import com.abdownloadmanager.desktop.DesktopAddDownloadDialogManager
import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.integration.IntegrationHandler
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.DesktopDownloadDialogManager
import com.abdownloadmanager.shared.pagemanager.EditDownloadDialogManager
import com.abdownloadmanager.shared.pagemanager.FileChecksumDialogManager
import com.abdownloadmanager.shared.pagemanager.NotificationSender
import com.abdownloadmanager.shared.pagemanager.PerHostSettingsPageManager
import com.abdownloadmanager.shared.pagemanager.QueuePageManager
import com.abdownloadmanager.shared.util.SharedConstants
import com.abdownloadmanager.desktop.PowerActionManager
import com.abdownloadmanager.desktop.actions.onevennts.DesktopOnDownloadCompletionActionProvider
import com.abdownloadmanager.desktop.actions.onevennts.DesktopOnQueueEventActionProvider
import com.abdownloadmanager.desktop.integration.IntegrationHandlerImp
import com.abdownloadmanager.desktop.pages.category.DesktopCategoryDialogManager
import com.abdownloadmanager.desktop.pages.settings.FontManager
import com.abdownloadmanager.shared.ui.theme.ThemeManager
import ir.amirab.downloader.queue.QueueManager
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.desktop.storage.*
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.theme.ISystemThemeDetector
import com.abdownloadmanager.desktop.utils.*
import com.abdownloadmanager.desktop.utils.native_messaging.NativeMessaging
import com.abdownloadmanager.desktop.utils.native_messaging.NativeMessagingManifestApplier
import com.abdownloadmanager.desktop.utils.proxy.AutoConfigurableProxyProviderForDesktop
import com.abdownloadmanager.desktop.utils.proxy.DesktopSystemProxySelectorProvider
import com.abdownloadmanager.desktop.utils.proxy.ProxyCachingConfig
import com.abdownloadmanager.integration.HLSDownloadCredentialsFromIntegration
import com.abdownloadmanager.integration.HttpDownloadCredentialsFromIntegration
import com.abdownloadmanager.integration.IDownloadCredentialsFromIntegration
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import ir.amirab.downloader.DownloadManagerMinimalControl
import ir.amirab.downloader.DownloadSettings
import ir.amirab.downloader.connection.HttpDownloaderClient
import ir.amirab.downloader.connection.OkHttpHttpDownloaderClient
import ir.amirab.downloader.db.*
import ir.amirab.downloader.monitor.DownloadMonitor
import ir.amirab.downloader.utils.IDiskStat
import com.abdownloadmanager.integration.Integration
import com.abdownloadmanager.resources.ABDMLanguageResources
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.downloaderinui.hls.HLSDownloaderInUi
import com.abdownloadmanager.shared.downloaderinui.http.HttpDownloaderInUi
import com.abdownloadmanager.shared.pagemanager.SettingsPageManager
import com.abdownloadmanager.shared.repository.BaseAppRepository
import com.abdownloadmanager.shared.storage.BaseAppSettingsStorage
import com.abdownloadmanager.shared.storage.ExtraDownloadSettingsStorage
import com.abdownloadmanager.shared.storage.ExtraQueueSettingsStorage
import com.abdownloadmanager.shared.storage.IExtraDownloadSettingsStorage
import com.abdownloadmanager.shared.storage.IExtraQueueSettingsStorage
import com.abdownloadmanager.shared.storage.PerHostSettingsDatastoreStorage
import com.abdownloadmanager.shared.storage.ProxyDatastoreStorage
import com.abdownloadmanager.shared.ui.theme.ThemeSettingsStorage
import com.abdownloadmanager.shared.ui.widget.NotificationManager
import com.abdownloadmanager.shared.updater.UpdateDownloaderViaDownloadSystem
import com.abdownloadmanager.shared.util.AppVersion
import com.abdownloadmanager.shared.util.DefinedPaths
import com.abdownloadmanager.shared.util.DesktopDiskStat
import com.abdownloadmanager.shared.util.DesktopSystemThemeDetector
import com.abdownloadmanager.shared.util.SizeAndSpeedUnitProvider
import com.abdownloadmanager.shared.util.UserAgentProviderFromSettings
import com.abdownloadmanager.shared.util.*
import com.abdownloadmanager.updateapplier.DesktopDirectLinkUpdateApplier
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
import com.abdownloadmanager.shared.util.ondownloadcompletion.OnDownloadCompletionActionProvider
import com.abdownloadmanager.shared.util.ondownloadcompletion.OnDownloadCompletionActionRunner
import com.abdownloadmanager.shared.util.onqueuecompletion.OnQueueEventActionRunner
import com.abdownloadmanager.shared.util.onqueuecompletion.OnQueueCompletionActionProvider
import com.abdownloadmanager.shared.util.perhostsettings.IPerHostSettingsStorage
import com.abdownloadmanager.shared.util.perhostsettings.PerHostSettingsItem
import com.abdownloadmanager.shared.util.perhostsettings.PerHostSettingsManager
import com.abdownloadmanager.shared.util.ui.IMyIcons
import com.abdownloadmanager.shared.util.proxy.IProxyStorage
import com.abdownloadmanager.shared.util.proxy.ProxyData
import com.abdownloadmanager.shared.util.proxy.ProxyManager
import com.arkivanov.essenty.lifecycle.Lifecycle
import ir.amirab.downloader.DownloaderRegistry
import ir.amirab.downloader.connection.UserAgentProvider
import ir.amirab.downloader.connection.proxy.AutoConfigurableProxyProvider
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
import ir.amirab.downloader.queue.ManualDownloadQueue
import ir.amirab.downloader.utils.EmptyFileCreator
import ir.amirab.util.compose.IIconResolver
import ir.amirab.util.compose.localizationmanager.LanguageManager
import ir.amirab.util.compose.localizationmanager.LanguageSourceProvider
import ir.amirab.util.compose.localizationmanager.LanguageStorage
import ir.amirab.util.config.datastore.kotlinxSerializationDataStore
import ir.amirab.util.desktop.DesktopUtils
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
        DesktopDiskStat()
    }
    single<ISystemThemeDetector> {
        DesktopSystemThemeDetector()
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
    single {
        ProxyCachingConfig.default()
    }
    single<AutoConfigurableProxyProvider> {
        AutoConfigurableProxyProviderForDesktop(get())
    }
    single<SystemProxySelectorProvider> {
        DesktopSystemProxySelectorProvider(get())
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
    single {
        ManualDownloadQueue(get(), get())
    }
    single<IDownloadMonitor> {
        DownloadMonitor(
            downloadManager = get(),
            manualDownloadQueue = get(),
            downloadItemStateFactory = inject(),
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
                get<AppSettingsStorage>().defaultDownloadFolder.value
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
            DesktopExtraDownloadItemSettings
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
            DesktopExtraQueueSettings
        )
    }.apply {
        bind<IExtraQueueSettingsStorage<*>>()
    }
    single<OnDownloadCompletionActionProvider> {
        DesktopOnDownloadCompletionActionProvider(get())
    }
    single<OnQueueCompletionActionProvider> {
        DesktopOnQueueEventActionProvider(get())
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
                // TODO remove this later
                polymorphic(IDownloadCredentialsFromIntegration::class) {
                    subclass(
                        HttpDownloadCredentialsFromIntegration::class,
                        HttpDownloadCredentialsFromIntegration.serializer()
                    )
                    subclass(
                        HLSDownloadCredentialsFromIntegration::class,
                        HLSDownloadCredentialsFromIntegration.serializer()
                    )
                    defaultDeserializer {
                        HttpDownloadCredentialsFromIntegration.serializer()
                    }
                }
            }
        }
    }
}
val integrationModule = module {
    single<IntegrationHandler> {
        IntegrationHandlerImp()
    }
    single {
        Integration(get(), get(), get(), AppInfo.isInDebugMode())
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
        DesktopDirectLinkUpdateApplier(
            installationFolder = AppInfo.installationFolder,
            updateFolder = definedPaths.updateDir.toString(),
            logDir = definedPaths.logDir.toString(),
            appName = AppInfo.name,
            updatePreparer = UpdateDownloaderViaDownloadSystem(
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
        Startup.getStartUpManagerForDesktop(
            name = AppInfo.displayName,
            path = AppInfo.exeFile,
            args = listOf(AppArguments.Args.BACKGROUND),
            packageName = AppInfo.packageName,
        )
    }.apply {
        bind<AbstractStartupManager>()
    }
}
val nativeMessagingModule = module {
    single<NativeMessaging> {
        NativeMessaging(NativeMessagingManifestApplier.getForCurrentPlatform())
    }
}

val appModule = module {
    includes(downloaderModule)
    includes(downloadSystemModule)
    includes(coroutineModule)
    includes(jsonModule)
    includes(integrationModule)
    includes(updaterModule)
    includes(startUpModule)
    includes(nativeMessagingModule)
//    single {
//        NetworkChecker(get())
//    }
    single {
        AppInfo.definedPaths
    }.bind<DefinedPaths>()
    single {
        AppRepository(
            get(),
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
    single {
        FontManager(get())
    }
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
        val definedPaths = get<DesktopDefinedPaths>()
        PageStatesStorage(
            createMapConfigDatastore(
                definedPaths.pageStatesStorageFile.toFile(),
                get(),
            )
        )
    }
    single {
        val lifecycle = LifecycleRegistry(
            Lifecycle.State.RESUMED
        )
        val context = DefaultComponentContext(lifecycle)
        runBlocking {
            withContext(Dispatchers.Main) {
                AppComponent(context)
            }
        }
    }.apply {
        bind<DesktopDownloadDialogManager>()
        bind<DesktopAddDownloadDialogManager>()
        bind<DesktopCategoryDialogManager>()
        bind<EditDownloadDialogManager>()
        bind<FileChecksumDialogManager>()
        bind<QueuePageManager>()
        bind<NotificationSender>()
        bind<DownloadItemOpener>()
        bind<PerHostSettingsPageManager>()
        bind<PowerActionManager>()
        bind<SettingsPageManager>()
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
            currentVersion = AppInfo.version,
        )
    }
    single {
        AppVersionTracker(
            previousVersion = {
                // it MUST be booted first
                get<PreviousVersion>().get()
            },
            currentVersion = AppInfo.version,
        )
    }

    single {
        val appSettingsStorage: AppSettingsStorage = get()
        AppSSLFactoryProvider(
            ignoreSSLCertificates = appSettingsStorage.ignoreSSLCertificates
        )
    }
    single {
        val appSettingsStorage: AppSettingsStorage = get()
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
    single {
        KeepAwakeManager(
            DesktopUtils.keepAwakeService(),
            get(),
            get(),
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
    single { NotificationManager() }
}


object Di : KoinComponent {
    fun boot() {
        startKoin {
            modules(appModule)
        }
    }
}
