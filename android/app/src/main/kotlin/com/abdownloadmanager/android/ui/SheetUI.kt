package com.abdownloadmanager.android.ui

import androidx.compose.foundation.background
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.WindowInsets
import androidx.compose.foundation.layout.WindowInsetsSides
import androidx.compose.foundation.layout.areNavigationBarsVisible
import androidx.compose.foundation.layout.asPaddingValues
import androidx.compose.foundation.layout.consumeWindowInsets
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.imePadding
import androidx.compose.foundation.layout.navigationBars
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.only
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.systemBarsPadding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.text.style.TextOverflow
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.ResponsiveDialogScope
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.LocalContentColor
import com.abdownloadmanager.shared.util.ui.WithContentColor
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.IconSource

@Composable
fun ResponsiveDialogScope.SheetUI(
    header: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    WithContentColor(myColors.onSurface) {
        Column(
            Modifier
                .padding(
                    WindowInsets.navigationBars
                        .only(WindowInsetsSides.Horizontal)
                        .asPaddingValues()
                )
                .fillMaxWidth()
                .statusBarsPadding()
                .clip(
                    myShapes.createSheetWithCustomEdges(
                        topStart = isTopStartFree,
                        bottomStart = isBottomStartFree,
                        topEnd = isTopEndFree,
                        bottomEnd = isBottomEndFree,
                    )
                )
                .background(myColors.surface)
                .let { modifier ->
                    val verticalNavigationBarPaddingValues = WindowInsets.navigationBars
                        .only(WindowInsetsSides.Vertical)
                        .asPaddingValues()
                    modifier
                        .padding(verticalNavigationBarPaddingValues)
                        .consumeWindowInsets(verticalNavigationBarPaddingValues)
                }
                .imePadding()
                .padding(mySpacings.smallSpace)
        ) {
            header()
            Spacer(Modifier.height(mySpacings.mediumSpace))
            Box(
                Modifier
                    .fillMaxWidth()
            ) {
                content()
            }
        }
    }
}

@Composable
fun SheetHeader(
    headerTitle: @Composable () -> Unit = {},
    headerActions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = mySpacings.mediumSpace)
            .padding(horizontal = mySpacings.mediumSpace),
    ) {
        Box(Modifier.weight(1f)) {
            headerTitle()
        }
        Spacer(Modifier.width(mySpacings.smallSpace))
        Row(
            verticalAlignment = Alignment.CenterVertically,
        ) {
            headerActions()
        }
    }
}

@Composable
fun SheetTitle(
    title: String,
    icon: IconSource? = null
) {
    Row(
        Modifier.padding(start = mySpacings.mediumSpace, top = mySpacings.mediumSpace)
    ) {
        icon?.let {
            MyIcon(icon, null)
            Spacer(Modifier.width(mySpacings.mediumSpace))
        }
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = myTextSizes.xl,
            modifier = Modifier,
            maxLines = 1,
            overflow = TextOverflow.Ellipsis,
        )
    }
}

@Composable
fun SheetTitleWithDescription(
    title: String,
    description: String,
) {
    Column {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = myTextSizes.xl,
            modifier = Modifier.padding(start = mySpacings.mediumSpace, top = mySpacings.mediumSpace)
        )
        Text(
            text = description,
            modifier = Modifier
                .padding(start = mySpacings.mediumSpace, top = mySpacings.mediumSpace),
            color = LocalContentColor.current / 0.75f
        )

    }
}
