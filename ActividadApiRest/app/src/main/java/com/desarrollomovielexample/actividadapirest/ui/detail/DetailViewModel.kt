package com.desarrollomovielexample.actividadapirest.ui.detail

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollomovielexample.actividadapirest.data.remote.toApiMessage
import com.desarrollomovielexample.actividadapirest.data.repository.MedicamentosRepository
import com.desarrollomovielexample.actividadapirest.domain.Medicamento
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class DetailState(
    val item: Medicamento? = null,
    val loading: Boolean = true,
    val error: String? = null
)

@HiltViewModel
class DetailViewModel @Inject constructor(
    private val repository: MedicamentosRepository
) : ViewModel() {
    private val mutable = MutableStateFlow(DetailState())
    val state = mutable.asStateFlow()

    fun load(id: String) {
        viewModelScope.launch {
            mutable.value = DetailState()
            try {
                val item = repository.detail(id)
                mutable.value = DetailState(
                    item = item,
                    loading = false,
                    error = if (item == null) "Medicamento no encontrado." else null
                )
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutable.value = DetailState(loading = false, error = error.toApiMessage())
            }
        }
    }
}