package s.yarlykov.decoration.round

/**
 * Политика определения элементов, у которых нужно закруглять углы:
 * - Every: у каждого отдельного элемента согласно установленному RoundMode
 * - Group: у группы однотипных (одинаковый viewType) элементов
 *
 * Для режима Every можно дополнительно указать какие углы закруглять через RoundMode
 */
sealed class RoundPolitic {
    class Every(val roundMode: RoundMode): RoundPolitic()
    object Group: RoundPolitic()
}