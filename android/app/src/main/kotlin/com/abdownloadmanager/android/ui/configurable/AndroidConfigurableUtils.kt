package com.abdownloadmanager.android.ui.configurable

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.ColumnScope
import androidx.compose.foundation.layout.IntrinsicSize
import androidx.compose.foundation.layout.PaddingValues
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxHeight
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.size
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.remember
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.alpha
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.ui.configurable.Configurable
import com.abdownloadmanager.shared.ui.configurable.Help
import com.abdownloadmanager.shared.ui.configurable.isConfigEnabled
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.ui.WithContentAlpha
import com.abdownloadmanager.shared.util.ui.icon.MyIcons
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes
import com.abdownloadmanager.shared.util.ui.widget.MyIcon
import ir.amirab.util.compose.modifiers.autoMirror
import ir.amirab.util.ifThen


@Composable
fun ConfigTemplate(
    modifier: Modifier,
    title: @Composable ColumnScope.() -> Unit,
    value: @Composable ColumnScope.() -> Unit,
    nestedContent: @Composable ColumnScope.() -> Unit = {},
) {
    Column(
        modifier
    ) {
        Row(
            Modifier
                .height(IntrinsicSize.Max),
            horizontalArrangement = Arrangement.Center,
        ) {
            Column(
                Modifier.weight(1f, true),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.Start,
            ) {
                title()
            }
            Column(
                Modifier.fillMaxHeight(),
                verticalArrangement = Arrangement.Center,
                horizontalAlignment = Alignment.End,
            ) {
                value()
            }
        }
        Column(
            Modifier.fillMaxWidth()
        ) {
            nestedContent()
        }
    }
}

@Composable
fun <T> TitleAndDescription(
    cfg: Configurable<T>,
    describe: Boolean = true,
    modifier: Modifier = Modifier,
    contentPadding: PaddingValues = PaddingValues(8.dp),
) {
    val enabled = isConfigEnabled()
    Column(
        modifier
            .padding(contentPadding)
            .ifThen(!enabled) {
                alpha(0.5f)
            }
    ) {
        Row(verticalAlignment = Alignment.CenterVertically) {
            Text(
                cfg.title.rememberString(),
                fontSize = myTextSizes.base,
                fontWeight = FontWeight.Bold,
                modifier = Modifier.weight(1f, false)
            )
            if (cfg.description.rememberString().isNotBlank()) {
                Spacer(Modifier.size(4.dp))
                Help(
                    Modifier.align(Alignment.Top),
                    cfg
                )
            }
        }
        if (describe) {
            val value = cfg.backedBy.collectAsState().value
            val describedStringSource = remember(value) {
                cfg.describe(value)
            }
            val describeContent = describedStringSource.rememberString()
            if (describeContent.isNotBlank()) {
                Spacer(Modifier.size(4.dp))
                WithContentAlpha(0.75f) {
                    Text(
                        describeContent,
                        fontSize = myTextSizes.base,
                    )
                }
            }
        }
    }
}

@Composable
fun NextIcon() {
    MyIcon(
        MyIcons.next,
        null,
        Modifier
            .size(16.dp)
            .autoMirror()
    )
}
