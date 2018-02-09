package com.coderdeer.appdeer.utils

import android.app.Activity
import android.support.annotation.IdRes
import android.view.View

/**
 * Created by qureshi on 05/02/18.
 */

fun <T : View> Activity.bind(@IdRes res : Int) : Lazy<T> {
    @Suppress("UNCHECKED_CAST")
    return lazy { findViewById(res) as T }
}