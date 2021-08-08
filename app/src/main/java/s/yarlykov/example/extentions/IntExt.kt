package s.yarlykov.example.extentions

import android.content.res.Resources

/**
 * Px (Int) -> Dp
 */
val Int.dp: Int
    get() = (this / Resources.getSystem().displayMetrics.density).toInt()

/**
 * Dp (Int) -> Px
 */
val Int.px: Int
    get() = (this * Resources.getSystem().displayMetrics.density).toInt()