package s.yarlykov.example.prod.rv

import android.content.Context
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class NotificationsLayoutManager(context: Context) : LinearLayoutManager(context) {

    /**
     * Включаем/Отключаем прокрутку
     */
    var isScrollable: Boolean = true

    override fun scrollVerticallyBy(
        dy: Int,
        recycler: RecyclerView.Recycler?,
        state: RecyclerView.State?
    ): Int {

        return if (isScrollable) {
            super.scrollVerticallyBy(dy, recycler, state)
        } else {
            0
        }
    }
}