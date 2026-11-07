package com.desarrollomovielexample.actividadapirest

import android.os.Bundle
import androidx.activity.ComponentActivity
import androidx.activity.compose.setContent
import androidx.compose.foundation.background
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.*
import androidx.compose.foundation.lazy.LazyColumn
import androidx.compose.foundation.lazy.LazyListState
import androidx.compose.foundation.lazy.items
import androidx.compose.foundation.lazy.rememberLazyListState
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.*
import androidx.compose.runtime.*
import androidx.compose.runtime.snapshotFlow
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.desarrollomovielexample.actividadapirest.domain.Farmacia
import com.desarrollomovielexample.actividadapirest.domain.Medicamento
import com.desarrollomovielexample.actividadapirest.domain.MedicamentoEnFarmacia
import com.desarrollomovielexample.actividadapirest.ui.detail.DetailViewModel
import com.desarrollomovielexample.actividadapirest.ui.home.HomeViewModel
import com.desarrollomovielexample.actividadapirest.ui.home.HomeState
import com.desarrollomovielexample.actividadapirest.ui.pharmacy.PharmacyViewModel
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.flow.distinctUntilChanged

@AndroidEntryPoint
class MainActivity : ComponentActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContent {
            MaterialTheme {
                var selectedMedicine by remember { mutableStateOf<String?>(null) }
                var selectedPharmacy by remember { mutableStateOf<Farmacia?>(null) }
                when {
                    selectedPharmacy != null -> PharmacyScreen(
                        pharmacy = selectedPharmacy!!,
                        onBack = { selectedPharmacy = null }
                    )
                    selectedMedicine != null -> DetailScreen(
                        id = selectedMedicine!!,
                        onBack = { selectedMedicine = null },
                        onSelectPharmacy = { selectedPharmacy = it }
                    )
                    else -> HomeScreen(onSelect = { selectedMedicine = it })
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun HomeScreen(onSelect: (String) -> Unit, vm: HomeViewModel = hiltViewModel()) {
    val state by vm.state.collectAsStateWithLifecycle()
    val listState = rememberLazyListState()

    LaunchedEffect(listState, state.items.size, state.canLoadMore, state.isLoading, state.error) {
        snapshotFlow { listState.layoutInfo.visibleItemsInfo.lastOrNull()?.index ?: -1 }
            .distinctUntilChanged()
            .collect { lastVisibleIndex ->
                if (state.items.isNotEmpty() && state.canLoadMore && !state.isLoading &&
                    state.error == null && lastVisibleIndex >= state.items.size - PREFETCH_DISTANCE
                ) vm.loadMore()
            }
    }

    Scaffold(topBar = {
        Column {
            TopAppBar(title = { Text("Medicamentos") })
            ConnectionStatusBanner(state)
        }
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(16.dp)) {
            OutlinedTextField(
                value = state.searchQuery,
                onValueChange = vm::search,
                label = { Text("Buscar por nombre o principio activo") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (state.searchQuery.isNotEmpty()) {
                        TextButton(onClick = { vm.search("") }) { Text("Limpiar") }
                    }
                }
            )
            OutlinedTextField(
                value = state.selectedLaboratorio,
                onValueChange = vm::laboratory,
                label = { Text("Filtrar por laboratorio") },
                modifier = Modifier.fillMaxWidth(),
                singleLine = true,
                trailingIcon = {
                    if (state.selectedLaboratorio.isNotEmpty()) {
                        TextButton(onClick = { vm.laboratory("") }) { Text("Limpiar") }
                    }
                }
            )
            when {
                state.isLoading && state.items.isEmpty() -> CircularProgressIndicator()
                state.error != null && state.items.isEmpty() -> Column {
                    Text(state.error ?: "Error")
                    Button(onClick = vm::retry) { Text("Reintentar") }
                }
                state.items.isEmpty() -> Column {
                    Text("No hay resultados con esos filtros.")
                    Button(onClick = {
                        vm.search("")
                        vm.laboratory("")
                    }) { Text("Limpiar filtros") }
                }
                else -> MedicamentosList(
                    state = state,
                    listState = listState,
                    onSelect = onSelect,
                    onLoadMore = vm::loadMore
                )
            }
        }
    }
}

@Composable
private fun ConnectionStatusBanner(state: HomeState) {
    val backgroundColor = when {
        !state.hasNetworkStatus -> MaterialTheme.colorScheme.surfaceVariant
        state.isOffline -> MaterialTheme.colorScheme.errorContainer
        else -> MaterialTheme.colorScheme.primaryContainer
    }
    val foregroundColor = when {
        !state.hasNetworkStatus -> MaterialTheme.colorScheme.onSurfaceVariant
        state.isOffline -> MaterialTheme.colorScheme.onErrorContainer
        else -> MaterialTheme.colorScheme.onPrimaryContainer
    }
    val label = when {
        !state.hasNetworkStatus -> "Verificando conexión…"
        state.isOffline -> "Sin conexión"
        else -> "En línea"
    }
    Surface(color = backgroundColor) {
        Row(
            modifier = Modifier.fillMaxWidth().padding(horizontal = 16.dp, vertical = 6.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Box(Modifier.size(9.dp).background(foregroundColor, CircleShape))
            Spacer(Modifier.width(8.dp))
            Text(label, color = foregroundColor, style = MaterialTheme.typography.labelMedium)
        }
    }
}

@Composable
private fun ColumnScope.MedicamentosList(
    state: HomeState,
    listState: LazyListState,
    onSelect: (String) -> Unit,
    onLoadMore: () -> Unit
) {
    LazyColumn(Modifier.fillMaxWidth().weight(1f), state = listState) {
        items(state.items, key = { it.id }) { item ->
            MedicamentoCard(item, onClick = { onSelect(item.id) })
        }
        if (state.isLoading) {
            item(key = "loading") {
                Row(Modifier.fillMaxWidth().padding(16.dp), horizontalArrangement = Arrangement.Center) {
                    CircularProgressIndicator()
                }
            }
        }
        if (state.error != null && state.items.isNotEmpty()) {
            item(key = "pagination-error") {
                Column(
                    Modifier.fillMaxWidth().padding(16.dp),
                    horizontalAlignment = Alignment.CenterHorizontally
                ) {
                    Text(state.error)
                    Button(onClick = onLoadMore) { Text("Reintentar carga") }
                }
            }
        }
    }
}

@Composable
private fun MedicamentoCard(item: Medicamento, onClick: () -> Unit) {
    Card(Modifier.fillMaxWidth().padding(vertical = 4.dp).clickable(onClick = onClick)) {
        Column(Modifier.padding(16.dp)) {
            Text(item.nombreComercial, style = MaterialTheme.typography.titleMedium)
            Text(item.principioActivo ?: "Principio activo sin registrar")
            Text(item.laboratorio ?: "Laboratorio sin registrar")
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun DetailScreen(
    id: String,
    onBack: () -> Unit,
    onSelectPharmacy: (Farmacia) -> Unit,
    vm: DetailViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(id) { vm.load(id) }
    Scaffold(topBar = {
        TopAppBar(
            title = { Text("Detalle") },
            navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }
        )
    }) { padding ->
        Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
            when {
                state.loading -> CircularProgressIndicator()
                state.error != null -> Column {
                    Text(state.error ?: "Error")
                    Button(onClick = { vm.load(id) }) { Text("Reintentar") }
                }
                else -> state.item?.let { item ->
                    Text(item.nombreComercial, style = MaterialTheme.typography.headlineSmall)
                    Text("Principio activo: ${item.principioActivo ?: "—"}")
                    Text("Laboratorio: ${item.laboratorio ?: "—"}")
                    Text("Presentación: ${item.presentacion ?: "—"}")
                    Text("Concentración: ${item.concentracion ?: "—"}")
                    Spacer(Modifier.height(16.dp))
                    Text("Existencias por farmacia", style = MaterialTheme.typography.titleMedium)
                    if (item.inventario.isEmpty()) Text("Sin inventario registrado (HTTP 204 o lista vacía).")
                    item.inventario.forEach { stock ->
                        val pharmacy = stock.farmacia
                        if (pharmacy != null) {
                            TextButton(onClick = { onSelectPharmacy(pharmacy) }) {
                                Text(pharmacy.nombre)
                            }
                            Text("Stock: ${stock.stockActual ?: 0} · precio: ${stock.precioUnitario ?: "—"}")
                            pharmacy.direccion?.let { Text(it) }
                        } else {
                            Text("Farmacia no disponible · stock: ${stock.stockActual ?: 0}")
                        }
                    }
                }
            }
        }
    }
}

@OptIn(ExperimentalMaterial3Api::class)
@Composable
private fun PharmacyScreen(
    pharmacy: Farmacia,
    onBack: () -> Unit,
    vm: PharmacyViewModel = hiltViewModel()
) {
    val state by vm.state.collectAsStateWithLifecycle()
    LaunchedEffect(pharmacy.id) { vm.load(pharmacy.id) }

    Scaffold(topBar = {
        TopAppBar(
            title = { Text(pharmacy.nombre) },
            navigationIcon = { TextButton(onClick = onBack) { Text("Volver") } }
        )
    }) { padding ->
        when {
            state.isLoading -> Box(
                Modifier.fillMaxSize().padding(padding),
                contentAlignment = Alignment.Center
            ) { CircularProgressIndicator() }
            state.error != null -> Column(Modifier.fillMaxSize().padding(padding).padding(20.dp)) {
                Text(state.error ?: "Error al cargar inventario.")
                Button(onClick = { vm.load(pharmacy.id) }) { Text("Reintentar") }
            }
            state.items.isEmpty() -> Box(
                Modifier.fillMaxSize().padding(padding).padding(20.dp),
                contentAlignment = Alignment.Center
            ) {
                Text("Esta farmacia no tiene medicamentos registrados (HTTP 204 o lista vacía).")
            }
            else -> LazyColumn(
                Modifier.fillMaxSize().padding(padding).padding(horizontal = 16.dp)
            ) {
                item {
                    pharmacy.direccion?.let { Text(it, modifier = Modifier.padding(vertical = 12.dp)) }
                    Text("Medicamentos disponibles", style = MaterialTheme.typography.titleLarge)
                }
                items(state.items, key = { it.medicamento.id }) { row ->
                    FarmaciaMedicamentoCard(row)
                }
            }
        }
    }
}

@Composable
private fun FarmaciaMedicamentoCard(row: MedicamentoEnFarmacia) {
    Card(Modifier.fillMaxWidth().padding(vertical = 5.dp)) {
        Column(Modifier.padding(16.dp)) {
            Text(row.medicamento.nombreComercial, style = MaterialTheme.typography.titleMedium)
            Text("Principio activo: ${row.medicamento.principioActivo ?: "—"}")
            Text("Laboratorio: ${row.medicamento.laboratorio ?: "—"}")
            Text("Presentación: ${row.medicamento.presentacion ?: "—"}")
            Text("Stock: ${row.stockActual ?: 0}")
            Text("Precio unitario: ${row.precioUnitario ?: "No registrado"}")
        }
    }
}

private const val PREFETCH_DISTANCE = 5
