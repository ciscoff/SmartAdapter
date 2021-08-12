package s.yarlykov.example.prod.domain

sealed class ModelState {
    object Init : ModelState()
    object Loading : ModelState()
    data class Failure(val data: String? = null) : ModelState()
    data class Success(val data: List<MockMessage>) : ModelState()
    data class Update(val data: List<MockMessage>) : ModelState()
}