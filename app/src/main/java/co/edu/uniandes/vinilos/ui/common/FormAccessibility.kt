package co.edu.uniandes.vinilos.ui.common

import android.annotation.SuppressLint
import android.content.Context
import android.graphics.Rect
import android.view.View
import android.view.accessibility.AccessibilityManager
import android.view.accessibility.AccessibilityNodeInfo
import android.widget.TextView
import androidx.core.view.AccessibilityDelegateCompat
import androidx.core.view.ViewCompat
import androidx.core.view.accessibility.AccessibilityNodeInfoCompat
import androidx.core.view.isVisible

fun View.bindFormSubmit(submit: () -> Unit, vararg fields: Pair<View, TextView?>) {
    for ((field, message) in fields) {
        if (message == null) continue
        ViewCompat.setAccessibilityDelegate(field, object : AccessibilityDelegateCompat() {
            override fun onInitializeAccessibilityNodeInfo(host: View, info: AccessibilityNodeInfoCompat) {
                super.onInitializeAccessibilityNodeInfo(host, info)
                if (message.isVisible) {
                    info.isContentInvalid = true
                    info.error = message.text
                }
            }
        })
    }
    setOnClickListener {
        submit()
        val invalid = fields.firstOrNull { (field, message) ->
            message?.isVisible ?: ((field as? TextView)?.error != null)
        }?.first ?: return@setOnClickListener
        invalid.post { invalid.focusAfterInvalidSubmit() }
    }
}

@SuppressLint("AccessibilityFocus")
private fun View.focusAfterInvalidSubmit() {
    if (!isShown) return
    requestRectangleOnScreen(Rect(0, 0, width, height), true)
    val manager = context.getSystemService(Context.ACCESSIBILITY_SERVICE) as AccessibilityManager
    if (manager.isTouchExplorationEnabled) {
        performAccessibilityAction(AccessibilityNodeInfo.ACTION_ACCESSIBILITY_FOCUS, null)
    } else {
        requestFocus()
    }
}
