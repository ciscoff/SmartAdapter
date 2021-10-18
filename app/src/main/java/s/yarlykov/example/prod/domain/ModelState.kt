package s.yarlykov.example.prod.domain

sealed class ModelState {
    object Init : ModelState()
    object Loading : ModelState()   // Начальная загрузка. Шиммер эффект.
    object Updating : ModelState()  // Подгрузка при пагинации, например.
    data class Failure(val data: String? = null) : ModelState()
    data class Success(val data: List<MockMessage>) : ModelState()
    data class Updated(val data: List<MockMessage>) : ModelState()
}