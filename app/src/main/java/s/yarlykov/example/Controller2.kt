package s.yarlykov.example

import android.view.ViewGroup
import androidx.annotation.LayoutRes
import s.yarlykov.lib.smartadapter.adapter.Collector
import s.yarlykov.lib.smartadapter.controller.BindableItemController
import s.yarlykov.lib.smartadapter.model.item.BindableItem

/**
 * Определяем конкретный ТИП на базе дженерик-КЛАССОВ, поэтому для всех type parameters
 * устанавливаем явные ТИПЫ, а именно TextModel и ViewHolder1.
 *
 */
class Controller2(@LayoutRes val layoutRes: Int) :
    BindableItemController<TextModel, ViewHolder2>() {

    override fun bind(holder: ViewHolder2, item: BindableItem<TextModel, ViewHolder2>) {
        bind(holder, item.data)
    }

    override fun createViewHolder(parent: ViewGroup, eventCollector: Collector?): ViewHolder2 {
        return ViewHolder2(parent, layoutRes)
    }

    override fun viewType(): Int = layoutRes
}