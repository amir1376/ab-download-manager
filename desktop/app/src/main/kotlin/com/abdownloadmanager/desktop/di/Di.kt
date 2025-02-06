package com.abdownloadmanager.desktop.di

import GithubApi
import com.abdownloadmanager.UpdateDownloadLocationProvider
import com.abdownloadmanager.UpdateManager
import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.integration.IntegrationHandler
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.SharedConstants
import com.abdownloadmanager.desktop.integration.IntegrationHandlerImp
import com.abdownloadmanager.desktop.pages.settings.ThemeManager
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
import ir.amirab.downloader.connection.DownloaderClient
import ir.amirab.downloader.connection.OkHttpDownloaderClient
import ir.amirab.downloader.db.*
import ir.amirab.downloader.monitor.DownloadMonitor
import ir.amirab.downloader.utils.IDiskStat
import ir.amirab.util.startup.Startup
import com.abdownloadmanager.integration.Integration
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
import com.abdownloadmanager.shared.utils.ui.IMyIcons
import com.abdownloadmanager.shared.utils.proxy.IProxyStorage
import com.abdownloadmanager.shared.utils.proxy.ProxyData
import com.abdownloadmanager.shared.utils.proxy.ProxyManager
import ir.amirab.downloader.connection.proxy.AutoConfigurableProxyProvider
import ir.amirab.downloader.connection.proxy.ProxyStrategyProvider
import ir.amirab.downloader.connection.proxy.SystemProxySelectorProvider
import ir.amirab.downloader.monitor.IDownloadMonitor
import ir.amirab.downloader.utils.EmptyFileCreator
import ir.amirab.util.compose.localizationmanager.LanguageManager
import ir.amirab.util.compose.localizationmanager.LanguageStorage
import ir.amirab.util.config.datastore.kotlinxSerializationDataStore

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
    single<DownloaderClient> {
        OkHttpDownloaderClient(
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
        DownloadManager(get(), get(), get(), get(), get(), get())
    }.bind(DownloadManagerMinimalControl::class)
    single<IDownloadMonitor> {
        DownloadMonitor(get())
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
        DownloadSystem(get(), get(), get(), get(), get(), get(), get())
    }
}
val coroutineModule = module {
    single {
        CoroutineScope(SupervisorJob())
    }
}
val jsonModule = module {
    single {
        Json {
            this.encodeDefaults = true
            this.prettyPrint = true
            this.ignoreUnknownKeys = true
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
    }
    single {
        ThemeManager(get(), get(), get())
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
    }.bind<LanguageStorage>()
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
    single<OkHttpClient> {
        val appSSLFactoryProvider: AppSSLFactoryProvider = get()
        OkHttpClient
            .Builder()
            .dispatcher(Dispatcher().apply {
                //bypass limit on concurrent connections!
                maxRequests = Int.MAX_VALUE
                maxRequestsPerHost = Int.MAX_VALUE
            })
            .sslSocketFactory(
                appSSLFactoryProvider.createSSLSocketFactory(),
                appSSLFactoryProvider.trustManager,
            )
            .build()
    }

}


object Di : KoinComponent {
    fun boot() {
        startKoin {
            modules(appModule)
        }
    }
}
