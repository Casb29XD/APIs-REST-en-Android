package com.desarrollomovielexample.actividadapirest.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.viewModelScope
import com.desarrollomovielexample.actividadapirest.data.network.NetworkConnectivityObserver
import com.desarrollomovielexample.actividadapirest.data.remote.toApiMessage
import com.desarrollomovielexample.actividadapirest.data.repository.MedicamentosRepository
import com.desarrollomovielexample.actividadapirest.domain.Medicamento
import dagger.hilt.android.lifecycle.HiltViewModel
import javax.inject.Inject
import kotlinx.coroutines.CancellationException
import kotlinx.coroutines.Job
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update
import kotlinx.coroutines.launch

data class HomeState(
    val items: List<Medicamento> = emptyList(),
    val isLoading: Boolean = false,
    val isOffline: Boolean = false,
    val hasNetworkStatus: Boolean = false,
    val searchQuery: String = "",
    val selectedLaboratorio: String = "",
    val error: String? = null,
    val canLoadMore: Boolean = true
)

@HiltViewModel
class HomeViewModel @Inject constructor(
    private val repository: MedicamentosRepository,
    network: NetworkConnectivityObserver
) : ViewModel() {
    private val mutable = MutableStateFlow(HomeState())
    val state = mutable.asStateFlow()
    private var offset = 0
    private var generation = 0
    private var filterJob: Job? = null
    private var loadJob: Job? = null

    init {
        requestPage(reset = true, requestGeneration = generation)
        viewModelScope.launch {
            network.isOnline.collect { online ->
                mutable.update { it.copy(isOffline = !online, hasNetworkStatus = true) }
                if (online && mutable.value.items.isEmpty() && mutable.value.error != null) retry()
            }
        }
    }

    fun search(value: String) {
        if (value == mutable.value.searchQuery) return
        mutable.update { it.copy(searchQuery = value) }
        reloadForFilters()
    }

    fun laboratory(value: String) {
        if (value == mutable.value.selectedLaboratorio) return
        mutable.update { it.copy(selectedLaboratorio = value) }
        reloadForFilters()
    }

    private fun reloadForFilters() {
        generation++
        val currentGeneration = generation
        filterJob?.cancel()
        loadJob?.cancel()
        loadJob = null
        offset = 0
        mutable.update {
            it.copy(items = emptyList(), isLoading = true, error = null, canLoadMore = true)
        }
        filterJob = viewModelScope.launch {
            delay(300)
            requestPage(reset = true, requestGeneration = currentGeneration)
        }
    }

    fun loadMore() {
        if (mutable.value.isLoading || !mutable.value.canLoadMore) return
        requestPage(reset = false, requestGeneration = generation)
    }

    fun retry() {
        generation++
        filterJob?.cancel()
        loadJob?.cancel()
        loadJob = null
        mutable.update { it.copy(isLoading = false, error = null) }
        requestPage(reset = true, requestGeneration = generation)
    }

    private fun requestPage(reset: Boolean, requestGeneration: Int) {
        if (loadJob?.isActive == true) return
        if (reset) offset = 0
        val stateAtRequest = mutable.value
        loadJob = viewModelScope.launch {
            mutable.update { it.copy(isLoading = true, error = null) }
            try {
                val page = repository.list(
                    offset = offset,
                    limit = PAGE_SIZE,
                    query = stateAtRequest.searchQuery,
                    laboratory = stateAtRequest.selectedLaboratorio
                )
                if (requestGeneration != generation) return@launch
                offset += page.size
                mutable.update { old ->
                    old.copy(
                        items = if (reset) page else old.items + page,
                        canLoadMore = page.size == PAGE_SIZE,
                        isLoading = false
                    )
                }
            } catch (cancelled: CancellationException) {
                throw cancelled
            } catch (error: Exception) {
                if (requestGeneration != generation) return@launch
                mutable.update { it.copy(isLoading = false, error = error.toApiMessage()) }
            }
        }
    }

    private companion object {
        const val PAGE_SIZE = 20
    }
}
