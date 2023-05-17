package com.example.hannapp.ui.screens

import androidx.compose.foundation.layout.*
import androidx.compose.material.icons.Icons
import androidx.compose.material.icons.filled.Add
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.Icon
import androidx.compose.material3.IconButton
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.collectAsState
import androidx.compose.runtime.getValue
import androidx.compose.runtime.mutableStateOf
import androidx.compose.runtime.remember
import androidx.compose.runtime.saveable.rememberSaveable
import androidx.compose.runtime.setValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.platform.LocalFocusManager
import androidx.compose.ui.res.painterResource
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.tooling.preview.Preview
import androidx.hilt.navigation.compose.hiltViewModel
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import androidx.navigation.NavHostController
import androidx.navigation.compose.rememberNavController
import androidx.paging.PagingData
import androidx.paging.compose.LazyPagingItems
import androidx.paging.compose.collectAsLazyPagingItems
import com.example.hannapp.R
import com.example.hannapp.data.model.NutrimentUiLogModel
import com.example.hannapp.data.model.NutritionUiModel
import com.example.hannapp.ui.button.FAB
import com.example.hannapp.ui.components.AppScaffold
import com.example.hannapp.ui.components.AppTopBar
import com.example.hannapp.ui.components.NavigationBar
import com.example.hannapp.ui.history.NutrimentHistoryContent
import com.example.hannapp.ui.mood.Mood
import com.example.hannapp.ui.output.CalculationContent
import com.example.hannapp.ui.selection.SelectionContent
import com.example.hannapp.ui.theme.Constraints.PADDING
import com.example.hannapp.ui.theme.Constraints.SPACE_VERTICAL
import com.example.hannapp.ui.theme.HannAppTheme
import com.example.hannapp.ui.viewmodel.NutritionSelectViewModel
import com.example.hannapp.ui.viewmodel.NutritionUiState
import kotlinx.coroutines.flow.flowOf

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun NutrimentLogContent(
    modifier: Modifier,
    uiState: NutritionUiState,
    pagingItems: LazyPagingItems<NutritionUiModel>,
    loggedNutriments: List<NutrimentUiLogModel>,
    isEditMode: Boolean,
    onEditMode: (Boolean) -> Unit,
    onAdd: (String) -> Unit,
    navController: NavHostController,
    onClickBoxClick: () -> Unit,
    selectedNutriment: NutritionUiModel,
    onNutrimentSelected: (NutritionUiModel) -> Unit,
    onLoggedNutrimentSelected: (NutrimentUiLogModel) -> Unit,
    clear: () -> Unit
) {
    val snackBarHost = remember { SnackbarHostState() }
    val focusManager = LocalFocusManager.current
    var quantity by rememberSaveable { mutableStateOf("") }

    AppScaffold(
        topBar = {
            AppTopBar {
                if(isEditMode){
                    Text(
                        text = stringResource(id = R.string.edit_mode),
                        color = MaterialTheme.colorScheme.error,
                    )
                }
                IconButton(onClick = { clear() }) {
                    Icon(
                        painter = painterResource(id = R.drawable.restore),
                        contentDescription = null
                    )
                }
            }
        },
        bottomBar = { NavigationBar(navController) },
        snackBarHost = { SnackbarHost(hostState = snackBarHost) },
        floatingActionButton = {
            if (uiState.isSelectionValid) {
                FAB({ Icon(Icons.Default.Add, null) }) {
                    onAdd(quantity)
                    quantity = ""
                    focusManager.clearFocus()
                }
            }
        }
    ) { paddingValues ->
        Row(
            modifier = Modifier
                .fillMaxSize()
                .padding(paddingValues),
            verticalAlignment = Alignment.CenterVertically,
            horizontalArrangement = Arrangement.SpaceEvenly
        ) {
            Column(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(horizontal = PADDING)
            ) {
                SelectionContent(
                    modifier = Modifier.fillMaxWidth(),
                    uiState = uiState,
                    snackBarHost = snackBarHost,
                    onClickBoxClick = onClickBoxClick,
                    quantity = quantity,
                    onQuantityChanged = { quantity = it },
                    selectedNutriment = selectedNutriment,
                    onNutrimentChanged = { onNutrimentSelected(it) },
                    pagingItems = pagingItems,
                )

                Spacer(modifier = Modifier.height(SPACE_VERTICAL))

                NutrimentHistoryContent(
                    modifier = Modifier.fillMaxWidth(),
                    nutriments = loggedNutriments,
                    onLongClick = {
                        onEditMode(true)
                        onLoggedNutrimentSelected(it)
                        quantity = it.quantity.toString()
                    }
                )
            }

            CalculationContent(
                modifier = Modifier
                    .fillMaxWidth(0.5f)
                    .padding(horizontal = PADDING),
                mood = Mood.GREEN
            )
        }
    }
}

