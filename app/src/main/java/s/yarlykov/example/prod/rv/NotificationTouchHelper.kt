package s.yarlykov.example.prod.rv

import android.content.Context
import android.graphics.Rect
import android.view.MotionEvent
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.core.view.children
import androidx.recyclerview.widget.RecyclerView
import s.yarlykov.example.R
import s.yarlykov.example.extentions.forceSiblingsToDo
import s.yarlykov.example.prod.animation.Animators
import kotlin.math.abs
import kotlin.math.sign

/**
 * Класс для обработки боковых свайпов на элементах списка, содержащих сообщения UNC (Notification),
 * то есть хранимых в NotificationController.Holder. Класс заточен на работу с определенным layout
 * и ожидает, что в нем присутствуют элементы c идентификаторами: id/lower_layer, id/upper_layer,
 * id/cancel_button, id/action_button.
 *
 * Класс использует компонент GestureDirectionDetector, чтобы определить направление свайпа и
 * перехватить обработку горизонтальных движений. Если детектор зафиксировал свайп Right/Left,
 * то метод onInterceptTouchEvent перехватывает ACTION_MOVE и все оследующие события этого жеста
 * поступают в onTouchEvent.
 *
 * RecyclerView.OnScrollListener позволяет отслеживать события скола и реагировать на них
 * позиционирование "открытых" элементов списка в начальное состояние.
 */
