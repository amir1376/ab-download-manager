package com.abdownloadmanager.android.ui.page

import androidx.compose.foundation.basicMarquee
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.runtime.Immutable
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableIntStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.LocalContentColor
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes

@Immutable
data class PageContentParams(
    val paddingValues: PaddingValues,
)

@Composable
fun PageUi(
    header: @Composable () -> Unit,
    footer: @Composable () -> Unit,
    modifier: Modifier = Modifier,
    content: @Composable (params: PageContentParams) -> Unit,
) {
    var headerHeight by remember {
        mutableIntStateOf(0)
    }
    var footerHeight by remember {
        mutableIntStateOf(0)
    }
    val density = LocalDensity.current
    val contentPadding = PaddingValues(
        top = density.run { headerHeight.toDp() },
        bottom = density.run { footerHeight.toDp() },
    )
    Box(modifier) {
        content(
            PageContentParams(contentPadding)
        )
        Box(
            Modifier
                .onSizeChanged {
                    headerHeight = it.height
                }
                .align(Alignment.TopCenter)
        ) {
            header()
        }
        Box(
            Modifier
                .onSizeChanged {
                    footerHeight = it.height
                }
                .align(Alignment.BottomCenter)
        ) {
            footer()
        }
    }
}


@Composable
fun PageHeader(
    modifier: Modifier = Modifier,
    headerTitle: @Composable () -> Unit = {},
    leadingIcon: (@Composable () -> Unit)? = null,
    headerActions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = modifier
            .fillMaxWidth()
//            .padding(vertical = mySpacings.mediumSpace)
            .padding(horizontal = mySpacings.mediumSpace),
    ) {
        leadingIcon?.let {
            it()
            Spacer(Modifier.width(mySpacings.smallSpace))
        }
        Box(
            Modifier.weight(1f),
            contentAlignment = Alignment.CenterStart,
        ) {
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
fun PageTitle(
    title: String,
) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = myTextSizes.xl,
        maxLines = 1,
        modifier = Modifier
            .padding(start = mySpacings.largeSpace)
            .padding(vertical = mySpacings.largeSpace)
            .basicMarquee()
    )
}

@Composable
fun PageTitleWithDescription(
    title: String,
    description: String,
) {
    Column(
        Modifier
            .padding(start = mySpacings.largeSpace)
            .padding(vertical = mySpacings.largeSpace)
    ) {
        Text(
            text = title,
            fontWeight = FontWeight.Bold,
            fontSize = myTextSizes.xl,
            modifier = Modifier
        )
        Spacer(Modifier.width(mySpacings.mediumSpace))
        Text(
            text = description,
            modifier = Modifier,
            color = LocalContentColor.current / 0.75f
        )
    }
}
