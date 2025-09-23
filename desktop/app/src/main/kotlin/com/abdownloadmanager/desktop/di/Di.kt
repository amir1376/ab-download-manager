package com.abdownloadmanager.desktop.di

import com.abdownloadmanager.github.GithubApi
import com.abdownloadmanager.UpdateDownloadLocationProvider
import com.abdownloadmanager.UpdateManager
import com.abdownloadmanager.desktop.AddDownloadDialogManager
import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.integration.IntegrationHandler
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.DownloadDialogManager
import com.abdownloadmanager.desktop.EditDownloadDialogManager
import com.abdownloadmanager.desktop.FileChecksumDialogManager
import com.abdownloadmanager.desktop.NotificationSender
import com.abdownloadmanager.desktop.PerHostSettingsPageManager
import com.abdownloadmanager.desktop.QueuePageManager
import com.abdownloadmanager.desktop.SharedConstants
import com.abdownloadmanager.desktop.PowerActionManager
import com.abdownloadmanager.desktop.actions.onevennts.DesktopOnDownloadCompletionActionProvider
import com.abdownloadmanager.desktop.actions.onevennts.DesktopOnQueueEventActionProvider
import com.abdownloadmanager.desktop.integration.IntegrationHandlerImp
import com.abdownloadmanager.desktop.pages.category.CategoryDialogManager
import com.abdownloadmanager.desktop.pages.settings.FontManager
import com.abdownloadmanager.shared.ui.theme.ThemeManager
import com.abdownloadmanager.desktop.pages.updater.UpdateDownloaderViaDownloadSystem
import ir.amirab.downloader.queue.QueueManager
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.desktop.storage.*
import com.abdownloadmanager.shared.utils.ui.icon.MyIcons
import com.abdownloadmanager.shared.utils.ui.theme.ISystemThemeDetector
import com.abdownloadmanager.desktop.utils.*
import com.abdownloadmanager.desktop.utils.native_messaging.NativeMessaging
import com.abdownloadmanager.desktop.utils.native_messaging.NativeMessagingManifestApplier
import com.abdownloadmanager.desktop.utils.proxy.AutoConfigurableProxyProviderForDesktop
import com.abdownloadmanager.desktop.utils.proxy.DesktopSystemProxySelectorProvider
import com.abdownloadmanager.desktop.utils.proxy.ProxyCachingConfig
import com.arkivanov.decompose.DefaultComponentContext
import com.arkivanov.essenty.lifecycle.LifecycleRegistry
import ir.amirab.downloader.DownloadManagerMinimalControl
import ir.amirab.downloader.DownloadSettings
import ir.amirab.downloader.connection.HttpDownloaderClient
import ir.amirab.downloader.connection.OkHttpHttpDownloaderClient
import ir.amirab.downloader.db.*
import ir.amirab.downloader.monitor.DownloadMonitor
import ir.amirab.downloader.utils.IDiskStat
import ir.amirab.util.startup.Startup
import com.abdownloadmanager.integration.Integration
import com.abdownloadmanager.shared.downloaderinui.DownloaderInUiRegistry
import com.abdownloadmanager.shared.downloaderinui.http.HttpDownloaderInUi
import com.abdownloadmanager.shared.storage.IExtraDownloadSettingsStorage
import com.abdownloadmanager.shared.storage.IExtraQueueSettingsStorage
import com.abdownloadmanager.shared.ui.theme.ThemeSettingsStorage
import com.abdownloadmanager.shared.util.SizeAndSpeedUnitProvider
import com.abdownloadmanager.shared.utils.*
import com.abdownloadmanager.updateapplier.DesktopUpdateApplier
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
import com.abdownloadmanager.shared.utils.appinfo.PreviousVersion
import com.abdownloadmanager.shared.utils.autoremove.RemovedDownloadsFromDiskTracker
import com.abdownloadmanager.shared.utils.category.*
import com.abdownloadmanager.shared.utils.ondownloadcompletion.OnDownloadCompletionActionProvider
import com.abdownloadmanager.shared.utils.ondownloadcompletion.OnDownloadCompletionActionRunner
import com.abdownloadmanager.shared.utils.onqueuecompletion.OnQueueEventActionRunner
import com.abdownloadmanager.shared.utils.onqueuecompletion.OnQueueCompletionActionProvider
import com.abdownloadmanager.shared.utils.perhostsettings.IPerHostSettingsStorage
import com.abdownloadmanager.shared.utils.perhostsettings.PerHostSettingsItem
import com.abdownloadmanager.shared.utils.perhostsettings.PerHostSettingsManager
import com.abdownloadmanager.shared.utils.ui.IMyIcons
import com.abdownloadmanager.shared.utils.proxy.IProxyStorage
import com.abdownloadmanager.shared.utils.proxy.ProxyData
import com.abdownloadmanager.shared.utils.proxy.ProxyManager
import ir.amirab.downloader.DownloaderRegistry
import ir.amirab.downloader.connection.UserAgentProvider
import ir.amirab.downloader.connection.proxy.AutoConfigurableProxyProvider
import ir.amirab.downloader.connection.proxy.ProxyStrategyProvider
import ir.amirab.downloader.connection.proxy.SystemProxySelectorProvider
import ir.amirab.downloader.downloaditem.DownloadJob
import ir.amirab.downloader.downloaditem.IDownloadItem
import ir.amirab.downloader.downloaditem.http.HttpDownloadItem
import ir.amirab.downloader.downloaditem.http.HttpDownloader
import ir.amirab.downloader.monitor.DownloadItemStateFactory
import ir.amirab.downloader.monitor.IDownloadMonitor
import ir.amirab.downloader.utils.EmptyFileCreator
import ir.amirab.util.compose.localizationmanager.LanguageManager
import ir.amirab.util.compose.localizationmanager.LanguageStorage
import ir.amirab.util.config.datastore.kotlinxSerializationDataStore
import ir.amirab.util.desktop.DesktopUtils
import ir.amirab.util.desktop.downloadlocation.LinuxDownloadLocationProvider
import ir.amirab.util.desktop.downloadlocation.MacDownloadLocationProvider
import ir.amirab.util.desktop.downloadlocation.WindowsDownloadLocationProvider
import ir.amirab.util.platform.Platform
import ir.amirab.util.platform.asDesktop
import kotlinx.serialization.modules.SerializersModule
import kotlinx.serialization.modules.polymorphic
import okhttp3.Protocol
import okhttp3.internal.tls.OkHostnameVerifier

