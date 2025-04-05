package fi.idk.kotivaloscreenshame

import android.graphics.drawable.Drawable

data class AppItem(val packageName: String,
                   val appName: String,
                   val icon: Drawable,
                   var isChecked: Boolean = false
)
