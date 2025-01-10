package com.abdownloadmanager.desktop.pages.settings.configurable.widgets

import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.abdownloadmanager.shared.utils.div
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.shape.RoundedCornerShape
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp

@Composable
fun RenderConfigurableGroup(
    group: ConfigurableGroup,
    modifier: Modifier,
) {
    val enabled by group.nestedEnabled.collectAsState()
    val visible by group.nestedVisible.collectAsState()
    val title by group.groupTitle.collectAsState()
    val verticalPadding = 8
    Column(modifier
        .clip(RoundedCornerShape(6.dp))
        .background(myColors.surface/50)
        .padding(start = verticalPadding.dp)
        .padding(horizontal = 4.dp)
    ) {
        title?.rememberString()?.let {
            Text(
                text = it,
                fontSize = myTextSizes.base,
                fontWeight = FontWeight.Bold,
                modifier = Modifier
                    .padding(start = 8.dp)
                    .padding(vertical = 8.dp)
            )
            Spacer(Modifier
                .fillMaxWidth()
                .height(1.dp)
                .background(myColors.onSurface / 0.1f)
            )
        }
        group.mainConfigurable?.let {
            RenderConfigurable(it,
                Modifier.fillMaxWidth()
                    .padding(vertical = 4.dp)
            )
        }
        AnimatedVisibility(visible) {
            Column(
                Modifier
                    .padding(top = 4.dp)
                    .padding(horizontal = 4.dp)
                    .padding(bottom = verticalPadding.dp)
                ,
                verticalArrangement = Arrangement
                    .spacedBy(8.dp)
            ) {
                group.nestedConfigurable.forEach {
                    RenderConfigurable(
                        cfg = it,
                        modifier = Modifier.fillMaxWidth(),
                        groupInfo = ConfigGroupInfo(
                            enabled = enabled,
                            visible = visible,
                        )
                    )
                }
            }
        }
    }

}

