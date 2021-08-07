package s.yarlykov.decoration.round

import android.graphics.Canvas
import android.os.Build
import android.view.View
import androidx.annotation.RequiresApi
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.decoration.Decorator
import s.yarlykov.decoration.Decorator.UNDEFINE_VIEW_HOLDER

/**
 * Установливает кастомный ViewOutlineProvider, который будет возвращать форму для обрезки View.
 *
 * Требуется API 21+
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class RoundDecorator(
    private val cornerRadius: Float,
    private val roundPolitic: RoundPolitic = RoundPolitic.Every(RoundMode.ALL)
) : Decorator.ViewHolderDecorator {

    override fun draw(
        canvas: Canvas,
        view: View,
        recyclerView: RecyclerView,
        state: RecyclerView.State
    ) {

        val viewHolder = recyclerView.getChildViewHolder(view)
        val nextViewHolder =
            recyclerView.findViewHolderForAdapterPosition(viewHolder.adapterPosition + 1)
        val previousChildViewHolder =
            recyclerView.findViewHolderForAdapterPosition(viewHolder.adapterPosition - 1)

        if (cornerRadius != 0f) {
            val roundMode = getRoundMode(previousChildViewHolder, viewHolder, nextViewHolder)
            val outlineProvider = view.outlineProvider

            if (outlineProvider is RoundOutlineProvider) {
                outlineProvider.roundMode = roundMode
                view.invalidateOutline()
            } else {
                view.outlineProvider = RoundOutlineProvider(cornerRadius, roundMode)
                view.clipToOutline = true
            }
        }
    }

    /**
     * Определить что закруглять согласно установленной политике.
     */
    private fun getRoundMode(
        previousChildViewHolder: RecyclerView.ViewHolder?,
        currentViewHolder: RecyclerView.ViewHolder?,
        nextChildViewHolder: RecyclerView.ViewHolder?
    ): RoundMode {

        val previousHolderItemType = previousChildViewHolder?.itemViewType ?: UNDEFINE_VIEW_HOLDER
        val currentHolderItemType = currentViewHolder?.itemViewType ?: UNDEFINE_VIEW_HOLDER
        val nextHolderItemType = nextChildViewHolder?.itemViewType ?: UNDEFINE_VIEW_HOLDER

        // Если RoundPolitic.Every, то просто вернуть roundMode. Он будет действовать одинаково
        // на каждый элемент списка.
        if (roundPolitic is RoundPolitic.Every) return roundPolitic.roundMode

        // Если RoundPolitic.Group, то нужно определить находится ли текущий viewHolder в группе
        // однотипных элементов и какое положение он в ней занимает. В зависимости от полученных
        // данных принимаем решение о том что закруглять и нужно ли закруглять вообще.
        return when {
            previousHolderItemType != currentHolderItemType && currentHolderItemType != nextHolderItemType -> RoundMode.ALL
            previousHolderItemType != currentHolderItemType && currentHolderItemType == nextHolderItemType -> RoundMode.TOP
            previousHolderItemType == currentHolderItemType && currentHolderItemType != nextHolderItemType -> RoundMode.BOTTOM
            previousHolderItemType == currentHolderItemType && currentHolderItemType == nextHolderItemType -> RoundMode.NONE
            else -> RoundMode.NONE
        }
    }
}