val downloaderModule = module {
    single<IDownloadQueueDatabase> {
        DownloadQueueFileStorageDatabase(
            queueFolder = get<DownloadFoldersRegistry>().registerAndGet(
                AppInfo.downloadDbDir.resolve("queues")
            ),
            fileSaver = get(),
        )
    }
    single<IDownloadListDb> {
        DownloadListFileStorage(
            downloadListFolder = get<DownloadFoldersRegistry>().registerAndGet(
                AppInfo.downloadDbDir.resolve("downloadlist")
            ),
            fileSaver = get(),
        )
    }
    single {
        TransactionalFileSaver(get())
    }
    single<IDownloadPartListDb> {
        PartListFileStorage(
            get<DownloadFoldersRegistry>().registerAndGet(
                AppInfo.downloadDbDir.resolve("parts")
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
        HttpDownloader(inject())
    }
    single {
        HttpDownloaderInUi(get(), get())
    }
    single {
        DownloaderInUiRegistry().apply {
            add(get<HttpDownloaderInUi>())
        }
    }.bind<DownloadItemStateFactory<IDownloadItem, DownloadJob>>()
    single {
        DownloaderRegistry().apply {
            add(get<HttpDownloader>())
        }
    }
    single {
        DownloadManager(
            get(),
            get(),
            get(),
            get(),
            get(),
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
        CategoryFileStorage(
            file = get<DownloadFoldersRegistry>().registerAndGet(
                AppInfo.downloadDbDir.resolve("categories")
            ).resolve("categories.json"),
            fileSaver = get()
        )
    }.bind<CategoryStorage>()
    single {
        FileIconProviderUsingCategoryIcons(
            get(),
            get(),
            MyIcons,
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
        )
    }
    single {
        val extraDownloadSettingsStorageFolder = get<DownloadFoldersRegistry>().registerAndGet(
            AppInfo.downloadDbDir.resolve("extra_download_settings")
        )
        ExtraDownloadSettingsStorage(extraDownloadSettingsStorageFolder, get())
    }.bind<IExtraDownloadSettingsStorage<*>>()
    single {
        val extraQueueSettingsStorageFolder = get<DownloadFoldersRegistry>().registerAndGet(
            AppInfo.downloadDbDir.resolve("extra_queue_settings")
        )
        ExtraQueueSettingsStorage(extraQueueSettingsStorageFolder, get())
    }.bind<IExtraQueueSettingsStorage<*>>()
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
            }
        }
    }
}
val integrationModule = module {
    single<IntegrationHandler> {
        IntegrationHandlerImp()
    }
    single {
        Integration(get(), get(), AppInfo.isInDebugMode())
    }
}
val updaterModule = module {
    single {
        UpdateDownloadLocationProvider {
            AppInfo.updateDir.resolve("downloads")
        }
    }
    single<UpdateApplier> {
        DesktopUpdateApplier(
            installationFolder = AppInfo.installationFolder,
            updateFolder = AppInfo.updateDir.path,
            logDir = AppInfo.logDir.path,
            appName = AppInfo.name,
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
        Startup.getStartUpManagerForDesktop(
            name = AppInfo.displayName,
            path = AppInfo.exeFile,
            args = listOf(AppArguments.Args.BACKGROUND),
            packageName = AppInfo.packageName,
        )
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
        AppRepository()
    }.apply {
        bind<SizeAndSpeedUnitProvider>()
    }
    single {
        ThemeManager(get(), get(), get())
    }
    single {
        FontManager(get())
    }
    single {
        LanguageManager(get())
    }
    single {
        MyIcons
    }.bind<IMyIcons>()
    single {
        ProxyDatastoreStorage(
            kotlinxSerializationDataStore(
                AppInfo.optionsDir.resolve("proxySettings.json"),
                get(),
                ProxyData::default,
            )
        )
    }.bind<IProxyStorage>()
    single {
        AppSettingsStorage(
            createMapConfigDatastore(
                AppInfo.configDir.resolve("appSettings.json"),
                get(),
            )
        )
    }.apply {
        bind<LanguageStorage>()
        bind<ThemeSettingsStorage>()
    }
    single {
        PageStatesStorage(
            createMapConfigDatastore(
                AppInfo.configDir.resolve("pageStatesStorage.json"),
                get(),
            )
        )
    }
    single {
        val lifecycle = LifecycleRegistry()
        val context = DefaultComponentContext(lifecycle)
        runBlocking {
            withContext(Dispatchers.Main) {
                AppComponent(context)
            }
        }
    }.apply {
        bind<DownloadDialogManager>()
        bind<AddDownloadDialogManager>()
        bind<CategoryDialogManager>()
        bind<EditDownloadDialogManager>()
        bind<FileChecksumDialogManager>()
        bind<QueuePageManager>()
        bind<NotificationSender>()
        bind<DownloadItemOpener>()
        bind<PerHostSettingsPageManager>()
        bind<PowerActionManager>()
    }
    single {
        RemovedDownloadsFromDiskTracker(
            get(), get(), get(),
        )
    }
    single {
        PreviousVersion(
            systemPath = AppInfo.systemDir,
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
    single<SystemDownloadLocationProvider> {
        when (Platform.asDesktop()) {
            Platform.Desktop.Windows -> WindowsDownloadLocationProvider()
            Platform.Desktop.Linux -> LinuxDownloadLocationProvider()
            Platform.Desktop.MacOS -> MacDownloadLocationProvider()
        }
    }
    single<IPerHostSettingsStorage> {
        PerHostSettingsDatastoreStorage(
            kotlinxSerializationDataStore<List<PerHostSettingsItem>>(
                AppInfo.optionsDir.resolve("perHostSettings.json"),
                get(),
                ::emptyList,
            )
        )
    }
    single {
        PerHostSettingsManager(get())
    }

}


object Di : KoinComponent {
    fun boot() {
        startKoin {
            modules(appModule)
        }
    }
}
