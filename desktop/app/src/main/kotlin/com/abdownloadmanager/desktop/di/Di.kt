package com.abdownloadmanager.desktop.di

import com.abdownloadmanager.desktop.AppArguments
import com.abdownloadmanager.integration.IntegrationHandler
import com.abdownloadmanager.desktop.AppComponent
import com.abdownloadmanager.desktop.integration.IntegrationHandlerImp
import com.abdownloadmanager.desktop.pages.settings.ThemeManager
import ir.amirab.downloader.queue.QueueManager
import com.abdownloadmanager.desktop.repository.AppRepository
import com.abdownloadmanager.desktop.storage.*
import com.abdownloadmanager.desktop.ui.icon.MyIcons
import com.abdownloadmanager.desktop.utils.*
import com.abdownloadmanager.desktop.utils.native_messaging.NativeMessaging
import com.abdownloadmanager.desktop.utils.native_messaging.NativeMessagingManifestApplier
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
import com.abdownloadmanager.updatechecker.DummyUpdateChecker
import com.abdownloadmanager.updatechecker.UpdateChecker
import com.abdownloadmanager.utils.FileIconProvider
import com.abdownloadmanager.utils.FileIconProviderUsingCategoryIcons
import com.abdownloadmanager.utils.category.*
import com.abdownloadmanager.utils.compose.IMyIcons
import com.abdownloadmanager.utils.proxy.IProxyStorage
import com.abdownloadmanager.utils.proxy.ProxyData
import com.abdownloadmanager.utils.proxy.ProxyManager
import ir.amirab.downloader.connection.proxy.ProxyStrategyProvider
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
    single<DownloaderClient> {
        OkHttpDownloaderClient(
            OkHttpClient
                .Builder()
                .dispatcher(Dispatcher().apply {
                    //bypass limit on concurrent connections!
                    maxRequests = Int.MAX_VALUE
                    maxRequestsPerHost = Int.MAX_VALUE
                }).build(),
            get()
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
    single<UpdateChecker> {
        DummyUpdateChecker(AppVersion.get())
    }
}
val startUpModule = module {
    single {
        Startup.getStartUpManagerForDesktop(
            name = AppInfo.name,
            path = AppInfo.exeFile?.let { exeFile ->
                "$exeFile ${AppArguments.Args.BACKGROUND}"
            },
            jar = false,
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
        ThemeManager(get(), get())
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

}


object Di : KoinComponent {
    fun boot() {
        startKoin {
            modules(appModule)
        }
    }
}