class NotificationTouchHelper(val context: Context) :
    RecyclerView.OnScrollListener(), RecyclerView.OnItemTouchListener {

    // ViewHolder, который выбран пользователем
    private var selectedViewHolder: NotificationController.Holder? = null

    // Направление свайпа
    private var direction: GestureDirectionDetector.Direction = Direction.Unknown

    // Детектор направления свайпа
    private val gestureDetector = object : GestureDirectionDetector(context) {
        override fun onDirectionDetected(direction: Direction) {
            this@NotificationTouchHelper.direction = direction
        }
    }

    private var prevX = 0f
    private var prevY = 0f

    override fun onInterceptTouchEvent(rv: RecyclerView, event: MotionEvent): Boolean {
        gestureDetector.onTouchEvent(event)

        return when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                /*Timber.d("NTH MotionEvent.ACTION_DOWN")*/
                // Отслеживаем тачи по элементам сообщений. Декоративные элементы игнорим.
                selectedViewHolder = findTouchedViewHolder(rv, event)
                false
            }
            MotionEvent.ACTION_MOVE -> {
                /*Timber.d("NTH MotionEvent.ACTION_MOVE")*/

                val holder = selectedViewHolder ?: return false

                // При фиксации бокового движения перехватываем если:
                // - Двигаемся влево (делаем доступной кнопку "Delete")
                // - ИЛИ Двигаемся вправо И у холдера isEditable == true.
                //   Это значит, что элемент ещё "не прочитан" и можно показать кнопку "Прочитано".
                //
                // Если событие перехватываем, то все последующие ACTION_MOVE и финальный ACTION_UP
                // будут поступать только в onTouchEvent. Вызовы onInterceptTouchEvent возобновятся
                // только после обработки ACTION_UP в onTouchEvent.
                val isIntercepted = (direction == Direction.Left ||
                        (direction == Direction.Right && holder.isEditable))

                // Стартовые координаты для свайпа устанавливаем при фиксации бокового жеста
                if (isIntercepted) {
                    prevX = event.x
                    prevY = event.y
                }
                isIntercepted
            }
            // Сюда попадаем если оба ACTION_DOWN и ACTION_MOVE вернули false. Например это
            // происходит при клике на кнопке.
            MotionEvent.ACTION_UP -> {
                /*Timber.d("NTH MotionEvent.ACTION_UP")*/
                // TODO Так делать не надо, потому что cancelView() деактивирует кнопки и
                // TODO клики по ним перестают работать.
//                selectedViewHolder?.itemView?.cancelView()

                // TODO ХЗ надо ли так делать. Тач в любом месте закрывает элемент
                selectedViewHolder
                    ?.itemView
                    ?.findViewById<View>(R.id.upper_layer)
                    ?.let {
                        Animators.translateX(it, 0f)
                    }
                selectedViewHolder = null
                false
            }
            else -> {
                false
            }
        }
    }

    override fun onTouchEvent(rv: RecyclerView, event: MotionEvent) {
        gestureDetector.onTouchEvent(event)

        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                // TODO Событие не получаем потому что игнорим в onInterceptTouchEvent
            }
            MotionEvent.ACTION_MOVE -> {
                /*Timber.d("NTH MotionEvent.ACTION_MOVE")*/
                val holder = selectedViewHolder ?: return

                val upperLayer = holder.itemView.findViewById<View>(R.id.upper_layer)

                // В качестве базы расчета смещения используется предыдущее значение event.x (prevX)
                // Это позволяет правильно отрабатывать в обоих случаях - когда элемент тянем
                // из исходного состояния и из состояния ожидания.
                val dX = event.x - prevX
                val translationX = dX + upperLayer.translationX
                val maxTranslation = getMaxTranslation(holder.itemView, translationX.toInt())

                upperLayer.translationX =
                    if (abs(translationX) > maxTranslation) {
                        // Смещения достаточно, чтобы выбрать элемент
                        holder.itemView.isSelected = true
                        maxTranslation * sign(translationX)
                    } else {
                        // Смещения не достаточно, чтобы выбрать элемент.
                        holder.itemView.isSelected = false
                        upperLayer.translationX + dX
                    }
                prevX = event.x

                // Закрасить фон видимой части нижнего слоя
                drawLowerLayer(holder.itemView, upperLayer.translationX)
            }
            MotionEvent.ACTION_UP -> {
                /*Timber.d("NTH MotionEvent.ACTION_UP")*/

                prevX = 0f
                prevY = 0f

                val holder = selectedViewHolder ?: return

                // Завершили жест выбором элемента: при "открытой" кнопке.
                if (holder.itemView.isSelected) {
                    holder.itemView.apply {
                        forceSiblingsToDo {
                            cancelView()
                        }
                        enableButton()
                    }
                    return
                }

                // Завершили жест не сделав выбор, то есть без "открытия" кнопки.
                holder.itemView.cancelView()
                selectedViewHolder = null
            }
            else -> {
            }
        }
    }

    override fun onRequestDisallowInterceptTouchEvent(disallowIntercept: Boolean) {
        // nothing to do
    }

    /**
     * При скроле нужно анимированно "закрыть" все открытые элементы и отменить selectedHolder
     */
    override fun onScrollStateChanged(recyclerView: RecyclerView, newState: Int) {
        super.onScrollStateChanged(recyclerView, newState)
        recyclerView.children.forEach { child ->
            child.cancelView()
        }
        selectedViewHolder?.itemView?.isSelected = false
        selectedViewHolder = null
    }

    /**
     * Закрасить фон вокруг кнопки нужным цветом.
     */
    private fun drawLowerLayer(view: View, offset: Float) {
        val buttonsLayerBackgroundColor = if (offset > 0f) {
            android.R.color.holo_green_dark
        } else {
            android.R.color.holo_orange_dark
        }

        view.setBackgroundColor(
            ContextCompat.getColor(
                view.context,
                buttonsLayerBackgroundColor
            )
        )
    }

    /**
     * Вычислить максимальную дистанцию для смещения ползунка таким образом, чтобы появляющаяся
     * кнопка оказалась горизонтально по центру между ползунком и границей RecyclerView. Например,
     * если двигаем ползунок вправо, то открывается левая кнопка. Берем её ширину и двойное значение
     * левого маргина. В сумме это и будет максимальным translation.
     */
    private fun getMaxTranslation(itemView: View, dX: Int): Int {
        val default = itemView.width / 4

        val button = if (dX > 0) {
            itemView.findViewById<View>(R.id.action_button)
        } else {
            itemView.findViewById<View>(R.id.cancel_button)
        }

        val lp = button.layoutParams as? ViewGroup.MarginLayoutParams ?: return default
        val margin = if (dX > 0) lp.leftMargin else lp.rightMargin

        return button.width + margin * 2
    }

    /**
     * Найти ViewHolder с сообщением. Элементы с декором игнорим.
     */
    private fun findTouchedViewHolder(
        recyclerView: RecyclerView,
        event: MotionEvent
    ): NotificationController.Holder? {

        val (x, y) = event.x to event.y

        selectedViewHolder?.let { viewHolder ->
            if (viewHolder.itemView.hitTest(x.toInt(), y.toInt())) {
                return viewHolder
            }
        }

        recyclerView.findChildViewUnder(x, y)?.let { child ->
            val holder = recyclerView.findContainingViewHolder(child)
            if (holder is NotificationController.Holder) {
                return holder
            }
        }
        return null
    }

    /**
     * Проверить попадание координат (x, y) в область View
     */
    private fun View.hitTest(x: Int, y: Int): Boolean {
        return Rect().apply { getHitRect(this) }.contains(x, y)
    }

    /**
     * Вернуть View элемента списка в исходное состояние:
     * - isSelected = false
     * - верхний слой в начальную позицию
     * - кнопки деактивировать, чтобы не реагировали на тачи сквозь верхний слой
     */
    private fun View.cancelView() {
        val upperLayer = findViewById<View>(R.id.upper_layer)
        if (upperLayer?.translationX != 0f) {
            Animators.translateX(upperLayer, 0f)
        }

        findViewById<View>(R.id.action_button)?.isEnabled = false
        findViewById<View>(R.id.cancel_button)?.isEnabled = false
        isSelected = false
    }

    /**
     * Сделать активной кнопку, которая видна пользователю
     */
    private fun View.enableButton() {
        val upperLayer = findViewById<View>(R.id.upper_layer) ?: return

        if (upperLayer.translationX > 0f) {
            findViewById<View>(R.id.action_button)?.isEnabled = true
        }
        if (upperLayer.translationX < 0f) {
            findViewById<View>(R.id.cancel_button)?.isEnabled = true
        }
    }
}