@Composable
fun NutrimentLogScreen(
    viewModel: NutritionSelectViewModel = hiltViewModel(),
    navController: NavHostController
) {
    val uiState by viewModel.uiState.collectAsState()
    val nutriments = viewModel.nutriments.collectAsLazyPagingItems()
    val logged by viewModel.nutrimentLog.collectAsStateWithLifecycle()
    val isEditMode by rememberSaveable { mutableStateOf(false) }

    NutrimentLogContent(
        modifier = Modifier.fillMaxSize(),
        uiState = uiState,
        pagingItems = nutriments,
        loggedNutriments = logged,
        isEditMode = isEditMode,
        onEditMode = {},
        onAdd = { toAdd ->
            if (uiState.isSelectionValid) {
                viewModel.apply {
                    castAsDouble(toAdd) { add(it) }
                }
            }
        },
        navController = navController,
        onClickBoxClick = { viewModel.getAll() },
        selectedNutriment = uiState.nutritionUiModel,
        onNutrimentSelected = { viewModel.select(it) },
        onLoggedNutrimentSelected = { TODO()},
        clear = { viewModel.clearAll() }
    )
}

@Preview(device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape")
@Composable
fun NutrimentLogScreen_LightMode() {
    HannAppTheme {
        NutrimentLogContent(
            modifier = Modifier,
            uiState = NutritionUiState(),
            pagingItems = flowOf(PagingData.from(listOf(NutritionUiModel()))).collectAsLazyPagingItems(),
            loggedNutriments =
            listOf(
                NutrimentUiLogModel(
                    nutrition = NutritionUiModel(
                        name = "Peach"
                    ),
                    quantity = 123.4,
                    unit = "g",
                    timeStamp = 1681801313
                ),
                NutrimentUiLogModel(
                    nutrition = NutritionUiModel(
                        name = "Apple"
                    ),
                    quantity = 123.4,
                    unit = "g",
                    timeStamp = 1681801313
                ),
                NutrimentUiLogModel(
                    nutrition = NutritionUiModel(
                        name = "Chocolate"
                    ),
                    quantity = 123.4,
                    unit = "g",
                    timeStamp = 1681801313
                )
            ),
            isEditMode = false,
            onEditMode = {},
            onAdd = {},
            onClickBoxClick = {},
            navController = rememberNavController(),
            selectedNutriment = NutritionUiModel(),
            onNutrimentSelected = {},
            onLoggedNutrimentSelected = {},
            clear = {})
    }
}

@Preview(device = "spec:width=1280dp,height=800dp,dpi=240,orientation=landscape")
@Composable
fun NutrimentLogScreen_EditMode_LightMode() {
    HannAppTheme {
        NutrimentLogContent(
            modifier = Modifier,
            uiState = NutritionUiState(),
            pagingItems = flowOf(PagingData.from(listOf(NutritionUiModel()))).collectAsLazyPagingItems(),
            loggedNutriments =
            listOf(
                NutrimentUiLogModel(
                    nutrition = NutritionUiModel(
                        name = "Peach"
                    ),
                    quantity = 123.4,
                    unit = "g",
                    timeStamp = 1681801313
                ),
                NutrimentUiLogModel(
                    nutrition = NutritionUiModel(
                        name = "Apple"
                    ),
                    quantity = 123.4,
                    unit = "g",
                    timeStamp = 1681801313
                ),
                NutrimentUiLogModel(
                    nutrition = NutritionUiModel(
                        name = "Chocolate"
                    ),
                    quantity = 123.4,
                    unit = "g",
                    timeStamp = 1681801313
                )
            ),
            isEditMode = true,
            onEditMode = {},
            onAdd = {},
            onClickBoxClick = {},
            navController = rememberNavController(),
            selectedNutriment = NutritionUiModel(),
            onNutrimentSelected = {},
            onLoggedNutrimentSelected = {},
            clear = {})
    }
}
