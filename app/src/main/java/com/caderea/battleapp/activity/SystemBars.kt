package com.caderea.battleapp.activity

import android.app.Activity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.core.view.updatePadding

/**
 * Pads the screen's content so it isn't drawn under the status and navigation bars, which
 * apps targeting Android 15 and later draw behind by default.
 */
fun Activity.padContentForSystemBars() {
    ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { view, insets ->
        val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
        view.updatePadding(left = bars.left, top = bars.top, right = bars.right, bottom = bars.bottom)
        insets
    }
}
