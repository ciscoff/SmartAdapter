package s.yarlykov.decoration.sticky

/**
 * Признак Sticky Aware элемента. Идея такова: Если мы используем список со Sticky, то все
 * элементы этого списка должны участвовать в Sticky-функционале, а именно предоставлять некие
 * признаки, по которым в каждый момент можно выбрать правильную битмапу для липучки.
 *
 * В Sticky-списках мы группируем элементы по какому-то признаку, например по дате или дню недели.
 * Каждая группа имеет свой Header-элемент, который и становится липучкой. То есть группа состоит
 * из заголовка и элементов. Удобно иметь какой-нибудь атрибут, который имеет одинаковое и
 * уникальное значение у всех членов группы. Я назвал этот атрибут groupId: Int. Его значение
 * можно формировать из любого поля модели. Например все сообщения чата на 10 августа будут
 * иметь одинаковую дату "2021-08-10", тогда значение "2021-08-10".hashCode() тоже будет
 * одинаковым у всех.
 *
 * А главное вот в чем: в каждый момент можно определить нужную битмапу для липучки. Достаточно
 * просто найти самый верхний видимый элемент списка и по его groupId выбрать из HashMap
 * нужную битмапу. ИФСЁ !
 */
interface StickyHolder {
    val groupId: Int

    /**
     * Признак элемента заголовка
     */
    interface Header : StickyHolder

    /**
     * ПРизнак элемента данных
     */
    interface Data : StickyHolder

    companion object {
        const val NO_ID = -1
    }
}