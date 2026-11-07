package com.desarrollomovielexample.actividadapirest.ui.pharmacy

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollomovielexample.actividadapirest.data.remote.toApiMessage
import com.desarrollomovielexample.actividadapirest.data.repository.MedicamentosRepository
import com.desarrollomovielexample.actividadapirest.domain.MedicamentoEnFarmacia
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.launch

data class PharmacyState(
    val items: List<MedicamentoEnFarmacia> = emptyList(),
    val isLoading: Boolean = false,
    val error: String? = null
)

@HiltViewModel
class PharmacyViewModel @Inject constructor(
    private val repository: MedicamentosRepository
) : ViewModel() {
    private val mutable = MutableStateFlow(PharmacyState())
    val state = mutable.asStateFlow()

    fun load(idFarmacia: String) {
        viewModelScope.launch {
            mutable.value = PharmacyState(isLoading = true)
            try {
                val items = repository.medicamentosPorFarmacia(idFarmacia)
                mutable.value = PharmacyState(items = items)
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                mutable.value = PharmacyState(error = error.toApiMessage())
            }
        }
    }
}