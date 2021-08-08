package s.yarlykov.example.prod.animation

import android.animation.Animator
import android.animation.AnimatorSet
import android.animation.ArgbEvaluator
import android.animation.ObjectAnimator
import android.graphics.drawable.*
import android.os.Build
import android.view.View
import android.view.animation.LinearInterpolator
import android.widget.ImageView
import androidx.cardview.widget.CardView

object Animators {

    private const val DEFAULT_DURATION = 180L
    private const val DEFAULT_DELAY = 200L

    private val defaultInterpolator = LinearInterpolator()

    fun hide(view: View?, before: (() -> Unit)? = null, after: (() -> Unit)? = null) {

        view?.let { _view ->
            ObjectAnimator.ofFloat(_view, View.ALPHA, 0f).apply {
                interpolator = LinearInterpolator()
                duration = DEFAULT_DURATION
                addListener(
                    listener(
                        before,
                        after
                    )
                )
            }.start()
        }
    }

    fun scale(
        view: View?,
        factor: Float,
        before: (() -> Unit)? = null,
        after: (() -> Unit)? = null
    ) {
        view?.let { _view ->

            val scaleX = ObjectAnimator.ofFloat(_view, View.SCALE_X, factor).apply {
                interpolator = LinearInterpolator()
                duration =
                    DEFAULT_DURATION
            }
            val scaleY = ObjectAnimator.ofFloat(_view, View.SCALE_Y, factor).apply {
                interpolator = LinearInterpolator()
                duration =
                    DEFAULT_DURATION
            }

            AnimatorSet().apply {
                addListener(
                    listener(
                        before,
                        after
                    )
                )
                playTogether(scaleX, scaleY)
            }.start()
        }
    }

    /**
     * Запускаем анимацию в AVD.
     * По окончании анимации устанавливаем в ImageView другой AVD, который будет работать
     * для reverse-анимации, если потребуется такая.
     */
    fun animateAvd(view: ImageView, reverse: Drawable) {
        val drawable = view.drawable

        if (Build.VERSION.SDK_INT >= Build.VERSION_CODES.M) {
            if (drawable is Animatable2) {

                drawable.registerAnimationCallback(object : Animatable2.AnimationCallback() {
                    override fun onAnimationEnd(drawable: Drawable?) {
                        view.setImageDrawable(reverse)
                    }
                })
                drawable.start()
            }
        } else if (drawable is Animatable) {
            view.postDelayed({
                view.setImageDrawable(reverse)
            }, DEFAULT_DELAY)
            drawable.start()
        }
    }

    /**
     * Анимирует цвет, то есть работает со значениями Int, которые интерпретирует как ARGB
     *
     */
    fun color(
        view: View?,
        colorFrom: Int,
        colorTo: Int,
        before: (() -> Unit)? = null,
        after: (() -> Unit)? = null
    ) {
        ObjectAnimator.ofObject(
            view,
            "backgroundColor",
            ArgbEvaluator(),
            colorFrom,
            colorTo
        ).apply {
            interpolator =
                defaultInterpolator
            duration =
                DEFAULT_DURATION
            addListener(
                listener(
                    before,
                    after
                )
            )
        }.start()
    }

    fun cardColor(
        view: CardView?,
        colorFrom: Int,
        colorTo: Int,
        before: (() -> Unit)? = null,
        after: (() -> Unit)? = null
    ) {
        val colors = arrayOf(ColorDrawable(colorFrom), ColorDrawable(colorTo))
        val trans = TransitionDrawable(colors)

        view?.background = trans
        trans.startTransition(DEFAULT_DURATION.toInt())
    }

    fun translateX(
        view: View?,
        shift: Float,
        animDuration: Long = DEFAULT_DURATION,
        before: (() -> Unit)? = null,
        after: (() -> Unit)? = null
    ) {
        view?.let { _view ->
            ObjectAnimator.ofFloat(_view, View.TRANSLATION_X, shift).apply {
                interpolator =
                    defaultInterpolator
                duration = animDuration
                addListener(
                    listener(
                        before,
                        after
                    )
                )
            }.start()
        }
    }

    /**
     * Генерит AnimatorListenerTemplate с указанным набором кода
     */
    private fun listener(before: (() -> Unit)?, after: (() -> Unit)?): Animator.AnimatorListener {
        return AnimatorListenerTemplate(
            onStart = {
                before?.invoke()
            },
            onEnd = {
                after?.invoke()
            })
    }
}