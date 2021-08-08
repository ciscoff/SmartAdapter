package s.yarlykov.example.prod.animation

import android.animation.Animator

/**
 * Шаблон для переопределения Animator.AnimatorListener
 */
class AnimatorListenerTemplate(
    private val onStart: () -> Unit,
    private val onEnd: () -> Unit
) : Animator.AnimatorListener {

    override fun onAnimationStart(animation: Animator?) {
        onStart()
    }

    override fun onAnimationEnd(animation: Animator?) {
        onEnd()
    }

    override fun onAnimationRepeat(animation: Animator?) {}
    override fun onAnimationCancel(animation: Animator?) {}
}