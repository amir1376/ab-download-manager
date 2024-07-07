package ir.amirab.util.customwindow.util

import java.awt.Shape
import java.awt.Window
import java.lang.reflect.Method

object CustomWindowDecorationAccessing {
    init {
        UnsafeAccessing.assignAccessibility(
            UnsafeAccessing.desktopModule,
            listOf(
                "java.awt"
            )
        )
    }

    private val customWindowDecorationInstance: Any? = try {
        val customWindowDecoration = Class.forName("java.awt.Window\$CustomWindowDecoration")
        val constructor = customWindowDecoration.declaredConstructors.first()
        constructor.isAccessible = true
        constructor.newInstance()
    } catch (e: Exception) {
        null
    }

    private val setCustomDecorationEnabledMethod: Method? =
        getMethod("setCustomDecorationEnabled", Window::class.java, Boolean::class.java)

    private val setCustomDecorationTitleBarHeightMethod: Method? =
        getMethod("setCustomDecorationTitleBarHeight", Window::class.java, Int::class.java)

    private val setCustomDecorationHitTestSpotsMethod: Method? =
        getMethod("setCustomDecorationHitTestSpots", Window::class.java, MutableList::class.java)

    private fun getMethod(name: String, vararg params: Class<*>): Method? {
        return try {
            val clazz = Class.forName("java.awt.Window\$CustomWindowDecoration")
            val method = clazz.getDeclaredMethod(
                name, *params
            )
            method.isAccessible = true
            method
        } catch (e: Exception) {
            null
        }
    }

    val isSupported = customWindowDecorationInstance != null && setCustomDecorationEnabledMethod != null

    internal fun setCustomDecorationEnabled(window: Window, enabled: Boolean) {
        val instance = customWindowDecorationInstance ?: return
        val method = setCustomDecorationEnabledMethod ?: return
        method.invoke(instance, window, enabled)
    }

    internal fun setCustomDecorationTitleBarHeight(window: Window, height: Int) {
        val instance = customWindowDecorationInstance ?: return
        val method = setCustomDecorationTitleBarHeightMethod ?: return
        method.invoke(instance, window, height)
    }

    internal fun setCustomDecorationHitTestSpotsMethod(window: Window, spots: Map<Shape, Int>) {
        val instance = customWindowDecorationInstance ?: return
        val method = setCustomDecorationHitTestSpotsMethod ?: return
        method.invoke(instance, window, spots.entries.toMutableList())
    }
}
