package com.abdownloadmanager.desktop.ui.widget

import com.abdownloadmanager.desktop.ui.theme.myColors
import com.abdownloadmanager.desktop.ui.theme.myTextSizes
import com.abdownloadmanager.utils.compose.WithContentAlpha
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.desktop.ui.icons.AbIcons
import com.abdownloadmanager.desktop.ui.icons.default.AddLink
import com.abdownloadmanager.desktop.ui.icons.default.DownSpeed
import com.abdownloadmanager.resources.Res
import com.abdownloadmanager.utils.compose.widget.Icon
import ir.amirab.util.compose.resources.myStringResource

@Composable
fun AddUrlButton(
    modifier: Modifier=Modifier,
    onClick:()->Unit
) {
    val shape = RoundedCornerShape(12.dp)
    Row(
        modifier
            .clip(shape)
            .background(myColors.surface)
            .clickable(onClick = onClick)
            .height(32.dp)
//            .width(120.dp)
            .padding(horizontal = 8.dp),
        verticalAlignment = Alignment.CenterVertically,

        ) {
        WithContentAlpha(1f) {
            Icon(
                imageVector = AbIcons.Default.AddLink,
                contentDescription = null,
                modifier = Modifier.size(16.dp)
            )
            Spacer(Modifier.width(10.dp))
            Text(
                myStringResource(Res.string.new_download),
                Modifier,
                maxLines = 1,
                fontSize = myTextSizes.sm,
            )
        }
        Spacer(Modifier.width(10.dp))
        Box(
            Modifier
                .clip(RoundedCornerShape(6.dp))
                .background(
                    myColors.primaryGradient
                ).padding(4.dp)
        ) {
            Icon(
                imageVector = AbIcons.Default.DownSpeed,
                contentDescription = null,
                modifier = Modifier.size(12.dp),
                tint = myColors.onPrimaryGradient,
            )
        }
    }

}
