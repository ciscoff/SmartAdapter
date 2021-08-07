package s.yarlykov.decoration

/**
 * Контейнер DecorBinder связывает viewType с декоратором отдельного типа.
 */
class DecorBinder<D>(val viewType: Int, val decorator: D)