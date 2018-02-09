package com.coderdeer.appdeer.utils

import android.util.Patterns
import android.text.TextUtils



/**
 * Created by qureshi on 27/01/18.
 */

fun String.validateFields(): Boolean = !TextUtils.isEmpty(this)

fun String.validateEmail(): Boolean =
        !(TextUtils.isEmpty(this) || !Patterns.EMAIL_ADDRESS.matcher(this).matches())


