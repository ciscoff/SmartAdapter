package s.yarlykov.example.arch

import android.view.ViewGroup
import android.widget.TextView
import s.yarlykov.example.R
import s.yarlykov.lib.smartadapter.holder.BindableViewHolder

class ViewHolder1(parent: ViewGroup, layoutRes: Int) :
    BindableViewHolder<TextModel>(parent, layoutRes) {
    private val textTitle = itemView.findViewById<TextView>(R.id.textTitle)
    private val textDescription = itemView.findViewById<TextView>(R.id.textDescription)

    override fun bind(data: TextModel) {
        textTitle?.apply { text = data.header }
    }
}