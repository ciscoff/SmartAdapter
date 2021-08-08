package s.yarlykov.example.extentions

import android.graphics.Rect
import android.view.TouchDelegate
import android.view.View
import androidx.recyclerview.widget.RecyclerView

/**
 * Найти ближайший родительский RecyclerView
 */
fun View.findRecyclerViewParent(): RecyclerView? {

    return if (parent !is RecyclerView) {
        (parent as View).findRecyclerViewParent()
    } else {
        parent as RecyclerView
    }
}

/**
 * Пройти по всем "братьям" в родительском RecyclerView и на каждом выполнить операцию op
 */
inline fun <reified T : View> T.forceSiblingsToDo(op: T.() -> Unit) {
    findRecyclerViewParent()?.let { rv ->
        rv.layoutManager?.apply {
            for (i in 0 until childCount) {
                getChildAt(i)?.let { child ->
                    if (child is T && this@forceSiblingsToDo != child) {
                        child.op()
                    }
                }
            }
        }
    }
}

fun View.expandTouchArea(_left: Int = 0, _top: Int = 0, _right: Int = 0, _bottom: Int = 0) {
    (parent as View).post {
        val delegateArea = Rect().apply {
            getHitRect(this)
            left -= _left.px
            top -= _top.px
            right += _right
            bottom += _bottom.px
        }

        (parent as? View)?.apply {
            touchDelegate = TouchDelegate(delegateArea, this@expandTouchArea)
        }
    }
}

fun View.restoreTouchArea() {
    (parent as View).post {
        val delegateArea = Rect().apply { setEmpty() }

        (parent as? View)?.apply {
            touchDelegate = TouchDelegate(delegateArea, this@restoreTouchArea)
        }
    }
}