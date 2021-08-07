package s.yarlykov.decoration.round

import android.graphics.Outline
import android.os.Build
import android.view.View
import android.view.ViewOutlineProvider
import androidx.annotation.RequiresApi

/**
 * Outline - простая форма для отрисовки теней View или для обрезки (clip) содержимого View
 *
 * @param outlineRadius Радиус угла
 * @param roundMode Режим отрисовки углов (все, только один какой-то, ...)
 */
@RequiresApi(Build.VERSION_CODES.LOLLIPOP)
class RoundOutlineProvider(
    var outlineRadius: Float = 0f,
    var roundMode: RoundMode = RoundMode.NONE
) : ViewOutlineProvider() {

    private val topOffset
        get() = when (roundMode) {
            RoundMode.ALL, RoundMode.TOP -> 0
            RoundMode.NONE, RoundMode.BOTTOM -> cornerRadius.toInt()
        }
    private val bottomOffset
        get() = when (roundMode) {
            RoundMode.ALL, RoundMode.BOTTOM -> 0
            RoundMode.NONE, RoundMode.TOP -> cornerRadius.toInt()
        }
    private val cornerRadius
        get() = if (roundMode == RoundMode.NONE) {
            0f
        } else {
            outlineRadius
        }

    /**
     * Установить форму контура нашей View. Тут можно поиграться со значениями, чтобы увидеть
     * как меняется размер и обрезка.
     */
    override fun getOutline(view: View, outline: Outline) {

        outline.setRoundRect(
            0,
            0 - topOffset,
            view.width,
            view.height + bottomOffset,
            cornerRadius
        )
    }
}