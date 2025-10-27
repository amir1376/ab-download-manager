package com.abdownloadmanager.android.pages.onboarding

import androidx.activity.compose.BackHandler
import androidx.compose.foundation.Image
import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.layout.statusBarsPadding
import androidx.compose.foundation.layout.wrapContentWidth
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.shadow
import androidx.compose.ui.graphics.Brush
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.layout.onSizeChanged
import androidx.compose.ui.platform.LocalDensity
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.ui.widget.IconActionButton
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.ui.widget.TransparentIconActionButton
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.myColors
import com.abdownloadmanager.shared.util.ui.theme.myShapes
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import ir.amirab.util.compose.StringSource
import ir.amirab.util.compose.layout.RelativeAlignment
import kotlin.math.roundToInt

@Composable
fun StartUpPageTemplate(
    header: @Composable () -> Unit,
    actions: @Composable () -> Unit,
    content: @Composable () -> Unit,
) {
    Column(
        modifier = Modifier
            .background(myColors.background)
            .statusBarsPadding()
            .navigationBarsPadding(),

        ) {
        header()
        Box(
            modifier = Modifier.weight(1f),
        ) {
            content()
        }
        actions()
    }
}

@Composable
fun StartUpPageHeader(
    title: StringSource,
    onBackPressed: (() -> Unit)? = null,
) {
    BackHandler(onBackPressed != null) {
        onBackPressed?.invoke()
    }
    Row(
        modifier = Modifier.padding(
            horizontal = mySpacings.largeSpace, vertical = mySpacings.largeSpace
        ),
        verticalAlignment = Alignment.CenterVertically,
    ) {
        var backButtonWidth by remember { mutableStateOf(0) }
        if (onBackPressed != null) {
            TransparentIconActionButton(
                MyIcons.back,
                contentDescription = "Back",
                onClick = { onBackPressed() },
                modifier = Modifier.onSizeChanged {
                    backButtonWidth = it.width
                }
            )
        } else {
            backButtonWidth = 0
        }
        Text(
            text = title.rememberString(),
            fontWeight = FontWeight.Bold,
            fontSize = myTextSizes.xl,
            modifier = Modifier
                .weight(1f)
                .wrapContentWidth(
                    RelativeAlignment.Horizontal(
                        mainAlignment = Alignment.CenterHorizontally,
                        relative = -backButtonWidth / 2
                    ),
                )
        )
    }
}

@Composable
fun StartUpPageActions(
    content: @Composable () -> Unit
) {
    Box(
        Modifier
            .padding(horizontal = mySpacings.largeSpace, vertical = mySpacings.largeSpace)
    ) {
        content()
    }
}

@Composable
fun AppIcon(
    modifier: Modifier = Modifier,
    size: Dp = 52.dp,
) {
    val shape = RoundedCornerShape(24.dp)
    Image(
        MyIcons.appIcon.rememberPainter(),
        null,
        modifier
            .shadow(12.dp, shape, spotColor = myColors.primary)
            .clip(shape)
            .border(
                1.dp,
                Brush.linearGradient(
                    listOf(myColors.primary, myColors.secondary)
                ),
                shape
            )
            .background(myColors.surface)
            .padding(16.dp)
            .size(size)
    )
}
