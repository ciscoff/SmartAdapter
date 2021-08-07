package s.yarlykov.decoration.round

/**
 * Какие углы закругляем:
 * - TOP: оба верхних
 * - BOTTOM: оба нижних
 * - ALL: все
 * - NONE: ни одного
 */
enum class RoundMode {
    TOP,
    BOTTOM,
    ALL,
    NONE
}