package com.abdownloadmanager.desktop.utils

import androidx.compose.runtime.Immutable
import androidx.compose.ui.graphics.vector.ImageVector
import com.abdownloadmanager.desktop.ui.icons.AbIcons
import com.abdownloadmanager.desktop.ui.icons.colored.BrowserGoogleChrome
import com.abdownloadmanager.desktop.ui.icons.colored.BrowserMicrosoftEdge
import com.abdownloadmanager.desktop.ui.icons.colored.BrowserMozillaFirefox
import com.abdownloadmanager.desktop.ui.icons.colored.BrowserOpera

sealed class BrowserType(
    val code:String
){
    data object Firefox:BrowserType("firefox")
    data object Chrome:BrowserType("chrome")
    data object Opera:BrowserType("opera")
    data object Edge:BrowserType("edge")
}
fun BrowserType.getName():String{
    return when(this){
        BrowserType.Chrome -> "Google Chrome"
        BrowserType.Edge -> "Microsoft Edge"
        BrowserType.Firefox -> "Mozilla Firefox"
        BrowserType.Opera -> "Opera"
    }
}
fun BrowserType.getIcon(): ImageVector {
    return when(this){
        BrowserType.Chrome -> AbIcons.Colored.BrowserGoogleChrome
        BrowserType.Edge -> AbIcons.Colored.BrowserMicrosoftEdge
        BrowserType.Firefox -> AbIcons.Colored.BrowserMozillaFirefox
        BrowserType.Opera -> AbIcons.Colored.BrowserOpera
    }
}
@Immutable
data class BrowserIntegrationModel(
    val type:BrowserType,
    val url:String,
)