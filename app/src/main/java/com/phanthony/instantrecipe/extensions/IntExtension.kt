package com.phanthony.instantrecipe.extensions

import android.content.res.Resources


// https://medium.com/@johanneslagos/dp-to-px-and-viceversa-for-kotlin-d797815d852b
val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()