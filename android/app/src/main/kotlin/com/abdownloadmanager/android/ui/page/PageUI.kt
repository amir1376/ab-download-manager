package com.abdownloadmanager.android.ui.page

import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.RowScope
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.layout.width
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.font.FontWeight
import com.abdownloadmanager.shared.ui.widget.Text
import com.abdownloadmanager.shared.util.div
import com.abdownloadmanager.shared.util.ui.LocalContentColor
import com.abdownloadmanager.shared.util.ui.theme.mySpacings
import com.abdownloadmanager.shared.util.ui.theme.myTextSizes


@Composable
fun PageHeader(
    headerTitle: @Composable () -> Unit = {},
    leadingIcon: (@Composable () -> Unit)? = null,
    headerActions: @Composable RowScope.() -> Unit = {},
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        modifier = Modifier
            .fillMaxWidth()
            .padding(vertical = mySpacings.mediumSpace)
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
    title: String
) {
    Text(
        text = title,
        fontWeight = FontWeight.Bold,
        fontSize = myTextSizes.xl,
        modifier = Modifier
            .padding(start = mySpacings.largeSpace)
            .padding(vertical = mySpacings.largeSpace)
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
