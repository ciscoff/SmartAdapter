package s.yarlykov.example.prod.rv

import android.content.Context
import android.view.MotionEvent
import android.view.ViewConfiguration
import kotlin.math.atan2
import kotlin.math.sqrt

/**
 * См. проект: https://github.com/stfalcon-studio/FrescoImageViewer
 */

typealias Direction = GestureDirectionDetector.Direction

abstract class GestureDirectionDetector(context: Context) {

    abstract fun onDirectionDetected(direction: Direction)

    /** default = 14 */
    private val touchSlop = ViewConfiguration.get(context).scaledTouchSlop

    private var startX = 0f
    private var startY = 0f
    private var isDetected = false

    fun onTouchEvent(event: MotionEvent) {
        when (event.actionMasked) {
            MotionEvent.ACTION_DOWN -> {
                startX = event.x
                startY = event.y
            }
            MotionEvent.ACTION_MOVE -> {
                if (!isDetected && getDistance(event) > touchSlop) {
                    isDetected = true
                    onDirectionDetected(getDirection(startX, startY, event.x, event.y))
                }
            }
            MotionEvent.ACTION_UP -> {
                onDirectionDetected(Direction.Unknown)
                startX = 0f
                startX = 0f
                isDetected = false
            }
            MotionEvent.ACTION_CANCEL -> {
                onDirectionDetected(Direction.Unknown)
            }
        }
    }

    private fun getDirection(x1: Float, y1: Float, x2: Float, y2: Float): Direction {
        val angle = getAngle(x1, y1, x2, y2)
        return Direction.get(angle)
    }

    /**
     * Расстояние между точками
     */
    private fun getDistance(event: MotionEvent): Float {
        val dX = event.x - startX
        val dY = event.y - startY
        return sqrt(dX * dX + dY * dY)
    }

    /**
     * Угол наклона линии между точками (x1, y1) - (x2, y2)
     */
    private fun getAngle(x1: Float, y1: Float, x2: Float, y2: Float): Double {
        val angle = Math.toDegrees(atan2((y1 - y2).toDouble(), (x2 - x1).toDouble()))
        return if (angle < 0) {
            angle + 360.0
        } else angle
    }

    /**
     * Направление движения
     */
    enum class Direction {
        Up,
        Down,
        Left,
        Right,
        Unknown;

        companion object {
            fun get(angle: Double): Direction {
                return when (angle) {
                    in ANGLE_0..ANGLE_45, in ANGLE_315..ANGLE_360 -> Right
                    in ANGLE_225..ANGLE_315 -> Down
                    in ANGLE_45..ANGLE_135 -> Up
                    else -> Left
                }
            }

            private const val ANGLE_0 = 0.0
            private const val ANGLE_45 = 45.0
            private const val ANGLE_135 = 135.0
            private const val ANGLE_225 = 225.0
            private const val ANGLE_315 = 315.0
            private const val ANGLE_360 = 360.0
        }
    }
}