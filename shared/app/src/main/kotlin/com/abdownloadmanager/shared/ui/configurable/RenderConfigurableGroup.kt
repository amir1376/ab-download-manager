package com.abdownloadmanager.shared.ui.configurable

import com.abdownloadmanager.shared.utils.ui.myColors
import com.abdownloadmanager.shared.utils.ui.theme.myTextSizes
import com.abdownloadmanager.shared.utils.div
import androidx.compose.animation.AnimatedVisibility
import androidx.compose.foundation.background
import androidx.compose.foundation.layout.*
import com.abdownloadmanager.shared.ui.widget.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.Dp
import androidx.compose.ui.unit.dp
import com.abdownloadmanager.shared.utils.ui.theme.myShapes

@Composable
fun RenderConfigurableGroup(
    group: ConfigurableGroup,
    modifier: Modifier,
    itemPadding: PaddingValues = PaddingValues(),
    spaceBy: Dp = 8.dp,
) {
    val enabled by group.nestedEnabled.collectAsState()
    val visible by group.nestedVisible.collectAsState()
    val title by group.groupTitle.collectAsState()
    Column(modifier
        .clip(myShapes.defaultRounded)
        .background(myColors.surface/50)
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
                    .padding(itemPadding)
            )
        }
        AnimatedVisibility(visible) {
            Column(
                Modifier
                    .padding(top = 4.dp)
                    .padding(horizontal = 4.dp)
                ,
                verticalArrangement = Arrangement.spacedBy(spaceBy)
            ) {
                group.nestedConfigurable.forEach {
                    RenderConfigurable(
                        cfg = it,
                        modifier = Modifier
                            .fillMaxWidth()
                            .padding(itemPadding),
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

