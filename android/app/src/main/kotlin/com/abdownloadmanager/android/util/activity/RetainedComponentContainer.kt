package com.abdownloadmanager.android.util.activity

import android.content.Intent
import androidx.activity.ComponentActivity
import androidx.activity.compose.LocalActivity
import androidx.compose.runtime.Composable
import com.abdownloadmanager.shared.util.BaseComponent
import com.abdownloadmanager.shared.util.mvi.ContainsEffects
import com.abdownloadmanager.shared.util.mvi.HandleEffects
import com.abdownloadmanager.shared.util.mvi.supportEffects
import com.arkivanov.decompose.ComponentContext
import com.arkivanov.decompose.childContext
import java.lang.ref.WeakReference

class RetainedComponentContainer<T>(
    ctx: ComponentContext,
    factory: RetainedComponentContainer<T>.(ComponentContext) -> T
) : BaseComponent(ctx),
    ContainsEffects<RetainedComponentContainer.Effects> by supportEffects(),
    ActivityActions {
    private var currentActivity: WeakReference<ComponentActivity> = WeakReference(null)
    fun reinitialize(activity: ComponentActivity) = apply {
        this.currentActivity = WeakReference(activity)
    }

    fun getCurrentActivity(): ComponentActivity? {
        return currentActivity.get()
    }

    override fun startActivityAction(intent: Intent) {
        sendEffect(Effects.StartActivity(intent))
    }

    override fun finishActivityAction() {
        sendEffect(Effects.FinishActivity)
    }

    // it's better to create scope for factory to prevent accidentally accessing this
    // for now make sure to not use [component] inside factory!
    val component: T by lazy {
        factory(childContext("main"))
    }

    sealed interface Effects {
        data class StartActivity(val intent: Intent) : Effects
        data object FinishActivity : Effects
    }
}

@Composable
fun RetainedComponentContainer<*>.HandleActivityEffects() {
    val activity = LocalActivity.current
    HandleEffects(this) {
        when (it) {
            RetainedComponentContainer.Effects.FinishActivity -> {
                activity?.finish()
            }

            is RetainedComponentContainer.Effects.StartActivity -> {
                activity?.startActivity(it.intent)
            }
        }
    }
}
